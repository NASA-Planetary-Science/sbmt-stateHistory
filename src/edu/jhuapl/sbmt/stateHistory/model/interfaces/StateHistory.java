package edu.jhuapl.sbmt.stateHistory.model.interfaces;

import java.util.Map.Entry;
import java.util.Set;

import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInvalidTimeException;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;

/**
 * Interface for classes that store state history segment information
 * @author steelrj1
 *
 */
public interface StateHistory
{
    /**
     *
     */
	public static final double epsilon = 0.0000001;

	/**
	 * Returns the time window for this state history
     * @return
     */
    public Double getTimeWindow();

    /**
     * Returns the current time fraction
     * @return
     */
    public Double getTimeFraction();

    /**
     * Sets the current time fraction
     * @param state
     * @param timeFraction
     */
    public void setTimeFraction(Double timeFraction) throws StateHistoryInvalidTimeException;

    /**
     * Returns the current time in the time window for this state history
     * @return
     */
    public Double getCurrentTime();

    /**
     * Sets the current time in the time window for this state history
     * @param time
     */
    public void setCurrentTime(Double time) throws StateHistoryInvalidTimeException;

    /**
     * Returns the minimum available time in this state history
     * @return
     */
    public Double getMinTime();

    /**
     * Returns the maximum available time in this state history
     * @return
     */
    public Double getMaxTime();

    /**
     * @param flybyState
     */
    public void addState(State flybyState);

    /**
     * @param time
     * @param flybyState
     */
    public void addStateAtTime(Double time, State flybyState);

    /**
     * @param time
     * @return
     */
    public Entry<Double, State> getStateBeforeOrAtTime(Double time);

    /**
     * @param time
     * @return
     */
    public Entry<Double, State> getStateAtOrAfter(Double time);

    /**
     * @param time
     * @return
     */
    public State getStateAtTime(Double time);

    /**
     * @return
     */
    public State getCurrentState();

    /**
     * @return
     */
    public Set<Double> getAllTimes();

    /**
     * @return
     */
    public StateHistoryKey getKey();


    //Heavenly body position getters

    /**
     * Returns the spacecraft position in the body fixed frame
     * @return
     */
    public double[] getSpacecraftPosition();

    /**
     * Returns the sun position in the body fixed frame
     * @return
     */
    public double[] getSunPosition();

    /**
     * Returns the earth position in the body fixed frame
     * @return
     */
    public double[] getEarthPosition();

    /**
     * @return
     */
    public String getStateHistoryName();

    /**
     * @param name
     */
    public void setStateHistoryName(String name);

    /**
     * Returns the trajectory object for this state history
     * @return
     */
    public Trajectory getTrajectory();

    /**
     * Sets the trajectory for this StateHistory to <pre>traj</pre>
     * @param traj
     */
    public void setTrajectory(Trajectory traj);

	/**
	 * Sets the color of the trajectory associated with this state history
	 * @param color
	 */
	public void setTrajectoryColor(Double[] color);
}
