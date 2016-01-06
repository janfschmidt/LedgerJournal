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
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class JournalActivity extends AppCompatActivity {

    ListView journalListView;
    TransactionsAdapter journalAdapter;
    ArrayList<Transaction> journalList = new ArrayList<Transaction>();

    JournalDataSource dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Up-Button

        Bundle bundle = getIntent().getExtras();
        String topfName = bundle.getString("topf");

        this.setTitle(topfName);

        dataSource = new JournalDataSource(this);
        dataSource.open();
        dataSource.close();

        //attaching TransactionsAdapter to journalList
        journalListView = (ListView) findViewById(R.id.journalListView);
        journalAdapter = new TransactionsAdapter(this, journalList);
        journalListView.setAdapter(journalAdapter);

        //populate list
        Transaction tmp = new Transaction("2015/12/24", "Weihnachtsmann", "Ausgaben:Geschenke", 101.42, "", "€");
        journalList.add(tmp);
        journalAdapter.notifyDataSetChanged();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.journalFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTransaction();
                Intent i = new Intent(JournalActivity.this, TransactionActivity.class);
                startActivityForResult(i, 1);
            }
        });
    }

    public void addTransaction() {
        Transaction tmp = new Transaction("2015/12/24", "Weihnachtsmann", "Ausgaben:Geschenke", 101.42, "", "€");
        journalList.add(tmp);
        journalAdapter.notifyDataSetChanged();
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
        TextView account = (TextView) convertView.findViewById(R.id.entryAccount);
        TextView amount = (TextView) convertView.findViewById(R.id.entryAmount);
        TextView preCurrency = (TextView) convertView.findViewById(R.id.entryPreCurrency);
        TextView postCurrency = (TextView) convertView.findViewById(R.id.entryPostCurrency);
        // Populate the data into the template view using the data object
        date.setText(t.date);
        payee.setText(t.payee);
        account.setText(t.account);
        amount.setText(String.format("%1$,.2f",t.amount));
        preCurrency.setText(t.preCurrency);
        postCurrency.setText(t.postCurrency);

        // Return the completed view to render on screen
        return convertView;
    }
}
