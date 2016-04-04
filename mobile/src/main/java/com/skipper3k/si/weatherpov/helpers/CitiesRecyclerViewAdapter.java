package com.skipper3k.si.weatherpov.helpers;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
    private static final String TAG = CitiesRecyclerViewAdapter.class.getSimpleName();
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

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mCityName.setText(cities.get(position).name);
//        holder.mCityTemp.setText("" + cities.get(position).temp);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return cities.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout root;

        public TextView mCityName;

        public ViewHolder(LinearLayout v) {
            super(v);
            root = v;

            mCityName = (TextView)root.findViewById(R.id.cityName);
        }
    }
}
