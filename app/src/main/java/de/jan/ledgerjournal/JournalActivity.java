package de.jan.ledgerjournal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Up-Button


        dataSource = new JournalDataSource(this);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

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
        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
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

        File journalFile = new File(journal.exportFilePath( sharedPref.getString("exportpath", SettingsActivity.defaultPath(this)) ));
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(journalFile));
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name +" "+ getResources().getString(R.string.share_subject));
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

            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.context_journal, menu);
            if (topfId == JournalDbHelper.TEMPLATE_TOPFID)
                menu.removeItem(R.id.context_journal_template);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        if (item.getItemId() == R.id.context_journal_edit) {
            Log.d("JournalActivity", "Context menu: edit selected");
            editJournalTransaction(info.position);
        }
        else if (item.getItemId() == R.id.context_journal_delete) {
            Log.d("JournalActivity", "Context menu: delete selected");
            deleteJournalTransaction(info.position);
        }
        else if (item.getItemId() == R.id.context_journal_template) {
            addToTemplates( journal.get(info.position) );
        }
        else {
            return false;
        }
        return true;
    }


    private void showAllJournalTransactions() {
        journal.set(dataSource.getAllTransactions(topfId));
        journalAdapter.notifyDataSetChanged();
        Log.d("JournalActivity", "called notifyDataSetChanged()");
    }

    private void deleteJournalTransaction(int position) {
        dataSource.deleteTransaction(journal.get(position));
        journal.remove(position);
        journalAdapter.notifyDataSetChanged();
    }

    private void clearJournal() {
        dataSource.clearTopf(topfId);
        journal.clear();
        journalAdapter.notifyDataSetChanged();
    }

    private void editJournalTransaction(int position) {
        Intent i = new Intent(JournalActivity.this, TransactionActivity.class);
        i.putExtra("topfId", topfId);
        i.putExtra("transaction", journal.get(position));
        startActivity(i);
    }

    private void addToTemplates(Transaction t) {
        if (dataSource.addTemplate(t)) {
            Toast toast = Toast.makeText(this, R.string.toast_addtemplate, Toast.LENGTH_SHORT);
            toast.show();
        }
        else {
            replaceTemplateDialog(t);
        }
    }


    private void saveToFile() {
        journal.export( sharedPref.getString("exportpath", SettingsActivity.defaultPath(this)) );
        Toast toast = Toast.makeText(this, getResources().getString(R.string.toast_exportfile) +" "+ journal.exportFilePath( sharedPref.getString("exportpath", SettingsActivity.defaultPath(this)) ), Toast.LENGTH_LONG);
        toast.show();
        if (topfId != JournalDbHelper.TEMPLATE_TOPFID) // deleting templates after export makes no sense
            deleteExported();
    }

    protected void replaceTemplateDialog(final Transaction t) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(getString(R.string.dialog_replacetemplate)+" " + t.payee + "?");
        alert.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dataSource.replaceTemplate(t);
                Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.toast_replacetemplate) +" "+ t.payee, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        alert.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    private void deleteExported() {
        String delete = sharedPref.getString("delete", "never");
        Log.d("JournalActivity", "deleteExported() called: Preference is " + delete);
        if (delete.equals("always")) {
            clearJournal();
        }
        else if (delete.equals("ask")) {
            deleteExportedDialog();
        }
        // else: nothing is deleted.
    }

    protected void deleteExportedDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(getResources().getString(R.string.dialog_deleteexported_1)+" "+journal.size()+" "+ getResources().getString(R.string.dialog_deleteexported_2)+" "+ journal.name + "?");
        alert.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                clearJournal();
            }
        });
        alert.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
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

        // If no existing view is reused: inflate the view & create PostingLayouts
        // This is done for new Transactions OR if number of postings has changed
        if (view == null || ((ViewGroup)view).getChildCount() != (t.numPostings()+1)) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.transaction, parent, false);
            LinearLayout transactionLayout = (LinearLayout) view.findViewById(R.id.transactionLayout);
            //for each posting, PostingLayout is created
            for (Posting p : t.getPostings()) {
                PostingLayout pl = new PostingLayout(getContext());
                pl.set(p);
                transactionLayout.addView(pl);
            }
        }
        // If view is reused: update PostingLayouts
        else {
            int numViews = ((ViewGroup)view).getChildCount();
            //Log.d("TransactionAdapter", "update up to "+numViews+" Views for Payee "+t.payee);
            int i=0;
            for (int n=0; i<numViews; ++n) {
                if (i >= t.numPostings())
                    break;
                View nextChild = ((ViewGroup)view).getChildAt(n);
                if (nextChild instanceof PostingLayout) {
                    //Log.d("TransactionAdapter", "update PostingLayout (child "+n+")");
                    ((PostingLayout)nextChild).set(t.posting(i));
                    i++;
                }
            }
        }

        // always set date & payee
        TextView date = (TextView) view.findViewById(R.id.transactDate);
        TextView payee = (TextView) view.findViewById(R.id.transactPayee);
        date.setText(t.date);
        payee.setText(t.payee);

        return view;
    }
}
