package edu.jhuapl.sbmt.stateHistory.ui.state.color;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import edu.jhuapl.saavtk.colormap.Colormap;
import edu.jhuapl.saavtk.colormap.ColormapUtil;
import edu.jhuapl.saavtk.colormap.Colormaps;

import glum.gui.component.GComboBox;
import glum.gui.component.GNumberField;
import glum.text.SigFigNumberFormat;
import net.miginfocom.swing.MigLayout;

/**
 * UI that allows the user to select a color bar and specify the range of values
 * associated with the color bar.
 *
 * Originally made for Lidar by lopeznr1
 *
 * @author steelrj1
 */
public class ColorBarPanel<G1> extends JPanel implements ActionListener
{
	// State vars
	private List<ActionListener> listenerL;
	private double defaultMin, defaultMax;

	// Gui vars
	private final JLabel featureL;
	private final CustomListCellRenderer featureLCR;
	private final GComboBox<G1> featureBox;
	private final GComboBox<Colormap> colormapBox;
	private final JLabel colormapL;
	private final JLabel minValueL, maxValueL, numLevelsL;
	private final GNumberField minValueNF, maxValueNF, numLevelsNF;
	private final JCheckBox logScaleCB;
	private final JCheckBox showColorBarCB;
	private final JButton resetB;
	private final JButton applyB;
	private final JToggleButton syncB;

	/**
	 * Standard Constructor
	 */
	public ColorBarPanel()
	{
		listenerL = new ArrayList<>();
		defaultMin = Double.NaN;
		defaultMax = Double.NaN;

		featureL = new JLabel("Property:");
		featureLCR = new CustomListCellRenderer();
		featureBox = new GComboBox<>(this, featureLCR);
		featureBox.addActionListener(this);

		// ColorBar components
		colormapL = new JLabel();

		colormapBox = new GComboBox<>();
//		colormapBox.setRenderer(ColormapUtil.getFancyColormapRender2(colormapBox));
//		for (String aStr : Colormaps.getAllBuiltInColormapNames())
//		{
//			Colormap cmap = Colormaps.getNewInstanceOfBuiltInColormap(aStr);
//			colormapBox.addItem(cmap);
//			if (cmap.getName().equals(Colormaps.getCurrentColormapName()))
//				colormapBox.setSelectedItem(cmap);
//		}
//		colormapBox.addActionListener(this);

		// Range, NumColorLevels components
		minValueL = new JLabel("Min Val:");
		maxValueL = new JLabel("Max Val:");
		numLevelsL = new JLabel("# Levels:");
		minValueNF = new GNumberField(this, new SigFigNumberFormat(3));
		maxValueNF = new GNumberField(this, new SigFigNumberFormat(3));
		numLevelsNF = new GNumberField(this);
		numLevelsNF.setValue(32);

		// Action buttons
		logScaleCB = new JCheckBox("Log scale");
		logScaleCB.addActionListener(this);
		showColorBarCB = new JCheckBox("Show Color Bar");
		showColorBarCB.addActionListener(this);
		resetB = new JButton("Range Reset");
		resetB.addActionListener(this);
		syncB = new JToggleButton("Sync", true);
		syncB.addActionListener(this);
		applyB = new JButton("Apply");
		applyB.addActionListener(this);

		// Construct the GUI
		buildGui();

		updateColorMapArea();
	}

	/**
	 * Registers a listener with this panel.
	 */
	public void addActionListener(ActionListener aListener)
	{
		listenerL.add(aListener);
	}

	/**
	 * Adds in a feature to the supported list of features.
	 */
	public void addFeatureType(G1 aFeature, String aLabel)
	{
		featureLCR.addMapping(aFeature, aLabel);
		featureBox.addItem(aFeature);
	}

	/**
	 * Deregisters a listener with this panel.
	 */
	public void delActionListener(ActionListener aListener)
	{
		listenerL.remove(aListener);
	}

