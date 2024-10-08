package edu.jhuapl.sbmt.stateHistory.controllers.imagers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import com.google.common.collect.ImmutableSet;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.util.ProgressStatusListener;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.controllers.IPlannedDataController;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.io.PlannedImageIOHelper;
import edu.jhuapl.sbmt.stateHistory.model.planning.imagers.PlannedImageCollection;
import edu.jhuapl.sbmt.stateHistory.model.planning.imagers.PlannedImageScheduleCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import edu.jhuapl.sbmt.stateHistory.ui.imagers.schedule.PlannedImageScheduleView;

/**
 * Class that controls the UI for the planned image schedules
 * @author steelrj1
 *
 */
public class PlannedImageScheduleTableController implements IPlannedDataController<PlannedImageScheduleView>
{
	PlannedImageScheduleView view;
	PlannedImageScheduleCollection collection;
	StateHistoryRendererManager rendererManager;
	IStateHistoryMetadata historyMetadata = null;
	ArrayList<PropertyChangeListener> pcls = new ArrayList<PropertyChangeListener>();

	public PlannedImageScheduleTableController(StateHistoryRendererManager rendererManager, SmallBodyModel smallBodyModel)
	{
		this.rendererManager = rendererManager;
		collection = new PlannedImageScheduleCollection();

		this.view = new PlannedImageScheduleView(collection);

		//creates a new image collection for each schedule and loads it into the tool
		view.getTable().getLoadPlannedImageButton().addActionListener(e -> {

			File file = CustomFileChooser.showOpenDialog(view, "Select File");
        	if (file == null) return;

        	Runnable runner = new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						PlannedImageCollection imageCollection = new PlannedImageCollection(file.getAbsolutePath(), smallBodyModel);
			        	if (rendererManager.getSelectedItems().size() > 0) historyMetadata = rendererManager.getSelectedItems().asList().get(0).getMetadata();
			        		imageCollection.setStateHistoryMetadata(historyMetadata);
			        	imageCollection.updateStateHistorySource(rendererManager.getHistoryCollection().getCurrentRun());
			        	pcls.forEach(listener -> imageCollection.addPropertyChangeListener(listener));
			        	imageCollection.addListener((aSource, aEventType) -> { refreshView(); });
			        	collection.addCollection(imageCollection);

						PlannedImageIOHelper.loadPlannedImagesFromFileWithName(file.getAbsolutePath(), historyMetadata, imageCollection, new ProgressStatusListener()
						{

							@Override
							public void setProgressStatus(String status, int progress)
							{
								if (progress == 0) view.getTable().getProcessingLabel().setText(status);
								else view.getTable().getProcessingLabel().setText(status);
								view.getTable().repaint();
								view.getTable().validate();
							}
						},
						() -> {
							SwingUtilities.invokeLater(new Runnable()
							{
								@Override
								public void run()
								{
									view.getTable().getProcessingLabel().setText("Ready.");
									updateSyncState();
								}
							});
						});
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			Thread thread = new Thread(runner);
			thread.start();
		});


		view.getTable().getSyncWithTimelineButton().addActionListener(e -> {
			updateSyncState();
		});

		view.getTable().getShowPlannedImageButton().addActionListener(e -> {

			collection.getSelectedItems().forEach( coll -> coll.setShowing(true));
			refreshView();
		});

		view.getTable().getHidePlannedImageButton().addActionListener(e -> {
			collection.getSelectedItems().forEach( coll -> coll.setShowing(false));
			refreshView();
		});

		view.getTable().getDeleteScheduleButton().addActionListener(e -> {
			collection.getSelectedItems().forEach( coll -> coll.setShowing(false));
			collection.getSelectedItems().forEach( coll -> collection.removeCollection(coll));
			refreshView();
		});

		collection.addListener((aSource, aEventType) ->
		{
			refreshView();
		});
	}

	private void updateSyncState()
	{
		rendererManager.setSyncImages(view.getTable().getSyncWithTimelineButton().isSelected());
		collection.getAllItems().forEach( coll -> coll.updateFootprints() );
		rendererManager.propertyChange(new PropertyChangeEvent(this, Properties.MODEL_CHANGED, null, null));
		refreshView();
	}

	/**
	 * @param listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		pcls.add(listener);
	}

	private void refreshView()
	{
		updateButtonState();
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				view.repaint();
	            view.validate();
			}
		});
	}

	private void updateButtonState()
	{
		ImmutableSet<PlannedImageCollection> selectedItems = collection.getSelectedItems();
		boolean allMapped = true;
		for (PlannedImageCollection imageCollection : selectedItems)
		{
			if (imageCollection.isShowing() == false) allMapped = false;
		}
		view.getTable().getHidePlannedImageButton().setEnabled((selectedItems.size() > 0) && allMapped);
		view.getTable().getShowPlannedImageButton().setEnabled((selectedItems.size() > 0) && !allMapped);
		view.getTable().getDeleteScheduleButton().setEnabled(selectedItems.size() > 0);
	}

	@Override
	public PlannedImageScheduleView getView()
	{
		return view;
	}

	/**
	 * @return the collection
	 */
	public PlannedImageScheduleCollection getCollection()
	{
		return collection;
	}

}
