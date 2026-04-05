package io.trapedge.runelite;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

@Slf4j
@PluginDescriptor(
	name = "TrapEdge",
	description = "Trap warnings and next action for OSRS flips",
	tags = {"flipping", "grand exchange", "profit", "risk", "decision"}
)
public class TrapEdgePlugin extends Plugin
{
	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private TrapEdgePanel panel;

	@Inject
	private TrapEdgeConfig config;

	private NavigationButton navigationButton;

	@Override
	protected void startUp()
	{
		navigationButton = NavigationButton.builder()
			.tooltip("TrapEdge")
			.icon(createIcon())
			.priority(5)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navigationButton);
		if (config.autoRefreshOnStartup())
		{
			panel.refreshSnapshot();
		}
		log.info("TrapEdge plugin started");
	}

	@Override
	protected void shutDown()
	{
		if (navigationButton != null)
		{
			clientToolbar.removeNavigation(navigationButton);
		}
		log.info("TrapEdge plugin stopped");
	}

	@Provides
	TrapEdgeConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TrapEdgeConfig.class);
	}

	private BufferedImage createIcon()
	{
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(new Color(143, 124, 255));
		g.fillRoundRect(0, 0, 16, 16, 6, 6);
		g.setColor(Color.WHITE);
		g.drawString("T", 5, 12);
		g.dispose();
		return image;
	}
}
