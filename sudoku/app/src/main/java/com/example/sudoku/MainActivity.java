package com.example.sudoku;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    ArrayList<Sudoku> savedSudokuData = new ArrayList<>();
    int positionOfClicked = -1;
    ListView sudokuListview;
    Button deleteSudoku, newSudoku, playSudoku;
    SudokuListAdapter listAdapter;
    int iterator = 0;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CONTEXT_MENU);
        setContentView(R.layout.activity_main);

        sudokuListview = findViewById(R.id.sudoku_listview);
        deleteSudoku = findViewById(R.id.deleteSudoku);
        newSudoku = findViewById(R.id.newSudoku);
        playSudoku = findViewById(R.id.playSudoku);

        // sharedPreferences is what's used to store all the user data in the local storage to keep progress and created sudoku:s
        SharedPreferences pref = getApplicationContext().getSharedPreferences("sudoku", 0);

        // forEach is a function that accepts a lambda expression, serves as a for-loop
        pref.getAll().forEach((key, pair) -> {
            // key -> the UUID in string
            // pair -> the actual data for the sudoku

            // we parse the data into a sudoku and add it to the arraylist for the list adapter
            Sudoku sudoku = Utility.parseDataIntoSudoku(UUID.fromString(key), (String) pair);
            savedSudokuData.add(sudoku);
        });

        // since the layout sudoku_list_item has multiple views inside, we need to specify which view we want the adapter to apply to
        listAdapter = new SudokuListAdapter(MainActivity.this, savedSudokuData);

        sudokuListview.setAdapter(listAdapter);
        sudokuListview.setClickable(true);

        // we don't want the button to be clickable before a sudoku have been chosen
        deleteSudoku.setEnabled(false);
        playSudoku.setEnabled(false);

        sudokuListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // we saved the position into a global variable so we can access it outside of this method
                positionOfClicked = position;
                deleteSudoku.setEnabled(true);
                playSudoku.setEnabled(true);
            }
        });
    }

    public void onCreateNewSudokuClick(View view) {
        // send the user to new_sudoku activity
        Intent intent = new Intent(MainActivity.this, NewSudokuActivity.class);
        startActivity(intent);
    }

    public void playSudoku(View view) {
        // we send the user to the play_activity and attach the UUID of the sudoku
        // since intent.putExtra doesnt accept a (Sudoku class) as its parameter we must use sharedPreference to retrieve the sudoku
        Sudoku sudoku = savedSudokuData.get(positionOfClicked);

        if(sudoku != null){
            Intent intent = new Intent(MainActivity.this, PlayActivity.class);
            intent.putExtra("UUID", sudoku.id.toString());

            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "No sudoku found.", Toast.LENGTH_SHORT).show();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onDeleteSudokuClick(View view) {
        // we want to delete the highlighted sudoku, if it exists
        if (positionOfClicked != -1) {
            SharedPreferences pref = getApplicationContext().getSharedPreferences("sudoku", 0);

            // we look through all saved sudoku and remove the one that matches the position of the clicked sudoku
            pref.getAll().forEach((key, pair) -> {
                if (iterator == positionOfClicked) {
                    pref.edit().remove(key).apply();
                    savedSudokuData.remove(positionOfClicked);
                    listAdapter.notifyDataSetChanged();

                    if(savedSudokuData.size() == 0){
                        // if there are no sudoku's left then we want to disable the buttons
                        newSudoku.setEnabled(false);
                        deleteSudoku.setEnabled(false);
                    }
                }
                iterator++;
            });

            // in order to reset the loop
            iterator = 0;
        }
    }
}
