package com.mobiletin.inputmethod.sqlite;/*
   Created by Noman and  Email = nomanarif.cdz@gmail.com on 6/7/2016.
*/


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mobiletin.inputmethod.MySuperAppApplication;

import java.util.ArrayList;
import java.util.List;

public class DBDictionary {
    private DataBaseHelper databaseHelper;
    private Context mContext;

    public DBDictionary(Context context) {
        this.databaseHelper = new DataBaseHelper(context);
        this.mContext = context;
    }



    public List<DictionaryModel> getUrduDicFromEnglishWord(String word,int limit) {
        String sqlStatement=null;

        if(!MySuperAppApplication.isProbablyArabic(word)) {
      sqlStatement = "Select * from ZTRANSLITERATEDDATA WHERE ZWORD LIKE '" + word + "%' limit " + limit;
        }
        else
        {
            sqlStatement = "Select * from ZTRANSLITERATEDDATA WHERE TARGETWORD LIKE '" + word + "%' limit " + limit;
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


    public DictionaryModel getSingleWordUrdu(String word) {
        String sqlStatement = "Select * from ZTRANSLITERATEDDATA WHERE ZWORD LIKE '" + word + "' " ;

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


}

