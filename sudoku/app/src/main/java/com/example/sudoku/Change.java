package com.example.sudoku;

/**
 * A Change class that represents one move made by the user.
 * Cell - the cell the change was applied to
 * prevValue - the value before the change was made
 * newValue - the new value after the change
 *
 * This class is used to track moves of the user in order to backtrack (undo button)
 */
public class Change {
    public final Cell cell;
    public final int prevValue;
    public final int newValue;

    public Change(Cell cell, int prevValue, int newValue){
        this.cell = cell;
        this.prevValue = prevValue;
        this.newValue = newValue;
    }
}
