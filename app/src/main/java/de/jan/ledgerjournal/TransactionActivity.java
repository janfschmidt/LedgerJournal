/*
 * Copyright (c) 2016-2017 Jan Felix Schmidt <janschmidt@mailbox.org>
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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
                    try {
                        cursor = dataSource.getMatchingTemplate(JournalDbHelper.COLUMN_PAYEE, constraint.toString());
                    } catch(Exception e) {}
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

        // "EDIT" MODE: fill form with given Transaction
        if (bundle.containsKey("transaction")) {
            editme = bundle.getParcelable("transaction");
            insertTransaction(editme);
            editMode = true;
        }
        // "ADD NEW" MODE: set default date (today) for new Transaction
        else {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            c = Calendar.getInstance();
            dateFormater = new SimpleDateFormat( sharedPref.getString("dateformat", "@string/preferences_dateformat_default") );
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
        for (int i=0; i<inputPostings.size(); i++) {
            inputPostings.get(i).setAutoCompleteAccounts(accounts);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        dataSource.close();
    }



    // menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.menu_transaction, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_ok:
                onOkClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    // OK/Save Button
    public void onOkClick() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Transaction t = new Transaction();

        // write data to transaction
        t.date = inputDate.getText().toString();
        t.payee = inputPayee.getText().toString();
        t.currency = inputPostings.get(0).getCurrency();
        Posting p = new Posting();
        for (PostingInputLayout pil : inputPostings) {
            t.addPosting(pil.getPosting());
        }

        //write transaction to database
        if (editMode) {
            t.setDatabaseID(editme.getDatabaseID());
            t.currencyPosition = editme.currencyPosition;
            dataSource.editTransaction(t);
            finish();
        }
        else {
            if (topfId == JournalDbHelper.TEMPLATE_TOPFID) {
                if (dataSource.addTemplate(t))
                    finish();
                else
                    replaceTemplateDialog(t);
            }
            else {
                t.currencyPosition = sharedPref.getBoolean("currpos", true);
                dataSource.addTransaction(t, topfId);
                finish();
            }
        }
    }

    // + Button (add Posting input line)
    public void onAddPostingClick(View v) {
        if (inputPostings.size() < JournalDbHelper.MAX_POSTINGS) {
            addPostingInputLine();
            inputPostings.get(inputPostings.size()-1).setAutoCompleteAccounts(accounts);
        }
        else {
            Toast toast = Toast.makeText(this, R.string.toast_maxpostings, Toast.LENGTH_SHORT);
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
            PostingInputLayout pil = inputPostings.get(i);
            pil.setPosting(t.posting(i));
            inputPostings.set(i,pil);
            //Log.d("TransactionActivity", "inputPostings "+i+": "+ t.posting(i).amount + "//" + pil.getAmount() +"//"+ inputPostings.get(i).getAmount());
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


    protected void replaceTemplateDialog(final Transaction t) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(getString(R.string.dialog_replacetemplate) + " " + t.payee + "?");
        alert.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dataSource.replaceTemplate(t);
                finish();
            }
        });
        alert.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        });
        alert.show();
    }
}
