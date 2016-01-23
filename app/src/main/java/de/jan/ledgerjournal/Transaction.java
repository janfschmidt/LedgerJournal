package de.jan.ledgerjournal;

import android.util.Log;

import java.util.ArrayList;

public class Transaction {
    public String date;
    public String payee;
    public String currency;
    protected ArrayList<Posting> postings = new ArrayList<Posting>();

    public Transaction(String date, String payee, String currency){
        this.date = date;
        this.payee = payee;
        this.currency = currency;
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
    public int numPostings() {return postings.size();}

    public void addPosting(Posting p){
        if (postings.size() >= JournalDbHelper.MAX_POSTINGS)
            throw new RuntimeException("Maximum number of postings per Transaction reached! addPosting() aborted.");
        else
            postings.add(p);
    }
    public void addPosting(String account, double amount){
        this.addPosting(new Posting(account, amount,this.currency));
    }

    public void deletePosting(int index) {
        postings.remove(index);
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
        return account + "\t" + value();
    }

    public String value() {
        if (amount == 0.0)
            return "";
        else
            return String.format("%.2f %s", amount, currency);
    }
}