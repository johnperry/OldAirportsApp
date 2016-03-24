package org.jp.airports;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by John on 3/19/2016.
 */
public class AirportAdapter extends ArrayAdapter<Airport> {
    private final Context context;

    public AirportAdapter(Context context) {
        super(context, -1);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.airport_list, parent, false);

        Airport airport = getItem(position);
        TextView idView = (TextView) rowView.findViewById(R.id.AirportListID);
        idView.setText(airport.id);
        TextView nameView = (TextView) rowView.findViewById(R.id.AirportListName);
        nameView.setText(airport.name);
        TextView cityStateView = (TextView) rowView.findViewById(R.id.AirportListCityState);
        cityStateView.setText(airport.city + ", " + airport.state);
        return rowView;
    }
}
