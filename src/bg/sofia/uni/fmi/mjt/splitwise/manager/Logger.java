package bg.sofia.uni.fmi.mjt.splitwise.manager;

import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.io.File;
public class Logger {
    public static void saveStackTrace(IOException e, String pathname) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();
        File fileException = new File(pathname);
        try (Writer writer = new FileWriter(pathname, true)) {
            fileException.createNewFile();
            writer.write(exceptionAsString);
        } catch (IOException ee) {
            System.out.println("");
        }
    }
}
