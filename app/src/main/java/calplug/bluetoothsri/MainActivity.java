package calplug.bluetoothsri;

import android.bluetooth.BluetoothAdapter;
import calplug.bluetoothsri.bluetoothUtility.ConnectionHandler;
import calplug.bluetoothsri.Utility.AlternateFunctionListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import calplug.bluetoothsri.com.MAVLink.Parser;

import java.io.IOException;

import android.app.AlertDialog;

import calplug.bluetoothsri.bluetoothUtility.BluetoothConnectionListener;
import calplug.bluetoothsri.bluetoothUtility.ConnectionStateChangedListener;

/**
 * Main activity reads input from bluetooth or direct input. Output
 * a data structure to construct graph.
 * @author Zhihao
 *
 */
public class MainActivity extends ActionBarActivity {

    private Context context = null;
    private EditText terminalRx = null;

    protected AlertDialog.Builder connectPairDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        setupConnectPairDialog();

        // Initialize buttons
        //
        final Button tileButton = (Button) findViewById(R.id.submit);
        final Button connectButton = (Button) findViewById(R.id.connect_button);

        // Initialize Rx textEditor
        //
        terminalRx = (EditText) findViewById(R.id.editText);
        terminalRx.setMovementMethod(new ScrollingMovementMethod());

        // Set button click events
        //
        setButtonEvents(tileButton, connectButton);

        // set Bluetooth ConnectionHandler
        //
        setBluetoothConnectionHandler(connectButton);
    }

    protected void setupConnectPairDialog()
    {
        CharSequence options[] = new CharSequence[] {"Connect", "Pair"};

        this.connectPairDialog = new AlertDialog.Builder(MainActivity.this);
        this.connectPairDialog.setTitle("Connect or pair:");

        // Listener for dialog click
        DialogInterface.OnClickListener CPDonClickListener =
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        switch (which)
                        {
                            case 0:
                                // Launch connect activity
                                Log.d("DACODA", "Launching connect activity!");
                                Intent connectIntent = new Intent(MainActivity.this, ConnectActivity.class);
                                startActivity(connectIntent);
                                break;
                            case 1:
                                // Launch pair activity
                                Log.d("DACODA", "Launching pair activity!");
                                Intent pairIntent = new Intent(MainActivity.this, PairActivity.class);
                                startActivity(pairIntent);
                                break;
                        }
                    }
                };

        connectPairDialog.setItems(options, CPDonClickListener);
    }

    /**
     * set Bluetooth ConnectionHandler
     * @param connectButton
     */
    private void setBluetoothConnectionHandler( final Button connectButton) {
        // Bluetooth read
        ConnectionHandler.getInstance().addBluetoothConnectionListener
                (new BluetoothConnectionListener() {
                     @Override
                     public void dataReceived(final byte[] data) {
                         runOnUiThread(new Runnable() {
                             @Override
                             public void run() {

                             }
                         });
                     }
                 }
                );

        ConnectionHandler.getInstance().addConnectionStateChangedListener(new ConnectionStateChangedListener() {
            // Set Buttons to have two states
            @Override
            public void stateChanged() {
                boolean isConnected = ConnectionHandler.getInstance().isConnected();
                connectButton.setText((isConnected) ? "Disconnect" : "Connect");
                terminalRx.setText("");
            }
        });
    }

    /**
     * Set button click events
     * @param tileButton
     * @param connectButton
     */
    private void setButtonEvents(Button tileButton,
                                 Button connectButton) {
        tileButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // draw tile button
                //
                int[] dataPoolArray = textDataParsing();
                if (dataPoolArray[0] == -1) {
                    // string parsing error, caused by wrong input format
                    //
                    return;
                }
                // Intent glIntent = new Intent(context, RSIViewerActivity.class);
                Intent glIntent = new Intent(context, RSIViewerActivity.class);
                glIntent.putExtra("dataPoolArray", dataPoolArray);
                glIntent.putExtra("method", 0);
                terminalRx.setText(""); // clear Rx text
                startActivity(glIntent);
            }
        });

        connectButton.setOnClickListener(new AlternateFunctionListener() {
            @Override
            public void OnClickSecondary() {
                try {
                    ConnectionHandler.getInstance().disconnect();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Interface Error Recovered",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void OnClickPrimary() {
                // show connect activity
                //
                connectPairDialog.show();
//                Intent connectIntent = new Intent(MainActivity.this, ConnectActivity.class);
//                startActivity(connectIntent);
            }
        });

    }

    /** @author Sahil
     * This Methods is direct from source code ABT Terminal
     * Method prompts user to enable bluetooth adapter if available.
     */
    private void initBluetooth() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Toast.makeText(getApplicationContext(), "No bluetooth adapter available!", Toast.LENGTH_LONG).show();
            return;
        }
        while (!adapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }
    }

    /**
     * textDataParsing parses input data by comma and semicolon
     * @return int array, set the first int to -1 if encounter error
     */
    private int[] textDataParsing(){

//        // terminate parsing if input is empty
//        //
//        if (terminalRx.getText().toString().length() == 0) {
//            Toast.makeText(context, noEnoughData, toastShort).show();
//            return new int[] {-1};
//        }
//        // parse mat by flag ::
//        //
//        String[] lines = parseLine(terminalRx.getText().toString(), "::");
//        String[] segmentsInLine;
//        String[] digitsInSegment;
//        Stack<Integer> dataPool = new Stack<>();
//
//        try {
//            for (String line: lines) {
//                // parse tile by flag ;
//                segmentsInLine = parseLine(line, ";");
//                for (String segment: segmentsInLine){
//                    // parse sensor by ,
//                    digitsInSegment = parseLine(segment, ",");
//                    for (String digit: digitsInSegment){
//                        dataPool.push(Integer.parseInt(digit));
//                    }
//                }
//            }
//        } catch (Exception e) {
//            Toast.makeText(context, wrongDataFormat, toastShort).show();
//            return new int[] {-1};
//        }
//
//
//        int[] dataPoolArray = new int[dataPool.toArray().length];
//        Integer length = dataPool.toArray().length - 1;
//        Integer esi = 0;
//        while (!dataPool.empty()) {
//            dataPoolArray[length - esi]=dataPool.pop();
//            esi++;
//        }
//        return dataPoolArray;

        return new int[] {1};
    }

    /**
     * parse string lines
     * @param string
     * @param delimiter
     * @return separated data segments as an array
     */
    private String[] parseLine(String string, String delimiter){

        String[] segment = string.split(delimiter);
        return segment;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        initBluetooth();
    }
}
