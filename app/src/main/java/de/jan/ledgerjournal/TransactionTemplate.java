
package de.jan.ledgerjournal;

import android.provider.ContactsContract;

import java.util.ArrayList;


/**
 * Created by jan on 06.02.2016.
 */

public class TransactionTemplate {
    public String payee;
    private ArrayList<String> accounts = new ArrayList<>();
    private int databaseID; // id to identify template in database

    public TransactionTemplate(String payee, String account1, String account2) {
        this.payee = payee;
        if (account1 != "")
            accounts.add(account1);
        if (account2 != "")
            accounts.add(account2);
        databaseID = -1;
    }
    public TransactionTemplate(String payee) {this(payee, "", "");}

    public Transaction toTransaction() {
        Transaction t = new Transaction("",payee,"");
        t.setDatabaseID(databaseID);
        for (String acc : accounts)
            t.addPosting(acc, 0.0);
        return t;
    }


    public ArrayList<String> getAccounts() {return accounts;}
    public String getAccount(int index) {return accounts.get(index);}
    public int numAccounts() {return accounts.size();}

    void addAccount(String account) {
        if (accounts.size() >= JournalDbHelper.MAX_POSTINGS)
            throw new RuntimeException("Maximum number of accounts per TransactionTemplate reached! addAccount() aborted.");
    else
        accounts.add(account);
    }

    protected void setDatabaseID(int id) {databaseID = id;}
    public int getDatabaseID() {
        if (databaseID == -1)
            throw new RuntimeException("TransactionTemplate DatabaseID was not set!");
        else
            return databaseID;
    }

}

