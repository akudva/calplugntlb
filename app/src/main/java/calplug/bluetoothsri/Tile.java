package calplug.bluetoothsri;

public class Tile
{
    public Magnetometer[] magnetometers;

    // The default mode should be eight mag.'s with no accelerometer
    public Tile()
    {
        initMagnetometers(8);
    }

    public Tile(int numMagnetometers)
    {
        initMagnetometers(numMagnetometers);
    }

    public void updateMagnetometers(float[] magnetometerData)
    {
        for (int i = 0; i < magnetometerData.length / 3; ++i)
        {
            this.magnetometers[i].x = (int) magnetometerData[3*i];
            this.magnetometers[i].y = (int) magnetometerData[3*i + 1];
            this.magnetometers[i].z = (int) magnetometerData[3*i + 2];
            this.magnetometers[i].updateVThetaRatio();
        }
    }

    public void initMagnetometers(int numMagnetometers)
    {
        this.magnetometers = new Magnetometer[numMagnetometers];

//        // The following are given in millimeters
//        int[][] magnetometer_locations = new int[][]
//                { {10, 140},
//                  {140, 140},
//                  {140, 10},
//                  {10, 10},
//                  {40, 110},
//                  {110, 110},
//                  {110, 40},
//                  {40, 40},
//                  {75, 75} };

        // The following are given in millimeters
        int[][] magnetometer_locations = new int[][]
                {{2, 28},
                        {28, 28},
                        {28, 2},
                        {2, 2},
                        {8, 22},
                        {22, 22},
                        {22, 8},
                        {8, 8}};

        // Set locations for all magnetometers in tile
        for (int i = 0; i < numMagnetometers; ++i)
        {
            magnetometers[i] = new Magnetometer();
            magnetometers[i].location = magnetometer_locations[i];
        }
    }
}
