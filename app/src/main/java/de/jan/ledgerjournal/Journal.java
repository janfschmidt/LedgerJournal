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

import android.os.Environment;
import android.util.Log;

import java.io.File;
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

    Calendar c = Calendar.getInstance();
    SimpleDateFormat dateComment = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    SimpleDateFormat dateFilename = new SimpleDateFormat("yyyyMMdd-HHmmss");

    public Journal() {setName("unknown");}
    public Journal(String name) {setName(name);}

    public int size() {return list.size();}
    public void setName(String name) {this.name = name;}

    public void add(Transaction t) {list.add(t);}
    public void add(ArrayList<Transaction> tl) {list.addAll(tl);}
    public void remove(int index) {list.remove(index);}
    public void clear() {list.clear();}
    public Transaction get(int index) {return list.get(index);}
    public void set(ArrayList<Transaction> tl) {list.clear(); add(tl);}

    public String exportFilename() {return name + "_" + dateFilename.format(c.getTime()) + ".led";}
    public String exportFilePath(String exportDir) {return exportDir + "/" + exportFilename();}


    // export journal to text file
    public void export(String exportDir, int width) {
        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
                File dir = new File( exportDir );
                if (!dir.exists()) {
                    dir.mkdirs();
                }
            try {
                FileWriter writer = new FileWriter( new File(dir, exportFilename()) );

                writer.append("# LedgerJournal export at ").append(dateComment.format(c.getTime())).append("\n");

                for (Transaction t : list) {
                    writer.append(t.print(width));
                }

                writer.flush();
                writer.close();
                Log.d("Journal.export", "Wrote file " + exportFilePath(exportDir));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void export(String exportDir) {export(exportDir, 35);}

}
