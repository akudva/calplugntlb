package calplug.bluetoothsri;

import android.util.Log;

public class Tile
{
    public Magnetometer[] magnetometers;

    public static int xDimension = 150;
    public static int yDimension = 150;

    // Initialize Tile based on what configuration it should be in
    public Tile( ConfigurationDetails.tileModes tileMode )
    {
        int numberMagnetometers = 8;
        boolean hasAccelerometer = false;

        switch (tileMode)
        {
            case EIGHT_MAGNETOMETERS_SANS_ACCELEROMETER:
                numberMagnetometers = 8;
                hasAccelerometer = false;
                break;
            case EIGHT_MAGNETOMETERS_WITH_ACCELEROMETER:
                numberMagnetometers = 8;
                hasAccelerometer = true;
                break;
            case NINE_MAGNETOMETERS_SANS_ACCELEROMETER:
                numberMagnetometers = 9;
                hasAccelerometer = false;
                break;
            case NINE_MAGNETOMETERS_WITH_ACCELEROMETER:
                numberMagnetometers = 9;
                hasAccelerometer = true;
                break;
        }

        initMagnetometers(numberMagnetometers);
        // initAccelerometer(hasAccelerometer);
    }

    public void updateMagnetometers(float[] magnetometerData)
    {
        Log.d("TILE", "Updating magnetometer with following measurements");
        for (int i = 0; i < magnetometerData.length / 3; ++i)
        {
            Log.d("TILE", String.format("Magnetometer %d: %.3f, %.3f, %.3f", i, magnetometerData[3*i], magnetometerData[3*i+1], magnetometerData[3*i+2]));
            this.magnetometers[i].x = (int) magnetometerData[3*i];
            this.magnetometers[i].y = (int) magnetometerData[3*i + 1];
            this.magnetometers[i].z = (int) magnetometerData[3*i + 2];
            // this.magnetometers[i].updateVThetaRatio();
        }
    }

    public void initMagnetometers(int numMagnetometers)
    {
        this.magnetometers = new Magnetometer[numMagnetometers];

        // The following are given in millimeters
        int[][] magnetometerLocations= new int[][]
                { {10, 140},
                  {140, 140},
                  {140, 10},
                  {10, 10},
                  {40, 110},
                  {110, 110},
                  {110, 40},
                  {40, 40},
                  {75, 75} };

        // Set locations for all magnetometers in tile
        for (int i = 0; i < numMagnetometers; ++i)
        {
            magnetometers[i] = new Magnetometer(magnetometerLocations[i]);
        }
    }
}
