/**
 *
 */
package edu.jhuapl.sbmt.stateHistory.model.stateHistory.standard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.core.util.TimeUtil;
import edu.jhuapl.sbmt.pointing.IPointingProvider;
import edu.jhuapl.sbmt.pointing.State;
import edu.jhuapl.sbmt.pointing.StateHistoryUtil;
import edu.jhuapl.sbmt.pointing.pregen.PregenPointingProvider;
import edu.jhuapl.sbmt.pointing.scState.CsvState;
import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
import edu.jhuapl.sbmt.stateHistory.config.StateHistoryConfig;
import edu.jhuapl.sbmt.stateHistory.model.StateHistorySourceType;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryIntervalGenerator;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInputException;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInvalidTimeException;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryMetadata;

/**
 * Class that generates state history from a pre-generated state history file that lives on the server
 * @author steelrj1
 *
 */
public class PregenStateHistoryIntervalGenerator implements IStateHistoryIntervalGenerator
{
	StateHistoryConfig config;

	String sourceFile;

	private IPointingProvider pointingProvider;
	/**
	 *
	 */
	public PregenStateHistoryIntervalGenerator(StateHistoryConfig config)
	{
		this.config = config;
		this.sourceFile = config.timeHistoryFile;
	}

	@Override
	public void setSourceFile(String sourceFile, SpiceInfo spice)
	{
		setSourceFile(sourceFile);
	}

	public StateHistory createNewTimeInterval(StateHistory history, Function<Double, Void> progressFunction) throws StateHistoryInputException, StateHistoryInvalidTimeException
	{
		IStateHistoryMetadata metadata = history.getMetadata();
		String startString = edu.jhuapl.sbmt.core.util.TimeUtil.et2str(metadata.getStartTime());
		String endString = edu.jhuapl.sbmt.core.util.TimeUtil.et2str(metadata.getEndTime());
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
		DateTime start = formatter.parseDateTime(startString.substring(0, 23));
		DateTime end = formatter.parseDateTime(endString.substring(0, 23));
		if (sourceFile.endsWith("csvstate"))
			pointingProvider = PregenPointingProvider.builder(sourceFile, metadata.getStartTime(), metadata.getEndTime()).build();
		else
			pointingProvider = PregenPointingProvider.builder(sourceFile, start, end).build();


		return createNewTimeInterval(history, metadata.getKey(), start, end,
										metadata.getTimeWindow()/(24.0 * 60.0 * 60.0 * 1000.0), metadata.getStateHistoryName(), progressFunction);
	}

	public StateHistory createNewTimeInterval(StateHistoryKey key, DateTime startTime, DateTime endTime, double duration,
			String name, Function<Double, Void> progressFunction) throws StateHistoryInputException, StateHistoryInvalidTimeException
	{
		return createNewTimeInterval(null, key, startTime, endTime, duration, name, progressFunction);
	}


