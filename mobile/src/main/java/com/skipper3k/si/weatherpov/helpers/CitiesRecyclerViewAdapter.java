package com.skipper3k.si.weatherpov.helpers;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skipper3k.si.weatherpov.R;
import com.skipper3k.si.weatherpov.data.WPOVCity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by skipper3k on 04/04/16.
 *
 * Recycleviews adapter with custom layout implementation
 *
 */
public class CitiesRecyclerViewAdapter extends RecyclerView.Adapter<CitiesRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = CitiesRecyclerViewAdapter.class.getSimpleName();
    DateFormat sdf = SimpleDateFormat.getDateTimeInstance();
    /**
     * the list of favoured cities
     */
    private List<WPOVCity> cities;

    public void setData(List<WPOVCity> cities) {
        this.cities = cities;
    }

    public CitiesRecyclerViewAdapter(List<WPOVCity> cities) {
        this.cities = cities;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CitiesRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {

        LinearLayout v = (LinearLayout)LayoutInflater.from(parent.getContext())
                .inflate(R.layout.city_list_item, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mCityName.setText(cities.get(position).name);
        holder.mCityLastUpdate.setText(cities.get(position).lastUpdated != null ? sdf.format(cities.get(position).lastUpdated) : "never");
        holder.mCityTemperature.setText((cities.get(position).temp + "°C"));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return cities.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout root;

        public TextView mCityName;
        public TextView mCityTemperature;
        public TextView mCityLastUpdate;

        public ViewHolder(LinearLayout v) {
            super(v);
            root = v;

            mCityName = (TextView)root.findViewById(R.id.cityName);
            mCityTemperature = (TextView)root.findViewById(R.id.cityTemp);
            mCityLastUpdate = (TextView)root.findViewById(R.id.lastUpdated);
        }
    }
}
