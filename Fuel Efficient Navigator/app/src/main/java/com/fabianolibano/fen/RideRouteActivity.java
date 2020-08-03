package com.fabianolibano.fen;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class RideRouteActivity extends ActionBarActivity //FragmentActivity
{
    //Data
    private String urlString;
    private CustomLocation originLocation;
    private CustomLocation destinyLocation;
    private ArrayList<String> polylines;

    //UI
    private GoogleMap routeMap;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_route);

        getParameters();

        setUpMap();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ride_route, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_launch_gmaps:
                launchGoogleMaps();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    //Activity, Parameters
    protected void getParameters()
    {
        urlString = getIntent().getStringExtra("urlString");
        originLocation = (CustomLocation)getIntent().getSerializableExtra("originLocation");
        destinyLocation = (CustomLocation)getIntent().getSerializableExtra("destinyLocation");
        polylines = getIntent().getStringArrayListExtra("polylines");
    }



    //Map
    private void setUpMap()
    {
        if (routeMap == null)
        {
            routeMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            if (routeMap != null)
            {
                addMarkersAndRoute();
                routeMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener()
                {
                    @Override
                    public void onCameraChange(CameraPosition cameraPosition)
                    {
                        routeMap.animateCamera(CameraUpdateFactory.newLatLngBounds(calculateMapBounds(), 50));
                        routeMap.setOnCameraChangeListener(null);
                    }
                });

                
                routeMap.setMyLocationEnabled(true);
                routeMap.setTrafficEnabled(true);
                routeMap.getUiSettings().setCompassEnabled(true);
                routeMap.getUiSettings().setRotateGesturesEnabled(true);
            }
        }
    }

    private void addMarkersAndRoute()
    {
        //routeMap.setTrafficEnabled(true);
        routeMap.addMarker(new MarkerOptions().position(new LatLng(originLocation.getLatitude(), originLocation.getLongitude())).title("Origin").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).flat(true));
        routeMap.addMarker(new MarkerOptions().position(new LatLng(destinyLocation.getLatitude(), destinyLocation.getLongitude())).title("Destination"));
        routeMap.addPolyline(generateRoute());

    }

    private PolylineOptions generateRoute()
    {
        PolylineOptions polylineOptions = new PolylineOptions();
        for (int i=0; i<polylines.size(); i++)
        {
            List<LatLng> stepPolylines = decodePoly(polylines.get(i));
            for (int j=0; j<stepPolylines.size(); j++)
            {
                polylineOptions.add(stepPolylines.get(j));
            }
        }

        polylineOptions.color(0xb30569fe);
        polylineOptions.width(33);

        return polylineOptions;
    }

    private List<LatLng> decodePoly(String encoded)
    {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len)
        {
            int b, shift = 0, result = 0;
            do
            {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do
            {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    private LatLngBounds calculateMapBounds()
    {
        LatLng origin = new LatLng(originLocation.getLatitude(), originLocation.getLongitude());
        LatLng destiny = new LatLng(destinyLocation.getLatitude(), destinyLocation.getLongitude());

        LatLngBounds bounds = LatLngBounds.builder().include(origin).include(destiny).build();

        return bounds;
    }

    private void launchGoogleMaps()
    {
        String intentUrl = "https://www.google.com/maps/dir/?api=1&" + (urlString.split("\\?", 2)[1]).replace("via:","");
        Uri gmIntentUri = Uri.parse(intentUrl);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }
}