package com.cloudera.oryx.lazarus.speed;

import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
//import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.apache.spark.mllib.linalg.Vectors;

import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.regression.LinearRegressionModel;
import org.apache.spark.mllib.regression.LinearRegressionWithSGD;
import org.apache.spark.rdd.RDD;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import scala.Tuple2;

public class RegressionModelBuilder implements Serializable {

    String[] hoursOfDay = {"00:00", "00:30", "01:00", "01:30", "2:0", "02:30", "03:00", "03:30", "4:0", "4:30",
        "5:0", "5:30", "6:0", "6:3", "7:0", "7:30", "8:0", "8:30", "9:0", "9:30", "10:0", "10:30",
        "11:0", "11:30", "12:0", "12:30", "13:0", "13:30", "14:0", "14:30", "15:0", "15:30", "16:0", "16:30",
        "17:0", "17:30", "18:0", "18:30", "19:0", "19:30", "20:0", "20:30", "21:0", "21:30", "22:0", "22:30",
        "23:0", "23:30"};

    long numOfIterations = 0;

    Map<String, JavaRDD<LabeledPoint>> labelMap = new HashMap<>();
    Map<String, double[]> thetaMap = new HashMap<>();
    List<Double> errorList = new ArrayList<>();

    //Map is initialised when the LazarusSpeedModelManager object is created
    public RegressionModelBuilder() {
        initMap();
    }

    private void initMap() {
        for (int i = 0; i < hoursOfDay.length; i++) {
            double[] thetas = new double[]{0, 0, 0};
            thetaMap.put(hoursOfDay[i], thetas);
        }

    }

    public LinearRegressionModel buildModel(JavaRDD<LabeledPoint> dataPoint, double[] thetas) {

        RDD<LabeledPoint> rdd = JavaRDD.toRDD(dataPoint);

        LinearRegressionWithSGD model = new LinearRegressionWithSGD();

        //  model.setFeatureScaling(true);    
        model.setIntercept(true);
        model.optimizer().setNumIterations(1);
        model.optimizer().setStepSize(1);
        // Pass 4 Paramters

        LinearRegressionModel model1 = model.run(rdd, Vectors.dense(thetas));

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>MODEL BUILT SUCCESFULLY<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>" + Vectors.dense(thetas).toString() + "<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>" + model1.predict(dataPoint.first().features()) + "<<<<<<<<<<<<<< PREDICTION");
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>" + dataPoint.first().label() + "<<<<<<<<<<<<<<< EXPECTED");
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>" + model1.intercept() + "<<<<<<<<<<<<<<< INTERCEPT");
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>" + model1.weights() + "<<<<<<<<<<<<<<< weights");
        this.errorList.add(getSquaredError(dataPoint.first().label(), model1.predict(dataPoint.first().features())));
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>" + getSquaredError(dataPoint.first().label(), model1.predict(dataPoint.first().features())) + "<<<<<<<<<<<<<<<< Squared Error!!!!");
        System.out.println(">>>>>>>>>>>>>>>>>>>>>" + getCostFunction() + "<<<<<<<<<<<<<<<<COST FUNCTION!!!");
        System.out.println(">>>>>>>>>>>>>>>>>>>>>" + getMSE() + "<<<<<<<<<<<<<<<<COST FUNCTION!!!");
        numOfIterations++; //increment the value if iterations so far

        return model1;
    }

    public double[] getWeights(LinearRegressionModel model) {

        System.out.println("THIISSSSSSSSS IS THE WEIGHTS!!!!!!!!!" + model.weights().apply(0) + " " + model.weights().apply(1) + " " + model.weights().apply(2) + " ");

        return model.weights().toArray();
    }

    /*
    FOR One-Pass Online Learning
     */
    public static double getSquaredError(double label, double predicted) {

        return Math.pow(label - predicted, 2);

    }

    public double getMSE() {

        double totalError = getTotalError();
        double meanError = 0;
        if (!this.errorList.isEmpty()) {
            meanError = totalError / this.errorList.size();
        } else {
            //soemthing
        }
        return meanError;
    }

    /*
    checks if the error is decreasing. if not returns false
     */
    public boolean errorIsDecreasing(double error) {

        if (!this.errorList.isEmpty() && this.errorList.get(this.errorList.size() - 1) >= error) {
            return true;

        } else {
            return false;
        }

    }

    public double getCostFunction() {

        double totalError = getTotalError();
        double costFunction = totalError / 2;
        return costFunction;
    }

    public double getTotalError() {

        double totalError = 0;

        for (Iterator<Double> it = this.errorList.iterator(); it.hasNext();) {
            totalError = totalError + it.next();
        }
        return totalError;
    }
    
    
   


}
