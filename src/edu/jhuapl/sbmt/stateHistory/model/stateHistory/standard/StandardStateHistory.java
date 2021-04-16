package edu.jhuapl.sbmt.stateHistory.model.stateHistory.standard;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.saavtk.util.ColorUtil;
import edu.jhuapl.sbmt.stateHistory.model.StateHistorySourceType;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryLocationProvider;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryTrajectoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.State;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryIOException;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInvalidTimeException;
import edu.jhuapl.sbmt.stateHistory.model.scState.CsvState;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.AbstractStateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryTrajectoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.trajectory.StandardTrajectory;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;

/**
 * Standard class for holding history information.  A timeToStateMap keeps a correlation between ephemeris time
 * and an object that obeys the <pre>State</pre> interface.
 *
 * This class contains a reference to an object that implements the <pre>Trajectory</pre> interface, which helps describe
 * how this state history can be rendered.
 * @author steelrj1
 *
 */
public class StandardStateHistory extends AbstractStateHistory
{

    /**
     *
     */
    private Color color;

    //Metadata Information
    private static final Key<StandardStateHistory> STANDARD_STATE_HISTORY_KEY = Key.of("StandardStateHistory");
	private static final Key<StateHistoryKey> STATEHISTORY_KEY_KEY = Key.of("key");
	private static final Key<Double> CURRENT_TIME_KEY = Key.of("currentTime");
	private static final Key<Double> START_TIME_KEY = Key.of("startTime");
	private static final Key<Double> END_TIME_KEY = Key.of("stopTime");
	private static final Key<String> STATE_HISTORY_NAME_KEY = Key.of("name");
	private static final Key<String> STATE_HISTORY_DESCRIPTION_KEY = Key.of("description");
	private static final Key<Double[]> COLOR_KEY = Key.of("color");
	private static final Key<String> TYPE_KEY = Key.of("type");
	private static final Key<String> SOURCE_FILE = Key.of("sourceFile");

    public static void initializeSerializationProxy()
	{
    	InstanceGetter.defaultInstanceGetter().register(STANDARD_STATE_HISTORY_KEY, (source) -> {

    		StateHistoryKey key = source.get(STATEHISTORY_KEY_KEY);
    		Double currentTime = source.get(CURRENT_TIME_KEY);
    		Double startTime = source.get(START_TIME_KEY);
    		Double endTime = source.get(END_TIME_KEY);
    		String name = source.get(STATE_HISTORY_NAME_KEY);
    		StateHistorySourceType type = StateHistorySourceType.valueOf(source.get(TYPE_KEY));
    		String sourceFile = source.get(SOURCE_FILE);
    		String description = "";
    		try
    		{
    			description = source.get(STATE_HISTORY_DESCRIPTION_KEY);
    		}
    		catch (IllegalArgumentException iae) {}
    		if ((name == null) || (name.equals("")))
				name = "Segment_" + key.getValue();
    		Double[] colorAsDouble = source.get(COLOR_KEY);
    		StateHistoryMetadata metadata = new StateHistoryMetadata(key, currentTime, startTime, endTime, name, description, type);
    		StandardStateHistory stateHistory = new StandardStateHistory(metadata, sourceFile);
    		stateHistory.getTrajectoryMetadata().setTrajectory(new StandardTrajectory(stateHistory));
    		stateHistory.getTrajectoryMetadata().setTrajectoryColor(ColorUtil.getColorFromRGBA(colorAsDouble));
    		return stateHistory;

    	}, StandardStateHistory.class, stateHistory -> {

    		IStateHistoryMetadata metadata = stateHistory.getMetadata();
    		IStateHistoryLocationProvider locationProvider = stateHistory.getLocationProvider();
    		IStateHistoryTrajectoryMetadata trajectoryMetadata = stateHistory.getTrajectoryMetadata();
    		float[] colorComponents = ColorUtil.getRGBColorComponents(trajectoryMetadata.getTrajectory().getColor());
    		SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
    		result.put(STATEHISTORY_KEY_KEY, metadata.getKey());
    		result.put(CURRENT_TIME_KEY, metadata.getCurrentTime());
    		result.put(START_TIME_KEY, metadata.getStartTime());
    		result.put(END_TIME_KEY, metadata.getEndTime());
    		result.put(STATE_HISTORY_NAME_KEY, metadata.getStateHistoryName());
    		result.put(STATE_HISTORY_DESCRIPTION_KEY, metadata.getStateHistoryDescription());
    		result.put(TYPE_KEY, metadata.getType().toString());
    		result.put(SOURCE_FILE, locationProvider.getSourceFile());
    		result.put(COLOR_KEY, new Double[] { (double)colorComponents[0], (double)colorComponents[1], (double)colorComponents[2], (double)trajectoryMetadata.getTrajectory().getColor().getAlpha()/255.0});
    		return result;
    	});
	}