	/**
	 * Returns the user configured {@link ColorMapAttr} .
	 */
	public ColorMapAttr getColorMapAttr()
	{
		String name = colormapBox.getChosenItem().getName();
		double minVal = minValueNF.getValue();
		double maxVal = maxValueNF.getValue();
		int numLevels = numLevelsNF.getValueAsInt(-1);
		boolean isLogScale = logScaleCB.isSelected();

		return new ColorMapAttr(name, minVal, maxVal, numLevels, isLogScale);
	}

	/**
	 * Returns the Colormap name associated with this panel.
	 */
	public String getColormapName()
	{
		return colormapBox.getChosenItem().getName();
	}

	/**
	 * Returns the FeatureType associated with this panel.
	 * <P>
	 * The FeatureType is the physical quality attribute that coloring will be
	 * based off of.
	 */
	public G1 getFeatureType()
	{
		return featureBox.getChosenItem();
	}

	/**
	 * Returns the current min value.
	 */
	public double getCurrentMinValue()
	{
		return minValueNF.getValue();
	}

	/**
	 * Returns the current min value.
	 */
	public double getCurrentMaxValue()
	{
		return maxValueNF.getValue();
	}

	/**
	 * Returns the default min value.
	 */
	public double getDefaultMinValue()
	{
		return defaultMin;
	}

	/**
	 * Returns the default max value.
	 */
	public double getDefaultMaxValue()
	{
		return defaultMax;
	}

	/**
	 * Updates the GUI to reflect the new min,max range values.
	 * <P>
	 * This method will not trigger an event.
	 */
	public void setCurrentMinMax(double aMin, double aMax)
	{
		minValueNF.setValue(aMin);
		maxValueNF.setValue(aMax);

		updateControlArea();
	}

	/**
	 * Sets in the default values for the range. This will have an effect on the
	 * resetB UI.
	 */
	public void setDefaultRange(double aMin, double aMax)
	{
		defaultMin = aMin;
		defaultMax = aMax;

		updateControlArea();
	}

	/**
	 * Sets in the active selected feature.
	 */
	public void setFeatureType(G1 aFeature)
	{
		featureBox.setSelectedItem(aFeature);
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();

		// Reset the defaults
		if (source == resetB)
		{
			setCurrentMinMax(defaultMin, defaultMax);
			doAutoSync();
		}

		// Apply the settings
		else if (source == applyB)
		{
			notifyListeners();
		}

		// Sync toggle
		else if (source == syncB)
		{
			updateControlArea();
			doAutoSync();
		}

		// LogScale UI
		else if (source == logScaleCB)
		{
			notifyListeners();
		}

		// ColorMap ComboBox UI
		else if (source == colormapBox)
		{
			updateColorMapArea();
			doAutoSync();
		}

		// Property ComboBox UI
		else if (source == featureBox)
		{
			updateColorMapArea();

			updateDefaultRange();
			setCurrentMinMax(defaultMin, defaultMax);

			doAutoSync();
		}

		// Various NumberFields UI
		else if (source == minValueNF || source == maxValueNF || source == numLevelsNF)
		{
			updateControlArea();

			if (source == numLevelsNF)
				updateColorMapArea();

			doAutoSync();
		}

		// Color Bar display UI
		else if (source == showColorBarCB)
		{
			notifyListeners();
		}
	}

	/**
	 * Helper method that updates the colorMapL to reflect the selection of
	 * colorMapBox and numColorLevelsNF.
	 */
	protected void updateColorMapArea()
	{
		int iconW = colormapL.getWidth();
		if (iconW < 16)
			iconW = 16;
		int iconH = colormapL.getHeight();
		if (iconH < 16)
			iconH = 16;

		int numLevels = numLevelsNF.getValueAsInt(-1);
		if (numLevels == -1)
			numLevels = 0;

		String name = colormapBox.getChosenItem().getName();
		Colormap tmpColormap = Colormaps.getNewInstanceOfBuiltInColormap(name);
		tmpColormap.setNumberOfLevels(numLevels);
		colormapL.setIcon(ColormapUtil.createIcon(tmpColormap, iconW, iconH));
	}

