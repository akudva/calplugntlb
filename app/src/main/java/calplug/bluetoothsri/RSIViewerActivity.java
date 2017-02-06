package calplug.bluetoothsri;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import calplug.bluetoothsri.bluetoothUtility.ConnectionHandler;
import calplug.bluetoothsri.bluetoothUtility.BluetoothConnectionListener;
import calplug.bluetoothsri.com.MAVLink.MAVLinkPacket;
import calplug.bluetoothsri.com.MAVLink.Parser;
import calplug.bluetoothsri.com.MAVLink.common.*;
import calplug.bluetoothsri.heatMapUtility.ChartData;
import calplug.bluetoothsri.heatMapUtility.HeatMapDataConstructor;
import calplug.bluetoothsri.heatMapUtility.HeatMapHelper;

/**
 * RSIViewerActivity create SRIViewerCanvas, implements vector calculations
 * @author Zhihao
 *
 */
public class RSIViewerActivity extends ActionBarActivity {

    private Context context = null;
    private static Parser mavParser = new Parser();

    protected static int[] dataPoolArray; // the array to display
    protected static int[] dataArrayBefore, dataArrayAfter; // pre-surgery and post-surgery data

    protected static int method; // 0: tile, 1: mat
    protected static boolean acquirePostSurgeryData = false;
    protected static boolean hasPreSurgeryData = false;
    protected static boolean hasPostSurgeryData = false;
    protected static boolean hasDisplayedDiff = false;
    private static final String TAG = "RSIVIEWER";

    private final int column = 30;
    private final int row = 30;
    private final double dataRange = 90;
    private final int step = 10;
    private List<ChartData> samplePoints = new ArrayList();
    private int max_theta = 0;
    private int[] thetaList;

    private Mat mat;

    public enum Mode {
        EIGHT_MAGS,
        EIGHT_MAGS_WITH_ACC,
        NINE_MAGS,
        NINE_MAGS_WITH_ACC
    }

