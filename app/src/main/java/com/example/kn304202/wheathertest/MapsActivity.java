package com.example.kn304202.wheathertest;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.InfoWindowAdapter {

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 0;

    //Maps
    private GoogleMap mMap;
    Marker marker;

    //Location
    LocationManager locationManager;
    private String sProvider;
    Location location;

    //AsyncTask
    GetWheatherData wheatherData;

    //Json --> Gson
    Gson gson;
    GsonWheather gsWheather;

    Boolean bDataReady = false;
    LayoutInflater inflater;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        inflater = getLayoutInflater();

        //Location Stuff -----------------------------------------------------------------------------------------------------------------------------------------/

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (!(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) && !(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Vous devez activer la localisation")
                        .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // FIRE ZE MISSILES!
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog, and quit the App
                                finishAndRemoveTask();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }

            Criteria criteria = new Criteria();
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            sProvider = locationManager.getBestProvider(criteria, true);
            location = locationManager.getLastKnownLocation(sProvider);
            locationManager.requestLocationUpdates(sProvider, 30000, 1, this);


        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        }



        //-----------------------------------------------------------------------------------------------------------------------------------------/


        mapFragment.getMapAsync(this);

        if (location == null)
            Toast.makeText(this, "Location not available ..", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_FINE_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    if (!(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) && !(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage("Vous devez activer la localisation")
                                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // FIRE ZE MISSILES!
                                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // User cancelled the dialog, and quit the App
                                        finishAndRemoveTask();
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }

                    Criteria criteria = new Criteria();
                    criteria.setPowerRequirement(Criteria.POWER_LOW);
                    sProvider = locationManager.getBestProvider(criteria, true);
                    location = locationManager.getLastKnownLocation(sProvider);
                    locationManager.requestLocationUpdates(sProvider, 30000, 1, this);

                } else {

                    finishAndRemoveTask();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;

        }

    }


    //AsyncTask
    private class GetWheatherData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... sUrls) {
            //we use the OkHttp library from https://github.com/square/okhttp
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(sUrls[0])
                    .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
                if(response.isSuccessful())
                    return response.body().string();
                else
                    return "Request failed ..";
            } catch (IOException e) {
                e.printStackTrace();
                return "Request failed ..";
            }

        }

        @Override
        protected void onPostExecute(String sJson) {
            super.onPostExecute(sJson);

            if(!sJson.equals("Request failed ..")){
                //Construction de l'objet général "gsWheather"
                //--> La donnée renvoyée par l'API dans son ensemble
                gson = new GsonBuilder().create();
                gsWheather = gson.fromJson(sJson, GsonWheather.class);
                gsWheather.weather[0].translateFrench();


                /**********************************************ON VA FAIRE AUTREMENT PLUS SIMPLE****************************************
                //On découpe la String --> construire "gsMain"
                int iStart, iEnd;
                iStart = s.indexOf("temp") - 2;
                iEnd = s.indexOf("},", iStart) + 1;
                String subS = s.substring(iStart, iEnd);

                //Construction de "gsInWheather(s)"
                //--> contient la description, et l'icone
                gson = new GsonBuilder().create();
                gsMain = gson.fromJson(subS, GsonWheather.main.class);

                //On découpe la String -->
                //l'API renvoie tantôt 1 Json, tantôt un tableau de Json
                iStart = s.indexOf("id") - 2;
                iEnd = s.indexOf("]", iStart);
                subS = s.substring(iStart, iEnd);

                gson = new GsonBuilder().create();
                if(subS.contains("},{")){
                    subS = "[" + subS + "]";
                    gsInWeathers = gson.fromJson(subS, GsonWheather.weather[].class);
                    gsInWheather = gsInWeathers[0];
                } else {
                    gsInWheather = gson.fromJson(subS, GsonWheather.weather.class);
                }

                gsInWheather.translateFrench();
                *****************************************************************************************************************/



                Toast.makeText(getApplicationContext(), gsWheather.weather[0].description, Toast.LENGTH_SHORT).show();

                mMap.setInfoWindowAdapter(MapsActivity.this);
                marker.showInfoWindow();
                bDataReady = true;

            } else {
                marker.setTitle("Erreur");
                marker.setSnippet("Veuillez réessayer plus tard..");
                marker.showInfoWindow();
            }

        }
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney;
        if (location != null)
            sydney = new LatLng(location.getLatitude(), location.getLongitude());
        else
            sydney = new LatLng(32.482895, 3.674763);

        //Display the marker
        marker = mMap.addMarker(new MarkerOptions().position(sydney).title("Gherdaïa").snippet("Waiting.."));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 9));
        marker.setTitle("Waiting..");
        marker.setSnippet("...");
        marker.showInfoWindow();

        //Get wheather informations, and display it on the marker
        wheatherData = new GetWheatherData();
        wheatherData.execute("http://api.openweathermap.org/data/2.5/weather?lat=" + sydney.latitude + "&lon=" + sydney.longitude + "&units=metric&lang=fr&appid=" + getResources().getString(R.string.open_wheather_API_key));

        //Map stuff..
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        //On click, get the selected place's wheather data
         mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
             @Override
             public void onMapClick(LatLng latLng) {

                 mMap.setInfoWindowAdapter(null);
                 marker.setPosition(latLng);
                 marker.setTitle("Waiting..");
                 marker.setSnippet("...");
                 marker.showInfoWindow();
                 bDataReady = false;

                 wheatherData = new GetWheatherData();
                 wheatherData.execute("http://api.openweathermap.org/data/2.5/weather?lat=" + latLng.latitude + "&lon=" + latLng.longitude + "&units=metric&lang=fr&appid=" + getResources().getString(R.string.open_wheather_API_key));
             }
         });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if(bDataReady){
                    Intent intent = new Intent(MapsActivity.this, ForecastActivity.class);
                    intent.putExtra("CITY_ID", gsWheather.id);
                    intent.putExtra("CITY_NAME", gsWheather.name);
                    startActivity(intent);
                }
            }
        });


    }

    //For the custom Info Window (on click)--/
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        //return null;
        View customView = inflater.inflate(R.layout.custom_infowindow, null);

        TextView tvCity = (TextView) customView.findViewById(R.id.tvCity);
        TextView tvTemp = (TextView) customView.findViewById(R.id.tvCurrentTemp);
        TextView tvHumidity = (TextView) customView.findViewById(R.id.tvHumidity);
        ImageView ivCurrent = (ImageView) customView.findViewById(R.id.ivCurrent);

        tvCity.setText(gsWheather.name);
        tvTemp.setText(gsWheather.main.temp + "°C");
        tvHumidity.setText("Humidité: " + gsWheather.main.humidity + " %");

        ivCurrent.setImageDrawable(getDrawable(getResources().getIdentifier("_" + gsWheather.weather[0].icon, "drawable", getPackageName())));

        return customView;
    }


    //---NOT USED ANYMORE .../
    private View prepareInfoView(Marker marker){
        //prepare InfoView programmatically

        LinearLayout mainLayout = new LinearLayout(MapsActivity.this);
        LinearLayout.LayoutParams mainLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setLayoutParams(mainLayoutParams);

        TextView tvCity = new TextView(MapsActivity.this);
        tvCity.setTypeface(null, Typeface.BOLD);
        tvCity.setTextColor(Color.BLACK);
        tvCity.setGravity(Gravity.CENTER_HORIZONTAL);
        tvCity.setText(gsWheather.name);


        LinearLayout infoView = new LinearLayout(MapsActivity.this);
        LinearLayout.LayoutParams infoViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        infoView.setOrientation(LinearLayout.HORIZONTAL);
        infoView.setLayoutParams(infoViewParams);


        ImageView infoImageView = new ImageView(MapsActivity.this);

        //On récupère l'îcone correspondante ..
        infoImageView.setImageDrawable(getResources().getDrawable(getResources().getIdentifier("_" + gsWheather.weather[0].icon, "drawable", getPackageName())));

        /*
        switch(gsInWheather.description){
            case "Ciel dégagé":
                drawable = getResources().getDrawable(R.drawable.sunny);
                infoImageView.setImageDrawable(drawable);
                break;

            case "Quelques nuages ..":
            drawable = getResources().getDrawable(R.drawable.cloudy1);
                infoImageView.setImageDrawable(drawable);
            break;

            case "Nuages dispersés":
                drawable = getResources().getDrawable(R.drawable.cloudy3);
                infoImageView.setImageDrawable(drawable);
            break;

            case "Ciel couvert ..":
                drawable = getResources().getDrawable(R.drawable.cloudy5);
                infoImageView.setImageDrawable(drawable);
            break;

            case "Légères averses":
                drawable = getResources().getDrawable(R.drawable.shower1);
                infoImageView.setImageDrawable(drawable);
            break;

            default:
                drawable = getResources().getDrawable(R.drawable.sunny);
                infoImageView.setImageDrawable(drawable);


        }*/


        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(120,120);
        infoImageView.setLayoutParams(params);
        infoView.addView(infoImageView);

        LinearLayout subInfoView = new LinearLayout(MapsActivity.this);
        LinearLayout.LayoutParams subInfoViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        subInfoView.setOrientation(LinearLayout.VERTICAL);
        subInfoView.setLayoutParams(subInfoViewParams);


        TextView tvTemp = new TextView(MapsActivity.this);
        tvTemp.setText(gsWheather.main.temp + "°C");
        tvTemp.setTypeface(null, Typeface.BOLD);
        tvTemp.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);

        TextView tvHumidity = new TextView(MapsActivity.this);
        tvHumidity.setText("Humidity: " + gsWheather.main.humidity + " %");

        subInfoView.addView(tvTemp);
        subInfoView.addView(tvHumidity);
        infoView.addView(subInfoView);

        mainLayout.addView(tvCity);
        mainLayout.addView(infoView);

        return mainLayout;



    }


    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(sProvider, 400, 1, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(locationManager != null)
            locationManager.removeUpdates(this);
    }

    //Location Listener---------------------------------------------------------------------------------------------------------------------/
        @Override
        public void onLocationChanged(Location location) {
            //Toast.makeText(getApplicationContext(), "Lat: " + String.valueOf(location.getLatitude()), Toast.LENGTH_LONG).show();
            marker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Toast.makeText(getApplicationContext(), "Status Changed !" , Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            //Toast.makeText(getApplicationContext(), "Lat: " + String.valueOf(location.getLatitude()), Toast.LENGTH_LONG).show();
            marker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    //------------------------------------------------------------------------------------------------------------------------------/
}
