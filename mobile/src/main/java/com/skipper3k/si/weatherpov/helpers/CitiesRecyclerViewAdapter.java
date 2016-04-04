package com.skipper3k.si.weatherpov.helpers;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.skipper3k.si.weatherpov.R;
import com.skipper3k.si.weatherpov.data.WPOVCity;

import java.util.List;

/**
 * Created by skipper3k on 04/04/16.
 *
 * Recycleviews adapter with custom layout implementation
 *
 */
public class CitiesRecyclerViewAdapter extends RecyclerView.Adapter<CitiesRecyclerViewAdapter.ViewHolder> {

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
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.city_list_item, parent, false);

        TextView cityName = (TextView)v.findViewById(R.id.cityName);
        TextView cityTemp = (TextView)v.findViewById(R.id.cityTemp);

        ViewHolder vh = new ViewHolder(cityName, cityTemp);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mCityName.setText(cities.get(position).name);
        holder.mCityTemp.setText(cities.get(position).temp);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return cities.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mCityName;
        public TextView mCityTemp;
        public ViewHolder(TextView v, TextView v2) {
            super(v);

            mCityName = v;
            mCityTemp = v2;
        }
    }
}
