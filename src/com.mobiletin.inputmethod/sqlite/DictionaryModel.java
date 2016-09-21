package com.mobiletin.inputmethod.sqlite;/*
   Created by Noman and  Email = nomanarif.cdz@gmail.com on 6/7/2016.
*/

public class DictionaryModel {
    private int Z_PK;
    private String ZWORD;
    private String TARGETWORD;
    private String SUGGESTIONS;
    private int INDEXING;

    public String getZWORD() {
        return ZWORD;
    }

    public void setZWORD(String ZWORD) {
        this.ZWORD = ZWORD;
    }

    public String getTARGETWORD() {
        return TARGETWORD;
    }

    public void setTARGETWORD(String TARGETWORD) {
        this.TARGETWORD = TARGETWORD;
    }

    public int getZ_PK() {
        return Z_PK;
    }

    public void setZ_PK(int z_PK) {
        Z_PK = z_PK;
    }

    public String getSUGGESTIONS() {
        return SUGGESTIONS;
    }

    public void setSUGGESTIONS(String SUGGESTIONS) {
        this.SUGGESTIONS = SUGGESTIONS;
    }

    public int getINDEXING() {
        return INDEXING;
    }

    public void setINDEXING(int INDEXING) {
        this.INDEXING = INDEXING;
    }
}
