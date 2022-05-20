package com.example.sudoku;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.system.ErrnoException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class PlayActivity extends AppCompatActivity {
    private Setup boardSetup;
    private BoardView boardView;
    private Button hintBtn, solveBtn, noteBtn;
    Difficulty difficulty;
    // a variable to indicate whether the winning/game over sound has already been played or not.
    boolean isSoundPlayed = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CONTEXT_MENU);
        Bundle extras = getIntent().getExtras();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_play);

        boardView = (BoardView) findViewById(R.id.sudokuBoard);
        hintBtn = (Button) findViewById(R.id.hint);

        //  extras is what's been sent from new_sudoku or main_activity activities
        if (extras != null) {
            String idString = extras.getString("UUID");
            String difficultyString = extras.getString("difficulty");

            if (difficultyString != null) {
                // if difficulty exists then it means we want to create a new sudoku
                Difficulty difficulty = Difficulty.getDifficulty(difficultyString);
                boardView.setDifficultyAndInitiateSetup(difficulty);
            } else if (idString != null) {
                UUID id = UUID.fromString(idString);
                // if the id exists then we want to load the sudoku with that id'
                SharedPreferences prefs = getSharedPreferences("sudoku", 0);
                String data = prefs.getString(id.toString(), "0");

                if (data.equals("0")) {
                    // if the data weren't found then something wrong has happened, we throw an error
                    try {
                        throw new Exception("Oops. No data with the provided UUID was found.");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                Sudoku sudoku = Utility.parseDataIntoSudoku(id, data);
                boardView.initiateSetup(sudoku);
            }
        }
        // we need to invalidate the boardView because we've updated its information (added setup)
        boardView.invalidate();

        boardSetup = (Setup) boardView.getSetupInstance();
        boardSetup.setBoardView(boardView);
    }


    @SuppressLint("DefaultLocale")
    public void displayHint(View view) {
        Button hintBtn = (Button) view;
        boardSetup.displayHint();
        hintBtn.setText(String.format("Hint (%d/3)", boardSetup.getHintAttempts()));
        // it could be that the user solves the last cell using the hint button
        checkIfSolvedIfYesPlaySound();
    }

    @SuppressLint("ResourceAsColor")
    public void toggleNote(View view) {
        boardSetup.toggleNote();
        // if note is on, we want to change the color of the button to make it obvious
        if (boardSetup.isNoteOn()) {
            ((Button) view).setText(R.string.note_on);
        } else {
            ((Button) view).setText(R.string.note_off);
        }
    }

    public void btnPress(View view) {
        Button btn = (Button) view;
        int value = Integer.parseInt((String) btn.getText());

        if (boardSetup.isNoteOn()) {
            boardSetup.setNote(value);
        } else {
            boardSetup.setNumberInBoard(value);
        }

        boardView.invalidate();

        // everytime a button is clicked, we want to check if the board has been solved
        checkIfSolvedIfYesPlaySound();
    }

    private void checkIfSolvedIfYesPlaySound() {
        // if its solved, play a winning sound
        if (boardSetup.getSudoku().isSolved() && !isSoundPlayed) {
            final MediaPlayer mp = MediaPlayer.create(PlayActivity.this, R.raw.wow);
            mp.start();
            // in order to only play the sound once
            isSoundPlayed = true;
        }
    }

    public void undoLastStep(View btn) {
        // if the undo:ing is unsuccessful, we send a message
        if (!boardSetup.undoLast()) {
            Toast.makeText(getApplicationContext(), "Nothing to undo...", Toast.LENGTH_SHORT).show();
        }
    }

    public void unsetCell(View view) {
        boardSetup.unsetCurrentCell();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // when the activity is paused for whatever reason (game closed, etc) we save the progress
        Utility.saveSudokuData(boardSetup.getSudoku(), getApplicationContext());
    }

    @Override
    public void onBackPressed() {
        // when the go-back button is pressed, we save the data and send the user to the main page
        // (instead of sending the user to the previous activity)
        Utility.saveSudokuData(boardSetup.getSudoku(), getApplicationContext());

        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
}