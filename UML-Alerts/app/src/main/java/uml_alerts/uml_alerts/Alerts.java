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

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Alerts extends ActionBarActivity {

    // Const file name for the CSV file.
    private static final String CSV_FILE = "alerts.csv";

    // App Log Tag.
    private static final String APP_TAG = "UML ALERTS";

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
    private static final int MENU_REFRESH = Menu.FIRST;
    private static final int MENU_CONTACTS = Menu.FIRST + 1;
    private static final int MENU_OTHER = Menu.FIRST + 2;
    private static final int MENU_ABOUT = Menu.FIRST + 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alerts);

        // Logcat, one for every function / method.
        Log.v(APP_TAG, "Starting onCreate()...");

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

        try{
            // Try and open / read from the CSV file.
            OpenCSV();
        } catch(Exception e) {
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


    public void createListView() {
        Log.v("createListView", "Starting createListView()...");

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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        Log.v(APP_TAG, "Starting onCreateOptionsMenu()...");

        // Idea from:
        // http://stackoverflow.com/questions/17311833/how-we-can-add-menu-item-dynamically

        // Menu - need to make some icons!
        menu.add(0, MENU_REFRESH, Menu.NONE, "Refresh alerts"); //.setIcon(android.R.drawable.ic_dialog_alert);
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
        Log.v(APP_TAG, "Starting onOptionsItemSelected()...");

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case MENU_REFRESH:
                viewRefresh();
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
        Log.v(APP_TAG, "Starting viewAbout()...");

        // Launches a new activity.
        Intent myIntent = new Intent(Alerts.this, About.class);
        //myIntent.putExtra("key", value); //Optional parameters
        Alerts.this.startActivity(myIntent);
    }

    // Updates the ListView.
    public void viewRefresh() {
        Log.v(APP_TAG, "Starting viewRefresh()...");

        updateListView();
    }

    // Launches the Contacts activity
    public void viewContacts() {
        Log.v(APP_TAG, "Starting viewContacts()...");

        // Launches a new activity.
        Intent myIntent = new Intent(Alerts.this, Contacts.class);
        //myIntent.putExtra("key", value); //Optional parameters
        Alerts.this.startActivity(myIntent);
    }

    // Launches the Other Settings activity
    public void viewOtherSettings() {
        Log.v(APP_TAG, "Starting viewOtherSettings()...");

        // Launches a new activity.
        Intent myIntent = new Intent(Alerts.this, OtherSettings.class);
        //myIntent.putExtra("key", value); //Optional parameters
        Alerts.this.startActivity(myIntent);
    }
}


