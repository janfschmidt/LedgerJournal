package de.jan.ledgerjournal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by jan on 23.01.16. From http://www.programmierenlernenhq.de/sqlite-datenbank-in-android-app-integrieren/
 */
public class ToepfeDataSource {
    private SQLiteDatabase db;
    private ToepfeDbHelper dbHelper;


    public ToepfeDataSource(Context context) {
        dbHelper = new ToepfeDbHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
        Log.d("ToepfeDataSource", "Datenbank-Referenz erhalten. Pfad: " + db.getPath());
    }

    public void close() {
        dbHelper.close();
        Log.d("ToepfeDataSource", "Datenbank geschlossen.");
    }


    private String getString(Cursor cursor, String columnname) {
        int index = cursor.getColumnIndex(columnname);
        return cursor.getString(index);
    }

    private int getInt(Cursor cursor, String columnname) {
        int index = cursor.getColumnIndex(columnname);
        return cursor.getInt(index);
    }

    public int getTopfId(String topfname) {
        Cursor cursor = db.query(ToepfeDbHelper.TABLE_TOEPFE, ToepfeDbHelper.columns(), ToepfeDbHelper.COLUMN_TOPFNAME + "='" + topfname + "'", null, null, null, null);
        Log.d("ToepfeDataSource", cursor.getCount() + " db-Eintrag mit topfname " + topfname + " gelesen.");
        cursor.moveToFirst(); // topfname is unique, so there can be only one entry
        return getInt(cursor, ToepfeDbHelper.COLUMN_TOPFID);
    }

    public String getTopfName(int topfid) {
        Cursor cursor = db.query(ToepfeDbHelper.TABLE_TOEPFE, ToepfeDbHelper.columns(), ToepfeDbHelper.COLUMN_TOPFID + "=" + topfid, null, null, null, null);
        Log.d("ToepfeDataSource", cursor.getCount() + " db-Eintrag mit topfid " + topfid + " gelesen.");
        cursor.moveToFirst(); // topfid is unique, so there can be only one entry
        return getString(cursor, ToepfeDbHelper.COLUMN_TOPFNAME);
    }

    // get list of all Toepfe - e.g. to populate ListView
    public ArrayList<String> getAllToepfe() {
        ArrayList<String> list = new ArrayList<>();
        Cursor cursor = db.query(ToepfeDbHelper.TABLE_TOEPFE, ToepfeDbHelper.columns(), null, null, null, null, null);
        Log.d("ToepfeDataSource", cursor.getCount() + " db-Einträge gelesen.");

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            String t = getString(cursor, ToepfeDbHelper.COLUMN_TOPFNAME);
            list.add(t);
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }



    // add a topf in the database
    public void addTopf(String topfname) {
        ContentValues cv = new ContentValues();
        cv.put(ToepfeDbHelper.COLUMN_TOPFNAME, topfname);

        long insertid = db.insert(ToepfeDbHelper.TABLE_TOEPFE, null, cv);
        Log.d("ToepfeDataSource", "db entry added with insert id " + insertid);
    }

    // delete a topf from the database
    public void  deleteTopf(int topfid) {
        int num = db.delete(ToepfeDbHelper.TABLE_TOEPFE, ToepfeDbHelper.COLUMN_TOPFID + "=" + topfid, null);

        if (num > 1)
            throw new RuntimeException("deleteTopf() deleted "+num+" Journals, but 1 was expected.");
        else if (num == 0)
            throw new RuntimeException("deleteTopf(): no Journal with id "+topfid+" found.");
    }
}
