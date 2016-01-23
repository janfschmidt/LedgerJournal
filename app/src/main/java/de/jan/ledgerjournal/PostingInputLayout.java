package de.jan.ledgerjournal;

import android.content.Context;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by jan on 23.01.2016.
 *
 * Single posting to be displayed in TransactionActivity for input of a new Transaction
 * posting consists of account and value (with commodity)
 */
public class PostingInputLayout extends LinearLayout {

    public AutoCompleteTextView account;
    public EditText amount;
    public TextView currency;
    protected LayoutParams accParams;
    protected LayoutParams amountParams;
    protected LayoutParams currParams;

    ArrayAdapter<String> accountAdapter;

    PostingInputLayout(Context context) {
        super(context);
        this.setOrientation(LinearLayout.HORIZONTAL);
        LayoutParams postingParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(postingParams);

        account = new AutoCompleteTextView(context);
        accParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        accParams.weight = 3;
        accParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        account.setLayoutParams(accParams);
        account.setHint("Your:Account");

        amount = new EditText(context);
        amountParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        amountParams.weight = 8;
        amountParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        amount.setLayoutParams(amountParams);
        amount.setGravity(Gravity.RIGHT);
        amount.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        amount.setHint("0.00");

        currency = new TextView(context);
        currParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        currParams.weight = 10;
        currParams.gravity = Gravity.CENTER;
        currency.setLayoutParams(currParams);
        currency.setText("€");


        // auto complete from string lists in strings.xml
        String[] accounts = getResources().getStringArray(R.array.accountList);
        accountAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1,accounts);
        account.setAdapter(accountAdapter);
        account.setThreshold(1);

        this.addView(account);
        this.addView(amount);
        this.addView(currency);
    }



    public String getAccount() {return account.getText().toString();}
    public double getAmount() {return parseAmount(amount.getText().toString());}
    public String getCurrency() {return currency.getText().toString();}

    public Posting getPosting() {
        Posting p = new Posting( getAccount(), getAmount(), getCurrency() );
        return p;
    }

    private static double parseAmount(String s) {
        double res = 0.0;
        try {
            res = Double.parseDouble(s);
        } catch (NullPointerException e) {
        } catch (NumberFormatException e) {
        }
        return res;
    }
}
