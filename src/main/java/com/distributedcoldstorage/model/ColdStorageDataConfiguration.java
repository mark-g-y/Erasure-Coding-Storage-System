package com.distributedcoldstorage.model;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class ColdStorageDataConfiguration {

    private String id;
    private String[] serverAddresses;
    private String[] serverGroupings;
    private int dataByteLength;

    public ColdStorageDataConfiguration(DBObject coldStorageDataConfiguration) {
        id = coldStorageDataConfiguration.get("_id").toString();
        dataByteLength = (Integer)coldStorageDataConfiguration.get("length");

        BasicDBList serverData = (BasicDBList)coldStorageDataConfiguration.get("servers");
        serverAddresses = new String[serverData.size()];
        serverGroupings = new String[serverData.size()];
        for (int i = 0; i < serverData.size(); i++) {
            BasicDBObject currentServerData = (BasicDBObject)serverData.get(i);
            serverAddresses[i] = (String)currentServerData.get("address");
            serverGroupings[i] = (String)currentServerData.get("data_group_assignment");
        }
    }

    public String getId() {
        return id;
    }

    public String[] getServerAddresses() {
        return serverAddresses;
    }

    public String[] getServerGroupings() {
        return serverGroupings;
    }

    public int getDataByteLength() {
        return dataByteLength;
    }

}