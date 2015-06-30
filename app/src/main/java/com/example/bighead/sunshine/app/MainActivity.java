package com.example.bighead.sunshine.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback{

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    //private static final String FORECASTFRAGMENT_TAG = ForecastFragment.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private static String mLocation;
    private static String mTemperatureUnit;
    private static boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG,"in onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//We don't add forcastFragment dynamically because we have already done it in XML.
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.container, new ForecastFragment(), FORECASTFRAGMENT_TAG)    //tag the fragment by FORECASTFRAGMENT_TAG
//                    .commit();                                      //then we can use findFragmentByTag(FORECASTFRAGMENT_TAG) to get it.
//        }

        //Record the current location.
        mLocation = Utility.getPreferredLocation(this);
        //Record the current temperature unit.
        mTemperatureUnit = PreferenceManager.getDefaultSharedPreferences(this)
                            .getString(this.getString(R.string.pref_temperature_unit_key),
                                        this.getString(R.string.pref_temperature_unit_default));
        if(findViewById(R.id.weather_detail_container) != null){
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            // we check savedInstanceState, because if we rotate the phone, the system saves the fragment state in the saved state bundle
            // and is smart enough to restore this state.
            if(savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.weather_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }

        }else{
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        //If we are in "TwoPane" mode, we use normal today layout in the forecastFragment's adapter of list view.
        ForecastFragment forecastFragment = (ForecastFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast);
        forecastFragment.setUseTodayLayout(!mTwoPane);

    }

    @Override
    protected void onStart() {
        Log.v(LOG_TAG,"in onStart");
        super.onStart();
        // The activity is about to become visible.
    }
    @Override
    protected void onResume() {
        Log.v(LOG_TAG,"in onResume");

        ForecastFragment ff = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
        //check whether the current location equals old location. if not, then update it by calling onLocationChanged.
        String locationTemp = Utility.getPreferredLocation(this);
        String temperatureUnitTemp = PreferenceManager.getDefaultSharedPreferences(this)
                                        .getString(this.getString(R.string.pref_temperature_unit_key),
                                                this.getString(R.string.pref_temperature_unit_default));
        if(locationTemp != null && !locationTemp.equals(mLocation)){
            if(ff == null){
                Log.e( LOG_TAG, "Can not find Forecastfragment by findFragmentById()");
            }else {
                ff.onLocationChanged();
            }

            DetailFragment df = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if(df == null){
                Log.e( LOG_TAG, "Can not find Detailfragment by findFragmentByTag()");
            }else{
                df.onLocationChanged(locationTemp);
            }

            mLocation = locationTemp;

        }else if(temperatureUnitTemp != null && !temperatureUnitTemp.equals(mTemperatureUnit)){
            ff.onTemperatureTypeChange();
            mTemperatureUnit = temperatureUnitTemp;
        }

        super.onResume();
        // The activity has become visible (it is now "resumed").
    }
    @Override
    protected void onPause() {
        Log.v(LOG_TAG,"in onPause");
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused").
    }
    @Override
    protected void onStop() {
        Log.v(LOG_TAG,"in onStop");
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
    }
    @Override
    protected void onDestroy() {
        Log.v(LOG_TAG,"in onDestroy");
        super.onDestroy();
        // The activity is about to be destroyed.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

           startActivity(new Intent(this, SettingsActivity.class));

            return true;
        }

        if(id == R.id.action_viewmap){

            openPreferredLocationInMap();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationInMap(){
        String location = Utility.getPreferredLocation(this);

        Uri buildUri = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", location)
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(buildUri);
        //check if there is existing tool or app corresponding to "ACTION_VIEW" type.
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        }
    }

    @Override
    public void onItemSelected(Uri dateUri) {
        if(mTwoPane){

            //DetailFragment df = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, dateUri);
            //args.putString("dateUri", dateUri.toString());

            DetailFragment df = new DetailFragment();
            df.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, df, DETAILFRAGMENT_TAG)
                    .commit();

        }else{
            Intent intent = new Intent(this, DetailActivity.class).setData(dateUri);
            startActivity(intent);
        }
    }
}
