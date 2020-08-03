package com.fabianolibano.fen;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.fabianolibano.fen.model.Ride;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class RideEstimativeActivity extends AppCompatActivity
{
    //Data
    private Ride ride = new Ride();
    private CustomLocation userLocation;
    private Location originLocation;
    private Location destinyLocation;
    private ArrayList<String> googlePolylines;
    private ArrayList<String> customPolylines;
    private String originString;
    private String destinyString;
    private final String key = "AIzaSyBhYK2nShGi02KknsxqOFP6PZ7TqfSMGV0";
    private String simpleUrlString;
    private String complexUrlString;

    //UI
    private TextView fuelView;
    private TextView timeView;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_estimative);

        setUpInterface();
        getParameters();

        generateStrings();

        try
        {
            try
            {
                parseJSON(requestServerResponse(simpleUrlString), 0);
                parseJSON(requestServerResponse(complexUrlString), 1);
            }
            catch (JSONException e)
            {
                //Alert
            }
        }
        catch (ExecutionException e)
        {
            //Alert
        }
        catch (InterruptedException e)
        {
           e.printStackTrace();
        }

        showUpdatedResults();
    }


    //Actions, Parameters
    protected void getParameters()
    {
        ride = (Ride)getIntent().getSerializableExtra("ride");
        userLocation = (CustomLocation)getIntent().getSerializableExtra("userLocation");
    }

    public void showGoogleSuggestedRoute(View view)
    {
        CustomLocation customOriginLocation = new CustomLocation(originLocation.getLatitude(), originLocation.getLongitude());
        CustomLocation customDestinyLocation = new CustomLocation(destinyLocation.getLatitude(), destinyLocation.getLongitude());

        Intent intent = new Intent("com.fabianolibano.fen.RideRouteActivity");
        intent.putExtra("urlString", simpleUrlString);
        intent.putExtra("originLocation", customOriginLocation);
        intent.putExtra("destinyLocation", customDestinyLocation);
        intent.putStringArrayListExtra("polylines", this.googlePolylines);
        startActivity(intent);
    }

    public void showCustomSuggestedRoute(View view)
    {
        CustomLocation customOriginLocation = new CustomLocation(originLocation.getLatitude(), originLocation.getLongitude());
        CustomLocation customDestinyLocation = new CustomLocation(destinyLocation.getLatitude(), destinyLocation.getLongitude());

        Intent intent = new Intent("com.fabianolibano.fen.RideRouteActivity");
        intent.putExtra("urlString", complexUrlString);
        intent.putExtra("originLocation", customOriginLocation);
        intent.putExtra("destinyLocation", customDestinyLocation);
        intent.putStringArrayListExtra("polylines", this.customPolylines);
        startActivity(intent);
    }



    //Interface
    protected void setUpInterface()
    {
        fuelView = (TextView)findViewById(R.id.txtUsageRes);
        timeView = (TextView)findViewById(R.id.txtTimeRes);
    }


    protected void showUpdatedResults()
    {
        DecimalFormat fmt = new DecimalFormat("##.####");
        fuelView.setText(fmt.format(ride.fuelConsumed));
        fmt = new DecimalFormat("##.##");
        timeView.setText(fmt.format(ride.estimatedTime));
    }



    //Connection
    protected String requestServerResponse(String urlString) throws ExecutionException, InterruptedException
    {
        String response = "";

        try
        {
            response = (String) new JSONHandler().execute(urlString).get();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        return response;
    }

    protected void parseJSON(String json, int mode) throws JSONException
    {
        JSONObject jsonObject = new JSONObject(json);

        JSONArray routesArray = jsonObject.getJSONArray("routes");
        JSONObject route = routesArray.getJSONObject(0);
        JSONArray legs = route.getJSONArray("legs");
        JSONObject leg = legs.getJSONObject(0);

        JSONObject startLocationObject = leg.getJSONObject("start_location");
        originLocation = new Location("");
        originLocation.setLatitude(startLocationObject.getDouble("lat"));
        originLocation.setLongitude(startLocationObject.getDouble("lng"));

        JSONObject endLocationObject = leg.getJSONObject("end_location");
        destinyLocation = new Location("");
        destinyLocation.setLatitude(endLocationObject.getDouble("lat"));
        destinyLocation.setLongitude(endLocationObject.getDouble("lng"));

        JSONArray stepsArray = leg.getJSONArray("steps");

        if(mode == 0)
            googlePolylines = new ArrayList<>();
        else
            customPolylines = new ArrayList<>();

        for (int i=0; i<stepsArray.length(); i++)
        {
            String polyline = "";

            polyline = stepsArray.getJSONObject(i).getJSONObject("polyline").getString("points");
            if(mode == 0)
                googlePolylines.add(polyline);
            else
                customPolylines.add(polyline);
        }
    }

    //Strings
    protected void generateStrings()
    {
        originString = ride.origin;
        originString = formatString(originString);
        
        destinyString = ride.destination;
        destinyString = formatString(destinyString);

        simpleUrlString = "https://maps.googleapis.com/maps/api/directions/json?origin=" + originString +  "&destination=" + destinyString + "&mode=driving&key=" + key;

        complexUrlString = "https://maps.googleapis.com/maps/api/directions/json?origin=" + originString +  "&destination=" + destinyString + "&waypoints=";
        String waypoints = "";
        for(int i=0; i<ride.route.size(); i++)
            waypoints = waypoints + "via:" + ride.route.get(i).getLatitude() + "," + ride.route.get(i).getLongitude() + "|";
        complexUrlString = complexUrlString + waypoints + "&mode=driving&key=" + key;
    }

    protected String formatString(String string)
    {
        string = string.replace(' ', '+');

        return string;
    }
}