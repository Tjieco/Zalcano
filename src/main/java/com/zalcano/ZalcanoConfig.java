package com.zalcano;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Zalcano")
public interface ZalcanoConfig extends Config
{
	@ConfigItem(
			position = 1,
			keyName = "Health",
			name = "Show Zalcano health",
			description = "Shows current Zalcano health."
	)
	default boolean showHealth()
	{
		return true;
	}

	@ConfigItem(
			position = 2,
			keyName = "PlayerCount",
			name = "Show amount of players in the room",
			description = "Shows amount of players in the Zalcano room. (Players standing at the gate are excluded)"
	)
	default boolean showPlayerCount()
	{
		return true;
	}

	@ConfigItem(
		position = 3,
		keyName = "Damage",
		name = "Show damage dealt by player",
		description = "Shows damage dealt by the player and minimum reward potential"
	)
	default boolean showDamageDealt()
	{
		return true;
	}

	@ConfigItem(
			position = 3,
			keyName = "ToolSeed",
			name = "Show chance of getting Tool Seed",
			description = "Shows the % chance to obtain a tool seed with your participation. Assuming 3 down kills"
	)
	default boolean showToolSeedCalculations()
	{
		return true;
	}

}
