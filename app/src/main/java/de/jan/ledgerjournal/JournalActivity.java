package de.jan.ledgerjournal;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class JournalActivity extends AppCompatActivity {

    Journal journal;
    ListView journalListView;
    TransactionsAdapter journalAdapter;

    int topfId;
    JournalDataSource dataSource;
    ToepfeDataSource toepfeSource;

    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Up-Button


        journal = new Journal();
        dataSource = new JournalDataSource(this);
        toepfeSource = new ToepfeDataSource(this);
        toepfeSource.open();

        Bundle bundle = getIntent().getExtras();
        topfId = bundle.getInt("topfId");


        //attaching TransactionsAdapter to journalList
        journalListView = (ListView) findViewById(R.id.journalListView);
        journalAdapter = new TransactionsAdapter(this, journal.list);
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



    // Share menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.menu_journal, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        // Set share intent
        mShareActionProvider.setShareIntent(createShareIntent());
        // Return true to display menu
        return true;
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        String filename = toepfeSource.getTopfName(topfId) + ".led";
        journal.export(filename);

        File journalFile = new File(getFilesDir(), filename);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(journalFile));
        startActivity(Intent.createChooser(shareIntent, "Share Ledger file using")); //ich möchte keinen "Chooser" sobald ich JournalActivity öffne!

        return shareIntent;
    }

    // Sets new share Intent.
    // Use this method to change or set Share Intent in your Activity Lifecycle.
    private void changeShareIntent(Intent shareIntent) {
        mShareActionProvider.setShareIntent(shareIntent);
    }




    private void showAllJournalTransactions() {
        journal.set( dataSource.getAllTransactions(topfId) );
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

            LinearLayout transactionLayout = (LinearLayout) view.findViewById(R.id.transactionLayout);
            //for each posting, PostingLayout is created
            for (Posting p : t.postings) {
                PostingLayout pl = new PostingLayout(getContext());
                pl.setAccount(p.account);
                pl.setValue(p.value());
                transactionLayout.addView(pl);
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
