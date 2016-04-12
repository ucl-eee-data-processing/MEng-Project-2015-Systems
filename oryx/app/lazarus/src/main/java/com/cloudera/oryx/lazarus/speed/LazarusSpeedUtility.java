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
package com.cloudera.oryx.lazarus.speed;
import com.cloudera.oryx.lazarus.serving.LazarusServingUtility;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 *
 * @author tokwii
 */
public class LazarusSpeedUtility {
    
    public LazarusSpeedUtility(){
        
    }
    public static Map<String, double[]> initializeWeights(){
        Map<String, double[] > modelWeights = 
           Collections.synchronizedMap(new HashMap<String, double[] >());
        for(int i = 0; i < 48; i ++ ){
            String timeStamp = LazarusServingUtility.indexToTime(i);
            modelWeights.put(timeStamp, new double []{ -1.0, 0.0, 0.0, 0.0, 0.0});
        }
        return modelWeights;
    }
    
    public void loadExistingWeights(){
    
    }
    public static String twentyFourHourTime(double scaledTime ){
        long small = 2L;
        double rescaler = 1000000;
        double tmpTime = scaledTime *  rescaler;
        double unixTime = tmpTime * 10000;
        long sysTime =  (long) unixTime * 1000L + small;
        Date date = new  Date(sysTime);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedDate = sdf.format(date);
        String[] timeStamp = formattedDate.split(" ");
        String [] hourMinutes = timeStamp[1].split(":");
        String time;
        System.out.println(timeStamp[1]);
        int hour = Integer.parseInt(hourMinutes[0]);
        System.out.println(hourMinutes[1]);
        if(Integer.parseInt(hourMinutes[1]) < 29){
            time = (new Integer(hour)).toString() + ":00";
        }else{
            time = (new Integer(hour)).toString()+ ":30";
        }
        return time;
    }
    
}
