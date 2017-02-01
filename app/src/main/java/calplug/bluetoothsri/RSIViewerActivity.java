package calplug.bluetoothsri;

import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import calplug.bluetoothsri.heatMapUtility.ChartData;
import calplug.bluetoothsri.heatMapUtility.HeatMap;
import calplug.bluetoothsri.heatMapUtility.HeatMapDataConstructor;
import calplug.bluetoothsri.heatMapUtility.HeatMapHelper;
import calplug.bluetoothsri.mathUtility.vectorMath;

/**
 * RSIViewerActivity create SRIViewerCanvas, implements vector calculations
 * @author Zhihao
 *
 */
public class RSIViewerActivity extends ActionBarActivity {

    private Context context = null;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // config the activity interface
        //
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_gl_viewer);
        context = getApplicationContext();

        // initialize graph variables
        //
        HeatMapHelper heatMap = (HeatMapHelper) findViewById(R.id.heat_map);
        JSONArray limits = new JSONArray();

        // Initialize buttons
        //
        final Button buttonBefore = (Button) findViewById(R.id.button_before);
        final Button buttonAfter = (Button) findViewById(R.id.button_after);
        final Button buttonCompare = (Button) findViewById(R.id.button_compare);
        final Button buttonClear = (Button) findViewById(R.id.button_clear);

        setButtonStyles(buttonAfter, buttonBefore, buttonCompare);
        setButtonClickEvents(buttonAfter, buttonBefore, buttonCompare, buttonClear);

        // get data pool from main activity
        //
        Log.d("RSIVIEWER", "Printing out the DataPool stuff!");
        getDataPool();
        System.out.println(Arrays.toString(dataPoolArray)); // print dataPoolArray for debug
        int thetaList[];
        thetaList = vectorMath.getThetaList(dataPoolArray);
        System.out.println(Arrays.toString(thetaList)); // print theta list for debug

        // initialize sample matrix
        //
        heatMap.setVerbose(false);
        List<ChartData> samplePoints = new ArrayList();

//        int[][] magnetometer_locations = new int[][]
//                      { { 1,  1},
//                        { 1, 20},
//                        {20,  1},
//                        {20, 20} };

        // Locations of magnetometers for a 30x30 grid
        int[][] magnetometer_locations = new int[][]
                      { { 2, 28},
                        {28, 28},
                        {28,  2},
                        { 2,  2},
                        { 8, 22},
                        {22, 22},
                        {22,  8},
                        { 8,  8} };

        // Keep track of the highest theta so that we can pass it to limitsHelper later
        int max_theta= 0;
        for (int i = 0; i < magnetometer_locations.length; ++i)
        {
            // Some of the magnetometers are reading very high values all the time
            // e.g. 2035,1151,2043. If we get these, ignore that magnetometer...
            if (dataPoolArray[i*3] > 1900)
            {
                String log_string = String.format("Magnetometer reading %d, %d, %d (mag. #%d) looks suspect... Skipping...",
                        dataPoolArray[i*3],
                        dataPoolArray[i*3+1],
                        dataPoolArray[i*3+2],
                        i+1);
                Log.d("RSIVIEWER", log_string);
                continue;
            }

            if (thetaList[i] > max_theta)
                max_theta = thetaList[i];

            String row = String.format("R%d", magnetometer_locations[i][1]);
            String col = String.format("C%d", magnetometer_locations[i][0]);
            samplePoints.add(new ChartData(row, col, thetaList[i]));
            String log_info = String.format("Adding %d to position %s %s!", thetaList[i], row, col);
            Log.d("RSIVIEWER", log_info);
        }

        HeatMapDataConstructor mDataConstructor =
                new HeatMapDataConstructor(row,
                        column,
                        samplePoints);

        heatMap.setLimitsHelper(step, max_theta);
        heatMap.setColsRowsHelper(row, column);
        heatMap.setDataHelper(mDataConstructor);
    }

    /**
     * getDataPool retrieves data pool from main activity
     */
    protected void getDataPool() {

        Bundle bundle = getIntent().getExtras();
        dataPoolArray = bundle.getIntArray("dataPoolArray");
        method = bundle.getInt("method");
        //System.out.println(Arrays.toString(dataPoolArray));
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
                if (hasDisplayedDiff) {
                    // display pre-surgery data
                    //
                    Intent glIntent = new Intent(context, RSIViewerActivity.class);
                    glIntent.putExtra("dataPoolArray", dataArrayBefore);
                    glIntent.putExtra("method", 1);
                    startActivity(glIntent);
                } else {
                    // record vectors before surgery
                    //
                    dataArrayBefore = dataPoolArray;
                    Toast.makeText(getApplicationContext(), "Data saved.",
                            Toast.LENGTH_LONG).show();

                    acquirePostSurgeryData = false; //reset post-surgery flag to false
                    hasPostSurgeryData = false;
                    hasDisplayedDiff = false;
                    hasPreSurgeryData = true;

                    buttonAfter.setText(getString(R.string.button_get_after));
                    buttonAfter.setEnabled(true);
                    buttonCompare.setEnabled(false);
                }
            }
        });

        buttonAfter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (hasDisplayedDiff) {
                    // display post-surgery data
                    //
                    Intent glIntent = new Intent(context, RSIViewerActivity.class);
                    glIntent.putExtra("dataPoolArray", dataArrayAfter);
                    glIntent.putExtra("method", 1);
                    startActivity(glIntent);
                } else if (hasPreSurgeryData && !acquirePostSurgeryData) {
                    // acquire data
                    //
                    Intent mainIntent = new Intent(context, MainActivity.class);
                    acquirePostSurgeryData = true;
                    startActivity(mainIntent);
                } else if (hasPreSurgeryData && acquirePostSurgeryData) {
                    // record vectors after surgery
                    //
                    dataArrayAfter = dataPoolArray;
                    Toast.makeText(getApplicationContext(), "Data saved.",
                            Toast.LENGTH_LONG).show();
                    buttonCompare.setEnabled(true);
                    hasPostSurgeryData = true;
                }

            }
        });

        buttonCompare.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (hasPreSurgeryData && hasPostSurgeryData) {
                    // display difference between pre-surgery vectors and
                    // post-surgery vectors
                    //
                    dataPoolArray = getVectorDifference(dataArrayBefore, dataArrayAfter);
                    Intent glIntent = new Intent(context, RSIViewerActivity.class);
                    glIntent.putExtra("dataPoolArray", dataPoolArray);
                    glIntent.putExtra("method", 1);
                    startActivity(glIntent);

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
        if (!hasPreSurgeryData) {
            buttonAfter.setEnabled(false);
        } else {
            buttonAfter.setEnabled(true);
        }

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

        int[] result;
        result = new int[] {1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1,
                1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0};
        int length = before.length;
        for (int i = 0; i < length; i++) {
            result[i] = result[i] + Math.abs(after[i] - before[i]);
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
