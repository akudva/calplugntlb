package calplug.bluetoothsri;

import android.content.Context;
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

public class RSIViewerActivity extends ActionBarActivity {

    private Context context = null;

    private static final String TAG = "RSIVIEWER";

    // Heatmap variables
    private final int column = 60;
    private final int row = 60;
    private final double dataRange = 90;
    private final int step = 10;
    private List<ChartData> samplePoints = new ArrayList();

    // Mat and tile variables
    private Mat mat;
    private final int numberTiles = 4;
    private final int[][] tileLocations = {{0, 0}, {1, 0}, {1, 0}, {1, 1}};

    // MAVLink messages parser
    private static Parser mavParser = new Parser();

    // Lists of our data
    ArrayList<int[]> mostRecentData;
    ArrayList<int[]> baselineData;
    ArrayList<int[]> differencedData;

    // Some configuration data
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
        final Button buttonBaseline = (Button) findViewById(R.id.button_baseline);
        final Button buttonDataRequest = (Button) findViewById(R.id.button_data_request);

        setButtonStyles(buttonBaseline, buttonDataRequest);
        setButtonClickEvents(buttonBaseline, buttonDataRequest);

        mat = new Mat(numberTiles, tileLocations, ConfigurationDetails.tileModes.EIGHT_MAGNETOMETERS_SANS_ACCELEROMETER);

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
        Log.d("HEATMAP", "Creating heatmap");
        HeatMapHelper heatMap = (HeatMapHelper) findViewById(R.id.heat_map);
        heatMap.setVerbose(false);

        // Reset sample points
        mostRecentData = mat.getMagnetometerData();

        // TODO: keep track of max theta value


        // TODO: Check to make sure the data is actually 3 members long
        // Plot data in heatmap to scale
        //
        // NOTE: Here not only do we SCALE down the dimensions, we also have
        // to flip the ROW (or y-coordinate) as this heatmap has the origin
        // in the upper left and not the lower left
        float xScale = row / (float) mat.xDimension;
        float yScale = column / (float) mat.yDimension;

         Log.d("HEATMAP", String.format("Scale is (%d / %d = ) %.3f x (%d / %d = ) %.3f", row, mat.xDimension, xScale, column, mat.yDimension, yScale));

        // Reset samplePoints
        samplePoints = new ArrayList();
        for (int[] data : mostRecentData)
        {
            int magnetometerColumn = (int) (data[0] * xScale);
            int magnetometerRow = row - (int) (data[1] * yScale);
            String rowString = String.format("R%d", magnetometerRow);
            String colString = String.format("C%d", magnetometerColumn);
            samplePoints.add(new ChartData(rowString, colString, data[2]));
            Log.d("HEATMAP", String.format("Adding value %2d to row (%3d x %.3f = ) %2d and column (%3d x %.3f = ) %2d", data[2],
                    data[0], xScale, magnetometerColumn,
                    data[1], yScale, magnetometerRow));
        }

        HeatMapDataConstructor mDataConstructor = new HeatMapDataConstructor(row, column, samplePoints);

        // Let's go from 0 to 90 in steps of 10
        heatMap.setLimitsHelper(step, dataRange);
        heatMap.setColsRowsHelper(row, column);
        heatMap.setDataHelper(mDataConstructor);

        // TODO: If a UI button is triggered, don't send a message

        // Request another message from the hub
        requestData();
    }

    // Request data from the Hub
    protected void requestData()
    {
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

                if (message.tile_number == numberTiles-1)
                {
                    createHeatMap();
                }
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

    protected void setButtonClickEvents(final Button buttonBaseline,
                                        final Button buttonDataRequest) {
        buttonBaseline.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Have to do clone to make sure that dataArrayBefore doesn't just use the same reference
                Toast.makeText(getApplicationContext(), "Saved baseline...",
                        Toast.LENGTH_LONG).show();
            }
        });

        buttonDataRequest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                requestData();
            }
        });

    }

    protected void setButtonStyles(Button buttonBaseline,
                                   Button buttonDataRequest) {

        // Setting enabled/disabled
        // buttonCompare.setEnabled(true);

        // Changing button text
        // buttonBefore.setText(getString(R.string.button_show_before));
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
