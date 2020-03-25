package edu.jhuapl.sbmt.stateHistory.ui.popup;

import java.awt.Component;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.dialog.DirectoryChooser;
import edu.jhuapl.saavtk.gui.util.MessageUtil;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryModelIOHelper;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;

/**
 * Object that defines the action: "Save Tracks". (based on code from lopeznr1)
 *
 * @author steelrj1
 */
/**
 * @author steelrj1
 *
 */
class SaveFileAction extends StateHistoryPopAction<StateHistory>
{
	// Ref vars
	/**
	 *
	 */
	private final StateHistoryCollection refManager;
	/**
	 *
	 */
	private final Component refParent;

	/**
	 * Standard Constructor
	 */
	/**
	 * @param aManager
	 * @param aParent
	 */
	public SaveFileAction(StateHistoryCollection aManager, Component aParent)
	{
		refManager = aManager;
		refParent = aParent;
	}

	/**
	 *
	 */
	@Override
	public void executeAction(List<StateHistory> aItemL)
	{
		Component rootComp = JOptionPane.getFrameForComponent(refParent);
		Set<StateHistory> workS = refManager.getSelectedItems();

		// Prompt the user for the save folder
		String title = "Specify the folder to save " + workS.size() + " state history files";
		File targPath = DirectoryChooser.showOpenDialog(rootComp, title);
		if (targPath == null)
			return;

		// Save all of the selected items into the target folder
		StateHistory history = null;
		int passCnt = 0;
		try
		{
			for (StateHistory stateHistory : workS)
			{
				history = stateHistory;
				StateHistoryModelIOHelper.saveIntervalToFile(refManager.getSmallBodyModel().getConfig().getShapeModelName(), stateHistory, new File(targPath, stateHistory.getTrajectoryName()).getAbsolutePath());
				passCnt++;
			}
		}
		catch (Exception aExp)
		{
			String errMsg = "Failed to save " + (workS.size() - passCnt) + "files. Failed on state history file: ";
			errMsg += history.getTrajectoryName();
			JOptionPane.showMessageDialog(rootComp, errMsg, "Error Saving State History Files", JOptionPane.ERROR_MESSAGE);
			aExp.printStackTrace();
		}
	}

	/**
	 *
	 */
	@Override
	public void setChosenItems(Collection<StateHistory> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		// Update our associated MenuItem
		String displayStr = MessageUtil.toPluralForm("Save File", aItemC);
		aAssocMI.setText(displayStr);
	}

}
