package uml_alerts.uml_alerts;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Alerts extends ActionBarActivity {

    // Map for the Alerts.
    // Key: Phone Number (in String form)
    // Value: Message
    Map<String, String> alerts_list = new HashMap<>();

    // ListView to display the map data.
    ListView alert_list;

    // Adapter for the ListView.
    SimpleAdapter list_adapter;

    // ArrayList for the ListView
    ArrayList<Map<String, String>> list;

    // Add Alert button
    Button addAlertButton;

    // User input variables from the dialog for entering a phone number / message
    String mMessage = "";
    String mPhone = "";

    // Menu items
    private static final int MENU_ALERTS = Menu.FIRST;
    private static final int MENU_CONTACTS = Menu.FIRST + 1;
    private static final int MENU_OTHER = Menu.FIRST + 2;
    private static final int MENU_ABOUT = Menu.FIRST + 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alerts);

        // ******************************************
        //  Below this is the code for the ListView.
        // ******************************************
        createListView();

        // ********************************
        //  Below this is the button code.
        // ********************************
        addAlertButton = (Button) findViewById(R.id.AddAlertButton);

        addAlertButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                AddAlert();
            }
        });
    }

    public void createListView() {
        // Get the list view from the XML file.
        alert_list = (ListView) findViewById(R.id.listView);

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
        list.clear();
        list = buildData();
        list_adapter.notifyDataSetChanged();
    }


    private ArrayList<Map<String, String>> buildData() {
        // This will at some point pull from the alerts_list map!
        // For now it is hard coded for testing purposes.

        ArrayList<Map<String, String>> list = new ArrayList<>();

        for (Map.Entry<String, String> entry : alerts_list.entrySet()) {
            list.add(putData(entry.getKey(), entry.getValue()));
        }

        list.add(putData("603-999-9999", "Help! I've fallen and I can't get up!"));
        list.add(putData("867-123-4567", "I'm being stalked! CALL THE POLICE!"));
        list.add(putData("999-999-1234", "Hi I'm leaving for a trip."));
        return list;
    }


    private HashMap<String, String> putData(String phone_number, String alert) {
        HashMap<String, String> item = new HashMap<>();
        item.put("phone_number", phone_number);
        item.put("alert", alert);
        return item;
    }


    public void AddAlert() {
        Log.i("Adding alert...", "");


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
                      }
                  }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        text_entry.show();

        // Add the phone number / message to the map of alerts.
        alerts_list.put(mPhone, mMessage);

        // Update the ListView.
        updateListView();
    }


    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

    }


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

        // Launches a new activity.
        Intent myIntent = new Intent(Alerts.this, About.class);
        //myIntent.putExtra("key", value); //Optional parameters
        Alerts.this.startActivity(myIntent);
    }

    // Launches the Alerts activity
    public void viewAlerts() {

        // Launches a new activity.
        Intent myIntent = new Intent(Alerts.this, Alerts.class);
        //myIntent.putExtra("key", value); //Optional parameters
        Alerts.this.startActivity(myIntent);
    }

    // Launches the Contacts activity
    public void viewContacts() {

        // Launches a new activity.
        Intent myIntent = new Intent(Alerts.this, Contacts.class);
        //myIntent.putExtra("key", value); //Optional parameters
        Alerts.this.startActivity(myIntent);
    }

    // Launches the Other Settings activity
    public void viewOtherSettings() {

        // Launches a new activity.
        Intent myIntent = new Intent(Alerts.this, OtherSettings.class);
        //myIntent.putExtra("key", value); //Optional parameters
        Alerts.this.startActivity(myIntent);
    }
}
