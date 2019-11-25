package edu.jhuapl.sbmt.stateHistory.model.stateHistory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.saavtk.model.AbstractModel;
import edu.jhuapl.saavtk.util.Properties;
//import edu.jhuapl.sbmt.client.ModelFactory;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel.StateHistoryKey;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.HasTime;

public class StateHistoryCollection extends AbstractModel implements PropertyChangeListener, HasTime
{
    private SmallBodyModel smallBodyModel;
    private ArrayList<StateHistoryKey> keys = new ArrayList<StateHistoryKey>();
    private List<StateHistoryModel> simRuns = new ArrayList<StateHistoryModel>();
    private StateHistoryModel currentRun = null;

    public StateHistoryCollection(SmallBodyModel smallBodyModel)
    {
        this.smallBodyModel = smallBodyModel;
    }

    private boolean containsKey(StateHistoryKey key)
    {
        for (StateHistoryModel run : simRuns)
        {
            if (run.getKey().equals(key))
                return true;
        }

        return false;
    }

    private StateHistoryModel getRunFromKey(StateHistoryKey key)
    {
        for (StateHistoryModel run : simRuns)
        {
            if (run.getKey().equals(key))
                return run;
        }

        return null;
    }

    public StateHistoryKey getKeyFromRow(int row)
    {
        if (keys.size() > row) {
            return keys.get(row);
        }
        return null;
    }

    public StateHistoryModel getRunFromRow(int row)
    {
        return getRunFromKey(getKeyFromRow(row));
    }

    public StateHistoryModel getCurrentRun()
    {
        return currentRun;
    }

    public void setCurrentRun(StateHistoryKey key)
    {
        StateHistoryModel run = getRunFromKey(key);
        if (run != null && run != currentRun)
        {
            currentRun = run;
        }

    }

    public void addRun(StateHistoryModel run)//  throws FitsException, IOException
    {
        StateHistoryKey key = run.getKey();
        if (containsKey(key))
        {
            this.currentRun = this.getRun(key);
            return;
        }

        // set the current run
        simRuns.add(run);
        keys.add(key);
//        setCurrentRun(key);  TODO only do this when selected? this way loading a new interval doesn't override existing one
        run.addPropertyChangeListener(this);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void removeRun(StateHistoryKey key)
    {
        if (!containsKey(key))
            return;

        StateHistoryModel run = getRunFromKey(key);
        simRuns.remove(run);
        keys.remove(key);

        // change the current run to the first on the list
//        this.currentRun = simRuns.get(0);
        this.currentRun = null;

        run.removePropertyChangeListener(this);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        this.pcs.firePropertyChange(Properties.MODEL_REMOVED, null, run);
    }

    public void removeRuns(StateHistoryKey[] keys)
    {
        for (StateHistoryKey key : keys) {
            removeRun(key);
        }
    }

    /**
     * Remove all images of the specified source
     * @param source
     */
//    public void removeRuns(StateHistorySource source)
//    {
//        for (StateHistoryModel run : simRuns)
//            if (run.getKey().source == source)
//                removeRun(run.getKey());
//    }

    public void setShowTrajectories(boolean show)
    {
        for (StateHistoryModel run : simRuns)
            run.setShowSpacecraft(show);
    }

    public ArrayList<vtkProp> getProps()
    {
        if (currentRun != null)
            return currentRun.getProps();
        else
            return new ArrayList<vtkProp>();
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
    {
        if (currentRun != null)
            return currentRun.getClickStatusBarText(prop, cellId, pickPosition);
        else
            return "No simulation run selected";
    }

//    public String getRunName(vtkActor actor)
//    {
//        if (currentRun != null)
//            return currentRun.getKey().name;
//        else
//            return "No simulation run selected";
//    }

    public StateHistoryModel getRun(vtkActor actor)
    {
        return currentRun;
    }

    public StateHistoryModel getRun(StateHistoryKey key)
    {
        return getRunFromKey(key);
    }

    public boolean containsRun(StateHistoryKey key)
    {
        return containsKey(key);
    }

    public void setTimeFraction(Double timeFraction)
    {
        if (currentRun != null)
           currentRun.setTimeFraction(timeFraction);
    }

    public Double getTimeFraction()
    {
        if (currentRun!= null)
            return currentRun.getTimeFraction();
        else
            return null;
    }

    public void setOffset(double offset)
    {
        if (currentRun != null)
            currentRun.setOffset(offset);
    }

    public double getOffset()
    {
        if (currentRun!= null)
            return currentRun.getOffset();
        else
            return 0.0;
    }

    public Double getPeriod()
    {
        if (currentRun != null)
            return ((HasTime)currentRun).getPeriod();
        else
            return 0.0;
    }

    public int size()
    {
        return simRuns.size();
    }

    public List<StateHistoryKey> getKeys()
    {
        return keys;
    }

}
