package com.example.readLogSDroid;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: n.bilan
 * Date: 11.12.13
 * Time: 11:00
 * To change this template use File | Settings | File Templates.
 */
public class dbXML {
    private Context fContext;
    private String DATABASE_NAME = "saleit.db";
    private String FNAME ;
    public String TABLE_NAME;
    //public String md5 ;
    public ArrayList<Object> FIELD;
    public ArrayList<Object> DATA;
    public ContentValues values;

    dbXML(Context context,String f_name) {
        fContext = context;
        FNAME=f_name;
        int dotIdx = f_name.lastIndexOf('.');
        int bslashIdx = f_name.lastIndexOf('/');
        if (dotIdx != -1 && bslashIdx != -1) {
            TABLE_NAME=f_name.substring(bslashIdx + 1, dotIdx);
        }


    }

    public void onCreate() {
        try {
            SAXBuilder parser = new SAXBuilder();
            FileReader fr = new FileReader(FNAME);
            Document rDoc = parser.build(fr);
            FIELD = new ArrayList<Object>();
            DATA = new ArrayList<Object>();


            StringBuilder rowName = new StringBuilder();
            StringBuilder rowData = new StringBuilder();
            List<Element> temp = rDoc.getRootElement().getChildren();
            for (int i = 0; i < temp.size(); ++i) {
                List<Element> tempchild =temp.get(i).getChildren();
                for (int y = 0; y < tempchild.size(); ++y) {
                    if (tempchild.get(y).getName().equalsIgnoreCase("row")){
                        List<Attribute> tempAttribute= tempchild.get(y).getAttributes();
                        ContentValues values = new ContentValues();
                        for (int o = 0; o < tempAttribute.size(); ++o) {
                            values.put(tempAttribute.get(o).getName(),tempAttribute.get(o).getValue());
                        }
                        values.put("hash_code",values.hashCode());
                        DATA.add(values);
                    }else {
                    List<Element> tempchild1 =tempchild.get(y).getChildren();
                    for (int u = 0; u < tempchild1.size(); ++u) {
                        System.out.println(tempchild1.get(u).getName());
                        if (tempchild1.get(u).getName().equalsIgnoreCase("AttributeType")){
                            List<Attribute> tempAttribute= tempchild1.get(u).getAttributes();
                            List<Element> childType =tempchild1.get(u).getChildren();
                            recordField recField =new recordField();
                            //имя поля
                            for (int o = 0; o < tempAttribute.size(); ++o) {
                                if(tempAttribute.get(o).getName().equalsIgnoreCase("name")){
                                    recField.nameField=tempAttribute.get(o).getValue();
                                    break;
                                }

                            }
                            //тип поля
                            for (int o = 0; o < childType.size(); ++o) {
                                List<Attribute> tempAttribute1= childType.get(o).getAttributes();
                                for (int p = 0; p < tempAttribute1.size(); ++p) {
                                    if(tempAttribute1.get(p).getName().equalsIgnoreCase("type")){
                                        recField.typeField=tempAttribute1.get(p).getValue();
                                    }
                                    if(tempAttribute1.get(p).getName().equalsIgnoreCase("maxLength")){
                                        recField.length=Integer.decode(tempAttribute1.get(p).getValue());
                                    }
                                }

                            }
                            FIELD.add(recField);
                        }
                    }
                    }

                }
            }

        }


        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        dbXML dbXML = (dbXML) o;

        if (!FIELD.equals(dbXML.FIELD)) return false;
        if (!TABLE_NAME.equals(dbXML.TABLE_NAME)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = TABLE_NAME.hashCode();
        result = 31 * result + FIELD.hashCode();
        return result;
    }
}
