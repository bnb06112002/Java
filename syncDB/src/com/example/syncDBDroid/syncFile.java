package com.example.syncDBDroid;


import android.content.Context;
import android.os.Environment;

import java.io.*;


/**
 * Created with IntelliJ IDEA.
 * User: n.bilan
 * Date: 24.12.13
 * Time: 15:21
 * To change this template use File | Settings | File Templates.
 */
public class syncFile {
    private Context fContext;
    private String fName ;
    private long fSize;
    final String SALEIT_DIR="SALEIT";

    public String FilePatch;
    public int FileSize;
    public File FileSync;
    public String errorMsg;

    syncFile(Context context,String f_name,long f_size) {
        fContext = context;
        fName=f_name;
        fSize=f_size;
        FileSync=null;
        FilePatch="";
        FileSize=0;
    }

    private boolean createFileNotSD() {
        try {

            OutputStream outputstream = fContext.openFileOutput(fName, fContext.MODE_PRIVATE);//MODE_APPEND

            // пишем данные
            byte[] bytes=new byte[0];
            outputstream.write(bytes);
            // закрываем поток
            outputstream.close();

            FileSync = new File(fContext.getFilesDir(),fName);
            FilePatch=fName;
            return true;
        } catch (FileNotFoundException e) {
            errorMsg="wFile|" + e.getMessage();
            return false;
          //  msg=h.obtainMessage(stGrid,"wFile|" + e.getMessage());
          //  h.sendMessage(msg);
        } catch (IOException e) {
            errorMsg="wFile|" + e.getMessage();
            return false;
          //  msg=h.obtainMessage(stGrid,"wFile|" + e.getMessage());
          //  h.sendMessage(msg);
        }
    }
    private boolean createFileSD(){
        // получаем путь к SD
        File sdPath = Environment.getExternalStorageDirectory();
        // добавляем свой каталог к пути
        sdPath = new File(sdPath.getAbsolutePath() + "/"+SALEIT_DIR);
        // создаем каталог
        sdPath.mkdirs();
        // формируем объект File, который содержит путь к файлу
        File sdFile = new File(sdPath, fName);
        sdFile.delete();
        try {
            sdFile.createNewFile();
            FileSync=sdFile;
            FilePatch=FileSync.getAbsolutePath();
            return true;
        } catch (IOException e) {
            errorMsg="wSd|" + e.getMessage();
           // msg=h.obtainMessage(stGrid,"wSD|" + e.getMessage());
           // h.sendMessage(msg);  //To change body of catch statement use File | Settings | File Templates.
           // Result_Cl=false;
            return false;
        }
    }
    public boolean createFile(){
        // проверяем доступность SD
        errorMsg="";
        String SDState = Environment.getExternalStorageState();
        if (SDState.equals(Environment.MEDIA_REMOVED) ||
                SDState.equals(Environment.MEDIA_UNMOUNTED) || SDState.equals(Environment.MEDIA_NOFS))
        {
          return  createFileNotSD();
        }
        else{return createFileSD();}
    }

   private boolean writeFile(byte[] bytes) {
        try {
            // отрываем поток для записи
            //InputStream in = fContext.openFileInput(FilePatch);
            OutputStream outputstream = fContext.openFileOutput(FilePatch, fContext.MODE_APPEND);//MODE_APPEND
            // пишем данные
            outputstream.write(bytes);
            // закрываем поток
            outputstream.close();
            FileSize= (int) FileSync.length();
            return true;
        } catch (FileNotFoundException e) {
            errorMsg="wFile|" + e.getMessage();
            return false;
           // msg=h.obtainMessage(stGrid,"wFile|" + e.getMessage());
           // h.sendMessage(msg);

        } catch (IOException e) {
            errorMsg="wFile|" + e.getMessage();
            return false;
           // msg=h.obtainMessage(stGrid,"wFile|" + e.getMessage());
           // h.sendMessage(msg);

        }
    }

    private boolean writeFileSD(byte[] bytes) {
        try {
            // открываем поток для записи
            // FileWriter f0 = new FileWriter(sdFile);
            FileOutputStream fos = new FileOutputStream(FileSync,true);
            // пишем данные
            fos.write(bytes);
            // закрываем поток
            fos.close();
            FileSize= (int) FileSync.length();
            return true;
        } catch (IOException e) {
            errorMsg="wSd|" + e.getMessage();
            //msg=h.obtainMessage(stGrid,"wSD|" + e.getMessage());
           // h.sendMessage(msg);
           // Result_Cl=false;
           return false;
        }
    }

    public boolean saveFile(byte[] bytes) {
        // проверяем доступность SD
        errorMsg="";
        String SDState = Environment.getExternalStorageState();
        if (SDState.equals(Environment.MEDIA_REMOVED) ||
                SDState.equals(Environment.MEDIA_UNMOUNTED) || SDState.equals(Environment.MEDIA_NOFS))
        {
         return writeFile(bytes);
        }
        else{return writeFileSD(bytes);}
    }

}
