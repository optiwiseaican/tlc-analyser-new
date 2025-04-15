package com.aican.tlcanalyzer.dataClasses;

import java.util.ArrayList;

public class SplitContourData {

    String id;
    String name;
    boolean isSelected;
    String contourImageName;
    String mainImageName;
    String masterImage;
    String hr;
    String rmSpot;
    String finalSpot;
    String intensityPlotTableID;
    ArrayList<Double> volumeDATAList;
    public ArrayList<RFvsArea> rFvsAreaArrayList;
    ArrayList<ContourSet> contourSetArrayList;
    ArrayList<ContourData> contourData;

    ArrayList<LabelData> labelDataArrayList;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SplitContourData{name='").append(name).append("', isSelected=").append(isSelected)
                .append(", contourImageName='").append(contourImageName).append("', mainImageName='").append(mainImageName).append("'}");

        sb.append("\nVolume Data: ").append(volumeDATAList);
        sb.append("\nRF vs Area List: ").append(rFvsAreaArrayList);
        sb.append("\nContour Set List: ").append(contourSetArrayList);
        sb.append("\nContour Data List: ").append(contourData);
        sb.append("\nintensityPlotTableID: ").append(intensityPlotTableID);

        return sb.toString();
    }

    public SplitContourData(String id, String name, boolean isSelected, String contourImageName, String mainImageName,
                            String hr, String rmSpot, String finalSpot, ArrayList<Double> volumeDATAList, ArrayList<RFvsArea> rFvsAreaArrayList,
                            ArrayList<ContourSet> contourSetArrayList, ArrayList<ContourData> contourData,
                            ArrayList<LabelData> labelDataArrayList, String intensityPlotTableID) {
        this.id = id;
        this.name = name;
        this.isSelected = isSelected;
        this.contourImageName = contourImageName;
        this.mainImageName = mainImageName;
        this.hr = hr;
        this.rmSpot = rmSpot;
        this.finalSpot = finalSpot;
        this.volumeDATAList = volumeDATAList;
        this.rFvsAreaArrayList = rFvsAreaArrayList;
        this.contourSetArrayList = contourSetArrayList;
        this.contourData = contourData;
        this.labelDataArrayList = labelDataArrayList;
        this.intensityPlotTableID = intensityPlotTableID;
    }

//    public SplitContourData(String name, boolean isSelected, String contourImageName, String mainImageName, ArrayList<Double> volumeDATAList, ArrayList<RFvsArea> rFvsAreaArrayList, ArrayList<ContourSet> contourSetArrayList, ArrayList<ContourData> contourData) {
//        this.name = name;
//        this.isSelected = isSelected;
//        this.contourImageName = contourImageName;
//        this.mainImageName = mainImageName;
//        this.volumeDATAList = volumeDATAList;
//        this.rFvsAreaArrayList = rFvsAreaArrayList;
//        this.contourSetArrayList = contourSetArrayList;
//        this.contourData = contourData;
//    }

//    public SplitContourData(String contourImageName, String mainImageName, ArrayList<Double> volumeDATA, ArrayList<RFvsArea> rFvsAreaArrayList, ArrayList<ContourSet> contourSetArrayList, ArrayList<ContourData> contourData) {
//        this.contourImageName = contourImageName;
//        this.mainImageName = mainImageName;
//        this.volumeDATA = volumeDATA;
//        this.rFvsAreaArrayList = rFvsAreaArrayList;
//        this.contourSetArrayList = contourSetArrayList;
//        this.contourData = contourData;
//    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<LabelData> getLabelDataArrayList() {
        return labelDataArrayList;
    }

    public void setLabelDataArrayList(ArrayList<LabelData> labelDataArrayList) {
        this.labelDataArrayList = labelDataArrayList;
    }

    public String getHr() {
        return hr;
    }

    public void setHr(String hr) {
        this.hr = hr;
    }

    public String getRmSpot() {
        return rmSpot;
    }

    public void setRmSpot(String rmSpot) {
        this.rmSpot = rmSpot;
    }

    public String getFinalSpot() {
        return finalSpot;
    }

    public void setFinalSpot(String finalSpot) {
        this.finalSpot = finalSpot;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntensityPlotTableID() {
        return intensityPlotTableID;
    }

    public void setIntensityPlotTableID(String intensityPlotTableID) {
        this.intensityPlotTableID = intensityPlotTableID;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
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

    public String getMasterImage() {
        return masterImage;
    }

    public void setMasterImage(String masterImage) {
        this.masterImage = masterImage;
    }

    public ArrayList<Double> getVolumeDATAList() {
        return volumeDATAList;
    }

    public void setVolumeDATAList(ArrayList<Double> volumeDATAList) {
        this.volumeDATAList = volumeDATAList;
    }

    public ArrayList<RFvsArea> getrFvsAreaArrayList() {
        return rFvsAreaArrayList;
    }

    public void setrFvsAreaArrayList(ArrayList<RFvsArea> rFvsAreaArrayList) {
        this.rFvsAreaArrayList = rFvsAreaArrayList;
    }

    public ArrayList<ContourSet> getContourSetArrayList() {
        return contourSetArrayList;
    }

    public void setContourSetArrayList(ArrayList<ContourSet> contourSetArrayList) {
        this.contourSetArrayList = contourSetArrayList;
    }

    public ArrayList<ContourData> getContourData() {
        return contourData;
    }

    public void setContourData(ArrayList<ContourData> contourData) {
        this.contourData = contourData;
    }
}
