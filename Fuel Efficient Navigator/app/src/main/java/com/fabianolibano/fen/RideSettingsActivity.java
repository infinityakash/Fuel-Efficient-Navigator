package com.fabianolibano.fen;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.fabianolibano.fen.model.Ride;
import com.fabianolibano.fen.model.node;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class RideSettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener
{
    //Data
    private Ride ride = new Ride();
    private GoogleApiClient client;
    private LocationRequest request;
    private Location userLocation;
    private mapGraph map;
    private String[] routeTimes;

    //UI
    private EditText txtMPG;
    private EditText txtGPM;
    private Spinner spnOrig, spnDest, spnDay, spnTime;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_settings);

        setUpInterface();

        buildGoogleApiClient();
        createLocationRequest();
    }

    //Actions, Parameters
    public void estimateRide(View view)
    {
        LocationServices.FusedLocationApi.removeLocationUpdates(client, this);

        generateGraph();

        Intent intent = new Intent("com.fabianolibano.fen.RideEstimativeActivity");
        intent.putExtra("ride", this.ride);

        if (userLocation != null)
        {
            CustomLocation customUserLocation = new CustomLocation(userLocation.getLatitude(), userLocation.getLongitude());
            intent.putExtra("userLocation", customUserLocation);
        }

        startActivity(intent);
    }



    //Interface
    protected void setUpInterface()
    {
        txtMPG = (EditText)findViewById(R.id.txtMPG);
        txtGPM = (EditText)findViewById(R.id.txtGPM);

        spnDay = (Spinner)findViewById(R.id.spnDay);
        spnDest = (Spinner)findViewById(R.id.spnDest);
        spnOrig = (Spinner)findViewById(R.id.spnOrig);
        spnTime = (Spinner)findViewById(R.id.spnTime);

        populateSpinners();
    }

    protected void populateSpinners()
    {
        // Populate crossroads
        ArrayAdapter<CharSequence> spinAdapter1 = ArrayAdapter.createFromResource(this,
                R.array.description, android.R.layout.simple_spinner_item);
        spinAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnOrig.setAdapter(spinAdapter1);
        spnOrig.setSelection(0);

        ArrayAdapter<CharSequence> spinAdapter2 = ArrayAdapter.createFromResource(this,
                R.array.description, android.R.layout.simple_spinner_item);
        spinAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnDest.setAdapter(spinAdapter2);
        spnDest.setSelection(21);

        Calendar cal = Calendar.getInstance();
        int calIndex = 0;
        int timeIndex = 5;
        int time = cal.get(Calendar.HOUR_OF_DAY) * 100 + cal.get(Calendar.MINUTE);
        if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
        {
            calIndex = 1;
            if(time >= 500 && time < 730) {
                timeIndex = 0;
                routeTimes = getResources().getStringArray(R.array.time_weekend_1);
            }
            else if (time >= 730 && time < 930) {
                timeIndex = 1;
                routeTimes = getResources().getStringArray(R.array.time_weekend_2);
            }
            else if (time >= 930 && time < 1530) {
                timeIndex = 2;
                routeTimes = getResources().getStringArray(R.array.time_weekend_3);
            }
            else if (time >= 1530 && time < 1830) {
                timeIndex = 3;
                routeTimes = getResources().getStringArray(R.array.time_weekend_4);
            }
            else if (time >= 1830 && time < 2130) {
                timeIndex = 4;
                routeTimes = getResources().getStringArray(R.array.time_weekend_5);
            }
            else
            {
                routeTimes = getResources().getStringArray(R.array.time_weekend_6);
            }
        }
        else
        {
            if(time >= 500 && time < 730) {
                timeIndex = 0;
                routeTimes = getResources().getStringArray(R.array.time_weekday_1);
            }
            else if (time >= 730 && time < 930) {
                timeIndex = 1;
                routeTimes = getResources().getStringArray(R.array.time_weekday_2);
            }
            else if (time >= 930 && time < 1530) {
                timeIndex = 2;
                routeTimes = getResources().getStringArray(R.array.time_weekday_3);
            }
            else if (time >= 1530 && time < 1830) {
                timeIndex = 3;
                routeTimes = getResources().getStringArray(R.array.time_weekday_4);
            }
            else if (time >= 1830 && time < 2130) {
                timeIndex = 4;
                routeTimes = getResources().getStringArray(R.array.time_weekday_5);
            }
            else
            {
                routeTimes = getResources().getStringArray(R.array.time_weekday_6);
            }
        }

        List<String> dayItems =  new ArrayList<String>();
        dayItems.add("Weekday");
        dayItems.add("Weekend");
        ArrayAdapter<String> spinAdapter3 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dayItems);
        spinAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnDay.setAdapter(spinAdapter3);
        spnDay.setSelection(calIndex);

        List<String> timeItems =  new ArrayList<String>();
        timeItems.add("Morning");
        timeItems.add("Peak (Morning)");
        timeItems.add("Mid-day");
        timeItems.add("Peak (Afternoon)");
        timeItems.add("Evening");
        timeItems.add("Night");
        ArrayAdapter<String> spinAdapter4 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, timeItems);
        spinAdapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnTime.setAdapter(spinAdapter4);
        spnTime.setSelection(timeIndex);

    }

    // Route graph
    public void generateGraph()
    {
        map = new mapGraph();
        int timeIndex, dayIndex;

        timeIndex = spnTime.getSelectedItemPosition();
        dayIndex = spnDay.getSelectedItemPosition();

        if(dayIndex == 1)
        {
            if(timeIndex==0) {
                routeTimes = getResources().getStringArray(R.array.time_weekend_1);
            }
            else if (timeIndex==1) {
                routeTimes = getResources().getStringArray(R.array.time_weekend_2);
            }
            else if (timeIndex==2) {
                routeTimes = getResources().getStringArray(R.array.time_weekend_3);
            }
            else if (timeIndex==3) {
                routeTimes = getResources().getStringArray(R.array.time_weekend_4);
            }
            else if (timeIndex==4) {
                routeTimes = getResources().getStringArray(R.array.time_weekend_5);
            }
            else
            {
                routeTimes = getResources().getStringArray(R.array.time_weekend_6);
            }
        }
        else
        {
            if(timeIndex==0) {
                routeTimes = getResources().getStringArray(R.array.time_weekday_1);
            }
            else if (timeIndex==1) {
                routeTimes = getResources().getStringArray(R.array.time_weekday_2);
            }
            else if (timeIndex==2) {
                routeTimes = getResources().getStringArray(R.array.time_weekday_3);
            }
            else if (timeIndex==3) {
                routeTimes = getResources().getStringArray(R.array.time_weekday_4);
            }
            else if (timeIndex==4) {
                routeTimes = getResources().getStringArray(R.array.time_weekday_5);
            }
            else
            {
                routeTimes = getResources().getStringArray(R.array.time_weekday_6);
            }
        }



        // populate the nodes from the xml
        String[] locations = getResources().getStringArray(R.array.lat_lon);
        String[] probs = getResources().getStringArray(R.array.probability);
        String[] delays = getResources().getStringArray(R.array.red_delay);

        for(int i = 0; i < locations.length; i++)
        {
            map.addNode(locations[i], Double.valueOf(probs[i]), Double.valueOf(delays[i]), i);
        }

        String[] src = getResources().getStringArray(R.array.u);
        String[] dst = getResources().getStringArray(R.array.v);
        String[] distance = getResources().getStringArray(R.array.distance);

        for(int i = 0; i < routeTimes.length; i++)
        {
            map.addEdge(map.getNode(Integer.valueOf(src[i])), map.getNode(Integer.valueOf(dst[i])), Double.valueOf(distance[i]), Double.valueOf(routeTimes[i]), i);
        }

        // Below code runs tha shortest path algorithm
        node start = map.getNode(spnOrig.getSelectedItemPosition());
        node end = map.getNode(spnDest.getSelectedItemPosition());

        LinkedList<node> route = map.runDijkstra(start, end, Double.valueOf(txtGPM.getText().toString()), (1.0/Double.valueOf(txtMPG.getText().toString())));
        ride.route.clear();
        if(route != null) {
            // Below code goes through path to get location/stops
            Iterator<node> itr = route.descendingIterator();
            while (itr.hasNext()) {
                CustomLocation intersection = itr.next().getLoc();
                ride.route.add(intersection);
            }

            // Below code calculates predicted time of trip
            double elapsedTimeMin = map.getRouteTime();

            // Below code calculated fuel efficiency of trip
            double gallonsConsumed = map.getRouteConsumption();

            // Add info to ride object
            ride.origin = locations[spnOrig.getSelectedItemPosition()];
            ride.destination = locations[spnDest.getSelectedItemPosition()];
            ride.estimatedTime = elapsedTimeMin;
            ride.fuelConsumed = gallonsConsumed;
        }
    }

    //Location
    protected synchronized void buildGoogleApiClient()
    {
        client = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        client.connect();
    }

    protected void createLocationRequest()
    {
        request = new LocationRequest();
        request.setInterval(3000);
        request.setFastestInterval(1000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        //Alert
    }

    public void onNothingSelected(AdapterView<?> parent)
    {
        //Alert
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        userLocation = LocationServices.FusedLocationApi.getLastLocation(client);
        LocationServices.FusedLocationApi.requestLocationUpdates(client, request, this);
    }

    @Override
    public void onLocationChanged(Location location)
    {
        userLocation = location;
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        //Alert
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        //Alert
    }
}
