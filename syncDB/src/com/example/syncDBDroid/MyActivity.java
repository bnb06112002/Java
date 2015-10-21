package com.example.syncDBDroid;

import android.app.Activity;


import android.app.ProgressDialog;
import android.content.ContentValues;

import android.content.Intent;
import android.content.SharedPreferences;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;


import android.os.Handler;
import android.os.Message;

import android.view.View;
import android.widget.*;


import java.io.*;
import java.net.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;


public class MyActivity extends Activity  {
    //статусы хендлера
    final int stGrid=0;//добавить в грид лога сообщение
    final int stClearDialog=1;//скрыть диалог и прогресбар
    final int stProgress=2;//показать прогрес в прогресбаре
    final int stDownUpLoadMax=3;//установить прогресбару максимальное значение
    final int stDialog=4;//показать окно диалога с сообщением
    //статусы лога(иконки)
    final int stInfo_icon=-1;  //информационное сообщение андроида
    final int stError_icon=-2; //ошибка андроида
    final int stConnect_icon=9;//коннект к серверу
    final int stServWar_icon=2;//сообщение от сервера(предупреждение)
    final int stServErr_icon=1;//сообщение от сервера(ошибка)
    final int stDisconnect_icon=0;//дисконект
    //запросы,ответы по сокету
    final int sc_requestInfo=101;//запросить информацию о сервере
    final int sc_requestDB=102;//запросить БД у сервера
    final int sc_returnDB=301;//отдать БД серверу

    final int sc_errorCL=202;//отправить серверу сообщение что ошибка на клиенте
    final int sc_disconnectCL=200;
    final int sc_readyCL=103;//готовность клиента принять файл
    final int sc_successCL=201;//клиент успешно принял данные
    final int sc_readySrv=303;//готовность сервера принять данные


    ImageButton tBtn;
    Spinner spinnerServ;
    ProgressBar pbDownload;
    ProgressDialog dialog;
    int id;
    Handler h;
    Message msg;
    GridView gridView;
    ArrayList<item> gridArray = new ArrayList<item>();
    CustomGridViewAdapter customGridAdapter;

   // String FILE_NAME;
    String SALEIT_DIR="SALEITDIR";
   // int FILE_SIZE;
   syncFile outFile;
   syncFile inFile;
    SharedPreferences sPref;
    choiseSetting chSetting;
    //boolean  Result_Cl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

         tBtn=(ImageButton) findViewById(R.id.refresh);
        spinnerServ = (Spinner) findViewById(R.id.spinnerServer);
        pbDownload=(ProgressBar) findViewById(R.id.pbDownload);

        dialog = new ProgressDialog(this);

        spinnerServ.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                if(id!=arg2){
                  id=arg2;
                }
                initConst();

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

