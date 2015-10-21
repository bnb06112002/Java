package com.example.readLogSDroid;

import android.app.Activity;


import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Xml;
import android.view.View;
import android.widget.*;
import org.xmlpull.v1.XmlPullParser;

import java.io.*;
import java.net.*;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;


public class MyActivity extends Activity  {
    ImageButton tBtn;
    Spinner spinner;
    ProgressBar pbDownload;
    ProgressDialog dialog;

    int id;
    Handler h;
    Message msg;
    GridView gridView;
    ArrayList<item> gridArray = new ArrayList<item>();
    CustomGridViewAdapter customGridAdapter;
    int serverPort;// = 1023; // здесь обязательно нужно указать порт к которому привязывается сервер.
    String address;// = "192.168.1.2"; // это IP-адрес компьютера, где исполняется наша серверная программа.
    String FILE_NAME;
    String SALEIT_DIR="SALEITDIR";
    int FILE_SIZE;
    boolean  Result_Cl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
       // tBtn=(ToggleButton) findViewById(R.id.toggleButton);
         tBtn=(ImageButton) findViewById(R.id.refresh);
        spinner = (Spinner) findViewById(R.id.spinnerServer);
        pbDownload=(ProgressBar) findViewById(R.id.pbDownload);

        dialog = new ProgressDialog(this);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                if(id!=arg2){
                  id=arg2;
                 //   tBtn.setChecked(false);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

         h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                // обновляем TextView

                switch (msg.what)
                {

                    case 0: {setGrid((String) msg.obj);
                        tBtn.setEnabled(false);
                       // tBtn.setChecked(true);

                        break;}
                    case 1: {//tBtn.setChecked(false);
                        tBtn.setEnabled(true);
                        pbDownload.setProgress(0);
                        pbDownload.setVisibility(View.GONE);

                        dialog.dismiss();
                        break;}
                    case 2: { pbDownload.setProgress(msg.arg1);break;}
                    case 3: {  pbDownload.setMax(msg.arg1);
                        pbDownload.setProgress(0);
                        pbDownload.setVisibility(1);
                        break;}
                    case 4: {

                        dialog.setMessage("Synchronization...");
                        dialog.setIndeterminate(false);
                        dialog.setCancelable(false);
                        dialog.show();

                        break;}
                  /*  case 5: {tvInfo.setText("Creat Socket Fail!");break;}
                    case 6: {tvInfo.setText("Не отправленны данные!");break;}
                    case 7: {tvInfo.setText("Не полученны данные!");break;}
                    case 8: {tvInfo.setText("s_data!");break;}  */
                }
            };
        };

        loadSpin();
    }



    //загрузка в Список выбора настроечных данных коннекта к серверу
    public void loadSpin(){
        Integer i;
        ArrayList data = new ArrayList();
        SharedPreferences sPref;
        sPref = getSharedPreferences("PrefINI", MODE_PRIVATE);
       // libMetod lib=new libMetod();
       // lib.getSpin(this,sPref,spinner);
         i=0;
        String savedText = sPref.getString("SERVER_HOST"+String.valueOf(i),"");
        while(savedText!=""){
            data.add(savedText+":"+sPref.getString("SERVER_PORT"+String.valueOf(i), ""));
            i++;
            savedText = sPref.getString("SERVER_HOST"+String.valueOf(i),"");
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        spinner.setAdapter(adapter);

    }



    //открытие сокета, отправка запроса, получение сообщения
    public void mainSocket(String aPar) {
        InetAddress ipAddress;
        Socket msocket = null;
        BufferedReader inb = null;
        DataOutputStream out = null;
        DataInputStream input = null;
        try {
            h.sendEmptyMessage(4);
            ipAddress = InetAddress.getByName(address); // создаем объект который отображает вышеописанный IP-адрес.

            msocket = new Socket();
            //msocket = new Socket(ipAddress, serverPort); // создаем сокет используя IP-адрес и порт сервера.
            msocket.connect(new InetSocketAddress(ipAddress, serverPort),5000);
            msocket.setSoTimeout(60000);


            // Берем входной и выходной потоки сокета, теперь можем получать и отсылать данные клиентом.
            // Конвертируем потоки в другой тип, чтоб легче обрабатывать текстовые сообщения.
            inb = new BufferedReader(new InputStreamReader(msocket.getInputStream(),"windows-1251"));
            out = new DataOutputStream(msocket.getOutputStream());
            input = new DataInputStream(msocket.getInputStream());


            out.writeUTF(aPar); // отсылаем введенную строку текста серверу.
            out.flush(); // заставляем поток закончить передачу данных.

            int result;
            int b;
            String lineIn = "";

            while (true) {

                result = inb.read();
                lineIn="";
                StringBuffer response = new StringBuffer();
                while (result != 35) {
                    response.append(Character.toChars(result));
                    lineIn=lineIn+(char)result;
                    result = inb.read();
                }

                break;

            }
            msg=h.obtainMessage(0,lineIn);
            h.sendMessage(msg);
            out.writeUTF("201"); // отсылаем серверу что мы успешно приняли данные.
            out.flush(); // заставляем поток закончить передачу данных.
            //
            out.writeUTF("102"); // запрос файла.
            out.flush(); // заставляем поток закончить передачу данных.
            //ждём подтверждение формирования файла
            lineIn = "";
            while (true) {
                result = inb.read();
                lineIn="";
                StringBuffer response = new StringBuffer();
                while (result != 35) {
                    response.append(Character.toChars(result));
                    lineIn=lineIn+(char)result;
                    result = inb.read();
                }

                break;
            }
            //строка ответа должна содержать название файла и его размер
            FILE_NAME="";
            FILE_SIZE=0;
            Result_Cl=false;
            if (lineIn.isEmpty())
            {
                String s="Server|Empty Message";
                msg=h.obtainMessage(0, s);
                h.sendMessage(msg);
                out.writeUTF("202"); // ошибка клиента.
                out.flush(); // заставляем поток закончить передачу данных.
                out.writeUTF("200"); // отсылаем серверу что мы отключились.
                out.flush(); // заставляем поток закончить передачу данных.
                msocket.close();
            }else
            {
                String[] strTokens;
                strTokens=lineIn.split("[|]");
                if (strTokens.length==4){
                FILE_NAME=strTokens[2];
                FILE_SIZE= Integer.parseInt(strTokens[3]);
                    msg=h.obtainMessage(0,lineIn);
                    h.sendMessage(msg);

                    CrFile();
                    msg=h.obtainMessage(3,FILE_SIZE,0);
                    h.sendMessage(msg);
                 if (!Result_Cl){
                     out.writeUTF("202"); // ошибка клиента.
                     out.flush(); // заставляем поток закончить передачу данных.
                     msocket.close();
                 }
                 out.writeUTF("103"); // готовы принимать.
                 out.flush(); // заставляем поток закончить передачу данных.

                int length,count,rest;
                count=0;
                 if (FILE_SIZE>1024){
                 rest=1024;
                 }else{rest=FILE_SIZE;}

                byte[] bytes = new byte[rest];
                while ((length =  input.read(bytes,0,rest)) != -1)
                    {
                        count=count+length;
                        rest=FILE_SIZE-count;
                        if(rest>1024){
                            rest=1024;
                        }

                        saveFile(bytes);
                        msg=h.obtainMessage(2,count,0);
                        h.sendMessage(msg);
                        if (!Result_Cl){
                            out.writeUTF("202"); // ошибка клиента.
                            out.flush(); // заставляем поток закончить передачу данных.
                            msocket.close();
                        }
                        bytes = new byte[rest];
                        if (count==FILE_SIZE){
                            out.writeUTF("201"); // успешно приняли данные.
                            out.flush(); // заставляем поток закончить передачу данных.
                            break;}
                    }
                } else{
                    String s="Server|Error Protokol";
                    msg=h.obtainMessage(0,s);
                    h.sendMessage(msg);
                }
            }


            out.writeUTF("200"); // отсылаем серверу что мы отключились.
            out.flush(); // заставляем поток закончить передачу данных.
        } catch (SocketException x) {
            String s="Socket|"+x.getMessage();
            msg=h.obtainMessage(0,s);
            h.sendMessage(msg);
        } catch (UnsupportedEncodingException e) {
            String s="Encoding|" + e.getMessage();
            msg=h.obtainMessage(0,s);
            h.sendMessage(msg);
        } catch (UnknownHostException e) {
            String s="Host|"+e.getMessage();
            msg=h.obtainMessage(0,s);
            h.sendMessage(msg);
        } catch (IOException e) {
            String s="IO|"+e.getMessage();
            msg=h.obtainMessage(0,s);
            h.sendMessage(msg);
        }
        finally {
            if (input!=null){
                try {
                    input.close();
                } catch (IOException e) {
                    String s="input|" + e.getMessage();  //To change body of catch statement use File | Settings | File Templates.
                    msg=h.obtainMessage(0,s);
                    h.sendMessage(msg);
                }
            }
            try { if (msocket!=null){
                msocket.close(); }
            } catch (IOException e) {
                String s="socket|" + e.getMessage();  //To change body of catch statement use File | Settings | File Templates.
                msg=h.obtainMessage(0,s);
                h.sendMessage(msg);
            }
            try { if (inb!=null){
                inb.close();}
            } catch (IOException e) {
                String s="inb|" + e.getMessage();  //To change body of catch statement use File | Settings | File Templates.
                msg=h.obtainMessage(0,s);
                h.sendMessage(msg);
            }
            try { if (out!=null){
                out.close(); }
            } catch (IOException e) {
                String s="out|" + e.getMessage();  //To change body of catch statement use File | Settings | File Templates.
                msg=h.obtainMessage(0,s);
                h.sendMessage(msg);
            }
           h.sendEmptyMessage(1);
        }
        //запускаем поток распаковки файла

    }

    public void servertoDB(){
        FILE_NAME="tbl_RoutesN.xml";
        FILE_SIZE=9380;
        if (FILE_NAME==null||FILE_SIZE==0){
            return;
        }

            // получаем путь к SD
            File sdPath = Environment.getExternalStorageDirectory();
            // добавляем свой каталог к пути
            sdPath = new File(sdPath.getAbsolutePath() + "/"+SALEIT_DIR);
            // формируем объект File, который содержит путь к файлу
            File sdFile = new File(sdPath, FILE_NAME);
         // File sdFile = new File(FILE_NAME);
            if (!sdFile.exists()){
             //выводимо сообщение что по заданому пути файл не найден
             return;
            } else{
             if(sdFile.length()!=FILE_SIZE){
              //выводим сообщение что размеры файла не совпали
              return;
             }

         //парсим файл на выходе обьект для создания БД
                dbXML dxml=new dbXML(this,sdFile.getPath());
                dxml.onCreate();
                //dxml.TABLE_NAME
            // Инициализируем наш класс-обёртку
          //  DatabaseHelper dbh = new DatabaseHelper(this,sdPath+"/"+FILE_NAME);
            DatabaseHelper dbh = new DatabaseHelper(this,dxml);

// База нам нужна для записи и чтения
            SQLiteDatabase sqdb = dbh.getWritableDatabase();
                /*Блок синхронизации от сервера*/
                // Удаляем записи из тмп таблицы
                sqdb.delete("tmp_"+dxml.TABLE_NAME,null,null);
                // Добавляем записи в таблицу tmp
                for (int y = 0; y < dxml.DATA.size(); ++y){
                    ContentValues values = (ContentValues) dxml.DATA.get(y);
                    sqdb.insert("tmp_"+dxml.TABLE_NAME, null, values);
                }
                //Удаляем записи из правильной таблицы где хешкод не совпал и статус этих записей 0(пришли из сервера)
               // String strsql="delete "+dxml.TABLE_NAME+" where not hash_code in(select hash_code from tmp_"+dxml.TABLE_NAME+")and status=0";
               // sqdb.execSQL(strsql);
                //Добавляем в правильную таблицу недостающие записи от сервера
                String sqlStr="insert into %1$s (%2$s) " +
                        "select * from %3$s a where not a.hash_code IN(select hash_code from %1$s)";
                StringBuilder fieldStr= new StringBuilder();
                for(int i=0;i < dxml.FIELD.size(); ++i){
                    if(i>0){fieldStr.append(",");}
                  fieldStr.append(dxml.FIELD.get(i).toString());
                }
                sqdb.execSQL(String.format(sqlStr,dxml.TABLE_NAME,fieldStr.toString(),"tmp_"+dxml.TABLE_NAME));
                /*Блок синхронизации от сервера*/

                /*Cursor c = sqdb.rawQuery("SELECT name FROM sqlite_master"  +
                        " WHERE type='table' and name<>'android_metadata'"+
                        " ORDER BY name",null);
                String s="";
                while (c.moveToNext()) {
                s= c.getString(0);
                System.out.println(s);
                }
              c.close();*/
            /* Cursor c1 = sqdb.rawQuery(String.format("SELECT * FROM %s", dxml.TABLE_NAME),null);
              while (c1.moveToNext()) {
                    Integer i=c1.getColumnCount();
                    for (int y = 0; y < i; ++y){
                    StringBuilder s1=new StringBuilder();
                    s1.append(c1.getColumnName(y)).append("=").append(c1.getString(y));
                   // s=s+"["+s1.toString()+"]";
                    }
                 // System.out.println(s);
                }
                c1.close();
             */
             /*   ResultSet rs = sqdb.execSQL("SELECT name FROM sqlite_master\n" +
                                        "                WHERE type='table'\n" +
                                        "                ORDER BY name");*/

// закрываем соединения с базой данных
            sqdb.close();
            dbh.close();

        }

    }



    // прорисовка сообщений в гриде
    public void setGrid(String pStr) {
        String[] strTokens;
        String strTime, strDistr, strMsg;
        int intStatus;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.S");
        strTokens = pStr.split("[|]");
        if (strTokens.length != 4) {
            intStatus = 100;
            strTime = sdf.format(new Date());
            strDistr = strTokens[0];
            strMsg = strTokens[1];
        } else{
        intStatus = Integer.parseInt(strTokens[0]);
        strTime = strTokens[1];
        strDistr = strTokens[2];
        strMsg = strTokens[3];
        }

        Bitmap stIcon = null;
        switch (intStatus) {
            case 100:
               stIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.sterrorui);
                break;
            case 9:
                 stIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.ststatserver);
                break;
            case 2:
                stIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.stwarningserver);
                break;
            case 1:
                 stIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.sterrorserver);
                break;
            default:
                stIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.stserver);
        }


        gridArray.add(0,new item(stIcon,strTime,strDistr,strMsg));

        gridView = (GridView) findViewById(R.id.gridView);
        customGridAdapter = new CustomGridViewAdapter(this, R.layout.row_grid, gridArray);
        gridView.setAdapter(customGridAdapter);
    }



   //реакция на клик кнопки подкоючения к сокету
    public void onTobinClick(View v) throws IOException {
        String msg;

        if (spinner.getSelectedItem()!=null){

            String[] strTokens;
            strTokens =  spinner.getSelectedItem().toString().split("[:]");
            if (strTokens.length!=2){
                serverPort= 1023; // здесь обязательно нужно указать порт к которому привязывается сервер.
                address = "192.168.1.2";
            }else{
                address=strTokens[0];
                serverPort=Integer.parseInt(strTokens[1]);
            }
       // if (tBtn.isChecked())
        {
          try {
              tBtn.setEnabled(false);

              Thread t = new Thread(new Runnable() {
                  public void run() {mainSocket("101");}});
           t.start();

              // msg=tBtn.getTextOn().toString();
              msg="";
          }
          catch(Exception e) {
             // tBtn.setChecked(false);
              msg=e.getMessage();//tBtn.getTextOff().toString();

          }


        }



        msg=msg+":"+ spinner.getSelectedItem().toString();
        Toast.makeText(getBaseContext(), msg,Toast.LENGTH_LONG).show();
      } else{
         //   tBtn.setChecked(false);
            Toast.makeText(getBaseContext(),"Необходимо выбрать сервер.",Toast.LENGTH_LONG).show();

        }
    }

    public void onSettingClick(View v)
    {
        Intent intent = new Intent(this, Settings.class);
        startActivityForResult(intent, 0);
       // startActivity(intent);

       // finish();
    }

    public void onOpenDb(View v)
    {
        servertoDB();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            if (data!=null){
              if (data.getBooleanExtra("spiner",false)){
                   loadSpin();
            }
          }
        }
    }
    void  writeFile(byte[] bytes) {
        try {
            // отрываем поток для записи
            InputStream in = openFileInput(FILE_NAME);
            OutputStream outputstream = openFileOutput(FILE_NAME, MODE_APPEND);//MODE_APPEND
            // пишем данные

            outputstream.write(bytes);

            // закрываем поток
            outputstream.close();

        } catch (FileNotFoundException e) {
            String s="wFile|" + e.getMessage();  //To change body of catch statement use File | Settings | File Templates.
            msg=h.obtainMessage(0,s);
            h.sendMessage(msg);

        } catch (IOException e) {
            String s="wFile|" + e.getMessage();  //To change body of catch statement use File | Settings | File Templates.
            msg=h.obtainMessage(0,s);
            h.sendMessage(msg);

        }
    }
    void createFile() {
        try {
            // отрываем поток для записи
            InputStream in = openFileInput(FILE_NAME);
            OutputStream outputstream = openFileOutput(FILE_NAME, MODE_PRIVATE);//MODE_APPEND
            // пишем данные
            byte[] bytes=new byte[0];
            outputstream.write(bytes);
            // закрываем поток
            outputstream.close();
        } catch (FileNotFoundException e) {
            String s="wFile|" + e.getMessage();  //To change body of catch statement use File | Settings | File Templates.
            msg=h.obtainMessage(0,s);
            h.sendMessage(msg);;
        } catch (IOException e) {
            String s="wFile|" + e.getMessage();  //To change body of catch statement use File | Settings | File Templates.
            msg=h.obtainMessage(0,s);
            h.sendMessage(msg);
        }
    }
    void createFileSD(){
        // получаем путь к SD
        File sdPath = Environment.getExternalStorageDirectory();
        // добавляем свой каталог к пути
        sdPath = new File(sdPath.getAbsolutePath() + "/"+SALEIT_DIR);
        // создаем каталог
        sdPath.mkdirs();
        // формируем объект File, который содержит путь к файлу
        File sdFile = new File(sdPath, FILE_NAME);
        sdFile.delete();
        try {
            sdFile.createNewFile();
            Result_Cl=true;
        } catch (IOException e) {
            String s="wSD|" + e.getMessage();  //To change body of catch statement use File | Settings | File Templates.
            msg=h.obtainMessage(0,s);
            h.sendMessage(msg);  //To change body of catch statement use File | Settings | File Templates.
            Result_Cl=false;
        }
    }
    void writeFileSD(byte[] bytes) {
        // получаем путь к SD
        File sdPath = Environment.getExternalStorageDirectory();
        // добавляем свой каталог к пути
        sdPath = new File(sdPath.getAbsolutePath() + "/"+SALEIT_DIR);

        // формируем объект File, который содержит путь к файлу
        File sdFile = new File(sdPath, FILE_NAME);

        try {

            // открываем поток для записи
           // FileWriter f0 = new FileWriter(sdFile);
            FileOutputStream fos = new FileOutputStream(sdFile,true);
            // пишем данные

            fos.write(bytes);

            // закрываем поток
            fos.close();
            Result_Cl=true;
        } catch (IOException e) {
            String s="wSD|" + e.getMessage();  //To change body of catch statement use File | Settings | File Templates.
            msg=h.obtainMessage(0,s);
            h.sendMessage(msg);
            Result_Cl=false;
        }
    }
    // Метод для сохранения файла
    public void CrFile() {
        // проверяем доступность SD
        String SDState = Environment.getExternalStorageState();
        if (SDState.equals(Environment.MEDIA_REMOVED) ||
                SDState.equals(Environment.MEDIA_UNMOUNTED) || SDState.equals(Environment.MEDIA_NOFS))
        {
            createFile();
        }
        else{createFileSD();}


    }
    // Метод для сохранения файла
    private void saveFile(byte[] bytes) {
        // проверяем доступность SD
        String SDState = Environment.getExternalStorageState();
        if (SDState.equals(Environment.MEDIA_REMOVED) ||
                SDState.equals(Environment.MEDIA_UNMOUNTED) || SDState.equals(Environment.MEDIA_NOFS))
         {
            writeFile(bytes);
        }
        else{writeFileSD(bytes);}


    }

}
