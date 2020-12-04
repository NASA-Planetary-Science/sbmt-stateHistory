package edu.jhuapl.sbmt.stateHistory.model.stateHistory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistoryCollectionChangedListener;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.SettableMetadata;
import glum.gui.panel.itemList.ItemProcessor;
import glum.item.ItemEventListener;

/**
 * Item manager that governs the available state histories for display in the
 * table, and once selected, in the renderer
 *
 * @author steelrj1
 *
 */
public class StateHistoryCollection /*extends SaavtkItemManager<StateHistory> implements PropertyChangeListener,*/ implements MetadataManager
{
	/**
	 *
	 */
	private ArrayList<StateHistoryCollectionChangedListener> changeListeners = new ArrayList<StateHistoryCollectionChangedListener>();

	/**
	 *
	 */
	private ArrayList<StateHistoryKey> keys = new ArrayList<StateHistoryKey>();

	/**
	 *
	 */
	private List<StateHistory> simRuns = new ArrayList<StateHistory>();

	/**
	 *
	 */
	private StateHistory currentRun = null;

	/**
	 *
	 */
	private String bodyName;

	private Vector<String> availableFOVs;

	final Key<List<StateHistory>> stateHistoryKey = Key.of("stateHistoryCollection");

	/**
	 * @param smallBodyModel
	 */
	public StateHistoryCollection(SmallBodyModel smallBodyModel)
	{
		this.bodyName = smallBodyModel.getConfig().getShapeModelName();
		availableFOVs = new Vector<String>();
	}

	public void addStateHistoryCollectionChangedListener(StateHistoryCollectionChangedListener listener)
	{
		changeListeners.add(listener);
	}

	public void fireHistorySegmentUpdatedListeners(StateHistory history)
	{
		for (StateHistoryCollectionChangedListener listener : changeListeners)
		{
			listener.historySegmentUpdated(history);
		}
	}

	/**
	 * @param run
	 */
	public void addRunToList(StateHistory run)
	{
		simRuns.add(run);
		keys.add(run.getKey());
	}

	/**
	 * @param run
	 */
	public void removeRunFromList(StateHistory run)
	{
		simRuns.remove(run);
		keys.remove(run.getKey());
	}

	/**
	 * @param run
	 * @return
	 */
	public void addRun(StateHistory run)
	{
		availableFOVs.clear();
		if (run.getPointingProvider() != null)
			Arrays.stream(run.getPointingProvider().getInstrumentNames()).forEach(inst -> availableFOVs.add(inst));
		this.currentRun = run;
	}


	/**
	 *
	 */
	public Double getPeriod()
	{
		if (currentRun != null)
			return currentRun.getTimeWindow();
		else
			return 0.0;
	}

	public ItemProcessor<String> getAllFOVProcessor()
	{
		return new ItemProcessor<String>()
		{

			@Override
			public void addListener(ItemEventListener aListener)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void delListener(ItemEventListener aListener)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public ImmutableList<String> getAllItems()
			{
				return ImmutableList.copyOf(StateHistoryCollection.this.availableFOVs);
			}

			@Override
			public int getNumItems()
			{
				return StateHistoryCollection.this.availableFOVs.size();
			}
		};
	}

	/**
	 * Stores the model to metadata
	 */
	@Override
	public Metadata store()
	{
		SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
    	result.put(stateHistoryKey, simRuns);
    	return result;
	}

	/**
	 * Fetches the model from metadata
	 */
	@Override
	public void retrieve(Metadata source)
	{
		simRuns = source.get(stateHistoryKey);
	}

	/**
	 * @return the keys
	 */
	public ArrayList<StateHistoryKey> getKeys()
	{
		return keys;
	}

	/**
	 * @return the currentRun
	 */
	public StateHistory getCurrentRun()
	{
		return currentRun;
	}

	/**
	 * @param currentRun the currentRun to set
	 */
	public void setCurrentRun(StateHistory currentRun)
	{
		this.currentRun = currentRun;
	}

	/**
	 * @return the bodyName
	 */
	public String getBodyName()
	{
		return bodyName;
	}

	/**
	 * @return the availableFOVs
	 */
	public Vector<String> getAvailableFOVs()
	{
		return availableFOVs;
	}

	/**
	 * @return the simRuns
	 */
	public List<StateHistory> getSimRuns()
	{
		return simRuns;
	}
}
