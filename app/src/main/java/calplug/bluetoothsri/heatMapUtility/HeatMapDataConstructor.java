package calplug.bluetoothsri.heatMapUtility;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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


public class HeatMapDataConstructor {

    private List<ChartData> values = new ArrayList();
    private double[][] valuesBuffer;
    private boolean[][] isAssigned;
    private Set targetRows = new HashSet();
    private int rowsTotal, columnsTotal;

    public HeatMapDataConstructor(
            int rows,
            int columns,
            List<ChartData> samplePoints){

        rowsTotal = rows;
        columnsTotal = columns;

        isAssigned = new boolean[rows + 1][columns + 1];
        valuesBuffer = new double[rows + 1][columns + 1];

        setSamplePoints(samplePoints);
        setTargetLines();
        drawIntermediateLines();
    }

    private void setSamplePoints(List<ChartData> samplePoints) {

        // process a single sample point at a time
        //
        ChartData currentSamplePoint;
        int column, row;

        // get each value from list, and copy the value to Values list.
        //
        for (int sampleIndex = 0;
             sampleIndex < samplePoints.size();
             sampleIndex++) {
            currentSamplePoint = samplePoints.get(sampleIndex);

            // Update valueBuffer, isAssigned and targetRows
            //
            row = getRoleNumberFromString(currentSamplePoint.getRows());
            column = getRoleNumberFromString(currentSamplePoint.getColumn());
            valuesBuffer[row][column] = currentSamplePoint.getHeat_value();
            isAssigned[row][column] = true;
            targetRows.add(row);
        }
    }

    private int getRoleNumberFromString(String roleNumber) {
        // remove the first character and convert to int
        //
        return Integer.parseInt(roleNumber.substring(1));
    }


    private void drawIntermediateLines() {

        double sourceValue = 0, targetValue;
        int fillCount;

        for (int colIndex = 1; colIndex <= columnsTotal; ++colIndex) {
            fillCount = 0;
            for (int rowIndex = 1; rowIndex <= rowsTotal; ++rowIndex) {
                if (rowIndex == 1 && !targetRows.contains(rowIndex)) {
                    // record origin color at top edge
                    //
                    sourceValue = 0;
                    ++fillCount;
                } else if (targetRows.contains(rowIndex)) {
                    // encounter target row, calculate shade and reset origin color
                    //
                    targetValue = valuesBuffer[rowIndex][colIndex];
                    if (fillCount >= 1) {
                        fillIntermediateLineSegment(colIndex,
                                rowIndex,
                                fillCount,
                                sourceValue,
                                targetValue,
                                false);
                    }
                    sourceValue = targetValue;
                    fillCount = 0;
                } else if (rowIndex == rowsTotal) {
                    // finish target line (the last grid is not assigned)
                    //
                    ++fillCount;
                    targetValue = 0;
                    fillIntermediateLineSegment(colIndex,
                            rowIndex,
                            fillCount,
                            sourceValue,
                            targetValue,
                            true);
                } else {
                    // increase fill counter if the grid is neither an assigned nor edge
                    //
                    ++fillCount;
                }
            }


        }
    }


    private void setTargetLines() {
        Iterator tLineIterator = targetRows.iterator();
        int currentRow;
        while (tLineIterator.hasNext()) {
            // iterate the target line set
            //
            currentRow = (int) tLineIterator.next();
            if (currentRow <= 0 || currentRow > rowsTotal) {
                // Todo: handle wrong input
                //
                System.out.println("STDERROR: wrong input");
            } else {
                // draw the target line at currentRow
                //
                drawTargerLine(currentRow);
            }
        }
    }

