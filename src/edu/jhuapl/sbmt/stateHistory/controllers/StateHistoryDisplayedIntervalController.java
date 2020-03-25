package edu.jhuapl.sbmt.stateHistory.controllers;

import edu.jhuapl.sbmt.gui.lidar.PercentIntervalChanger;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.ui.version2.StateHistoryDisplayedIntervalPanel;
import edu.jhuapl.sbmt.util.TimeUtil;

import glum.item.ItemEventType;

/**
 * Controller that displays the "Available Files" panel in the state history tab
 * @author steelrj1
 *
 */
public class StateHistoryDisplayedIntervalController
{
	/**
	 * JPanel for displaying the displayed interval controls
	 */
	private StateHistoryDisplayedIntervalPanel view;

	/**
	 * The current set of StateHistory intervals
	 */
	private StateHistoryCollection intervalSet;

	/**
	 * Constructor.
	 *
	 * Adds listeners to the <pre>intervalSet</pre> object as well as the
	 * <pre>StateHistoryDisplayedIntervalPanel</pre>'s time interval changer object
	 *
	 * @param interval			The current set of StateHistory intervals
	 */
	public StateHistoryDisplayedIntervalController(StateHistoryCollection intervalSet)
	{
		this.intervalSet = intervalSet;
		view = new StateHistoryDisplayedIntervalPanel();

		//If the selected item is changed, update the current run, reset the time range, and the time interval label
		intervalSet.addListener((aSource, aEventType) -> {

			if (aEventType != ItemEventType.ItemsSelected) return;
			if (intervalSet.getSelectedItems().size() > 0)
			{
				intervalSet.setCurrentRun(intervalSet.getSelectedItems().asList().get(0));
				updateDisplayedTimeRange(0.0, 1.0);
			}
			view.getTimeIntervalChanger().setEnabled(intervalSet.getSelectedItems().size() > 0);
		});

		//The action listener for the time interval changer; takes values from the changer
		//and passes them onto the current run so the display can properly update
		view.getTimeIntervalChanger().addActionListener(e -> {
			PercentIntervalChanger changer = view.getTimeIntervalChanger();
			double minValue = changer.getLowValue();
			double maxValue = changer.getHighValue();
			double timeFraction = intervalSet.getCurrentRun().getTimeFraction();
			intervalSet.setTrajectoryMinMax(intervalSet.getCurrentRun(), minValue, maxValue);
			intervalSet.getCurrentRun().setMinDisplayFraction(minValue);
			intervalSet.getCurrentRun().setMaxDisplayFraction(maxValue);
			intervalSet.getCurrentRun().setTimeFraction(timeFraction);

			updateDisplayedTimeRange(minValue, maxValue);
		});
	}

	/**
	 * Updates the displayed start/stop time label
	 * @param minValue the minimum fraction (between 0 and 1) of the entire interval being displayed
	 * @param maxValue the maximum fraction (between 0 and 1) of the entire interval being displayed
	 */
	private void updateDisplayedTimeRange(double minValue, double maxValue)
	{
		String minTime = TimeUtil.et2str(intervalSet.getCurrentRun().getMinTime());
		String maxTime = TimeUtil.et2str(intervalSet.getCurrentRun().getMaxTime());

		view.getDisplayedStartTimeLabel().setText(minTime.substring(0, minTime.length()-3));
		view.getDisplayedStopTimeLabel().setText(maxTime.substring(0, maxTime.length()-3));
	}

	/**
	 * Returns the view associated with this controller
	 * @return the view associated with this controller
	 */
	public StateHistoryDisplayedIntervalPanel getView()
	{
		return view;
	}
}