    public StandardStateHistory(StateHistoryMetadata metadata)
    {
    	this.metadata = new StateHistoryMetadata(metadata) {
			@Override
			public void setCurrentTime(Double time) throws StateHistoryInvalidTimeException
			{
				if( time < getStartTime() || time > getEndTime())
		        {
		        	throw new StateHistoryInvalidTimeException("Entered time (" + time + ") is outside the range of the selected interval (" + getStartTime() + "-" + getEndTime() + ").");
		        }
				currentTime = time;
			}

			/**
		     *
		     */
			@Override
		    public Double getStartTime()
		    {
		    	if (startTime != null) return startTime;
		        return ((StandardStateHistoryLocationProvider)locationProvider).getTimeToStateMap().firstKey();
		    }

			@Override
		    public Double getEndTime()
		    {
		    	if (endTime != null) return endTime;
		        return ((StandardStateHistoryLocationProvider)locationProvider).getTimeToStateMap().lastKey();
		    }
		};
    	this.locationProvider = new StandardStateHistoryLocationProvider(this);
    	this.trajectoryMetadata = new StateHistoryTrajectoryMetadata();

    }

    public StandardStateHistory(StateHistoryMetadata metadata, String sourceFile)
    {
    	this(metadata);
    	locationProvider.setSourceFile(sourceFile);
    }

//    /**
//     * @param key
//     */
//    public StandardStateHistory(StateHistoryKey key)
//    {
//    	this.key = key;
//    }
//
//    /**
//     * @param key
//     * @param currentTime
//     * @param startTime
//     * @param endTime
//     * @param name
//     * @param color
//     */
//    public StandardStateHistory(StateHistoryKey key, Double currentTime, Double startTime, Double endTime, String name, String description, Color color, StateHistorySourceType type, String sourceFile)
//    {
//    	this.key = key;
//    	this.currentTime = currentTime;
//    	this.startTime = startTime;
//    	this.endTime = endTime;
//    	this.color = color;
//    	this.stateHistoryName = name;
//    	this.stateHistoryDescription = description;
//    	this.type = type;
//    	this.sourceFile = sourceFile;
//    }

//    /**
//     *
//     */
//    public void setCurrentTime(Double dt) throws StateHistoryInvalidTimeException
//    {
//        if( dt < getStartTime() || dt > getEndTime())
//        {
//        	throw new StateHistoryInvalidTimeException("Entered time (" + dt + ") is outside the range of the selected interval (" + getStartTime() + "-" + getEndTime() + ").");
////            JOptionPane.showMessageDialog(null, "Entered time is outside the range of the selected interval.", "Error",
////                    JOptionPane.ERROR_MESSAGE);
////            return;
//        }
////        Interval interval1 = new Interval(getMinTime().longValue(), dt.longValue());
////        Interval interval2 = new Interval(getMinTime().longValue(), getMaxTime().longValue());
//
////        org.joda.time.Duration duration1 = interval1.toDuration();
////        org.joda.time.Duration duration2 = interval2.toDuration();
//
////        BigDecimal num1 = new BigDecimal(duration1.getMillis());
////        BigDecimal num2 = new BigDecimal(duration2.getMillis());
////        BigDecimal tf = num1.divide(num2,50,RoundingMode.UP);
//        this.currentTime = dt;
////        this.time = Double.parseDouble(tf.toString());
//    }
//
//    /**
//     *
//     */
//    public Double getStartTime()
//    {
//    	if (startTime != null) return startTime;
//        return timeToStateMap.firstKey();
//    }
//
//
//    public Double getEndTime()
//    {
//    	if (endTime != null) return endTime;
//        return timeToStateMap.lastKey();
//    }


