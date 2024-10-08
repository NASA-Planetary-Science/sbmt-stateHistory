package edu.jhuapl.sbmt.stateHistory.model;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.swing.SwingUtilities;

import org.joda.time.DateTime;

import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
import edu.jhuapl.sbmt.stateHistory.config.StateHistoryConfig;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryIntervalGenerator;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistoryCollectionChangedListener;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistoryModelChangedListener;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryIOException;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInputException;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInvalidTimeException;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.spice.SpiceStateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.spice.SpiceStateHistoryLocationProvider;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import edu.jhuapl.ses.jsqrl.impl.FixedMetadata;
import edu.jhuapl.ses.jsqrl.impl.gson.Serializers;
import picante.mechanics.providers.lockable.LockableEphemerisLinkEvaluationException;

/**
 * @author steelrj1
 *
 */
public class StateHistoryModel
{
	/**
	 *
	 */
	Map<StateHistorySourceType, IStateHistoryIntervalGenerator> intervalGenerators = new HashMap<StateHistorySourceType, IStateHistoryIntervalGenerator>();

	/**
	 *
	 */
	List<StateHistoryModelChangedListener> listeners = new ArrayList<StateHistoryModelChangedListener>();

	private String customDataFolder;

	/**
	 *
	 */
	private StateHistoryCollection collection;

	/**
	 *
	 */
	public static int INITIAL_SLIDER_VALUE = 0;

	/**
	 *
	 */
	public static int FINAL_SLIDER_VALUE = 900;

	/**
	 *
	 */
	private StateHistoryConfig config;

	/**
	 *
	 */
	private IStateHistoryIntervalGenerator activeIntervalGenerator;

	private boolean initialized;

	private StateHistoryRendererManager rendererManager;

	private StateHistory newTimeInterval = null;

