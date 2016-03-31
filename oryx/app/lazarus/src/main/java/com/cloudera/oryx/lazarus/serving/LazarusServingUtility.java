/*
 * Copyright 2016 tokwii.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudera.oryx.lazarus.serving;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.net.URLConnection;
import java.net.URL;
import java.io.InputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.TimeZone;

/**
 *
 * @author tokwii
 */
public class LazarusServingUtility {
    private final String OWN_API_KEY = "795c1ff0b7c8af640f1f88310e296cd8";
    //private OwmClient owm;
 
     public LazarusServingUtility(){
     }
 
    public Map<String,Integer> predictedDummy(String start, String end){
        HashMap<String, Integer> energyData = new HashMap<String, Integer>();
        Random rand = new Random();
        String startTime = start.split("T")[1];
        String endTime = end.split("T")[1];
        int startIndex = timeToIndex(startTime);
        int endIndex = timeToIndex(endTime);
        for(int hour = startIndex; hour <= endIndex ; hour++){
            int power = rand.nextInt((25000 - 20000) + 1) + 20000;
            String timeStamp = start.split("T")[0] + "T" + indexToTime(hour);
            energyData.put(timeStamp, power);
        }
        return energyData;
    }
    
    public static Map<String, Long> stringUnixTime(String start, String end){
        try{
            HashMap<String, Long> strUnixTime = new HashMap<String, Long>();
            String startTime = start.split("T")[1];
            String endTime = end.split("T")[1];
            int startIndex = timeToIndex(startTime);
            int endIndex = timeToIndex(endTime);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd HH:mm");
            dateFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
            for(int hour = startIndex; hour <= endIndex ; hour++){       
                String dateString = start.split("T")[0] + " " + indexToTime(hour); 
                Date date = dateFormat.parse(dateString);
                long unixTime = date.getTime()/1000;
                String timeStamp = start.split("T")[0] + "T" + indexToTime(hour);
                strUnixTime.put(timeStamp, Long.valueOf(unixTime));
            }
            return strUnixTime;
        }catch(ParseException e){
            System.out.println(e.getStackTrace());
            return null;
        }
    }

    public static int timeToIndex(String twentyFourHourTime){
        String[] timeArray = twentyFourHourTime.split(":");
        if(Integer.parseInt(timeArray[1]) == 30){
            return 2*Integer.parseInt(timeArray[0]) + 1;
        }else{
            return 2*Integer.parseInt(timeArray[0]);
        }
    }
 
    public static String indexToTime(int index){
        if (index%2 == 0){
            String hour = Integer.toString(index/2);
            String time = hour + ":00";
            if(time.length() != 5){
                time = "0" + time;
            }
            return time;
        }else{
            String hour = Integer.toString(index/2);
            String time = hour + ":30";
            if(time.length() != 5){
                time = "0" + time;
            }
            return time;
        }
    
    }
    
    
    public static String weightsToString(int index, double [] weights){
        double timeIndex = (double) index;
        return  Double.valueOf(timeIndex).toString() + "-" + 
                Double.valueOf(weights[0]).toString() + "-" + 
                Double.valueOf(weights[1]).toString() + "-" + 
                Double.valueOf(weights[2]).toString();
    }
  
    public static double [] stringToWeights(String strWeights){
        String [] strWeightArray = strWeights.split("-");
        double [] doubleWeightArray = new double[4];
        for(int i=0; i < strWeightArray.length; i++)    {        
            doubleWeightArray[i] = Double.parseDouble(strWeightArray[i]);
        }
        return doubleWeightArray;
    }
    
    public static ArrayList<Double> weatherData(String lat, String lon){
        try{
            ArrayList<Double> currForecast = new ArrayList<Double>(); 
            ObjectMapper mapper = new ObjectMapper();
            String url = "http://api.openweathermap.org/data/2.5/weather?" + 
                          "lat=" + lat + "&" +
                          "lon=" + lon + "&" +
                          "APPID=" + "795c1ff0b7c8af640f1f88310e296cd8";
            URLConnection connection = new URL(url).openConnection();
            InputStream response = connection.getInputStream(); 
            JsonNode rootNode = mapper.readValue(response, JsonNode.class);
            JsonNode temp = rootNode.path("main").path("temp");
            JsonNode sunrise = rootNode.path("sys").path("sunrise");
            JsonNode sunset = rootNode.path("sys").path("sunset");
            
            Double sunsetValue = Double.parseDouble(sunset.toString());
            Double sunriseValue = Double.parseDouble(sunrise.toString());
            Double temperature = Double.parseDouble(temp.toString());
            Double daylight = Double.valueOf(sunsetValue.doubleValue() - sunriseValue.doubleValue());
            currForecast.add(0,temperature);
            currForecast.add(1,daylight);
            System.out.println(currForecast);
            return currForecast;
        }catch(IOException e){
             System.err.println(e);
             
        }
        return null;
    }
    
    public static Map<String, ArrayList<Double>> scaledTestDataSet(String start, String end){
        //Lat and Log for Cornell
        String lat = "42.44";
        String log = "-76.53";
        Map<String, ArrayList<Double>> testData = new HashMap<String, ArrayList<Double>>(); 
        Map<String, Long> timeUnix = stringUnixTime(start,end);
        ArrayList<Double> weatherForeCast = weatherData(lat, log);
        for (String key: timeUnix.keySet()){
            ArrayList<Double> scaledData = new ArrayList<Double>(3);
            double scaledTime = timeUnix.get(key).longValue()/1E10;
            double scaledTemp = weatherForeCast.get(0).doubleValue() /1000;
            double scaledDaylight = weatherForeCast.get(1).doubleValue() /100000;
            scaledData.add(0, new Double(scaledTime));
            scaledData.add(1, new Double(scaledTemp));
            scaledData.add(2, new Double(scaledDaylight));
            testData.put(key, scaledData);
        }
        return testData;
    }
    
}