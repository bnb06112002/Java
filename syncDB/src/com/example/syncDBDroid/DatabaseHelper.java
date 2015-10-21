package com.example.syncDBDroid;



import android.content.SharedPreferences;

import android.content.Context;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper {
   // private final Context fContext;
    private String DATABASE_NAME;
    private final  dbXML fXML;

    DatabaseHelper(Context context,dbXML xmld,choiseSetting chSetting,SharedPreferences sPref) {

       // super(context, DATABASE_NAME, null,vDB);//xmld.hashCode()
        super(new DatabaseContext(context,chSetting.isSD()), chSetting.getDb_name(), null, xmld.hashCode());
        DATABASE_NAME=chSetting.getDb_name();
        fXML=xmld;
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt(chSetting.getDb_name(),xmld.hashCode());
        ed.commit();

    }
    DatabaseHelper(Context context,dbXML xmld,String db_name,SharedPreferences sPref) {

        // super(context, DATABASE_NAME, null,vDB);//xmld.hashCode()
        super(context, db_name, null, xmld.hashCode());
        DATABASE_NAME=db_name;
        fXML=xmld;
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt(db_name,xmld.hashCode());
        ed.commit();
    }

    DatabaseHelper(Context context,choiseSetting chSetting,int vDB) {

        super(new DatabaseContext(context,chSetting.isSD()), chSetting.getDb_name(), null,vDB);//xmld.hashCode()
        DATABASE_NAME=chSetting.getDb_name();
        fXML=null;
    }
    DatabaseHelper(Context context,String db_name,int vDB) {

        super(context, db_name, null,vDB);//xmld.hashCode()
        DATABASE_NAME=db_name;
        fXML=null;


    }
    DatabaseHelper(Context context,String db_name,boolean isSD,int vDB) {
        super(new DatabaseContext(context,isSD), db_name, null,vDB);//xmld.hashCode()
        DATABASE_NAME=db_name;
        fXML=null;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        if (fXML==null){
         return;
        }
        StringBuilder sqlStr=new StringBuilder();

        sqlStr.append("CREATE TABLE ").append("%s").append(" (");
        sqlStr.append("hash_code INTEGER,hash_codeCL INTEGER, status INTEGER DEFAULT 0,");
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

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + fXML.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS tmp_" + fXML.TABLE_NAME);
        onCreate(db);
    }
}
