package io.trapedge.runelite;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("trapedge")
public interface TrapEdgeConfig extends Config
{
	@ConfigItem(
		keyName = "apiBaseUrl",
		name = "API base URL",
		description = "TrapEdge plugin API base URL"
	)
	default String apiBaseUrl()
	{
		return "http://127.0.0.1:4311";
	}

	@ConfigItem(
		keyName = "autoRefreshOnStartup",
		name = "Auto refresh on startup",
		description = "Fetch the latest snapshot when the plugin starts"
	)
	default boolean autoRefreshOnStartup()
	{
		return true;
	}
}
