package com.example.bighead.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {
    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = com.example.bighead.sunshine.app.Utility.isMetric(mContext);
        String highLowStr = com.example.bighead.sunshine.app.Utility.formatTemperature(mContext, high, isMetric) +
                "/" + com.example.bighead.sunshine.app.Utility.formatTemperature(mContext, low, isMetric);
        return highLowStr;
    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor) {
        // get row indices for our cursor
//        int idx_max_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP);
//        int idx_min_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP);
//        int idx_date = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
//        int idx_short_desc = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC);

        String highAndLow = formatHighLows(
                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));

        return com.example.bighead.sunshine.app.Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }


    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        //Determine layoutId from viewType
        switch (viewType){
            case VIEW_TYPE_TODAY:{
                layoutId = R.layout.list_item_forecast_today;
                break;
            }
            case VIEW_TYPE_FUTURE_DAY:{
                layoutId = R.layout.list_item_forecast;
                break;
            }
            default:{
                Log.e("in newView", "viewType not match");
            }
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY ;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    /*
       This is where we fill-in the views with the contents of the cursor.
    */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder)view.getTag();

        //get the date of weather
        String weatherDate = Utility.getFriendlyDayString(context, cursor.getLong(ForecastFragment.COL_WEATHER_DATE));
        //get the description of weather
        String weatherDescription = cursor.getString(ForecastFragment.COL_WEATHER_DESC);

        //check whether user changes setting.
        boolean isMetric = Utility.isMetric(context);
        //get max and min temperature.
        String temperatureMax = Utility.formatTemperature(mContext, cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP), isMetric);
        String temperatureMin = Utility.formatTemperature(mContext, cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP), isMetric);

        // Read weather icon ID from cursor
        int weatherConditionId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        //choose the corresponding icon depending on the type of layout
        int viewType = getItemViewType(cursor.getPosition());
        int iconId = -1;
        if(viewType == VIEW_TYPE_TODAY){
            iconId = Utility.getArtResourceForWeatherCondition(weatherConditionId);
        }else if(viewType == VIEW_TYPE_FUTURE_DAY){
            iconId = Utility.getIconResourceForWeatherCondition(weatherConditionId);
        }else{
            Log.e("in bindView", "viewType not match VIEW_TYPE_TODAY or VIEW_TYPE_FUTURE_DAY");
        }

        //bind data to the corresponding UI view
        viewHolder.iconView.setImageResource(iconId);
        viewHolder.dateView.setText(weatherDate);
        viewHolder.descriptionView.setText(weatherDescription);
        viewHolder.highTempView.setText(temperatureMax);
        viewHolder.lowTempView.setText(temperatureMin);

    }

    /**
     * Use ViewHolder can improve performance of adapter. Let ListView moves smoothly.
     *
     * **/
    private class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

         public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }

    /**
     * This is my version of converting db date to user friendly date string.
     *
     * **/
//    //formate weatherDate String to UserFriendlyDateString.
//    private String getUserFriendlyDateString(long givenDate){
//
//        String userFriendlyDateString = null;
//        //initial calendar object to get current time.
//        Calendar calendar = Calendar.getInstance();
//        //initial calendar to get given time.
//        Calendar givenDateCalendar = Calendar.getInstance();
//        givenDateCalendar.setTimeInMillis(givenDate);
//
//        int dayDifference = getDayDifference(calendar, givenDateCalendar);
//
//        switch(dayDifference){
//            case 0:{
//                userFriendlyDateString = mContext.getString(R.string.today) +
//                            givenDateCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) +
//                            givenDateCalendar.get(Calendar.DATE);;
//                break;
//            }
//            case 1:{
//                userFriendlyDateString = mContext.getString(R.string.tomorrow);
//                break;
//            }
//            default:{
////                int calendarWeekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH);
////                int givenDateWeekOfMonth = givenDateCalendar.get(Calendar.WEEK_OF_MONTH);
//                calendar.add(Calendar.DATE, dayDifference);
//                userFriendlyDateString = givenDateCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US);
//                if(dayDifference > 6){
//                    userFriendlyDateString += " " + givenDateCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) +
//                            givenDateCalendar.get(Calendar.DATE);
//
//                }
//            }
//        }
//
//        return userFriendlyDateString;
//    }
//
//    private int getDayDifference(Calendar calender, Calendar givenDate){
//
//        Log.v("givenDate day number", String.valueOf(givenDate.get(Calendar.DAY_OF_YEAR)));
//        Log.v("calendar day number", String.valueOf(calender.get(Calendar.DAY_OF_YEAR)));
//
//        if(calender.get(Calendar.YEAR) != givenDate.get(Calendar.YEAR)){
//           return givenDate.get(Calendar.DAY_OF_YEAR) +
//                    (calender.getActualMaximum(Calendar.DAY_OF_YEAR) - calender.get(Calendar.DAY_OF_YEAR));
//        }
//
//        return givenDate.get(Calendar.DAY_OF_YEAR) - calender.get(Calendar.DAY_OF_YEAR);
//    }

}