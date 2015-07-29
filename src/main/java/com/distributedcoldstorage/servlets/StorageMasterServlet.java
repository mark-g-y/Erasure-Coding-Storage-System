package com.distributedcoldstorage.servlets;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.distributedcoldstorage.config.ResponseCodes;
import com.distributedcoldstorage.encoding.Encoder;
import com.distributedcoldstorage.httpcommunication.ColdStorageAccessor;
import com.distributedcoldstorage.model.ColdStorageData;

public class StorageMasterServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String storageId = request.getParameter("id");
        
        // for debugging, we set the result to a string so it's easy to print. Otherwise we will return a byte array
        // because this allows a wide variety of files to be stored
        ColdStorageData result = ColdStorageAccessor.getDataFromColdStorage(storageId);
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        if (result == null) {
            response.setHeader("status", Integer.toString(ResponseCodes.NON_EXISTENT_FILE_ID));
        } else {
            int status = result.getStatus() == ColdStorageData.Status.NEEDS_REPAIR ? ResponseCodes.FILE_NEEDS_RECONSTRUCTION : ResponseCodes.OK;
            response.setHeader("status", Integer.toString(status));
            response.getWriter().println(result.getData());
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //<TODO> replace with client data
        byte[] data = "testing 123".getBytes();

        String storageId = ColdStorageAccessor.sendToColdStorage(data);
        
        JSONObject jsonResponse = new JSONObject();
        try {
            jsonResponse.put("storageId", storageId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
    	response.setContentType("application/json");
    	response.setStatus(HttpServletResponse.SC_OK);
    	response.getWriter().print(jsonResponse.toString());
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String storageId = request.getParameter("id");

        int result = ColdStorageAccessor.repairDataInColdStorage(storageId);

        JSONObject jsonResponse = new JSONObject();
        try {
            jsonResponse.put("storageId", storageId);
            jsonResponse.put("result", result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
    	response.setContentType("application/json");
    	response.setStatus(HttpServletResponse.SC_OK);
    	response.getWriter().println(jsonResponse.toString());
    }
}