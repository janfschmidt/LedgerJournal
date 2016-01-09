package de.jan.ledgerjournal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TransactionActivity extends AppCompatActivity implements View.OnClickListener {

    EditText dateText;
    Button okButton;

    Calendar c;
    SimpleDateFormat dateFormater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Up-Button

        dateText = (EditText) findViewById(R.id.editDate);

        this.setTitle("Add new Transaction");

        c = Calendar.getInstance();
        dateFormater = new SimpleDateFormat("yyyy/MM/dd");
        dateText.setText( dateFormater.format(c.getTime()), TextView.BufferType.EDITABLE);
        // setzen eines Datums: Date x = dateFormater.parse("2015/11/12")

        okButton = (Button) findViewById(R.id.button);
        okButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Transaction t = new Transaction();
        t.date = dateText.getText().toString();

    }
}
