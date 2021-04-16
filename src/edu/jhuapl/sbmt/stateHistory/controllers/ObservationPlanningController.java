package edu.jhuapl.sbmt.stateHistory.controllers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.plateColoring.ColoringDataManager;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.stateHistory.controllers.imagers.PlannedImageScheduleTableController;
import edu.jhuapl.sbmt.stateHistory.controllers.lidars.PlannedLidarScheduleTableController;
import edu.jhuapl.sbmt.stateHistory.controllers.spectrometers.PlannedSpectrumScheduleTableController;
import edu.jhuapl.sbmt.stateHistory.controllers.viewControls.ObservationPlanningViewControlsController;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInvalidTimeException;
import edu.jhuapl.sbmt.stateHistory.model.planning.PlannedDataActorRegister;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.model.time.BaseStateHistoryTimeModelChangedListener;
import edu.jhuapl.sbmt.stateHistory.model.time.StateHistoryTimeModel;
import edu.jhuapl.sbmt.stateHistory.rendering.PlannedDataProperties;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import edu.jhuapl.sbmt.stateHistory.ui.ObservationPlanningView;
import edu.jhuapl.sbmt.stateHistory.ui.state.version2.StateHistoryIntervalPlaybackPanel;

import glum.item.ItemEventType;

public class ObservationPlanningController implements PropertyChangeListener
{
	StateHistoryController stateHistoryController;
	ObservationPlanningView view = new ObservationPlanningView();
//	PlannedImageTableController imageTableController;
	PlannedImageScheduleTableController imageScheduleTableController;
	PlannedSpectrumScheduleTableController spectrumScheduleTableController;
	PlannedLidarScheduleTableController lidarScheduleTableController;
//	PlannedSpectrumTableController spectrumTableController;
//	PlannedLidarTableController lidarTableController;
	PlannedDataController plannedDataController;
	PlannedDataActorRegister registrar;
	ObservationPlanningViewControlsController viewControlsController;
	StateHistoryIntervalPlaybackPanel intervalPlaybackPanel;
//	PlannedImageCollection imageCollection;
//	PlannedSpectrumCollection spectrumCollection;
//	PlannedLidarTrackCollection lidarTrackCollection;
	StateHistoryTimeModel timeModel;
	StateHistoryRendererManager rendererManager;

    /**
     * Controller for the interval playback panel
     */
    private StateHistoryIntervalPlaybackController intervalPlaybackController;

