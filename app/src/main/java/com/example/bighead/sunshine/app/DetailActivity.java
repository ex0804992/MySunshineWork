package com.example.bighead.sunshine.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class DetailActivity extends ActionBarActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
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

            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment {

        private ShareActionProvider mshareActionProvider;
        private String mforecastStr;
        private static final String FORCAST_SHARE_HASHTAG = " #SunshineApp";
        private static final String LOG_TAG = DetailFragment.class.getSimpleName();

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        private Intent createShareForcastIntent(){

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, mforecastStr+FORCAST_SHARE_HASHTAG);
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
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            Intent intent = getActivity().getIntent();
            if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT)){
                mforecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);
                TextView textView = (TextView)rootView.findViewById(R.id.detail_text_view);
                textView.setText(mforecastStr);
            }


            return rootView;
        }
    }
}
