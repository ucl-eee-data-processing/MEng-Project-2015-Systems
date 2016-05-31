package com.cloudera.oryx.lazarus.speed;
import java.io.Serializable;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DataPreProcessor implements Serializable{
	
    
    public LabeledPoint getLabeledPoint(String line){
            
         LabeledPoint lp = null;    
            
        try{
                JSONObject json = (JSONObject) new JSONParser().parse(line);
                JSONObject weatherJson = (JSONObject) json.get("weather");
                
                 String time = json.get("time").toString();
                 String energy = json.get("energy").toString();
                 String temp = weatherJson.get("temp").toString();
                 String daylight = weatherJson.get("daylight").toString();

                double timeValue = Double.parseDouble(time);
                double energyValue = Double.parseDouble(energy);
                double tempValue = Double.parseDouble(temp);
                double daylightValue = Double.parseDouble(daylight);
                
                lp = new LabeledPoint(energyValue, Vectors.dense(timeValue/1E10, tempValue/1000, daylightValue/100000));
               // lp = new LabeledPoint(energyValue, Vectors.dense(timeValue, tempValue, daylightValue));
       
        }catch(ParseException e){
            System.out.println(e.getMessage());
        }
                
               return lp ;
            
    }
    
    
    
    

}
