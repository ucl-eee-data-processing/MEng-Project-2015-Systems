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
import org.bitpipeline.lib.owm.OwmClient;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
//import java.util.Date;

/**
 *
 * @author tokwii
 */
public class LazarusServingUtility {
    public static final String OWN_API_KEY = "795c1ff0b7c8af640f1f88310e296cd8";
    private OwmClient owm;
 
    LazarusServingUtility(){
    }

    public void getCurrentWeather(String lat, String lon){
        this.owm = new OwmClient();
        this.owm.setAPPID(OWN_API_KEY);
        //Object weatherData = this.owm.currentWeatherAtCity((float)lat, (float) lon,5);
        //System.out.println(weatherData);
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
}