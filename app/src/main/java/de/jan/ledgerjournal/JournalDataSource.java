package de.jan.ledgerjournal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaActionSound;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by jan on 06.01.16. From http://www.programmierenlernenhq.de/sqlite-datenbank-in-android-app-integrieren/
 */
public class JournalDataSource {
    private SQLiteDatabase db;
    private JournalDbHelper dbHelper;


    public JournalDataSource(Context context) {
        dbHelper = new JournalDbHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
        Log.d("JournalDataSource", "Datenbank-Referenz erhalten. Pfad: " + db.getPath());
    }

    public void close() {
        dbHelper.close();
        Log.d("JournalDataSource", "Datenbank geschlossen.");
    }


    private String getString(Cursor cursor, String columnname) {
        int index = cursor.getColumnIndex(columnname);
        return cursor.getString(index);
    }
    private double getDouble(Cursor cursor, String columnname) {
        int index = cursor.getColumnIndex(columnname);
        return cursor.getDouble(index);
    }


    // get Transaction from database cursor
    private Transaction cursorToTransaction(Cursor cursor) {
        String date = getString(cursor, JournalDbHelper.COLUMN_DATE);
        String payee = getString(cursor, JournalDbHelper.COLUMN_PAYEE);
        String currency = getString(cursor, JournalDbHelper.COLUMN_CURRENCY);

        Transaction t = new Transaction(date,payee, currency);
        for (int i=0; i<JournalDbHelper.MAX_POSTINGS; i++) {
            t.addPosting(getString(cursor, JournalDbHelper.columnAcc(i)), getDouble(cursor, JournalDbHelper.columnVal(i)));
            Log.d("cursorToTransaction", "Posting " + i + ": " + t.posting(i).print());
        }

        return t;
    }


    // get list of all Transactions from given Topf - e.g. to populate ListView
    public ArrayList<Transaction> getAllTransactions(int topfid) {
        ArrayList<Transaction> list = new ArrayList<>();
        Cursor cursor = db.query(JournalDbHelper.TABLE_JOURNAL, JournalDbHelper.columns(), JournalDbHelper.getTopfFilter(topfid), null, null, null, null);
        Log.d("JournalDataSource", cursor.getCount() + " db-Einträge fuer Topfid " + topfid + " gelesen.");

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            Transaction t = cursorToTransaction(cursor);
            list.add(t);
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    // add a transaction to a topf in the database
    public void addTransaction(Transaction t, int topfid) {
        ContentValues cv = new ContentValues();
        cv.put(JournalDbHelper.COLUMN_TOPFID, topfid);
        cv.put(JournalDbHelper.COLUMN_DATE, t.date);
        cv.put(JournalDbHelper.COLUMN_PAYEE, t.payee);
        cv.put(JournalDbHelper.COLUMN_CURRENCY, t.currency);
        for (int i=0; i<t.numPostings(); i++) {
            cv.put(JournalDbHelper.columnAcc(i), t.posting(i).account);
            cv.put(JournalDbHelper.columnVal(i), t.posting(i).amount);
        }

        long insertid = db.insert(JournalDbHelper.TABLE_JOURNAL, null, cv);
        Log.d("JournalDataSource", "db entry added with insert id " + insertid);
    }
}