	/***
	 * Creates a new time history for the body with the given time range.
	 *
	 * @param key					StateHistoryKey representing this interval
	 * @param length			 	length of interval
	 * @param name					name of interval
	 * @param progressionFunction	Closure-like Function that takes in progress and allows you to update completion status
	 * @return 						StateHistory object if successful; null otherwise
	 */
	public StateHistory createNewTimeInterval(StateHistory tempHistory, StateHistoryKey key, DateTime startTime, DateTime endTime, double duration,
			String name, Function<Double, Void> progressFunction) throws StateHistoryInputException, StateHistoryInvalidTimeException
	{
		StandardStateHistory history = (StandardStateHistory)tempHistory;
		File path = null;
		final int lineLength = 121;
		// gets the history file from the server
		path = FileCache.getFileFromServer(config.timeHistoryFile);
		// removes the time zone from the time
		String startString = startTime.toString().substring(0, 23);
		String endString = endTime.toString().substring(0, 23);

		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
		DateTime start = formatter.parseDateTime(startString.substring(0, 23));
		DateTime end = formatter.parseDateTime(endString.substring(0, 23));
		if (sourceFile.endsWith("csvstate"))
			pointingProvider = PregenPointingProvider.builder(sourceFile, TimeUtil.str2et(startString),  TimeUtil.str2et(endString)).build();
		else
			pointingProvider = PregenPointingProvider.builder(sourceFile, start, end).build();

		// searches the file for the specified times
		String queryStart = StateHistoryUtil.readString(lineLength, path);
		String queryEnd = StateHistoryUtil.readString(
				(int) StateHistoryUtil.getBinaryFileLength(path, lineLength) * lineLength - lineLength, path);

		// error checking
		if (startTime.compareTo(endTime) > 0)
		{
			throw new StateHistoryInputException("The entered times are not in the correct order.");
		}
		if (startString.compareTo(queryStart) < 0 || endString.compareTo(queryEnd) > 0)
		{
			throw new StateHistoryInputException("One or more of the query times are out of range of the available data.");
		}

		// get start and stop positions in file
		int positionStart = StateHistoryUtil.binarySearch(1, (int) StateHistoryUtil.getBinaryFileLength(path, lineLength), startString, false, lineLength, path);
		int positionEnd = StateHistoryUtil.binarySearch(1, (int) StateHistoryUtil.getBinaryFileLength(path, lineLength), endString, true, lineLength, path);
		// check length of time
		if (StateHistoryUtil.readString(positionStart, path)
				.compareTo(StateHistoryUtil.readString(positionEnd, path)) == 0)
		{
			throw new StateHistoryInputException("The queried time interval is too small.");
		}

		// sets the default name to "startTime_endTime"
		if (name == null || name.equals(""))
		{
//			name = StateHistoryUtil.readString(positionStart, path) + "_"
//					+ StateHistoryUtil.readString(positionEnd, path);
			name = "Segment_" + key.getValue();
		}

		// creates the trajectory
		StateHistoryMetadata metadata = new StateHistoryMetadata(key, TimeUtil.str2et(startString),
				TimeUtil.str2et(startString), TimeUtil.str2et(endString),
				name, "", StateHistorySourceType.PREGEN);
		if (tempHistory == null) history = new StandardStateHistory(metadata, sourceFile);
//		Trajectory trajectory = new StandardTrajectory(history);
//		IStateHistoryLocationProvider locationProvider = history.getLocationProvider();
//		IStateHistoryTrajectoryMetadata trajectoryMetadata = history.getTrajectoryMetadata();
//
//		trajectory.setPointingProvider(pointingProvider);
//		trajectory.setStartTime(TimeUtil.str2et(startString));
//		trajectory.setStopTime(TimeUtil.str2et(endString));
//		trajectory.setNumPoints(1000);
//
		List<State> segments = new ArrayList<State>();
		// reads the binary file and writes the data to a CSV file
		for (int i = positionStart; i <= positionEnd; i += lineLength)
		{
			//populate the position array at this index
			int[] position = new int[12];
			for (int j = 0; j < position.length; j++)
			{
				position[j] = i + 25 + (j * 8);
			}

			//populate a  state object, and use it to populate the history and trajectory
			State state = new CsvState(i, path, position);

			// add to history
			segments.add(state);

			double completion = 100 * ((double) (i - positionStart)) / (double) (positionEnd - positionStart);
			if (progressFunction != null) progressFunction.apply(completion);
		}
		history.buildHistory(segments, pointingProvider, TimeUtil.str2et(startString), TimeUtil.str2et(endString));
//		metadata.setCurrentTime(metadata.getStartTime());
//		trajectoryMetadata.setTrajectory(trajectory);
//		metadata.setType(StateHistorySourceType.PREGEN);
//		locationProvider.setSourceFile(sourceFile);
//		locationProvider.setPointingProvider(pointingProvider);
		return history;
	}

	/**
	 * @param sourceFile the sourceFile to set
	 */
	public void setSourceFile(String sourceFile)
	{
		this.sourceFile = sourceFile;
	}
}
