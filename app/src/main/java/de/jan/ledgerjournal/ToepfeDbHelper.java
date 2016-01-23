package de.jan.ledgerjournal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by jan on 06.01.16. From http://www.programmierenlernenhq.de/sqlite-datenbank-in-android-app-integrieren/
 */
public class ToepfeDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "toepfe.db";
    public static final int DB_VERSION = 1;
    public static final String TABLE_TOEPFE = "toepfe";

    public static final String COLUMN_TOPFID = "topfid";
    public static final String COLUMN_TOPFNAME = "topfname";




    public ToepfeDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d("ToepfeDbHelper", "Datenbank " + getDatabaseName() + " erzeugt.");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            Log.d("ToepfeDbHelper", "Tabelle wird angelegt mit Befehl: " + sqlCreate());
            db.execSQL(sqlCreate());
        }
        catch(Exception e) {
            Log.e("ToepfeDbHelper", "Fehler beim Anlegen der Tabelle: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static String sqlCreate() {
        String cmd = "CREATE TABLE " + TABLE_TOEPFE + "(" +
                COLUMN_TOPFID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TOPFNAME + " TEXT NOT NULL UNIQUE);";
        return cmd;
    }

    // create array with database column names, used by ToepfeDataSource for Cursor
    public static String[] columns() {
        ArrayList<String> l = new ArrayList<String>();
        l.add(COLUMN_TOPFNAME);
        l.add(COLUMN_TOPFID);
        String[] a = new String[l.size()];
        a = l.toArray(a);
        return a;
    }

}
