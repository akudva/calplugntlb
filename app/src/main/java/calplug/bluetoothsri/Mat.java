package calplug.bluetoothsri;

import java.util.ArrayList;

import calplug.bluetoothsri.com.MAVLink.MAVLinkPacket;
import calplug.bluetoothsri.com.MAVLink.Messages.MAVLinkMessage;
import calplug.bluetoothsri.com.MAVLink.common.*;

public class Mat
{
    public ArrayList<Tile> tiles;
    public int[][] tile_locations; // This should be of the form { {0, 0}, {0, 1}, {1, 1}, {1, 0} }
    public int xSpacing;
    public int ySpacing;

    public Mat()
    {
        tiles = new ArrayList<>();
        tiles.add(new Tile());
    }

    // Returns an ArrayList of { x, y, thetaValue }
    public ArrayList<int[]> getMagnetometerData()
    {
        // TODO: Go through and check the modes of each of the tiles to get the total magnetometers
        ArrayList<int[]> magnetometerData = new ArrayList<>();

        // TODO: Make sure to take into account the tile positions
        for (Tile tile : tiles)
        {
            for (Magnetometer mag : tile.magnetometers)
            {
                if ( mag.isFucked )
                    continue;

                magnetometerData.add(new int[] {mag.location[0], mag.location[1], mag.vThetaRatio} );
            }
        }

        return magnetometerData;
    }

    public void updateTile(int tileNumber, float[] magnetometerData)
    {
        // TODO: Verify and make consistent exactly which tile we are handling
        tiles.get(tileNumber - 1).updateMagnetometers(magnetometerData);
    }
}
