package edu.jhuapl.sbmt.stateHistory.model.interfaces;

import java.util.Map.Entry;
import java.util.Set;

public interface StateHistory extends HasTime
{
    public static final double epsilon = 0.0000001;

    public Double getTime();

    public void setTime(Double time);

    public Double getTimeFraction();

    public void setTimeFraction(Double time);

    public Double getMinTime();
    public Double getMaxTime();

    public void put(State flybyState);

    public void put(Double time, State flybyState);

    public Entry<Double, State> getFloorEntry(Double time);

    public Entry<Double, State> getCeilingEntry(Double time);

    public State getValue(Double time);

    public State getCurrentValue();

    public double[] getSpacecraftPosition();

    public double[] getSunPosition();

    public double[] getEarthPosition();

    public Set<Double> getAllKeys();
}
