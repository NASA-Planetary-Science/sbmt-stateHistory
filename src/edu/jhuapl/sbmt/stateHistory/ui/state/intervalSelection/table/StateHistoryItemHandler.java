package edu.jhuapl.sbmt.stateHistory.ui.state.intervalSelection.table;

import java.util.ArrayList;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.jhuapl.saavtk.color.provider.ColorProvider;
import edu.jhuapl.sbmt.core.util.TimeUtil;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryLocationProvider;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;

/**
 * @author steelrj1
 *
 */
public class StateHistoryItemHandler extends BasicItemHandler<StateHistory, StateHistoryColumnLookup>
{
	/**
	 *
	 */
	private final StateHistoryCollection stateHistoryCollection;
	private StateHistoryRendererManager rendererManager;

	/**
	 * @param aManager
	 * @param aComposer
	 */
	public StateHistoryItemHandler(StateHistoryRendererManager rendererManager, QueryComposer<StateHistoryColumnLookup> aComposer)
	{
		super(aComposer);
		this.rendererManager = rendererManager;
		stateHistoryCollection = rendererManager.getHistoryCollection();
	}

	/**
	 *
	 */
	@Override
	public Object getColumnValue(StateHistory stateHistory, StateHistoryColumnLookup aEnum)
	{
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
		String timeString;
		ColorProvider colorProvider = rendererManager.getColorProviderForStateHistory(stateHistory);
		IStateHistoryMetadata metadata = stateHistory.getMetadata();
		IStateHistoryLocationProvider locationProvider = stateHistory.getLocationProvider();
		switch (aEnum)
		{
			case Map:
				return metadata.isMapped();
			case Show:
				return metadata.isVisible();
			case Color:
				return colorProvider;
			case Name:
				if (metadata.getStateHistoryName().equals(""))
				{
					metadata.setStateHistoryName("Segment_" + metadata.getKey().getValue());
					return metadata.getStateHistoryName();
				}
				return metadata.getStateHistoryName();
			case Description:
				return metadata.getStateHistoryDescription();
			case Source:
				return metadata.getType() + " (" + locationProvider.getSourceFile() + ")";
			case StartTime:
				fmt.withZone(DateTimeZone.UTC);
				timeString = TimeUtil.et2str(metadata.getStartTime());
				return timeString.substring(0, 23);
			case EndTime:
				fmt.withZone(DateTimeZone.UTC);
				timeString = TimeUtil.et2str(metadata.getEndTime());
				return timeString.substring(0, 23);
			default:
				break;
		}

		throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

	/**
	 *
	 */
	@Override
	public void setColumnValue(StateHistory history, StateHistoryColumnLookup aEnum, Object aValue)
	{
		IStateHistoryMetadata metadata = history.getMetadata();
		if (aEnum == StateHistoryColumnLookup.Map)
		{
			if (!metadata.isMapped())
			{
				Thread thread = new Thread(new Runnable()
				{

					@Override
					public void run()
					{
						rendererManager.addRun(history);
					}
				});
				thread.start();

			}
			else
			{
				rendererManager.removeRun(history);
			}
		}
		else if (aEnum == StateHistoryColumnLookup.Show)
		{
			if (metadata.isMapped()) rendererManager.setVisibility(history, (boolean) aValue);

		}
		else if (aEnum == StateHistoryColumnLookup.Name)
		{
			metadata.setStateHistoryName((String)aValue);
			stateHistoryCollection.fireHistorySegmentUpdatedListeners(history);
		}
		else if (aEnum == StateHistoryColumnLookup.Description)
		{
			metadata.setStateHistoryDescription((String)aValue);
			stateHistoryCollection.fireHistorySegmentUpdatedListeners(history);
		}
		else if (aEnum == StateHistoryColumnLookup.Color)
		{
			ArrayList<StateHistory> histories = new ArrayList<StateHistory>();
			histories.add(history);
			rendererManager.installCustomColorProvider(histories, (ColorProvider) aValue);

		}
		else
			throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}
}