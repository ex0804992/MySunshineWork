package com.example.bighead.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bighead.sunshine.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    static final String DETAIL_URI = "URI";
    private static Uri mUri;    //The uri is put in the loader.
    private ShareActionProvider mshareActionProvider;
    private String mforecast;
    private static final String FORCAST_SHARE_HASHTAG = " #SunshineApp";
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final int DETAIL_LOADER = 0;
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            //This works because the WeatherProvider returns location data joined with
            //weather data, even though they're stored in two different tables.
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING

    };

    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_MAX_TEMP = 3;
    private static final int COL_WEATHER_MIN_TEMP = 4;
    private static final int COL_WEATHER_HUMIDITY = 5;
    private static final int COL_WEATHER_WIND_SPEED = 6;
    private static final int COL_WEATHER_PRESSURE = 7;
    private static final int COL_WEATHER_DEGREE = 8;
    private static final int COL_WEATHER_CONDITION_ID = 9;

    private TextView mFriendlyDateView;
    private TextView mDateView;
    private TextView mTempMaxTextView;
    private TextView mTempMinTextView;
    private TextView mDescTextView;
    private TextView mHumidityTextView;
    private TextView mWindSpeedTextView;
    private TextView mPressureTextView;
    private ImageView mIconView;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    //when location is changed by changing settings, we update data and restart loader;
    public void onLocationChanged(String location){

        Uri uri = mUri;
        if(uri != null){
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            mUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, date);
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    private Intent createShareForcastIntent(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, mforecast +FORCAST_SHARE_HASHTAG);
        return intent;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_share_action_provider, menu);
        MenuItem shareItem = menu.findItem(R.id.menu_item_share);
        mshareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

        if(mshareActionProvider != null){
            mshareActionProvider.setShareIntent(createShareForcastIntent());
        }else{
            Log.e(LOG_TAG, "Share Action Provider is null?");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "In onResume");

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "In onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //we replace or open detailFragment with new DetailFragment whatever pattern the layout is.
        //Because of using new DetailFragment, onCreateView will be execute every time.
        Bundle args = getArguments();
        if(args != null) {
        //mUri = Uri.parse(args.getString("dateUri"));
          mUri = args.getParcelable(DetailFragment.DETAIL_URI); //another data save format used to save uri.
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mFriendlyDateView = (TextView)rootView.findViewById(R.id.detail_day_textview);
        mDateView = (TextView)rootView.findViewById(R.id.detail_date_textview);
        mTempMaxTextView = (TextView)rootView.findViewById(R.id.list_item_high_textview);
        mTempMinTextView = (TextView)rootView.findViewById(R.id.list_item_low_textview);
        mDescTextView = (TextView)rootView.findViewById(R.id.list_item_forecast_textview);
        mHumidityTextView = (TextView)rootView.findViewById(R.id.list_item_humidity_textview);
        mWindSpeedTextView = (TextView)rootView.findViewById(R.id.list_item_wind_textview);
        mPressureTextView = (TextView)rootView.findViewById(R.id.list_item_pressure_textview);
        mIconView = (ImageView)rootView.findViewById(R.id.list_item_icon);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Log.v(LOG_TAG, "In onCreateLoader");

        if(mUri == null){
            return null;
        }

        switch (id) {
            case DETAIL_LOADER: {
                return new CursorLoader(getActivity(),
                        mUri,
                        FORECAST_COLUMNS,
                        null,
                        null,
                        null);
            }
            default: {
                return null;
            }
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");
        if(!data.moveToFirst()){
            return ;
        }
        // Read weather condition ID from cursor
        int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
        int iconId = Utility.getArtResourceForWeatherCondition(weatherId);
        // Read date from cursor and update views for day of week and date
        String dayString = Utility.getDayName(getActivity(), data.getLong(COL_WEATHER_DATE));
        String dateString = Utility.getFormattedMonthDay(getActivity(), data.getLong(COL_WEATHER_DATE));

        // Read high and low temperature from cursor and update view
        boolean isMetric = Utility.isMetric(getActivity());
        String minTemperatureString = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
        String maxTemperatureString = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);

        // Read description from cursor and update view
        String descString = data.getString(COL_WEATHER_DESC);

        // Read humidity from cursor and update view
        String humidityString = getActivity().getString(R.string.format_humidity, data.getDouble(COL_WEATHER_HUMIDITY));

        // Read wind speed and direction from cursor and update view
        String windSpeedString = Utility.getFormattedWind(getActivity(),
                data.getFloat(COL_WEATHER_WIND_SPEED),
                data.getFloat(COL_WEATHER_DEGREE));

        // Read pressure from cursor and update view
        String pressureString = getActivity().getString(R.string.format_pressure, data.getFloat(COL_WEATHER_PRESSURE));

        mFriendlyDateView.setText(dayString);
        mDateView.setText(dateString);
        mDescTextView.setText(descString);
        mTempMaxTextView.setText(maxTemperatureString);
        mTempMinTextView.setText(minTemperatureString);
        mHumidityTextView.setText(humidityString);
        mWindSpeedTextView.setText(windSpeedString);
        mPressureTextView.setText(pressureString);
        mIconView.setImageResource(iconId);

        // We still need this for the share intent
        mforecast = String.format("%s - %s - %s/%s", dateString, descString, maxTemperatureString, minTemperatureString);

        //Because We reload the cursor, we need to update the shareIntent.
        if(mshareActionProvider != null){
            mshareActionProvider.setShareIntent(createShareForcastIntent());
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}