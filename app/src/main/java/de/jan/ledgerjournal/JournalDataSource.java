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
    private int getInt(Cursor cursor, String columnname) {
        int index = cursor.getColumnIndex(columnname);
        return cursor.getInt(index);
    }

    // get Transaction from database cursor
    private Transaction cursorToTransaction(Cursor cursor) {
        String date = getString(cursor, JournalDbHelper.COLUMN_DATE);
        String payee = getString(cursor, JournalDbHelper.COLUMN_PAYEE);
        String currency = getString(cursor, JournalDbHelper.COLUMN_CURRENCY);
        int id = getInt(cursor, JournalDbHelper.COLUMN_ID);

        Transaction t = new Transaction(date,payee, currency);
        for (int i=0; i<JournalDbHelper.MAX_POSTINGS; i++) {
            String account = getString(cursor, JournalDbHelper.columnAcc(i));
            if (account != null) {
                t.addPosting(account, getDouble(cursor, JournalDbHelper.columnVal(i)));
                Log.d("cursorToTransaction", "Posting " + i + ": " + t.posting(i).print());
            }
        }
        t.setDatabaseID(id);

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

    // delete a transaction
    public void  deleteTransaction(Transaction t) {
        int id = t.getDatabaseID();
        int num = db.delete(JournalDbHelper.TABLE_JOURNAL, JournalDbHelper.COLUMN_ID + "=" + id, null);

        if (num > 1)
            throw new RuntimeException("deleteTransaction() deleted "+num+" Transactions, but 1 was expected.");
        else if (num == 0)
            throw new RuntimeException("deleteTransaction(): no Transaction with id "+id+" found.");
    }

    // delete a complete Journal/Topf by id
    public void deleteTopf(int topfid) {
        int num = db.delete(JournalDbHelper.TABLE_JOURNAL, JournalDbHelper.COLUMN_TOPFID + "=" + topfid, null);

        if (num == 0)
            throw new RuntimeException("deleteTopf(): no Transaction with topfid "+topfid+" found.");

        Log.d("JournalDataSource", "deleted Journal with Topfid " + topfid + ", " + num + " Transactions deleted.");
    }
}
