package calplug.bluetoothsri.heatMapUtility;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/************************************************************************
 Author: Numetric Technologies
    Licensed under GNU GPL v3
    https://github.com/numetriclabz/numAndroidCharts

 Minor modification includes,
    disable legends display
    change the type of heatmap data from int to double

 *************************************************************************/

public class HeatMap extends View {

    private int width, height;
    private JSONArray limits;
    private List<ChartData> data, columns, rows;
    private float block_width, block_height;
    private boolean verbose = false;
    RectF legends;

    private Paint paint, textPaint;

    public HeatMap(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
    }

    private void init(){

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setDither(true);
        textPaint.setTextSize(20);

        block_height = width /columns.size();
        block_width = (width - getRowTextWidth())/ rows.size() - 5;

        setKeepScreenOn(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();

        width = Math.max(minw, MeasureSpec.getSize(widthMeasureSpec)) + 70;
        height = MeasureSpec.getSize(heightMeasureSpec) +70; // adjust size here

        setMeasuredDimension(width, height);
    }


    @Override
    protected void onDraw(Canvas canvas){

        init();

        int valIndex = 0;
        float rowTop = 20;
        float rowBottom = block_height  + 20;

        for(int rowIndex = 0; rowIndex < rows.size(); rowIndex++){

            float colLeft = 20 + getRowTextWidth();
            float colRight = block_width + 20 + getRowTextWidth();


            for (int colIndex = 0; colIndex < columns.size(); colIndex++){

                String color = fillColor(data.get(valIndex).getHeat_value());
                RectF rect = new RectF(colLeft, rowTop, colRight, rowBottom);
                // may adjust colLeft, colRight to cover grid lines


                paint.setColor(Color.parseColor(color));
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawRect(rect, paint);

                String value = data.get(valIndex).getHeat_value()+"";
                //display value
                if (verbose)
                    canvas.drawText(value, (colRight + colLeft)/2, (rowTop + rowBottom)/2, textPaint);

                colLeft += block_width;
                colRight += block_width;
                valIndex++;
            }

            textPaint.setTextSize(24);
            String value = rows.get(rowIndex).getLabels();
            if (verbose)
                canvas.drawText(value, 10, (rowTop + rowBottom)/2, textPaint);

            rowTop += block_height;
            rowBottom +=block_height;
        }

        plotXAxisLabels(canvas, (rowBottom - block_height + 20));
        //addLegends(canvas, (int)rowBottom, (int)(20 + getRowTextWidth()));
    }


    private void plotXAxisLabels(Canvas canvas, float bottom){

        float colLeft = 20 ;
        float colRight = block_width ;

        for (int colIndex = 0; colIndex < columns.size(); colIndex++){

            String value = columns.get(colIndex).getLabels();

            canvas.drawText(value, (colLeft + colRight)/2 + 30, bottom + getColTextHeight(), textPaint);
            colLeft += block_width;
            colRight += block_width;
        }
    }


    public void setLimits(JSONArray limit){
        this.limits = limit;
        invalidate();
    }


    public void setColumns(List<ChartData> columns){
        this.columns = columns;
    }


    public void setRows(List<ChartData> rows){
        this.rows = rows;
    }


    public void setDataSet(List<ChartData> dataSet){
        this.data = dataSet;
    }


    private float getRowTextWidth(){

        Rect bounds = new Rect();
        textPaint.setTextSize(24);

        float text_width = 0;

        for(int rowIndex = 0; rowIndex < rows.size(); rowIndex++){

            textPaint.getTextBounds("Three", 0, "Three".length(), bounds);

            if(bounds.width() > text_width)
                text_width = bounds.width();
        }
        return text_width;
    }


    private float getColTextHeight(){

        Rect bounds = new Rect();
        textPaint.setTextSize(24);

        float text_width = 0;

        for(int rowIndex = 0; rowIndex < rows.size(); rowIndex++){

            //String row = rows.get(rowIndex).getRows();
            textPaint.getTextBounds("Three", 0, "Three".length(), bounds);

            if(bounds.width() > text_width)
                text_width = bounds.height();
        }
        return text_width;
    }


    private String fillColor(double value){

        String color = "";

        for(int index = 0; index < limits.length(); index++){

            try {
                JSONObject limit = (JSONObject)limits.get(index);

                int min = limit.getInt("minvalue");
                int max = limit.getInt("maxvalue");

                if(value >= min && value < max){
                    color =  limit.getString("colorcode");
                    break;
                }
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
        return color;
    }

    private void addLegends(Canvas canvas, int top, int left){

        legends = new RectF(left, top, width, height);

        for(int index = 0; index < limits.length(); index++){

            try {
                JSONObject limit = (JSONObject)limits.get(index);

                String label = limit.getString("label");
                String color =  limit.getString("colorcode");

                Rect r = new Rect(left, top, left + 30, top + 30);
                paint.setColor(Color.parseColor(color));
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawRect(r, paint);
                canvas.drawText(label, left + 40, top + 20, textPaint);
                float text_width = textPaint.measureText(label, 0, label.length());
                left += (text_width + 60) ;
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    public void setVerbose(boolean verboseSwitch) {
        verbose = verboseSwitch;
    }
}
