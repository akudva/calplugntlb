package calplug.bluetoothsri;

import calplug.bluetoothsri.mathUtility.vectorMath;

public class Magnetometer
{
    public int[] location = new int[2];
    public int x, y, z; // x, y, and z magnetic field values
    public boolean isFucked;

    public Magnetometer(int[] initLocation) { location = initLocation; }

    // NOTE: Not sure if I should have the vThetaRatio here or not. For now just calculate
    // when creating heatmap
    //
    // public int vThetaRatio;
    // public void updateVThetaRatio() { vThetaRatio = (int) vectorMath.getVectorThetaRatio(x, y, z); }
}
