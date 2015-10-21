package com.example.syncDBDroid;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class CustomGridViewAdapter  extends ArrayAdapter<item> {
    Context context;
    int layoutResourceId;
    ArrayList<item> data = new ArrayList<item>();

    public CustomGridViewAdapter(Context context, int layoutResourceId,
                                 ArrayList<item> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RecordHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new RecordHolder();
            holder.txtTime = (TextView) row.findViewById(R.id.textTime);
            holder.txtDistr= (TextView) row.findViewById(R.id.textDistr);
            holder.txtMsg= (TextView) row.findViewById(R.id.textMsg);
            holder.imageItem = (ImageView) row.findViewById(R.id.item_image);
            row.setTag(holder);
        } else {
            holder = (RecordHolder) row.getTag();
        }

        item item = data.get(position);
        holder.txtTime.setText(item.getTitleTime());
        holder.txtDistr.setText(item.getTitleDistr());
        holder.txtMsg.setText(item.getTitleMsg());
        holder.imageItem.setImageBitmap(item.getImage());
        return row;

    }

    static class RecordHolder {
        TextView txtTime,txtDistr,txtMsg;
        ImageView imageItem;

    }
}
