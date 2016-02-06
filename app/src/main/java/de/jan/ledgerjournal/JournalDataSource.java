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
class MyDataSource {
    MyDataSource() {}

    protected String getString(Cursor cursor, String columnname) {
        int index = cursor.getColumnIndex(columnname);
        return cursor.getString(index);
    }
    protected double getDouble(Cursor cursor, String columnname) {
        int index = cursor.getColumnIndex(columnname);
        return cursor.getDouble(index);
    }
    protected int getInt(Cursor cursor, String columnname) {
        int index = cursor.getColumnIndex(columnname);
        return cursor.getInt(index);
    }
}



public class JournalDataSource extends MyDataSource {
    private SQLiteDatabase db;
    private JournalDbHelper dbHelper;
    protected String logTag = this.getClass().getSimpleName();


    public JournalDataSource(Context context) {
        dbHelper = new JournalDbHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
        Log.d(logTag, "Datenbank-Referenz erhalten. Pfad: " + db.getPath());
        if(dbHelper.isNew()) {
            addDefaultTemplates();
            Log.d(logTag, "Added default Transaction Templates to database.");
        }
    }

    public void close() {
        dbHelper.close();
        Log.d(logTag, "Datenbank geschlossen.");
    }



    public Cursor getMatching(String table, String columnname, String constraint) {
        return db.query(table, null, columnname + " LIKE ?", new String[] {constraint+"%"}, null, null, null);
    }



