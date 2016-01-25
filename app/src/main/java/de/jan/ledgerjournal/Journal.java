package de.jan.ledgerjournal;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by jan on 25.01.2016.
 */
public class Journal {
    public ArrayList<Transaction> list = new ArrayList<>();

    public void add(Transaction t) {list.add(t);}
    public void add(ArrayList<Transaction> tl) {list.addAll(tl);}

    public void set(ArrayList<Transaction> tl) {list.clear(); add(tl);}


    // export journal to text file
    public void export(String filename) {
        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
                File root = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LedgerJournal"); // anderes Verzeichnis?
                if (!root.exists()) {
                    root.mkdirs();
                }
            try {
                File file = new File(root.getAbsolutePath(), filename);
                FileWriter writer = new FileWriter(file);
                writer.append("blabla\njaja so ist das, \t ok?"); // hier sinnvollen Inhalt einfügen!
                writer.flush();
                writer.close();
                Log.d("Journal.export", "Wrote file " + root + "/" + filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
