package uml_alerts.uml_alerts;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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


public class MainActivity extends ActionBarActivity {

    Button alerts_button, contacts_button, other_button, about_button;
    int backButtonCount = 0;

    Button sendBtn;
    EditText phoneNumber;
    EditText Message;

    // Menu items
    private static final int MENU_ALERTS = Menu.FIRST;
    private static final int MENU_CONTACTS = Menu.FIRST + 1;
    private static final int MENU_OTHER = Menu.FIRST + 2;
    private static final int MENU_ABOUT = Menu.FIRST + 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alerts_button = (Button) findViewById(R.id.button);
        alerts_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.alerts);
            }
        });

        contacts_button = (Button) findViewById(R.id.button2);
        contacts_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.contacts);
            }
        });

       other_button = (Button) findViewById(R.id.button3);
       other_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.othersettings);
            }
        });

        // About button
        about_button = (Button) findViewById(R.id.button4);
        about_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.about);
            }
        });

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
        super.onCreateOptionsMenu(menu);

        // Menu - need to make some icons!
        menu.add(0, MENU_ALERTS, Menu.NONE, "Alerts"); //.setIcon(android.R.drawable.ic_dialog_alert);
        menu.add(0, MENU_CONTACTS, Menu.NONE, "Contacts"); //.setIcon(android.R.drawable.ic_dialog_alert);
        menu.add(0, MENU_OTHER, Menu.NONE, "Other Settings"); //.setIcon(android.R.drawable.ic_dialog_alert);
        menu.add(0, MENU_ABOUT, Menu.NONE, "About"); //.setIcon(android.R.drawable.ic_dialog_alert);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);

//        super.onOptionsItemSelected(item);

        switch (id) {
            case MENU_ALERTS:
                //viewAbout();
                doSomething();
                break;
            case MENU_CONTACTS:
                //viewContacts();
                doSomething();
                break;
            case MENU_OTHER:
                //viewOtherSettings();
                doSomething();
                break;
            case MENU_ABOUT:
                //viewOtherSettings();
                doSomething();
                break;
        }
        return false;
    }

    public void doSomething() {
        Toast.makeText(this, "Testing 1 2 3", Toast.LENGTH_SHORT).show();
    }

    public void viewAbout(View view) {
        backButtonCount = 0;
        setContentView(R.layout.about);
    }

    public void viewAlerts(View view) {
        backButtonCount = 0;
        setContentView(R.layout.alerts);
    }

    public void viewContacts(View view) {
        backButtonCount = 0;
        setContentView(R.layout.contacts);
    }

    public void viewOtherSettings(View view) {
        backButtonCount = 0;
        setContentView(R.layout.othersettings);
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
