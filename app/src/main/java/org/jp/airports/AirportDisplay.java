package org.jp.airports;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by John on 3/21/2016.
 */
public class AirportDisplay extends AppCompatActivity implements LocationListener {
    Airport airport = null;
    LocationManager locationManager;
    Location location = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        Intent intent = getIntent();
        String airportID = intent.getStringExtra(AirportSearch.EXTRA_MESSAGE);
        AirportsDB db = AirportsDB.getInstance();
        this.airport = db.getAirport(airportID);

        String latlon = String.format("(%.3f, %.3f)", airport.lat, airport.lon);
        String wmmvar = String.format("%.1f", airport.getWMMMagneticDeclination());

        setText(R.id.AirportID, airport.id);
        setText(R.id.AirportName, airport.name);
        setText(R.id.AirportCity, airport.city);
        setText(R.id.AirportState, airport.state);
        setText(R.id.AirportElev, airport.elev, " ft", R.id.ElevRow);
        setText(R.id.AirportLatLon, latlon);
        setText(R.id.AirportRwy, airport.rwy, " ft", R.id.RunwayRow);
        setText(R.id.AirportVar, airport.var, "°", R.id.VarRow);
        setText(R.id.AirportWMMDec, wmmvar, "°", R.id.AirportWMMDecRow);
        displayLocationParams();
    }

    private void displayLocationParams() {
        String dist = (airport.dist >= 0.0) ?
                String.format("%.1f", airport.dist)
                : null;
        String trueBrng = (airport.dist >= 0.0) ?
                String.format("%03.0f", airport.trueBrng)
                : null;
        String magBrng = (airport.dist >= 0.0) ?
                String.format("%03.0f", airport.magBrng)
                : null;

        setText(R.id.AirportDist, dist, " nm", R.id.DistRow);
        setText(R.id.AirportTrueBearing, trueBrng, "°", R.id.TrueBearingRow);
        setText(R.id.AirportMagBearing, magBrng, "°", R.id.MagBearingRow);

        String latlon = (location != null) ?
                String.format("(%.3f, %.3f)", location.getLatitude(), location.getLongitude())
                : null;
        String wmmdec =  (location != null) ?
                String.format("%.1f", Airport.getWMMMagneticDeclination(location))
                : null;
        String altitude =  (location != null) ?
                String.format("%.0f", location.getAltitude() * 39.37/12)
                : null;

        View titleRow = (View)findViewById(R.id.CurrentLocationTitleRow);
        if (location == null) {
            titleRow.setVisibility(View.GONE);
        }
        else {
            titleRow.setVisibility(View.VISIBLE);
        }
        setText(R.id.CurrentLocationLatLon, latlon, "", R.id.CurrentLocationLatLonRow);
        setText(R.id.CurrentLocationWMMDec, wmmdec, "°", R.id.CurrentLocationWMMDecRow);
        setText(R.id.CurrentLocationAltitude, altitude, " ft", R.id.CurrentLocationAltitudeRow);
    }

    private void setText(int id, String text, String units, int rowID) {
        View row = (View)findViewById(rowID);
        if ((text == null) || text.trim().equals("")) {
            row.setVisibility(View.GONE);
        }
        else {
            setText(id, text + units);
            row.setVisibility(View.VISIBLE);
        }
    }

    private void setText(int id, String text) {
        TextView view = (TextView) findViewById(id);
        view.setText(text);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    3000,   // 3 sec
                    10, //10 meter minimum change
                    this);  //LocationListener
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    public void onLocationChanged(Location location) {
        if (location != null) {
            this.location = location;
            if (airport != null) {
                airport.setDistanceFrom(location);
                displayLocationParams();
            }
        }
    }
    public void onProviderDisabled(String provider) { }
    public void onProviderEnabled(String provider) { }
    public void onStatusChanged(String provider, int x, Bundle bundle) { }

    public void startMap(View view) {
        String uri = String.format("geo:%.4f,%.4f?z=14", airport.lat, airport.lon);
        Uri intentUri = Uri.parse(uri);
        Intent intent = new Intent(Intent.ACTION_VIEW, intentUri);
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
        else Toast.makeText(getApplicationContext(),
                "The Map application is unavailable", Toast.LENGTH_LONG).show();
    }

    public void startEarth(View view) {
        String uri = String.format("geo:%.4f,%.4f?z=15", airport.lat, airport.lon);
        Uri intentUri = Uri.parse(uri);
        Intent intent = new Intent(Intent.ACTION_VIEW, intentUri);
        intent.setPackage("com.google.earth");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
        else Toast.makeText(getApplicationContext(),
                "The Earth application is unavailable", Toast.LENGTH_LONG).show();
    }

    public void startBrowser(View view) {
        Uri uri = Uri.parse("http://www.airnav.com/airport/" + airport.id);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
        else Toast.makeText(getApplicationContext(),
                "The browser is unavailable", Toast.LENGTH_LONG).show();
    }
}
