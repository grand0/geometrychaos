package ru.kpfu.itis.gr201.ponomarev.bheditor.util;

import java.util.Comparator;

public class DoubleComparator implements Comparator<Double> {

    private static final double EPS = 1e-10;

    @Override
    public int compare(Double o1, Double o2) {
        if (Math.abs(o1 - o2) <= EPS) {
            return 0;
        } else if (o1 < o2) {
            return -1;
        } else {
            return 1;
        }
    }
}
