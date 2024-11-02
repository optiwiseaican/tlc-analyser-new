package com.aican.tlcanalyzer.dataClasses;

public class ContourTableData {


    String id;
    String rf;
    String rfTop;
    String rfBottom;
    String cv;
    String area;
    String volume;
    String areaPercentage;

    boolean isSelected = true;

    @Override
    public String toString() {
        return "ID: " + id +
                ", RF: " + rf +
                ", RF Top: " + rfTop +
                ", RF Bottom: " + rfBottom +
                ", CV: " + cv +
                ", Area: " + area +
                ", Volume: " + volume +
                ", Area Percentage: " + areaPercentage +
                ", Is Selected: " + isSelected;
    }

    public ContourTableData(String id, String rf, String rfTop, String rfBottom, String cv, String area, String volume, String areaPercentage) {
        this.id = id;
        this.rf = rf;
        this.rfTop = rfTop;
        this.rfBottom = rfBottom;
        this.cv = cv;
        this.area = area;
        this.volume = volume;
        this.areaPercentage = areaPercentage;
    }

    public ContourTableData(String id, String rf, String rfTop, String rfBottom, String cv, String area, String volume, String areaPercentage, boolean isSelected) {
        this.id = id;
        this.rf = rf;
        this.rfTop = rfTop;
        this.rfBottom = rfBottom;
        this.cv = cv;
        this.area = area;
        this.volume = volume;
        this.areaPercentage = areaPercentage;
        this.isSelected = isSelected;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRf() {
        return rf;
    }

    public void setRf(String rf) {
        this.rf = rf;
    }

    public String getRfTop() {
        return rfTop;
    }

    public void setRfTop(String rfTop) {
        this.rfTop = rfTop;
    }

    public String getRfBottom() {
        return rfBottom;
    }

    public void setRfBottom(String rfBottom) {
        this.rfBottom = rfBottom;
    }

    public String getCv() {
        return cv;
    }

    public void setCv(String cv) {
        this.cv = cv;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getAreaPercentage() {
        return areaPercentage;
    }

    public void setAreaPercentage(String areaPercentage) {
        this.areaPercentage = areaPercentage;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
