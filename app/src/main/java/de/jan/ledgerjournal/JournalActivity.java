package de.jan.ledgerjournal;

import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class JournalActivity extends AppCompatActivity {

    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Up-Button

        text = (TextView) findViewById(R.id.journalText);

        Bundle bundle = getIntent().getExtras();
        String topfName = bundle.getString("topf");

        this.setTitle(topfName);
        text.setText("Hier geht es um " + topfName);
    }


}
