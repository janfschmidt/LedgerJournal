/*
 * Copyright (c) 2017 Jan Felix Schmidt <janschmidt@mailbox.org>
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

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;


public class Transaction implements Parcelable {
    public String date;
    public String payee;
    public String currency;
    public boolean currencyPosition;
    private ArrayList<Posting> postings = new ArrayList<>();
    private int databaseID; // id to identify Transaction in Journal database

    public Transaction(String date, String payee, String currency, boolean currencyPosition){
        this.date = date;
        this.payee = payee;
        this.currency = currency;
        this.currencyPosition = currencyPosition;
        this.databaseID = -1;
    }

    public Transaction(String date, String payee, String currency) {
        this(date, payee, currency, true);
    }

    public Transaction() {
        this("", "", "€");
    }

    //construct from Parcel
    public Transaction(Parcel in) {
        this.date = in.readString();
        this.payee = in.readString();
        this.currency = in.readString();
        this.currencyPosition = in.readByte()!=0;
        in.readTypedList(postings, Posting.CREATOR);
        this.databaseID = in.readInt();
    }

    public Posting posting(int index) {return postings.get(index);}
    public ArrayList<Posting> getPostings() {return postings;}
    public int numPostings() {return postings.size();}

    public void addPosting(Posting p){
        if (postings.size() >= JournalDbHelper.MAX_POSTINGS)
            throw new RuntimeException("Maximum number of postings per Transaction reached! addPosting() aborted.");
        else {
            p.currencyPosition = this.currencyPosition;
            postings.add(p);
        }
    }
    public void addPosting(String account, double amount) {
         addPosting( new Posting(account, amount, this.currency) );
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

    public String print(int width) {
        String s = date + " * " + payee + "\n";
        for (Posting p : postings) {
            s += "    " + p.print(width) + "\n";
        }
        s += "\n";
        return s;
    }
    public String print() {return print(35);}


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
        out.writeByte((byte) (currencyPosition? 1 : 0));
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
    boolean currencyPosition; //true=behind

    public Posting(String account, double amount, String currency, boolean currencyPosition) {
        this.account = account;
        this.amount = amount;
        this.currency = currency;
        this.currencyPosition = currencyPosition;
    }
    public Posting(String account, double amount, String currency) {
        this(account, amount, currency, true);
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
        this.currencyPosition = in.readByte()!=0;
    }


    public String print(int width) {
        return String.format("%-"+width+"s %12s", account, value());
    }

    public String value() {
        if (amount == 0.0)
            return "";
        else {
            if (currencyPosition)
                return String.format("%.2f %s", amount, currency);
            else
                return String.format("%s %.2f", currency, amount);
        }
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
        out.writeByte((byte) (currencyPosition? 1 : 0));
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