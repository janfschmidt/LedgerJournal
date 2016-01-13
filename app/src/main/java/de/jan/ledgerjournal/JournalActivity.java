package de.jan.ledgerjournal;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class JournalActivity extends AppCompatActivity {

    ListView journalListView;
    TransactionsAdapter journalAdapter;
    ArrayList<Transaction> journalList = new ArrayList<>();
    String topfName;
    int topfId;

    JournalDataSource dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Up-Button

        Bundle bundle = getIntent().getExtras();
        topfName = bundle.getString("topfName");
        topfId = bundle.getInt("topfId");

        this.setTitle(topfName);

        dataSource = new JournalDataSource(this);

        //attaching TransactionsAdapter to journalList
        journalListView = (ListView) findViewById(R.id.journalListView);
        journalAdapter = new TransactionsAdapter(this, journalList);
        journalListView.setAdapter(journalAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.journalFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //addTransaction();
                Intent i = new Intent(JournalActivity.this, TransactionActivity.class);
                i.putExtra("topfName", topfName);
                i.putExtra("topfId", topfId);
                startActivity(i);
            }
        });
    }

    protected void onStart() {
        super.onStart();

        //populate list
        /*
        Transaction tmp = new Transaction("2015/12/24", "Weihnachtsmann", "Ausgaben:Geschenke", 101.42, "€");
        journalList.add(tmp);
        journalAdapter.notifyDataSetChanged();
        */
        showAllJournalTransactions();
    }

    public void addTransaction() {
        Posting[] p = new Posting[2];
        p[0].account = "Ausgaben:Geschenke:Spaß mit Soße und ganz viel Zeug";
        p[0].amount = 101.42;
        Transaction tmp = new Transaction("2015/12/24", "Weihnachtsmann\n mein Freund", p, "€");
        journalList.add(tmp);
        journalAdapter.notifyDataSetChanged();
    }

    private void showAllJournalTransactions() {
        dataSource.open();
        ArrayList<Transaction> transactions = dataSource.getAllTransactions(topfId);
        journalList.clear();
        journalList.addAll(transactions);
        journalAdapter.notifyDataSetChanged();
        dataSource.close();
    }

}




class TransactionsAdapter extends ArrayAdapter<Transaction> {

    public TransactionsAdapter(Context context, ArrayList<Transaction> transactions) {
        super(context, 0, transactions);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Transaction t = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.transaction, parent, false);
        }

        // Lookup view for data population
        TextView date = (TextView) convertView.findViewById(R.id.transactDate);
        TextView payee = (TextView) convertView.findViewById(R.id.transactPayee);
        LinearLayout postings = (LinearLayout) convertView.findViewById(R.id.postingsLayout);
        TextView account = (TextView) convertView.findViewById(R.id.entryAccount);
        TextView value = (TextView) convertView.findViewById(R.id.entryValue);
        TextView account2 = (TextView) convertView.findViewById(R.id.entryAccount2);
        TextView value2 = (TextView) convertView.findViewById(R.id.entryValue2);


        // Populate the data into the template view using the data object
        date.setText(t.date);
        payee.setText(t.payee);

       /* //for each posting, linearlayout created from code in PostingsLayout class. Not working...
        PostingLayout p = new PostingLayout(getContext());
        p.setAccount(t.account);
        p.setValue("25.42 €");
        postings.addView(p);*/

        account.setText(t.posting(0).account);
        value.setText(getValueString(t.posting(0).amount, t.currency));
        account2.setText(t.posting(1).account);
        value2.setText(getValueString(t.posting(1).amount, t.currency));

        // Return the completed view to render on screen
        return convertView;
    }

    private String getValueString(double amount, String currency) {
        if (amount == 0.0)
            return "";
        else
            return String.format("%.2f %s", amount, currency);
    }
}
