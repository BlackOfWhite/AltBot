package org.preferences.managers;

import org.logic.transactions.model.CancelOption;

import java.io.*;
import java.util.ArrayList;

public class PersistenceManager {

    public static void saveCancelOptionCollection(ArrayList<CancelOption> cancelOptions) {
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;
        try {
            fout = new FileOutputStream("cancelOptions.ser");
            oos = new ObjectOutputStream(fout);
            oos.writeObject(cancelOptions);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fout.close();
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<CancelOption> loadCancelOptionCollection() {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        ArrayList<CancelOption> arrayList = new ArrayList<>();
        try {
            fis = new FileInputStream("cancelOptions.ser");
            ois = new ObjectInputStream(fis);
            try {
                arrayList = (ArrayList<CancelOption>) ois.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
                ois.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return arrayList;
    }

}
