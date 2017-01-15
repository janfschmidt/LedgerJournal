/*
 * Copyright (c) 2017 Jan Felix Schmidt <janschmidt@mailbox.org>
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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(R.string.layout_settings_title);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
            // Set Export Path default
            EditTextPreference path = (EditTextPreference) findPreference("exportpath");
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            path.setText( sharedPref.getString("exportpath", defaultPath(getActivity())) );
        }
    }


    static public String defaultPath(Context context) {
        String doc;
        if (android.os.Build.VERSION.SDK_INT >= 19)
            doc = Environment.DIRECTORY_DOCUMENTS;
        else
            doc = "Documents";
        return Environment.getExternalStoragePublicDirectory(doc) + "/" + context.getResources().getString(R.string.app_name);
    }


}