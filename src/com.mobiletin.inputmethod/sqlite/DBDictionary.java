package com.mobiletin.inputmethod.sqlite;/*
   Created by Noman and  Email = nomanarif.cdz@gmail.com on 6/7/2016.
*/


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mobiletin.inputmethod.MySuperAppApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DBDictionary {
    private DataBaseHelper databaseHelper;
    private Context mContext;

    public DBDictionary(Context context) {
        this.databaseHelper = new DataBaseHelper(context);
        this.mContext = context;
    }


    public List<DictionaryModel> getUrduDicFromEnglishWord(String word, int limit) {
        String sqlStatement = null;


        String special = "\"'!:;/#-@#$%^&*()_";
        String pattern = ".*[" + Pattern.quote(special) + "].*";
        if(word.matches(pattern))
        {
            return  null;
        }


        if (!MySuperAppApplication.isProbablyArabic(word)) {
            sqlStatement = "Select * from ZTRANSLITERATEDDATA WHERE ZWORD LIKE '" + word + "' limit " + limit;
        }
        else
        {
            return null;
        }
        SQLiteDatabase db = this.databaseHelper.getReadableDatabase();
        if (db == null)
            return null;
        List<DictionaryModel> dictionaryModelModelList = new ArrayList<DictionaryModel>();
        Cursor c = db.rawQuery(sqlStatement, null);
        DictionaryModel dictionaryModelModel = null;
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                dictionaryModelModel = new DictionaryModel();
                dictionaryModelModel.setZ_PK(c.getInt(c.getColumnIndex("Z_PK")));
                dictionaryModelModel.setZWORD(c.getString(c.getColumnIndex("ZWORD")));
                dictionaryModelModel.setTARGETWORD(c.getString(c.getColumnIndex("TARGETWORD")));
                dictionaryModelModel.setSUGGESTIONS(c.getString(c.getColumnIndex("SUGGESTIONS")));
                dictionaryModelModel.setINDEXING(c.getInt(c.getColumnIndex("INDEXING")));

                dictionaryModelModelList.add(dictionaryModelModel);

            } while (c.moveToNext());
            c.close();
        }
        db.close();
        return dictionaryModelModelList;
    }

    public List<UrduWordModel> getUrduWords(String word) {
        DBDictionaryUrdu db = new DBDictionaryUrdu(mContext);
        List<UrduWordModel> dictionaryModelModelList = db.getUrduWords(word);

        return dictionaryModelModelList;
    }

    public DictionaryModel getSingleWordUrdu(String word) {

        word = word.replace("\'", "");
        String sqlStatement = "Select * from ZTRANSLITERATEDDATA WHERE ZWORD LIKE '" + word + "' ";

        SQLiteDatabase db = this.databaseHelper.getReadableDatabase();
        if (db == null)
            return null;
        Cursor c = db.rawQuery(sqlStatement, null);
        DictionaryModel dictionaryModelModel = null;
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {

                dictionaryModelModel = new DictionaryModel();
                dictionaryModelModel.setZ_PK(c.getInt(c.getColumnIndex("Z_PK")));
                dictionaryModelModel.setZWORD(c.getString(c.getColumnIndex("ZWORD")));
                dictionaryModelModel.setTARGETWORD(c.getString(c.getColumnIndex("TARGETWORD")));
                dictionaryModelModel.setSUGGESTIONS(c.getString(c.getColumnIndex("SUGGESTIONS")));
                dictionaryModelModel.setINDEXING(c.getInt(c.getColumnIndex("INDEXING")));
            } while (c.moveToNext());
            c.close();
        }
        db.close();
        return dictionaryModelModel;
    }


    public boolean insertSuggestion(DictionaryModel dictionaryModel) {
        if (this.databaseHelper == null)
            return false;
        SQLiteDatabase db = this.databaseHelper.getWritableDatabase();
        if (db == null)
            return false;

        ContentValues contentValues = new ContentValues();

        //inset max number
        Cursor cursor = db.query("ZTRANSLITERATEDDATA", new String[]{"MAX(Z_PK) AS MAX"}, null, null, null, null, null);
        cursor.moveToFirst(); // to move the cursor to first record
        int index = cursor.getColumnIndex("MAX");
        int data = cursor.getInt(index);
        //////


        contentValues.put("Z_PK", (data + 1));
        contentValues.put("ZWORD", dictionaryModel.getZWORD());
        contentValues.put("TARGETWORD", dictionaryModel.getTARGETWORD());
        contentValues.put("SUGGESTIONS", dictionaryModel.getSUGGESTIONS());
        contentValues.put("INDEXING", dictionaryModel.getINDEXING());

        String sqlStatement = "Select * from ZTRANSLITERATEDDATA WHERE ZWORD LIKE '" + dictionaryModel.getZWORD() + "' ";
        Cursor c = db.rawQuery(sqlStatement, null);

        if (c.moveToFirst()) {
            return false;
        } else {
            long id = db.insert("ZTRANSLITERATEDDATA", null, contentValues);
            if (id == -1)
                return false;
        }

        db.close();
        return true;
    }


}

