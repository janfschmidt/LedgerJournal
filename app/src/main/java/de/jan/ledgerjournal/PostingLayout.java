package de.jan.ledgerjournal;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by jan on 10.01.2016.
 *
 * Single posting to be displayed in a Transaction in a Journal
 * posting consists of account and value (with commodity)
 */
public class PostingLayout extends LinearLayout {
    TextView account;
    TextView value;

    PostingLayout(Context context) {
        super(context);

        LayoutParams postingParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(postingParams);

        account = new TextView(context);
        value = new TextView(context);
        LinearLayout.LayoutParams textParams = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        int margin = (int) getResources().getDimension(R.dimen.activity_horizontal_margin);
        textParams.setMargins(margin, 0, margin, 0);
        textParams.weight = 1;
        account.setLayoutParams(textParams);
        textParams.weight = 0;
        textParams.gravity = Gravity.RIGHT;
        value.setLayoutParams(textParams);
    }

    public void setAccount(String acc) {account.setText(acc);}
    public void setValue(String val) {value.setText(val);}

}
