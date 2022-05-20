package com.example.sudoku;

import android.util.Log;

import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;
import java.util.UUID;

class Sudoku {
    private int[][] board = new int[9][9];
    private final int[][] solvedBoard;
    private final int[][] unsolvedBoard;
    private final ArrayList<Integer> arrOfValidNumbers = new ArrayList<Integer>(9);
    public ArrayList<Cell> emptyBoxes = new ArrayList<>();
    private int solutions;
    private final Difficulty difficulty;
    // the id is used to keep track of the specific sudoku and be able to update its data
    // Sudoku.toString() would've given two different values even if the two sudoku instances had exact the same properties
    public final UUID id;

    // this constructor is called if we want to create a new sudoku of a specified difficulty
    Sudoku(Difficulty difficulty) {
        this.difficulty = difficulty;
        id = UUID.randomUUID();
        fillArrayListWithValidNumbers();
        fillBoard();
        solvedBoard = Utility.deepCopyArray(board);
        removeCellsFromBoard();
        unsolvedBoard = Utility.deepCopyArray(board);
    }

    // this constructor is called when we want to create a sudoku from parsed data from shared preferences
    // (a sudoku that has been previously created)
    public Sudoku(UUID id, Difficulty difficulty, int[][] board, int[][] unsolvedBoard,int[][] solvedBoard) {
        this.id = id;
        this.difficulty = difficulty;
        this.board = board;
        this.solvedBoard = solvedBoard;
        this.unsolvedBoard = unsolvedBoard;
        findAndAddEmptyBoxes();
        Utility.parseBoard(solvedBoard);
    }

    // loops through the unsolved board and registers cells with the value 0
    private void findAndAddEmptyBoxes() {
        for(int r = 0; r<9; r++){
            for(int c=0; c<9; c++){
                if(unsolvedBoard[r][c] == 0){
                    emptyBoxes.add(new Cell(r, c));
                }
            }
        }
    }

    // filling the arraylist arrOfValidNumbers with numbers from 1-9 (valid sudoku numbers)
    private void fillArrayListWithValidNumbers() {
        for (int i = 1; i <= 9; i++) {
            arrOfValidNumbers.add(i);
        }
    }

    // the sudoku board is empty by default, in the method below, we use backtracking to fill the sudoku with a valid solution
    private boolean fillBoard() {
        int row = 0, col = 0;

        for (int i = 0; i < 81; i++) {
            row = (int) Math.floor((float)i / 9);
            col = i % 9;
            // if cell is empty
            if (board[row][col] == 0) {
                Collections.shuffle(arrOfValidNumbers);
                // check if the value exists in row, col and square
                for (int j = 0; j < arrOfValidNumbers.size(); j++) {
                    int value = arrOfValidNumbers.get(j);
                    if (valueIsValid(new Cell(row, col), value)) {
                        board[row][col] = value;
                        if (checkIfBoardIsFull()) {
                            return true;
                        } else {
                            if (fillBoard()) {
                                return true;
                            }
                        }
                    }
                }
                break;
            }
        }
        board[row][col] = 0;
        return false;
    }