    // ====================== Transactions =====================
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
                Log.d(logTag, "Posting " + i + ": " + t.posting(i).print());
            }
        }
        t.setDatabaseID(id);

        return t;
    }

    // get list of all Transactions from given Topf - e.g. to populate ListView
    public ArrayList<Transaction> getAllTransactions(int topfid) {
        ArrayList<Transaction> list = new ArrayList<>();
        Cursor cursor = db.query(JournalDbHelper.TABLE_JOURNAL, JournalDbHelper.columns_JOURNAL(), JournalDbHelper.getTopfFilter(topfid), null, null, null, null);
        Log.d(logTag, cursor.getCount() + " db-Einträge fuer Topfid " + topfid + " gelesen.");

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
        Log.d(logTag, "db entry added with insert id " + insertid);
    }

    // edit a transaction (update)
    public void editTransaction(Transaction t){
        ContentValues cv = new ContentValues();
        cv.put(JournalDbHelper.COLUMN_DATE, t.date);
        cv.put(JournalDbHelper.COLUMN_PAYEE, t.payee);
        cv.put(JournalDbHelper.COLUMN_CURRENCY, t.currency);
        for (int i=0; i<t.numPostings(); i++) {
            cv.put(JournalDbHelper.columnAcc(i), t.posting(i).account);
            cv.put(JournalDbHelper.columnVal(i), t.posting(i).amount);
        }
        db.update(JournalDbHelper.TABLE_JOURNAL, cv, JournalDbHelper.COLUMN_ID + "=" + t.getDatabaseID(), null);
        Log.d(logTag, "updated db entry with id " + t.getDatabaseID());
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
        db.delete(JournalDbHelper.TABLE_TOEPFE, JournalDbHelper.COLUMN_TOPFID + "=" + topfid, null);

        if (num == 0)
            throw new RuntimeException("deleteTopf(): no Transaction with topfid "+topfid+" found.");

        Log.d(logTag, "deleted Journal with Topfid " + topfid + ", " + num + " Transactions deleted.");
    }




    // ====================== Toepfe =====================
    public int getTopfId(String topfname) {
        Cursor cursor = db.query(JournalDbHelper.TABLE_TOEPFE, JournalDbHelper.columns_TOEPFE(), JournalDbHelper.COLUMN_TOPFNAME + "='" + topfname + "'", null, null, null, null);
        Log.d(logTag, cursor.getCount() + " db-Eintrag mit topfname " + topfname + " gelesen.");
        cursor.moveToFirst(); // topfname is unique, so there can be only one entry
        int id = getInt(cursor, JournalDbHelper.COLUMN_TOPFID);
        cursor.close();
        return id;
    }

    public String getTopfName(int topfid) {
        Cursor cursor = db.query(JournalDbHelper.TABLE_TOEPFE, JournalDbHelper.columns_TOEPFE(), JournalDbHelper.COLUMN_TOPFID + "=" + topfid, null, null, null, null);
        Log.d(logTag, cursor.getCount() + " db-Eintrag mit topfid " + topfid + " gelesen.");
        cursor.moveToFirst(); // topfid is unique, so there can be only one entry
        String name = getString(cursor, JournalDbHelper.COLUMN_TOPFNAME);
        cursor.close();
        return name;
    }

    // get list of all Toepfe - e.g. to populate ListView
    public ArrayList<String> getAllToepfe() {
        ArrayList<String> list = new ArrayList<>();
        Cursor cursor = db.query(JournalDbHelper.TABLE_TOEPFE, JournalDbHelper.columns_TOEPFE(), null, null, null, null, null);
        Log.d(logTag, cursor.getCount() + " db-Einträge gelesen.");

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            String t = getString(cursor, JournalDbHelper.COLUMN_TOPFNAME);
            list.add(t);
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    // add a topf in the database
    public void addTopf(String topfname) {
        ContentValues cv = new ContentValues();
        cv.put(JournalDbHelper.COLUMN_TOPFNAME, topfname);

        long insertid = db.insert(JournalDbHelper.TABLE_TOEPFE, null, cv);
        Log.d(logTag, "db entry added with insert id " + insertid);
    }

    public void editTopf(String oldTopfname, String newTopfname) {
        ContentValues cv = new ContentValues();
        cv.put(JournalDbHelper.COLUMN_TOPFNAME, newTopfname);

        int id = getTopfId(oldTopfname);
        db.update(JournalDbHelper.TABLE_TOEPFE, cv, JournalDbHelper.COLUMN_TOPFID + "=" + id, null);
        Log.d(logTag, "updated db entry " + oldTopfname + " -> " + newTopfname);
    }




    // ====================== Transaction Templates =====================
    // get template from database cursor
    public TransactionTemplate cursorToTemplate(Cursor cursor) {
        String payee = getString(cursor, JournalDbHelper.COLUMN_PAYEE);
        int id = getInt(cursor, JournalDbHelper.COLUMN_ID);

        TransactionTemplate t = new TransactionTemplate(payee);
        for (int i=0; i<JournalDbHelper.MAX_POSTINGS; i++) {
            String account = getString(cursor, JournalDbHelper.columnAcc(i));
            if (account != null) {
                t.addAccount(account);
            }
        }
        t.setDatabaseID(id);

        return t;
    }

    // get list of all Templates - e.g. to populate ListView
    public ArrayList<TransactionTemplate> getAllTemplates() {
        ArrayList<TransactionTemplate> list = new ArrayList<>();
        Cursor cursor = db.query(JournalDbHelper.TABLE_TEMPLATES, JournalDbHelper.columns_TEMPLATES(), null, null, null, null, null);
        Log.d(logTag, cursor.getCount() + " db-Einträge gelesen.");

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            TransactionTemplate t = cursorToTemplate(cursor);
            list.add(t);
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    public TransactionTemplate getTemplate(String payee) {
        Cursor cursor = db.query(JournalDbHelper.TABLE_TEMPLATES, JournalDbHelper.columns_TEMPLATES(), JournalDbHelper.COLUMN_PAYEE + "=" + payee, null, null, null, null);
        cursor.moveToFirst();
        TransactionTemplate t = cursorToTemplate(cursor);
        cursor.close();
        return t;
    }

    // get list of all template payees
    public String[] getAllTemplatePayees() {
        ArrayList<TransactionTemplate> list = getAllTemplates();
        ArrayList<String> payees = new ArrayList<>();
        for (TransactionTemplate t : list) {
            payees.add(t.payee);
        }
        String[] s = new String[payees.size()];
        s = payees.toArray(s);
        return s;
    }

    public String[] getAllTemplateAccounts() {
        Cursor cursor = db.query(true, JournalDbHelper.TABLE_TEMPLATES, JournalDbHelper.columns_TEMPLATES(),  null, null, null, null, null, null);
        ArrayList<String> list = new ArrayList<>();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            TransactionTemplate t = cursorToTemplate(cursor);
            list.addAll(t.getAccounts());
            cursor.moveToNext();
        }
        cursor.close();
        String[] s = new String[list.size()];
        s = list.toArray(s);
        return s;
    }

    // add a template to the database
    public void addTemplate(TransactionTemplate t) {
        ContentValues cv = new ContentValues();
        cv.put(JournalDbHelper.COLUMN_PAYEE, t.payee);
        for (int i=0; i<t.numAccounts(); i++) {
            cv.put(JournalDbHelper.columnAcc(i), t.getAccount(i));
        }
        long insertid = db.insert(JournalDbHelper.TABLE_TEMPLATES, null, cv);
        Log.d(logTag, "db entry added with insert id " + insertid);
    }
    public void addTemplate(String payee, String acc1, String acc2) {
        addTemplate(new TransactionTemplate(payee, acc1, acc2));
    }
    public void addTemplate(Transaction t) {}

    // edit a template (update)
    public void editTemplate(TransactionTemplate t){
        ContentValues cv = new ContentValues();
        cv.put(JournalDbHelper.COLUMN_PAYEE, t.payee);
        for (int i=0; i<t.numAccounts(); i++) {
            cv.put(JournalDbHelper.columnAcc(i), t.getAccount(i));
        }
        db.update(JournalDbHelper.TABLE_TEMPLATES, cv, JournalDbHelper.COLUMN_ID + "=" + t.getDatabaseID(), null);
        Log.d(logTag, "updated db entry with id " + t.getDatabaseID());
    }

    // delete a template
    public void  deleteTemplate(TransactionTemplate t) {
        int id = t.getDatabaseID();
        int num = db.delete(JournalDbHelper.TABLE_TEMPLATES, JournalDbHelper.COLUMN_ID + "=" + id, null);

        if (num > 1)
            throw new RuntimeException("deleteTemplate() deleted "+num+" Transactions, but 1 was expected.");
        else if (num == 0)
            throw new RuntimeException("deleteTemplate(): no Transaction with id "+id+" found.");
    }

    // add default templates
    protected void addDefaultTemplates() {
        addTemplate("Edeka", "Ausgaben:Bargeld", "Ausgaben:Lebensmittel");
        addTemplate("Rewe", "Ausgaben:Bargeld", "Ausgaben:Lebensmittel");
        addTemplate("Grieche", "Ausgaben:Bargeld", "Ausgaben:Ausgehen:Gastronomie");
    }
}
