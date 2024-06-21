package edu.jhuapl.sbmt.stateHistory.model.planning;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JFrame;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.model.SaavtkItemManager;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.core.rendering.DataActor;
import glum.item.ItemEventType;
import vtk.vtkProp;


public abstract class BasePlannedDataScheduleCollection<C extends BasePlannedDataCollection> extends SaavtkItemManager<C> implements PropertyChangeListener
{
	protected List<C> plannedData = new ArrayList<C>();
	protected List<vtkProp> footprintActors = new ArrayList<vtkProp>();

	protected List<DataActor> plannedDataActors = new ArrayList<DataActor>();
	protected Hashtable<C, JFrame> fullScheduleFrames = new Hashtable<C, JFrame>();


	@Override
	public List<vtkProp> getProps()
	{
		return footprintActors;
	}

	public void notify(Object obj, ItemEventType type)
	{
		notifyListeners(obj, type);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (!Properties.MODEL_CHANGED.equals(evt.getPropertyName())) return;
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void addCollection(C data)
	{
		plannedData.add(data);
		setAllItems(plannedData);
	}

	public void removeCollection(C data)
	{
		plannedData.remove(data);
		setAllItems(plannedData);
	}

	/**
	 *
	 */
	@Override
	public ImmutableList<C> getAllItems()
	{
		return ImmutableList.copyOf(plannedData);
	}

	/**
	 *
	 */
	@Override
	public int getNumItems()
	{
		return plannedData.size();
	}


	public abstract void showDetailedScheduleFor(C collection);
}
