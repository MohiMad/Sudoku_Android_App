package com.example.sudoku;

/**
 * An enum holding constants of the difficulties
 * An enum is a special type of class in java that holds constants.
 * Instead of representing the difficulties as 0,1,2,3,4,5 ... we use an enum
 *
 * This enum has one parameter which is the text of how we want the difficulty for the user to be represented
 */
public enum Difficulty {
    EASY("Easy"), MEDIUM("Medium"), HARD("Hard"), VERY_HARD("Very Hard"), EXPERT("Expert");

    private String text;

    Difficulty(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    // compares a string to the difficulties, if it matches any, return the difficulty, otherwise return EASY
    public static Difficulty getDifficulty(String string){
        switch (string){
            default:
            case "Easy":
                return EASY;
            case "Medium":
                return MEDIUM;
            case "Hard":
                return HARD;
            case "Very Hard":
                return VERY_HARD;
            case "Expert":
                return EXPERT;
        }

    }
}
