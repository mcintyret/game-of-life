package com.mcintyre.gameoflife.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class FileHandling {
    public static final String rootFilePath = "./files/"; // /Users/TomsMacbook/Documents/workspace/Finance/";

    public static List<String> readFile(String fileName) {
        fileName = fileName.replace(":", "-");
        String line;
        List<String> data = new ArrayList<String>();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(fileName));
            try {
                while ((line = br.readLine()) != null) {
                    data.add(line);
                }
            } finally {
                br.close();
            }
        } catch (FileNotFoundException fN) {
            System.out.println("Claiming file " + fileName + " doesn't exist");
            fN.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return data;
    }

    public static String readFileSingleString(String fileName) {
        fileName = fileName.replace(":", "-");
        File f = new File(fileName);
        if (f.exists()) {
            String line = "";
            StringBuilder bigString = new StringBuilder();

            try {
                FileReader fr = new FileReader(fileName);
                BufferedReader br = new BufferedReader(fr);
                while ((line = br.readLine()) != null) {
                    // System.out.println(line);
                    bigString.append(line);
                }
            } catch (FileNotFoundException fN) {
                fN.printStackTrace();
            } catch (IOException e) {
                System.out.println(e);
            }
            return bigString.toString();
        } else {
            System.out.println("Claiming file " + fileName + " doesn't exist");
            return null;
        }
    }

    public static void writeFile(String fileName, String text, boolean append) {
        try {
            File file = new File(fileName);

            if (!file.exists()) {
                file.createNewFile();
            }

            BufferedWriter out = new BufferedWriter(new FileWriter(file, append));
            out.write(text);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeFile(String fileName, List<String> text, boolean append) {
        try {
            File file = new File(fileName);

            if (!file.exists()) {
                file.createNewFile();
            }

            BufferedWriter out = new BufferedWriter(new FileWriter(file, append));
            for (String line : text) {
                out.write(line + "\n");
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean exists(String fileName) {
        File f = new File(fileName);
        return f.exists();
    }

    public static void rename(String oldname, String newname) {
        File f = new File(oldname);
        if (f.exists()) {
            f.renameTo(new File(newname));
        } else {
            System.out.println("No file named " + oldname + " exists. Check it out...");
        }
    }

    public static void Serialize(Object o, String filename) {
        System.out.println("Serialising to " + filename);

        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try {
            fos = new FileOutputStream(filename);
            out = new ObjectOutputStream(fos);
            out.writeObject(o);
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static Object Deserialize(String filename) {
        Object o = new Object();

        System.out.println("Deserialising data from " + filename);

        FileInputStream fis = null;
        ObjectInputStream in = null;
        try {
            fis = new FileInputStream(filename);
            in = new ObjectInputStream(fis);
            o = in.readObject();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return o;
    }

    public static void delete(String filename) {
        if (filename == null) return;
        File f = new File(filename);
        f.delete();
    }

}
