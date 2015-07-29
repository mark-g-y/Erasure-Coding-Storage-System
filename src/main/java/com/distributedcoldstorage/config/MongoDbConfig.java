package com.distributedcoldstorage.config;

public class MongoDbConfig {

    public final String HOSTNAME;
    public final int PORT;
    public final String DB;

    public MongoDbConfig(String hostname, int port, String db) {
        HOSTNAME = hostname;
        PORT = port;
        DB = db;
    }

}