package fr.wakemybus.playground.googleplacesautocomplete.model;

/**
 * Created by thibaultguegan on 20/01/15.
 */
public class PlacesAutoCompletePredictions {

    public PlacesAutoCompletePrediction [] predictions;
    public String status;

    public static class PlacesAutoCompletePrediction {

        public String description;
        public String reference;
        public Terms[] terms;
        public String[] types;
        MatchedSubstring[] matched_substrings;

        public static class Terms {
            public String value;
            public int offset;
        }

        public static class MatchedSubstring {
            public int offset;
            public int length;
        }

        @Override
        public String toString() {
            return description;
        }


    }
}
