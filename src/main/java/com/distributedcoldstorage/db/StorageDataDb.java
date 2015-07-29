package com.distributedcoldstorage.db;

import org.bson.types.ObjectId;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import com.distributedcoldstorage.config.Configuration;
import com.distributedcoldstorage.config.StorageWriteConfig;
import com.distributedcoldstorage.model.ColdStorageDataConfiguration;

public class StorageDataDb {

    private static StorageDataDb storageDataDb;

    private static final String STORAGE_DATA_COLLECTION = "storageDataCollection";

    private DB mongoDb;
    private DBCollection storageDataCollection;
    private DBObject storageData;
    private StorageWriteConfig storageWriteConfig = Configuration.getInstance().getStorageWriteConfig();

    public StorageDataDb() {
        mongoDb = MongoDbManager.getInstance().getDB();
        storageDataCollection = mongoDb.getCollection(STORAGE_DATA_COLLECTION);
        storageData = storageDataCollection.findOne();
        if (storageData == null) {
            storageData = new BasicDBObject();
            storageDataCollection.insert(storageData);
        }
    }

    public String[] getStorageLocations(String key) {
        String[] foo = new String[1];
        foo[0] = (String)storageData.get(key);
        return foo;
    }

    public synchronized String add(String[] serverNumberings, int length) {
        BasicDBObject coldStorageData = new BasicDBObject();
        BasicDBList serverData = new BasicDBList();
        for (int i = 0; i < storageWriteConfig.SERVER_ADDRESSES.length; i++) {
            BasicDBObject serverInfo = new BasicDBObject();
            serverInfo.put("address", storageWriteConfig.SERVER_ADDRESSES[i]);
            serverInfo.put("data_group_assignment", serverNumberings[i]);
            serverData.add(serverInfo);
        }
        ObjectId id = new ObjectId();
        coldStorageData.put("_id", id);
        coldStorageData.put("servers", serverData);
        coldStorageData.put("length", length);
        storageData.put(id.toString(), coldStorageData);
        BasicDBObject setNewFieldQuery = new BasicDBObject().append("$set", storageData);
        storageDataCollection.update(new BasicDBObject().append("_id", storageData.get("_id")), setNewFieldQuery);
        return id.toString();
    }

    public synchronized ColdStorageDataConfiguration get(String id) {
        BasicDBObject coldStorageDataConfiguration = (BasicDBObject)storageData.get(id);
        if (coldStorageDataConfiguration == null) {
            return null;
        }
        return new ColdStorageDataConfiguration(coldStorageDataConfiguration);
    }

    public synchronized static StorageDataDb getInstance() {
        if (storageDataDb == null) {
            storageDataDb = new StorageDataDb();
        }
        return storageDataDb;
    }
}