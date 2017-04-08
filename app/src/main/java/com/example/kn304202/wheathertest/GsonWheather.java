package com.example.kn304202.wheathertest;

/**
 * Created by kn304202 on 31/03/2017.
 * Used to parse JSON data using Google class Gson.
 */

public class GsonWheather {

    public weather[] weather;
    public main main;

    public static class coord{
        double lon;
        double lat;
    }

    public class weather{
        int id;
        String main;
        String description;
        String icon;


        public void translateFrench(){

            switch(description){
                case "few clouds":
                    description = "Quelques nuages ..";
                    break;

                case "overcast clouds":
                    description = "Ciel couvert ..";
                    break;

                case "clear sky":
                    description = "Ciel dégagé";
                    break;

                case "light rain":
                    description = "Légères averses";
                    break;

                case "scattered clouds":
                    description = "Nuages dispersés";
                    break;
            }

        }


    }

    String base;

    public class main{

        double temp;
        double pressure;
        double humidity;
        double temp_min;
        double temp_max;
    }

    int visibility;

    public static class wind{
        double speed;
        double deg;
    }

    public static class clouds{
        int all;
    }

    long dt;

    public static class sys{
        int type;
        int id;
        double message;
        String country;
        long sunrise;
        long sunset;
    }

    long id;
    String name;
    int cod;


}
