package com.distributedcoldstorage.servlets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

public class StorageSlaveServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getHeader("id");
        byte[] imageBytes;
        boolean fileError;
        try {
            FileInputStream fis = new FileInputStream(new File(id));
            imageBytes = IOUtils.toByteArray(fis);
            fis.close();
            fileError = false;
        } catch (IOException e) {
            imageBytes = new byte[0];
            fileError = true;
        }
        response.setHeader("Content-Length", Integer.toString(imageBytes.length));
        response.setHeader("File-Error", Boolean.toString(fileError));
        response.getOutputStream().write(imageBytes);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        InputStream is = request.getInputStream();
        byte[] bytes = IOUtils.toByteArray(is);
        String id = request.getHeader("id");
        FileOutputStream fos = new FileOutputStream(new File(id));
        fos.write(bytes);
        fos.close();
        response.getWriter().write("");
    }
}