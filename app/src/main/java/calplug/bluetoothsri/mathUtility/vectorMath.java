package calplug.bluetoothsri.mathUtility;

/**
 * vector math functions
 * @author Zhihao
 */
public class vectorMath {

    /**
     * return the length of a vector
     * @param x
     * @param y
     * @param z
     * @return length of a vector
     */
    public static double vLength(int x, int y, int z) {
        return Math.sqrt(x*x + y*y +z*z);
    }

    /**
     * calculate vTheta value
     * @param x1
     * @param y1
     * @param z1
     * @return
     */
    public static double vTheta(int x1, int y1, int z1) {
        //projected vector
        //
        int x2 = x1; int y2 = y1; int z2 = 0;
        int innerProduct = x1*x2 + y1*y2 + z1*z2;
        double cosine = (double) innerProduct /
                ((vLength(x1, y1, z1) * vLength(x2, y2, z2)));
        double theta = Math.acos(cosine);
        return theta;
    }

    /**
     * calculate phi value
     * @param x1
     * @param y1
     * @param z1
     * @return phi of a vector
     */
    public static double vPhi(int x1, int y1, int z1) {
        // unit vector
        //
        int x0 = -1; int y0 = 0; int z0 = 0;
        // projected vector
        //
        int x2 = x1; int y2 = y1; int z2 = 0;
        int innerProduct = x0*x2 + y0*y2 + z0*z2;
        double cosine = (double) innerProduct /
                ((vLength(x0, y0, z0) * vLength(x2, y2, z2)));
        double theta = Math.acos(cosine);
        return theta;
    }

    /**
     * return the length of the longest vector of all vectors
     * @param dataPoolArray
     * @return
     */
    public static double vLongVectorLength(int[] dataPoolArray){

        double longestLength = 0.0d, currentLength = 0.0d;
        for (int index = 0; index < dataPoolArray.length; index+=3){
            currentLength = vLength(dataPoolArray[index], dataPoolArray[index+1], dataPoolArray[index+2]);
            if (currentLength >= longestLength){
                longestLength = currentLength;
            }
        }
        return longestLength;
    }

    /**
     * return current vector length / longest vector length
     * @param ratio
     * @return
     */
    public static float lengthRatioDistribution(float ratio){

        double rRatio = 1.0d / ratio;
        return (float) Math.tanh(rRatio);
    }


    /**
     * get vector's space coordinates and calc theta value
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static float getVectorThetaRatio(int x, int y, int z){

        float theta = (float) Math.toDegrees(vTheta(x, y, z));
        if (Float.isNaN(theta)){
            theta = 90.0f; // vector is perpendicular
        }
        System.out.println(theta);
        return Math.abs(theta);
    }

    public static int[] getThetaList(int[] dataPool) {
        int[] thetaList = new int[]{};
        for (int index = 0; index < dataPool.length; index += 3) {
            thetaList = addInttoIntArray(thetaList,
                    Math.round(getVectorThetaRatio(dataPool[index],
                            dataPool[index + 1],
                            dataPool[index + 2])));
        }
        return thetaList;
    }

    public static int[] addInttoIntArray(int[] oldArray, int value) {

        int[] newArray = new int[oldArray.length + 1];
        for (int index = 0; index < oldArray.length; index++) {
            newArray[index] = oldArray[index];
        }
        newArray[newArray.length - 1] = value;
        return newArray;
    }
}
