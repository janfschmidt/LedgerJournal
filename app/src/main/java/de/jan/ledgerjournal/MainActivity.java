package de.jan.ledgerjournal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ListView topfListView;
    ArrayAdapter topfArrayAdapter;
    ArrayList topfList = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTopfDialog();
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });

        topfListView = (ListView) findViewById(R.id.topfListView);
        topfArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, topfList);
        topfListView.setAdapter(topfArrayAdapter);

        topfList.add("Jan");
        topfList.add("Haushalt");
        topfArrayAdapter.notifyDataSetChanged();

        topfListView.setOnItemClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d("topf Click", position + ": " + topfList.get(position));
        Intent i = new Intent(MainActivity.this, JournalActivity.class);
        i.putExtra("topf", topfList.get(position).toString());
        startActivity(i);
    }


    protected void addTopfDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        //alert.setTitle("Hello!");
        alert.setMessage("Add a new Topf:");

        // Create EditText for entry
        final EditText input = new EditText(this);
        alert.setView(input);

        // Make an "OK" button to save the Text to topfList
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                topfList.add(input.getText().toString());
                topfArrayAdapter.notifyDataSetChanged();
            }
        });

        // Make a "Cancel" button that simply dismisses the alert
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {}
        });

        alert.show();
    }
}