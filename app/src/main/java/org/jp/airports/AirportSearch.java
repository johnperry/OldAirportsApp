package org.jp.airports;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Collections;
import java.util.LinkedList;

public class AirportSearch extends AppCompatActivity implements LocationListener {

    public final static String EXTRA_MESSAGE = "org.jp.airports.AIRPORTID";

    LocationManager locationManager = null;
    Location location = null;
    AirportAdapter adapter;
    Toast searchToast = null;
    String lastSearch = "";
    boolean nearestSearch = false;
    LinkedList<Airport> list = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AirportsDB db = AirportsDB.getInstance(getApplicationContext());
        ListView listView = (ListView) findViewById(R.id.AirportList);
        adapter = new AirportAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new SearchSelectionListener(this));
        EditText editText = (EditText) findViewById(R.id.SearchText);
        editText.addTextChangedListener(new SearchTextWatcher());
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
                    100, //100 meter minimum change
                    this);   //LocationListener
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new NearestListener());
        }
        else Toast.makeText(getApplicationContext(),
                "Cannot obtain permission for LocationManager", Toast.LENGTH_LONG).show();
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
            if (nearestSearch) {
                search(location);
            }
            else if (list != null) {
                showResults(list, false);
            }
        }
    }
    public void onProviderDisabled(String provider) {
        Toast.makeText(getBaseContext(), "GPS disabled", Toast.LENGTH_LONG);
    }
    public void onProviderEnabled(String provider) {
        Toast.makeText(getBaseContext(), "GPS enabled", Toast.LENGTH_LONG);
    }
    public void onStatusChanged(String provider, int x, Bundle bundle) { }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class SearchSelectionListener implements AdapterView.OnItemClickListener {
        AppCompatActivity creator;
        public SearchSelectionListener(AppCompatActivity creator) {
            this.creator = creator;
        }
        public void onItemClick(AdapterView<?> av, View view, int position, long id) {
            Airport airport = adapter.getItem(position);
            Intent intent = new Intent(creator, AirportDisplay.class);
            intent.putExtra(EXTRA_MESSAGE, airport.getID());
            startActivity(intent);
        }
    }

    class NearestListener implements View.OnClickListener {
        public NearestListener() { }
        public void onClick(View view) {
            if (location != null) {
                search(location);
                nearestSearch = true;
            }
            else {
                Toast.makeText(getApplicationContext(),
                        "Location is unavailable", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void clear(View view) {
        EditText searchText = (EditText) findViewById(R.id.SearchText);
        searchText.setText("");
        search(view);
    }

    class SearchTextWatcher implements TextWatcher {
        public SearchTextWatcher() { }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        public void afterTextChanged(Editable s) {
            if (s.length() > 1) {
                search();
                nearestSearch = false;
            }
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) { }
    }

    public void search(View view) {
        lastSearch += "x";
        search();
    }

    public void search(Location loc) {
        if (searchToast != null) searchToast.cancel();
        Context context = getApplicationContext();
        AirportsDB db = AirportsDB.getInstance(context);
        LinkedList<Airport> list = db.search(loc);
        showResults(list, true);
    }

    public void search() {
        if (searchToast != null) searchToast.cancel();
        EditText searchText = (EditText) findViewById(R.id.SearchText);
        String text = searchText.getText().toString();
        if (!text.equals(lastSearch)) {
            lastSearch = text;
            Context context = getApplicationContext();
            AirportsDB db = AirportsDB.getInstance(context);
            list = db.search(text);
            showResults(list, true);
        }
    }

    private void showResults(LinkedList<Airport> list, boolean showToast) {
        if (location != null) {
            for (Airport ap : list) ap.setDistanceFrom(location);
        }
        Collections.sort(list);
        adapter.clear();
        adapter.addAll(list);
        if (showToast) {
            int n = list.size();
            String result = (n > 0) ?
                    n + " airport" + ((n > 1) ? "s" : "") + " found" :
                    "No airports found";
            Context context = getApplicationContext();
            searchToast = Toast.makeText(context, result, Toast.LENGTH_SHORT);
            searchToast.show();
        }
    }
}
