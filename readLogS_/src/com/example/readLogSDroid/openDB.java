package com.example.readLogSDroid;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;


public class openDB extends Activity {
    LayoutInflater ltInflater;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.dbitems);
        LinearLayout DBL = (LinearLayout)findViewById(R.id.linLayout);
        ltInflater = getLayoutInflater();
        for(int i=1;i<2;i++){
            addArticle(DBL,"Title "+i , "s"+i, i);
        }

    }

    /**
     * Функция добавления новости
     *
     * @param DBL - объект к которому добавляется новость (LinearLayout, id=NewsLayout)
     * @param Text - Текст
     * @param id - индентификатор
     */
    public void addArticle(LinearLayout DBL, String Title, String Text,int id){
        TextView Aurthor = new TextView(this);
        TextView Artical = new TextView(this);

        int height= FrameLayout.LayoutParams.FILL_PARENT;
        int width= FrameLayout.LayoutParams.WRAP_CONTENT;

        // Заполнение поля Автор
        Aurthor.setText(Title);
        Aurthor.setTextColor(Color.BLACK);
        Aurthor.setBackgroundColor(0x99CCFF);
        Aurthor.setTextSize(18);

        //Заполнение поля Текст
        Artical.setId(id);
        Artical.setClickable(true);
        Artical.setText(Text);
        Artical.setTextColor(Color.BLACK);
        Artical.setBackgroundColor(Color.WHITE);
        Artical.setTextSize(15);

        View item = ltInflater.inflate(R.layout.itemdb, DBL, false);
       // item.addFocusables();
        item.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        item.setBackgroundColor(Color.BLACK);

        //Добавление записи в форму
       // DBL.addView(item);
        DBL.addView(Aurthor, new LinearLayout.LayoutParams(height, width));
        DBL.addView(Artical, new LinearLayout.LayoutParams(height, width));
    }
}
