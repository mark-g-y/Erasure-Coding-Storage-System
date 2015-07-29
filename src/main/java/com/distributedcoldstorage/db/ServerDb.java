package com.distributedcoldstorage.db;

import com.mongodb.DB;

public class ServerDb {

	private static ServerDb serverDb;

	private DB mongoDb;

	public ServerDb() {
		mongoDb = MongoDbManager.getInstance().getDB();
	}

	public static ServerDb getInstance() {
		if (serverDb == null) {
			serverDb = new ServerDb();
		}
		return serverDb;
	}
}