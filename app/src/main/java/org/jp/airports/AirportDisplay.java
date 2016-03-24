package org.jp.airports;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by John on 3/21/2016.
 */
public class AirportDisplay extends AppCompatActivity {

    Airport airport = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        Intent intent = getIntent();
        String airportID = intent.getStringExtra(AirportSearch.EXTRA_MESSAGE);
        AirportsDB db = AirportsDB.getInstance();
        airport = db.getAirport(airportID);

        String latlon = String.format("(%.3f, %.3f)", airport.lat, airport.lon);
        String dist = (airport.dist >= 0.0) ?
                String.format("%.1f nm @ %.0f°", airport.dist, airport.brng)
                : null;

        setText(R.id.AirportID, airport.id);
        setText(R.id.AirportName, airport.name);
        setText(R.id.AirportCity, airport.city);
        setText(R.id.AirportState, airport.state);
        setText(R.id.AirportElev, airport.elev, " ft", R.id.ElevRow);
        setText(R.id.AirportLatLon, latlon);
        setText(R.id.AirportRwy, airport.rwy, " ft", R.id.RunwayRow);
        setText(R.id.AirportVar, airport.var, "°", R.id.VarRow);
        setText(R.id.AirportDist, dist, "", R.id.DistRow);
     }

    private void setText(int id, String text, String units, int rowID) {
        if ((text == null) || text.trim().equals("")) {
            View row = (View)findViewById(rowID);
            row.setVisibility(View.GONE);
        }
        else {
            setText(id, text + units);
        }
    }

    private void setText(int id, String text) {
        TextView view = (TextView) findViewById(id);
        view.setText(text);
    }

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
