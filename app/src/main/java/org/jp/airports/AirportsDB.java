package org.jp.airports;

import android.content.Context;
import android.content.res.AssetManager;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 * Created by John on 3/19/2016.
 */
public class AirportsDB {
    private static final String AirportsFilename = "Airports.txt";
    private static AirportsDB instance = null;
    private Hashtable<String,Airport> airports = null;

    public static AirportsDB getInstance(Context context) {
        if (instance == null) {
            instance = new AirportsDB(context);
        }
        return instance;
    }

    public static AirportsDB getInstance() {
        return instance;
    }

    protected AirportsDB(Context context) {
        airports = new Hashtable<String,Airport>();
        AssetManager am = context.getAssets();
        try {
            String filename = context.getResources().getString(R.string.AirportsFilename);
            InputStream is = am.open(filename);
            String[] airportStrings = getText(is).split("\n");
            for (String airportString : airportStrings) {
                Airport airport = new Airport(airportString);
                airports.put(airport.getID(), airport);
            }
            Toast toast = Toast.makeText(
                    context,
                    airports.size() + " airports loaded",
                    Toast.LENGTH_LONG);
            toast.show();
        }
        catch (Exception ex) {
            Toast toast = Toast.makeText(context, "Unable to load the airports", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public static String getText(InputStream inputStream) throws Exception{
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            isr = new InputStreamReader(inputStream);
            br = new BufferedReader(isr);
            String line;
            StringBuilder text = new StringBuilder();
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            return text.toString();
        }
        finally {
            br.close();
        }
    }

    public Airport getAirport(String id) {
        Airport ap = airports.get(id);
        if (ap == null) ap = airports.get("K"+id);
        return ap;
    }

    public LinkedList<Airport> search(String sc) {
        sc = sc.toLowerCase();
        LinkedList<Airport> aps = new LinkedList<Airport>();
        for (Airport ap : airports.values()) {
            if (ap.matches(sc)) {
                aps.add(ap);
            }
        }
        return aps;
    }

    public LinkedList<Airport> search(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        double delta = 0.5;
        LinkedList<Airport> aps = new LinkedList<Airport>();
        for (Airport ap : airports.values()) {
            if ( (Math.abs(ap.lat - lat) < delta)
                    && (Math.abs(ap.lon - lon) < delta)) {
                aps.add(ap);
            }
        }
        return aps;
    }

    public double getDistance(String fromID, String toID) {
        Airport fromAP = airports.get(fromID);
        Airport toAP = airports.get(toID);
        if ((fromAP != null) && (toAP != null)) {
            V3 fromV3 = new V3(fromAP.lat, fromAP.lon);
            V3 toV3 = new V3(toAP.lat, toAP.lon);
            double dotProduct = fromV3.dot(toV3);
            double angle = Math.acos(dotProduct);
            return angle * 3440.0; //scale to nm
        }
        return 0.0;
    }

    class V3 {
        double x;
        double y;
        double z;
        public V3(double lat, double lon) {
            lat = lat * Math.PI / 180.0;
            lon = lon * Math.PI / 180.0;
            z = Math.sin(lat);
            x = Math.cos(lat) * Math.cos(lon);
            y = Math.cos(lat) * Math.sin(lon);
        }
        public double dot(V3 v) {
            return x * v.x + y * v.y + z * v.z;
        }
    }

}
