package de.jan.ledgerjournal;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
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
    int topfId;

    JournalDataSource dataSource;
    ToepfeDataSource toepfeSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Up-Button

        dataSource = new JournalDataSource(this);
        toepfeSource = new ToepfeDataSource(this);
        toepfeSource.open();

        Bundle bundle = getIntent().getExtras();
        topfId = bundle.getInt("topfId");


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
                i.putExtra("topfId", topfId);
                startActivity(i);
            }
        });
    }

    protected void onStart() {
        super.onStart();

        toepfeSource.open();
        dataSource.open();
        this.setTitle(toepfeSource.getTopfName(topfId));

        //populate list
        showAllJournalTransactions();
    }

    protected void onResume() {
        super.onResume();
        showAllJournalTransactions();
    }

    protected void onStop() {
        super.onStop();
        toepfeSource.close();
        dataSource.close();
    }


    private void showAllJournalTransactions() {
        ArrayList<Transaction> transactions = dataSource.getAllTransactions(topfId);
        journalList.clear();
        journalList.addAll(transactions);
        journalAdapter.notifyDataSetChanged();
    }

}




class TransactionsAdapter extends ArrayAdapter<Transaction> {

    public TransactionsAdapter(Context context, ArrayList<Transaction> transactions) {
        super(context, 0, transactions);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        // Get the data item for this position
        Transaction t = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.transaction, parent, false);

            LinearLayout postings = (LinearLayout) view.findViewById(R.id.postingsLayout);
            //for each posting, postingslayout created
            for (Posting p : t.postings) {
                PostingLayout pl = new PostingLayout(getContext());
                pl.setAccount(p.account);
                pl.setValue(p.value());
                postings.addView(pl);
            }
        }

        // Lookup view for data population
        TextView date = (TextView) view.findViewById(R.id.transactDate);
        TextView payee = (TextView) view.findViewById(R.id.transactPayee);

        // Populate the data into the template view using the data object
        date.setText(t.date);
        payee.setText(t.payee);

        // Return the completed view to render on screen
        return view;
    }
}
