package org.jp.airports;

import android.location.Location;
import android.util.Log;

/**
 * Created by John on 3/19/2016.
 */
public class Airport implements Comparable<Airport> {
    private static final String TAG = "Airport";
    public String id;
    public String name = "";
    public String city = "";
    public String state = "";
    public double lat = 0.0;
    public double lon = 0.0;
    public String elev = "";
    public String rwy = "";
    public String var = "";
    public double dist = 0.0;
    public double brng = 0.0;

    private String airportText;

    public Airport(String airportText) {
        String[] ap = airportText.split("\\|");
        for (int i=0; i<ap.length; i++) {
            switch (i) {
                case 0: this.id = ap[0]; break;
                case 1: this.name = ap[1]; break;
                case 2: this.city = ap[2]; break;
                case 3: this.state = ap[3]; break;
                case 4:
                    String[] latlon = ap[4].split(",");
                    this.lat = Double.parseDouble(latlon[0]);
                    this.lon = Double.parseDouble(latlon[1]);
                    break;
                case 5: this.elev = ap[5]; break;
                case 6: this.rwy = ap[6]; break;
                case 7: this.var = ap[7]; break;
            }
        }
        this.airportText = (id +"|"+name+"|"+city+"|"+state+"|").toLowerCase();
    }

    public String getID() {
        return id;
    }

    public boolean matches(String sc) {
        return airportText.contains(sc);
    }

    public int compareTo(Airport ap) {
        if ((dist >= 0.0) && (ap.dist >= 0.0)) {
            if (dist < ap.dist) return -1;
            if (dist > ap.dist) return 1;
        }
        return id.compareTo(ap.id);
    }

    public void setDistanceFrom(Location location) {
        dist = getDistanceFrom(location);
        brng = getBearingFrom(location);
    }

    public double getDistanceFrom(Location location) {
        if (location != null) {
            V3 fromV3 = new V3(location.getLatitude(), location.getLongitude());
            V3 toV3 = new V3(lat, lon);
            double dotProduct = fromV3.dot(toV3);
            double angle = Math.acos(dotProduct);
            return angle * 3440.0; //scale to nm
        }
        return -1.0;
    }

    public double getBearingFrom(Location location) {
        if (location != null) {
            V3 loc = new V3(location);
            V3 here = new V3(lat, lon);
            return loc.bearingTo(here);
        }
        return -1.0;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id + "\n");
        sb.append("    " + name + "\n");
        sb.append("    " + city + ", " + state + "\n");
        sb.append("    " + "("+lat+","+lon + ")" + "\n");
        sb.append("    Elev: " + elev + "\n");
        sb.append("    Rwy:  " + rwy + "\n");
        sb.append("    Var:  " + var + "\n");
        return sb.toString();
    }

}
