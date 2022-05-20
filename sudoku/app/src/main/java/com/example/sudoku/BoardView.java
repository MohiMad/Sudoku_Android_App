package com.example.sudoku;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class BoardView extends View {
    private final int boardColor;
    private final int cellFillColor;
    private final int cellsHighlightColor;
    private final int backgroundColor;
    private final int letterColor;
    private final int letterColorSolved;
    private final int similarNumberColor;
    private final int numberCorrect;
    private final int numberIncorrect;

    private final Paint boardColorPaint = new Paint();
    private final Paint cellFillColorPaint = new Paint();
    private final Paint cellsHighlightColorPaint = new Paint();
    private final Paint backgroundColorPaint = new Paint();
    private final Paint similarNumberColorPaint = new Paint();
    private final Paint numberCorrectPaint = new Paint();
    private final Paint numberIncorrectPaint = new Paint();
    private final Paint letterPaint = new Paint();
    private final Rect letterPaintBounds = new Rect();
    private Setup sudokuSetup;
    private int cellSize;
    private Canvas canvas;

    public BoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SudokuBoard, 0, 0);

        try {
            // we retrieve the values assigned in the BoardView initiated in play_activity
            boardColor = a.getInteger(R.styleable.SudokuBoard_boardColor, 0);
            cellFillColor = a.getInteger(R.styleable.SudokuBoard_cellFillColor, 0);
            similarNumberColor = a.getInteger(R.styleable.SudokuBoard_similarNumberColor, 0);
            cellsHighlightColor = a.getInteger(R.styleable.SudokuBoard_cellsHighlightColor, 0);
            backgroundColor = a.getInteger(R.styleable.SudokuBoard_backgroundColor, 0);
            letterColor = a.getInteger(R.styleable.SudokuBoard_letterColor, 0);
            letterColorSolved = a.getInteger(R.styleable.SudokuBoard_letterColorSolved, 0);
            numberIncorrect = a.getInteger(R.styleable.SudokuBoard_numberIncorrect, 0);
            numberCorrect = a.getInteger(R.styleable.SudokuBoard_numberCorrect, 0);
        } finally {
            a.recycle();
        }
    }

    //Making the sudoku board flexible based on the device width
    @Override
    protected void onMeasure(int width, int height) {
        super.onMeasure(width, height);

        //Get the minimum value of the width and the height
        int dimension = Math.min(getMeasuredWidth(), getMeasuredHeight());
        //There is 9 cells per square
        cellSize = dimension / 9;

        setMeasuredDimension(dimension, dimension);
    }

    private void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        setCanvas(canvas);

        // Override the canvas to draw the sudoku board
        // The code below sets the properties of the paints
        boardColorPaint.setStyle(Paint.Style.STROKE);
        boardColorPaint.setStrokeWidth(16);
        boardColorPaint.setColor(boardColor);
        boardColorPaint.setAntiAlias(true);

        cellsHighlightColorPaint.setStyle(Paint.Style.FILL);
        cellsHighlightColorPaint.setColor(cellsHighlightColor);
        cellsHighlightColorPaint.setAntiAlias(true);

        cellFillColorPaint.setStyle(Paint.Style.FILL);
        cellFillColorPaint.setColor(cellFillColor);
        cellFillColorPaint.setAntiAlias(true);

        similarNumberColorPaint.setStyle(Paint.Style.FILL);
        similarNumberColorPaint.setColor(similarNumberColor);
        similarNumberColorPaint.setAntiAlias(true);

        numberCorrectPaint.setStyle(Paint.Style.FILL);
        numberCorrectPaint.setColor(numberCorrect);
        numberCorrectPaint.setAntiAlias(true);

        numberIncorrectPaint.setStyle(Paint.Style.FILL);
        numberIncorrectPaint.setColor(numberIncorrect);
        numberIncorrectPaint.setAntiAlias(true);

        backgroundColorPaint.setStyle(Paint.Style.FILL);
        backgroundColorPaint.setColor(backgroundColor);
        backgroundColorPaint.setAntiAlias(true);

        letterPaint.setStyle(Paint.Style.FILL);
        letterPaint.setAntiAlias(true);
        letterPaint.setColor(letterColor);

        // drawing a white rectangle to cover the entire canvas (and draw over edited ones to reset the canvas)
        canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), backgroundColorPaint);

        Sudoku sudoku = sudokuSetup.getSudoku();
        // if the user finished the sudoku, we notify if the solution is correct or false
        if (sudoku.isAllEmptyBoxesFilled()) {
            highlightCorrectAndOrInCorrectCells(canvas, sudoku);
            colorCell(canvas, sudokuSetup.getSelectedRow(), sudokuSetup.getSelectedColumn());
            drawBoard(canvas);
            drawConstants(canvas);
            displayNotes(canvas);
            drawNumbers(canvas);
        } else {
            // we only want to highlight similar numbers when the sudoku isn't full yet
            // otherwise the colors are gonna overlap with correct and incorrect colors
            colorCell(canvas, sudokuSetup.getSelectedRow(), sudokuSetup.getSelectedColumn());
            highlightSimilarNumbers(canvas);
            drawBoard(canvas);
            drawConstants(canvas);
            displayNotes(canvas);
            drawNumbers(canvas);
        }

        invalidate();
    }

    private void highlightCorrectAndOrInCorrectCells(Canvas canvas, Sudoku sudoku) {
        ArrayList<Cell> emptyBoxes = sudoku.getEmptyBoxes();
        int[][] solvedBoard = sudoku.getSolvedBoard();
        int[][] board = sudoku.getBoard();

        for (Cell emptyCell : emptyBoxes) {
            if (board[emptyCell.getRow()][emptyCell.getColumn()] == solvedBoard[emptyCell.getRow()][emptyCell.getColumn()]) {
                // the number is correct, highlight it in green
                canvas.drawRect(emptyCell.getColumn() * cellSize, emptyCell.getRow() * cellSize, (emptyCell.getColumn()+1) * cellSize, (emptyCell.getRow()+1) * cellSize, numberCorrectPaint);
            } else {
                canvas.drawRect(emptyCell.getColumn() * cellSize, emptyCell.getRow() * cellSize, (emptyCell.getColumn()+1) * cellSize, (emptyCell.getRow()+1) * cellSize, numberIncorrectPaint);
            }

        }
    }

    private void drawConstants(Canvas canvas) {
        letterPaint.setTextSize(cellSize);
        int[][] unSolvedBoard = sudokuSetup.getUnSolvedBoard();

        //Drawing "constant" (unchanged) cells
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                // we basically draw everything in the original cell that isn't equal to 0 (because its not a constant)
                if (unSolvedBoard[r][c] != 0) {
                    drawNumberOnCanvas(canvas, r, c, unSolvedBoard[r][c]);
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // a boolean to indicate whether a click has been registered by the user or not
        boolean isValid;

        float x = event.getX(), y = event.getY();

        int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            // fires when the user clicks on the canvas

            // calculating which cell the user have clicked on by dividing the x and y coordinates which the size of the cell
            sudokuSetup.setSelectedRow((int) Math.ceil(y / cellSize));
            sudokuSetup.setSelectedColumn((int) Math.ceil(x / cellSize));

            isValid = true;
        } else {
            isValid = false;
        }

        return isValid;
    }

    private void displayNotes(Canvas canvas) {
        letterPaint.setTextSize((float) cellSize / 3);
        int[][][] notes = getSetupInstance().getNotes();

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                for (int index = 0; index < 9; index++) {
                    // If the value stored is 1 then number (index + 1) should be displayed as a note
                    if (notes[r][c][index] == 1) {
                        // if a value is already set we want to prioritize the value over the note
                        if (sudokuSetup.getSudoku().getBoard()[r][c] == 0) {

                            int number = index + 1;
                            String note = Integer.toString(number);
                            float width, height;

                            letterPaint.getTextBounds(note, 0, note.length(), letterPaintBounds);
                            width = letterPaint.measureText(note);
                            height = letterPaintBounds.height();
                            float toIncrementX, toIncrementY;

                            switch (number % 3) {
                                default:
                                case 1:
                                    toIncrementX = (cellSize - width) / 2 - width - 0.5f * width;
                                    break;

                                case 2:
                                    toIncrementX = (cellSize - width) / 2;
                                    break;
                                case 0:
                                    toIncrementX = (cellSize - width) / 2 + (cellSize - width) / 3;
                                    break;
                            }

                            switch ((int) Math.floor((float) (number - 1) / 3)) {
                                default:
                                case 0:
                                    toIncrementY = (cellSize - height) / 2 - (height / 6);break;

                                case 1:
                                    toIncrementY = (cellSize - height) / 2 + (cellSize / 4);
                                    break;

                                case 2:
                                    toIncrementY = (cellSize - height) / 2 + (cellSize / 1.75f);
                                    break;
                            }

                            canvas.drawText(note, (c * cellSize) + toIncrementX, (r * cellSize) + toIncrementY, letterPaint);
                        }
                    }
                }
            }
        }
    }

    protected void drawNumbers(Canvas canvas) {
        int[][] board = sudokuSetup.getBoardFromSudokuInstance();
        letterPaint.setTextSize(cellSize);

        letterPaint.setColor(letterColorSolved);
        // drawing the numbers picked by the user
        for (Cell number : sudokuSetup.getEmptyBoxesFromSudokuInstance()) {
            int row = number.getRow();
            int column = number.getColumn();
            // if its not 0, then we draw it on the canvas
            if (board[row][column] != 0) {
                drawNumberOnCanvas(canvas, row, column, board[row][column]);
            }
        }
    }

    private void drawNumberOnCanvas(Canvas canvas, int row, int column, int value) {
        String text = Integer.toString(value);
        float width, height;

        letterPaint.getTextBounds(text, 0, text.length(), letterPaintBounds);
        width = letterPaint.measureText(text);
        height = letterPaintBounds.height();

        canvas.drawText(text, (column * cellSize) + ((cellSize - width) / 2), (row * cellSize + cellSize) - ((cellSize - height) / 2), letterPaint);
    }

    private void colorCell(Canvas canvas, int r, int c) {
        if (cellIsValid() && cellDoesNotHoldAConstant(r, c)) {
            highlightSelectedRow(canvas, r);
            highlightSelectedColumn(canvas, c);
            highlightSelectedSquare(canvas, r, c);
            highlightSelectedCell(canvas, r, c);
        }
    }

    private void highlightSelectedRow(Canvas canvas, int r) {
        //Horizontal draw so the user knows which row they're currently at
        canvas.drawRect(0, (r - 1) * cellSize, 9 * cellSize, r * cellSize, cellsHighlightColorPaint);
    }

    private void highlightSelectedColumn(Canvas canvas, int c) {
        //Vertical draw to show the user which column they're currently at
        canvas.drawRect((c - 1) * cellSize, 0, c * cellSize, cellSize * 9, cellsHighlightColorPaint);
    }

    private void highlightSelectedSquare(Canvas canvas, int r, int c) {
        int squareIndex = sudokuSetup.getSudoku().getIndexOfSquare(new Cell(r - 1, c - 1));
        int left = (squareIndex % 3) * cellSize * 3;
        int top = ((int) Math.floor((double) squareIndex / 3)) * cellSize * 3;

        canvas.drawRect(left, top, left + cellSize * 3, top + cellSize * 3, cellsHighlightColorPaint);
    }

    private void highlightSelectedCell(Canvas canvas, int r, int c) {
        // Coloring the selected cell to notify the user that the cell has been selected
        canvas.drawRect((c - 1) * cellSize, (r - 1) * cellSize, c * cellSize, r * cellSize, cellFillColorPaint);
    }

    private void highlightSimilarNumbers(Canvas canvas) {
        int[][] board = sudokuSetup.getSudoku().getBoard();
        // we don't want to highlight cells with the value 0
        if (sudokuSetup.selectedCellIsNotNull()) {
            // if the value isn't 0, proceed
            if (sudokuSetup.getValueInSelectedCell() != 0) {
                for (int r = 1; r <= 9; r++) {
                    for (int c = 1; c <= 9; c++) {
                        // if a value in the board the same as the value in the selected cell, highlight it
                        if (board[r - 1][c - 1] == sudokuSetup.getValueInSelectedCell()) {
                            canvas.drawRect((c - 1) * cellSize, (r - 1) * cellSize, c * cellSize, r * cellSize, similarNumberColorPaint);
                        }
                    }
                }
            }
        }
    }

    private boolean cellDoesNotHoldAConstant(int row, int column) {
        for (Cell emptyCell : sudokuSetup.getEmptyBoxesFromSudokuInstance()) {
            // if the cell is found inside the "emptyBoxes" arraylist, then the cell is not a "constant".
            if ((emptyCell.getRow() + 1) == row && (emptyCell.getColumn() + 1) == column) {
                return true;
            }
        }

        return false;
    }

    private boolean cellIsValid() {
        return sudokuSetup.getSelectedColumn() != -1 && sudokuSetup.getSelectedRow() != -1;
    }

    private void drawThickLine() {
        boardColorPaint.setStyle(Paint.Style.STROKE);
        boardColorPaint.setStrokeWidth(10);
        boardColorPaint.setColor(boardColor);
    }

    private void drawThinLine() {
        boardColorPaint.setStyle(Paint.Style.STROKE);
        boardColorPaint.setStrokeWidth(4);
        boardColorPaint.setColor(boardColor);
    }

    private void drawBoard(Canvas canvas) {
        //Draw columns
        for (int c = 0; c < 10; c++) {
            if (c % 3 == 0) {
                drawThickLine();
            } else {
                drawThinLine();
            }

            canvas.drawLine(cellSize * c, 0, cellSize * c, getWidth(), boardColorPaint);
        }

        //Draw rows
        for (int r = 0; r < 10; r++) {
            if (r % 3 == 0) {
                drawThickLine();
            } else {
                drawThinLine();
            }

            canvas.drawLine(0, cellSize * r, getWidth(), cellSize * r, boardColorPaint);

        }
    }

    public Setup getSetupInstance() {
        return sudokuSetup;
    }

    public void setDifficultyAndInitiateSetup(Difficulty difficulty) {
        sudokuSetup = new Setup(difficulty);
    }

    public void initiateSetup(Sudoku sudoku) {
        sudokuSetup = new Setup(sudoku);
    }
}
