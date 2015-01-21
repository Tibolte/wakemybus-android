package fr.wakemybus.playground.googleplacesautocomplete;

import android.content.Context;
import android.location.Location;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;

import fr.wakemybus.playground.googleplacesautocomplete.model.PlacesAutoCompletePredictions;

/**
 * Created by thibaultguegan on 20/01/15.
 */
public class PlacesAutoCompleteAdapter extends ArrayAdapter<PlacesAutoCompletePredictions.PlacesAutoCompletePrediction> implements
        Filterable {
    ArrayList<PlacesAutoCompletePredictions.PlacesAutoCompletePrediction> resultList;

    Location locationBias;
    Long radiusBias;

    public PlacesAutoCompleteAdapter(Context context, int textViewResourceId, Location location, long radius) {
        super(context, textViewResourceId);
        locationBias = location;
        radiusBias = radius;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public PlacesAutoCompletePredictions.PlacesAutoCompletePrediction getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // Retrieve the autocomplete results.
                    resultList = GooglePlacesApiWrapper.autocomplete(constraint
                            .toString(), locationBias, radiusBias);

                    // Assign the data to the FilterResults
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }
}
