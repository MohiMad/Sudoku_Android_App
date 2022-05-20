package com.example.sudoku;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

class Utility {
    public static int[][] deepCopyArray(int[][] arr) {
        int[][] copyArr = new int[arr.length][arr.length];
        for (int r = 0; r < arr.length; r++) {
            for (int c = 0; c < arr.length; c++) {
                copyArr[r][c] = arr[r][c];
            }
        }

        return copyArr;
    }

    public static void parseBoard(int[][] board) {
        // a method that prints the board out in the console, used for debugging
        for (int i = 0; i < board.length; i++) {
            if (i % 3 == 0 && i != 0) {
                System.out.println("- - - - - - - - - - - - - ");
            }

            for (int j = 0; j < board[0].length; j++) {
                if (j % 3 == 0 && j != 0) {
                    System.out.print(" | ");
                }

                if (j == 8) {
                    System.out.println(board[i][j]);
                } else {
                    System.out.print(board[i][j] + " ");
                }
            }
        }
        System.out.println("________________________");
    }

    // takes the data saved in the shared preferences and parses it into a Sudoku instance
    public static Sudoku parseDataIntoSudoku(UUID uuid, String data) {
        int END_OF_SUDOKU = 81;
        int END_OF_UNSOLVED_SUDOKU = 162;
        int END_OF_SOLVED_SUDOKU = 243;

        int[][] board = parseStringArray(data.substring(0, END_OF_SUDOKU));
        int[][] unsolvedBoard = parseStringArray(data.substring(END_OF_SUDOKU, END_OF_UNSOLVED_SUDOKU));
        int[][] solvedBoard = parseStringArray(data.substring(END_OF_UNSOLVED_SUDOKU, END_OF_SOLVED_SUDOKU));
        Difficulty boardDifficulty;

        // the rest of the string is the difficulty
        boardDifficulty = Difficulty.getDifficulty(data.substring(END_OF_SOLVED_SUDOKU));

        if (boardDifficulty == null) {
            throw new IllegalArgumentException("Data does not have proper format");
        }

        return new Sudoku(uuid, boardDifficulty, board, unsolvedBoard, solvedBoard);
    }

    // saved a sudoku instance into the shared preferences
    public static void saveSudokuData(Sudoku sudoku, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("sudoku", 0);
        SharedPreferences.Editor editor = prefs.edit();

        // we only append numbers because after 81 chars, we know the solved board begins
        String data = stringifyDeepArray(sudoku.getBoard()) +
                stringifyDeepArray(sudoku.getUnsolvedBoard()) +
                stringifyDeepArray(sudoku.getSolvedBoard()) +
                sudoku.getDifficulty().getText();

        editor.putString(sudoku.id.toString(), data);
        editor.apply();
    }

    // turns a string of length 81 (and made of integers) into a 9x9 2D array
    public static int[][] parseStringArray(String str) {
        // if the string length isn't exactly 81, then it can't be transformed into 9x9 two dimensional array
        if (str.length() != 81) {
            throw new IllegalArgumentException("The string provided isn't of length 81");
        }

        int[][] arr = new int[9][9];

        for (int i = 0; i < 81; i++) {
            int r = (int) Math.floor((float) i / 9);
            int c = i % 9;
            arr[r][c] = Character.getNumericValue(str.charAt(i));
        }

        return arr;
    }


    // the contrast method of parseStringArray, turns an array of 9x9 into a string
    public static String stringifyDeepArray(int[][] arr) {
        if (arr.length != 9 || arr[0].length != 9) {
            throw new IllegalArgumentException("The array provided isn't a 9x9 array.");
        }

        StringBuilder str = new StringBuilder();

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                str.append(arr[r][c]);
            }
        }

        return str.toString();
    }
}
