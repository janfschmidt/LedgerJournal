package de.jan.ledgerjournal;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class JournalActivity extends AppCompatActivity {

    Journal journal;
    ListView journalListView;
    TransactionsAdapter journalAdapter;

    int topfId;
    JournalDataSource dataSource;

    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Up-Button


        dataSource = new JournalDataSource(this);

        Bundle bundle = getIntent().getExtras();
        topfId = bundle.getInt("topfId");

        //attaching TransactionsAdapter to journal
        journal = new Journal();
        journalListView = (ListView) findViewById(R.id.journalListView);
        journalAdapter = new TransactionsAdapter(this, journal.list);
        journalListView.setAdapter(journalAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.journalFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(JournalActivity.this, TransactionActivity.class);
                i.putExtra("topfId", topfId);
                startActivity(i);
            }
        });

        //context menu
        registerForContextMenu(journalListView);
    }

    protected void onResume() {
        super.onResume();
        dataSource.open();

        String name = dataSource.getTopfName(topfId);
        this.setTitle(name);
        journal.setName(name);

        showAllJournalTransactions();
    }

    protected void onStop() {
        super.onStop();
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
        mShareActionProvider.setOnShareTargetSelectedListener(new ShareActionProvider.OnShareTargetSelectedListener() {
            @Override
            public boolean onShareTargetSelected(ShareActionProvider actionProvider, Intent intent) {
                saveToFile();
                return false;
            }
        });

        // Set share intent
        mShareActionProvider.setShareIntent(createShareIntent());

        // Return true to display menu
        return true;
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/*");

        File journalFile = new File(journal.exportFilePath());
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(journalFile));
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "LedgerJournal export");
        //startActivity(Intent.createChooser(shareIntent, "Share Ledger file using")); //ich möchte keinen "Chooser" sobald ich JournalActivity öffne!
        return shareIntent;
    }


    //Other menu icons (export)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // menu_item_share not called, because it is handled by ShareActionProvider
            case R.id.menu_item_export:
                saveToFile();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }




   // List item context menu
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.journalListView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Transaction t = journal.get(info.position);
            menu.setHeaderTitle(t.date + "\t" + t.payee);

            menu.add("Edit Transaction");
            menu.add("Delete Transaction");
            menu.add("Add to Templates");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        if (item.getTitle()=="Edit Transaction") {
            Log.d("JournalActivity", "Context menu: edit selected");
            editJournalTransaction(info.position);
        }
        else if (item.getTitle()=="Delete Transaction") {
            Log.d("JournalActivity", "Context menu: delete selected");
            deleteJournalTransaction(info.position);
        }
        else if (item.getTitle()=="Add to Templates") {
            addToTemplates(info.position);
        }
        else {
            return false;
        }
        return true;
    }


    private void showAllJournalTransactions() {
        journal.set( dataSource.getAllTransactions(topfId) );
        journalAdapter.notifyDataSetChanged();
    }

    private void deleteJournalTransaction(int position) {
        dataSource.deleteTransaction( journal.get(position) );
        journal.remove(position);
        journalAdapter.notifyDataSetChanged();
    }

    private void editJournalTransaction(int position) {
        Intent i = new Intent(JournalActivity.this, TransactionActivity.class);
        i.putExtra("topfId", topfId);
        i.putExtra("transaction", journal.get(position));
        startActivity(i);
    }

    private void addToTemplates(int position) {
        dataSource.addTemplate( journal.get(position) );
        Toast toast = Toast.makeText(this, "Added Transaction to Templates.", Toast.LENGTH_SHORT);
        toast.show();
    }

    private void saveToFile() {
        journal.export();
        Toast toast = Toast.makeText(this, "Wrote file "+journal.exportFilePath(), Toast.LENGTH_LONG);
        toast.show();
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
            for (Posting p : t.getPostings()) {
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
