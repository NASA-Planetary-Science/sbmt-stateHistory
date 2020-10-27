package edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls.displayItems.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;

import edu.jhuapl.saavtk.color.gui.ColorProviderCellEditor;
import edu.jhuapl.saavtk.color.gui.ColorProviderCellRenderer;
import edu.jhuapl.saavtk.color.provider.ColorProvider;
import edu.jhuapl.saavtk.color.provider.ConstColorProvider;
import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.gui.util.ToolTipUtil;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.DisplayableItem;
import edu.jhuapl.sbmt.stateHistory.ui.state.popup.StateHistoryGuiUtil;
import edu.jhuapl.sbmt.stateHistory.ui.state.popup.StateHistoryPopupMenu;

import glum.gui.GuiUtil;
import glum.gui.misc.BooleanCellEditor;
import glum.gui.misc.BooleanCellRenderer;
import glum.gui.panel.itemList.ItemHandler;
import glum.gui.panel.itemList.ItemListPanel;
import glum.gui.panel.itemList.ItemProcessor;
import glum.gui.panel.itemList.query.QueryComposer;
import glum.gui.table.TablePopupHandler;
import glum.item.ItemManagerUtil;

public class DisplayOptionsTableView extends JPanel
{
	/**
	 * JButton to show state history in renderer
	 */
	private JButton showStateHistoryButton;

	/**
	 * JTable to display loaded state histories
	 */
	protected JTable resultList;

	/**
	 * JButtons for selection in the table
	 */
	private JButton selectAllB, selectInvertB, selectNoneB;

	/**
	 * The collection of loaded state history objects
	 */
	private StateHistoryCollection stateHistoryCollection;

	/**
	 * The state history item list panel, used to help handle interactions with
	 * the table
	 */
	private ItemListPanel<DisplayableItem> displayOptionsILP;

	/**
	 * The state history table handler, used to help populate the table
	 */
	private ItemHandler<DisplayableItem> displayOptionsTableHandler;

	/**
	 * @wbp.parser.constructor
	 */
	/**
	 * @param stateHistoryCollection
	 */
	public DisplayOptionsTableView(StateHistoryCollection stateHistoryCollection)
	{
		this.stateHistoryCollection = stateHistoryCollection;
		init();
	}

	/**
	 * Initializes UI elements
	 */
	protected void init()
	{
		resultList = buildTable();
		showStateHistoryButton = new JButton("Show State History");
		showStateHistoryButton.setEnabled(false);
	}

	/**
	 * Sets up the UI elements
	 */
	public void setup()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//		setBorder(new TitledBorder(null, "Available FOVs", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		JPanel panel_4 = new JPanel();
		add(panel_4);
		panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));

		Component horizontalGlue = Box.createHorizontalGlue();
		panel_4.add(horizontalGlue);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new java.awt.Dimension(150, 150));
		add(scrollPane);

		scrollPane.setViewportView(resultList);

