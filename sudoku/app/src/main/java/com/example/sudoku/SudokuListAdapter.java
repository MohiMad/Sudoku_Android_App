package com.example.sudoku;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class SudokuListAdapter extends ArrayAdapter<Sudoku> {

    // this class is made to be able to control multiple fields inside a listview
    public SudokuListAdapter(Context context, ArrayList<Sudoku> sudokuArrayList){
        super(context,R.layout.sudoku_list_item, sudokuArrayList);
    }

    @SuppressLint({"SetTextI18n", "ViewHolder"})
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LinearLayout rowLayout = null;
        Sudoku sudoku = getItem(position);

        if (convertView == null) {
            // inflating the row
            rowLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(
                    R.layout.sudoku_list_item, parent, false);

        } else {
            rowLayout = (LinearLayout) convertView;
        }

        TextView sudokuId = rowLayout.findViewById(R.id.sudoku_id);
        TextView sudokuDifficulty = rowLayout.findViewById(R.id.difficulty);

        // we set the id of the sudoku and its difficulty
        sudokuId.setText("Sudoku #" + (position+1));
        sudokuDifficulty.setText(sudoku.getDifficulty().getText());

        return rowLayout;
    }
}