package de.jan.ledgerjournal;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class TransactionActivity extends AppCompatActivity {
    String topfName;
    int topfId;
    Transaction editme;
    boolean editMode;

    LinearLayout inputLayout;
    EditText inputDate;
    AutoCompleteTextView inputPayee;
    ArrayList<PostingInputLayout> inputPostings = new ArrayList<>();

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
        inputLayout = (LinearLayout) findViewById(R.id.transactionInputLayout);
        inputDate = (EditText) findViewById(R.id.inputDate);
        inputPayee = (AutoCompleteTextView) findViewById(R.id.inputPayee);

        // always add 2 Posting Input Lines
        addPostingInputLine();
        addPostingInputLine();


        // auto complete from string lists in strings.xml
        String[] payees = getResources().getStringArray(R.array.payeeList);
        payeeAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,payees);
        inputPayee.setAdapter(payeeAdapter);
        inputPayee.setThreshold(1);


        // fill form with given Transaction (edit Transaction)
        if (bundle.containsKey("transaction")) {
            editme = bundle.getParcelable("transaction");
            insertTransaction(editme);
            editMode = true;
        }
        // set default date (today) for new Transaction
        else {
            c = Calendar.getInstance();
            dateFormater = new SimpleDateFormat("yyyy/MM/dd");
            inputDate.setText(dateFormater.format(c.getTime()), TextView.BufferType.EDITABLE);
            editMode = false;
        }
    }


    // OK Button
    public void onOkClick(View v) {
        Transaction t = new Transaction();

        // write data to transaction
        t.date = inputDate.getText().toString();
        t.payee = inputPayee.getText().toString();
        t.currency = "€";
        for (PostingInputLayout pil : inputPostings) {
            t.addPosting( pil.getPosting() );
        }

        //write transaction to database
        dataSource.open();
        if (editMode) {
            t.setDatabaseID(editme.getDatabaseID());
            dataSource.editTransaction(t);
        }
        else {
            dataSource.addTransaction(t, topfId);
        }

        dataSource.close();
        finish();
    }

    // + Button (add Posting input line)
    public void onAddPostingClick(View v) {
        if (inputPostings.size() < JournalDbHelper.MAX_POSTINGS) {
            addPostingInputLine();
        }
        else {
            Toast toast = Toast.makeText(this, "Maximum number of Postings reached.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }



    protected void addPostingInputLine() {
        PostingInputLayout pil = new PostingInputLayout(this);
        inputPostings.add(pil);
        inputLayout.addView(pil);
    }

    protected void insertTransaction(Transaction t) {
        inputDate.setText(t.date);
        inputPayee.setText(t.payee);

        while (t.numPostings() > inputPostings.size()) {
            addPostingInputLine();
        }
        for (int i=0; i<t.numPostings(); i++) {
            inputPostings.get(i).setPosting(t.posting(i));
        }
    }
}
