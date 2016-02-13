package de.jan.ledgerjournal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
    protected boolean getBool(Cursor cursor, String columnname) {
        int b = getInt(cursor, columnname);
        if (b==0)
            return false;
        else
            return true;
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
    public Cursor getMatchingTemplate(String columnname, String constraint) {
        return db.query(JournalDbHelper.TABLE_JOURNAL, JournalDbHelper.columns_TEMPLATES(), JournalDbHelper.getTemplateFilter(columnname + " LIKE ?"), new String[] {constraint+"%"}, null, null, null);
    }



    // ====================== Transactions =====================
    // get Transaction from database cursor
    private Transaction cursorToTransaction(Cursor cursor) {
        String date = getString(cursor, JournalDbHelper.COLUMN_DATE);
        String payee = getString(cursor, JournalDbHelper.COLUMN_PAYEE);
        String currency = getString(cursor, JournalDbHelper.COLUMN_CURRENCY);
        int id = getInt(cursor, JournalDbHelper.COLUMN_ID);
        boolean currpos = getBool(cursor, JournalDbHelper.COLUMN_CURRENCYPOSITION);

        Transaction t = new Transaction(date,payee, currency, currpos);
        for (int i=0; i<JournalDbHelper.MAX_POSTINGS; i++) {
            String account = getString(cursor, JournalDbHelper.columnAcc(i));
            if (account != null && !account.equals("")) {
                t.addPosting(account, getDouble(cursor, JournalDbHelper.columnVal(i)));
                //Log.d(logTag, "Posting " + i + ": " + t.posting(i).print());
            }
        }
        t.setDatabaseID(id);

        return t;
    }

    // get list of all Transactions from given Topf - e.g. to populate ListView
    public ArrayList<Transaction> getAllTransactions(int topfid) {
        ArrayList<Transaction> list = new ArrayList<>();
        Cursor cursor = db.query(JournalDbHelper.TABLE_JOURNAL, JournalDbHelper.columns_JOURNAL(), JournalDbHelper.getTopfFilter(topfid), null, null, null, null);
        Log.d(logTag, cursor.getCount() + " db-Einträge fuer Topfid " + topfid + " aus Tabelle "+JournalDbHelper.TABLE_JOURNAL+" gelesen.");

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
        cv.put(JournalDbHelper.COLUMN_CURRENCYPOSITION, (t.currencyPosition) ? 1 : 0);
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
        cv.put(JournalDbHelper.COLUMN_CURRENCYPOSITION, (t.currencyPosition)? 1 : 0);
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


    // delete all Transactions from a Journal/Topf given by id
    public void clearTopf(int topfid) {
        int num = db.delete(JournalDbHelper.TABLE_JOURNAL, JournalDbHelper.COLUMN_TOPFID + "=" + topfid, null);

        if (num == 0)
            throw new RuntimeException("deleteTopf(): no Transaction with topfid "+topfid+" found.");

        Log.d(logTag, "cleared Journal with Topfid " + topfid + ", " + num + " Transactions deleted.");
    }

    // delete a complete Journal/Topf by id
    public void deleteTopf(int topfid) {
        clearTopf(topfid);
        db.delete(JournalDbHelper.TABLE_TOEPFE, JournalDbHelper.COLUMN_TOPFID + "=" + topfid, null);
        Log.d(logTag, "deleted Journal with Topfid " + topfid + ".");
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
        Log.d(logTag, cursor.getCount() + " Topfname zu topfid " + topfid + " aus db gelesen.");
        cursor.moveToFirst(); // topfid is unique, so there can be only one entry
        String name = getString(cursor, JournalDbHelper.COLUMN_TOPFNAME);
        cursor.close();
        return name;
    }

    // get list of all Toepfe (NOT INCLUDING TEMPLATES) - e.g. to populate ListView
    public ArrayList<String> getAllToepfe() {
        ArrayList<String> list = new ArrayList<>();
        Cursor cursor = db.query(JournalDbHelper.TABLE_TOEPFE, JournalDbHelper.columns_TOEPFE(), null, null, null, null, null);
        Log.d(logTag, cursor.getCount() + " db-Einträge gelesen.");

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            if (getInt(cursor, JournalDbHelper.COLUMN_TOPFID) != JournalDbHelper.TEMPLATE_TOPFID) {
                String t = getString(cursor, JournalDbHelper.COLUMN_TOPFNAME);
                list.add(t);
            }
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
    public void addTemplateTopf() {
        ContentValues cv = new ContentValues();
        cv.put(JournalDbHelper.COLUMN_TOPFNAME, "Templates");
        cv.put(JournalDbHelper.COLUMN_TOPFID, JournalDbHelper.TEMPLATE_TOPFID);

        long insertid = db.insert(JournalDbHelper.TABLE_TOEPFE, null, cv);
        Log.d(logTag, "db entry for templates added with insert id " + insertid);
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
            if (account != null && !account.equals("")) {
                t.addAccount(account);
            }
        }
        t.setDatabaseID(id);

        return t;
    }

    // get list of all Templates - e.g. to populate ListView
    public ArrayList<Transaction> getAllTemplates() {
       return getAllTransactions(JournalDbHelper.TEMPLATE_TOPFID);
    }

    public TransactionTemplate getTemplate(String payee) {
        Cursor cursor = db.query(JournalDbHelper.TABLE_JOURNAL, JournalDbHelper.columns_TEMPLATES(), JournalDbHelper.getTemplateFilter(JournalDbHelper.COLUMN_PAYEE + "='" + payee + "'"), null, null, null, null);
        cursor.moveToFirst();
        TransactionTemplate t = cursorToTemplate(cursor);
        cursor.close();
        return t;
    }


    public ArrayList<String> getAllTemplatePayees() {
        Cursor cursor = db.query(true, JournalDbHelper.TABLE_JOURNAL, JournalDbHelper.columns_TEMPLATES(),  JournalDbHelper.getTemplateFilter(), null, null, null, null, null);
        ArrayList<String> list = new ArrayList<>();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            TransactionTemplate t = cursorToTemplate(cursor);
            list.add(t.payee);
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    public String[] getAllTemplateAccounts() {
        Cursor cursor = db.query(true, JournalDbHelper.TABLE_JOURNAL, JournalDbHelper.columns_TEMPLATES(),  JournalDbHelper.getTemplateFilter(), null, null, null, null, null);
        HashSet<String> list = new HashSet<>();
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

    public boolean addTemplate(Transaction t) {
        t.date = "";
        t.clearAmounts();
        // check if template with this payee already exists
        if (getAllTemplatePayees().contains(t.payee)) {
            return false;
        } else {
            addTransaction(t, JournalDbHelper.TEMPLATE_TOPFID);
            return true;
        }
    }
    public boolean addTemplate(String payee, String acc1, String acc2) {
        Transaction t = new Transaction("", payee, "");
        t.addPosting(acc1, 0.0);
        t.addPosting(acc2, 0.0);
        return addTemplate(t);
    }

    public void replaceTemplate(Transaction t) {
        Transaction old = getTemplate(t.payee).toTransaction();
        deleteTransaction(old);
        addTemplate(t);
    }


    // add default templates
    protected void addDefaultTemplates() {
        addTemplateTopf();
        addTemplate("Edeka", "Ausgaben:Bargeld", "Ausgaben:Lebensmittel");
        addTemplate("Rewe", "Ausgaben:Bargeld", "Ausgaben:Lebensmittel");
        addTemplate("Grieche", "Ausgaben:Bargeld", "Ausgaben:Ausgehen:Gastronomie");
    }
}