	public ObservationPlanningController(final ModelManager modelManager, SmallBodyModel smallBodyModel, StateHistoryRendererManager rendererManager, SmallBodyViewConfig config, ColoringDataManager coloringDataManager)
	{
		timeModel = StateHistoryTimeModel.getInstance();
		StateHistoryCollection runs = rendererManager.getRuns();
		stateHistoryController = new StateHistoryController(modelManager, rendererManager, timeModel);
		viewControlsController = new ObservationPlanningViewControlsController(stateHistoryController.getHistoryModel(), modelManager, rendererManager, coloringDataManager);
		stateHistoryController.setViewControlsController(viewControlsController);
		this.intervalPlaybackController = new StateHistoryIntervalPlaybackController(rendererManager, timeModel);
		this.rendererManager = rendererManager;
		view.addTab("S/C Trajectory", stateHistoryController.getView());
		intervalPlaybackController.getView().setEnabled(false);
		registrar = new PlannedDataActorRegister();
		this.rendererManager = rendererManager;

		timeModel.addTimeModelChangeListener(new BaseStateHistoryTimeModelChangedListener() {

			@Override
			public void timeChanged(double et)
			{
				super.timeChanged(et);
//				if (imageCollection.getNumItems() > 0 && rendererManager.isSyncImages())
//					imageCollection.propertyChange(new PropertyChangeEvent(this, PlannedDataProperties.TIME_CHANGED, null, et));
				if (imageScheduleTableController.getCollection().getNumItems() > 0 && rendererManager.isSyncImages())
					imageScheduleTableController.getCollection().getAllItems().forEach( coll -> coll.propertyChange(new PropertyChangeEvent(this, PlannedDataProperties.TIME_CHANGED, null, et)));
//				if (spectrumCollection.getNumItems() > 0 && rendererManager.isSyncSpectra())
//					spectrumCollection.propertyChange(new PropertyChangeEvent(this, PlannedDataProperties.TIME_CHANGED, null, et));
				if (spectrumScheduleTableController.getCollection().getNumItems() > 0 && rendererManager.isSyncSpectra())
					spectrumScheduleTableController.getCollection().getAllItems().forEach( coll -> coll.propertyChange(new PropertyChangeEvent(this, PlannedDataProperties.TIME_CHANGED, null, et)));
				if (lidarScheduleTableController.getCollection().getNumItems() > 0 && rendererManager.isSyncLidar())
					lidarScheduleTableController.getCollection().getAllItems().forEach( coll -> coll.propertyChange(new PropertyChangeEvent(this, PlannedDataProperties.TIME_CHANGED, null, et)));
//				if (lidarTrackCollection.getNumItems() > 0 && rendererManager.isSyncLidar())
//					lidarTrackCollection.propertyChange(new PropertyChangeEvent(this, PlannedDataProperties.TIME_CHANGED, null, et));

                try
				{
                	if (runs.getCurrentRun() != null)
                		runs.getCurrentRun().getMetadata().setCurrentTime(et);
				}
				catch (StateHistoryInvalidTimeException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
		});

		Renderer renderer = rendererManager.getRenderer();
//		imageCollection = new PlannedImageCollection("", smallBodyModel);
//		imageCollection.addPropertyChangeListener(this);
//		imageTableController = new PlannedImageTableController(imageCollection);
		imageScheduleTableController = new PlannedImageScheduleTableController(rendererManager, smallBodyModel);
		imageScheduleTableController.addPropertyChangeListener(this);

//		if (config.imagingInstruments.length > 0) view.addTab("Images", imageTableController.getView());

//		spectrumCollection = new PlannedSpectrumCollection(smallBodyModel);
//		spectrumCollection.addPropertyChangeListener(this);
//		spectrumTableController = new PlannedSpectrumTableController(modelManager, rendererManager, spectrumCollection, config);
//		if (config.hasSpectralData) view.addTab("Spectra", spectrumTableController.getView());
		spectrumScheduleTableController = new PlannedSpectrumScheduleTableController(rendererManager, smallBodyModel);
		spectrumScheduleTableController.addPropertyChangeListener(this);

//		lidarTrackCollection = new PlannedLidarTrackCollection("", modelManager, smallBodyModel, renderer);
//		lidarTrackCollection.addPropertyChangeListener(this);
//		lidarTableController = new PlannedLidarTableController(modelManager, rendererManager, lidarTrackCollection, config);
//		if (config.hasLidarData) view.addTab("LIDAR", lidarTableController.getView());
		lidarScheduleTableController = new PlannedLidarScheduleTableController(modelManager, rendererManager, smallBodyModel);
		lidarScheduleTableController.addPropertyChangeListener(this);

		plannedDataController = new PlannedDataController();
		plannedDataController.addChildController(imageScheduleTableController);
		plannedDataController.addChildController(spectrumScheduleTableController);
		plannedDataController.addChildController(lidarScheduleTableController);
		view.addTab("Planned Data", plannedDataController.getView());

        rendererManager.addListener((aSource, aEventType) -> {
			if (aEventType != ItemEventType.ItemsSelected) return;
			if (rendererManager.getRuns().getCurrentRun() == null) return;
			intervalPlaybackController.getView().setEnabled(rendererManager.getSelectedItems().size() > 0);
			intervalPlaybackController.currentOffsetTime = 0.0;
			timeModel.setTime(runs.getCurrentRun().getMetadata().getStartTime());
			imageScheduleTableController.getCollection().getAllItems().forEach(coll -> coll.updateStateHistorySource(runs.getCurrentRun()));
			spectrumScheduleTableController.getCollection().getAllItems().forEach(coll -> coll.updateStateHistorySource(runs.getCurrentRun()));
			lidarScheduleTableController.getCollection().getAllItems().forEach(coll -> coll.updateStateHistorySource(runs.getCurrentRun()));
//			imageCollection.updateStateHistorySource(runs.getCurrentRun());
//			lidarTrackCollection.updateStateHistorySource(runs.getCurrentRun());
			ObservationPlanningController.this.updatePlannedScience();
		});

        intervalPlaybackPanel = intervalPlaybackController.getView();

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getPropertyName().equals("PLANNED_IMAGES_CHANGED") || evt.getPropertyName().equals("PLANNED_SPECTRA_CHANGED") || evt.getPropertyName().equals("PLANNED_LIDAR_CHANGED"))
		{
			updatePlannedScience();
		}
	}

	private void updatePlannedScience()
	{
		rendererManager.clearPlannedScience();
		imageScheduleTableController.getCollection().getAllItems().forEach(coll -> rendererManager.addPlannedScienceActors(coll.getProps()));
		spectrumScheduleTableController.getCollection().getAllItems().forEach(coll -> rendererManager.addPlannedScienceActors(coll.getProps()));
		lidarScheduleTableController.getCollection().getAllItems().forEach(coll -> rendererManager.addPlannedScienceActors(coll.getProps()));
//		rendererManager.addPlannedScienceActors(imageCollection.getProps());
//		rendererManager.addPlannedScienceActors(lidarTrackCollection.getProps());
		rendererManager.propertyChange(new PropertyChangeEvent(this, Properties.MODEL_CHANGED, null, null));
	}

	public JPanel getView()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(view);
		panel.add(intervalPlaybackPanel);
		return panel;
	}
}