	public void saveStateToFile(String shapeModelName, String fileName) throws StateHistoryIOException
	{
		String fileNameWithExtension = fileName + ".csvstate";
		// writes the header for the new history
		try
        {
            FileWriter writer = new FileWriter(fileNameWithExtension);
//		            writer.append(config.getShapeModelName());
            writer.append(',');
            writer.append('\n');

            // Create header of name, description, color
            writer.append(getMetadata().getStateHistoryName() + ',');
            writer.append(trajectoryMetadata.getTrajectory().getTrajectoryDescription() + ',');
            for (double colorElement : trajectoryMetadata.getTrajectory().getColor().getColorComponents(null)) {
                writer.append(Double.toString(colorElement));
                writer.append(',');
            }
            writer.append(Double.toString(trajectoryMetadata.getTrajectory().getThickness()));
            writer.append('\n');

            // header of column names for each entry
            writer.append("#UTC");
            writer.append(',');
            writer.append(" Sun x");
            writer.append(',');
            writer.append(" Sun y");
            writer.append(',');
            writer.append(" Sun z");
            writer.append(',');
            writer.append(" Earth x");
            writer.append(',');
            writer.append(" Earth y");
            writer.append(',');
            writer.append(" Earth z");
            writer.append(',');
            writer.append(" SC x");
            writer.append(',');
            writer.append(" SC y");
            writer.append(',');
            writer.append(" SC z");
            writer.append(',');
            writer.append(" SCV x");
            writer.append(',');
            writer.append(" SCV y");
            writer.append(',');
            writer.append(" SCV z");
            writer.append('\n');
            writer.flush();
            writer.close();
        }
        catch (IOException e)
        {
        	e.printStackTrace();
            throw new StateHistoryIOException("A problem occurred when saving the state history; please see the console for a stack trace", e);
        }

        // get each flyby state in currentFlybyStateHistory, and write to CSV
        Set<Double> keySet = locationProvider.getAllTimes();
        for (Double key : keySet) {
            State history = locationProvider.getStateAtTime(key);
            history.writeToCSV(fileNameWithExtension);
        }
	}

	/**
	 * Loads state history interval from a file.  Format is the full expanded "Mark 1"
	 * format, used before SPICE integration.
	 * @param runFile
	 * @param shapeModelName
	 * @param key
	 * @return
	 * @throws StateHistoryIOException
	 */
	public StateHistory loadStateHistoryFromFile(File file, String shapeModelName, StateHistoryKey key) throws StateHistoryIOException
    {
		String extension = FilenameUtils.getExtension(file.getAbsolutePath());
		if (!extension.equals("csvstate") || !extension.equals("csv")) throw new StateHistoryIOException("Invalid file format");
		ArrayList<String[]> timeArray = new ArrayList<>(3);
        Integer firstIndex = null;
        String runDirName = file.getAbsolutePath();
        StandardTrajectory trajectory;

        try
        {
            String runName = file.getName();
            if (!runName.endsWith(".csv")) throw new StateHistoryIOException("File does not have a csv extension; please choose another file");

            BufferedReader in = new BufferedReader(new FileReader(file));
            in.close();


            StateHistory history = null;
            in = new BufferedReader(new FileReader(runDirName));

            if (firstIndex == null)
                firstIndex = 0;

         // create a new history instance and add it to the Map
            StateHistoryMetadata metadata = new StateHistoryMetadata(key);
            history = new StandardStateHistory(metadata, file.getAbsolutePath());

            trajectory = new StandardTrajectory(history);
            // fill in the Trajectory parameters
            trajectory.setId(0);

            // discard first line of body name
            in.readLine();

            // get name, desc, color form second line
            String info = in.readLine();
            String[] data = info.split(",");
//            trajectory.setName(data[0]);
            trajectory.setTrajectoryDescription(data[1]);

            // discard third line of headers
            in.readLine();

            String line;
            String[] timeSet = new String[2];
            timeArray.add(timeSet);
            while ((line = in.readLine()) != null)
            {
                // parse line of file
                State flybyState = new CsvState(line);

                // add to history
                history.getLocationProvider().addState(flybyState);

                double[] spacecraftPosition = flybyState.getSpacecraftPosition();

                //TODO go back and look at this once pointing provider implementation in place - needed?
//                trajectory.getX().add(spacecraftPosition[0]);
//                trajectory.getY().add(spacecraftPosition[1]);
//                trajectory.getZ().add(spacecraftPosition[2]);

                if(com.mysql.jdbc.StringUtils.isNullOrEmpty(timeArray.get(0)[0])){
                    timeArray.get(0)[0] = flybyState.getUtc();
                }
                timeArray.get(0)[1] = flybyState.getUtc();
            }
            in.close();

            history.getTrajectoryMetadata().setTrajectory(trajectory);

            history.getMetadata().setCurrentTime(history.getMetadata().getStartTime());
            trajectory.setStartTime(history.getMetadata().getStartTime());
            trajectory.setStopTime(history.getMetadata().getEndTime());
            trajectory.setNumPoints(Math.abs((int)(history.getMetadata().getEndTime() - history.getMetadata().getStartTime())/60));

            return history;
        }
        catch (Exception e1)
        {
        	e1.printStackTrace();
            throw new StateHistoryIOException("There was a problem reading the state history file.  See the console for details, and please make sure it is the right format", e1);
        }
    }

	public void validate()
	{
		isValid = locationProvider.validate();
	}
}