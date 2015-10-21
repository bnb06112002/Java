package com.example.readLogSDroid;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private final Context fContext;
    private static final String DATABASE_NAME = "saleit.db";
    private final  dbXML fXML;

    DatabaseHelper(Context context,dbXML xmld) {

        super(context, DATABASE_NAME, null, xmld.hashCode());
        fContext = context;
        fXML=xmld;
    }





    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        StringBuilder sqlStr=new StringBuilder();

        sqlStr.append("CREATE TABLE ").append("%s").append(" (");
        sqlStr.append("hash_code INTEGER, status INTEGER DEFAULT 0,");
        for (int i = 0; i < fXML.FIELD.size(); ++i){
            recordField rField = (recordField) fXML.FIELD.get(i);
            if(i>0){
                sqlStr.append(",");
            }
            sqlStr.append(rField.nameField).append(" ");
            if (rField.typeField.equalsIgnoreCase("int")){
                sqlStr.append("INTEGER");
            }else if(rField.typeField.equalsIgnoreCase("string")){
                sqlStr.append("TEXT");
            }


        }
        sqlStr.append(");");
        db.execSQL(String.format(sqlStr.toString(),fXML.TABLE_NAME));
        db.execSQL(String.format(sqlStr.toString(),"tmp_"+fXML.TABLE_NAME));
        /*db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                + "_id INTEGER PRIMARY KEY, " + "title TEXT, " + "color TEXT"
                + ");");*/

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        Log.w("TestBase", "Upgrading database from version " + oldVersion
                + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + fXML.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS tmp_" + fXML.TABLE_NAME);
        onCreate(db);
    }
}
