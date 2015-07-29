package com.distributedcoldstorage.encoding;

import java.util.ArrayList;
import java.util.HashMap;

import com.distributedcoldstorage.config.Configuration;
import com.distributedcoldstorage.utility.ArrayUtility;

public class Decoder {

    public static int[][] decode(int numGroups, int sizeEachGroup, String[] serverGroupings, ArrayList<int[]> values, ArrayList<int[]> encoded) {
        int[][] inputs = new int[numGroups][sizeEachGroup];
        
        HashMap<Integer, int[]> groupNumToValues = new HashMap<Integer, int[]>();
        
        for (int i = 0; i < values.size(); i++) {
            groupNumToValues.put(i, values.get(i));
        }
        
        boolean canExit = false;
        while (!canExit) {
            canExit = true;
            for (int i = 0; i < numGroups; i++) {
                String[] grouping = serverGroupings[i + numGroups].split(",");
                int index1 = Integer.parseInt(grouping[0]);
                int index2 = Integer.parseInt(grouping[1]);
                int[] equationRightHandSide = encoded.get(i);
                if (equationRightHandSide == null) {
                    continue;
                }
                if (groupNumToValues.get(index1) != null && groupNumToValues.get(index2) != null) {
                    continue;
                }
                if (groupNumToValues.get(index1) == null && groupNumToValues.get(index2) == null) {
                    canExit = false;
                    continue;
                }
                int nonNullIndex = groupNumToValues.get(index1) != null ? index1 : index2;
                int nullIndex = index1 == nonNullIndex ? index2 : index1;
                int[] knownValues = groupNumToValues.get(nonNullIndex);
                int[] solution = new int[knownValues.length];
                ArrayUtility.subtractArrays(equationRightHandSide, knownValues, solution);
                groupNumToValues.put(nullIndex, solution);
                values.set(nullIndex, solution);
            }
        }
        
        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = values.get(i);
        }
        
        return inputs;
    }
    
    public static String decodeIntArray(int[][] inputsReversed, int length) {
        byte[] bytes = new byte[length];
        int index = 0;
        for (int r = 0; r < inputsReversed.length; r++) {
            for (int c = 0; c < inputsReversed[r].length; c++) {
                bytes[index] = (byte)inputsReversed[r][c];
                index++;
                if (index >= length) {
                    break;
                }
            }
        }
        return new String(bytes);
    }
}