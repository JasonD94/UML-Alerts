package uml_alerts.uml_alerts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * TODO:
 *
 * I THINK I FIGURED THIS OUT BY USING A CSV FILE
 * 1. Figure out how to save data to internal memory.
 *    This includes saving an entire map to storage, so that we can
 *    save the user's phone number / message combinations. These are essentially their
 *    "alerts"
 *
 * Still need to do this.
 * 2. Add onclick listeners to the listview items. Make it so if you click on one,
 *    it loads the "send SMS" dialog seen in the MainActivity. This should send the
 *    displayed message to the displayed phone number.
 *
 * Still need to do this.
 * 3. Instead of having the user enter a phone number, allow them to look up a name in their
 *    address book. Access the contacts book, and let them start typing a name, and display
 *    possible matches. They should be able to press on a name, and have it load that contact's
 *    number into the map, along with whatever message they set.
 *
 * Need to remove this.
 *  4. The contact's page would then be unnecessary. Remove it, plus the main activity.
 *     The user should then be sent directly the alerts page.
 *
 *  THIS SHOULD BE DONE:
 *  5. Add a Google Map page instead to display the user's current location (if GPS is on),
 *     or an approximate location (if network location is on).
 *     Along with the Google Map page, allow user's to send their current location to their
 *     alert contact. This should be a google maps link preferably.
 *
 *     Could use the following URL for that:
 *     https://www.google.com/maps/@42.8398932,-71.5070972
 *
 *     Where the first value is Longitude, the second value is Latitude.
 *
 *     Example message could be:
 *
 *     *ALERT TEXT*
 *     I am currently at the following GPS location:
 *     http://maps.google.com/maps?z=1&h=m&q=loc:42.653167+-71.325955
 *
 *     42.653167, -71.325955
 *
 *     See:
 *     http://stackoverflow.com/questions/2660201/what-parameters-should-i-use-in-a-google-maps-url-to-go-to-a-lat-lon
 *     For Google Maps URL details.
 *
 *     http://maps.google.com/maps?z=1&h=m&q=loc:
 *     42.654802, -71.326363
 *
 *  6.
 *
 *
 */

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    Button sendBtn;
    EditText phoneNumber;
    EditText Message;

    // Google Maps base URL
    private static final String maps_URL = "http://maps.google.com/maps?z=1&h=m&q=loc:";

    // Const file name for the CSV file.
    private static final String CSV_FILE = "alerts.csv";

    // App Log Tag.
    private static final String APP_TAG = "UML ALERTS";

    // Map for the Alerts.
    // Key: Phone Number (in String form)
    // Value: Message
    Map<String, String> alerts_list = new HashMap<>();

    // Map for the contacts
    // Key: Name
    // Value: Phone Number
    ArrayList<HashMap<String,String>> contactData;

    // ListView to display the alerts.
    ListView alert_list;

    // ListView to display the contacts
    ListView contacts_list;

    // Adapter for the ListView.
    SimpleAdapter list_adapter;

    // ArrayList for the ListView
    ArrayList<Map<String, String>> list;

    // Add Alert button
    Button addAlertButton;

    // User input variables from the dialog for entering a phone number / message
    String mMessage = "";
    String mPhone = "";


    // Open data from a CSV file, save to the map.
    public void OpenCSV() throws Exception {
        // Opening CSV file log.
        Log.v(APP_TAG, "Starting OpenCSV()...");

        // Get path for storing / accessing the CSV file.
        String csv_path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + CSV_FILE;

        // Open data from the CSV file
        String line[];

        // Build an instance of the CSVReader class. Give it the CSV file's name.
        CSVReader reader = new CSVReader(new FileReader(csv_path));

        // Read all the data off the CSV file.
        line = reader.readNext();

        while(line != null) {
            // This gets the line and splits it based on the comma.
            List<String> container = Arrays.asList(line);

            // This gets the key / value from the List container.
            String key = container.get(0);
            String value = container.get(1);

            // Debugging
            Log.v(APP_TAG, "Key is: ");
            Log.v(APP_TAG, key);

            Log.v(APP_TAG, "Value is: ");
            Log.v(APP_TAG, value);

            // Add to the map.
            alerts_list.put(key, value);

            // Get the next line.
            line = reader.readNext();
        }

        // Now update the alerts view.
        updateListView();
    }


    /*
     * Paramterized method to sort Map e.g. HashMap or Hashtable in Java
     * throw NullPointerException if Map contains null key
     */
    public static <K extends Comparable,V extends Comparable> Map<K,V> sortByKeys(Map<K,V> map){
        List<K> keys = new LinkedList<K>(map.keySet());
        Collections.sort(keys);

        //LinkedHashMap will keep the keys in the order they are inserted
        //which is currently sorted on natural ordering
        Map<K,V> sortedMap = new LinkedHashMap<K,V>();
        for(K key: keys){
            sortedMap.put(key, map.get(key));
        }

        return sortedMap;
    }



    public void createListView() {
        Log.v("createListView", "Starting createListView()...");

        // Get the list view from the XML file.
        alert_list = (ListView) findViewById(R.id.listView);

        // Sort the alerts.
        alerts_list = sortByKeys(alerts_list);

        // Create a list of two items.
        // One is the user's phone number.
        // The other is the alert message.
        list = buildData();

        // From is a list of Key's.
        // to is an array of IDs for the strings.
        String[] from = { "phone_number", "alert" };
        int[] to = { android.R.id.text1, android.R.id.text2 };

        // Simple adapter is all we need for this.
        list_adapter = new SimpleAdapter(this, list, android.R.layout.simple_list_item_2, from, to);
        alert_list.setAdapter(list_adapter);
    }

    public void updateListView() {
        Log.v(APP_TAG, "Starting updateListView()...");
        createListView();
    }


    private ArrayList<Map<String, String>> buildData() {
        // This will at some point pull from the alerts_list map!
        // For now it is hard coded for testing purposes.
        Log.v(APP_TAG, "Starting buildData()...");

        ArrayList<Map<String, String>> tmp_list = new ArrayList<>();

        for (Map.Entry<String, String> entry : alerts_list.entrySet()) {
            tmp_list.add(putData(entry.getKey(), entry.getValue()));
        }

        // These are defaults, just for testing!
//        tmp_list.add(putData("603-999-9999", "Help! I've fallen and I can't get up!"));
//        tmp_list.add(putData("867-123-4567", "I'm being stalked! CALL THE POLICE!"));
        return tmp_list;
    }


    private HashMap<String, String> putData(String phone_number, String alert) {
        Log.v(APP_TAG, "Starting putData()...");

        HashMap<String, String> item = new HashMap<>();
        item.put("phone_number", phone_number);
        item.put("alert", alert);
        return item;
    }


    public void AddAlert() {
        Log.i(APP_TAG, "Starting AddAlert()...");

        // Text entry dialog.
        AlertDialog.Builder text_entry = new AlertDialog.Builder(this);

        String Title = "Add A New Alert!";
        String Message = "Please add a number and a message for this alert.";

        // Edit text to get user input from.
        final EditText number_input = new EditText(this);
        number_input.setHint("Enter your phone number here.");
        number_input.setInputType(InputType.TYPE_CLASS_NUMBER);

        final EditText msg_input = new EditText(this);
        msg_input.setHint("Enter your message here.");

        // Create a layout for the two input strings.
        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(number_input);
        lay.addView(msg_input);
        text_entry.setView(lay);

        // Set the title / message / positive & negative buttons.
        text_entry.setTitle(Title)
                .setMessage(Message)
                .setPositiveButton("Done.", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mPhone = number_input.getText().toString();
                        mMessage = msg_input.getText().toString();

                        // Add the phone number / message to the map of alerts.
                        alerts_list.put(mPhone, mMessage);

                        // Update the ListView.
                        updateListView();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        // Display the dialog
        text_entry.show();
    }


    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        Log.v(APP_TAG, "Starting onResume()...");

        getContacts();

        try{
            // Try and open / read from the CSV file.
            OpenCSV();
        } catch(Exception e) {
            // Do stuff with the exception.
            Log.v(APP_TAG, "Couldn't open CSV file!", e);
        }
    }


    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first

        Log.v(APP_TAG, "Starting onPause()...");

        // Call the SaveCSV function to save the map data to a CSV file.
        try {
            SaveCSV();
        } catch(Exception e) {
            // Write exception to logcat.
            Log.v(APP_TAG, "Error! Couldn't save file to CSV!", e);
        }
    }

    public void SaveCSV() throws Exception {
        // Opening CSV file log.
        Log.v(APP_TAG, "Starting SaveCSV()...");

        // Get path for storing / accessing the CSV file.
        String csv_path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + CSV_FILE;

        // Create an instance of the CSVWriter class. Give it the CSV file's name.
        CSVWriter writer = new CSVWriter(new FileWriter(csv_path));

        // Try saving just one thing for the time being. A simple # and Msg.
//        String[] alert = "6034930229,Please help I'm being stalked by an angry man!".split(",");

        // While we've got a valid thing in the map.
        for(Map.Entry<String, String> entry : alerts_list.entrySet()) {
            // Now pair will have a key / value that we can save.
            String key = entry.getKey();
            String value = entry.getValue();
            String next = key + "," + value;

            String[] cur_alert = next.split(",");
            writer.writeNext(cur_alert);
        }

//        // Write the testing alert to file.
//        writer.writeNext(alert);

        // Close the writer.
        writer.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // Logcat, one for every function / method.
        Log.v(APP_TAG, "Starting onCreate()...");

        // ******************************************
        //  Below this is the code for the ListView.
        // ******************************************
        createListView();

        // Get the contact data into the ArrayList.
        getContacts();

        // ********************************
        //  Below this is the button code.
        // ********************************
        addAlertButton = (Button) findViewById(R.id.AddAlertButton);

        addAlertButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                AddAlert();
            }
        });

        try{
            // Try and open / read from the CSV file.
            OpenCSV();
        } catch(Exception e) {
            // Do stuff with the exception.
            Log.v(APP_TAG, "Couldn't open CSV file!", e);
        }

        // SMS stuff
