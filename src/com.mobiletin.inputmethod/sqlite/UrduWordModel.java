package com.mobiletin.inputmethod.sqlite;

/**
 * Created by Administrator on 10/27/2016.
 */

public class UrduWordModel {
    private int id;
    private String word;
    private int frequency;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}
