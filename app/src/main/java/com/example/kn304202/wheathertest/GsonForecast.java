package com.example.kn304202.wheathertest;

/**
 * Created by norma on 01/04/2017.
 */

public class GsonForecast {

    public long dt;

    public main main;
    public weather[] weather;

    public class main{
        public double temp;
        public double temp_min;
        public double temp_max;
        public double pressure;
        public double sea_level;
        public double humidity;
        public double temp_kf;
    }

    public class weather{
        int id;
        String main;
        String description;
        String icon;
    }

    public static class clouds{
        int all;
    }

    public static class wind{
        double speed;
        double deg;
    }

    public static class sys{
        public String pod;
    }

    public String dt_txt;

}
