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
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
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
    ArrayList<String> topfList = new ArrayList<>();

    JournalDataSource dataSource;

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
            }
        });

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        dataSource = new JournalDataSource(this);

        topfListView = (ListView) findViewById(R.id.topfListView);
        topfArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, topfList);
        topfListView.setAdapter(topfArrayAdapter);

        topfListView.setOnItemClickListener(this);
        registerForContextMenu(topfListView);
    }

    protected void onResume() {
        super.onResume();
        dataSource.open();
        showAllToepfe();
    }

    protected void onStop() {
        super.onStop();
        dataSource.close();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(i);
            return true;
        }
        else if (id == R.id.action_templates) {
            Intent i = new Intent(MainActivity.this, JournalActivity.class);
            i.putExtra("topfId", JournalDbHelper.TEMPLATE_TOPFID);
            startActivity(i);
        }
        else if (id == R.id.action_about) {
            aboutDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent i = new Intent(MainActivity.this, JournalActivity.class);
        String topfname = topfList.get(position);
        i.putExtra("topfId", dataSource.getTopfId(topfname));
        startActivity(i);
    }



    // List item context menu
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_main, menu);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle(topfList.get(info.position));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        if (item.getItemId() == R.id.context_main_rename) {
            Log.d("MainActivity", "Context menu: rename selected");
            editTopfDialog(info.position);
        }
        else if (item.getItemId() == R.id.context_main_delete) {
            Log.d("MainActivity", "Context menu: delete selected");
            deleteDialog(info.position);
        }
        else {
            return false;
        }
        return true;
    }


    protected void addTopfDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.dialog_addjournal);
        final EditText input = new EditText(this);
        alert.setView(input);

        // Make an "OK" button to save the Text to topfList
        alert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String topfname = input.getText().toString();
                dataSource.addTopf(topfname);
                topfList.add(topfname);
                topfArrayAdapter.notifyDataSetChanged();
            }
        });

        // Make a "Cancel" button that simply dismisses the alert
        alert.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
    }

    protected void editTopfDialog(int index) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final String oldName =  topfList.get(index);
        alert.setTitle( getResources().getString(R.string.dialog_rename) +" "+ oldName );
        final EditText input = new EditText(this);
        input.setText(oldName);
        alert.setView(input);

        // Make an "OK" button to save the Text to topfList
        alert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newName = input.getText().toString();
                dataSource.editTopf(oldName, newName);
                showAllToepfe();
            }
        });

        // Make a "Cancel" button that simply dismisses the alert
        alert.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
    }

    protected void deleteDialog(final int index) {
        final String topfname = topfList.get(index);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(topfname);
        alert.setMessage(R.string.dialog_deletejournal);
        alert.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                deleteTopf(index);
            }
        });
        alert.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    protected void aboutDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.app_name);
        try {
            PackageInfo p = getPackageManager().getPackageInfo(getPackageName(),0);
            alert.setMessage("Version "+ p.versionName + " (Build "+p.versionCode+")\nLicense: GPLv3");
        } catch (PackageManager.NameNotFoundException e) {
            //should never be called?
        }
        alert.show();
    }



    private void showAllToepfe() {
        ArrayList<String> toepfe = dataSource.getAllToepfe();
        topfList.clear();
        topfList.addAll(toepfe);
        topfArrayAdapter.notifyDataSetChanged();
    }

    private void deleteTopf(int index) {
        //delete transactions from journal db
        int topfid = dataSource.getTopfId( topfList.get(index) );
        JournalDataSource journalDb = new JournalDataSource(this);
        journalDb.open();
        journalDb.deleteTopf(topfid);
        journalDb.close();

        //delete topf from topf db
        dataSource.deleteTopf(topfid);
        topfList.remove(index);
        topfArrayAdapter.notifyDataSetChanged();
    }
}