package org.me.gcu.equakestartercode;
// Carlos Leal, Matric Number 20/21 - S1828057

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MainActivity selfInstance;
    //private TextView rawDataDisplay;

    public final String URLSOURCE = "http://quakes.bgs.ac.uk/feeds/MhSeismology.xml";
    private LinkedList<EarthquakeClass> earthquakes;

    private boolean isFiltered = false;
    private Date filterStartDate, filterEndDate;
    private DatePickerDialog picker;

    private ThreadUpdateEarthquakes thrUpdateEarthquakes;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selfInstance = this;

        setContentView(R.layout.activity_main);
        Log.e("MyTag", "in onCreate");
        // Set up the raw links to the graphical components
        //rawDataDisplay = (TextView) findViewById(R.id.rawDataDisplay);

        Button btnGetData = (Button) findViewById(R.id.startButton);
        btnGetData.setOnClickListener(this::btnGetData_Click);

        Button btnFilterDate = (Button) findViewById(R.id.btnFilterDate);
        btnFilterDate.setOnClickListener(this::btnFilterDates_Click);

        Button btnClearFilters = (Button) findViewById(R.id.btnClearFilter);
        btnClearFilters.setOnClickListener(this::btnClearFilter_Click);

        EditText dpStartDate = (EditText) findViewById(R.id.inputStartDate);
        dpStartDate.setOnTouchListener( (v,me) -> this.inputOnDateInputListeners(v, me,true, filterStartDate));

        EditText dpEndDate = (EditText) findViewById(R.id.inputEndDate);
        dpEndDate.setOnTouchListener( (v,me) -> this.inputOnDateInputListeners(v, me, false, filterEndDate));

        Log.e("MyTag", "after btnGetData");
        // More Code goes here

        startProgress();
    }



    public void btnGetData_Click(View view) {
        Log.e("MyTag", "in onClick");
        startProgress();
        Log.e("MyTag", "after startProgress");
    }

    public void btnClearFilter_Click(View view) {
        this.isFiltered = false;

        this.filterStartDate = this.filterEndDate = null;

        EditText editText = (EditText) findViewById(R.id.inputStartDate);
        editText.setText("");

        editText = (EditText) findViewById(R.id.inputEndDate);
        editText.setText("");
    }

    public boolean inputOnDateInputListeners(View view, MotionEvent me, boolean isStartDate, Date defaultValue) {
        Calendar calendar = Calendar.getInstance();;
        int day, month, year;

        if (defaultValue != null)
            calendar.setTime(defaultValue);

        if (me != null && me.getAction() != MotionEvent.ACTION_UP)
            return true;

        if (picker != null && picker.isShowing())
            return true;

        day   = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH);
        year  = calendar.get(Calendar.YEAR);

        // date picker dialog
        picker = new DatePickerDialog(this,
            (dpdView, iYear, iMonth, iDay) -> {
                EditText editText;

                Calendar iCalendar = Calendar.getInstance();
                iCalendar.set(iYear, iMonth, iDay, 0, 0, 0);

                if (isStartDate) {
                    editText = (EditText) findViewById(R.id.inputStartDate);
                    filterStartDate = iCalendar.getTime();
                } else {
                    editText = (EditText) findViewById(R.id.inputEndDate);
                    filterEndDate = iCalendar.getTime();
                }

                editText.setText(iDay+"/"+(iMonth+1)+"/"+iYear);
            }, year, month, day);

        picker.show();
        return true;
    }

    public void btnFilterDates_Click(View view) {
        // If we have not filters render the earthquake list
        if (filterStartDate == null && filterEndDate == null) {
            this.isFiltered = false;
            this.loadEarthquakesList(earthquakes);
            return;
        }

        filterEarthquakes(filterStartDate, filterEndDate);
        this.isFiltered = true;
    }

    public void btnShowEarthquakeInformation(View view, EarthquakeClass earthquake) {
        Intent eQuakeMap = new Intent(MainActivity.this, EarthQuakeMap.class);
        Bundle eQuakeBundle = new Bundle();
        eQuakeBundle.putParcelableArrayList("List of Earthquakes", new ArrayList<>(earthquakes));
        eQuakeBundle.putParcelable("Selected Earthquake", earthquake);
        eQuakeMap.putExtras(eQuakeBundle);

        Toast.makeText(getBaseContext(), "HTML link: " + earthquake.getLink() + ",\n "
                + "Publication Date: " + earthquake.getPubDate() + ",\n " + "Earthquake category: "
                + earthquake.getCategory() + ",\n " + "Latitude distance: " + earthquake.getGeoLatitude() + ",\n "
                + "Longitude distance: " + earthquake.getGeoLongitude(), Toast.LENGTH_SHORT).show();

        startActivity(eQuakeMap);
    }

    public void startProgress() {
        // Run network access on a separate thread;
        new Thread(thrUpdateEarthquakes = new ThreadUpdateEarthquakes(this)).start();
    } //

    @Override
    protected void onResume() {
        super.onResume();
        setupEarthquakesLoading();
        thrUpdateEarthquakes.Start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        thrUpdateEarthquakes.Pause();
    }

    private void setupEarthquakesLoading() {
        LinearLayout.LayoutParams childParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        childParams.setMargins(5, 5, 5, 5);

        /* Find Tablelayout defined in main.xml */
        TableLayout tl = findViewById(R.id.earthquakeLocationsStrengths);
        tl.removeAllViews(); // Remove all rows

        /* Create a table row */
        TableRow trMap = new TableRow(tl.getContext());
        trMap.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        /* Create and add a Textview to show the earthquakes are loading... */
        TextView tvText = new TextView(tl.getContext());
        Date currentTime = Calendar.getInstance().getTime();
        tvText.setText("Loading earthquakes...");
        trMap.addView(tvText);

        tl.addView(trMap);
    }

    public boolean isFiltered() {
        return this.isFiltered;
    }

    public void loadEarthquakesList(List<EarthquakeClass> earthquakes) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            public void run() {
                Log.d("UI thread", "I am the UI thread");
                //similarly to the PullParser3 example, 'result' should be fed into the XMLPullParser

                //rawDataDisplay.setText(""); //result);

                // Write list to Log for testing
                if (earthquakes != null) {
                    Log.e("List of Earthquakes", "List not null");

                    LinearLayout.LayoutParams childParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    childParams.setMargins(5, 5, 5, 5);

                    /* Find Tablelayout defined in main.xml */
                    TableLayout tl = findViewById(R.id.earthquakeLocationsStrengths);
                    tl.removeAllViews(); // Remove all rows

                    /*Create button to take user to map, which displays markers based on earthquakes*/
                    TableRow trMap = new TableRow(tl.getContext());
                    trMap.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT));

                    TextView tvLastUpdate = new TextView(tl.getContext());
                    Date currentTime = Calendar.getInstance().getTime();
                    tvLastUpdate.setText(currentTime.toString());
                    trMap.addView(tvLastUpdate);

                    Button btnGotoMap = new Button(tl.getContext());
                    btnGotoMap.setText("Go to Map");
                    btnGotoMap.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
                    btnGotoMap.setOnClickListener(v -> {
                        Intent eQuakeMap = new Intent(MainActivity.this, EarthQuakeMap.class);
                        Bundle eQuakeBundle = new Bundle();
                        eQuakeBundle.putParcelableArrayList("List of Earthquakes", new ArrayList<>(earthquakes));
                        // eQuakeBundle.putParcable(earthquake);
                        eQuakeMap.putExtras(eQuakeBundle);

                        startActivity(eQuakeMap);
                    });

                    trMap.addView(btnGotoMap);
                    tl.addView(trMap, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT));

                    for (EarthquakeClass earthquake : earthquakes) {
                        Log.e("List of Earthquakes", earthquake.toString());

                        /* Create new rows to be added. */
                        TableRow tr = new TableRow(tl.getContext());
                        tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.WRAP_CONTENT));

                        TableRow trBtn = new TableRow(tl.getContext());
                        trBtn.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.WRAP_CONTENT, 1.0f));

                        /* Create a TextView and Button to be the row-content. */
                        TextView description = new TextView(selfInstance);
                        description.setText(" Location: " + earthquake.getMetadata().getLocation() + " //\n " + "Strength of earthquake: "
                                + earthquake.getMetadata().getMagnitude());
                        description.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.WRAP_CONTENT,1.0f));

                        Button viewDetails = new Button(tl.getContext());
                        viewDetails.setText("View more details");
                        viewDetails.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
                        viewDetails.setOnClickListener(v -> MainActivity.this.btnShowEarthquakeInformation(v, earthquake));

                        /* Add TextView and Button to rows. */
                        tr.addView(description);
                        trBtn.addView(viewDetails);

                        //If Magnitude is between
                        // 0-1 (blue),
                        // 1.1-2 (green),
                        // 2.1-3 (yellow),
                        // 3.1-4 (red) Do a substring on the description string
                        if (earthquake.getMetadata().getMagnitudeBetween(0, 1)) {
                            tr.setBackgroundColor(Color.parseColor("#7986CB")); // Blue
                        } else if (earthquake.getMetadata().getMagnitudeBetween(1, 2)) {
                            tr.setBackgroundColor(Color.parseColor("#81C784")); // Green
                        } else if (earthquake.getMetadata().getMagnitudeBetween(2, 3)) {
                            tr.setBackgroundColor(Color.parseColor("#FFF176")); // Yellow
                        } else if (earthquake.getMetadata().getMagnitude() >= 3) {
                            tr.setBackgroundColor(Color.parseColor("#FF7043")); // Red
                        }

                        /* Add row to TableLayout. */

                        tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT,
                                TableLayout.LayoutParams.WRAP_CONTENT));
                        tl.addView(trBtn, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                                TableLayout.LayoutParams.WRAP_CONTENT));

                    }
                } else {
                    Log.e("List of Earthquakes", "List is null");
                }
            }
        });
    }

    public void filterEarthquakes() {
        filterEarthquakes(filterStartDate, filterEndDate);
    }

    public void filterEarthquakes(Date date) {
        filterEarthquakes(date, date);
    }

    public void filterEarthquakes(Date startDate, Date endDate) {
        // Assume end date or start date if either is null
        if (startDate == null && endDate != null) {
            Calendar iCalendar = Calendar.getInstance();
            iCalendar.setTime(startDate);
            iCalendar.set(Calendar.HOUR, 0);
            iCalendar.set(Calendar.MINUTE, 0);
            iCalendar.set(Calendar.SECOND, 0);
            startDate = iCalendar.getTime();
        } else if (endDate == null && startDate != null) {
            Calendar iCalendar = Calendar.getInstance();
            iCalendar.setTime(startDate);
            iCalendar.set(Calendar.HOUR, 23);
            iCalendar.set(Calendar.MINUTE, 59);
            iCalendar.set(Calendar.SECOND, 59);
            endDate = iCalendar.getTime();
        }else if (startDate == null && endDate == null) {
            this.loadEarthquakesList(this.earthquakes);
            return;
        }

        if (this.earthquakes == null){
            Toast.makeText(getBaseContext(), "Please load the earthquake list\nbefore attempting to filter it!",
                Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<EarthquakeClass> earthquakes = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");

        for( EarthquakeClass eq : this.earthquakes ) {
            // Check if the date is empty
            if (eq.getPubDate() == null || eq.getPubDate().isEmpty())
                continue;

            try {
                // Parse the date
                Date date = dateFormat.parse(eq.getPubDate());

                // Check if the date is in a range
                if (date.after(startDate) && date.before(endDate))
                    earthquakes.add(eq);
            } catch (ParseException e) {
                Log.e("DateParse", "Failed to parse date: " + eq.getPubDate(), e);
            }
        }


        // Render the earthquakes
        this.loadEarthquakesList(earthquakes);
        //run the different functions, passing in earthquakes arraylist and displaying them as a toast.

        if (earthquakes.size() == 0)
        {
            Toast.makeText(getBaseContext(),  "Please increase the date range in order to compare earthquakes.",
                    Toast.LENGTH_SHORT).show();
        }
        else
        {
            String mostNortherlyEQuake = findMostNorth(earthquakes);
            String mostSoutherlyEQuake = findMostSouth(earthquakes);
            String mostEasternEQuake = findMostEast(earthquakes);
            String mostWesternEQuake = findMostWest(earthquakes);
            String mostPowerfulEQuake = findLargestMagnitude(earthquakes);
            String mostDeepEQuake = findDeepestEarthquake(earthquakes);
            String mostShallowEQuake = findShallowestEarthquake(earthquakes);

            Toast.makeText(getBaseContext(), mostNortherlyEQuake + "\n" + "" + "\n" + mostSoutherlyEQuake + "\n" + "" + "\n" + mostEasternEQuake + "\n" + "" + "\n" + mostWesternEQuake,
                    Toast.LENGTH_LONG).show();
            Toast.makeText(getBaseContext(), mostPowerfulEQuake,
                    Toast.LENGTH_SHORT).show();
            Toast.makeText(getBaseContext(), mostDeepEQuake,
                    Toast.LENGTH_SHORT).show();
            Toast.makeText(getBaseContext(), mostShallowEQuake,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String findMostNorth(ArrayList<EarthquakeClass> filteredEQuakes) {
        float lat = 0;
        EarthquakeClass mostNorth = filteredEQuakes.get(0);

        for( EarthquakeClass eq : filteredEQuakes ) {
            float eqLat = Float.parseFloat(eq.getGeoLatitude());
            if (eqLat > lat) {
                lat = eqLat;
                mostNorth = eq;
            }
        }
        return "The most Northerly Earthquake in this list is " + mostNorth.getTitle() + " with a latitude of: " + mostNorth.getGeoLatitude();
    }

    private String findMostSouth(ArrayList<EarthquakeClass> filteredEQuakes) {
        float lat = 0;
        EarthquakeClass mostSouth = filteredEQuakes.get(0);

        for( EarthquakeClass eq : filteredEQuakes ) {
            float eqLat = Float.parseFloat(eq.getGeoLatitude());
            if (eqLat < lat) {
                lat = eqLat;
                mostSouth = eq;
            }
        }
        return "The most Southerly Earthquake in this list is " + mostSouth.getTitle() + " with a latitude of: " + mostSouth.getGeoLatitude();
    }

    private String findMostEast(ArrayList<EarthquakeClass> filteredEQuakes) {
        float longi = 0;
        EarthquakeClass mostEast = filteredEQuakes.get(0);

        for( EarthquakeClass eq : filteredEQuakes ) {
            float eqLong = Float.parseFloat(eq.getGeoLongitude());
            if (eqLong > longi) {
                longi = eqLong;
                mostEast = eq;
            }
        }
        return "The most Eastern Earthquake in this list is " + mostEast.getTitle() + " with a latitude of: " + mostEast.getGeoLongitude();
    }

    private String findMostWest(ArrayList<EarthquakeClass> filteredEQuakes) {
        float longi = 0;
        EarthquakeClass mostWest = filteredEQuakes.get(0);

        for( EarthquakeClass eq : filteredEQuakes ) {
            float eqLong = Float.parseFloat(eq.getGeoLongitude());
            if (eqLong < longi) {
                longi = eqLong;
                mostWest = eq;
            }
        }
        return "The most Western Earthquake in this list is " + mostWest.getTitle() + " with a latitude of: " + mostWest.getGeoLongitude();
    }

    private String findLargestMagnitude(ArrayList<EarthquakeClass> filteredEQuakes) {
        float magnitude = 0;
        EarthquakeClass largestMagnitude = filteredEQuakes.get(0);

        for( EarthquakeClass eq : filteredEQuakes ) {
            float eqMag = eq.getMetadata().getMagnitude();
            if (eqMag > magnitude) {
                magnitude = eqMag;
                largestMagnitude = eq;
            }
        }
        return "The most Powerful Earthquake in this list is " + largestMagnitude.getTitle() + " with a magnitude of: " + largestMagnitude.getMetadata().getMagnitude();
    }

    private String findDeepestEarthquake(ArrayList<EarthquakeClass> filteredEQuakes)
    {
        float depth = 0;
        EarthquakeClass deepestEQuake = filteredEQuakes.get(0);

        for( EarthquakeClass eq : filteredEQuakes )
        {
            String depthToBeEdited = eq.getMetadata().getDepth();
            Log.e("depth", depthToBeEdited);
            String editedDepth = depthToBeEdited.substring(0,2).trim();
            Log.e("depthEdited", editedDepth);
            float eqDepth = Float.parseFloat(editedDepth);
            if (eqDepth > depth) {
                depth = eqDepth;
                deepestEQuake = eq;
            }
        }
        return "The Deepest Earthquake in this list is " + deepestEQuake.getTitle() + " with a depth of: " + deepestEQuake.getMetadata().getDepth();
    }

    private String findShallowestEarthquake(ArrayList<EarthquakeClass> filteredEQuakes)
    {
        float depth = 1000;
        EarthquakeClass shallowestEQuake = filteredEQuakes.get(0);

        for( EarthquakeClass eq : filteredEQuakes )
        {
            String depthToBeEdited = eq.getMetadata().getDepth();
            Log.e("depth", depthToBeEdited);
            String editedDepth = depthToBeEdited.substring(0,2).trim();
            Log.e("depthEdited", editedDepth);
            float eqDepth = Float.parseFloat(editedDepth);
            if (eqDepth < depth) {
                depth = eqDepth;
                shallowestEQuake = eq;
            }
        }
        return "The Shallowest Earthquake in this list is " + shallowestEQuake.getTitle() + " with a depth of: " + shallowestEQuake.getMetadata().getDepth();
    }

    public void setEarthquakes(LinkedList<EarthquakeClass> earthquakes) {
        this.earthquakes = earthquakes;
    }
}