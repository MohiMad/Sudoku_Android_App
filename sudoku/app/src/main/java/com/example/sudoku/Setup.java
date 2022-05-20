package com.example.sudoku;

import android.widget.Toast;

import java.util.ArrayList;

public class Setup {
    private int selectedRow = -1;
    private int selectedColumn = -1;
    private int hintAttempts = 3;
    private BoardView boardView;
    // to be changed
    private final Sudoku sudoku;
    // board is the board the user can fill in
    private final int[][] board;
    // solvedBoard is the board with its solutions
    private final int[][] solvedBoard;
    // unsolvedBoard is the backup board of "board", to know which constants were from the beginning
    private final int[][] unsolvedBoard;
    // note array. array level 1 for row, level 2 for column and level 3 consists of either 0s or 1s as to indicate whether a number is noted on the cell
    private final int[][][] notes = new int[9][9][9];
    private boolean note = false;
    private final ArrayList<Change> lastChanges = new ArrayList<>();
    private Cell lastHintPosition = new Cell(-1, -1);

    Setup(Difficulty difficulty) {
        sudoku = new Sudoku(difficulty);
        board = sudoku.getBoard();
        solvedBoard = sudoku.getSolvedBoard();
        unsolvedBoard = Utility.deepCopyArray(board);
    }

    Setup(Sudoku sudoku) {
        this.sudoku = sudoku;
        this.board = sudoku.getBoard();
        this.solvedBoard = sudoku.getSolvedBoard();
        this.unsolvedBoard = sudoku.getUnsolvedBoard();
    }

    protected void setBoardView(BoardView boardView) {
        this.boardView = boardView;
    }

    protected Sudoku getSudoku() {
        return sudoku;
    }

    protected void setNumberInBoard(int number) {
        if (selectedCellIsNotNull() && numberInSelectedCellIsNotConstant()) {
            if (getValueInSelectedCell() == number) {
                setValueInSelectedCell(0);
            } else {
                setValueInSelectedCell(number);
            }
        }
    }

    private boolean numberInSelectedCellIsNotConstant() {
        for (Cell emptyBox : sudoku.getEmptyBoxes()) {
            if (emptyBox.rowEquals(selectedRow - 1) && emptyBox.columnEquals(selectedColumn - 1)) {
                return true;
            }
        }

        return false;
    }

    public int getValueInSelectedCell() {
        return board[selectedRow - 1][selectedColumn - 1];
    }

    private void setValueInSelectedCell(int number) {
        Cell cell = new Cell(selectedRow - 1, selectedColumn - 1);
        // we save the last change
        lastChanges.add(new Change(cell, board[selectedRow - 1][selectedColumn - 1], number));
        board[selectedRow - 1][selectedColumn - 1] = number;
    }

    public boolean selectedCellIsNotNull() {
        return selectedRow != -1 && selectedColumn != -1;
    }

    public boolean selectedCellIsNull() {
        return selectedRow == -1 && selectedColumn == -1;
    }

    // getters and setter below
    public int getSelectedRow() {
        return selectedRow;
    }

    public int getSelectedColumn() {
        return selectedColumn;
    }

    public void setSelectedColumn(int c) {
        selectedColumn = c;
    }

    public void setSelectedRow(int r) {
        selectedRow = r;
    }

    public int[][] getBoardFromSudokuInstance() {
        return board;
    }

    public ArrayList<Cell> getEmptyBoxesFromSudokuInstance() {
        return sudoku.getEmptyBoxes();
    }

    public int getHintAttempts() {
        return hintAttempts;
    }

    // the onClick method for the hint button
    public void displayHint() {
        if ((boardView == null || selectedCellIsNull()))
            return;

        // if the user is out of attempts
        if (hintAttempts < 1)
            return;

        // we don't want to do anything if the user clicked on hint on the exact same cell
        if (lastHintPosition.rowEquals(selectedRow - 1) || lastHintPosition.columnEquals(selectedColumn - 1))
            return;

        // if the value in the selected cell is constant, we don't want to waste a hint
        if (!numberInSelectedCellIsNotConstant())
            return;

        int cellValueInSolvedBoard = solvedBoard[selectedRow - 1][selectedColumn - 1];

        setValueInSelectedCell(cellValueInSolvedBoard);
        // saving the last hint position for the next time the user clicks the button
        lastHintPosition = new Cell(selectedRow - 1, selectedColumn - 1);
        hintAttempts--;
    }

    public void toggleNote() {
        note = !note;
    }

    public boolean isNoteOn() {
        return note;
    }

    public int[][][] getNotes() {
        return notes;
    }

    public void setNote(int number) {
        // we set the note by toggling between 1 and 0, (1 is true and 0 is false)
        if (notes[selectedRow - 1][selectedColumn - 1][number - 1] == 1) {
            notes[selectedRow - 1][selectedColumn - 1][number - 1] = 0;
        } else {
            notes[selectedRow - 1][selectedColumn - 1][number - 1] = 1;
        }
    }

    public int[][] getUnSolvedBoard() {
        return unsolvedBoard;
    }

    // a method to undo the last step the user made
    public boolean undoLast() {
        // we won't do anything if there are no changes made to the board
        if (lastChanges.size() == 0)
            return false;

        int lastChangeIndex = lastChanges.size() - 1;
        Change lastChange = lastChanges.get(lastChangeIndex);
        // we remove the last change made after storing it because it's irrelevant now
        lastChanges.remove(lastChangeIndex);

        // resetting the previous value
        board[lastChange.cell.getRow()][lastChange.cell.getColumn()] = lastChange.prevValue;

        return true;
    }

    // a method that unsets the value in the current cell, used for the erase-button
    public void unsetCurrentCell() {
        board[selectedRow - 1][selectedColumn - 1] = 0;
    }
}
