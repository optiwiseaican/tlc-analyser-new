package com.aican.tlcanalyzer.dataClasses;

public class ContourData {

    String id;
    String rf;
    String rfTop;
    String rfBottom;
    String cv;
    String area;
    String volume;
    String chemicalName;
    boolean isSelected = true;
    int buttonColor;

    @Override
    public String toString() {
        String selectedStatus = isSelected ? "Selected" : "Not Selected";
        return "ID: " + id + ", RF: " + rf + ", RF Top: " + rfTop + ", RF Bottom: " + rfBottom +
                ", CV: " + cv + ", Area: " + area + ", Volume: " + volume + ", " + selectedStatus;
    }

    public ContourData(String id, String rf, String rfTop, String rfBottom, String cv, String area, String volume, boolean isSelected, int buttonColor) {
        this.id = id;
        this.rf = rf;
        this.rfTop = rfTop;
        this.rfBottom = rfBottom;
        this.cv = cv;
        this.area = area;
        this.volume = volume;
        this.isSelected = isSelected;
        this.buttonColor = buttonColor;
    }

    // 0,1,2,3,4,5,7
//   String CREATE_TABLE_NEW_USER = "CREATE TABLE " + tableName + "(id TEXT, rf TEXT, rfTop TEXT, rfBottom TEXT, cv TEXT, " +
//                "area TEXT, " +
//                "areaPercent TEXT, volume TEXT)";
    public ContourData(String id, String rf, String rfTop, String rfBottom, String cv, String area, String volume, String chemicalName) {
        this.id = id;
        this.rf = rf;
        this.rfTop = rfTop;
        this.rfBottom = rfBottom;
        this.cv = cv;
        this.area = area;
        this.volume = volume;
        this.chemicalName = chemicalName;
    }

    public ContourData(String id, String rf, String rfTop, String rfBottom, String cv, String area, String volume, boolean isSelected) {
        this.id = id;
        this.rf = rf;
        this.rfTop = rfTop;
        this.rfBottom = rfBottom;
        this.cv = cv;
        this.area = area;
        this.volume = volume;
        this.isSelected = isSelected;
    }


    public String getChemicalName() {
        return chemicalName;
    }

    public void setChemicalName(String chemicalName) {
        this.chemicalName = chemicalName;
    }

    public int getButtonColor() {
        return buttonColor;
    }

    public void setButtonColor(int buttonColor) {
        this.buttonColor = buttonColor;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
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

}
