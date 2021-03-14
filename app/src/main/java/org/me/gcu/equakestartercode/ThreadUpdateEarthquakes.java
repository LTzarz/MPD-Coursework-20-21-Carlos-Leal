package org.me.gcu.equakestartercode;

//Carlos Leal, Matric Number 20/21 - S1828057

import android.os.Handler;
import android.os.HandlerThread;

public class ThreadUpdateEarthquakes implements Runnable {

    private MainActivity mainActivity;
    private Handler handler;
    private HandlerThread handlerThread;

    final int delay = 15*1000; //Delay for 15 seconds.  One second = 1000 milliseconds.

    private boolean isRunning = false;
    public boolean getIsRunning() { return this.isRunning; }

    ThreadParseEarthquakeInformation threadParseEarthquakeInformation;

    public ThreadUpdateEarthquakes(MainActivity activity) {
        this.mainActivity = activity;
        this.threadParseEarthquakeInformation = new ThreadParseEarthquakeInformation(
            activity, activity.URLSOURCE);

        HandlerThread thread = new HandlerThread("ThreadUpdateEarthquakes");
        thread.start();
        this.handler = new Handler(thread.getLooper());
    }

    public ThreadUpdateEarthquakes Start() {
        this.isRunning = true;
        handler.postDelayed(this, 0);
        return this;
    }

    public ThreadUpdateEarthquakes Pause() {
        this.isRunning = false;
        handler.removeCallbacks(this);
        return this;
    }

    @Override
    public void run() {
        if (!this.isRunning) return;

        this.threadParseEarthquakeInformation.run();

        // Wait another 15 seconds.
        handler.postDelayed(this, delay);
    }
}
