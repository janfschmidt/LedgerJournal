package de.jan.ledgerjournal;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by jan on 25.01.2016.
 */
public class Journal {
    public ArrayList<Transaction> list = new ArrayList<>();
    protected String name;

    public Journal() {setName("unknown");}
    public Journal(String name) {setName(name);}

    public void setName(String name) {this.name = name;}

    public void add(Transaction t) {list.add(t);}
    public void add(ArrayList<Transaction> tl) {list.addAll(tl);}
    public void remove(int index) {list.remove(index);}
    public Transaction get(int index) {return list.get(index);}

    public void set(ArrayList<Transaction> tl) {list.clear(); add(tl);}

    public String exportDir() {return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/LedgerJournal";}
    public String exportFilename() {return name + ".led";}
    public String exportFilePath() {return exportDir() + "/" + exportFilename();}


    // export journal to text file
    public void export() {
        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
                File dir = new File( exportDir() );
                if (!dir.exists()) {
                    dir.mkdirs();
                }
            try {
                FileWriter writer = new FileWriter( new File(dir, exportFilename()) );

                Calendar c = Calendar.getInstance();
                SimpleDateFormat dateFormater = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                writer.append("# LedgerJournal export at " + dateFormater.format(c.getTime()) + "\n");

                for (Transaction t : list) {
                    writer.append(t.print());
                }

                writer.flush();
                writer.close();
                Log.d("Journal.export", "Wrote file " + exportFilePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
