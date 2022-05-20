package com.example.sudoku;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

/**
 * Generates a valid Cell inside a grid of 81
 * Serves as an Object where { row: Number, column: Number }
 */
public class Cell {
    private int row;
    private int column;
    private int value;

    Cell(int row, int column){
        this.row = row;
        this.column = column;
    }

    Cell(int row, int column, int value){
        this.row = row;
        this.column = column;
        this.value = value;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public int getValue() {
        return value;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public boolean rowEquals(int value){
        return row == value;
    }

    public boolean columnEquals(int value){
        return column == value;
    }

    @NonNull
    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        return String.format("{ row: %d, column: %d }", this.row, this.column);
    }

}
