package com.distributedcoldstorage.httpcommunication;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.distributedcoldstorage.config.Configuration;
import com.distributedcoldstorage.config.ResponseCodes;
import com.distributedcoldstorage.config.StorageConfig;
import com.distributedcoldstorage.config.StorageWriteConfig;
import com.distributedcoldstorage.db.StorageDataDb;
import com.distributedcoldstorage.encoding.Decoder;
import com.distributedcoldstorage.encoding.Encoder;
import com.distributedcoldstorage.model.ColdStorageData;
import com.distributedcoldstorage.model.ColdStorageDataConfiguration;
import com.distributedcoldstorage.utility.ArrayUtility;

public class ColdStorageAccessor {

    public static ColdStorageData getDataFromColdStorage(final String storageId) {
        // get item from each server
        final ConcurrentHashMap<String, int[]> retrievedData = new ConcurrentHashMap<String, int[]>();
        final ColdStorageDataConfiguration coldStorageDataConfiguration = StorageDataDb.getInstance().get(storageId);
        if (coldStorageDataConfiguration == null) {
            return null;
        }
        final int length = coldStorageDataConfiguration.getDataByteLength();
        final String[] serverAddresses = coldStorageDataConfiguration.getServerAddresses();
        final String[] serverGroupings = coldStorageDataConfiguration.getServerGroupings();
        final Thread[] getGroupFromColdStorageThreads = new Thread[serverAddresses.length];
        for (int i = 0; i < serverAddresses.length; i++) {
            final int index = i;
            getGroupFromColdStorageThreads[index] = new Thread() {
                @Override
                public void run() {
                    //<TODO> remove serverGrouping id when not testing on same server
                    int[] storedData = getGroupFromColdStorage(storageId + serverGroupings[index], serverAddresses[index] + StorageConfig.STORAGE_SLAVE_PATH);
                    try {
                        retrievedData.put(serverGroupings[index], storedData);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            getGroupFromColdStorageThreads[i].start();
        }
        for (Thread t : getGroupFromColdStorageThreads) {
            try {
                t.join();
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
        ArrayList<Integer> missingValues = new ArrayList<Integer>();
        ArrayList<int[]> values = new ArrayList<int[]>();
        for (int i = 0; i < serverAddresses.length / 2; i++) {
            int[] intArray = retrievedData.get(serverGroupings[i]);
            values.add(intArray);
            if (intArray == null) {
                missingValues.add(i);
            }
        }
        ArrayList<Integer> missingCombos = new ArrayList<Integer>();
        ArrayList<int[]> encoded = new ArrayList<int[]>();
        for (int i = serverAddresses.length / 2; i < serverGroupings.length; i++) {
            int[] intArray = retrievedData.get(serverGroupings[i]);
            encoded.add(intArray);
            if (intArray == null) {
                missingCombos.add(i);
            }
        }
        // if too many missing pieces, we cannot reconstruct, and set the resulting 2D array to null. Otherwise, decode missing pieces
        int[][] inputsReversed = null;
        byte[] result = null;
        if (missingCombos.size() + missingValues.size() > serverGroupings.length / 2 || missingValues.size() >= serverGroupings.length / 2) {
            // <TODO> implement system of equations to allow ability to solve edge case of missing values
            inputsReversed = null;
        } else {
            inputsReversed = Decoder.decode(serverAddresses.length / 2, (int)(Math.ceil(length * 1.0 / serverAddresses.length)), serverGroupings, values, encoded);
            // for debugging, we return a string so it's easy to print. Otherwise we will return a byte array
            // because this allows a wide variety of files to be stored
            result = Decoder.decodeIntArray(inputsReversed, length);
        }
        
        return new ColdStorageData(result, inputsReversed, missingValues, missingCombos);
    }
    
    private static int[] getGroupFromColdStorage(String storageId, String url) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        
        httpGet.addHeader("id", storageId);
        
        try {
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (Boolean.parseBoolean(response.getFirstHeader("File-Error").getValue())) {
                throw new IOException("Error retrieving file from cold storage");
            }
            byte[] bytes = EntityUtils.toByteArray(entity);

            return ArrayUtility.convertToIntArray(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
        
    }

    public static String sendToColdStorage(byte[] data) {

        final StorageWriteConfig storageConfig = Configuration.getInstance().getStorageWriteConfig();
        final int length = data.length;
        final int[][] inputs = Encoder.generateGroupedInputs(data);
        final ArrayList<int[]> encoded = Encoder.encode(inputs);
        final String[] numbering = Encoder.generateGroupedInputsServerAssignments(inputs.length, encoded.size());
        final String storageId = StorageDataDb.getInstance().add(numbering, length);

        Thread[] sendGroupForColdStorageThreads = new Thread[inputs.length + encoded.size()];
        for (int i = 0; i < inputs.length; i++) {
            final int index = i;
            sendGroupForColdStorageThreads[i] = new Thread() {
                @Override
                public void run() {
                    try {
                        sendGroupForColdStorage(storageId, Integer.toString(index), inputs[index], storageConfig.SERVER_ADDRESSES[index] + StorageConfig.STORAGE_SLAVE_PATH);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            sendGroupForColdStorageThreads[i].start();
        }
        for (int i = 0; i < encoded.size(); i++) {
            // server num will be staggered, since we already used some servers above to store inputs
            final int serverNum = i + inputs.length;
            final int index = i;
            sendGroupForColdStorageThreads[serverNum] = new Thread() {
                @Override
                public void run() {
                    String id = Encoder.getCombos().get(index).get(0) + "," + Encoder.getCombos().get(index).get(1);
                    try {
                        sendGroupForColdStorage(storageId, id, encoded.get(index), storageConfig.SERVER_ADDRESSES[serverNum] + StorageConfig.STORAGE_SLAVE_PATH);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            sendGroupForColdStorageThreads[serverNum].start();
        }
        for (Thread t : sendGroupForColdStorageThreads) {
            try {
                t.join();
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
        return storageId;
    }
    
    private static void sendGroupForColdStorage(String storageId, String serverNum, int[] input, String url) throws JSONException, ClientProtocolException, IOException {

        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        //<TODO> debug only - id is storageId + serverNum. Would normally just be storageId, but we only have 1 test server, so need to make it unique
        httpPost.addHeader("id", storageId + serverNum);
        httpPost.addHeader("Content-Type", "application/json");

        httpPost.setEntity(new ByteArrayEntity(ArrayUtility.convertToByteArray(input)));

        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            InputStream instream = entity.getContent();
            try {
                List<Object> output = IOUtils.readLines(instream);
                for (Object out : output) {
                    System.out.println(out);
                }
            } finally {
                instream.close();
            }
        }
    }

    public static int repairDataInColdStorage(final String storageId) {
        ColdStorageDataConfiguration coldStorageDataConfiguration = StorageDataDb.getInstance().get(storageId);
        if (coldStorageDataConfiguration == null) {
            return ResponseCodes.NON_EXISTENT_FILE_ID;
        }
        final String[] serverAddresses = coldStorageDataConfiguration.getServerAddresses();
        final String[] serverGroupings = coldStorageDataConfiguration.getServerGroupings();
        // we should repair the missing stuff here
        final ColdStorageData data = getDataFromColdStorage(storageId);
        final int[][]inputsReversed = data.getServerGroupedData();
        final ArrayList<Integer> missingValues = data.getMissingValues();
        final ArrayList<Integer> missingCombos = data.getMissingCombos();

        // too broken to repair
        if (inputsReversed == null) {
            return ResponseCodes.CANNOT_RECONSTRUCT;
        }

        Thread[] repairDataThreads = new Thread[missingValues.size() + missingCombos.size()];
        for (int i = 0; i < missingValues.size(); i++) {
            final int index = i;
            repairDataThreads[i] = new Thread() {
                @Override
                public void run() {
                    try {
                        sendGroupForColdStorage(storageId, Integer.toString(missingValues.get(index)), inputsReversed[missingValues.get(index)], serverAddresses[missingValues.get(index)] + StorageConfig.STORAGE_SLAVE_PATH);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            repairDataThreads[i].start();
        }
        for (int i = 0; i < missingCombos.size(); i++) {
            final int index = i;
            repairDataThreads[missingValues.size() + i] = new Thread() {
                @Override
                public void run() {
                    try {
                        int missingIndex = missingCombos.get(index);
                        sendGroupForColdStorage(storageId, serverGroupings[missingIndex], Encoder.encodeOneCombo(inputsReversed, serverGroupings[missingIndex]), serverAddresses[missingIndex] + StorageConfig.STORAGE_SLAVE_PATH);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            repairDataThreads[missingValues.size() + i].start();
        }

        for (Thread t : repairDataThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return ResponseCodes.OK;
    }
}