    private void drawTargerLine(int currentRow) {
        double sourceValue = 0, targetValue;
        int fillCount = 0;
        for (int currentCol = 1;
             currentCol <= columnsTotal;
             ++currentCol) {
            if (currentCol == 1
                    && !isAssigned[currentRow][currentCol]) {
                // record origin color at left edge
                //
                sourceValue = 0;
                ++fillCount;
            } else if (isAssigned[currentRow][currentCol]) {
                // encounter assigned grid, calculate shade and reset origin color
                //
                targetValue = valuesBuffer[currentRow][currentCol];
                if (fillCount >= 1) {
                    fillTargetLineSegment(currentCol, currentRow,
                            fillCount, sourceValue, targetValue, false);
                }
                sourceValue = targetValue;
                fillCount = 0;
            } else if (currentCol == columnsTotal) {
                // finish target line (the last grid is not assigned)
                //
                ++fillCount;
                targetValue = 0;
                fillTargetLineSegment(currentCol, currentRow,
                        fillCount, sourceValue, targetValue, true);
            } else {
                // increase fill counter if the grid is neither an assigned nor edge
                //
                ++fillCount;
            }
        }
    }

    private void fillIntermediateLineSegment(int currentCol,
                                       int currentRow,
                                       int fillCount,
                                       double sourceValue,
                                       double targetValue,
                                       boolean endRowFlag) {
        double drawValue;
        int drawCount = 1;
        // draw backward
        //
        if (endRowFlag) {
            // draw until the last column
            //
            for (int drawIndex = currentRow - fillCount + 1;
                 drawIndex <= currentRow; ++drawIndex) {
                drawValue = getAverageValue(sourceValue,
                        targetValue,
                        fillCount,
                        drawCount);
                // set assign flag and buffer
                //
                isAssigned[drawIndex][currentCol] = true;
                valuesBuffer[drawIndex][currentCol] = drawValue;
                ++drawCount;
            }
        } else {
            // draw until the position before the assigned grid
            //
            for (int drawIndex = currentRow - fillCount;
                 drawIndex < currentRow; ++drawIndex) {
                drawValue = getAverageValue(sourceValue,
                        targetValue,
                        fillCount,
                        drawCount);
                // set assign flag and buffer
                //
                isAssigned[drawIndex][currentCol] = true;
                valuesBuffer[drawIndex][currentCol] = drawValue;
                ++drawCount;
            }
        }
    }

    private void fillTargetLineSegment(int currentCol,
                                         int currentRow,
                                         int fillCount,
                                         double sourceValue,
                                         double targetValue,
                                       boolean endColFlag) {
        double drawValue;
        int drawCount = 1;
        // draw backward
        //
        if (endColFlag) {
            // draw until the last column
            //
            for (int drawIndex = currentCol - fillCount + 1;
                 drawIndex <= currentCol; ++drawIndex) {
                drawValue = getAverageValue(sourceValue,
                        targetValue,
                        fillCount,
                        drawCount);
                // set assign flag and buffer
                //
                isAssigned[currentRow][drawIndex] = true;
                valuesBuffer[currentRow][drawIndex] = drawValue;
                ++drawCount;
            }
        } else {
            // draw until the position before the assigned grid
            //
            for (int drawIndex = currentCol - fillCount;
                 drawIndex < currentCol; ++drawIndex) {
                drawValue = getAverageValue(sourceValue,
                        targetValue,
                        fillCount,
                        drawCount);
                // set assign flag and buffer
                //
                isAssigned[currentRow][drawIndex] = true;
                valuesBuffer[currentRow][drawIndex] = drawValue;
                ++drawCount;
            }
        }
    }

    /**
     * getAverageValue sets the average value between target and origin
     * @param sourceValue
     * @param targetValue
     */
    private double getAverageValue(double sourceValue,
                                 double targetValue,
                                 int fillCount,
                                 int CurrentStep) {

        double stepLength =
                (targetValue - sourceValue) / (fillCount + 1);
        return sourceValue + CurrentStep * stepLength;
    }

    public List<ChartData> getDataSet() {
        for (int rowIndex = 1; rowIndex <= rowsTotal; ++rowIndex) {
            for (int colIndex = 1; colIndex <= columnsTotal; ++colIndex) {
                values.add(new ChartData(
                        String.format("R%d", rowIndex),
                        String.format("C%d", colIndex),
                        valuesBuffer[rowIndex][colIndex]));
            }
        }
        return values;
    }
}
