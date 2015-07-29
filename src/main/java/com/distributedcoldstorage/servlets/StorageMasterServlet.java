package com.distributedcoldstorage.servlets;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import org.json.JSONException;
import org.json.JSONObject;

import com.distributedcoldstorage.config.ResponseCodes;
import com.distributedcoldstorage.encoding.Encoder;
import com.distributedcoldstorage.httpcommunication.ColdStorageAccessor;
import com.distributedcoldstorage.model.ColdStorageData;

public class StorageMasterServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String storageId = request.getParameter("id");
        
        ColdStorageData result = ColdStorageAccessor.getDataFromColdStorage(storageId);
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        if (result == null) {
            response.setHeader("status", Integer.toString(ResponseCodes.NON_EXISTENT_FILE_ID));
        } else if (result.getData() == null) {
            response.setHeader("status", Integer.toString(ResponseCodes.CANNOT_RECONSTRUCT));
        } else {
            int status = result.getStatus() == ColdStorageData.Status.NEEDS_REPAIR ? ResponseCodes.FILE_NEEDS_RECONSTRUCTION : ResponseCodes.OK;
            response.setHeader("status", Integer.toString(status));
            response.getOutputStream().write(result.getData());
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        InputStream is = request.getInputStream();
        byte[] data = IOUtils.toByteArray(is);

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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
    	response.setContentType("application/json");
    	response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("status", Integer.toString(result));
    	response.getWriter().println(jsonResponse.toString());
    }
}