package no.tuva.knitproj.domain;

public class Gauge {
    private final int stitchesPer10Cm;
    private final double needleSize;
    private final int stitchesPer10CmHeight;

    public Gauge(int stitchesPer10Cm, int stitchesPer10CmHeight, double needleSize) {
        this.stitchesPer10Cm = stitchesPer10Cm;
        this.needleSize = needleSize;
        this.stitchesPer10CmHeight = stitchesPer10CmHeight;
    }

    public int getStitchesPer10Cm() {
        return stitchesPer10Cm;
    }

    public double getNeedleSize() {
        return needleSize;
    }

    public int getStitchesPer10CmHeight(){
        return stitchesPer10CmHeight;
    }
}
