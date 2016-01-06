package de.jan.ledgerjournal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by jan on 06.01.16. From http://www.programmierenlernenhq.de/sqlite-datenbank-in-android-app-integrieren/
 */
public class JournalDataSource {
    private SQLiteDatabase db;
    private JournalDbHelper dbHelper;

    public JournalDataSource(Context context) {
        dbHelper = new JournalDbHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
        Log.d("JournalDataSource", "Datenbank-Referenz erhalten. Pfad: " + db.getPath());
    }

    public void close() {
        dbHelper.close();
        Log.d("JournalDataSource", "Datenbank geschlossen.");
    }
}
