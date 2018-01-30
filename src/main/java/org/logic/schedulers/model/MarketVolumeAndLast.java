package org.logic.schedulers.model;

public class MarketVolumeAndLast {

    private double volume;
    private double last;

    public double getVolume() {
        return volume;
    }

    public double getLast() {
        return last;
    }

    public MarketVolumeAndLast(double volume, double last) {
        this.volume = volume;
        this.last = last;
    }
}
