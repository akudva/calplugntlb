package calplug.bluetoothsri;

import android.bluetooth.BluetoothAdapter;
import calplug.bluetoothsri.bluetoothUtility.ConnectionHandler;
import calplug.bluetoothsri.Utility.AlternateFunctionListener;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import calplug.bluetoothsri.com.MAVLink.MAVLinkPacket;
import calplug.bluetoothsri.com.MAVLink.Messages.MAVLinkMessage;
import calplug.bluetoothsri.com.MAVLink.common.msg_tile_measurements_eight;
import calplug.bluetoothsri.com.MAVLink.common.*;
import calplug.bluetoothsri.com.MAVLink.Parser;

import java.io.IOException;
import java.util.Stack;
import java.io.InputStream;
import java.io.ByteArrayInputStream;


import calplug.bluetoothsri.bluetoothUtility.BluetoothConnectionListener;
import calplug.bluetoothsri.bluetoothUtility.ConnectionStateChangedListener;

/**
 * Main activity reads input from bluetooth or direct input. Output
 * a data structure to construct graph.
 * @author Zhihao
 *
 */
public class MainActivity extends ActionBarActivity {

    private int toastShort = Toast.LENGTH_SHORT;
    private CharSequence noEnoughData = "No enough data to process.";
    private CharSequence wrongDataFormat = "Wrong data format.";

    public static Parser mavParser = new Parser();


    private Context context = null;
    private EditText terminalRx = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        // Initialize buttons
        //
        final Button tileButton = (Button) findViewById(R.id.submit);
        final Button matButton = (Button) findViewById(R.id.matSubmit);
        final Button connectButton = (Button) findViewById(R.id.connect_button);
        final Button pairButton = (Button) findViewById(R.id.pair_button);

        // Initialize Rx textEditor
        //
        terminalRx = (EditText) findViewById(R.id.editText);
        terminalRx.setMovementMethod(new ScrollingMovementMethod());

        // Set button click events
        //
        setButtonEvents(tileButton, matButton, connectButton, pairButton);

        // set Bluetooth ConnectionHandler
        //
        setBluetoothConnectionHandler(pairButton, connectButton);
    }

    /**
     * set Bluetooth ConnectionHandler
     * @param pairButton
     * @param connectButton
     */
    private void setBluetoothConnectionHandler(final Button pairButton,
                                               final Button connectButton) {
        // Bluetooth read
        ConnectionHandler.getInstance().addBluetoothConnectionListener
                (new BluetoothConnectionListener() {
                     @Override
                     public void dataReceived(final byte[] data) {
                         runOnUiThread(new Runnable() {
                             @Override
                             public void run() {
                                 //recieve incoming data and append to TerminalRX string

                                 // Print out hex of the incoming data
//                                 for (int i = 0; i < data.length; ++i) {
//                                     String terminal_string = String.format("%02x ", data[i]);
//                                     terminalRx.append(terminal_string);
//                                 }

                                 // TODO: Figure out why we have to convert this to a ByteArrayInputStream
                                 InputStream is = new ByteArrayInputStream(data);
                                 try {
                                     while(is.available() > 0) {
                                         MAVLinkPacket packet = mavParser.mavlink_parse_char(is.read());
                                         if(packet != null){
                                             terminalRx.append(String.format("msgid: %d", packet.msgid));
                                             msg_tile_measurements_eight rcvd_msg = new msg_tile_measurements_eight(packet);
                                             for (int i = 0; i < rcvd_msg.mag_data.length; ++i)
                                             {
                                                 terminalRx.append(String.format("%.2f", rcvd_msg.mag_data[i]));
                                                 terminalRx.append(", ");
                                             }
                                             terminalRx.append("");
                                             terminalRx.append(rcvd_msg.toString());
                                         }
                                     }
                                     System.out.println("End tlog");
                                 } catch (IOException e) {
                                     e.printStackTrace();
                                 }


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
                pairButton.setText((isConnected) ? "Send" : "Pair");
                connectButton.setText((isConnected) ? "Disconnect" : "Connect");
                terminalRx.setText("");
            }
        });
    }

    /**
     * Set button click events
     * @param tileButton
     * @param matButton
     * @param connectButton
     * @param pairButton
     */
    private void setButtonEvents(Button tileButton,
                                 Button matButton,
                                 Button connectButton,
                                 Button pairButton) {
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
                Intent glIntent = new Intent(context, RSIViewerActivity.class);
                glIntent.putExtra("dataPoolArray", dataPoolArray);
                glIntent.putExtra("method", 0);
                terminalRx.setText(""); // clear Rx text
                startActivity(glIntent);
            }
        });

        matButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // draw mat button
                //
                int [] dataPoolArray = textDataParsing();
                if (dataPoolArray[0] == -1) {
                    // string parsing error, caused by wrong input format
                    //
                    return;
                }
                Intent glIntent = new Intent(context, RSIViewerActivity.class);
                glIntent.putExtra("dataPoolArray", dataPoolArray);
                glIntent.putExtra("method", 1);
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
                Intent connectIntent = new Intent(MainActivity.this, ConnectActivity.class);
                startActivity(connectIntent);
            }
        });

        pairButton.setOnClickListener(new AlternateFunctionListener() {
            @Override
            public void OnClickSecondary () {
                // send acquire signal to partner
                //
                String acquire = "HELLO*";
                try {
                    ConnectionHandler.getInstance().sendBytes(acquire.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                terminalRx.setText("");
            }

            @Override
            public void OnClickPrimary() {
                // show pair activity
                //
                Intent pairIntent = new Intent(MainActivity.this, PairActivity.class);
                startActivity(pairIntent);
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

        // terminate parsing if input is empty
        //
        if (terminalRx.getText().toString().length() == 0) {
            Toast.makeText(context, noEnoughData, toastShort).show();
            return new int[] {-1};
        }
        // parse mat by flag ::
        //
        String[] lines = parseLine(terminalRx.getText().toString(), "::");
        String[] segmentsInLine;
        String[] digitsInSegment;
        Stack<Integer> dataPool = new Stack<>();

        try {
            for (String line: lines) {
                // parse tile by flag ;
                segmentsInLine = parseLine(line, ";");
                for (String segment: segmentsInLine){
                    // parse sensor by ,
                    digitsInSegment = parseLine(segment, ",");
                    for (String digit: digitsInSegment){
                        dataPool.push(Integer.parseInt(digit));
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(context, wrongDataFormat, toastShort).show();
            return new int[] {-1};
        }


        int[] dataPoolArray = new int[dataPool.toArray().length];
        Integer length = dataPool.toArray().length - 1;
        Integer esi = 0;
        while (!dataPool.empty()) {
            dataPoolArray[length - esi]=dataPool.pop();
            esi++;
        }
        return dataPoolArray;
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