	/**
	 * @param start
	 *            Start time of the inclusive range for this body model's state
	 *            history
	 * @param end
	 *            End time of the inclusive range for this body model's state
	 *            history
	 * @param smallBodyModel
	 * @param renderer
	 * @param modelManager
	 * @throws StateHistoryInputException
	 * @throws IOException
	 */
	public StateHistoryModel(StateHistoryConfig config, String customDataFolder, StateHistoryRendererManager rendererManager) throws IOException, StateHistoryInputException, StateHistoryInvalidTimeException
	{
		this.config = config;
		this.customDataFolder = customDataFolder;
		this.collection = rendererManager.getHistoryCollection();
		this.rendererManager = rendererManager;
		this.collection.addStateHistoryCollectionChangedListener(new StateHistoryCollectionChangedListener()
		{

			@Override
			public void historySegmentUpdated(StateHistory history)
			{
				try
				{
					updateConfigFile();
					rendererManager.updateRun(history);
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * @param listener
	 */
	public void addStateHistoryModelChangedListener(StateHistoryModelChangedListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * @param historySegment
	 */
	private void fireHistorySegmentCreatedListener(StateHistory historySegment)
	{
		listeners.forEach(listener -> listener.historySegmentCreated(historySegment));
	}

	/**
	 * @param historySegment
	 */
	private void fireHistorySegmentRemovedListener(StateHistory historySegment)
	{
		listeners.forEach(listener -> listener.historySegmentRemoved(historySegment));
	}

	public void removeRun(StateHistory historySegment) throws IOException
	{
		rendererManager.removeRun(historySegment);
		collection.removeRunFromList(historySegment);
		rendererManager.setSelectedItems(new ArrayList<StateHistory>());
		if (collection.getSimRuns().isEmpty()) { collection.setCurrentRun(null); }
		rendererManager.setAllItems(collection.getSimRuns());
		fireHistorySegmentRemovedListener(historySegment);
		updateConfigFile();
	}

	/***
	 * Interval Creation
	 ***/

	/***
	 * Creates a new time history for the body with the given time range.
	 *
	 * @param key
	 *            StateHistoryKey representing this interval
	 * @param length
	 *            length of interval
	 * @param name
	 *            name of interval
	 * @param progressionFunction
	 *            Closure-like Function that takes in progress and allows you to
	 *            update completion status
	 * @return StateHistory object if successful; null otherwise
	 */
	public void createNewTimeInterval(StateHistoryKey key, DateTime startTime, DateTime endTime, double duration,
			String name, Function<Double, Void> progressFunction) throws StateHistoryInputException, StateHistoryInvalidTimeException, IOException,
																		  InvocationTargetException, InterruptedException,
																		  LockableEphemerisLinkEvaluationException
	{

		try
		{
			newTimeInterval = activeIntervalGenerator.createNewTimeInterval(key, startTime, endTime, duration, name,
					progressFunction);
		}
		catch (LockableEphemerisLinkEvaluationException lelee)
		{
			throw lelee;
		}
		finally
		{
			if (newTimeInterval != null)
			{
				SwingUtilities.invokeAndWait(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							collection.addRunToList(newTimeInterval);
							rendererManager.setAllItems(collection.getSimRuns());
							fireHistorySegmentCreatedListener(newTimeInterval);
						}
						catch  (LockableEphemerisLinkEvaluationException lelee)
						{
							try
							{
								removeRun(newTimeInterval);
							}
							catch (IOException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							throw lelee;
						}
					}
				});
				updateConfigFile();
			}
		}
	}

//	/**
//	 * @param history
//	 * @param file
//	 * @throws StateHistoryIOException
//	 */
//	public void saveHistoryToFile(StateHistory history, File file) throws StateHistoryIOException
//	{
//		StateHistoryModelIOHelper.saveIntervalToFile(viewConfig.getShapeModelName(), history, file.getAbsolutePath());
//	}

//	/**
//	 * @param runFile
//	 * @param bodyModel
//	 * @throws StateHistoryIOException
//	 */
//	public void loadIntervalFromFile(File runFile, SmallBodyModel bodyModel) throws StateHistoryIOException, IOException
//	{
//		StateHistory newRow = StateHistoryModelIOHelper.loadStateHistoryFromFile(runFile,
//				viewConfig.getShapeModelName(), new StateHistoryKey(collection));
//		collection.addRunToList(newRow);
//		rendererManager.setAllItems(collection.getSimRuns());
//		fireHistorySegmentCreatedListener(newRow);
//		updateConfigFile();
//	}

	public void addInterval(StateHistory history) throws StateHistoryIOException
	{
		collection.addRunToList(history);
		rendererManager.setAllItems(collection.getSimRuns());
		fireHistorySegmentCreatedListener(history);
		try {
			updateConfigFile();
		}
		catch (IOException e)
		{
			throw new StateHistoryIOException(e);
		}
	}

	public List<StateHistory> loadRunList() throws IOException, StateHistoryInputException, StateHistoryInvalidTimeException, StateHistoryIOException
	{
		List<StateHistory> invalidHistories = new ArrayList<StateHistory>();
		if (initialized) return invalidHistories;
		if (!(new File(getConfigFilename()).exists())) return invalidHistories;

		FixedMetadata metadata = Serializers.deserialize(new File(getConfigFilename()), "StateHistory");
		collection.retrieve(metadata);
		for (StateHistory history : collection.getSimRuns())
		{
			history.validate();
			if (history.isValid() == false)
			{
//				boolean invalidKernelAlreadyExists = invalidHistories.stream().filter(his -> his.getSourceFile().equals(history.getSourceFile())).count() > 0;
//				if (!invalidKernelAlreadyExists)
					invalidHistories.add(history);
			}
		}
		updateConfigFile();
		return invalidHistories;
	}

	/**
	 * @throws IOException
	 */
	public void initializeRunList() throws IOException, StateHistoryInputException, StateHistoryInvalidTimeException, StateHistoryIOException//, SpiceKernelNotFoundException
	{
		for (StateHistory history : rendererManager.getAllItems())
		{
			setIntervalGenerator(history.getMetadata().getType());
			SpiceInfo spice = null;
			if (history instanceof SpiceStateHistory) spice = ((SpiceStateHistoryLocationProvider)history.getLocationProvider()).getSpiceInfo();
			activeIntervalGenerator.setSourceFile(history.getLocationProvider().getSourceFile(), spice);
			activeIntervalGenerator.createNewTimeInterval(history, null);
		}
		rendererManager.setAllItems(rendererManager.getHistoryCollection().getSimRuns());
		initialized = true;
	}

	/**
	 * @return
	 */
	private String getConfigFilename()
	{
		File stateHistoryConfigFile = new File(customDataFolder, "stateHistory.txt");
		return stateHistoryConfigFile.getAbsolutePath();
	}

	/**
	*
	*/
	private void updateConfigFile() throws IOException
	{
		Serializers.serialize("StateHistory", collection, new File(getConfigFilename()));
	}

	public void setIntervalGenerator(StateHistorySourceType generatorType)
	{
		this.activeIntervalGenerator = intervalGenerators.get(generatorType);
	}

	public void registerIntervalGenerator(StateHistorySourceType generatorType, IStateHistoryIntervalGenerator generator)
	{
		intervalGenerators.put(generatorType, generator);
	}

	/**
	 * @return the runs
	 */
	public StateHistoryCollection getHistoryCollection()
	{
		return collection;
	}

	/**
	 * @return the activeIntervalGenerator
	 */
	public IStateHistoryIntervalGenerator getActiveIntervalGenerator()
	{
		return activeIntervalGenerator;
	}

//	/**
//	 * @param activeIntervalGenerator the activeIntervalGenerator to set
//	 */
//	public void setActiveIntervalGenerator(IStateHistoryIntervalGenerator activeIntervalGenerator)
//	{
//		this.activeIntervalGenerator = activeIntervalGenerator;
//	}

	/**
	 * @return the customDataFolder
	 */
	public String getCustomDataFolder()
	{
		return customDataFolder;
	}

	/**
	 * @return the viewConfig
	 */
	public StateHistoryConfig getViewConfig()
	{
		return config;
	}
}