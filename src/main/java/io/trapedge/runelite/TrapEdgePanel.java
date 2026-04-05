package io.trapedge.runelite;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

@Singleton
public class TrapEdgePanel extends PluginPanel
{
	private static final String FILTER_ALL = "All";
	private static final Color CARD_BACKGROUND = ColorScheme.DARK_GRAY_COLOR;
	private static final Color PANEL_BACKGROUND = ColorScheme.DARKER_GRAY_COLOR;
	private static final Color TEXT_MUTED = new Color(190, 190, 190);
	private static final Color CHIP_NEUTRAL = new Color(77, 86, 102);
	private static final Color CHIP_GOOD = new Color(59, 112, 75);
	private static final Color CHIP_WARN = new Color(146, 110, 47);
	private static final Color CHIP_RISK = new Color(125, 61, 61);
	private static final Color CHIP_INFO = new Color(69, 88, 130);

	private final TrapEdgeApiClient apiClient;
	private final DefaultComboBoxModel<ItemChoice> itemChoices = new DefaultComboBoxModel<>();
	private final JComboBox<ItemChoice> itemSelector = new JComboBox<>(itemChoices);
	private final JTextField searchField = new JTextField();
	private final JComboBox<String> confidenceFilter = new JComboBox<>(new String[] {FILTER_ALL, "Strong", "Medium", "Weak"});
	private final JComboBox<String> actionFilter = new JComboBox<>(new String[] {FILTER_ALL, "Avoid", "Small size only", "Tradeable", "Investigate"});
	private final JComboBox<String> sortSelector = new JComboBox<>(new String[] {
		"Trap score (high first)",
		"Usable profit (high first)",
		"Confidence (best first)",
		"Name (A-Z)"
	});

	private final JLabel statusLabel = buildValueLabel("No data loaded yet");
	private final JLabel summaryLabel = buildValueLabel("No snapshot yet");
	private final JLabel filterSummaryLabel = buildMutedLabel("No active filters");
	private final JLabel itemCountChip = buildChipLabel("Items —", CHIP_INFO);
	private final JLabel replayChip = buildChipLabel("Replay —", CHIP_NEUTRAL);
	private final JLabel historyChip = buildChipLabel("History —", CHIP_NEUTRAL);
	private final JLabel detailTitleLabel = buildValueLabel("No item selected");
	private final JLabel detailMetricsLabel = buildMutedLabel("Select an item to load compact detail.");
	private final JLabel detailConfidenceChip = buildChipLabel("Confidence —", CHIP_NEUTRAL);
	private final JLabel detailActionChip = buildChipLabel("Action —", CHIP_NEUTRAL);
	private final JLabel detailCategoryChip = buildChipLabel("Category —", CHIP_NEUTRAL);
	private final JLabel detailRiskChip = buildChipLabel("Trap —", CHIP_NEUTRAL);

	private final JTextArea triageArea = buildArea(9);
	private final JTextArea detailArea = buildArea(11);
	private final JTextArea memoryArea = buildArea(9);

	private TrapEdgeSnapshot lastSnapshot;
	private ItemDetail selectedDetail;
	private boolean suppressSelectionEvents;

