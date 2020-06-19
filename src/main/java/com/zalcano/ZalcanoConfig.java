package com.zalcano;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Zalcano")
public interface ZalcanoConfig extends Config
{
	@ConfigItem(
		keyName = "Zalcano",
		name = "ZalcanoPlugin",
		description = "Zalcano Utilities"
	)
	default String greeting()
	{
		return "Hello";
	}
}