    // checks if all cells have been filled
    private boolean checkIfBoardIsFull() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == 0) return false;
            }
        }

        return true;
    }

    // after the sudoku is solved and full, we remove cells one by one until we end up with one unique solution
    // the difficulty of the sudoku is used to decide how many tries we can try without ending up with multiple solutions to the sudoku
    private void removeCellsFromBoard() {
        // enum.ordinal() returns the order of the constant it is in (its index)
        int solvingAttempts = difficulty.ordinal() + 1;

        while (solvingAttempts > 0) {
            // random cells
            int row = (int) Math.floor(Math.random() * 9);
            int col = (int) Math.floor(Math.random() * 9);

            // find non-zero cells only (valid ones)
            while (board[row][col] == 0) {
                row = (int) Math.floor(Math.random() * 9);
                col = (int) Math.floor(Math.random() * 9);
            }

            int backupValue = board[row][col];
            board[row][col] = 0;

            solutions = 0;
            tryToSolveAndCountSolutions();

            // multiple solutions, we put back the value we just removed and decrement attempts by one
            if (solutions != 1) {
                board[row][col] = backupValue;
                solvingAttempts -= 1;
            } else {
                board[row][col] = 0;
                emptyBoxes.add(new Cell(row, col));
            }
        }
    }

    // backtracking algorithm used to count how many solutions the board has
    // used in removeCellsFromBoard to check if the board still has one unique solution and not multiple
    // is very similar to fillBoard the only differnece is that we try a value one by one instead of shuffling and picking a random number
    private boolean tryToSolveAndCountSolutions() {
        int row = 0, col = 0;
        for (int i = 0; i < 81; i++) {
            row = (int) Math.floor((float)i / 9);
            col = i % 9;
            // if cell is empty
            if (board[row][col] == 0) {
                for (int value = 1; value < 10; value++) {
                    if (valueIsValid(new Cell(row, col), value)) {
                        board[row][col] = value;
                        if (checkIfBoardIsFull()) {
                            solutions += 1;
                            break;
                        } else {
                            if (tryToSolveAndCountSolutions()) {
                                return true;
                            }
                        }
                    }
                }
                break;
            }
        }
        board[row][col] = 0;
        return false;
    }

    /**
     * a value is valid when it doesn't already exist in same row, column or square
     * @param cell - the cell
     * @param value - the value you want to check its validity
     * @return {boolean} whether the value is valid or not
     */
    private boolean valueIsValid(Cell cell, int value) {
        int[] square = getSquare(getIndexOfSquare(cell));
        return (
                valueDoesNotExistInArray(board[cell.getRow()], value) &&
                        valueDoesNotExistInArray(getCol(cell.getColumn()), value) &&
                        valueDoesNotExistInArray(square, value)
        );
    }

    // returns the index of the square for the provided cell
    public int getIndexOfSquare(Cell cell) {
        int row = cell.getRow();
        int column = cell.getColumn();
        int square = 0;

        if (row <= 2) {
            if (column <= 2) {
                square = 0;
            } else if (column >= 3 && column <= 5) {
                square = 1;
            } else {
                square = 2;
            }
        } else if (row >= 3 && row <= 5) {
            if (column <= 2) {
                square = 3;
            } else if (column >= 3 && column <= 5) {
                square = 4;
            } else {
                square = 5;
            }
        } else {
            if (column <= 2) {
                square = 6;
            } else if (column >= 3 && column <= 5) {
                square = 7;
            } else {
                square = 8;
            }
        }

        return square;
    }

    public int[] getSquare(int whichSquare) {
        if (whichSquare > 8 || whichSquare < 0) throw new IllegalArgumentException(
                "The parameter 'whichSquare' should be within the range of 0 and 8!"
        );

        int[] square = new int[9];
        int indexesToIterateThrough = 0;
        int i = whichSquare;
        int indexesToCaptureStart = 0;
        int indexesToCaptureEnd = 0;

        switch (whichSquare) {
            case 0:
            case 3:
            case 6:
                indexesToIterateThrough = 3;
                i = whichSquare;
                indexesToCaptureStart = 0;
                indexesToCaptureEnd = 3;
                break;
            case 1:
            case 4:
            case 7:
                i = whichSquare - 1;
                indexesToIterateThrough = 2;
                indexesToCaptureStart = 3;
                indexesToCaptureEnd = 6;
                break;
            case 2:
            case 5:
            case 8:
                i = whichSquare - 2;
                indexesToIterateThrough = 1;
                indexesToCaptureStart = 6;
                indexesToCaptureEnd = 9;
                break;
        }

        int index = 0;
        for (; i < whichSquare + indexesToIterateThrough; i++) {
            for (int j = indexesToCaptureStart; j < indexesToCaptureEnd; j++) {
                square[index] = board[i][j];
                index++;
            }
        }

        return square;
    }

    public int[] getCol(int col) {
        if (col > 8 || col < 0) throw new IllegalArgumentException(
                "The parameter 'col' should be within the range of 0 and 8!"
        );

        int[] column = new int[9];
        for (int i = 0; i < board.length; i++) {
            column[i] = board[i][col];
        }

        return column;
    }

    private boolean valueDoesNotExistInArray(int[] arr, int value) {
        for (int j : arr) {
            if (value == j) return false;
        }

        return true;
    }

    public int[][] getBoard(){
        return board;
    }

    public int[][] getSolvedBoard(){
        return solvedBoard;
    }

    public ArrayList<Cell> getEmptyBoxes(){
        return emptyBoxes;
    }

    public boolean isSolved(){
        for(int r = 0; r<9; r++){
            for(int c = 0; c<9; c++){
                if(board[r][c] != solvedBoard[r][c]){
                    return false;
                }
            }
        }
        return true;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int[][] getUnsolvedBoard() {
        return this.unsolvedBoard;
    }

    public boolean isAllEmptyBoxesFilled() {
        for(Cell emptyBox : emptyBoxes){
            // if one box is empty then return false
            if(board[emptyBox.getRow()][emptyBox.getColumn()] == 0){
                return false;
            }
        }

        return true;
    }
}
