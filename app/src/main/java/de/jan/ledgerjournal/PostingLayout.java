package de.jan.ledgerjournal;

import android.content.Context;
import android.text.TextUtils;
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
    public TextView account;
    public TextView value;
    protected LinearLayout.LayoutParams accParams;
    protected LinearLayout.LayoutParams valParams;

    PostingLayout(Context context) {
        super(context);
        this.setOrientation(LinearLayout.HORIZONTAL);

        LayoutParams postingParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(postingParams);

        account = new TextView(context);
        value = new TextView(context);
        accParams = new LayoutParams(0,LayoutParams.WRAP_CONTENT);
        valParams = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        int margin = (int) getResources().getDimension(R.dimen.activity_horizontal_margin);
        accParams.setMargins(margin, 0, margin, 0);
        accParams.gravity = Gravity.LEFT;
        accParams.weight = 1;
        account.setLayoutParams(accParams);
        account.setSingleLine();
        account.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        account.setHorizontallyScrolling(true);
        valParams.gravity = Gravity.RIGHT;
        value.setLayoutParams(valParams);

        this.addView(account);
        this.addView(value);
    }


    public void setAccount(String acc) {account.setText(acc);}
    public void setValue(String val) {value.setText(val);}
    public void set(Posting p) {setAccount(p.account); setValue(p.value());}

}
