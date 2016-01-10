package de.jan.ledgerjournal;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TransactionActivity extends AppCompatActivity implements View.OnClickListener {
    String topfName;
    int topfId;

    EditText inputDate;
    AutoCompleteTextView inputPayee;
    AutoCompleteTextView inputAcc0;
    EditText inputVal0;
    AutoCompleteTextView inputAcc1;
    EditText inputVal1;
    Button okButton;

    ArrayAdapter<String> accountAdapter;
    ArrayAdapter<String> payeeAdapter;

    Calendar c;
    SimpleDateFormat dateFormater;

    JournalDataSource dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Up-Button

        dataSource = new JournalDataSource(this);

        Bundle bundle = getIntent().getExtras();
        topfName = bundle.getString("topfName");
        topfId = bundle.getInt("topfId");

        this.setTitle(topfName);

        inputDate = (EditText) findViewById(R.id.inputDate);
        inputPayee = (AutoCompleteTextView) findViewById(R.id.inputPayee);
        inputAcc0 = (AutoCompleteTextView) findViewById(R.id.inputAcc0);
        inputVal0 = (EditText) findViewById(R.id.inputVal0);
        inputAcc1 = (AutoCompleteTextView) findViewById(R.id.inputAcc1);
        inputVal1 = (EditText) findViewById(R.id.inputVal1);

        // auto complete from string lists in strings.xml
        String[] accounts = getResources().getStringArray(R.array.accountList);
        accountAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,accounts);
        String[] payees = getResources().getStringArray(R.array.payeeList);
        payeeAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,payees);
        inputAcc0.setAdapter(accountAdapter);
        inputAcc0.setThreshold(1);
        inputAcc1.setAdapter(accountAdapter);
        inputAcc1.setThreshold(1);
        inputPayee.setAdapter(payeeAdapter);
        inputPayee.setThreshold(1);


        // set default date (today)
        c = Calendar.getInstance();
        dateFormater = new SimpleDateFormat("yyyy/MM/dd");
        inputDate.setText(dateFormater.format(c.getTime()), TextView.BufferType.EDITABLE);
        // setzen eines Datums: Date x = dateFormater.parse("2015/11/12")

        okButton = (Button) findViewById(R.id.button);
        okButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        Transaction t = new Transaction();

        // write data to transaction
        t.date = inputDate.getText().toString();
        t.payee = inputPayee.getText().toString();
        t.currency = "€";
        t.postings[0].account = inputAcc0.getText().toString();
        t.postings[0].amount = parseAmount(inputVal0.getText().toString());
        t.postings[1].account = inputAcc1.getText().toString();
        t.postings[1].amount = parseAmount(inputVal1.getText().toString());

        //write transaction to database
        dataSource.open();
        dataSource.addTransaction(t, topfId);
        dataSource.close();
        finish();
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