//		JPanel panel_1 = new JPanel();
//		add(panel_1);
//		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
//		panel_1.add(showStateHistoryButton);

	}

	/**
	 * Builds the JTable.
	 *
	 * @return
	 */
	private JTable buildTable()
	{
		ActionListener listener = e ->
		{
			Object source = e.getSource();

			if (source == selectAllB)
				ItemManagerUtil.selectAll(stateHistoryCollection);
			else if (source == selectNoneB)
				ItemManagerUtil.selectNone(stateHistoryCollection);
			else if (source == selectInvertB)
			{
				ItemManagerUtil.selectInvert(stateHistoryCollection);
			}
		};

		// Popup menu
		StateHistoryPopupMenu stateHistoryPopupMenu = StateHistoryGuiUtil
				.formStateHistoryFileSpecPopupMenu(stateHistoryCollection, this);

		// Table header
		selectInvertB = GuiUtil.formButton(listener, IconUtil.getSelectInvert());
		selectInvertB.setToolTipText(ToolTipUtil.getSelectInvert());

		selectNoneB = GuiUtil.formButton(listener, IconUtil.getSelectNone());
		selectNoneB.setToolTipText(ToolTipUtil.getSelectNone());

		selectAllB = GuiUtil.formButton(listener, IconUtil.getSelectAll());
		selectAllB.setToolTipText(ToolTipUtil.getSelectAll());

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(selectInvertB, "w 24!,h 24!");
		buttonPanel.add(selectNoneB, "w 24!,h 24!");
		buttonPanel.add(selectAllB, "w 24!,h 24!,wrap 2");
		add(buttonPanel);

		// Table Content
		QueryComposer<DisplayOptionsColumnLookup> tmpComposer = new QueryComposer<>();
		tmpComposer.addAttribute(DisplayOptionsColumnLookup.Show, Boolean.class, "Show", null);
		tmpComposer.addAttribute(DisplayOptionsColumnLookup.Color, Color.class, "Color", null);
		tmpComposer.addAttribute(DisplayOptionsColumnLookup.Name, String.class, "Name", null);
		tmpComposer.addAttribute(DisplayOptionsColumnLookup.Label, String.class, "Label", null);
		tmpComposer.addAttribute(DisplayOptionsColumnLookup.Size, Double.class, "Size", null);

		tmpComposer.setEditor(DisplayOptionsColumnLookup.Show, new BooleanCellEditor());
		tmpComposer.setRenderer(DisplayOptionsColumnLookup.Show, new BooleanCellRenderer());
		JComboBox<Double> sizeCombo = new JComboBox<Double>(new Double[] {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0});
		sizeCombo.setSelectedIndex(5);
		tmpComposer.setEditor(DisplayOptionsColumnLookup.Size, new DefaultCellEditor(sizeCombo));

		tmpComposer.setEditor(DisplayOptionsColumnLookup.Label, new DefaultCellEditor(new JTextField()));
		tmpComposer.setEditor(DisplayOptionsColumnLookup.Color, new ColorProviderCellEditor<StateHistory>());
		tmpComposer.setRenderer(DisplayOptionsColumnLookup.Color, new ColorProviderCellRenderer(false));
		tmpComposer.setEditor(DisplayOptionsColumnLookup.Name, new DefaultCellEditor(new JTextField()));

		displayOptionsTableHandler = new DisplayOptionsItemHandler(stateHistoryCollection, tmpComposer);
		ItemProcessor<DisplayableItem> tmpIP = stateHistoryCollection.getDisplayItemsProcessor();
		displayOptionsILP = new ItemListPanel<>(displayOptionsTableHandler, tmpIP, true);
		displayOptionsILP.setSortingEnabled(true);
		configureColumnWidths();
		JTable stateHistoryTable = displayOptionsILP.getTable();
		stateHistoryTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		stateHistoryTable.addMouseListener(new TablePopupHandler(stateHistoryCollection, stateHistoryPopupMenu));

		return stateHistoryTable;
	}

	/**
	 * Returns the JTable used to display the list of loaded state histories
	 *
	 * @return the JTable used to display the list of loaded state histories
	 */
	public JTable getTable()
	{
		return resultList;
	}

	/**
	 * Returns the show state history button
	 *
	 * @return the show state history button
	 */
	public JButton getShowStateHistoryButton()
	{
		return showStateHistoryButton;
	}

	/**
	 * Configures the appropriate table colun width for the given expected type
	 * of data
	 */
	private void configureColumnWidths()
	{
		JTable tmpTable = displayOptionsILP.getTable();
		int minW = 30;

		ColorProvider blackCP = new ConstColorProvider(Color.BLACK);
		Object[] nomArr =
		{ true,  blackCP, "Segment000000000000000", "Segment000000000000000", 100000};
		for (int aCol = 0; aCol < nomArr.length; aCol++)
		{
			TableCellRenderer tmpRenderer = tmpTable.getCellRenderer(0, aCol);
			Component tmpComp = tmpRenderer.getTableCellRendererComponent(tmpTable, nomArr[aCol], false, false, 0,
					aCol);
			int tmpW = Math.max(minW, tmpComp.getPreferredSize().width + 1);
			tmpTable.getColumnModel().getColumn(aCol).setPreferredWidth(tmpW + 10);
		}
	}

}
