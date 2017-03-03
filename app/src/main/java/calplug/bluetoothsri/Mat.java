package calplug.bluetoothsri;

import android.util.Log;

import java.util.ArrayList;

public class Mat
{
    // TODO: Right now the Tiles and their locations are separate, it would probably be better to
    // TODO: (cont'd) join them in a single data structure
    public ArrayList<Tile> tiles;
    public ArrayList<int[]> tileLocations; // This should be of the form { {0, 0}, {0, 1}, {1, 1}, {1, 0} }
    public int xDimension = 1;
    public int yDimension = 1;
    public int xSpacing = 1;
    public int ySpacing = 1;

    public Mat(int numberTiles, int[][] initTileLocations, ConfigurationDetails.tileModes tileMode)
    {
        int rightmostTileCoordinate = 0;
        int topmostTileCoordinate = 0;

        tiles = new ArrayList<Tile>();
        tileLocations = new ArrayList<int[]>();

        // TODO: check that numberTiles and initTileLocations dimensions are correct
        for (int i = 0; i < numberTiles; ++i)
        {
            tiles.add( new Tile(tileMode) );
            tileLocations.add( initTileLocations[i] );

            int currentTileX = initTileLocations[i][0];
            int currentTileY = initTileLocations[i][1];

            if (currentTileX > rightmostTileCoordinate)
                rightmostTileCoordinate = currentTileX;

            if (currentTileY > topmostTileCoordinate)
                topmostTileCoordinate = currentTileY;

        }

        calculateDimensions(rightmostTileCoordinate, topmostTileCoordinate);
    }

    void calculateDimensions(int rightmostTileCoordinate, int topmostTileCoordinate)
    {
        Log.d("DACODA", String.format("Calculating dimensions for tile of size %d x %d", rightmostTileCoordinate, topmostTileCoordinate));
        xDimension = (rightmostTileCoordinate+1) * Tile.xDimension + rightmostTileCoordinate*xSpacing;
        yDimension = (topmostTileCoordinate+1) * Tile.yDimension + topmostTileCoordinate*ySpacing;
        Log.d("DACODA", String.format("The mat is %d x %d millimeters", xDimension, yDimension));
    }

    // Returns an ArrayList of { x, y, thetaValue }
    public ArrayList<int[]> getMagnetometerData()
    {
        // TODO: Go through and check the modes of each of the tiles to get the total magnetometers
        ArrayList<int[]> magnetometerData = new ArrayList<>();

        // TODO: Make sure to take into account the tile positions
        for (int i = 0; i < tiles.size(); ++i)
        {
            int xOffset = tileLocations.get(i)[0] * (Tile.xDimension + xSpacing);
            int yOffset = tileLocations.get(i)[1] * (Tile.yDimension + ySpacing);

            for (Magnetometer mag : tiles.get(i).magnetometers)
            {
                if ( mag.isFucked )
                    continue;

                magnetometerData.add(new int[] {mag.location[0] + xOffset, mag.location[1] + yOffset, mag.vThetaRatio} );
            }
        }

        return magnetometerData;
    }

    public void updateTile(int tileNumber, float[] magnetometerData)
    {
        Log.d("MAT", String.format("Updating tile #%d", tileNumber));
        // TODO: Verify and make consistent exactly which tile we are handling
        tiles.get(tileNumber).updateMagnetometers(magnetometerData);
    }

}