//        sendBtn = (Button) findViewById(R.id.sendSMS);
//        phoneNumber = (EditText) findViewById(R.id.phoneNo);
//        Message = (EditText) findViewById(R.id.sendMsg);

//        sendBtn.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                sendSMSMessage();
//            }
//        });
    }

    // Adds all the alerts to the list view.
    private void createAlerts() {
        createListView();
    }

    // Adds all the user's contacts to the list view.
    private void createContacts() {
        Log.v("createContacts", "Starting createContacts()...");

        // Get the list view from the XML file.
        contacts_list = (ListView) findViewById(R.id.listView);

        // From is a list of Key's.
        // to is an array of IDs for the strings.
        String[] from = { "name", "number" };
        int[] to = { android.R.id.text1, android.R.id.text2 };



        // Simple adapter is all we need for this.
        list_adapter = new SimpleAdapter(this, contactData, android.R.layout.simple_list_item_2, from, to);
        alert_list.setAdapter(list_adapter);
    }

    // Pulls data from the user's contacts book into the contacts map.
    public void getContacts() {
        Log.v("getContacts()", "Starting getContacts()...");

        // This code is for displaying contacts.
        // We should in fact search through contacts and save their number in the map instead.
        // May need to make an array of numbers though if more than one.
        // For now deal with just SMS messages so just one number per alert.

        contactData = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);
        while (cursor.moveToNext()) {
            try{
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor phones = getContentResolver().query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId, null, null);
                    while (phones.moveToNext()) {
                        String phoneNumber = phones.getString(phones.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER));
                        HashMap<String,String> map=new HashMap<>();
                        map.put("name", name);
                        map.put("number", phoneNumber);
                        contactData.add(map);
                    }
                    phones.close();
                }
            }
            catch(Exception e){

            }
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                // This sets up the alerts view.
                mTitle = getString(R.string.title_section1);
                createAlerts();
                break;
            case 2:
                // This sets up the contacts view.
                mTitle = getString(R.string.title_section2);
                createContacts();
                break;
            case 3:
                // This should load the about page.
                mTitle = getString(R.string.title_section3);
                viewAbout();
                break;
        }
    }


    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    // Location related functions
    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location);

        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status,
                                    Bundle extras) {}
    };

    private void updateWithNewLocation(Location location)
    {
        String latLongString = "No location found";
        if (location != null) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            latLongString = "Lat:" + lat + "\nLong:" + lng;
        }
        Log.v("UML ALERTS", latLongString);
    }

    /**
     *      Activity that sends an SMS message to a given phone number.
     *      Needs to have a phone number & message set to be successful.
     */
    protected void sendSMSMessage() {
        Log.i("Send SMS method", "");

        AlertDialog.Builder sms = new AlertDialog.Builder(this);
        sms.setTitle("Send the message?");
        sms.setMessage("Do you really want to send the SMS?");
        sms.setIcon(android.R.drawable.ic_dialog_alert);
        sms.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                Toast.makeText(MainActivity.this, "Sending SMS", Toast.LENGTH_SHORT).show();

                String phoneNo = phoneNumber.getText().toString();
                String message = Message.getText().toString();

                // Append a google maps URL to the message with a set location (for now)
                // Pre-set to Olsen Hall, or 42.654802, -71.326363
                double longitude = 42.654802;
                double latitude = -71.326363;

                // Get location - simplest way, all we want is lat and long!
                LocationManager locationManager;
                String svcName = Context.LOCATION_SERVICE;
                locationManager = (LocationManager)getSystemService(svcName);

                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setAltitudeRequired(false);
                criteria.setBearingRequired(false);
                criteria.setSpeedRequired(false);
                criteria.setCostAllowed(true);
                //String provider = locationManager.getBestProvider(criteria, true);

                Location location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
//                Location location = locationManager.getLastKnownLocation(provider);

                updateWithNewLocation(location);
//
//                locationManager.requestLocationUpdates(provider, 2000, 10, locationListener);

                try {
                    latitude = location.getLatitude ();
                    longitude = location.getLongitude ();
                }
                catch (NullPointerException e){
                    Log.v("UML ALERTS", "Couldn't get location. :(", e);
                }

                // Message for the user explaining why there's a google maps URL at the bottom.
                String location_msg = "\nMy current location is: ";

                String maps = maps_URL + Double.toString(latitude) + "," + Double.toString(longitude);
                message += location_msg + maps;

                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNo, null, message, null, null);
                    Toast.makeText(getApplicationContext(), "SMS sent.",
                            Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),
                            "SMS failed, please try again.",
                            Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }


            }});
        sms.setNegativeButton(android.R.string.no, null);

        AlertDialog dialog = sms.create();
        dialog.show();
    }

    // Adds options to the menu at the top of the app.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }


    /**
     *      Sets up the menu bar items.
     *      Adds:
     *      1) Alerts link
     *      2) Contacts link
     *      3) About link
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    // Launches the About activity
    public void viewAbout() {
        Log.v(APP_TAG, "Starting viewAbout()...");

        // Launches a new activity.
        Intent myIntent = new Intent(MainActivity.this, About.class);
        //myIntent.putExtra("key", value); //Optional parameters
        MainActivity.this.startActivity(myIntent);
    }
}
