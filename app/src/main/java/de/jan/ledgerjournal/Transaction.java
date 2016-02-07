package de.jan.ledgerjournal;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;

public class Transaction implements Parcelable {
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
        this("", "", "€");
    }

    //construct from Parcel
    public Transaction(Parcel in) {
        this.date = in.readString();
        this.payee = in.readString();
        this.currency = in.readString();
        in.readTypedList(postings, Posting.CREATOR);
        this.databaseID = in.readInt();
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

    public void clearAmounts() {
        for (Posting p : postings)
            p.amount = 0.0;
    }

    public TransactionTemplate toTemplate() {
        TransactionTemplate t = new TransactionTemplate(payee);
        t.setDatabaseID(databaseID);
        for (Posting p : postings)
            t.addAccount(p.account);
        return t;
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


    // functions for Parcelable - used to send a Transaction between Activities via Intent.putExtra()
    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags){
        out.writeString(date);
        out.writeString(payee);
        out.writeString(currency);
        out.writeTypedList(postings);
        out.writeInt(databaseID);
    }

    public static final Parcelable.Creator<Transaction> CREATOR =
            new Parcelable.Creator<Transaction>(){
                @Override
                public Transaction createFromParcel(Parcel source) {
                    return new Transaction(source);
                }
                @Override
                public Transaction[] newArray(int size) {
                    return new Transaction[size];
                }
            };
}




class Posting implements Parcelable {
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
    public Posting(Parcel in) {
        this.account = in.readString();
        this.amount = in.readDouble();
        this.currency = in.readString();
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


    // functions for Parcelable - used to send a Transaction between Activities via Intent.putExtra()
    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags){
        out.writeString(account);
        out.writeDouble(amount);
        out.writeString(currency);
    }

    public static final Parcelable.Creator<Posting> CREATOR =
            new Parcelable.Creator<Posting>(){
                @Override
                public Posting createFromParcel(Parcel source) {
                    return new Posting(source);
                }
                @Override
                public Posting[] newArray(int size) {
                    return new Posting[size];
                }
            };

}