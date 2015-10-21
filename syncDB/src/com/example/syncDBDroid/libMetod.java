package com.example.syncDBDroid;

import android.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;


public class libMetod {

    //загрузка в Список выбора настроечных данных коннекта к серверу
    public void getSpin(Context context,SharedPreferences sPref,Spinner spinner){
        Integer i;
        ArrayList data = new ArrayList();

      //  sPref = getSharedPreferences("PrefINI", MODE_PRIVATE);
        i=0;
        String savedText = sPref.getString("SERVER_HOST"+String.valueOf(i),"");
        while(savedText!=""){
            data.add(savedText+":"+sPref.getString("SERVER_PORT"+String.valueOf(i), ""));
            i++;
            savedText = sPref.getString("SERVER_HOST"+String.valueOf(i),"");
        }
        ArrayAdapter<String> adapterSpin = new ArrayAdapter<String>(context, R.layout.simple_spinner_item, data);
        adapterSpin.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterSpin);

    }
}
