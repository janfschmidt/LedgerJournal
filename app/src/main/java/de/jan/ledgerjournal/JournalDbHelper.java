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
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_PAYEE = "payee";
    public static final String COLUMN_ACCOUNT1 = "account1";
    public static final String COLUMN_AMOUNT1 = "amount1";
    public static final String COLUMN_ACCOUNT2 = "account2";
    public static final String COLUMN_AMOUNT2 = "amount2";
    public static final String COLUMN_CURRENCY = "currency";

    public static final String SQL_CREATE =
            "CREATE TABLE " + TABLE_JOURNAL + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DATE + " TEXT NOT NULL, " +
                    COLUMN_PAYEE + " TEXT NOT NULL, " +
                    COLUMN_ACCOUNT1 + " TEXT NOT NULL, " +
                    COLUMN_AMOUNT1 + " FLOAT NOT NULL, " +
                    COLUMN_ACCOUNT2 + " TEXT NOT NULL, " +
                    COLUMN_AMOUNT2 + " FLOAT NOT NULL, " +
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
}
