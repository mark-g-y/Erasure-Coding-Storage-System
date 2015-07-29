package com.distributedcoldstorage.model;

import java.util.ArrayList;

public class ColdStorageData {

    private byte[] resultingData;
    private int[][] serverGroupedData;
    private ArrayList<Integer> missingValues;
    private ArrayList<Integer> missingCombos;
    private int status;

    public ColdStorageData(byte[] resultingData, int[][] serverGroupedData, ArrayList<Integer> missingValues, ArrayList<Integer> missingCombos) {
        this.resultingData = resultingData;
        this.serverGroupedData = serverGroupedData;
        this.missingValues = missingValues;
        this.missingCombos = missingCombos;
        this.status = missingValues.size() != 0 || missingCombos.size() != 0 ? Status.NEEDS_REPAIR : Status.OK;
    }

    public byte[] getData() {
        return resultingData;
    }

    public int[][] getServerGroupedData() {
        return serverGroupedData;
    }

    public ArrayList<Integer> getMissingValues() {
        return missingValues;
    }

    public ArrayList<Integer> getMissingCombos() {
        return missingCombos;
    }

    public int getStatus() {
        return status;
    }

    public static class Status {
        public static final int OK = 0;
        public static final int NEEDS_REPAIR = 1;
    }
}