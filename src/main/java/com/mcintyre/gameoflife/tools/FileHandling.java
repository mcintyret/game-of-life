package com.mcintyre.gameoflife.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileHandling {
    public static final String ROOT_FILE_PATH = "./";

    public static boolean exists(String fileName) {
        File f = new File(fileName);
        return f.exists();
    }

    public static void serialize(Object o, String filename) {
        System.out.println("Serialising to " + filename);

        try (FileOutputStream fos = new FileOutputStream(filename);
             ObjectOutputStream out = new ObjectOutputStream(fos)) {
            out.writeObject(o);
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static Object deserialize(String filename) {

        System.out.println("Deserialising data from " + filename);

        try (FileInputStream fis = new FileInputStream(filename);
             ObjectInputStream in = new ObjectInputStream(fis)) {
            return in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}
