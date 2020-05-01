package edu.jhuapl.sbmt.stateHistory.ui.popup;

import java.awt.Color;
import java.util.List;

import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.ui.color.ColorProvider;
import edu.jhuapl.sbmt.stateHistory.ui.color.ConstColorProvider;

/**
 * Object that defines the action: "Fixed Color" (based on code from lopeznr1)
 *
 * @author steelrj1
 */
/**
 * @author steelrj1
 *
 * @param <G1>
 */
class FixedStateHistoryColorAction<G1> extends StateHistoryPopAction<G1>
{
	// Ref vars
//	/**
//	 *
//	 */
//	private final StateHistoryCollection refManager;
	/**
	 *
	 */
	private final ColorProvider refCP;

	/**
	 * Standard Constructor
	 */
	/**
	 * @param aManager
	 * @param aColor
	 */
	public FixedStateHistoryColorAction(StateHistoryCollection aManager, Color aColor)
	{
//		refManager = aManager;
		refCP = new ConstColorProvider(aColor);
	}

	/**
	 * Returns the color associated with this Action
	 */
	/**
	 * @return
	 */
	public Color getColor()
	{
		return refCP.getBaseColor();
	}

	/**
	 *
	 */
	@Override
	public void executeAction(List<G1> aItemL)
	{
//		refManager.installCustomColorProviders(aItemL, refCP, refCP);
	}

}