package com.distributedcoldstorage.db;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import com.distributedcoldstorage.config.Configuration;

public class MongoDbManager {

    private static MongoDbManager mongoDbManager;

    private static final String HOSTNAME = Configuration.getInstance().getMongoDbConfig().HOSTNAME;
    private static final int PORT = Configuration.getInstance().getMongoDbConfig().PORT;
    private static final String DB_NAME = Configuration.getInstance().getMongoDbConfig().DB;

    private MongoClient mongoClient;
    private DB db;

    public MongoDbManager() {
        mongoClient = new MongoClient(HOSTNAME, PORT);
        db = mongoClient.getDB(DB_NAME);
    }

    public DB getDB() {
        return db;
    }

    public static MongoDbManager getInstance() {
        if (mongoDbManager == null) {
            mongoDbManager = new MongoDbManager();
        }
        return mongoDbManager;
    }
}