package uml_alerts.uml_alerts;

import android.content.Context;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.MessageFormat;




public class location extends ActionBarActivity {

    LocationManager mLocManager;
    LocationListener mLocListener;
    GpsStatus.Listener gpsStatusListener;
    Location lastLocation;
    long lastFix;
    boolean hasFix;

    // TextViews
    TextView tvLatitude;
    TextView tvLongitude;
    TextView tvAccuracy;

    final DecimalFormat accuracyFormat = new DecimalFormat("###.00");

    private Handler handler = new Handler();
    private Runnable updateUITask = new Runnable() {
        public void run() {
            updateDisplay();
            handler.postDelayed(this, 100);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location);

        final Context context = getApplicationContext();

        // Setup TextViews
        tvLatitude = (TextView)findViewById(R.id.latitudeText);
        tvLongitude = (TextView)findViewById(R.id.longitudeText);
        tvAccuracy = (TextView)findViewById(R.id.accuracyText);

        hasFix = false;

        mLocManager = (LocationManager)context.getSystemService(LOCATION_SERVICE);
        boolean isGPSEnabled = mLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (isGPSEnabled && mLocManager != null){

            // Create the LocationListener
            mLocListener = new LocationListener(){
                public void onLocationChanged(Location loc){
                    updateLocation(loc);
                }
                public void onStatusChanged(String provider, int status, Bundle extras) {}
                public void onProviderEnabled(String provider) {}
                public void onProviderDisabled(String provider) {}
            };

            // Create the GPS status listener
            gpsStatusListener = new GpsStatus.Listener(){
                public void onGpsStatusChanged(int event){
                    switch(event){
                        case GpsStatus.GPS_EVENT_STARTED:
                            System.out.println("GPS_EVENT_STARTED");
                            break;
                        case GpsStatus.GPS_EVENT_STOPPED:
                            System.out.println("GPS_EVENT_STOPPED");
                            break;
                        case GpsStatus.GPS_EVENT_FIRST_FIX:
                            System.out.println("GPS_EVENT_FIRST_FIX");
                            hasFix = true;
                            break;
                        case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                            System.out.println("GPS_EVENT_SATELLITE_STATUS");
                            if (lastLocation != null) {
                                hasFix = (System.currentTimeMillis() - lastFix) < 2000;
                            }

                            if (hasFix) {
                                System.out.println("HAS FIX");
                            }
                            else {
                                System.out.println("NO FIX");
                            }
                    }
                }
            };

            lastLocation = mLocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null){
                lastFix = lastLocation.getTime();
            }

            handler.post(updateUITask);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onStop(){
        super.onStop();
        mLocManager.removeUpdates(mLocListener);
        mLocManager.removeGpsStatusListener(gpsStatusListener);
    }

    @Override
    protected void onResume(){
        super.onResume();
        mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocListener);
        mLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocListener);
        mLocManager.addGpsStatusListener(gpsStatusListener);
    }

    public void shareLocation (View view){
        if (lastLocation == null){
            return;
        }

        String link = formatLocation("http://maps.google.com/?q={0},{1}", lastLocation);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, link);
        intent.setType("text/plain");
        startActivity(Intent.createChooser(intent, getResources().getText(R.string.send_via)));
    }

    public void viewLocation (View view){
        if (lastLocation == null){
            return;
        }

        String uri = formatLocation("geo:{0},{1}?q={0},{1}", lastLocation);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(Intent.createChooser(intent, getResources().getText(R.string.view_via)));
    }

    public void updateLocation(Location location){
        //TODO: Check if new location is 'better'
        lastLocation = location;
        lastFix = location.getTime();
        updateDisplay();
    }

    public void updateDisplay(){

        if (lastLocation != null){

            tvLatitude.setText("" + lastLocation.getLatitude());
            tvLongitude.setText("" + lastLocation.getLongitude());
            tvAccuracy.setText(accuracyFormat.format(lastLocation.getAccuracy()) + "m");
        }
        else{
            tvLatitude.setText("N/A");
            tvLongitude.setText("N/A");
            tvAccuracy.setText("N/A");
        }

    }

    private String formatLocation(String s, Location l){
        // Hack to get around MessageFormat precision weirdness
        return MessageFormat.format(s, "" + l.getLatitude(), "" + l.getLongitude(), "" + l.getAccuracy());
    }

}
