package com.skipper3k.si.weatherpov.helpers;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skipper3k.si.weatherpov.CityDetailsActivity;
import com.skipper3k.si.weatherpov.R;
import com.skipper3k.si.weatherpov.WeatherPOVActivity;
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
    private Context context;

    public void setData(List<WPOVCity> cities) {
        this.cities = cities;
    }

    public CitiesRecyclerViewAdapter(List<WPOVCity> cities, Context context) {
        this.cities = cities;
        this.context = context;
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
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mCityName.setText(cities.get(position).name);
        holder.mCityLastUpdate.setText(cities.get(position).lastUpdated != null ? sdf.format(cities.get(position).lastUpdated) : "never");
        holder.mCityTemperature.setText((cities.get(position).temp + "°C"));

        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent detailsActivity = new Intent(context, CityDetailsActivity.class);
                detailsActivity.putExtra(WeatherPOVActivity.ADD_CITY_STRING, cities.get(position));
                context.startActivity(detailsActivity);
            }
        });
    }


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
