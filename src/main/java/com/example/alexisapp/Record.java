package com.example.alexisapp;

import java.util.Iterator;
import java.util.List;

public class Record {
    boolean EyesClosed;
    List<Double> Vector;

    double EuclideanDistance(List<Double> b) {
        Iterator<Double> iterator = b.iterator();
        double result = 0;
        for (Double d : Vector) {
            double i = iterator.next();
            result += (d - i) * (d - i);
        }
        return result;
    }
}
