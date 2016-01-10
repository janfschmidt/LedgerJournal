package de.jan.ledgerjournal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by jan on 06.01.16. From http://www.programmierenlernenhq.de/sqlite-datenbank-in-android-app-integrieren/
 */
public class JournalDataSource {
    private SQLiteDatabase db;
    private JournalDbHelper dbHelper;

    private static final String[] columns = {
            JournalDbHelper.COLUMN_DATE,
            JournalDbHelper.COLUMN_PAYEE,
            JournalDbHelper.COLUMN_ACC0,
            JournalDbHelper.COLUMN_ACC1,
            JournalDbHelper.COLUMN_ACC2,
            JournalDbHelper.COLUMN_ACC3,
            JournalDbHelper.COLUMN_VAL0,
            JournalDbHelper.COLUMN_VAL1,
            JournalDbHelper.COLUMN_VAL2,
            JournalDbHelper.COLUMN_VAL3,
            JournalDbHelper.COLUMN_CURRENCY
    };

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

        Posting[] p = new Posting[4];
        for (int i=0; i<p.length; i++)
            p[i] = new Posting();
        p[0].account = getString(cursor, JournalDbHelper.COLUMN_ACC0);
        p[0].amount = getDouble(cursor, JournalDbHelper.COLUMN_VAL0);
        p[1].account = getString(cursor, JournalDbHelper.COLUMN_ACC1);
        p[1].amount = getDouble(cursor, JournalDbHelper.COLUMN_VAL1);
        p[2].account = getString(cursor, JournalDbHelper.COLUMN_ACC2);
        p[2].amount = getDouble(cursor, JournalDbHelper.COLUMN_VAL2);
        p[3].account = getString(cursor, JournalDbHelper.COLUMN_ACC3);
        p[3].amount = getDouble(cursor, JournalDbHelper.COLUMN_VAL3);
        for (Posting pp : p) pp.currency = currency;

        Log.d("cursorToTransaction", "Postings: " + p[0].account + ", " + p[1].account);
        return new Transaction(date, payee, p, currency);
    }


    // get list of all Transactions from given Topf - e.g. to populate ListView
    public ArrayList<Transaction> getAllTransactions(int topfid) {
        ArrayList<Transaction> list = new ArrayList<>();
        Cursor cursor = db.query(JournalDbHelper.TABLE_JOURNAL, columns, JournalDbHelper.getTopfFilter(topfid), null, null, null, null);
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
        cv.put(JournalDbHelper.COLUMN_ACC0, t.postings[0].account);
        cv.put(JournalDbHelper.COLUMN_ACC1, t.postings[1].account);
        cv.put(JournalDbHelper.COLUMN_ACC2, t.postings[2].account);
        cv.put(JournalDbHelper.COLUMN_ACC3, t.postings[3].account);
        cv.put(JournalDbHelper.COLUMN_VAL0, t.postings[0].amount);
        cv.put(JournalDbHelper.COLUMN_VAL1, t.postings[1].amount);
        cv.put(JournalDbHelper.COLUMN_VAL2, t.postings[2].amount);
        cv.put(JournalDbHelper.COLUMN_VAL3, t.postings[3].amount);
        cv.put(JournalDbHelper.COLUMN_CURRENCY, t.currency);

        long insertid = db.insert(JournalDbHelper.TABLE_JOURNAL, null, cv);
        Log.d("JournalDataSource", "db entry added with insert id=" +  insertid);
    }
}
