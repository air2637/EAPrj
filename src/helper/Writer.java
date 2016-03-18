package helper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by khoi on 3/2/2016.
 */
public class Writer{
    public static void main(String[] args) {
        Writer w = new Writer("out.csv");
        try {
            w.println("Taxi,8188,359.01661273,50.0");
            w.println("Trans,8203,75.3942738202,50.0");
        }
        finally{
            //always close before end
            w.close();
        }
    }

    private PrintWriter w;
    public Writer(String filePathAndName){
        try {
            w = new PrintWriter(
                    new BufferedWriter(
                            new FileWriter(filePathAndName, false)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void print(String s){
        w.print(s);
    }

    public void println(String s){
        w.println(s);
    }

    public void close(){
        w.close();
    }
}

