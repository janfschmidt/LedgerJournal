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