package com.aican.tlcanalyzer.dataClasses;

import java.util.ArrayList;

public class ContourSet {
    ArrayList<XY> xyArrayList;

    public ContourSet(ArrayList<XY> xyArrayList) {
        this.xyArrayList = xyArrayList;
    }

    public ArrayList<XY> getXyArrayList() {
        return xyArrayList;
    }

    public void setXyArrayList(ArrayList<XY> xyArrayList) {
        this.xyArrayList = xyArrayList;
    }
}
