package de.jan.ledgerjournal;

public class Transaction {
    public String date;
    public String payee;
    public String account;
    public String preCurrency;
    public String postCurrency;
    public double amount;

    public Transaction(String date, String payee, String account, double amount, String preCurrency, String postCurrency) {
        this.date = date;
        this.payee = payee;
        this.account = account;
        this.preCurrency = preCurrency;
        this.amount = amount;
        this.postCurrency = postCurrency;
    }
    public Transaction(String date) {
        this.date = date;
        this.payee = "Proft";
        this.account = "keinPlan";
        this.preCurrency = "";
        this.amount = 12.34;
        this.postCurrency = "€";
    }
    public Transaction() {
        this("2016/01/01");
    }
}
