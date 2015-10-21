package com.example.syncDBDroid;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.*;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Settings extends Activity {

    SharedPreferences sPref;
    EditText tServer,tPort,tNameDB;
    TextView fServer,fPort,fNameDB;
    CheckBox isSD;
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
                StringTokenizer t = new StringTokenizer(st.nextToken(), "/") ;
                tNameDB.setText(t.nextToken());
                try{
                if (t.nextToken().equals("SD"))
                 isSD.setChecked(true);
                else
                 isSD.setChecked(false);
                }catch (Exception e){
                 isSD.setChecked(false);
                }

                countList=position;
               /* Toast.makeText(getApplicationContext(), "itemClick: position = " +
                        position + ", id = " + id + ", i="+itemStr +" "+ parent.getAdapter().getItem(position),
                        Toast.LENGTH_SHORT).show();*/
            }
        });
    }
    void initField(){
        tServer = (EditText) findViewById(R.id.edServer);
        tPort = (EditText) findViewById(R.id.edPort);
        tNameDB=(EditText) findViewById(R.id.edNameDB);
        fServer = (TextView) findViewById(R.id.fServer);
        fPort = (TextView) findViewById(R.id.fPort);
        fNameDB= (TextView) findViewById(R.id.fNameDB);
        isSD=(CheckBox)findViewById(R.id.isSD);
        lv = (ListView)findViewById(R.id.listServer);
    }

    public void onBtnAddClick(View view)
    {

        if (tServer.getText().toString().isEmpty() || tPort.getText().toString().isEmpty()||tNameDB.getText().toString().isEmpty() ) {
            Toast.makeText(this, "Field: " + fServer.getText()+ " or "+fPort.getText()+ " or "+fNameDB.getText()+ " can not be empty", Toast.LENGTH_SHORT).show();

        } else{
        countList=arrServer.size();
        saveServer();
        tServer.setText("");
        tPort.setText("");
        tNameDB.setText("");
        isSD.setChecked(false);
        }


    }

    public void onBtnSaveClick(View view)
    {

        if (tServer.getText().toString().isEmpty() || tPort.getText().toString().isEmpty()||tNameDB.getText().toString().isEmpty() ) {
            Toast.makeText(this, "Field: " + fServer.getText()+ " or "+fPort.getText()+ " or "+fNameDB.getText()+ " can not be empty", Toast.LENGTH_SHORT).show();

        } else{
        saveServer();
        tServer.setText("");
        tPort.setText("");
        tNameDB.setText("");
        isSD.setChecked(false);
        }
    }

    public void DeleteDB(String db_name,boolean is_SD) {
        if(db_name.equals(""))
            return;
        File FileDB;
        File sdcard = Environment.getExternalStorageDirectory();
        FileDB = this.getDatabasePath(db_name);

        if(is_SD&!sdcard.equals(Environment.MEDIA_REMOVED)&
                !sdcard.equals(Environment.MEDIA_UNMOUNTED)&
                !sdcard.equals(Environment.MEDIA_NOFS))
        {
            String dbfile = sdcard.getAbsolutePath() + File.separator+ "databases" + File.separator + db_name;
            if (!dbfile.endsWith(".db"))
            {
                dbfile += ".db" ;
            }

            FileDB = new File(dbfile);
        }

        if (!FileDB.exists())
            return;
        FileDB.delete();
        Toast.makeText(this, "Deleted: " + FileDB.getAbsolutePath(), Toast.LENGTH_SHORT).show();
    }

    public void onBtnDelClick(View view)
    {    if (tServer.getText().toString().isEmpty() || tPort.getText().toString().isEmpty() ) {
        Toast.makeText(this, "Field: " + fServer.getText()+ " or "+fPort.getText()+ " can not be empty", Toast.LENGTH_SHORT).show();

    } else{
        tServer.setText("");
        tPort.setText("");
        tNameDB.setText("");
        isSD.setChecked(false);
        if(countList!=0)
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
            savedText=savedText+":"+sPref.getString("SERVER_PORT"+String.valueOf(i), "")
                            +":"+ sPref.getString("NAME_DB"+String.valueOf(i), "anyDB");
            if (sPref.getBoolean("isSD" + String.valueOf(i), false))
                savedText=savedText+"/SD";
            arrServer.add(savedText);
            i++;
            savedText = sPref.getString("SERVER_HOST"+String.valueOf(i),"");
        }
        countList=arrServer.size();
        loadList();
    }

    void saveServer(){
        SharedPreferences.Editor ed = sPref.edit();
        if (tServer.getText().toString().equals("")){
            DeleteDB(sPref.getString("NAME_DB"+String.valueOf(countList),""),sPref.getBoolean("isSD" + String.valueOf(countList), false));
        ed.remove("SERVER_HOST"+String.valueOf(countList));
        ed.remove("SERVER_PORT"+String.valueOf(countList));
        ed.remove("NAME_DB"+String.valueOf(countList));
        ed.remove("isSD"+String.valueOf(countList));

        ed.commit();
         int i= countList;
          i=i+1;
         while (i<=(arrServer.size()-1)){
             ed.putString("SERVER_HOST"+String.valueOf(i-1),sPref.getString("SERVER_HOST"+String.valueOf(i),""));
             ed.putString("SERVER_PORT"+String.valueOf(i-1), sPref.getString("SERVER_PORT"+String.valueOf(i), ""));
             ed.putString("NAME_DB"+String.valueOf(i-1), sPref.getString("NAME_DB"+String.valueOf(i), ""));
             ed.putBoolean("isSD" + String.valueOf(i - 1), sPref.getBoolean("isSD" + String.valueOf(i), false));
             ed.remove("SERVER_HOST" + String.valueOf(i));
             ed.remove("SERVER_PORT"+String.valueOf(i));
             ed.remove("NAME_DB"+String.valueOf(i));
             ed.remove("isSD"+String.valueOf(i));
             ed.commit();
             i++;
         }
        }else{
        ed.putString("SERVER_HOST" + String.valueOf(countList), tServer.getText().toString());
        ed.putString("SERVER_PORT" + String.valueOf(countList), tPort.getText().toString());
        ed.putString("NAME_DB" + String.valueOf(countList), tNameDB.getText().toString());
        ed.putBoolean("isSD" + String.valueOf(countList),isSD.isChecked());
        ed.commit();}
        answerInent.putExtra("spiner",true);
        setResult(RESULT_OK, answerInent);
        loadServerList();
        Toast.makeText(this, "Server saved", Toast.LENGTH_SHORT).show();
    }
}