	/**
	 * Helper method that will updates the default range.
	 * <P>
	 * This method should be overridden to get custom default ranges.
	 */
	protected void updateDefaultRange()
	{
		setDefaultRange(0, 1.0);
	}

	/**
	 * Helper method which layouts the panel.
	 */
	private void buildGui()
	{
		setLayout(new MigLayout("", "0[right][120::,fill]15[left]0", "0[][]"));

		// Property feature
		add(featureL, "");
		add(featureBox, "growx,span,wrap");

		// Colormap selector area
		add(colormapL, "growx,span,w 10::,wrap 3");
		add(colormapBox, "growx,span,wrap");

		// Range, # Color levels, and # Ticks area
		add(minValueL, "");
		add(minValueNF, "");
		add(resetB, "sg g1,wrap");

		add(maxValueL, "");
		add(maxValueNF, "");
		add(syncB, "sg g1,wrap");

		add(numLevelsL, "");
		add(numLevelsNF, "");
		add(applyB, "sg g1,wrap 0");
//
//		add(showColorBarCB, "skip 2,sg g1,wrap 0");
	}

	/**
	 * Helper method that synchronizes the ColorMap when the sync toggle is
	 * enabled. Notification will be sent out to the listeners.
	 */
	private void doAutoSync()
	{
		// Bail if the syncB is not selected
		if (syncB.isSelected() == false)
			return;

		// Bail if action GUI is not in a valid state
		if (isColorMapConfigValid() == false)
			return;

		// Send out notification of the changes
		notifyListeners();
	}

	/**
	 * Helper method to determine if the Colormap configuration is even valid
	 */
	private boolean isColorMapConfigValid()
	{
		boolean isValid = true;
		isValid &= minValueNF.isValidInput();
		isValid &= maxValueNF.isValidInput();
		isValid &= minValueNF.getValue() <= maxValueNF.getValue();
		isValid &= numLevelsNF.isValidInput();

		return isValid;
	}

	/**
	 * Helper method that sends out notification to our listeners
	 */
	private void notifyListeners()
	{
		for (ActionListener aListener : listenerL)
			aListener.actionPerformed(new ActionEvent(this, 0, ""));
	}

	/**
	 * Helper method to configure the various UI elements in the control section.
	 * <P>
	 * UI elements in the control area will be disabled for invalid
	 * configuration.
	 */
	private void updateControlArea()
	{
		boolean isEnabled, isSwapped;

		// Update MinValue MaxValue UI elements
		String errMsgMin = null;
		String errMsgMax = null;
		Color fgColorMin = Color.BLACK;
		Color fgColorMax = Color.BLACK;
		Color fgColorLev = Color.BLACK;
		Color fgColorFail = minValueNF.getFailColor();
		isSwapped = minValueNF.getValue() > maxValueNF.getValue();
		isSwapped &= minValueNF.isValidInput() == true;
		isSwapped &= maxValueNF.isValidInput() == true;
		if (isSwapped == true)
		{
			errMsgMin = errMsgMax = "Min, Max values are swapped.";
			fgColorMin = fgColorMax = fgColorFail;
		}
		if (minValueNF.getText().isEmpty() == true)
			fgColorMin = fgColorFail;
		if (maxValueNF.getText().isEmpty() == true)
			fgColorMax = fgColorFail;
		if (numLevelsNF.getText().isEmpty() == true)
			fgColorLev = fgColorFail;

		minValueL.setForeground(fgColorMin);
		maxValueL.setForeground(fgColorMax);
		numLevelsL.setForeground(fgColorLev);
		minValueL.setToolTipText(errMsgMin);
		maxValueL.setToolTipText(errMsgMax);

		// Update enable state of resetB
		isEnabled = false;
		isEnabled |= Double.compare(defaultMin, minValueNF.getValue()) != 0;
		isEnabled |= Double.compare(defaultMax, maxValueNF.getValue()) != 0;
		resetB.setEnabled(isEnabled);

		// Update enable state of applyB
		isEnabled = syncB.isSelected() != true;
		isEnabled &= isColorMapConfigValid();
		applyB.setEnabled(isEnabled);
	}

}
