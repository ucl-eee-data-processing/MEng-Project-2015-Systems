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
//import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.net.URLConnection;
import java.net.URL;
import java.io.InputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;

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
    public static String weightsToString(double [] weights){
        return  Double.valueOf(weights[0]).toString() + "-" + 
                Double.valueOf(weights[1]).toString() + "-" + 
                Double.valueOf(weights[2]).toString();
    }
  
    public static double [] stringToWeights(String strWeights){
        String [] strWeightArray = strWeights.split("-");
        double [] doubleWeightArray = new double[3];
        
        for(int i=0; i < strWeightArray.length; i++){
            doubleWeightArray[i] = Double.parseDouble(strWeightArray[i]);
        }
        return doubleWeightArray;
    }
    
    public ArrayList<Double> weatherData(String lat, String lon){
        try{
            ArrayList<Double> currForecast = new ArrayList<Double>(); 
            ObjectMapper mapper = new ObjectMapper();
            String url = "http://api.openweathermap.org/data/2.5/weather?" + 
                          "lat=" + lat + "&" +
                          "lon=" + lon + "&" +
                          "APPID=" + this.OWN_API_KEY;
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
    
}