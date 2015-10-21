package com.example.syncDBDroid;

import android.app.Activity;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.*;


import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: n.bilan
 * Date: 12.12.13
 * Time: 17:45
 * To change this template use File | Settings | File Templates.
 */
public class DBWORK extends Activity {
    LinearLayout llMain;
    ImageButton btnAdd;
    ImageButton btnDel;
    ImageButton btnSave;
    Spinner spinnerTbl;
    Spinner spinnerRow;
    ScrollView scrollView;
    int wrapContent = LinearLayout.LayoutParams.WRAP_CONTENT;
    int fillParent = LinearLayout.LayoutParams.FILL_PARENT;
    DatabaseHelper dbh;
    SQLiteDatabase sqdb;
    String tbl_name;
    SharedPreferences sPref;//
    ArrayList datatbl;
    ArrayList datarow;
    ArrayAdapter<String> adapter1;
    String db_name;
    boolean isSD;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fdbwork);
        AdapterView.OnItemSelectedListener ClickSpinRow= new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (spinnerRow.getSelectedItem()!=null){
                    llMain.removeAllViews();
                    LoadRow(spinnerRow.getSelectedItemPosition()+1);

                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
                llMain.removeAllViews();
            }
        };
        View.OnClickListener clickBtn= new AdapterView.OnClickListener(){
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btnAdd:
                        btnAdd.setEnabled(false);
                        llMain.removeAllViews();
                        AppendRow();
                        break;
                    case R.id.btnDel:
                        DeleteRow(spinnerRow.getSelectedItemPosition()+1);
                        Toast.makeText(v.getContext(), "Удалено", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.btnSave:
                        if(!btnAdd.isEnabled()){
                         InsertRow();
                         btnAdd.setEnabled(true);
                         Toast.makeText(v.getContext(), "Добавлено", Toast.LENGTH_SHORT).show();
                        }else{
                        SavedRow(spinnerRow.getSelectedItemPosition()+1);
                        Toast.makeText(v.getContext(), "Сохранено", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        };
        scrollView=(ScrollView) findViewById(R.id.scrollView);
        llMain = (LinearLayout) findViewById(R.id.llMain);

        spinnerTbl = (Spinner) findViewById(R.id.spinnerTbl);
        spinnerRow = (Spinner) findViewById(R.id.spinnerRow);
        spinnerRow.setOnItemSelectedListener(ClickSpinRow);
        btnAdd = (ImageButton) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(clickBtn);

        btnDel = (ImageButton) findViewById(R.id.btnDel);
        btnDel.setOnClickListener(clickBtn);
        btnSave = (ImageButton) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(clickBtn);
        btnAdd.setEnabled(false);
        btnDel.setEnabled(false);
        btnSave.setEnabled(false);
        sPref= getSharedPreferences("PrefINI", MODE_PRIVATE);
        // Принимаем имя
        String db_name = getIntent().getStringExtra("Db_name");

// Принимаем фамилию
        boolean isSD = getIntent().getBooleanExtra("isSD",false);
        if(isSD){
        dbh = new DatabaseHelper(this,db_name,isSD,sPref.getInt(db_name,1));
        }
        else{
        dbh = new DatabaseHelper(this,db_name,sPref.getInt(db_name,1));
        }
        try{
        // База нам нужна для записи и чтения
        sqdb = dbh.getWritableDatabase();
         loadSpin();
        }catch(SQLiteException e){

        }

    }


   public void SavedRow(int position){
       Cursor c1 = sqdb.rawQuery("SELECT * FROM "  +tbl_name,null);
       try {
       if(c1.move(position)){
           ContentValues values = new ContentValues() ;
           ContentValues valuesEmpty = new ContentValues() ;
       for(int i=0;i < c1.getColumnCount(); ++i){
           EditText edTxt=(EditText) findViewById(i);
        //   Toast.makeText(this, edTxt.getText(), Toast.LENGTH_SHORT).show();
           if(!c1.getColumnName(i).equalsIgnoreCase("hash_code")& !c1.getColumnName(i).equalsIgnoreCase("status")&
           !c1.getColumnName(i).equalsIgnoreCase("hash_codeCL")){
            if (!String.valueOf(edTxt.getText()).isEmpty())//equals(""))
            {
            values.put(c1.getColumnName(i), String.valueOf(edTxt.getText()));
            }
            else
            valuesEmpty.put(c1.getColumnName(i), "");
           }
       }
           values.put("hash_codeCL",values.hashCode());
           if (c1.getInt( c1.getColumnIndex("status"))!=1){
            values.put("status", "2");
            datarow.set(position-1,position+"[Update]");
           }
        sqdb.update(tbl_name,values,"hash_code=?",new String[] {c1.getString(c1.getColumnIndex("hash_code"))});
        sqdb.update(tbl_name,valuesEmpty,"hash_code=?",new String[] {c1.getString(c1.getColumnIndex("hash_code"))});

        adapter1.notifyDataSetChanged();
       }
       }finally {
        c1.close();
        btnSave.setEnabled(false);
       }


   }
    public void DeleteRow(int position){
        Cursor c1 = sqdb.rawQuery("SELECT * FROM "  +tbl_name,null);
        try{
        if(c1.move(position)){
            ContentValues values = new ContentValues() ;
            values.put("status", 3);
            sqdb.update(tbl_name,values,"hash_code=?",new String[] {c1.getString(c1.getColumnIndex("hash_code"))});
            datarow.set(position-1,position+"[Delete]");
        }
        else{
         datarow.remove(position-1);
         spinnerRow.setSelection(datarow.size()-1);
         btnAdd.setEnabled(true);
        }  }finally {
            c1.close();
            adapter1.notifyDataSetChanged();
        }

    }

    public void AppendRow(){
        datarow.add("[New Record]");
        adapter1.notifyDataSetChanged();
        spinnerRow.setSelection(datarow.size()-1);

    }

    public void InsertRow(){
        Cursor c1 = sqdb.rawQuery("SELECT * FROM "  +tbl_name,null);
        try{
         ContentValues values = new ContentValues() ;
            for(int i=0;i < c1.getColumnCount(); ++i){
             EditText edTxt=(EditText) findViewById(i);
                if(!c1.getColumnName(i).equalsIgnoreCase("hash_code")& !c1.getColumnName(i).equalsIgnoreCase("status")&
                   !c1.getColumnName(i).equalsIgnoreCase("hash_codeCL")     ){
                    values.put(c1.getColumnName(i), String.valueOf(edTxt.getText()));
                }
            }
            int hashcod=values.hashCode();
            values.put("hash_codeCL",hashcod);
            values.put("hash_code", hashcod);
            values.put("status", "1");
            sqdb.insert(tbl_name,null,values);
        datarow.set(datarow.size()-1,datarow.size()+"[Insert]");
        adapter1.notifyDataSetChanged();
        }finally {
            c1.close();
        }


    }



    public void AppendControl(String nameF,String typeF,String dataF,int Pole){
        if (nameF.equalsIgnoreCase("hash_code")||nameF.equalsIgnoreCase("hash_codeCL")){
         return;
        }
        // Создание LayoutParams c шириной и высотой по содержимому
        LinearLayout.LayoutParams eParams = new LinearLayout.LayoutParams(
                fillParent, wrapContent);
        LinearLayout.LayoutParams tParams = new LinearLayout.LayoutParams(
                wrapContent, wrapContent);
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                fillParent, wrapContent);
        // переменная для хранения значения выравнивания
        // по умолчанию пусть будет LEFT
        int edGravity = Gravity.LEFT;
        int txtGravity = Gravity.CENTER_HORIZONTAL;
        int lGravity = Gravity.LEFT;
        // переносим полученное значение выравнивания в LayoutParams
        eParams.gravity = edGravity;

        tParams.gravity = txtGravity;
        lParams.gravity = lGravity;


        // создаем Button, пишем текст и добавляем в LinearLayout
        LinearLayout l_layout = new LinearLayout(this);
        l_layout.setOrientation(LinearLayout.HORIZONTAL);
        TextView txtNew=new TextView(this);
        EditText edTxt=new EditText(this);

          if (typeF.equals("TEXT")){
            edTxt.setInputType(InputType.TYPE_CLASS_TEXT);
          }else if(typeF.equals("INTEGER")){
            edTxt.setInputType(InputType.TYPE_CLASS_NUMBER);
          }

        txtNew.setText(nameF);
        edTxt.setText(dataF);
        edTxt.setTextSize(12);
        edTxt.setId(Pole);
        edTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                btnSave.setEnabled(true);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        txtNew.setTextSize(12);
        l_layout.addView(edTxt, eParams);
        llMain.addView(txtNew, tParams);
        llMain.addView(l_layout, lParams);
        btnDel.setEnabled(true);

    }

    public void LoadRow(int Row){
        Cursor c1 = sqdb.rawQuery("SELECT * FROM "  +tbl_name,null);
        Cursor cType = sqdb.rawQuery("PRAGMA table_info('"+tbl_name+"')",null);//type = 'table' and
       try{
        if(c1.move(Row))
        {
            while(cType.moveToNext()){
                String fName= cType.getString(cType.getColumnIndex("name"));
                String fType= cType.getString(cType.getColumnIndex("type"));
                if(!fName.equalsIgnoreCase("status")){
                    AppendControl(fName,fType,c1.getString(c1.getColumnIndex(fName)),c1.getColumnIndex(fName));
                }
            }

        }
        else{
            while(cType.moveToNext()){
                String fName= cType.getString(cType.getColumnIndex("name"));
                String fType= cType.getString(cType.getColumnIndex("type"));
                if(!fName.equalsIgnoreCase("status")){
                    AppendControl(fName,fType,"",c1.getColumnIndex(fName));
                }
            }
        }
       }finally {
           c1.close();
           cType.close();
           btnSave.setEnabled(false);
       }

    }
    public void loadSpin(){

                datatbl=new  ArrayList();
                datarow=new  ArrayList();
                Cursor c = sqdb.rawQuery("SELECT name FROM sqlite_master"  +
                        " WHERE type='table' and name<>'android_metadata'"+
                        " and not name like ('tmp_%')"+
                        " ORDER BY name",null);
                try{
                while (c.moveToNext()) {
                    datatbl.add(c.getString(0));
                }
                }
                finally {
                    c.close();
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, datatbl);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerTbl.setAdapter(adapter);
        if(datatbl.isEmpty()){spinnerTbl.setEnabled(false);}

        if (spinnerTbl.getSelectedItem()!=null){
            btnAdd.setEnabled(true);
            tbl_name=spinnerTbl.getSelectedItem().toString();


            Cursor c1 = sqdb.rawQuery("SELECT * FROM "  +tbl_name,null);
            try{
                while (c1.moveToNext())
                {
                  switch (c1.getInt(c1.getColumnIndex("status"))){
                      case 0:
                       datarow.add(c1.getPosition()+1+"[Server]");break;
                      case 1:
                       datarow.add(c1.getPosition()+1+"[Insert]");break;
                      case 2:
                       datarow.add(c1.getPosition()+1+"[Update]");break;
                      case 3:
                       datarow.add(c1.getPosition()+1+"[Delete]");break;

                  }
                }
                 adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, datarow);
                spinnerRow.setAdapter(adapter1);
                if(datarow.isEmpty()){spinnerRow.setEnabled(false);}
            }
            finally {
                c1.close();

            }
        }else{spinnerRow.setEnabled(false);}




    }

}
