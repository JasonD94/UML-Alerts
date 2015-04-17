package uml_alerts.uml_alerts;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class MainActivity extends ActionBarActivity {
    int backButtonCount = 0;
    Button sendBtn;
    EditText phoneNumber;
    EditText Message;

    // Menu items
    private static final int MENU_ALERTS = Menu.FIRST;
    private static final int MENU_CONTACTS = Menu.FIRST + 1;
    private static final int MENU_OTHER = Menu.FIRST + 2;
    private static final int MENU_ABOUT = Menu.FIRST + 3;

    // Google Maps base URL
    private static final String maps_URL = "http://maps.google.com/maps?z=1&h=m&q=loc:";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // SMS stuff
        sendBtn = (Button) findViewById(R.id.sendSMS);
        phoneNumber = (EditText) findViewById(R.id.phoneNo);
        Message = (EditText) findViewById(R.id.sendMsg);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                sendSMSMessage();
            }
        });
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

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        backButtonCount = 0;
    }

    // Adds options to the menu at the top of the app.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Idea from:
        // http://stackoverflow.com/questions/17311833/how-we-can-add-menu-item-dynamically

        // Menu - need to make some icons!
        menu.add(0, MENU_ALERTS, Menu.NONE, "Alerts"); //.setIcon(android.R.drawable.ic_dialog_alert);
        menu.add(0, MENU_CONTACTS, Menu.NONE, "Contacts"); //.setIcon(android.R.drawable.ic_dialog_alert);
        menu.add(0, MENU_OTHER, Menu.NONE, "Other Settings"); //.setIcon(android.R.drawable.ic_dialog_alert);
        menu.add(0, MENU_ABOUT, Menu.NONE, "About"); //.setIcon(android.R.drawable.ic_dialog_alert);
        return true;
    }


    /**
     *      Sets up the menu bar items.
     *      Adds:
     *      1) Alerts link
     *      2) Contacts link
     *      3) Other Settings link
     *      4) About link
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case MENU_ALERTS:
                viewAlerts();
                break;
            case MENU_CONTACTS:
                viewContacts();
                break;
            case MENU_OTHER:
                viewOtherSettings();
                break;
            case MENU_ABOUT:
                viewAbout();
                break;
        }
        return false;
    }

    // Launches the About activity
    public void viewAbout() {
        backButtonCount = 0;
        // Launches a new activity.
        Intent myIntent = new Intent(MainActivity.this, About.class);
        //myIntent.putExtra("key", value); //Optional parameters
        MainActivity.this.startActivity(myIntent);
    }

    // Launches the Alerts activity
    public void viewAlerts() {
        // Launches a new activity.
        Intent myIntent = new Intent(MainActivity.this, Alerts.class);
        MainActivity.this.startActivity(myIntent);
    }

    // Launches the Contacts activity
    public void viewContacts() {
        backButtonCount = 0;
        // Launches a new activity.
        Intent myIntent = new Intent(MainActivity.this, Contacts.class);
        //myIntent.putExtra("key", value); //Optional parameters
        MainActivity.this.startActivity(myIntent);
    }

    // Launches the Other Settings activity
    public void viewOtherSettings() {
        backButtonCount = 0;
        // Launches a new activity.
        Intent myIntent = new Intent(MainActivity.this, OtherSettings.class);
        //myIntent.putExtra("key", value); //Optional parameters
        MainActivity.this.startActivity(myIntent);
    }

    /**
     * Back button listener.
     * Will close the application if the back button pressed twice.
     */
    public void onBackPressed()
    {
        setContentView(R.layout.activity_main);

        if(backButtonCount >= 1)
        {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
        {
            //Toast.makeText(this, "Press the back button once again to close the application.", Toast.LENGTH_SHORT).show();
            backButtonCount++;
        }
    }
}
