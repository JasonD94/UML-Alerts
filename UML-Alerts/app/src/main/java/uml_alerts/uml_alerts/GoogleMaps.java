package uml_alerts.uml_alerts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class GoogleMaps extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {


    /**
     *      Top of this file includes member variables and default Android methods
     *
     */

    // Fragment managing the behaviors, interactions and presentation of the navigation drawer.
    private NavigationDrawerFragment mNavigationDrawerFragment;

    // Used to store the last screen title. For use in {@link #restoreActionBar()}.
    private CharSequence mTitle;
    GoogleMap googleMap;

    // App Log Tag.
    private static final String APP_TAG = "UML ALERTS";

    // Fix bug that launches alerts instead of Google Maps.
    private boolean first_boot = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_maps);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        createMapView();
        addMarker();
    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }


    /**
     *      Below this line is code for the map of alerts.
     *
     */


    /**
     * Initialises the mapview
     */
    private void createMapView(){
        /**
         * Catch the null pointer exception that
         * may be thrown when initialising the map
         */
        try {
            if(null == googleMap){
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.mapView)).getMap();

                /**
                 * If the map is still null after attempted initialisation,
                 * show an error to the user
                 */
                if(null == googleMap) {
                    Toast.makeText(getApplicationContext(),
                            "Error creating map", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NullPointerException exception){
            Log.e("mapApp", exception.toString());
        }
    }

    /**
     * Adds a marker to the map
     */
    private void addMarker(){

        /** Make sure that the map has been initialised **/
        if(null != googleMap){
            googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(0, 0))
                            .title("Marker")
                            .draggable(true)
            );
        }
    }



    /**
     *      Below this line is misc. stuff, mostly related to the sidebar.
     */

    // This is for the sidebar drawer
    // NOTE - IT DEFAULTS TO 0 + 1, OR TO POSITION 1!
    // That is what caused issues with loading the Google Maps view!
    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                // This sets up the alerts view.
                if(!first_boot) {
                    first_boot = true;
                } else {
                    mTitle = getString(R.string.title_section1);
                    launchAlerts();
                }
                break;
            case 2:
                // This sets up the contacts view.
                mTitle = getString(R.string.title_section2);
                launchAlerts();
                break;
            case 3:
                // This sets up the previous alerts view
                mTitle = getString(R.string.title_section3);
                launchAlerts();
                break;
            case 4:
                // This sets up the google maps view.
                mTitle = getString(R.string.title_section4);
                break;
            case 5:
                // This loads the about page.
                mTitle = getString(R.string.title_section5);
                viewAbout();
                break;
        }
    }


    // Launches alerts / contacts activity
    public void launchAlerts() {
        Log.v(APP_TAG, "Starting launchAlerts()");

        Intent myIntent = new Intent(GoogleMaps.this, MainActivity.class);
        startActivity(myIntent);
    }


    // Launches the About activity
    public void viewAbout() {
        Log.v(APP_TAG, "Starting viewAbout()...");

        // Launches a new activity.
        Intent myIntent = new Intent(GoogleMaps.this, About.class);
        startActivity(myIntent);
    }


    /**
     * Sets up the menu bar items.
     * Adds:
     * 1) Alerts link
     * 2) Contacts link
     * 3) Google Maps link
     * 4) About link
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((GoogleMaps) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    /** Adds options to the menu at the top of the app.
     * This currently isn't used, so it is commented out for now.
     */
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        if (!mNavigationDrawerFragment.isDrawerOpen()) {
//            // Only show items in the action bar relevant to this screen
//            // if the drawer is not showing. Otherwise, let the drawer
//            // decide what to show in the action bar.
//            getMenuInflater().inflate(R.menu.main, menu);
//            restoreActionBar();
//            return true;
//        }
//        return super.onCreateOptionsMenu(menu);
//    }

}