    public Mode mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Activity configuration: set as fullscreen and GL
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_gl_viewer);
        context = getApplicationContext();

        // initialize graph variables
        JSONArray limits = new JSONArray();

        // Initialize buttons
        final Button buttonBefore = (Button) findViewById(R.id.button_before);
        final Button buttonAfter = (Button) findViewById(R.id.button_after);
        final Button buttonCompare = (Button) findViewById(R.id.button_compare);
        final Button buttonClear = (Button) findViewById(R.id.button_clear);

        setButtonStyles(buttonAfter, buttonBefore, buttonCompare);
        setButtonClickEvents(buttonAfter, buttonBefore, buttonCompare, buttonClear);

        mat = new Mat();

        createHeatMap();

        // See if we got any MAVLink messages whenever bytes in Bluetooth buffer
        ConnectionHandler.getInstance().addBluetoothConnectionListener
                (new BluetoothConnectionListener() {
                     @Override
                     public void dataReceived(final byte[] data) {
                         runOnUiThread(new Runnable() {
                             @Override
                             public void run() {
                                 handleBluetooth(data);
                             }
                         });
                     }
                 }
                );

    }

    protected void createHeatMap()
    {
        Log.d("DACODA", "Creating heatmap");
        HeatMapHelper heatMap = (HeatMapHelper) findViewById(R.id.heat_map);
        heatMap.setVerbose(false);

        // Reset sample points
        samplePoints = new ArrayList();
        ArrayList<int[]> magnetometerData = mat.getMagnetometerData();

        // TODO: keep track of max theta value

        // TODO: Check to make sure the data is actually 3 members long
        for (int[] data : magnetometerData)
        {
            String rowString = String.format("R%d", data[0]);
            String colString = String.format("C%d", data[1]);
            samplePoints.add(new ChartData(rowString, colString, data[2]));
        }

        HeatMapDataConstructor mDataConstructor = new HeatMapDataConstructor(row, column, samplePoints);

        heatMap.setLimitsHelper(step, 90);
        heatMap.setColsRowsHelper(row, column);
        heatMap.setDataHelper(mDataConstructor);

        // Send a "2" to the Hub
        String acquire = "2";
        try { ConnectionHandler.getInstance().sendBytes(acquire.getBytes()); }
        catch (IOException e) { e.printStackTrace(); }
    }

    protected void handleBluetooth(final byte data[])
    {
        InputStream is = new ByteArrayInputStream(data);

        // Try and parse a MAVLink message out
        try
        {
            while ( is.available() > 0 )
            {
                MAVLinkPacket packet = mavParser.mavlink_parse_char(is.read());

                // If we got a MAVLink message...
                if ( packet != null )
                {
                    handleMAVLink(packet);
                }
            }
        }
        catch (IOException e) { e.printStackTrace(); }
    }

    protected void handleMAVLink(MAVLinkPacket packet)
    {
        // TODO: We should be checking if the message lines up with our current mode
        switch (packet.msgid)
        {
            case msg_ping.MAVLINK_MSG_ID_PING:
                return;

            case msg_measurement_mode_request.MAVLINK_MSG_ID_MEASUREMENT_MODE_REQUEST:
                return;

            // For any measurement message
            case msg_tile_measurements_eight.MAVLINK_MSG_ID_TILE_MEASUREMENTS_EIGHT:
            {
                msg_tile_measurements_eight message = new msg_tile_measurements_eight(packet);
                this.mat.updateTile(message.tile_number, message.mag_data);
                createHeatMap();
                break;
            }
            case msg_tile_measurements_eight_w_acc.MAVLINK_MSG_ID_TILE_MEASUREMENTS_EIGHT_W_ACC:
            {
                msg_tile_measurements_eight message = new msg_tile_measurements_eight(packet);
                this.mat.updateTile(message.tile_number, message.mag_data);
                createHeatMap();
                break;
            }
            case msg_tile_measurements_nine.MAVLINK_MSG_ID_TILE_MEASUREMENTS_NINE:
            {
                msg_tile_measurements_eight message = new msg_tile_measurements_eight(packet);
                this.mat.updateTile(message.tile_number, message.mag_data);
                createHeatMap();
                break;
            }
            case msg_tile_measurements_nine_w_acc.MAVLINK_MSG_ID_TILE_MEASUREMENTS_NINE_W_ACC:
            {
                msg_tile_measurements_eight message = new msg_tile_measurements_eight(packet);
                this.mat.updateTile(message.tile_number, message.mag_data);
                createHeatMap();
                break;
            }

            case msg_radio_status.MAVLINK_MSG_ID_RADIO_STATUS:
                return;

            default:
                return;
        }
    }

    /**
     * setButtonClickEvents initializes the functionality of buttons
     * @param buttonAfter
     * @param buttonBefore
     * @param buttonCompare
     * @param buttonClear
     */
    protected void setButtonClickEvents(final Button buttonAfter,
                                        final Button buttonBefore,
                                        final Button buttonCompare,
                                        final Button buttonClear) {
        buttonBefore.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Have to do clone to make sure that dataArrayBefore doesn't just use the same reference
                dataArrayBefore = dataPoolArray.clone();
                Toast.makeText(getApplicationContext(), "Saved baseline...",
                        Toast.LENGTH_LONG).show();
            }
        });

        buttonAfter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Reset the before data
                dataArrayBefore = new int[dataPoolArray.length];
            }
        });

        buttonCompare.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (hasPreSurgeryData && hasPostSurgeryData) {
                    // display difference between pre-surgery vectors and
                    // post-surgery vectors
                    //
//                    dataPoolArray = getVectorDifference(dataArrayBefore, dataArrayAfter);
//                    Intent glIntent = new Intent(context, RSIViewerActivity.class);
//                    glIntent.putExtra("dataPoolArray", dataPoolArray);
//                    glIntent.putExtra("method", 1);
//                    startActivity(glIntent);

                    hasDisplayedDiff = true;
                } else {
                    Toast.makeText(getApplicationContext(), "Please select post surgery data.",
                            Toast.LENGTH_LONG).show();
                }

            }
        });

        buttonClear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                hasPreSurgeryData = false;
                hasPostSurgeryData = false;
                acquirePostSurgeryData = false;
                hasDisplayedDiff = false;
                // acquire data
                //
                Intent mainIntent = new Intent(context, MainActivity.class);
                acquirePostSurgeryData = true;
                startActivity(mainIntent);
            }
        });
    }

    /**
     * setButtonStyles initializes the style and text of buttons
     * @param buttonAfter
     * @param buttonBefore
     * @param buttonCompare
     */
    protected void setButtonStyles(Button buttonAfter,
                                   Button buttonBefore,
                                   Button buttonCompare) {

        if (hasPreSurgeryData && hasPostSurgeryData) {
            buttonCompare.setEnabled(true);
        } else {
            buttonCompare.setEnabled(false);
        }

        if (hasDisplayedDiff) {
            buttonBefore.setText(getString(R.string.button_show_before));
            buttonAfter.setText(getString(R.string.button_show_after));
        }
    }

    /**
     * getVectorDifference shows the difference of vector arrays.
     * This function adds the abs value of diff on base color (green)
     * @param before vector array before surgery
     * @param after vector array after surgery
     * @return difference vector array
     */
    protected int[] getVectorDifference(int[] before, int[] after) {

        int len = before.length;
        int[] result = new int[len];
        for (int i = 0; i < len; i++)
        {
            result[i] = after[i] - before[i];
        }
        return result;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
