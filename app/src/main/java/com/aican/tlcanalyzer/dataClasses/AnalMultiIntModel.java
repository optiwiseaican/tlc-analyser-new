package com.aican.tlcanalyzer.dataClasses;

import java.util.ArrayList;

public class AnalMultiIntModel {

    boolean isSelected;
    String imageName;
    String mainImageName;
    String contourImageName;
    ArrayList<ContourData> dataArrayList;



    public AnalMultiIntModel(boolean isSelected, String imageName, String mainImageName, String contourImageName, ArrayList<ContourData> dataArrayList) {
        this.isSelected = isSelected;
        this.imageName = imageName;
        this.mainImageName = mainImageName;
        this.contourImageName = contourImageName;
        this.dataArrayList = dataArrayList;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
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

    public String getContourImageName() {
        return contourImageName;
    }

    public void setContourImageName(String contourImageName) {
        this.contourImageName = contourImageName;
    }

    public String getMainImageName() {
        return mainImageName;
    }

    public void setMainImageName(String mainImageName) {
        this.mainImageName = mainImageName;
    }
}
