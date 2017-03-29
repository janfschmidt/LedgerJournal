
/*
 * Copyright (c) 2016-2017 Jan Felix Schmidt <janschmidt@mailbox.org>
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

import java.util.ArrayList;


/**
 * Created by jan on 06.02.2016. Template version of Transaction
 */

public class TransactionTemplate {
    public String payee;
    private ArrayList<String> accounts = new ArrayList<>();
    private int databaseID; // id to identify template in database

    public TransactionTemplate(String payee, String account1, String account2) {
        this.payee = payee;
        if (!account1.equals(""))
            accounts.add(account1);
        if (!account2.equals(""))
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

