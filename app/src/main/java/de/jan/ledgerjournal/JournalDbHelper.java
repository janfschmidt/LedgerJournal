/*
 * Copyright (c) 2017 Jan Felix Schmidt <janschmidt@mailbox.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.jan.ledgerjournal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by jan on 06.01.16. based on http://www.programmierenlernenhq.de/sqlite-datenbank-in-android-app-integrieren/
 */



public class JournalDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "journal.db";
    public static final int DB_VERSION = 1;
    public static final String TABLE_JOURNAL = "journal";
    public static final String TABLE_TOEPFE = "toepfe";
    //public static final String TABLE_TEMPLATES = "templates";
    public static final int TEMPLATE_TOPFID = 0;

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TOPFID = "topfid";
    public static final String COLUMN_TOPFNAME = "topfname";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_PAYEE = "payee";
    public static final String COLUMN_CURRENCY = "currency";
    public static final String COLUMN_CURRENCYPOSITION = "currpos";

    public static final int MAX_POSTINGS = 4;
    public static final String COLUMN_BASENAME_ACC = "acc";
    public static final String COLUMN_BASENAME_VAL = "val";

    protected String logTag = this.getClass().getSimpleName();
    protected boolean newDB = false;


    public JournalDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d(logTag, "Datenbank " + getDatabaseName() + " erzeugt.");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            Log.d(logTag, "Tabelle wird angelegt mit Befehl: " + sqlCreate_JOURNAL());
            db.execSQL(sqlCreate_JOURNAL());
            Log.d(logTag, "Tabelle wird angelegt mit Befehl: " + sqlCreate_TOEPFE());
            db.execSQL(sqlCreate_TOEPFE());
//            Log.d(logTag, "Tabelle wird angelegt mit Befehl: " + sqlCreate_TEMPLATES());
//            db.execSQL(sqlCreate_TEMPLATES());
        }
        catch(Exception e) {
            Log.e(logTag, "Fehler beim Anlegen der Tabellen: " + e.getMessage());
        }
        newDB = true;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(logTag, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_JOURNAL);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TOEPFE);
        this.onCreate(db);
    }

    public boolean isNew() {return newDB;}
    public static String columnAcc(int index) {checkPostingIndex(index); return COLUMN_BASENAME_ACC + index;}
    public static String columnVal(int index) {checkPostingIndex(index); return COLUMN_BASENAME_VAL + index;}



    protected String sqlCreate_JOURNAL() {
        String cmd = "CREATE TABLE " + TABLE_JOURNAL + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TOPFID + " INTEGER NOT NULL, " +
                COLUMN_DATE + " TEXT NOT NULL, " +
                COLUMN_PAYEE + " TEXT NOT NULL, ";
        for (int i=0; i<MAX_POSTINGS; i++) {
            cmd +=  columnAcc(i) + " TEXT";
            if (i < 2) cmd +=  " NOT NULL";              // accounts 0&1 needed, further accounts optional
            cmd += ", " + columnVal(i) + " FLOAT, ";     // values are optional (added to zero by ledger)
        }
        cmd += COLUMN_CURRENCY + " TEXT NOT NULL, ";
        cmd += COLUMN_CURRENCYPOSITION + " INTEGER NOT NULL);";
        return cmd;
    }

    public String sqlCreate_TOEPFE() {
        return "CREATE TABLE " + TABLE_TOEPFE + "(" +
                COLUMN_TOPFID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TOPFNAME + " TEXT NOT NULL UNIQUE);";
    }


    // create array with database column names, used by JournalDataSource for Cursor
    public static String[] columns_JOURNAL() {
        ArrayList<String> l = new ArrayList<>();
        l.add(COLUMN_ID);
        l.add(COLUMN_DATE);
        l.add(COLUMN_PAYEE);
        l.add(COLUMN_CURRENCY);
        l.add(COLUMN_CURRENCYPOSITION);
        for (int i=0; i<MAX_POSTINGS; i++) {
            l.add(columnAcc(i));
            l.add(columnVal(i));
        }
        String[] a = new String[l.size()];
        a = l.toArray(a);
        return a;
    }

    public static String[] columns_TOEPFE() {
        ArrayList<String> l = new ArrayList<>();
        l.add(COLUMN_TOPFNAME);
        l.add(COLUMN_TOPFID);
        String[] a = new String[l.size()];
        a = l.toArray(a);
        return a;
    }

   public static String[] columns_TEMPLATES() {
        ArrayList<String> l = new ArrayList<>();
        l.add(COLUMN_ID);
        l.add(COLUMN_PAYEE);
        for (int i=0; i<MAX_POSTINGS; i++) {
            l.add(columnAcc(i));
        }
        String[] a = new String[l.size()];
        a = l.toArray(a);
        return a;
    }

    public static String[] columns_accounts() {
        ArrayList<String> l = new ArrayList<>();
        for (int i=0; i<MAX_POSTINGS; i++) {
            l.add(columnAcc(i));
        }
        String[] a = new String[l.size()];
        a = l.toArray(a);
        return a;
    }


    public static String getTopfFilter(int topfid) {
        return COLUMN_TOPFID + "=" + topfid;
    }
    public static String getTemplateFilter(){ return COLUMN_TOPFID + "=" + TEMPLATE_TOPFID; }
    public static String getTemplateFilter(String condition){ return COLUMN_TOPFID + "=" + TEMPLATE_TOPFID + " AND " + condition; }

    protected static void checkPostingIndex(int index) {
        if (index >= MAX_POSTINGS)
            throw new RuntimeException("JournalDbHelper: Posting index > MAX_POSTINGS per Transaction!");
    }
}
