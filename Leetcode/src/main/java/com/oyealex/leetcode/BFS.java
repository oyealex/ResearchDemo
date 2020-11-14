/*
 * Copyright (c) 2020. oyealex. All rights reserved.
 */

package com.oyealex.leetcode;

import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * 广度优先遍历
 *
 * @author oyealex
 * @version 1.0
 * @since 2020/11/4
 */
public class BFS extends TestBase {
    // https://leetcode-cn.com/problems/minesweeper/
    @Test
    public void no_529_minesweeper() {
        assertArrayEquals(toCharMatrix("B1E1BB1M1BB111BBBBBB", 4, 5),
                          updateBoard(toCharMatrix("EEEEEEEMEEEEEEEEEEEE", 4, 5), new int[]{3, 0}));
        assertArrayEquals(toCharMatrix("B1E1BB1X1BB111BBBBBB", 4, 5),
                          updateBoard(toCharMatrix("B1E1BB1M1BB111BBBBBB", 4, 5), new int[]{1, 2}));
    }

    char[][] updateBoard(char[][] board, int[] click) {
        int rowBound = board.length;
        int colBound = board[0].length;

        if (board[click[0]][click[1]] == 'M') {
            board[click[0]][click[1]] = 'X';
            return board;
        }

        Queue<int[]> queue = new LinkedList<>();

        queue.offer(click);
        while (!queue.isEmpty()) {
            int[] cp = queue.poll();
            char cpSymbol = board[cp[0]][cp[1]];
            if (cpSymbol == 'E') {
                char revealedSymbol = countMines(board, cp);
                board[cp[0]][cp[1]] = revealedSymbol;
                if (revealedSymbol != 'B') {
                    continue;
                }
            } else {
                continue;
            }
            for (int[] np : getAroundPoint8(cp[0], cp[1], rowBound, colBound)) {
                char npSymbol = board[np[0]][np[1]];
                if (npSymbol == 'E') {
                    queue.offer(np);
                }
            }
        }

        return board;
    }

    private char countMines(char[][] board, int[] point) {
        int rowBound = board.length;
        int colBound = board[0].length;
        int row = point[0];
        int col = point[1];
        int count = 0;
        for (int[] np : getAroundPoint8(row, col, rowBound, colBound)) {
            if ("MX".indexOf(board[np[0]][np[1]]) != -1) {
                count++;
            }
        }
        return count == 0 ? 'B' : (char) (count + '0');
    }
}
