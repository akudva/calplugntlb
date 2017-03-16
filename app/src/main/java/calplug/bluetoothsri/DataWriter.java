package calplug.bluetoothsri;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class DataWriter
{
    public void writeToFile(String data, Context context) {
        try {

            File path = context.getExternalFilesDir(null);
            File file = new File(path, "session-record.csv");

            FileOutputStream stream = new FileOutputStream(file, true);

            stream.write(data.getBytes());
            stream.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

}
