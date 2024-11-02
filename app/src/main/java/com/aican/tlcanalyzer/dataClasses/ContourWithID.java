package com.aican.tlcanalyzer.dataClasses;

import org.opencv.core.MatOfPoint;

public class ContourWithID {

    String id;
    MatOfPoint matOfPoint;

    public ContourWithID(String id, MatOfPoint matOfPoint) {
        this.id = id;
        this.matOfPoint = matOfPoint;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MatOfPoint getMatOfPoint() {
        return matOfPoint;
    }

    public void setMatOfPoint(MatOfPoint matOfPoint) {
        this.matOfPoint = matOfPoint;
    }
}
