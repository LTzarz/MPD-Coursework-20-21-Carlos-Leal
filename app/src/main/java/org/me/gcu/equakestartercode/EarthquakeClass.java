package org.me.gcu.equakestartercode;
// Carlos Leal, Matric Number 20/21 - S1828057

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class EarthquakeClass implements Parcelable, Serializable {

    public UUID Guid;

    private String title;
    private String description;
    private String link;
    private String pubDate;
    private String category;
    private String geoLatitude;
    private String geoLongitude;

    private EarthquakeMetadata metadata;

    public EarthquakeClass()
    {
        title = "";
        description = "";
        link = "";
        pubDate = "";
        category = "";
        geoLatitude = "";
        geoLongitude = "";
        metadata = new EarthquakeMetadata();
        Guid = UUID.randomUUID();
    }

    public EarthquakeClass(String aTitle,String aDescription,String aLink, String aPubDate,String aCategory, String aGeoLatitude,String aGeoLongitude)
    {
        title = aTitle;
        setDescription(aDescription);
        link = aLink;
        pubDate = aPubDate;
        category = aCategory;
        geoLatitude = aGeoLatitude;
        geoLongitude = aGeoLongitude;
        Guid = UUID.randomUUID();
    }



    public String getTitle()
    {
        return title;
    }

    public void setTitle(String aTitle)
    {
        title = aTitle;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String aDescription)
    {
        description = aDescription;

        HashMap<String, String> descriptionTags = new HashMap<>();

        String[] splitDesc = aDescription.split(";");
        for (String descriptor : splitDesc) {
            String[] descriptors = descriptor.split(":");

            Log.e("EarthquakeClass", String.format("Descriptor: (%s)", descriptor));

            String title = descriptors[0].trim(), text = descriptors[1].trim();
            Log.e("EarthquakeClass", String.format("Descriptor: (%s), title (%s), description (%s)", descriptor, title, text));

            descriptionTags.put(title, text);
        }

        metadata = new EarthquakeMetadata(descriptionTags);
    }


    public String getLink()
    {
        return link;
    }

    public void setLink(String aLink)
    {
        link = aLink;
    }

    public String getPubDate()
    {
        return pubDate;
    }

    public void setPubDate(String aPubDate)
    {
        pubDate = aPubDate;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String aCategory)
    {
        category = aCategory;
    }

    public String getGeoLatitude()
    {
        return geoLatitude;
    }

    public void setGeoLatitude(String aGeoLatitude)
    {
        geoLatitude = aGeoLatitude;
    }

    public String getGeoLongitude()
    {
        return geoLongitude;
    }

    public void setGeoLongitude(String aGeoLongitude)
    {
        geoLongitude = aGeoLongitude;
    }

    public String toString()
    {
        String temp;

        temp = title + ", " + description + ", " + link + ", " + pubDate + ", " + category + ", " + geoLatitude + ", " + geoLongitude;

        return temp;
    }

    public EarthquakeMetadata getMetadata() {
        return this.metadata;
    }

    // =========================================================

    protected EarthquakeClass(Parcel in) {
        Guid = UUID.fromString(in.readString());
        title = in.readString();
        description = in.readString();
        link = in.readString();
        pubDate = in.readString();
        category = in.readString();
        geoLatitude = in.readString();
        geoLongitude = in.readString();
        metadata = in.readParcelable(EarthquakeMetadata.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Guid.toString());
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(link);
        dest.writeString(pubDate);
        dest.writeString(category);
        dest.writeString(geoLatitude);
        dest.writeString(geoLongitude);
        dest.writeParcelable(metadata, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<EarthquakeClass> CREATOR = new Creator<EarthquakeClass>() {
        @Override
        public EarthquakeClass createFromParcel(Parcel in) {
            return new EarthquakeClass(in);
        }

        @Override
        public EarthquakeClass[] newArray(int size) {
            return new EarthquakeClass[size];
        }
    };
}
