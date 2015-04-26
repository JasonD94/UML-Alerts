package uml_alerts.uml_alerts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * TODO:
 *
 * 1. Instead of having the user enter a phone number, allow them to look up a name in their
 *    address book. Access the contacts book, and let them start typing a name, and display
 *    possible matches. They should be able to press on a name, and have it load that contact's
 *    number into the map, along with whatever message they set.
 *
 */

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    // Used to store the last screen title. For use in {@link #restoreActionBar()}.
    private CharSequence mTitle;

    // Google Maps base URL
    private static final String maps_URL = "http://maps.google.com/maps?z=1&h=m&q=loc:";

    // Const file name for the CSV file.
    private static final String CSV_FILE = "alerts.csv";

    // App Log Tag.
    private static final String APP_TAG = "UML ALERTS";

    // Map for the Alerts.
    // Key: Phone Number (in String form) - soon to be separated by dots for other numbers.
    // Value: Message
    Map<String, String> alerts_list = new HashMap<>();

    // Map for the contacts
    // Key: Name
    // Value: Phone Number
    ArrayList<HashMap<String, String>> contactData;

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


    /**
     *      Android related methods are at the top.
     *      Other app related methods are below the Android stuff.
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Logcat, one for every function / method.
        Log.v(APP_TAG, "Starting onCreate()...");

        // Fragment managing the behaviors, interactions and presentation of the navigation drawer.
        NavigationDrawerFragment mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // Loads the ListView up with alerts.
        createAlerts();

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

        try {
            // Try and open / read from the CSV file.
            OpenCSV();
        } catch (Exception e) {
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
        } catch (Exception e) {
            // Write exception to logcat.
            Log.v(APP_TAG, "Error! Couldn't save file to CSV!", e);
        }
    }


    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        Log.v(APP_TAG, "Starting onResume()...");

        getContacts();

        try {
            // Try and open / read from the CSV file.
            OpenCSV();
        } catch (Exception e) {
            // Do stuff with the exception.
            Log.v(APP_TAG, "Couldn't open CSV file!", e);
        }
    }


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

        while (line != null) {
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


    // Read from CSV file into local map variable.
    public void SaveCSV() throws Exception {
        // Opening CSV file log.
        Log.v(APP_TAG, "Starting SaveCSV()...");

        // Get path for storing / accessing the CSV file.
        String csv_path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + CSV_FILE;

        // Create an instance of the CSVWriter class. Give it the CSV file's name.
        CSVWriter writer = new CSVWriter(new FileWriter(csv_path));

        // While we've got a valid thing in the map.
        for (Map.Entry<String, String> entry : alerts_list.entrySet()) {
            // Now pair will have a key / value that we can save.
            String key = entry.getKey();
            String value = entry.getValue();
            String next = key + "," + value;

            String[] cur_alert = next.split(",");
            writer.writeNext(cur_alert);
        }

        // Close the writer.
        writer.close();
    }


    // This builds up the alerts ArrayList
    private ArrayList<Map<String, String>> buildData() {
        // This will at some point pull from the alerts_list map!
        // For now it is hard coded for testing purposes.
        Log.v(APP_TAG, "Starting buildData()...");

        ArrayList<Map<String, String>> tmp_list = new ArrayList<>();

        for (Map.Entry<String, String> entry : alerts_list.entrySet()) {
            tmp_list.add(putData(entry.getKey(), entry.getValue()));
        }

        return tmp_list;
    }


    // This is used by the above buildData as a helper method to generate the ArrayList
    // from the alerts map.
    private HashMap<String, String> putData(String phone_number, String alert) {
        Log.v(APP_TAG, "Starting putData()...");

        HashMap<String, String> item = new HashMap<>();
        item.put("phone_number", phone_number);
        item.put("alert", alert);
        return item;
    }


    // Technically this just redoes the list view stuff.
    // Don't really need this method if we're going to do it the lazy way.
    public void updateListView() {
        Log.v(APP_TAG, "Starting updateListView()...");
        createAlerts();
    }


    // Adds all the alerts to the list view.
    private void createAlerts() {
        Log.v("createListView", "Starting createListView()...");

        // Get the list view from the XML file.
        alert_list = (ListView) findViewById(R.id.listView);

        // Onclick listener for the alerts.
        alert_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Get the phone number(s) and the alert from the ListView
                Object obj = (alert_list.getItemAtPosition(position));
                HashMap<String, String> item = (HashMap<String, String>) obj;
                String phoneNumber = "TEST";
                String alert = "ALERT";

                phoneNumber = item.get("phone_number");
                alert = item.get("alert");

                sendSMSMessage(phoneNumber, alert);
            }
        });

        // Long click listener for deleting alerts
        alert_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the phone number(s) from the ListView
                Object obj = (alert_list.getItemAtPosition(position));
                HashMap<String, String> item = (HashMap<String, String>) obj;
                String phoneNumber = item.get("phone_number");

                Log.v("Deleting phone number: ", phoneNumber);

                // Delete the alert from the map.
                alerts_list.values().removeAll(Collections.singleton(phoneNumber));

                // Redraw the ListView
                createAlerts();
                return false;
            }
        });

        // Setup the Add alerts button.
        addAlertButton = (Button) findViewById(R.id.AddAlertButton);
        addAlertButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                AddAlert();
            }
        });

        // Sort the alerts.
        alerts_list = sortByKeys(alerts_list);

        // Create a list of two items.
        // One is the user's phone number.
        // The other is the alert message.
        list = buildData();

        // From is a list of Key's.
        // to is an array of IDs for the strings.
        String[] from = {"phone_number", "alert"};
        int[] to = {android.R.id.text1, android.R.id.text2};

        // Simple adapter is all we need for this.
        list_adapter = new SimpleAdapter(this, list, android.R.layout.simple_list_item_2, from, to);
        alert_list.setAdapter(list_adapter);
    }


    // Adds all the user's contacts to the list view.
    private void createContacts() {
        Log.v("createContacts", "Starting createContacts()...");

        // Get the list view from the XML file.
        contacts_list = (ListView) findViewById(R.id.listView);

        // Onclick listener for the contacts
        contacts_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // This does nothing because it shouldn't do anything at all.
            }
        });

        // Make the "add new alert button" actually just pop up the add new contact page.
        // Setup the Add alerts button.
        addAlertButton = (Button) findViewById(R.id.AddAlertButton);
        addAlertButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                AddContact();
            }
        });


        // From is a list of Key's.
        // to is an array of IDs for the strings.
        String[] from = {"name", "number"};
        int[] to = {android.R.id.text1, android.R.id.text2};

        // Sort the contacts in order.
        Collections.sort(contactData, new MapComparator("name"));

        // Simple adapter is all we need for this.
        list_adapter = new SimpleAdapter(this, contactData, android.R.layout.simple_list_item_2, from, to);
        contacts_list.setAdapter(list_adapter);
    }


    // Pulls data from the user's contacts book into the contacts map.
    public void getContacts() {
        Log.v("getContacts()", "Starting getContacts()...");

        // This code is for displaying contacts.
        // We should in fact search through contacts and save their number in the map instead.
        // May need to make an array of numbers though if more than one.
        // For now deal with just SMS messages so just one number per alert.

        contactData = new ArrayList<>();
        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            try {
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                    while (phones.moveToNext()) {
                        String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        HashMap<String, String> map = new HashMap<>();
                        map.put("name", name);
                        map.put("number", phoneNumber);
                        contactData.add(map);
                    }
                    phones.close();
                }
            } catch (Exception e) {

            }
        }
        cursor.close();
    }


    // This alerts an alert to the map, and onPause saves the map data to a csv file.
    // Makes a simple alert dialog with its own view stuff.
    public void AddAlert() {
        Log.i(APP_TAG, "Starting AddAlert()...");

        // Text entry dialog.
        AlertDialog.Builder text_entry = new AlertDialog.Builder(this);

        String result_number;

        TextView Title = new TextView(this);
        Title.setText("Add New Alert");
        Title.setTextAppearance(this, android.R.style.TextAppearance_Large);
        Title.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView Msg = new TextView(this);
        Msg.setText("Please add a contact(s) and a message for this alert.");
        Msg.setGravity(Gravity.CENTER_HORIZONTAL);

        // ** Display a list of contacts that we want to select from. **
        // Get the list view from the XML file.
        final ListView contact_list = new ListView(this);

        // From is a list of Key's.
        // to is an array of IDs for the strings.
        String[] from = {"name", "number"};
        int[] to = {android.R.id.text1, android.R.id.text2};

        // Sort the contacts in order.
        Collections.sort(contactData, new MapComparator("name"));

        // Simple adapter is all we need for this.
        list_adapter = new SimpleAdapter(this, contactData, android.R.layout.simple_list_item_multiple_choice, from, to);

        // Allow multiple choices for the list view.
        // See the following site for help:
        // http://theopentutorials.com/tutorials/android/listview/android-multiple-selection-listview/
        contact_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        contact_list.setAdapter(list_adapter);

        // Get the message
        final EditText msg_input = new EditText(this);
        msg_input.setHint("Enter your message here.");
        msg_input.setGravity(Gravity.LEFT);

        // Create a layout for the title, about and two input strings.
        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(Title);
        lay.addView(Msg);
        lay.addView(msg_input);
        lay.addView(contact_list);
        text_entry.setView(lay);

        // Set the title / message / positive & negative buttons.
        text_entry.setPositiveButton("Done.", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                // We need to get a list of the people we selected above.
                SparseBooleanArray checked = contact_list.getCheckedItemPositions();
                ArrayList<String> selectedItems = new ArrayList<>();
                for (int i = 0; i < checked.size(); i++) {
                    // Item position in adapter
                    int position = checked.keyAt(i);
                    // Add sport if it is checked i.e.) == TRUE!
                    if (checked.valueAt(i)) {
                        Object obj = (contact_list.getItemAtPosition(position));


                        //Object obj = parent.getItemAtPosition(position);
                        HashMap<String, String> item = (HashMap<String, String>) obj;
                        //String name = item.get("name");
                        String phoneNumber = item.get("number");
//                        String contact = name + " " + phoneNumber;

                        // Add the current number to the ArrayList.
                        selectedItems.add(phoneNumber); //contact);
                    }
                }

                // Now that we have an array list of strings, we can create the
                // one string that will save all the phone numbers separated by dots. (".")
                StringBuilder builder = new StringBuilder((""));
                for(String val : selectedItems) {
                    builder.append(val).append("\n");
                }
                mPhone = builder.toString();

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


    // Simple function for (+) button.
    public void AddContact() {
        Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
        startActivity(intent);
    }


    // This is for the sidebar drawer
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }


    // This is for the sidebar drawer
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
                // This loads the about page.
                mTitle = getString(R.string.title_section3);
                viewAbout();
                break;
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


    // Returns a location or something.
    private void updateWithNewLocation(Location location) {
        String latLongString = "No location found";
        if (location != null) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            latLongString = "Lat:" + lat + "\nLong:" + lng;
        }
        Log.v("UML ALERTS", latLongString);
    }


    /**
     *      Method that sends an SMS message to a given phone number.
     *      Needs to have a phone number & message set to be successful.
     *
     *      In the future: multiple numbers will be separated by a "." and will send
     *      separate SMS messages to those numbers.
     */
    protected void sendSMSMessage(final String _phoneNo, final String _message) {
        Log.i("Send SMS method", "");

        AlertDialog.Builder sms = new AlertDialog.Builder(this);
        sms.setTitle("Send the message?");
        sms.setMessage("Do you really want to send the SMS?");
        sms.setIcon(android.R.drawable.ic_dialog_alert);
        sms.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                Toast.makeText(MainActivity.this, "Sending Alert(s)", Toast.LENGTH_SHORT).show();

                // Parse the phone number string for (possibly) multiple phone numbers,
                // separated by newlines ("\n")
                List<String> numbers= new ArrayList<>();
                numbers = Arrays.asList(_phoneNo.split("\n"));

                for(String num : numbers) {
                    Log.v("Sending SMS Alert to: ", num);

                    String phoneNo = num;
                    String message = _message;

                    /**
                     *      THE LOCATION STUFF HERE IS PROBABLY BROKEN,
                     *      USE UDIT'S FIXED LOCATION STUFF...
                     *
                     */

                    // Append a google maps URL to the message with a set location (for now)
                    // Pre-set to Olsen Hall, or 42.654802, -71.326363
                    double latitude = 42.654802;
                    double longitude = -71.326363;

                    // Get location - simplest way, all we want is lat and long!
                    LocationManager locationManager;
                    String svcName = Context.LOCATION_SERVICE;
                    locationManager = (LocationManager) getSystemService(svcName);

                    Criteria criteria = new Criteria();
                    criteria.setAccuracy(Criteria.ACCURACY_FINE);
                    criteria.setAltitudeRequired(false);
                    criteria.setBearingRequired(false);
                    criteria.setSpeedRequired(false);
                    criteria.setCostAllowed(true);

                    Location location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
                    updateWithNewLocation(location);

                    try {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    } catch (NullPointerException e) {
                        Log.v("UML ALERTS", "Couldn't get location. :(", e);
                    }

                    // Message for the user explaining why there's a google maps URL at the bottom.
                    String location_msg = "\nMy current location is: ";

                    String maps = maps_URL + Double.toString(latitude) + "," + Double.toString(longitude);
                    message += location_msg + maps;

                    try {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(phoneNo, null, message, null, null);
                        Toast.makeText(getApplicationContext(), "SMS sent to: " + phoneNo,
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(),
                                "SMS failed, please try again.",
                                Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
        });
        sms.setNegativeButton(android.R.string.no, null);

        AlertDialog dialog = sms.create();
        dialog.show();
    }

    // Adds options to the menu at the top of the app.
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


    /**
     * Sets up the menu bar items.
     * Adds:
     * 1) Alerts link
     * 2) Contacts link
     * 3) About link
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
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }


    // Sorts a HashMap
    public static <K extends Comparable, V extends Comparable> Map<K, V> sortByKeys(Map<K, V> map) {
        List<K> keys = new LinkedList<K>(map.keySet());
        Collections.sort(keys);

        //LinkedHashMap will keep the keys in the order they are inserted
        //which is currently sorted on natural ordering
        Map<K, V> sortedMap = new LinkedHashMap<>();
        for (K key : keys) {
            sortedMap.put(key, map.get(key));
        }

        return sortedMap;
    }
}

// Used by the sort HashMap function above.
class MapComparator implements Comparator<Map<String, String>> {
    private final String key;

    public MapComparator(String key) {
        this.key = key;
    }

    public int compare(Map<String, String> first, Map<String, String> second) {
        String firstValue = first.get(key);
        String secondValue = second.get(key);
        return firstValue.compareTo(secondValue);
    }
}