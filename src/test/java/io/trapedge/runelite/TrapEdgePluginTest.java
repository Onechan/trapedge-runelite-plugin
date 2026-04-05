package io.trapedge.runelite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
import net.runelite.client.plugins.PluginDescriptor;
import org.junit.Test;

public class TrapEdgePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TrapEdgePlugin.class);
		RuneLite.main(args);
	}

	@Test
	public void pluginDescriptorLoads()
	{
		PluginDescriptor descriptor = TrapEdgePlugin.class.getAnnotation(PluginDescriptor.class);
		assertNotNull(descriptor);
		assertEquals("TrapEdge", descriptor.name());
	}
}
