package com.mobiletin.inputmethod.sqlite;/*
   Created by Noman and  Email = nomanarif.cdz@gmail.com on 6/7/2016.
*/


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DBDictionaryUrdu {
    private DataBaseHelperUrdu databaseHelper;
    private Context mContext;

    public DBDictionaryUrdu(Context context) {
        this.databaseHelper = new DataBaseHelperUrdu(context);
        this.mContext = context;
    }


    public List<UrduWordModel> getUrduWords(String word) {
        //Special Character matches
        String special = "\"'!:;/#-@#$%^&*()_";
        String pattern = ".*[" + Pattern.quote(special) + "].*";
        if(word.matches(pattern))
        {
            return  null;
        }


        String sqlStatement=null;
        sqlStatement = "Select * from Words_freq WHERE word LIKE '" + word + "%' limit 5";

        SQLiteDatabase db = this.databaseHelper.getReadableDatabase();
        if (db == null)
            return null;
        List<UrduWordModel> dictionaryModelModelList = new ArrayList<UrduWordModel>();
        Cursor c = db.rawQuery(sqlStatement, null);
        UrduWordModel dictionaryModelModel = null;
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                dictionaryModelModel = new UrduWordModel();
                dictionaryModelModel.setId(c.getInt(c.getColumnIndex("id")));
                dictionaryModelModel.setWord(c.getString(c.getColumnIndex("word")));
                dictionaryModelModel.setFrequency(c.getInt(c.getColumnIndex("freq")));

                dictionaryModelModelList.add(dictionaryModelModel);

            } while (c.moveToNext());
            c.close();
        }
        db.close();
        return dictionaryModelModelList;
    }


}

