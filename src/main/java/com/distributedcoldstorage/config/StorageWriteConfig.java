package com.distributedcoldstorage.config;

public class StorageWriteConfig {

    public final int NUM_SERVERS;
    public final int NUM_ALLOWED_FAIL;
    public final int NUM_GROUPS;

    public final String[] SERVER_ADDRESSES;

    public StorageWriteConfig(String[] serverAddresses) {
        SERVER_ADDRESSES = serverAddresses;
        NUM_SERVERS = SERVER_ADDRESSES.length;
        NUM_ALLOWED_FAIL = NUM_SERVERS / 2 - 1;
        NUM_GROUPS = NUM_SERVERS / 2;
    }

}