         h = new Handler() {
             @Override
            public void handleMessage(android.os.Message msg) {
                // обновляем TextView
                switch (msg.what){
                    case stGrid: {setGrid((String) msg.obj);
                        tBtn.setEnabled(false);

                        break;}
                    case stClearDialog: {
                        tBtn.setEnabled(true);
                        pbDownload.setProgress(0);
                        pbDownload.setVisibility(View.GONE);
                        dialog.dismiss();
                        break;}
                    case stProgress: { pbDownload.setProgress(msg.arg1);break;}
                    case stDownUpLoadMax: { pbDownload.setMax(msg.arg1);
                        pbDownload.setProgress(0);
                        pbDownload.setVisibility(1);
                        break;}
                    case stDialog: {
                        dialog.setMessage("Synchronization..."+msg.obj);
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
        sPref = getSharedPreferences("PrefINI", MODE_PRIVATE);
         i=0;
        String savedText = sPref.getString("SERVER_HOST"+String.valueOf(i),"");
        while(savedText!=""){
            savedText=savedText+":"+sPref.getString("SERVER_PORT"+String.valueOf(i), "")
                    +":"+ sPref.getString("NAME_DB"+String.valueOf(i), "anyDB");
            if (sPref.getBoolean("isSD"+String.valueOf(i), false))
                savedText=savedText+"/SD";
            data.add(savedText);
            i++;
            savedText = sPref.getString("SERVER_HOST"+String.valueOf(i),"");
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        spinnerServ.setAdapter(adapter);
        if (spinnerServ.getSelectedItem()==null){
            initConst();
        }


    }

    public void initConst(){

        if (spinnerServ.getSelectedItem()==null){
          return;
        }
        String[] strTokens;
        chSetting = new choiseSetting();
        strTokens =  spinnerServ.getSelectedItem().toString().split("[:]");
        for(int i=0;i<strTokens.length;++i){
            switch(i){
                case 0:{chSetting.setAddress(strTokens[0]);break;}
                case 1:{chSetting.setServerPort(Integer.parseInt(strTokens[1]));break;}
                case 2:{String[] str=strTokens[2].split("[/]");
                    for(int y=0;y<str.length;++y){
                        switch(y){
                            case 0:{chSetting.setDb_name(str[0]);break;}
                            case 1:{chSetting.setSD(true);break;}
                        }
                    }
                    break;}
            }

        }
    }

    public boolean requestInfoServ(BufferedReader inb, DataOutputStream out,DataInputStream input){
        boolean resultSync;
        resultSync=true;
        try {
            out.writeUTF(Integer.toString(sc_requestInfo)); // отсылаем введенную строку текста серверу.
            out.flush(); // заставляем поток закончить передачу данных.
        } catch (IOException e) {
            resultSync=false;
            String s="IO|"+e.getMessage();
            msg=h.obtainMessage(stGrid,s);
            h.sendMessage(msg);
            return resultSync;
        }

        int result;
        String lineIn = "";
        while (true) {
            try {
                result = inb.read();
                lineIn="";
                StringBuffer response = new StringBuffer();
                while (result != 35) {
                    response.append(Character.toChars(result));
                    lineIn=lineIn+(char)result;
                    result = inb.read();
                }
                break;
            } catch (IOException e) {
                resultSync=false;
                String s="IO|"+e.getMessage();
                msg=h.obtainMessage(stGrid,s);
                h.sendMessage(msg);
                return resultSync;
            }
        }
        msg=h.obtainMessage(stGrid,lineIn);
        h.sendMessage(msg);
        return resultSync;
    }

    public boolean requestSyncServ(BufferedReader inb, DataOutputStream out,DataInputStream input)  {
        boolean resultSync;
        resultSync=true;
        int result;
        String lineIn = "";
        //строка ответа должна содержать название файла и его размер
        String FILE_NAME="";
        long FILE_SIZE=0;
       // syncFile sFile;
        try {
        out.writeUTF(Integer.toString(sc_requestDB)); // запрос файла.
        out.flush(); // заставляем поток закончить передачу данных.
        } catch (IOException e) {
            resultSync=false;
            String s="IO|"+e.getMessage();
            msg=h.obtainMessage(stGrid,s);
            h.sendMessage(msg);
            return resultSync;
        }

        //ждём подтверждение формирования файла
        lineIn="";
            while (true) {
                try {
                 result = inb.read();
                if(result==-1){break;}
                StringBuffer response = new StringBuffer();
                while (result != 35) {
                    response.append(Character.toChars(result));
                    lineIn=lineIn+(char)result;
                    result = inb.read();
                }
                } catch (IOException e) {
                    resultSync=false;
                    String s="IO|"+e.getMessage();
                    msg=h.obtainMessage(stGrid,s);
                    h.sendMessage(msg);
                    return resultSync;
                }
                break;
            }



           // Result_Cl=false;
            if (lineIn.isEmpty())
            {
                String s="Server|Empty Message";
                msg=h.obtainMessage(stGrid, s);
                h.sendMessage(msg);
                try {
                    out.writeUTF(Integer.toString(sc_errorCL)); // ошибка клиента.
                    out.flush(); // заставляем поток закончить передачу данных.
                    out.writeUTF(Integer.toString(sc_disconnectCL)); // отсылаем серверу что мы отключились.
                    out.flush(); // заставляем поток закончить передачу данных.
                } catch (IOException e) {
                    resultSync=false;
                    String s1="IO|"+e.getMessage();
                    msg=h.obtainMessage(stGrid,s1);
                    h.sendMessage(msg);
                    return resultSync;
                }
                resultSync=false;
                return resultSync;
            }
              String[] strTokens;
               strTokens=lineIn.split("[|]");
              if (strTokens.length!=4)
              {
               resultSync=false;
               //String s="Server|Error Protokol";
                msg=h.obtainMessage(stGrid, lineIn);
                h.sendMessage(msg);
                return resultSync; }


                    FILE_NAME=strTokens[2];
                    FILE_SIZE= Integer.parseInt(strTokens[3]);
                    msg=h.obtainMessage(stGrid,lineIn);
                    h.sendMessage(msg);
                     inFile=new syncFile(this,FILE_NAME,FILE_SIZE);
                      if (!inFile.createFile())
                       {
                        try {
                        out.writeUTF(Integer.toString(sc_errorCL)); // ошибка клиента.
                        out.flush(); // заставляем поток закончить передачу данных.
                        } catch (IOException e) {
                            resultSync=false;
                            String s="IO|"+e.getMessage();
                            msg=h.obtainMessage(stGrid,s);
                            h.sendMessage(msg);
                            return resultSync;
                        }
                        msg=h.obtainMessage(stGrid,inFile.errorMsg);
                        h.sendMessage(msg);
                        return false;
                       }

                        msg=h.obtainMessage(stDownUpLoadMax, (int) FILE_SIZE,0);
                        h.sendMessage(msg);



        try {
            out.writeUTF(Integer.toString(sc_readyCL)); // готовы принимать.
            out.flush(); // заставляем поток закончить передачу данных.
        } catch (IOException e) {
            resultSync=false;
            msg=h.obtainMessage(stGrid,"IO|"+e.getMessage());
            h.sendMessage(msg);
            return resultSync;
        }

        int length,count,rest;
        count=0;
        if (FILE_SIZE>1024){
            rest=1024;
        }else{rest= (int) FILE_SIZE;}

        byte[] bytes = new byte[rest];
        try {
            while ((length =  input.read(bytes,0,rest)) != -1)
            {

                count=count+length;
                rest= (int) (FILE_SIZE-count);
                if(rest>1024){
                    rest=1024;
                }
                if(!inFile.saveFile(bytes)){
                    out.writeUTF(Integer.toString(sc_errorCL)); // ошибка клиента.
                    out.flush(); // заставляем поток закончить передачу данных.
                    msg=h.obtainMessage(stGrid,inFile.FilePatch+"|"+inFile.errorMsg);
                    h.sendMessage(msg);
                    return false;
                }
                //saveFile(bytes);
                msg=h.obtainMessage(stProgress,count,0);
                h.sendMessage(msg);
                // if (!Result_Cl){
                //     out.writeUTF(Integer.toString(sc_errorCL)); // ошибка клиента.
                //     out.flush(); // заставляем поток закончить передачу данных.
                //    return false;
                // }
                bytes = new byte[rest];
                if (count==FILE_SIZE){
                    out.writeUTF(Integer.toString(sc_successCL)); // успешно приняли данные.
                    out.flush(); // заставляем поток закончить передачу данных.
                    break;}
            }
        } catch (IOException e) {
            resultSync=false;
            msg=h.obtainMessage(stGrid,"IO|"+e.getMessage());
            h.sendMessage(msg);
            return resultSync;
        }
        try {
        out.writeUTF(Integer.toString(sc_disconnectCL)); // отсылаем серверу что мы отключились.
        out.flush(); // заставляем поток закончить передачу данных.
        } catch (IOException e) {
            resultSync=false;
            String s="IO|"+e.getMessage();
            msg=h.obtainMessage(stGrid,s);
            h.sendMessage(msg);
            return resultSync;
        }
        return resultSync;
    }

    public boolean requestSyncClient(BufferedReader inb, DataOutputStream out,DataInputStream input)  {
        boolean resultSync;
        resultSync=true;

        int result;
        int b;
        String lineIn = "";
        try {
            out.writeUTF(Integer.toString(sc_returnDB)+"|"+outFile.FilePatch+"|"+String.valueOf(outFile.FileSize)+"#"); // запрос на отправку файла.
            out.flush(); // заставляем поток закончить передачу данных.
        } catch (IOException e) {
            resultSync=false;
            String s="IO|"+e.getMessage();
            msg=h.obtainMessage(stGrid,s);
            h.sendMessage(msg);
            return resultSync;
        }
        //ждём подтверждение готовности сервера
        while (true) {
            try {
                result = inb.read();
                lineIn="";
                StringBuffer response = new StringBuffer();
                while (result != 35) {
                    response.append(Character.toChars(result));
                    lineIn=lineIn+(char)result;
                    result = inb.read();
                }
            } catch (IOException e) {
                resultSync=false;
                String s="IO|"+e.getMessage();
                msg=h.obtainMessage(stGrid,s);
                h.sendMessage(msg);
                return resultSync;
            }
            break;
        }

        if (!lineIn.isEmpty()&lineIn.equals(Integer.toString(sc_readySrv)))
        {// открываем поток для чтения
            BufferedInputStream br = null;
            try {
                br = new BufferedInputStream(new FileInputStream(outFile.FilePatch));
            int length,count,rest;
                count=0;
                if (outFile.FileSize>1024){
                    rest=1024;
                }else{rest=outFile.FileSize;}
                byte[] bytes = new byte[rest];
                while (-1 != (length = br.read(bytes, 0, rest)))
                {
                    count=count+length;
                    rest=outFile.FileSize-count;
                    if(rest>1024){
                        rest=1024;
                    }
                    out.write(bytes);
                    msg=h.obtainMessage(stProgress,count,0);
                    h.sendMessage(msg);
            }
                out.flush(); // заставляем поток закончить передачу данных.
            } catch (FileNotFoundException e) {
                resultSync=false;
                String s="FileNotFound|"+e.getMessage();
                msg=h.obtainMessage(stGrid,s);
                h.sendMessage(msg);
                return resultSync;
            } catch (IOException e) {
                resultSync=false;
                String s="IO|"+e.getMessage();
                msg=h.obtainMessage(stGrid,s);
                h.sendMessage(msg);
                return resultSync;
            }
            //ждём подтверждение сервера
            while (true) {
                try {
                    result = inb.read();
                    lineIn="";
                    StringBuffer response = new StringBuffer();
                    while (result != 35) {
                        response.append(Character.toChars(result));
                        lineIn=lineIn+(char)result;
                        result = inb.read();
                    }
                } catch (IOException e) {
                    resultSync=false;
                    String s="IO|"+e.getMessage();
                    msg=h.obtainMessage(stGrid,s);
                    h.sendMessage(msg);
                    return resultSync;
                }
                break;
            }
            msg=h.obtainMessage(stGrid,lineIn);
            h.sendMessage(msg);

        }

        try {
            out.writeUTF(Integer.toString(sc_disconnectCL)); // отсылаем серверу что мы отключились.
            out.flush(); // заставляем поток закончить передачу данных.
        } catch (IOException e) {
            resultSync=false;
            String s="IO|"+e.getMessage();
            msg=h.obtainMessage(stGrid,s);
            h.sendMessage(msg);
            return resultSync;
        }
        return resultSync;
    }
    //открытие сокета, отправка запроса, получение сообщения
    public void mainSocket(int aPar) {
        InetAddress ipAddress;
        Socket msocket = null;
        BufferedReader inb = null;
        DataOutputStream out = null;
        DataInputStream input = null;
        try {

            ipAddress = InetAddress.getByName(chSetting.getAddress()); // создаем объект который отображает вышеописанный IP-адрес.

            msocket = new Socket();
            msocket.connect(new InetSocketAddress(ipAddress, chSetting.getServerPort()),5000);
            msocket.setSoTimeout(60000);


            // Берем входной и выходной потоки сокета, теперь можем получать и отсылать данные клиентом.
            // Конвертируем потоки в другой тип, чтоб легче обрабатывать текстовые сообщения.
            inb = new BufferedReader(new InputStreamReader(msocket.getInputStream(),"windows-1251"));//"windows-1251"
            out = new DataOutputStream(msocket.getOutputStream());
            input = new DataInputStream(msocket.getInputStream());

            if (requestInfoServ(inb,out ,input)){
            switch (aPar){
                case sc_requestDB:{requestSyncServ(inb,out ,input );break;}
                case sc_returnDB:{requestSyncClient(inb,out ,input );break;}
            }
            }

        } catch (SocketException x) {
            String s="socket|"+x.getMessage();
            msg=h.obtainMessage(stGrid,s);
            h.sendMessage(msg);
        } catch (UnsupportedEncodingException e) {
            String s="Encoding|" + e.getMessage();
            msg=h.obtainMessage(stGrid,s);
            h.sendMessage(msg);
        } catch (UnknownHostException e) {
            String s="Host|"+e.getMessage();
            msg=h.obtainMessage(stGrid,s);
            h.sendMessage(msg);
        } catch (IOException e) {
            String s="IO|"+e.getMessage();
            msg=h.obtainMessage(stGrid,s);
            h.sendMessage(msg);
        }
        finally {
            if (input!=null){
                try {
                    input.close();
                } catch (IOException e) {
                    String s="input|" + e.getMessage();
                    msg=h.obtainMessage(stGrid,s);
                    h.sendMessage(msg);
                }
            }
            try { if (msocket!=null){
                msocket.close();
                String s=Integer.toString(stDisconnect_icon)+"|socket|Disconnect" ;
                msg=h.obtainMessage(stGrid,s);
                h.sendMessage(msg);
            }
            } catch (IOException e) {
                String s="socket|" + e.getMessage();
                msg=h.obtainMessage(stGrid,s);
                h.sendMessage(msg);
            }
            try { if (inb!=null){
                inb.close();}
            } catch (IOException e) {
                String s="inb|" + e.getMessage();
                msg=h.obtainMessage(stGrid,s);
                h.sendMessage(msg);
            }
            try { if (out!=null){
                out.close(); }
            } catch (IOException e) {
                String s="out|" + e.getMessage();
                msg=h.obtainMessage(stGrid,s);
                h.sendMessage(msg);
            }

        }


    }

    public void WorkDB(){
        boolean isXMLs=true;
        int rowcount = 0;
      //  String FILE_NAME="/data/tbl_RoutesN.xml";
      //  int FILE_SIZE=9384;
      //  if (FILE_NAME==null||FILE_SIZE==0){
           // return;
      //      isXMLs=false;
      //  }

            // получаем путь к SD
         //   File sdPath = Environment.getExternalStorageDirectory();
            // добавляем свой каталог к пути
         //   sdPath = new File(sdPath.getAbsolutePath() + "/"+SALEIT_DIR);
            // формируем объект File, который содержит путь к файлу
         //   File sdFile = new File(sdPath, FILE_NAME);

        //  File sdFile = new File(FILE_NAME);
            if (inFile==null||!inFile.FileSync.exists()){
             //выводимо сообщение что по заданому пути файл не найден
            // return;
                isXMLs=false;
            } else{
             if(inFile.FileSync.length()==0){
              //выводим сообщение что размеры файла не совпали
              //return;
               isXMLs=false;
             }
            }
        msg=h.obtainMessage(stDialog,"work with database");
        h.sendMessage(msg);
           try{

           if (isXMLs){
               sPref= getSharedPreferences("PrefINI", MODE_PRIVATE);
               msg=h.obtainMessage(stGrid,"-1|old ver. DB|"+sPref.getInt(chSetting.getDb_name(),1));
               h.sendMessage(msg);

         //парсим файл на выходе обьект для создания БД
                dbXML dxml=new dbXML(this,inFile.FileSync.getPath());
                dxml.onCreate();
               msg=h.obtainMessage(stGrid,"-1|in DB & ver. DB|"+dxml.DATA.size()+"["+dxml.hashCode()+"]");
               h.sendMessage(msg);
            // Инициализируем наш класс-обёртку

               DatabaseHelper dbh;
               if (chSetting.isSD()){
               dbh = new DatabaseHelper(this,dxml,chSetting,sPref);//передаём хешкод таблицы и структуры как версию БД
               }else{
                 dbh = new DatabaseHelper(this,dxml,chSetting.getDb_name(),sPref);//передаём хешкод таблицы и структуры как версию БД
               }
               try{
            // База нам нужна для записи и чтения
            SQLiteDatabase sqdb = dbh.getWritableDatabase();

               try{
                /*Блок синхронизации от сервера*/
                // Удаляем записи из тмп таблицы
                sqdb.delete("tmp_"+dxml.TABLE_NAME,null,null);
                // Добавляем записи в таблицу tmp
                for (int y = 0; y < dxml.DATA.size(); ++y){
                    ContentValues values = (ContentValues) dxml.DATA.get(y);
                    sqdb.insert("tmp_"+dxml.TABLE_NAME, null, values);
                }
                  //Выбираем записи которые есть у нас и они не инсертились и которых нет на сервере
                  Cursor c = sqdb.rawQuery("SELECT * FROM "+ dxml.TABLE_NAME +
                          " a where not a.hash_code in(select hash_code from tmp_"+dxml.TABLE_NAME+") and a.status<>1",null);
                  try {

                  //Удаляем лишние записи
                  String s="";
                  rowcount=0;
                  while (c.moveToNext()) {
                      s= c.getString(c.getColumnIndex("hash_code"));
                      sqdb.delete(dxml.TABLE_NAME,"hash_code="+s,null);
                      ++rowcount;
                  }
                  } catch(Exception e){
                      msg=h.obtainMessage(stGrid,"delete DB|"+e.getMessage());
                      h.sendMessage(msg);
                  }
                  finally {
                      c.close();
                  }
                   if(rowcount>0){
                   msg=h.obtainMessage(stGrid,"-1|delete DB|"+rowcount);
                   h.sendMessage(msg);}
                  //Выбираем записи которые есть на сервере но которых нет у нас
                  //и добавляем их
                   rowcount=0;
                  String sqlStr="select a.* from %1$s a where not a.hash_code IN(select hash_code from %2$s)";
                  Cursor c1 = sqdb.rawQuery(String.format(sqlStr,"tmp_"+dxml.TABLE_NAME,dxml.TABLE_NAME),null);
                  try{
                      while (c1.moveToNext()) {
                          ContentValues values = new ContentValues() ;
                          for(int i=0;i < c1.getColumnCount(); ++i){
                              values.put(c1.getColumnName(i),c1.getString(i));
                          }
                          sqdb.insert(dxml.TABLE_NAME, null, values);
                          ++rowcount;
                      }

                  } catch(Exception e){
                      msg=h.obtainMessage(stGrid,"insert DB|"+e.getMessage());
                      h.sendMessage(msg);
                  }
                  finally {
                   c1.close();
                  }
                   if(rowcount>0){
                       msg=h.obtainMessage(stGrid,"-1|insert DB|"+rowcount);
                       h.sendMessage(msg);}
                  rowcount=0;
                  String sqlStr1="select a.* from %1$s a where  a.hash_code IN(select hash_codeCL from %2$s where status<>0)";//and not a.hash_code IN(select hash_code from %2$s)
                  Cursor c2 = sqdb.rawQuery(String.format(sqlStr1,"tmp_"+dxml.TABLE_NAME,dxml.TABLE_NAME),null);
                   try {

                   while (c2.moveToNext()) {
                       ContentValues values = new ContentValues() ;
                       values.put("hash_code",c2.getInt(c2.getColumnIndex("hash_code")));
                       values.put("hash_codeCL",c2.getInt(c2.getColumnIndex("hash_code")));
                       values.put("status",0);
                       sqdb.update(dxml.TABLE_NAME,values,"hash_codeCL=?",new String[] {c2.getString(c1.getColumnIndex("hash_code"))});
                       ++rowcount;
                   }
                   }
                   catch(Exception e){
                       msg=h.obtainMessage(stGrid,"update DB|"+e.getMessage());
                       h.sendMessage(msg);
                   }
                   finally {
                       c2.close();
                   }

                   if(rowcount>0){
                       msg=h.obtainMessage(stGrid,"-1|update DB|"+rowcount);
                       h.sendMessage(msg);}
               }
               catch(SQLiteException e){
                   msg=h.obtainMessage(stGrid,"error DB|"+e.getMessage());
                   h.sendMessage(msg);
               }
               finally {
                   msg=h.obtainMessage(stGrid,"-1|current ver. DB|"+sqdb.getPath()+"["+sqdb.getVersion()+"]");
                   h.sendMessage(msg);
                   sqdb.close();
               } }finally{
                   dbh.close();
                   }
               }


               DatabaseHelper dbh;
               if (chSetting.isSD()){
               dbh =  new DatabaseHelper(this,chSetting,sPref.getInt(chSetting.getDb_name(),1));
               }
               else{
               dbh =  new DatabaseHelper(this,chSetting.getDb_name(),sPref.getInt(chSetting.getDb_name(),1));
               }

               try{
               SQLiteDatabase sqdb = dbh.getWritableDatabase();
                   try{
               Cursor c = sqdb.rawQuery("SELECT name FROM sqlite_master"  +
                       " WHERE type='table' and name<>'android_metadata'"+
                       " and not name like ('tmp_%')"+
                       " ORDER BY name",null);
               try{
                   while (c.moveToNext()) {
                       String tbl_name=c.getString(0);
                       msg=h.obtainMessage(stGrid,"-1|outDB|"+tbl_name);
                       h.sendMessage(msg);
                       rowcount=0;
                       Cursor c1 = sqdb.rawQuery("SELECT * FROM "+ tbl_name+" WHERE status<>0",null);
                       try{
                           String s="";
                           String strHeard="";
                           for (int i=0;i<c1.getColumnCount()-1;++i){
                            if (!c1.getColumnName(i).equalsIgnoreCase("hash_code")&!c1.getColumnName(i).equalsIgnoreCase("hash_codeCL")){//!c1.getColumnName(i).equalsIgnoreCase("status")||
                              strHeard=strHeard+c1.getColumnName(i)+";"; }
                           }
                           strHeard=strHeard+"\n";
                           outFile=new syncFile(this,tbl_name+".csv",strHeard.length());
                           if (!outFile.createFile()||!outFile.saveFile(strHeard.getBytes())){
                               msg=h.obtainMessage(stGrid,outFile.errorMsg);
                               h.sendMessage(msg);
                               return;
                           }

                           while (c1.moveToNext()) {
                               for (int i=0;i<c1.getColumnCount()-1;++i){
                            if (!c1.getColumnName(i).equalsIgnoreCase("hash_code")&!c1.getColumnName(i).equalsIgnoreCase("hash_codeCL")){//!c1.getColumnName(i).equalsIgnoreCase("status")||
                                if (c1.getString(i)!=null)
                                 s=s+c1.getString(i)+";";
                                else
                                 s=s+";";
                            }

                               }

                               ++rowcount;
                               s=s+ "\n";
                               if (!outFile.saveFile(s.getBytes())){
                                   msg=h.obtainMessage(stGrid,outFile.errorMsg);
                                   h.sendMessage(msg);
                                   break;
                               }
                           }
                            if(outFile.errorMsg.equals(""))
                             msg=h.obtainMessage(stGrid,stInfo_icon+"|save File&rowcount|"+outFile.FilePatch+"("+outFile.FileSize+")["+rowcount+"]");
                           else
                             msg=h.obtainMessage(stGrid,stError_icon+"|save File&rowcount|"+outFile.FilePatch+"("+outFile.FileSize+")["+rowcount+"]");
                           h.sendMessage(msg);




                       }catch(SQLiteException e){
                           msg=h.obtainMessage(stGrid,"select DB|"+e.getMessage());
                           h.sendMessage(msg);
                       }finally {
                        c1.close();
                       }
                   }
               }catch(Exception e){
                   msg=h.obtainMessage(stGrid,"select DB|"+e.getMessage());
                   h.sendMessage(msg);
               }
               finally {
                   c.close();
               }
                   }catch(SQLiteException e){
                           msg=h.obtainMessage(stGrid,"error DB|"+e.getMessage());
                           h.sendMessage(msg);
                       }finally {
                       sqdb.close();
                   }
               }catch(SQLiteException e){
                   msg=h.obtainMessage(stGrid,"error DB|"+e.getMessage());
                   h.sendMessage(msg);
               }

               finally {dbh.close();
                   }

           }catch(SQLiteException e){
               msg=h.obtainMessage(stGrid,"error DB|"+e.getMessage());
               h.sendMessage(msg);
           }


               finally {

           }


          }





    // прорисовка сообщений в гриде
    public void setGrid(String pStr) {
        String[] strTokens;
        String strTime, strDistr, strMsg;
        int intStatus;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.S");
        strTokens = pStr.split("[|]");
        intStatus =0;
        strTime = sdf.format(new Date());
        strDistr ="";
        strMsg ="";
        if (strTokens.length ==2) {
            intStatus = stError_icon;
            strTime = sdf.format(new Date());
            strDistr = "["+strTokens[0]+"]";
            strMsg = strTokens[1];
        }else if (strTokens.length ==3){
            intStatus = Integer.parseInt(strTokens[0]);
            strTime =  sdf.format(new Date());
            strDistr = "["+strTokens[1]+"]";
            strMsg = strTokens[2];
        }
        else if (strTokens.length ==4){
        intStatus = Integer.parseInt(strTokens[0]);
        strTime = strTokens[1];
        strDistr = "["+strTokens[2]+"]";
        strMsg = strTokens[3];
        }

        Bitmap stIcon = null;
        switch (intStatus) {
            case stInfo_icon:
                stIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.info_icon);
                break;
            case stError_icon:
               stIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.error_icon);
                break;
            case stConnect_icon:
                 stIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.server_connect);
                break;
            case stServWar_icon:
                stIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.server_warning);
                break;
            case stServErr_icon:
                 stIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.server_error);
                break;
            case stDisconnect_icon:
                stIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.disconnect);
                break;
            default:
                stIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.server_info);
        }


        gridArray.add(0,new item(stIcon,strTime,strDistr,strMsg));

        gridView = (GridView) findViewById(R.id.gridView);
        customGridAdapter = new CustomGridViewAdapter(this, R.layout.row_grid, gridArray);
        gridView.setAdapter(customGridAdapter);
    }

    public void mainSyncDB(){
        //запрос данных от сервера
        msg=h.obtainMessage(stDialog,"from the server");
        h.sendMessage(msg);
        mainSocket(sc_requestDB);//запрос на получение данных от сервера

        WorkDB();

        if (!(outFile==null)){
         msg=h.obtainMessage(stDialog,"from the remote client");
         h.sendMessage(msg);
         mainSocket(sc_returnDB);//запрос отправки данных на сервер
        }else{
            msg=h.obtainMessage(stGrid,Integer.toString(stInfo_icon)+"|outFile|not send Empty file");
            h.sendMessage(msg);
        }

        h.sendEmptyMessage(stClearDialog);
    }

   //реакция на клик кнопки синхронизации
    public void onTobinClick(View v) throws IOException {

        if (spinnerServ.getSelectedItem()==null){
         Toast.makeText(getBaseContext(),"Необходимо выбрать сервер.",Toast.LENGTH_LONG).show();
         return;
        }


          try {
              tBtn.setEnabled(false);
              Thread t = new Thread(new Runnable() {
                  public void run() {mainSyncDB();}});
              t.start();
          }
          catch(Exception e) {
              msg=h.obtainMessage(stGrid,e.getMessage());
              h.sendMessage(msg);

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
        Intent intent = new Intent(this, DBWORK.class);
        intent.putExtra("Db_name", chSetting.getDb_name());
        intent.putExtra("isSD",chSetting.isSD());
        startActivityForResult(intent, 0);

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

    /*void  writeFile(byte[] bytes) {
        try {
            // отрываем поток для записи
            InputStream in = openFileInput(FILE_NAME);
            OutputStream outputstream = openFileOutput(FILE_NAME, MODE_APPEND);//MODE_APPEND
            // пишем данные

            outputstream.write(bytes);

            // закрываем поток
            outputstream.close();

        } catch (FileNotFoundException e) {
            msg=h.obtainMessage(stGrid,"wFile|" + e.getMessage());
            h.sendMessage(msg);

        } catch (IOException e) {
            msg=h.obtainMessage(stGrid,"wFile|" + e.getMessage());
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
            msg=h.obtainMessage(stGrid,"wFile|" + e.getMessage());
            h.sendMessage(msg);
        } catch (IOException e) {
            msg=h.obtainMessage(stGrid,"wFile|" + e.getMessage());
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
            msg=h.obtainMessage(stGrid,"wSD|" + e.getMessage());
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
            msg=h.obtainMessage(stGrid,"wSD|" + e.getMessage());
            h.sendMessage(msg);
            Result_Cl=false;
        }
    }

    public boolean writeFilestr(String FILENAME,String str) {
        boolean res=false;
        try {
            // отрываем поток для записи
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    openFileOutput(FILENAME, MODE_PRIVATE)));
            // пишем данные
            bw.write(str);
            // закрываем поток
            bw.close();
            openFileInput(FILENAME);
            FILE_SIZE=str.getBytes().length;
            FILE_NAME=FILENAME;
            res=true;
        } catch (FileNotFoundException e) {
            msg=h.obtainMessage(stGrid,"wStr|" + e.getMessage());
            h.sendMessage(msg);
        } catch (IOException e) {
            msg=h.obtainMessage(stGrid,"wStr|" + e.getMessage());
            h.sendMessage(msg);
        }
        return res;
    }



    public boolean writeFileSDstr(String FILENAME_SD,String str) {
        boolean res=false;
        // проверяем доступность SD
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
          //  Log.d(LOG_TAG, "SD-карта не доступна: " + Environment.getExternalStorageState());
            return res;
        }
        // получаем путь к SD
        File sdPath = Environment.getExternalStorageDirectory();
        // добавляем свой каталог к пути
        sdPath = new File(sdPath.getAbsolutePath() + "/" + SALEIT_DIR);
        // создаем каталог
        sdPath.mkdirs();
        // формируем объект File, который содержит путь к файлу
        File sdFile = new File(sdPath, FILENAME_SD);
        try {
            // открываем поток для записи
            BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile));
            // пишем данные
            bw.write(str);
            // закрываем поток
            bw.close();
            res=true;
          //  Log.d(LOG_TAG, "Файл записан на SD: " + sdFile.getAbsolutePath());
        } catch (IOException e) {
            msg=h.obtainMessage(stGrid,"wStrSD|" + e.getMessage());
            h.sendMessage(msg);
        }
      return res;
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


    }*/

}
