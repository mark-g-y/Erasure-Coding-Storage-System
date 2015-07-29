package com.distributedcoldstorage.utility;

public class ArrayUtility {
    
    public static void subtractArrays(int[] first, int[] second, int[] result) {
        for (int i = 0; i < first.length; i++) {
            result[i] = first[i] - second[i];
        }
    }

    public static void addArrays(int[] first, int[] second, int[] result) {
        for (int i = 0; i < first.length; i++) {
            result[i] = first[i] + second[i];
        }
    }
    
    public static int[] convertToIntArray(byte[] input) {
        int[] ret = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            ret[i] = input[i];
        }
        return ret;
    }
    
    public static byte[] convertToByteArray(int[] input) {
        byte[] ret = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            ret[i] = (byte) input[i];
        }
        return ret;
    }
}