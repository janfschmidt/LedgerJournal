package de.jan.ledgerjournal;

import android.util.Log;

import java.util.ArrayList;

public class Transaction {
    public String date;
    public String payee;
    public String currency;
    private ArrayList<Posting> postings = new ArrayList<Posting>();
    private int databaseID; // id to identify Transaction in Journal database

    public Transaction(String date, String payee, String currency){
        this.date = date;
        this.payee = payee;
        this.currency = currency;
        this.databaseID = -1;
    }

    public Transaction(String date, String payee, Posting[] postings, String currency){
        this(date,payee,currency);
        for (Posting p : postings)
            this.addPosting(p);
    }

    public Transaction() {
        this("2016/01/01", "Mr X", "€");
    }

    public Posting posting(int index) {return postings.get(index);}
    public ArrayList<Posting> getPostings() {return postings;}
    public int numPostings() {return postings.size();}

    public void addPosting(Posting p){
        if (postings.size() >= JournalDbHelper.MAX_POSTINGS)
            throw new RuntimeException("Maximum number of postings per Transaction reached! addPosting() aborted.");
        else
            postings.add(p);
    }
    public void addPosting(String account, double amount){
        this.addPosting(new Posting(account, amount, this.currency));
    }

    public void deletePosting(int index) {
        postings.remove(index);
    }

    protected void setDatabaseID(int id) {databaseID = id;}
    public int getDatabaseID() {
        if (databaseID == -1)
            throw new RuntimeException("Transaction DatabaseID was not set!");
        else
            return databaseID;
    }

    public String print() {
        String s = date + " * " + payee + "\n";
        for (Posting p : postings) {
            s += "\t" + p.print() + "\n";
        }
        s += "\n";
        return s;
    }
}


class Posting {
    public String account;
    public double amount;
    public String currency;

    public Posting(String account, double amount, String currency) {
        this.account = account;
        this.amount = amount;
        this.currency = currency;
    }
    public Posting() {
        this("", 0.0, "€");
    }
    public Posting(String account, double amount) {
        this(account, amount, "€");
    }
    public String print() {
        return String.format("%-40s %12s", account, value());
    }

    public String value() {
        if (amount == 0.0)
            return "";
        else
            return String.format("%.2f %s", amount, currency);
    }
}