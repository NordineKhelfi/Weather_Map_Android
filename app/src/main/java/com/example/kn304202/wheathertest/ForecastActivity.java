package com.example.kn304202.wheathertest;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ForecastActivity extends AppCompatActivity {

    long lCityId;
    TextView tvForecastTest;
    GsonForecast[] gsonForecasts;
    Gson gson;

    DateFormat format;
    Date date;

    TextView tvJr1, tvJr2, tvJr3, tvJr4, tvJr5, tvMat1, tvMat2, tvMat3, tvMat4, tvMat5, tvAprem1, tvAprem2, tvAprem3, tvAprem4, tvAprem5;
    ImageView ivJr1, ivJr2, ivJr3, ivJr4, ivJr5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        format = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");

        Intent i = getIntent();
        lCityId = i.getLongExtra("CITY_ID", 0);


        this.setTitle( i.getStringExtra("CITY_NAME"));
        tvForecastTest = (TextView) findViewById(R.id.tvForecastTest);
        tvForecastTest.setMovementMethod(new ScrollingMovementMethod());

        tvJr1 = (TextView) findViewById(R.id.tvJr1);
        tvJr2 = (TextView) findViewById(R.id.tvJr2);
        tvJr3 = (TextView) findViewById(R.id.tvJr3);
        tvJr4 = (TextView) findViewById(R.id.tvJr4);
        tvJr5 = (TextView) findViewById(R.id.tvJr5);

        tvMat1 = (TextView) findViewById(R.id.tvTmpMatinJr1);
        tvMat2 = (TextView) findViewById(R.id.tvTmpMatinJr2);
        tvMat3 = (TextView) findViewById(R.id.tvTmpMatinJr3);
        tvMat4 = (TextView) findViewById(R.id.tvTmpMatinJr4);
        tvMat5 = (TextView) findViewById(R.id.tvTmpMatinJr5);

        tvAprem1 = (TextView) findViewById(R.id.tvTmpApremJr1);
        tvAprem2 = (TextView) findViewById(R.id.tvTmpApremJr2);
        tvAprem3 = (TextView) findViewById(R.id.tvTmpApremJr3);
        tvAprem4 = (TextView) findViewById(R.id.tvTmpApremJr4);
        tvAprem5 = (TextView) findViewById(R.id.tvTmpApremJr5);

        ivJr1 = (ImageView) findViewById(R.id.ivJr1);
        ivJr2 = (ImageView) findViewById(R.id.ivJr2);
        ivJr3 = (ImageView) findViewById(R.id.ivJr3);
        ivJr4 = (ImageView) findViewById(R.id.ivJr4);
        ivJr5 = (ImageView) findViewById(R.id.ivJr5);

        GetForecastData atForecast = new GetForecastData();
        atForecast.execute("http://api.openweathermap.org/data/2.5/forecast?id=" + String.valueOf(lCityId) + "&units=metric&lang=fr&appid=" + getResources().getString(R.string.open_wheather_API_key));


    }


    //AsyncTask for http request --> getting Json-formatted wheather data

    private class GetForecastData extends AsyncTask<String, Void, String>{

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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //VARIABLES

            //valeurs des prévisions 09:00 / 15:00 sur 5 jours
            List<Double> dTempsMatin = new ArrayList<Double>();
            List<Double> dTempsAprem = new ArrayList<Double>();
            int iStart, iEnd;
            String[] sjours = new String[5];
            List<String> lIcones = new ArrayList<String>();
            int i = 0;
            //SDF
            SimpleDateFormat sdFormat = new SimpleDateFormat("E");



            //On découpe la String pour récupérer la partie du tableau..
            iStart = s.indexOf("[");
            if(s.contains("]}"))
                iEnd = s.indexOf("]}") + 1;
            else
                iEnd = s.indexOf("city") - 2;
            String sSub = s.substring(iStart, iEnd);

            //.. à partir de quoi on construit notre tableau de "GsonForcast"
            gson = new GsonBuilder().create();
            gsonForecasts = gson.fromJson(sSub, GsonForecast[].class);

            //On parcours notre tableau de "Forcasts" pour préparer nos données.
            for(GsonForecast gsForecast : gsonForecasts){
                //sTest += gsForecast.dt_txt + " " + gsForecast.main.temp + "\n\n";

                //On remplit les tableau de température Matin et Aprèm
                if(gsForecast.dt_txt.contains("09:00")){
                    dTempsMatin.add(gsForecast.main.temp);
                    lIcones.add(gsForecast.weather[0].icon);

                }
                else if(gsForecast.dt_txt.contains("15:00")){
                    dTempsAprem.add(gsForecast.main.temp);

                }

                //On récupère les 5 prochains Jours de la semaines
                if(gsForecast.dt_txt.contains("21:00")){

                    try {
                        //date = format.parse(gsForecast.dt_txt);
                        sjours[i++] = sdFormat.format(format.parse(gsForecast.dt_txt));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }


            }


            //On affiche
            if(dTempsMatin.size() == 4 && dTempsAprem.size() == 5){
                tvMat1.setText(dTempsAprem.get(0) + "°");
                tvMat2.setText(dTempsMatin.get(0) + "°");
                tvMat3.setText(dTempsMatin.get(1) + "°");
                tvMat4.setText(dTempsMatin.get(2) + "°");
                tvMat5.setText(dTempsMatin.get(3) + "°");

                tvAprem1.setText(dTempsAprem.get(0) + "°");
                tvAprem2.setText(dTempsAprem.get(1) + "°");
                tvAprem3.setText(dTempsAprem.get(2) + "°");
                tvAprem4.setText(dTempsAprem.get(3) + "°");
                tvAprem5.setText(dTempsAprem.get(4) + "°");

                ivJr1.setImageDrawable(getResources().getDrawable(getResources().getIdentifier("_" + lIcones.get(0), "drawable", getPackageName())));
                ivJr2.setImageDrawable(getResources().getDrawable(getResources().getIdentifier("_" + lIcones.get(0), "drawable", getPackageName())));
                ivJr3.setImageDrawable(getResources().getDrawable(getResources().getIdentifier("_" + lIcones.get(1), "drawable", getPackageName())));
                ivJr4.setImageDrawable(getResources().getDrawable(getResources().getIdentifier("_" + lIcones.get(2), "drawable", getPackageName())));
                ivJr5.setImageDrawable(getResources().getDrawable(getResources().getIdentifier("_" + lIcones.get(3), "drawable", getPackageName())));


            } else if(dTempsMatin.size() == 4 && dTempsAprem.size() == 4){

                tvMat1.setText(gsonForecasts[0].main.temp + "°");
                tvMat2.setText(dTempsMatin.get(0) + "°");
                tvMat3.setText(dTempsMatin.get(1) + "°");
                tvMat4.setText(dTempsMatin.get(2) + "°");
                tvMat5.setText(dTempsMatin.get(3) + "°");

                tvAprem1.setText(gsonForecasts[0].main.temp + "°");
                tvAprem2.setText(dTempsAprem.get(0) + "°");
                tvAprem3.setText(dTempsAprem.get(1) + "°");
                tvAprem4.setText(dTempsAprem.get(2) + "°");
                tvAprem5.setText(dTempsAprem.get(3) + "°");

            } else if(dTempsMatin.size() == 5 && dTempsAprem.size() == 5){
                tvMat1.setText(dTempsMatin.get(0) + "°");
                tvMat2.setText(dTempsMatin.get(1) + "°");
                tvMat3.setText(dTempsMatin.get(2) + "°");
                tvMat4.setText(dTempsMatin.get(3) + "°");
                tvMat5.setText(dTempsMatin.get(4) + "°");

                tvAprem1.setText(dTempsAprem.get(0) + "°");
                tvAprem2.setText(dTempsAprem.get(1) + "°");
                tvAprem3.setText(dTempsAprem.get(2) + "°");
                tvAprem4.setText(dTempsAprem.get(3) + "°");
                tvAprem5.setText(dTempsAprem.get(4) + "°");

                ivJr1.setImageDrawable(getResources().getDrawable(getResources().getIdentifier("_" + lIcones.get(0), "drawable", getPackageName())));
                ivJr2.setImageDrawable(getResources().getDrawable(getResources().getIdentifier("_" + lIcones.get(1), "drawable", getPackageName())));
                ivJr3.setImageDrawable(getResources().getDrawable(getResources().getIdentifier("_" + lIcones.get(2), "drawable", getPackageName())));
                ivJr4.setImageDrawable(getResources().getDrawable(getResources().getIdentifier("_" + lIcones.get(3), "drawable", getPackageName())));
                ivJr5.setImageDrawable(getResources().getDrawable(getResources().getIdentifier("_" + lIcones.get(4), "drawable", getPackageName())));

            }

            tvJr1.setText(sjours[0]);
            tvJr2.setText(sjours[1]);
            tvJr3.setText(sjours[2]);
            tvJr4.setText(sjours[3]);
            tvJr5.setText(sjours[4]);

            //tvForecastTest.setText(sTest);
            //Toast.makeText(getApplicationContext(), "dTempsMatin : " + dTempsMatin.size() + "\n dTempAprem : " + dTempsAprem.size(), Toast.LENGTH_LONG).show();

        }
    }



}
