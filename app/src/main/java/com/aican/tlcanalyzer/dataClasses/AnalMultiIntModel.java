package com.aican.tlcanalyzer.dataClasses;

import java.util.ArrayList;

public class AnalMultiIntModel {

    String imageName;
    ArrayList<ContourData> dataArrayList;

    public AnalMultiIntModel(String imageName, ArrayList<ContourData> dataArrayList) {
        this.imageName = imageName;
        this.dataArrayList = dataArrayList;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public ArrayList<ContourData> getDataArrayList() {
        return dataArrayList;
    }

    public void setDataArrayList(ArrayList<ContourData> dataArrayList) {
        this.dataArrayList = dataArrayList;
    }
}
