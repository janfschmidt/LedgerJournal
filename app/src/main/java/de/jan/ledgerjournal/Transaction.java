package de.jan.ledgerjournal;

import android.util.Log;

public class Transaction {
    public String date;
    public String payee;
    public String currency;
    public Posting[] postings = new Posting[4];

    public Transaction(String date, String payee, String currency){
        this.date = date;
        this.payee = payee;
        this.currency = currency;
        for (int i=0; i<postings.length; i++) {
            Log.d("Transaction Constructor", "init Posting " + i);
            postings[i] = new Posting();
        }
    }

    public Transaction(String date, String payee, Posting[] postings, String currency){
        this(date,payee,currency);

        if (postings.length > this.postings.length)
            throw new RuntimeException("Illegal number of postings given to Transaction");
        else {
            for (int i=0; i<postings.length; i++)
                this.postings[i] = postings[i];
        }
    }

    public Transaction() {
        this("2016/01/01", "Mr X", "€");
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
}