	@Inject
	public TrapEdgePanel(TrapEdgeApiClient apiClient)
	{
		this.apiClient = apiClient;

		setLayout(new BorderLayout());
		setBackground(PANEL_BACKGROUND);

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBorder(new EmptyBorder(10, 10, 10, 10));
		content.setBackground(PANEL_BACKGROUND);

		JButton refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(e -> refreshSnapshot());
		itemSelector.addActionListener(e -> {
			if (!suppressSelectionEvents)
			{
				loadSelectedItem();
			}
		});

		registerFilterListeners();

		content.add(buildSummaryCard());
		content.add(Box.createVerticalStrut(8));
		content.add(buildControlsCard(refreshButton));
		content.add(Box.createVerticalStrut(8));
		content.add(buildSectionCard("Triage", triageArea));
		content.add(Box.createVerticalStrut(8));
		content.add(buildDetailCard());
		content.add(Box.createVerticalStrut(8));
		content.add(buildSectionCard("Memory + Replay", memoryArea));

		JScrollPane scrollPane = new JScrollPane(content);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, BorderLayout.CENTER);
	}

	void refreshSnapshot()
	{
		statusLabel.setText("Loading snapshot...");
		new Thread(() -> {
			try
			{
				TrapEdgeSnapshot snapshot = apiClient.loadSnapshot();
				SwingUtilities.invokeLater(() -> renderSnapshot(snapshot));
			}
			catch (Exception ex)
			{
				SwingUtilities.invokeLater(() -> statusLabel.setText("Load failed: " + ex.getMessage()));
			}
		}, "trapedge-refresh").start();
	}

	private JPanel buildSummaryCard()
	{
		JPanel panel = buildCard();
		panel.add(buildSectionHeader("TrapEdge Control"), BorderLayout.NORTH);

		JPanel body = new JPanel();
		body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
		body.setOpaque(false);
		body.add(summaryLabel);
		body.add(Box.createVerticalStrut(4));
		body.add(statusLabel);
		body.add(Box.createVerticalStrut(8));
		body.add(buildChipRow(itemCountChip, replayChip, historyChip));
		panel.add(body, BorderLayout.CENTER);
		return panel;
	}

	private JPanel buildControlsCard(JButton refreshButton)
	{
		JPanel panel = buildCard();
		panel.add(buildSectionHeader("Controls"), BorderLayout.NORTH);

		JPanel body = new JPanel(new GridLayout(0, 2, 6, 6));
		body.setOpaque(false);

		styleTextField(searchField, "Filter by item, category, or reason...");
		styleCombo(itemSelector);
		styleCombo(confidenceFilter);
		styleCombo(actionFilter);
		styleCombo(sortSelector);
		styleButton(refreshButton);

		body.add(buildLabeledControl("Search", searchField));
		body.add(buildLabeledControl("Sort", sortSelector));
		body.add(buildLabeledControl("Confidence", confidenceFilter));
		body.add(buildLabeledControl("Action", actionFilter));
		body.add(buildLabeledControl("Item", itemSelector));
		body.add(buildLabeledControl("Refresh", refreshButton));

		JPanel wrapper = new JPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
		wrapper.setOpaque(false);
		wrapper.add(body);
		wrapper.add(Box.createVerticalStrut(6));
		wrapper.add(filterSummaryLabel);

		panel.add(wrapper, BorderLayout.CENTER);
		return panel;
	}

	private JPanel buildDetailCard()
	{
		JPanel panel = buildCard();
		panel.add(buildSectionHeader("Item Detail"), BorderLayout.NORTH);

		JPanel body = new JPanel();
		body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
		body.setOpaque(false);
		body.add(detailTitleLabel);
		body.add(Box.createVerticalStrut(4));
		body.add(detailMetricsLabel);
		body.add(Box.createVerticalStrut(8));
		body.add(buildChipRow(detailConfidenceChip, detailActionChip, detailCategoryChip, detailRiskChip));
		body.add(Box.createVerticalStrut(8));

		JScrollPane scrollPane = new JScrollPane(detailArea);
		scrollPane.setPreferredSize(new Dimension(340, 190));
		scrollPane.setBorder(BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR));
		body.add(scrollPane);

		panel.add(body, BorderLayout.CENTER);
		return panel;
	}

	private JPanel buildSectionCard(String title, JTextArea area)
	{
		JPanel panel = buildCard();
		panel.add(buildSectionHeader(title), BorderLayout.NORTH);
		JScrollPane scrollPane = new JScrollPane(area);
		scrollPane.setPreferredSize(new Dimension(340, 190));
		scrollPane.setBorder(BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR));
		panel.add(scrollPane, BorderLayout.CENTER);
		return panel;
	}

	private void registerFilterListeners()
	{
		searchField.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				applyFiltersKeepingSelection();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				applyFiltersKeepingSelection();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				applyFiltersKeepingSelection();
			}
		});

		confidenceFilter.addActionListener(e -> applyFiltersKeepingSelection());
		actionFilter.addActionListener(e -> applyFiltersKeepingSelection());
		sortSelector.addActionListener(e -> applyFiltersKeepingSelection());
	}

	private void renderSnapshot(TrapEdgeSnapshot snapshot)
	{
		this.lastSnapshot = snapshot;
		statusLabel.setText("Snapshot loaded: " + snapshot.sourceGeneratedAt);
		summaryLabel.setText(buildSummary(snapshot));
		itemCountChip.setText("Items " + (snapshot.assessmentSummary != null ? snapshot.assessmentSummary.total : 0));
		replayChip.setText(snapshot.replaySummary != null
			? "Replay " + snapshot.replaySummary.exactPasses + "/" + snapshot.replaySummary.total
			: "Replay n/a");
		historyChip.setText("History " + snapshot.runtimePostmortemCount);
		memoryArea.setText(renderMemory(snapshot.memoryPressure, snapshot.memoryPatterns, snapshot.proofCases, snapshot.replaySummary));
		applyFiltersKeepingSelection();
	}

	private void applyFiltersKeepingSelection()
	{
		if (lastSnapshot == null)
		{
			return;
		}

		int previousItemId = selectedDetail != null
			? selectedDetail.itemId
			: (((ItemChoice) itemSelector.getSelectedItem()) != null ? ((ItemChoice) itemSelector.getSelectedItem()).itemId : -1);
		List<TriageRow> filteredRows = buildFilteredRows(lastSnapshot.triage);
		rebuildItemChoices(filteredRows, previousItemId);
		triageArea.setText(renderTriage(filteredRows, lastSnapshot.actions, lastSnapshot.replaySummary, lastSnapshot.runtimePostmortemCount));
		filterSummaryLabel.setText(buildFilterSummary(filteredRows));
	}

	private List<TriageRow> buildFilteredRows(List<TriageRow> sourceRows)
	{
		if (sourceRows == null)
		{
			return new ArrayList<>();
		}

		String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
		String confidence = String.valueOf(confidenceFilter.getSelectedItem());
		String action = String.valueOf(actionFilter.getSelectedItem());
		Comparator<TriageRow> comparator = comparatorFor(String.valueOf(sortSelector.getSelectedItem()));

		return sourceRows.stream()
			.filter(row -> matchesSearch(row, search))
			.filter(row -> matchesConfidence(row, confidence))
			.filter(row -> matchesAction(row, action))
			.sorted(comparator)
			.collect(Collectors.toList());
	}

	private Comparator<TriageRow> comparatorFor(String sort)
	{
		if ("Usable profit (high first)".equals(sort))
		{
			return Comparator.comparingLong((TriageRow row) -> row.usableProfitEstimate).reversed()
				.thenComparingInt(row -> row.trapScore).reversed()
				.thenComparing(row -> row.itemName, String.CASE_INSENSITIVE_ORDER);
		}
		if ("Confidence (best first)".equals(sort))
		{
			return Comparator.comparingInt((TriageRow row) -> confidenceRank(row.executionConfidence)).reversed()
				.thenComparingInt(row -> row.trapScore).reversed()
				.thenComparing(row -> row.itemName, String.CASE_INSENSITIVE_ORDER);
		}
		if ("Name (A-Z)".equals(sort))
		{
			return Comparator.comparing(row -> row.itemName, String.CASE_INSENSITIVE_ORDER);
		}
		return Comparator.comparingInt((TriageRow row) -> row.trapScore).reversed()
			.thenComparingLong(row -> row.usableProfitEstimate).reversed()
			.thenComparing(row -> row.itemName, String.CASE_INSENSITIVE_ORDER);
	}

	private boolean matchesSearch(TriageRow row, String search)
	{
		if (search.isEmpty())
		{
			return true;
		}
		return joinSearchText(row).contains(search);
	}

	private String joinSearchText(TriageRow row)
	{
		return String.join(" ",
			row.itemName == null ? "" : row.itemName,
			row.caseCategory == null ? "" : row.caseCategory,
			row.reasonSummary == null ? "" : row.reasonSummary,
			row.recommendedActionLabel == null ? "" : row.recommendedActionLabel,
			row.executionConfidenceLabel == null ? "" : row.executionConfidenceLabel
		).toLowerCase(Locale.ROOT);
	}

	private boolean matchesConfidence(TriageRow row, String filterValue)
	{
		return FILTER_ALL.equals(filterValue) || filterValue.equalsIgnoreCase(row.executionConfidenceLabel) || filterValue.equalsIgnoreCase(row.executionConfidence);
	}

	private boolean matchesAction(TriageRow row, String filterValue)
	{
		if (FILTER_ALL.equals(filterValue))
		{
			return true;
		}
		return filterValue.equalsIgnoreCase(row.recommendedActionLabel) || filterValue.equalsIgnoreCase(row.recommendedAction);
	}

	private void rebuildItemChoices(List<TriageRow> filteredRows, int previousItemId)
	{
		suppressSelectionEvents = true;
		itemChoices.removeAllElements();
		for (TriageRow row : filteredRows)
		{
			itemChoices.addElement(new ItemChoice(row));
		}
		suppressSelectionEvents = false;
		selectBestItem(previousItemId);
	}

	private void selectBestItem(int previousItemId)
	{
		if (itemChoices.getSize() == 0)
		{
			selectedDetail = null;
			detailArea.setText("No items match the current filters.");
			clearDetailSummary();
			return;
		}

		int nextIndex = 0;
		for (int i = 0; i < itemChoices.getSize(); i++)
		{
			ItemChoice choice = itemChoices.getElementAt(i);
			if (choice.itemId == previousItemId)
			{
				nextIndex = i;
				break;
			}
		}

		suppressSelectionEvents = true;
		itemSelector.setSelectedIndex(nextIndex);
		suppressSelectionEvents = false;
		loadSelectedItem();
	}

	private void loadSelectedItem()
	{
		ItemChoice choice = (ItemChoice) itemSelector.getSelectedItem();
		if (choice == null)
		{
			return;
		}

		detailTitleLabel.setText(choice.name);
		detailMetricsLabel.setText("Loading compact detail...");
		detailArea.setText("Loading detail for " + choice.name + "...");
		new Thread(() -> {
			try
			{
				ItemDetail detail = apiClient.loadItemDetail(choice.itemId);
				SwingUtilities.invokeLater(() -> renderDetail(detail));
			}
			catch (Exception ex)
			{
				SwingUtilities.invokeLater(() -> detailArea.setText("Detail load failed: " + ex.getMessage()));
			}
		}, "trapedge-detail").start();
	}

	private void renderDetail(ItemDetail detail)
	{
		this.selectedDetail = detail;
		detailTitleLabel.setText(detail.itemName);
		detailMetricsLabel.setText(buildDetailMetrics(detail));
		detailConfidenceChip.setText(detail.executionConfidenceLabel);
		detailConfidenceChip.setBackground(colorForConfidence(detail.executionConfidenceLabel));
		detailActionChip.setText(detail.recommendedActionLabel);
		detailActionChip.setBackground(colorForAction(detail.recommendedActionLabel));
		detailCategoryChip.setText(shorten(detail.caseCategory, 22));
		detailCategoryChip.setBackground(CHIP_INFO);
		detailRiskChip.setText("Trap " + detail.trapScore);
		detailRiskChip.setBackground(colorForTrapScore(detail.trapScore));
		detailArea.setText(renderCompactDetailText(detail));
	}

	private void clearDetailSummary()
	{
		detailTitleLabel.setText("No item selected");
		detailMetricsLabel.setText("Select an item to load compact detail.");
		detailConfidenceChip.setText("Confidence —");
		detailConfidenceChip.setBackground(CHIP_NEUTRAL);
		detailActionChip.setText("Action —");
		detailActionChip.setBackground(CHIP_NEUTRAL);
		detailCategoryChip.setText("Category —");
		detailCategoryChip.setBackground(CHIP_NEUTRAL);
		detailRiskChip.setText("Trap —");
		detailRiskChip.setBackground(CHIP_NEUTRAL);
	}

	private String buildSummary(TrapEdgeSnapshot snapshot)
	{
		if (snapshot.assessmentSummary == null)
		{
			return "Snapshot ready";
		}
		return "Items " + snapshot.assessmentSummary.total
			+ " | replay exact " + (snapshot.replaySummary != null ? snapshot.replaySummary.exactPasses + "/" + snapshot.replaySummary.total : "n/a")
			+ " | history " + snapshot.runtimePostmortemCount;
	}

	private String buildFilterSummary(List<TriageRow> filteredRows)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Visible items: ").append(filteredRows.size());
		if (!FILTER_ALL.equals(String.valueOf(confidenceFilter.getSelectedItem())))
		{
			sb.append(" | confidence ").append(confidenceFilter.getSelectedItem());
		}
		if (!FILTER_ALL.equals(String.valueOf(actionFilter.getSelectedItem())))
		{
			sb.append(" | action ").append(actionFilter.getSelectedItem());
		}
		String search = searchField.getText() == null ? "" : searchField.getText().trim();
		if (!search.isEmpty())
		{
			sb.append(" | search \"").append(search).append("\"");
		}
		return sb.toString();
	}

	private String buildDetailMetrics(ItemDetail detail)
	{
		if (detail.raw == null)
		{
			return "Compact judgment ready.";
		}
		return "Buy/Sell " + detail.raw.buyPrice + "/" + detail.raw.sellPrice
			+ " • Net " + detail.raw.taxAdjustedMargin
			+ " • Usable " + detail.usableProfitEstimate
			+ " • 5m/1h " + detail.fiveMinuteFlow + "/" + detail.oneHourFlow
			+ " • Fresh " + detail.raw.freshnessSeconds + "s";
	}

	private String renderTriage(List<TriageRow> triage, List<ActionRow> actions, ReplaySummary replaySummary, int historyCount)
	{
		StringBuilder sb = new StringBuilder();
		if (replaySummary != null)
		{
			sb.append("Replay exact: ").append(replaySummary.exactPasses).append('/').append(replaySummary.total)
				.append(" | action: ").append(replaySummary.actionPasses).append('/').append(replaySummary.total)
				.append(" | flag coverage: ").append(replaySummary.flagCoveragePasses).append('/').append(replaySummary.total)
				.append("\n");
		}
		sb.append("History entries: ").append(historyCount).append("\n\n");
		if (triage != null)
		{
			for (int i = 0; i < Math.min(triage.size(), 8); i++)
			{
				TriageRow row = triage.get(i);
				sb.append(i + 1).append(". ").append(row.itemName)
					.append("\n   ").append(row.executionConfidenceLabel)
					.append(" | ").append(row.recommendedActionLabel)
					.append(" | trap ").append(row.trapScore)
					.append(" | usable ").append(row.usableProfitEstimate)
					.append("\n   ").append(row.caseCategory)
					.append("\n   ").append(shorten(row.reasonSummary, 170))
					.append("\n\n");
			}
		}
		if (actions != null && !actions.isEmpty())
		{
			ActionRow top = actions.get(0);
			sb.append("Top action note:\n")
				.append(top.itemName)
				.append(" -> ")
				.append(top.actionLabel)
				.append("\n")
				.append(shorten(top.whyNow, 160));
		}
		return sb.toString();
	}

	private String renderCompactDetailText(ItemDetail detail)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Why now\n")
			.append(shorten(detail.reasonSummary, 220))
			.append("\n\n")
			.append("Flags\n")
			.append(buildFlagSummary(detail))
			.append("\n\n")
			.append("Strengths\n")
			.append(buildStrengthSummary(detail))
			.append("\n\n")
			.append("Memory\n")
			.append(buildMemorySummary(detail))
			.append("\n\n")
			.append("History\n")
			.append(buildRuntimePostmortemSummary(detail))
			.append("\n\n")
			.append("Matched rules\n")
			.append(buildRuleSummary(detail));
		return sb.toString();
	}

	private String buildFlagSummary(ItemDetail detail)
	{
		if (detail.flagDetails == null || detail.flagDetails.isEmpty())
		{
			return "- none";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < Math.min(detail.flagDetails.size(), 3); i++)
		{
			FlagDetail flag = detail.flagDetails.get(i);
			sb.append("- ").append(flag.trapType)
				.append(" [").append(flag.severity).append("] ")
				.append(shorten(flag.explanation, 110));
			if (i < Math.min(detail.flagDetails.size(), 3) - 1)
			{
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	private String buildStrengthSummary(ItemDetail detail)
	{
		if (detail.strengths == null || detail.strengths.isEmpty())
		{
			return "- none";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < Math.min(detail.strengths.size(), 3); i++)
		{
			StrengthDetail strength = detail.strengths.get(i);
			sb.append("- ").append(strength.strengthType).append(": ").append(shorten(strength.explanation, 95));
			if (i < Math.min(detail.strengths.size(), 3) - 1)
			{
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	private String buildMemorySummary(ItemDetail detail)
	{
		if (detail.memoryNotes == null || detail.memoryNotes.isEmpty())
		{
			return "- no direct memory warning yet";
		}
		return detail.memoryNotes.stream()
			.limit(3)
			.map(note -> "- " + shorten(note, 100))
			.collect(Collectors.joining("\n"));
	}

	private String buildRuntimePostmortemSummary(ItemDetail detail)
	{
		if (detail.runtimePostmortems == null || detail.runtimePostmortems.isEmpty())
		{
			return "- no matching history yet";
		}
		return detail.runtimePostmortems.stream()
			.limit(2)
			.map(entry -> "- " + entry.postmortemLabel + " | exp " + entry.expectedMargin + " | real " + entry.realizedMargin + " | " + shorten(entry.notes, 80))
			.collect(Collectors.joining("\n"));
	}

	private String buildRuleSummary(ItemDetail detail)
	{
		if (detail.matchedRules == null || detail.matchedRules.isEmpty())
		{
			return "- none";
		}
		return detail.matchedRules.stream()
			.limit(3)
			.map(rule -> "- " + shorten(rule, 95))
			.collect(Collectors.joining("\n"));
	}

	private String renderMemory(List<MemoryRow> memoryRows, List<MemoryPattern> memoryPatterns, List<ProofCase> proofCases, ReplaySummary replaySummary)
	{
		StringBuilder sb = new StringBuilder();
		if (memoryRows != null)
		{
			for (int i = 0; i < Math.min(memoryRows.size(), 4); i++)
			{
				MemoryRow row = memoryRows.get(i);
				sb.append(row.itemName)
					.append(" — mistakes ").append(row.previousMistakes)
					.append(" — risk ").append(row.memoryRiskLevel)
					.append("\n");
				if (row.nextRuleHints != null && !row.nextRuleHints.isEmpty())
				{
					sb.append("  next rule: ").append(row.nextRuleHints.get(0)).append("\n");
				}
				sb.append("\n");
			}
		}
		if (memoryPatterns != null && !memoryPatterns.isEmpty())
		{
			sb.append("Patterns:\n");
			for (int i = 0; i < Math.min(memoryPatterns.size(), 3); i++)
			{
				MemoryPattern pattern = memoryPatterns.get(i);
				sb.append("- ").append(pattern.patternLabel)
					.append(" (").append(pattern.timesSeen).append(")")
					.append(" -> ").append(pattern.nextRule)
					.append("\n");
			}
			sb.append("\n");
		}
		if (replaySummary != null)
		{
			sb.append("Replay:\n")
				.append("- exact ").append(replaySummary.exactPasses).append('/').append(replaySummary.total).append("\n")
				.append("- confidence ").append(replaySummary.confidencePasses).append('/').append(replaySummary.total).append("\n")
				.append("- category ").append(replaySummary.categoryPasses).append('/').append(replaySummary.total).append("\n\n");
		}
		if (proofCases != null && !proofCases.isEmpty())
		{
			sb.append("Proof pack top case: ").append(proofCases.get(0).itemName).append(" — ").append(proofCases.get(0).recommendedActionLabel);
		}
		return sb.toString();
	}

	private JPanel buildCard()
	{
		JPanel panel = new JPanel(new BorderLayout(0, 8));
		panel.setBackground(CARD_BACKGROUND);
		panel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR),
			new EmptyBorder(8, 8, 8, 8)
		));
		panel.setAlignmentX(LEFT_ALIGNMENT);
		return panel;
	}

	private JPanel buildChipRow(JLabel... chips)
	{
		JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		row.setOpaque(false);
		for (JLabel chip : chips)
		{
			row.add(chip);
		}
		return row;
	}

	private JLabel buildSectionHeader(String text)
	{
		JLabel label = new JLabel(text);
		label.setForeground(Color.WHITE);
		label.setFont(FontManager.getRunescapeBoldFont());
		return label;
	}

	private JLabel buildFieldLabel(String text)
	{
		JLabel label = new JLabel(text);
		label.setForeground(TEXT_MUTED);
		return label;
	}

	private JLabel buildValueLabel(String text)
	{
		JLabel label = new JLabel(text);
		label.setForeground(Color.WHITE);
		return label;
	}

	private JLabel buildMutedLabel(String text)
	{
		JLabel label = new JLabel(text);
		label.setForeground(TEXT_MUTED);
		return label;
	}

	private JLabel buildChipLabel(String text, Color background)
	{
		JLabel label = new JLabel(text);
		label.setOpaque(true);
		label.setBackground(background);
		label.setForeground(Color.WHITE);
		label.setBorder(new EmptyBorder(3, 8, 3, 8));
		return label;
	}

	private JPanel buildLabeledControl(String label, JComponent control)
	{
		JPanel panel = new JPanel(new BorderLayout(0, 4));
		panel.setOpaque(false);
		panel.add(buildFieldLabel(label), BorderLayout.NORTH);
		panel.add(control, BorderLayout.CENTER);
		return panel;
	}

	private JTextArea buildArea(int rows)
	{
		JTextArea area = new JTextArea();
		area.setEditable(false);
		area.setRows(rows);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setBackground(PANEL_BACKGROUND);
		area.setForeground(Color.WHITE);
		area.setCaretColor(Color.WHITE);
		area.setBorder(new EmptyBorder(6, 6, 6, 6));
		return area;
	}

	private void styleTextField(JTextField field, String tooltip)
	{
		field.setBackground(PANEL_BACKGROUND);
		field.setForeground(Color.WHITE);
		field.setCaretColor(Color.WHITE);
		field.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR),
			new EmptyBorder(4, 6, 4, 6)
		));
		field.setToolTipText(tooltip);
	}

	private void styleCombo(JComboBox<?> comboBox)
	{
		comboBox.setBackground(PANEL_BACKGROUND);
		comboBox.setForeground(Color.WHITE);
		comboBox.setToolTipText("TrapEdge in-client control");
	}

	private void styleButton(JButton button)
	{
		button.setBackground(PANEL_BACKGROUND);
		button.setForeground(Color.WHITE);
	}

	private Color colorForConfidence(String confidenceLabel)
	{
		if (confidenceLabel == null)
		{
			return CHIP_NEUTRAL;
		}
		switch (confidenceLabel.toLowerCase(Locale.ROOT))
		{
			case "strong":
				return CHIP_GOOD;
			case "medium":
				return CHIP_WARN;
			case "weak":
				return CHIP_RISK;
			default:
				return CHIP_NEUTRAL;
		}
	}

	private Color colorForAction(String actionLabel)
	{
		if (actionLabel == null)
		{
			return CHIP_NEUTRAL;
		}
		String normalized = actionLabel.toLowerCase(Locale.ROOT);
		if (normalized.contains("avoid"))
		{
			return CHIP_RISK;
		}
		if (normalized.contains("small"))
		{
			return CHIP_WARN;
		}
		if (normalized.contains("tradeable") || normalized.contains("enter"))
		{
			return CHIP_GOOD;
		}
		return CHIP_INFO;
	}

	private Color colorForTrapScore(int trapScore)
	{
		if (trapScore >= 60)
		{
			return CHIP_RISK;
		}
		if (trapScore >= 25)
		{
			return CHIP_WARN;
		}
		return CHIP_GOOD;
	}

	private int confidenceRank(String confidence)
	{
		if (confidence == null)
		{
			return 0;
		}
		switch (confidence.toLowerCase(Locale.ROOT))
		{
			case "strong":
				return 3;
			case "medium":
				return 2;
			case "weak":
				return 1;
			default:
				return 0;
		}
	}

	private String shorten(String value, int maxLength)
	{
		if (value == null)
		{
			return "";
		}
		if (value.length() <= maxLength)
		{
			return value;
		}
		return value.substring(0, Math.max(0, maxLength - 1)).trim() + "…";
	}

	private static class ItemChoice
	{
		private final int itemId;
		private final String name;
		private final String confidenceLabel;
		private final String actionLabel;
		private final long usableProfitEstimate;
		private final int trapScore;

		private ItemChoice(TriageRow row)
		{
			this.itemId = row.itemId;
			this.name = row.itemName;
			this.confidenceLabel = row.executionConfidenceLabel;
			this.actionLabel = row.recommendedActionLabel;
			this.usableProfitEstimate = row.usableProfitEstimate;
			this.trapScore = row.trapScore;
		}

		@Override
		public String toString()
		{
			return name + " • " + confidenceLabel + " • " + actionLabel + " • U" + usableProfitEstimate + " • T" + trapScore;
		}
	}
}
