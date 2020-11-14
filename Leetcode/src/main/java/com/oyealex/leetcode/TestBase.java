/*
 * Copyright (c) 2020. oyealex. All rights reserved.
 */

package com.oyealex.leetcode;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 算法测试用例基础辅助类
 *
 * @author oyealex
 * @version 1.0
 * @since 2020/10/12
 */
public abstract class TestBase {
    /** 8方向的行坐标变化量 */
    protected static final int[] DELTA_ROW_8 = new int[]{-1, -1, 0, 1, 1, 1, 0, -1};
    /** 8方向的列坐标变化量 */
    protected static final int[] DELTA_COL_8 = new int[]{0, 1, 1, 1, 0, -1, -1, -1};

    protected static List<int[]> getAroundPoint4(int row, int col, int rowBound, int colBound) {
        List<int[]> result = new ArrayList<>(4);
        for (int index = 0; index < 8; index += 2) {
            int npRow = row + DELTA_ROW_8[index];
            int npCol = col + DELTA_COL_8[index];
            if (0 <= npRow && npRow < rowBound && 0 <= npCol && npCol < colBound) {
                result.add(new int[]{npRow, npCol});
            }
        }
        return result;
    }

    protected static List<int[]> getAroundPoint8(int row, int col, int rowBound, int colBound) {
        List<int[]> result = new ArrayList<>(8);
        for (int index = 0; index < 8; index++) {
            int npRow = row + DELTA_ROW_8[index];
            int npCol = col + DELTA_COL_8[index];
            if (0 <= npRow && npRow < rowBound && 0 <= npCol && npCol < colBound) {
                result.add(new int[]{npRow, npCol});
            }
        }
        return result;
    }

    protected static String prettyArray(Object[] array, String delimiter) {
        return prettyArray(array, delimiter, "", "");
    }

    protected static String prettyArray(Object[] array, String delimiter, String prefix, String suffix) {
        if (array == null) {
            return "";
        }
        int maxIndex = array.length - 1;
        if (maxIndex == -1) {
            return prefix + suffix;
        }
        StringBuilder builder = new StringBuilder(prefix);
        for (int index = 0; ; index++) {
            builder.append(nullToEmpty(array[index]));
            if (index == maxIndex) {
                return builder.append(suffix).toString();
            }
            builder.append(delimiter);
        }
    }

    protected static String prettyArray(char[] array, String delimiter, String prefix, String suffix) {
        if (array == null) {
            return "";
        }
        int maxIndex = array.length - 1;
        if (maxIndex == -1) {
            return prefix + suffix;
        }
        StringBuilder builder = new StringBuilder(prefix);
        for (int index = 0; ; index++) {
            builder.append(nullToEmpty(array[index]));
            if (index == maxIndex) {
                return builder.append(suffix).toString();
            }
            builder.append(delimiter);
        }
    }

    protected static String prettyMatrix(Object[][] matrix, String delimiter) {
        return prettyMatrix(matrix, delimiter, "", "");
    }

    protected static String prettyMatrix(char[][] matrix, String delimiter) {
        return prettyMatrix(matrix, delimiter, "", "");
    }

    protected static String prettyMatrix(Object[][] matrix, String delimiter, String prefix, String suffix) {
        if (matrix == null || matrix.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Object[] oneRow : matrix) {
            builder.append(prettyArray(oneRow, delimiter, prefix, suffix)).append("\n");
        }
        return builder.toString();
    }

    protected static String prettyMatrix(char[][] matrix, String delimiter, String prefix, String suffix) {
        if (matrix == null || matrix.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (char[] oneRow : matrix) {
            builder.append(prettyArray(oneRow, delimiter, prefix, suffix)).append("\n");
        }
        return builder.toString();
    }

    protected static String nullToEmpty(Object value) {
        return value == null ? "" : value.toString();
    }

    protected static String[][] toStringMatrix(String value, int row, int col) {
        String[][] matrix = new String[row][col];
        int index = 0;
        for (int rowIndex = 0; rowIndex < row; rowIndex++) {
            for (int colIndex = 0; colIndex < col; colIndex++) {
                matrix[rowIndex][colIndex] = String.valueOf(value.charAt(index++));
            }
        }
        return matrix;
    }

    protected static char[][] toCharMatrix(String value, int row, int col) {
        char[][] matrix = new char[row][col];
        int index = 0;
        for (int rowIndex = 0; rowIndex < row; rowIndex++) {
            for (int colIndex = 0; colIndex < col; colIndex++) {
                matrix[rowIndex][colIndex] = value.charAt(index++);
            }
        }
        return matrix;
    }

    @EqualsAndHashCode
    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    protected static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode(int val) {
            this(val, null, null);
        }

        @Override
        public String toString() {
            return "(" + val + ":" + nullToEmpty(left) + "," + nullToEmpty(right) + ")";
        }
    }
}
