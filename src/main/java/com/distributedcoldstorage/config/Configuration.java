package com.distributedcoldstorage.config;

import java.io.File;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Configuration {

    public static final String CONFIG_FILE_NAME = "config";

    private static Configuration configuration = new Configuration();
    private MongoDbConfig mongoDbConfig;
    private StorageWriteConfig storageWriteConfig;

    public Configuration() {
        try {
            Scanner sc = new Scanner(new File(CONFIG_FILE_NAME));
            sc.useDelimiter("\\Z");
            JSONObject fileContent = new JSONObject(sc.next());
            sc.close();
            parseConfiguration(fileContent);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void parseConfiguration(JSONObject config) throws JSONException {
        JSONObject mongoConfig = config.getJSONObject("mongodb");
        mongoDbConfig = new MongoDbConfig(mongoConfig.getString("hostname"), mongoConfig.getInt("port"), mongoConfig.getString("db"));

        JSONArray storageConfigs = config.getJSONArray("storage_servers");
        int length = storageConfigs.length() % 2 == 0 ? storageConfigs.length() : storageConfigs.length() - 1;
        String[] serverAddresses = new String[length];
        for (int i = 0; i < serverAddresses.length; i++) {
            serverAddresses[i] = storageConfigs.getString(i);
        }
        storageWriteConfig = new StorageWriteConfig(serverAddresses);
    }

    public MongoDbConfig getMongoDbConfig() {
        return mongoDbConfig;
    }

    public StorageWriteConfig getStorageWriteConfig() {
        return storageWriteConfig;
    }

    public static Configuration getInstance() {
        return configuration;
    }

}