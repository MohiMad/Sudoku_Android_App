package com.example.sudoku;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class NewSudokuActivity extends AppCompatActivity {
    AutoCompleteTextView autoCompleteText;
    ArrayAdapter<String> adapter;
    String[] difficulties;
    int selected;
    boolean isAlreadyClicked;
    Button createBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_sudoku);

        createBtn = findViewById(R.id.createBtn);
        autoCompleteText = findViewById(R.id.auto_complete_text);
        difficulties = new String[Difficulty.values().length];

        for(int i = 0; i < Difficulty.values().length; i++){
            difficulties[i] = Difficulty.values()[i].getText();
        }

        // adapter for the listview that holds all the difficulties
        adapter = new ArrayAdapter<String>(this, R.layout.list_item, difficulties);

        autoCompleteText.setAdapter(adapter);

        autoCompleteText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // we saved the position into a global variable
                selected = i;
            }
        });

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String itemValue = adapter.getItem(selected);

                if(itemValue != null && !isAlreadyClicked){
                    createBtn.setText(R.string.loading);
                    // to prevent spamming
                    isAlreadyClicked = true;

                    Intent i = new Intent(NewSudokuActivity.this, PlayActivity.class);
                    i.putExtra("difficulty", itemValue);
                    startActivity(i);
                } else {
                    Toast.makeText(NewSudokuActivity.this, "Please select a difficulty.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

}