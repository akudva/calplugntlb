package calplug.bluetoothsri;

import calplug.bluetoothsri.mathUtility.vectorMath;

public class Magnetometer
{
    public int[] location = new int[2];
    public int x, y, z; // x, y, and z magnetic field values
    public int vThetaRatio;
    public boolean isFucked;

    public void updateVThetaRatio()
    {
        vThetaRatio = (int) vectorMath.getVectorThetaRatio(x, y, z);
    }
}
