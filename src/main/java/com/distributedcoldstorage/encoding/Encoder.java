package com.distributedcoldstorage.encoding;

import java.util.ArrayList;

import com.distributedcoldstorage.config.Configuration;
import com.distributedcoldstorage.config.StorageWriteConfig;
import com.distributedcoldstorage.utility.ArrayUtility;

public class Encoder {

    private static final ArrayList<ArrayList<Integer>> combos = generateCombos();

    public static ArrayList<int[]> encode(int[][] inputs) {
        
        ArrayList<int[]> encoded = new ArrayList<int[]>();
        
        for (int r = 0; r < combos.size(); r++) {
            int[] results = encodeOneCombo(inputs, combos.get(r));
            encoded.add(results);
        }
        
        return encoded;
    }
    
    public static int[] encodeOneCombo(int[][] inputs, ArrayList<Integer> comboRow) {
        int[] results = new int[inputs[0].length];
        for (int c = 0; c < comboRow.size(); c++) {
            ArrayUtility.addArrays(results, inputs[comboRow.get(c)], results);
        }
        return results;
    }

    public static int[] encodeOneCombo(int[][] inputs, String combo) {
        int[] results = new int[inputs[0].length];
        String[] comboRow = combo.split(",");
        for (int c = 0; c < comboRow.length; c++) {
            ArrayUtility.addArrays(results, inputs[Integer.parseInt(comboRow[c])], results);
        }
        return results;
    }

    private static ArrayList<ArrayList<Integer>> generateCombos() {
        int numPerCombo = 2;
        ArrayList<ArrayList<Integer>> combos = new ArrayList<ArrayList<Integer>>();
        StorageWriteConfig storageWriteConfig = Configuration.getInstance().getStorageWriteConfig();
        for (int i = 0; i < storageWriteConfig.NUM_GROUPS; i++) {
            ArrayList<Integer> combo = new ArrayList<Integer>();
            for (int m = 0; m < numPerCombo; m++) {
                if (i + m >= storageWriteConfig.NUM_GROUPS) {
                    combo.add(i + m - storageWriteConfig.NUM_GROUPS);
                } else {
                    combo.add(i + m);
                }
            }
            combos.add(combo);
        }
        return combos;
    }

    public static String[] generateGroupedInputsServerAssignments(int numGroups, int numComboGroups) {
        String[] numbering = new String[numGroups + numComboGroups];
        for (int i = 0; i < numGroups; i++) {
            numbering[i] = Integer.toString(i);
        }
        for (int i = 0; i < numComboGroups; i++) {
            numbering[i + numGroups] = combos.get(i).get(0) + "," + combos.get(i).get(1);
        }
        return numbering;
    }

    public static ArrayList<ArrayList<Integer>> getCombos() {
        return combos;
    }

    public static int[][] generateGroupedInputs(byte[] input) {
        return generateGroupedInputs(ArrayUtility.convertToIntArray(input));
    }
    
    public static int[][] generateGroupedInputs(int[] input) {
        StorageWriteConfig storageWriteConfig = Configuration.getInstance().getStorageWriteConfig();
        int[][] inputs = new int[storageWriteConfig.NUM_GROUPS][(int)(Math.ceil(input.length * 1.0 / storageWriteConfig.NUM_GROUPS))];
        
        int inputIndex = 0;
        for (int r = 0; r < inputs.length; r++) {
            for (int c = 0; c < inputs[r].length; c++) {
                if (inputIndex < input.length) { 
                    inputs[r][c] = input[inputIndex];
                    inputIndex++;
                } else {
                    break;
                }
            }
        }

        return inputs;
    }
}