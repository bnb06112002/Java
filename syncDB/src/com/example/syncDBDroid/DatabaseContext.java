package com.example.syncDBDroid;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: n.bilan
 * Date: 25.12.13
 * Time: 15:34
 * To change this template use File | Settings | File Templates.
 */
class DatabaseContext extends ContextWrapper {
    private Context fContext;
    private boolean fisSD;

   // private static final String DEBUG_CONTEXT = "DatabaseContext";

    public DatabaseContext(Context base,boolean isSD) {
        super(base);
        fContext=base;
        fisSD=isSD;
    }

    @Override
    public File getDatabasePath(String name)
    {
        File sdcard = Environment.getExternalStorageDirectory();
        File result;
        result = fContext.getDatabasePath(name);


        if(fisSD&!sdcard.equals(Environment.MEDIA_REMOVED)&
            !sdcard.equals(Environment.MEDIA_UNMOUNTED)&
            !sdcard.equals(Environment.MEDIA_NOFS))
        {
            String dbfile = sdcard.getAbsolutePath() + File.separator+ "databases" + File.separator + name;
            if (!dbfile.endsWith(".db"))
            {
                dbfile += ".db" ;
            }

            result = new File(dbfile);

            if (!result.getParentFile().exists())
            {
                result.getParentFile().mkdirs();
            }

        }
        return result;




    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory)
    {
        SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null);


        return result;
    }
}
