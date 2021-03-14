package org.me.gcu.equakestartercode;
// Carlos Leal, Matric Number 20/21 - S1828057

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;

public class ThreadParseEarthquakeInformation implements Runnable {
    private String url;
    private MainActivity mainActivity;

    public ThreadParseEarthquakeInformation(MainActivity mainActivity, String url) {
        this.url = url;
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {
        String editedFeed = this.getXmlFromUrl();

        // Make call to parsing code
        LinkedList<EarthquakeClass> earthquakes = parseEarthquakeXMLInformation(editedFeed);
        mainActivity.setEarthquakes(earthquakes);

        if (mainActivity.isFiltered()) {
            mainActivity.filterEarthquakes();
        } else {
            mainActivity.loadEarthquakesList(earthquakes);
        }
    }

    public String getXmlFromUrl() {
        URL url;
        URLConnection yc;
        BufferedReader in = null;
        String inputLine = "";

        StringBuffer buffer = new StringBuffer();

        try {
            url = new URL(this.url);
            yc = url.openConnection();
            in = new BufferedReader(new InputStreamReader(yc.getInputStream()));

            while ((inputLine = in.readLine()) != null) {
                buffer.append(inputLine);
                Log.e("Constructing XML feed", inputLine);
            }
            in.close();
        } catch (IOException ae) {
            Log.e("IO Exception", "ioexception in run");
        }

        String editedFeed = buffer.substring(buffer.indexOf("<rss")).trim();
        return editedFeed;
    }

    public LinkedList<EarthquakeClass> parseEarthquakeXMLInformation(String dataToParse) {
        EarthquakeClass earthQuake = null;
        LinkedList<EarthquakeClass> alist = null;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(dataToParse));
            int eventType = xpp.getEventType();

            Log.e("Parse", "Begin Parsing");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                // Found a start tag
                if (eventType == XmlPullParser.START_TAG) {
                    // Check which Tag we have
                    if (xpp.getName().equalsIgnoreCase("channel")) {
                        alist = new LinkedList<EarthquakeClass>();
                    } else if (xpp.getName().equalsIgnoreCase("item")) {
                        Log.e("item", "Item Start Tag found");
                        earthQuake = new EarthquakeClass();
                    } else if (earthQuake != null) {
                        if (xpp.getName().equalsIgnoreCase("title")) {
                            // Now just get the associated text
                            String tagText = xpp.nextText();

                            Log.e("title tag", "Earthquake title is: " + tagText);
                            earthQuake.setTitle(tagText);
                        }
                        // Check which Tag we have
                        else if (xpp.getName().equalsIgnoreCase("description")) {
                            // Now just get the associated text
                            String tagText = xpp.nextText();

                            // Do something with text
                            Log.e("description tag", "Description is: " + tagText);
                            earthQuake.setDescription(tagText);
                        }
                        // Check which Tag we have
                        else if (xpp.getName().equalsIgnoreCase("link")) {
                            // Now just get the associated text
                            String tagText = xpp.nextText();
                            // Do something with text
                            Log.e("link tag", "Link is: " + tagText);
                            earthQuake.setLink(tagText);
                        }
                        // Check which Tag we have
                        else if (xpp.getName().equalsIgnoreCase("pubDate")) {
                            // Now just get the associated text
                            String tagText = xpp.nextText();
                            // Do something with text
                            Log.e("pubDate tag", "Publication Date is: " + tagText);
                            earthQuake.setPubDate(tagText);
                        }

                        // Check which Tag we have
                        else if (xpp.getName().equalsIgnoreCase("category")) {
                            // Now just get the associated text
                            String tagText = xpp.nextText();
                            // Do something with text
                            Log.e("category tag", "Category is: " + tagText);
                            earthQuake.setCategory(tagText);
                        }

                        // Check which Prefix we have
                        else if (xpp.getPrefix().equalsIgnoreCase("geo")) {
                            if (xpp.getName().equalsIgnoreCase("lat")) {
                                // Now just get the associated text
                                String tagText = xpp.nextText();
                                // Do something with text
                                Log.e("geo:lat tag", "The latitude is: " + tagText);
                                earthQuake.setGeoLatitude(tagText);
                            } else if (xpp.getName().equalsIgnoreCase("long")) {
                                // Now just get the associated text
                                String tagText = xpp.nextText();
                                // Do something with text
                                Log.e("geo:long tag", "The longitude is: " + tagText);
                                earthQuake.setGeoLongitude(tagText);
                            }
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (xpp.getName().equalsIgnoreCase("item")) {
                        Log.e("item tag", "This earthquake item consists of: " + earthQuake.toString());
                        alist.add(earthQuake);
                    } else if (xpp.getName().equalsIgnoreCase("channel")) {
                        int size;
                        size = alist.size();
                        Log.e("channel tag", "This is the amount of earthquakes obtained from the RSS feed: " + size);
                    }
                }


                // Get the next event
                eventType = xpp.next();

            } // End of while

            //return alist;
        } catch (XmlPullParserException ae1) {
            Log.e("Parsing error", "Parsing error: " + ae1.toString());
        } catch (IOException ae1) {
            Log.e("IO error", "IO error during parsing");
        }

        Log.e("EOD", "EOD");

        return alist;

    }
}
