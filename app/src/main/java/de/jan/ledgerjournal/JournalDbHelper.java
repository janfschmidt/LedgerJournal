package de.jan.ledgerjournal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by jan on 06.01.16. From http://www.programmierenlernenhq.de/sqlite-datenbank-in-android-app-integrieren/
 */
public class JournalDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "journal.db";
    public static final int DB_VERSION = 1;
    public static final String TABLE_JOURNAL = "journal";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TOPFID = "topfid";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_PAYEE = "payee";
    public static final String COLUMN_ACC0 = "acc0";
    public static final String COLUMN_ACC1 = "acc1";
    public static final String COLUMN_ACC2 = "acc2";
    public static final String COLUMN_ACC3 = "acc3";
    public static final String COLUMN_VAL0 = "val0";
    public static final String COLUMN_VAL1 = "val1";
    public static final String COLUMN_VAL2 = "val2";
    public static final String COLUMN_VAL3 = "val3";
    public static final String COLUMN_CURRENCY = "currency";

    public static final String SQL_CREATE =
            "CREATE TABLE " + TABLE_JOURNAL + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TOPFID + " INTEGER NOT NULL, " +
                    COLUMN_DATE + " TEXT NOT NULL, " +
                    COLUMN_PAYEE + " TEXT NOT NULL, " +
                    COLUMN_ACC0 + " TEXT NOT NULL, " +
                    COLUMN_VAL0 + " FLOAT, " +           // values are optional (added to zero by ledger)
                    COLUMN_ACC1 + " TEXT NOT NULL, " +
                    COLUMN_VAL1 + " FLOAT, " +
                    COLUMN_ACC2 + " TEXT, " +            // two accounts needed, more optional
                    COLUMN_VAL2 + " FLOAT, " +
                    COLUMN_ACC3 + " TEXT, " +
                    COLUMN_VAL3 + " FLOAT, " +
                    COLUMN_CURRENCY + " TEXT NOT NULL);";


    public JournalDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d("JournalDbHelper", "Datenbank " + getDatabaseName() + " erzeugt.");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            Log.d("JournalDbHelper", "Tabelle wird angelegt mit Befehl: " + SQL_CREATE);
            db.execSQL(SQL_CREATE);
        }
        catch(Exception e) {
            Log.e("JournalDbHelper", "Fehler beim Anlegen der Tabelle: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static String getTopfFilter(int topfid) {
        return COLUMN_TOPFID + "=" + topfid;
    }
}
