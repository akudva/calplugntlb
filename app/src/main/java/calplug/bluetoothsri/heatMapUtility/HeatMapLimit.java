package calplug.bluetoothsri.heatMapUtility;

import org.json.JSONArray;
import org.json.JSONObject;

/************************************************************************
 Copyright (C) 2016 Zhihao

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *************************************************************************/

public class HeatMapLimit {

    private JSONArray limits = new JSONArray();

    /**
     * construct a heatMapLimit object, fill limit list
     * @param stepCount
     * @param stepRange
     */
    public HeatMapLimit(int stepCount, double stepRange) {

        String colorCode;
        double stepLength = stepRange / stepCount;
        for (int stepIndex = 0;
             stepIndex <= stepCount;
             ++stepIndex) {
            colorCode = getColorCode(stepIndex, stepCount);
            addSingleLimit(stepLength * stepIndex,
                    stepLength * (stepIndex + 1),
                    colorCode);
        }
    }

    /**
     * add a single limit to the limit list
     * @param floor
     * @param ceiling
     * @param colorCode
     * @return
     */
    private boolean addSingleLimit(
            double floor,
            double ceiling,
            String colorCode) {

        JSONObject limit_tmp = new JSONObject();
        try {
            limit_tmp.put("minvalue", floor + "");
            limit_tmp.put("maxvalue", ceiling + "");
            limit_tmp.put("colorcode", colorCode);
            limit_tmp.put("label", "");
            this.limits.put(limit_tmp);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * adjust color based on the percentage of stepRatio
     * @param stepIndex
     * @param stepCount
     * @return
     */
    private String getColorCode(
            int stepIndex,
            int stepCount){

        double stepRatio = 1.0f * stepIndex / stepCount;
        double colorRGBValue_Tmp;
        int[] colorRGBArray = new int[2];
        String colorCode;

        if (stepRatio >= 0.5){
            colorRGBArray[0] = 255;
            colorRGBValue_Tmp = 1.0f * ((1 - stepRatio) * 2) * 255;
            colorRGBArray[1] = (int) colorRGBValue_Tmp;
        } else {
            colorRGBValue_Tmp = 1.0f * (stepRatio * 2) * 255;
            colorRGBArray[0] = (int) colorRGBValue_Tmp;
            colorRGBArray[1] = 255;
        }
        colorCode = String.format("#%02X%02X00",
                colorRGBArray[0],
                colorRGBArray[1]);
        return colorCode;
    }

    /**
     * return the auto-generated JSON array for heat map
     * @return limit list
     */
    public JSONArray getLimitArray() {
        return this.limits;
    }
}
