package com.example.readLogSDroid;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class Settings extends Activity {

    SharedPreferences sPref;
    EditText tServer,tPort;
    TextView fServer,fPort;
    ListView lv;
    Integer countList;
    ArrayList arrServer;
    Intent answerInent = new Intent();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        sPref = getSharedPreferences("PrefINI", MODE_PRIVATE);



        super.onCreate(savedInstanceState);
        setContentView(R.layout.fsettings);
        initField();
        loadServerList();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemStr;
                itemStr = (String) parent.getAdapter().getItem(position);
                StringTokenizer st = new StringTokenizer(itemStr, ":") ;
                tServer.setText(st.nextToken());
                tPort.setText(st.nextToken());
                countList=position;
                Toast.makeText(getApplicationContext(), "itemClick: position = " +
                        position + ", id = " + id + ", i="+itemStr +" "+ parent.getAdapter().getItem(position),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    void initField(){
        tServer = (EditText) findViewById(R.id.edServer);
        tPort = (EditText) findViewById(R.id.edPort);
        fServer = (TextView) findViewById(R.id.fServer);
        fPort = (TextView) findViewById(R.id.fPort);
        lv = (ListView)findViewById(R.id.listServer);
    }

    public void onBtnAddClick(View view)
    {

        if (tServer.getText().toString().isEmpty() || tPort.getText().toString().isEmpty() ) {
            Toast.makeText(this, "Field: " + fServer.getText()+ " or "+fPort.getText()+ " can not be empty", Toast.LENGTH_SHORT).show();

        } else{
        countList=arrServer.size();
        saveServer();
        tServer.setText("");
        tPort.setText(""); }


    }

    public void onBtnSaveClick(View view)
    {

        if (tServer.getText().toString().isEmpty() || tPort.getText().toString().isEmpty() ) {
            Toast.makeText(this, "Field: " + fServer.getText()+ " or "+fPort.getText()+ " can not be empty", Toast.LENGTH_SHORT).show();

        } else{
        saveServer();
        tServer.setText("");
        tPort.setText("");
        }
    }

    public void onBtnDelClick(View view)
    {    if (tServer.getText().toString().isEmpty() || tPort.getText().toString().isEmpty() ) {
        Toast.makeText(this, "Field: " + fServer.getText()+ " or "+fPort.getText()+ " can not be empty", Toast.LENGTH_SHORT).show();

    } else{
        tServer.setText("");
        tPort.setText("");
        saveServer();}
    }


    void loadList(){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,	android.R.layout.simple_list_item_1, arrServer);
        lv.setAdapter(adapter);
    }

    public void loadServerList(){

        arrServer = new ArrayList();

        Integer i;
        i=0;
        String savedText = sPref.getString("SERVER_HOST"+String.valueOf(i),"");
        while(savedText!="" & sPref.getString("SERVER_PORT"+String.valueOf(i), "")!="" ){
            arrServer.add(savedText+":"+sPref.getString("SERVER_PORT"+String.valueOf(i), ""));
            i++;
            savedText = sPref.getString("SERVER_HOST"+String.valueOf(i),"");
        }
        countList=arrServer.size();
        loadList();




    }

    void saveServer(){
        SharedPreferences.Editor ed = sPref.edit();
        if (tServer.getText().toString().equals("")){
        ed.remove("SERVER_HOST"+String.valueOf(countList));
        ed.remove("SERVER_PORT"+String.valueOf(countList));
        ed.commit();
         int i= countList;
          i=i+1;
         while (i<=(arrServer.size()-1)){
             ed.putString("SERVER_HOST"+String.valueOf(i-1),sPref.getString("SERVER_HOST"+String.valueOf(i),""));
             ed.putString("SERVER_PORT"+String.valueOf(i-1), sPref.getString("SERVER_PORT"+String.valueOf(i), ""));
             ed.remove("SERVER_HOST"+String.valueOf(i));
             ed.remove("SERVER_PORT"+String.valueOf(i));
             ed.commit();
             i++;
         }
        }else{
        ed.putString("SERVER_HOST" + String.valueOf(countList), tServer.getText().toString());
        ed.putString("SERVER_PORT" + String.valueOf(countList), tPort.getText().toString());
        ed.commit();}
        answerInent.putExtra("spiner",true);
        setResult(RESULT_OK, answerInent);
        loadServerList();
        Toast.makeText(this, "Server saved", Toast.LENGTH_SHORT).show();
    }
}