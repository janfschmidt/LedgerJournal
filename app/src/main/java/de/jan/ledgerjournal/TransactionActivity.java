package de.jan.ledgerjournal;

import android.database.Cursor;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
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


        // auto complete from Database
        SimpleCursorAdapter payeeAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null, new String[] {JournalDbHelper.COLUMN_PAYEE}, new int[] {android.R.id.text1}, 0);
        inputPayee.setAdapter(payeeAdapter);
        inputPayee.setThreshold(1);

        payeeAdapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            public String convertToString(android.database.Cursor cursor) {
                return dataSource.cursorToTemplate(cursor).payee;
            }
        });

        payeeAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence constraint) {
                Cursor cursor = null;
                if (constraint!=null && constraint!="") {
                    cursor = dataSource.getMatching(JournalDbHelper.TABLE_TEMPLATES, JournalDbHelper.COLUMN_PAYEE, constraint.toString());
                }
                return cursor;
            }
        });


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




    @Override
    protected void onResume() {
        super.onResume();
        dataSource.open();
    }

    @Override
    protected void onStop() {
        super.onStop();
        dataSource.close();
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
        if (editMode) {
            t.setDatabaseID(editme.getDatabaseID());
            dataSource.editTransaction(t);
        }
        else {
            dataSource.addTransaction(t, topfId);
        }
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
