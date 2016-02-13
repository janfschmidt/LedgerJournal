package de.jan.ledgerjournal;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;

/**
 * Created by jan on 23.01.2016.
 *
 * Single posting to be displayed in TransactionActivity for input of a new Transaction
 * posting consists of account and value (with commodity)
 */
public class PostingInputLayout extends LinearLayout {

    protected AutoCompleteTextView account;
    protected EditText amount;
    protected TextView currency;
    protected LayoutParams accParams;
    protected LayoutParams amountParams;
    protected LayoutParams currParams;

    String[] accounts = new String[] {};
    ArrayAdapter<String> accountAdapter;

    PostingInputLayout(Context context) {
        super(context);
        this.setOrientation(LinearLayout.HORIZONTAL);
        LayoutParams postingParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(postingParams);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        account = new AutoCompleteTextView(context);
        accParams = new LayoutParams(0,LayoutParams.WRAP_CONTENT);
        accParams.weight = 1;
        accParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;

        account.setLayoutParams(accParams);
        account.setHint(R.string.layout_posting_accounthint);
        account.setSingleLine();
        account.setEllipsize(TextUtils.TruncateAt.START);
        //account.setHorizontallyScrolling(true);

        amount = new EditText(context);
        amountParams = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        amountParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        amount.setLayoutParams(amountParams);
        amount.setGravity(Gravity.RIGHT);
        amount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        amount.setHint(formatAmount(0.0));

        currency = new TextView(context);
        currParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        currParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        currency.setLayoutParams(currParams);
        currency.setText(sharedPref.getString("currency", "@string/preferences_currency_default"));


        // auto complete from string list
        accountAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, accounts);
        account.setAdapter(accountAdapter);
        account.setThreshold(1);


        addViews(sharedPref.getBoolean("currpos", true));
    }

    public void setAutoCompleteAccounts(String[] acc) {
        accounts = acc;
        accountAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1, accounts);
        account.setAdapter(accountAdapter);
    }

    public String getAccount() {return account.getText().toString();}
    public double getAmount() {return parseAmount(amount.getText().toString());}
    public String getCurrency() {return currency.getText().toString();}

    public Posting getPosting() {
        return new Posting( getAccount(), getAmount(), getCurrency() );
    }

    public void setPosting(Posting p) {
        account.setText(p.account);
        if (p.amount != 0.0) {
            amount.setText(formatAmount(p.amount));
        }
        currency.setText(p.currency);
        setCurrencyPosition(p.currencyPosition);
    }

    protected String formatAmount(double amount) {
        DecimalFormat format = new DecimalFormat("0.00");
        return format.format(amount);
    }

    private void addViews(boolean currpos) {
        addView(account);
        if (currpos) {
            addView(amount);
            addView(currency);
        }
        else {
            addView(currency);
            addView(amount);
        }
    }
    public void setCurrencyPosition(boolean currpos) {
        removeAllViews();
        addViews(currpos);
        Log.d("PostingInputLayout", "currencyPosition set: " + currpos);
    }

    public void setAccount(String acc) {
        account.setText(acc);
    }

    private static double parseAmount(String s) {
        double res = 0.0;
        try {
            res = Double.parseDouble(s);
        } catch (Exception e) {
        }
        return res;
    }
}
