package org.me.gcu.equakestartercode;
// Carlos Leal, Matric Number 20/21 - S1828057

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class EarthquakeMetadata implements Parcelable, Serializable {

    private HashMap<String, String> tags;

    private String originDateTime = null;
    private String location = null;
    private String depth = null;
    private float magnitude = 0;

    public EarthquakeMetadata() {
        // Default values
    }

    public EarthquakeMetadata(HashMap<String, String> descriptionTags) {
        this.tags = descriptionTags;
        this.parseInformation();
    }

    protected EarthquakeMetadata(Parcel in) {
        String[] hashmapTags = in.createStringArray();
        String[] hashmapValues = in.createStringArray();

        tags = new HashMap<>();
        for (int idx = 0; idx < hashmapTags.length; idx++)
            tags.put(hashmapTags[idx], hashmapValues[idx]);

        this.parseInformation();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        String[] hashmapTags = new String[this.tags.size()];
        String[] hashmapValues= new String[this.tags.size()];

        int idx = 0;
        for (Map.Entry<String, String> entry : this.tags.entrySet()) {
            hashmapTags[idx]   = entry.getKey();
            hashmapValues[idx] = entry.getValue();
            idx ++;
        }
        dest.writeStringArray(hashmapTags);
        dest.writeStringArray(hashmapValues);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<EarthquakeMetadata> CREATOR = new Creator<EarthquakeMetadata>() {
        @Override
        public EarthquakeMetadata createFromParcel(Parcel in) {
            return new EarthquakeMetadata(in);
        }

        @Override
        public EarthquakeMetadata[] newArray(int size) {
            return new EarthquakeMetadata[size];
        }
    };

    private void parseInformation() {
        originDateTime = getDescriptionTag("Origin date/time", null);
        location = getDescriptionTag("Location", null);
        depth = getDescriptionTag("Depth", null);

        String Magnitude = getDescriptionTag("Magnitude", null);
        if (Magnitude != null) {
            try {
                magnitude = Float.parseFloat(Magnitude);
            } catch (NumberFormatException ex) {
                System.err.println(String.format("Failed to parse magnitude: %s", Magnitude));
            }
        }
    }

    public String getDescriptionTag(String tag) {
        return getDescriptionTag(tag, null);
    }

    public String getDescriptionTag(String tag, String defaultResult) {
        if (tags.containsKey(tag))
            return tags.get(tag);
        return defaultResult;
    }

    public HashMap<String, String> getTags() {
        return this.tags;
    }

    public String getOriginDateTime() {
        return originDateTime;
    }

    public String getLocation() {
        return location;
    }

    public String getDepth() {
        return depth;
    }

    public float getMagnitude() {
        return magnitude;
    }

    public boolean getMagnitudeBetween(float lowerValue, float upperValue) {
        return magnitude >= lowerValue && magnitude <= upperValue;
    }
}
