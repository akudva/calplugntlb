package calplug.bluetoothsri.heatMapUtility;

import android.content.Context;
import android.util.AttributeSet;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

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


public class HeatMapHelper extends HeatMap{

    public HeatMapHelper(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
    }

    public void setDataHelper(HeatMapDataConstructor dataConstructor) {
        this.setDataSet(dataConstructor.getDataSet());
    }

    public void setLimitsHelper(int stepCount, double stepRange) {
        HeatMapLimit heatMapLimit = new HeatMapLimit(stepCount, stepRange);
        JSONArray heatMaplimit_JSON = heatMapLimit.getLimitArray();
        this.setLimits(heatMaplimit_JSON);
    }

    public void setColsRowsHelper(int columns, int rows) {
        List<ChartData> helperColumns = addColumnToHelperColumns(columns);
        List<ChartData> helperRows= addRowToHelperRows(rows);
        this.setColumns(helperColumns);
        this.setRows(helperRows);
    }

    private List<ChartData> addColumnToHelperColumns(int columns) {
        List<ChartData> helperColumns = new ArrayList();
        for (int index = 1; index <= columns; ++index) {
            helperColumns.add(new ChartData(String.format("C%d", index)));
        }
        return helperColumns;
    }

    private List<ChartData> addRowToHelperRows(int rows) {
        List<ChartData> helperRows = new ArrayList();
        for (int index = 1; index <= rows; ++index) {
            helperRows.add(new ChartData(String.format("R%d", index)));
        }
        return helperRows;
    }
}
