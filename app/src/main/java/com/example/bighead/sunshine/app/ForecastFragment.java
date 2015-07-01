package com.example.bighead.sunshine.app;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.bighead.sunshine.app.data.WeatherContract;
import com.example.bighead.sunshine.app.service.SunshineService;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    Callback callback;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;  //Use to record user selected position of view of list
    private static final String SELECTED_KEY = "selected_position"; //Use to retrieve data saved in savedInstanceState bundle.
    private boolean mUseTodayLayout = true;     //Use to decide whether to use special Today Layout or not.
    private ForecastAdapter mForecastAdapter;
    private static final int FORECAST_LOADER = 0;   //Loader's tag to determine which loader we use.
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
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    public ForecastFragment(){
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.
        // To only show current and future dates, filter the query to return weather only for
        // dates after or including today.

        switch(id){
            case FORECAST_LOADER:{
                // This is called when a new Loader needs to be created.
                String locationSetting = Utility.getPreferredLocation(getActivity());
                Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                        locationSetting, System.currentTimeMillis());
                String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

                return new CursorLoader(getActivity(),
                        weatherForLocationUri,
                        FORECAST_COLUMNS,
                        null,
                        null,
                        sortOrder);
            }
            default:{
                return null;
            }
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mForecastAdapter.swapCursor(data);

        //set listView to selected position when activity is recreated after destroyed.
        if(mPosition != ListView.INVALID_POSITION) {
            mListView.smoothScrollToPosition(mPosition);
            mListView.setItemChecked(mPosition, true);      //This name makes me confuse, but this function is what we are looking for.
                                                            //use setItemChecked() to set android:state_activated="true".
        }else{
            //mPosition == ListView.INVALID_POSITION shows that list view is just initialized.
            //So, we pre selected the first item.
            //Most important, We should wait the listView to be populated by mForecastAdapter, or it will fail.
            if(getActivity().findViewById(R.id.weather_detail_container) != null) {
                final int firstPosition = mListView.getFirstVisiblePosition();
                final View firstItem = mListView.getChildAt(firstPosition);


//                new Handler().post(new Runnable() {
//                    @Override
//                    public void run() {
//                        mListView.performItemClick(
//                                firstItem,
//                                firstPosition,
//                                mListView.getAdapter().getItemId(firstPosition));
//                    }
//                });

            }
        }
     }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mForecastAdapter.swapCursor(null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    //To connect mainActivity's callback implementation to this callback.
    //When we call callback.onItemSelected we will execute implementation codes of mainActivity.
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.v("ForecastFragment", "In onAttach");
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callback = (Callback)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement Callback interface");
        }

    }

    /**
     * use to set mUseTodayLayout member variable.
     * If we choose true, we use special today layout(common in one pane layout) in listview's adapter.
     * Choose false when we want to use normal today layout(in two pane layout) in listview's adapter.
     * **/
    public void setUseTodayLayout(boolean useTodayLayout){
        mUseTodayLayout = useTodayLayout;
        if(mForecastAdapter != null){
            mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }

//Find Why I can't do all work in onresume.
//    @Override
//    public void onResume() {
//        super.onResume();
//
//
//            if (mPosition == ListView.INVALID_POSITION && getActivity().findViewById(R.id.weather_detail_container) != null) {
//                final int firstPosition = mListView.getFirstVisiblePosition();
//                final View firstItem = mListView.getChildAt(firstPosition);
//                Log.e("ONRESUME", "in onresume");
//                mListView.performItemClick(firstItem, firstPosition, mForecastAdapter.getItemId(firstPosition));
//                mPosition = firstPosition;
//
////                new Handler().post(new Runnable() {
////                    @Override
////                    public void run() {
////                        mListView.performItemClick(
////                                firstItem,
////                                firstPosition,
////                                mListView.getAdapter().getItemId(firstPosition));
////                    }
////                });
//
//            }
//
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        mListView = (ListView)rootView.findViewById(R.id.listview_forecast);
        mListView.setAdapter(mForecastAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //get position from listview's adapter
                Cursor cursor = (Cursor)parent.getItemAtPosition(position);
                if(cursor != null){
                    // CursorAdapter returns a cursor at the correct position for getItem(), or null
                    // if it cannot seek to that position.
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    Uri dateUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                            locationSetting,
                            cursor.getLong(COL_WEATHER_DATE));
                    callback.onItemSelected(dateUri);

//Another method to call onItemSelected
//                    ((Callback) getActivity()).onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
//                            locationSetting, cursor.getLong(COL_WEATHER_DATE)
//                    ));
                }
                mPosition = position;
            }
        });



        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if(savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)){
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swap out in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        //Because mForecastAdapter is initialized here, so we call setUseTodayLayout() as default setting.
        //We don't know whether onCreateView is completed precedes Activity's onCreate, So we call setUseTodayLayout here
        //and in Activity's onCreate.
        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if(mPosition != ListView.INVALID_POSITION){
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //First of all, get the data from internet, because there are no data in database.
        updateWeather();
        //tell app framework this fragment want to populate in menu option.
        //So, app framework will call corresponding method like "onCreateOptionsMenu".
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forecastfragment, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateWeather(){

        //Using PreferenceManager to get sharedPreferences.
        //sharedPreferences can be used to get preference(save key/value pair).
        String location = Utility.getPreferredLocation(getActivity());

//     This is my first version to update the settings to UI
//        String temperatureUnit = sharedPreferences.getString(getString(R.string.pref_temperature_unit_key)
//                ,getString(R.string.pref_temperature_unit_default));
//        new FetchWeatherTask().execute(postcode, temperatureUnit);

//      The initial version has been replaced by cursor loader and cursorAdapter
//        Intent intent = new Intent(getActivity(), SunshineService.class);
//        intent.putExtra("Location", location);
//        getActivity().startService(intent);
        Log.v("UPdateWeather", "Start update weather after 5 seconds.");
        Intent intent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
        intent.putExtra("Location", location);
        AlarmManager alarmManager = (AlarmManager)getActivity().getSystemService(getActivity().ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, 0);

        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 5 * 1000, pendingIntent);

        //new FetchWeatherTask(getActivity()).execute(location);
    }

    public void onLocationChanged(){
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    public void onTemperatureTypeChange(){
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

}