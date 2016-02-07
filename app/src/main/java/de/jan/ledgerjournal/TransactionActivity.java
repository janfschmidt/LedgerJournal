package de.jan.ledgerjournal;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
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
    String[] accounts;

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

        inputLayout = (LinearLayout) findViewById(R.id.transactionInputLayout);
        inputDate = (EditText) findViewById(R.id.inputDate);
        inputPayee = (AutoCompleteTextView) findViewById(R.id.inputPayee);

        // always add 2 Posting Input Lines
        addPostingInputLine();
        addPostingInputLine();


        // PAYEE auto complete from Database
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
                if (constraint != null && constraint != "") {
                    cursor = dataSource.getMatchingTemplate(JournalDbHelper.COLUMN_PAYEE, constraint.toString());
                }
                return cursor;
            }
        });

        // fill Template to form
        inputPayee.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                cursor.moveToPosition(position);

                insertTemplate( dataSource.cursorToTemplate(cursor) );
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
        String name = dataSource.getTopfName(topfId);
        this.setTitle(name);

        // ACCOUNT auto complete via string array filled from Database
        accounts = dataSource.getAllTemplateAccounts();
        for (PostingInputLayout pil : inputPostings) {
            pil.setAutoCompleteAccounts(accounts);
        }
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
            if (topfId == JournalDbHelper.TEMPLATE_TOPFID) {
                if (!dataSource.addTemplate(t))
                    dataSource.replaceTemplate(t);
            }
            else {
                dataSource.addTransaction(t, topfId);
            }
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
    protected void removePostingInputLine(PostingInputLayout pil){
        inputLayout.removeView(pil);
        inputPostings.remove(pil);
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

    protected void insertTemplate(TransactionTemplate t) {
        inputPayee.setText(t.payee);
        while (t.numAccounts() > inputPostings.size()) {
            addPostingInputLine();
        }
        int num = t.numAccounts();
        for (int i=0; i<num; i++) {
            inputPostings.get(i).setAccount(t.getAccount(i));
        }
        while (inputPostings.size() > num) {
            removePostingInputLine( inputPostings.get(num) );
        }
    }
    protected void insertTemplate(String payee) {insertTemplate( dataSource.getTemplate(payee) );}
}
