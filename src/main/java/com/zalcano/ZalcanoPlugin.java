package com.zalcano;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@PluginDescriptor(
	name = "Zalcano"
)
public class ZalcanoPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ZalcanoConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ZalcanoOverlay zalcanoOverlay;

	private int playercount = 0;
	private int minExcludedX = 3033;
	private int maxExcludedX = 3034;

	private int minExcludedY = 6063;
	private int maxExcludedY = 6065;

	private int plane = 0;

	@Getter
	private List<Player> playersInSight;
	private List<WorldPoint> excludedWorldPoints = new ArrayList<>();


	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(zalcanoOverlay);


		for(int x = minExcludedX; x <= maxExcludedX; x++) {
			for(int y =minExcludedY; y <= maxExcludedY; y++) {
				excludedWorldPoints.add(new WorldPoint(x, y, plane));
			}
		}
		log.info("Zalcano plugin started");
		playercount = 0;
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(zalcanoOverlay);
		log.info("Zalcano plugin stopped");
		playercount = 0;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			playersInSight = new ArrayList<>(client.getPlayers());
		}
	}

	@Subscribe
	public void onPlayerDespawned(PlayerDespawned playerDespawned)
	{
		Player player = playerDespawned.getPlayer();
		playersInSight = new ArrayList<>(client.getPlayers());
		playersInSight.remove(player);

		filterPlayersAtGate(playersInSight);

		playercount = playersInSight.size();
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", String.format("player: %s has despawned at position: %s, %s Plane: %s",
				player.getName(),
				player.getWorldLocation().getX(),
				player.getWorldLocation().getY(),
				player.getWorldLocation().getPlane()
		), null);client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "new playercount: " + playercount, null);
	}

	@Subscribe
	public void onPlayerSpawned(PlayerSpawned playerSpawned)
	{
		Player player = playerSpawned.getPlayer();
		playersInSight = new ArrayList<>(client.getPlayers());
		filterPlayersAtGate(playersInSight);
		playercount = playersInSight.size();
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", String.format("player: %s has spawned at position: %s, %s Plane: %s",
				player.getName(),
				player.getWorldLocation().getX(),
				player.getWorldLocation().getY(),
				player.getWorldLocation().getPlane()
		), null);
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "new playercount: " + playercount, null);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		int id = widgetLoaded.getGroupId();
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "widget has been opened " + id, null);
	}

	private void filterPlayersAtGate(List<Player> players) {
		while (true) {
			if (!playersInSight.remove(null)) break;
		}
		List<Player> playersToRemove = new ArrayList<>();
		for(Player p : players) {
			WorldPoint playerLocation = p.getWorldLocation();
			if (excludedWorldPoints.contains(playerLocation)) {
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", String.format("player: %s is on an excluded tile", p.getName()), null);
				playersToRemove.add(p);
			}
		}
		for(Player p : playersToRemove) {
			players.remove(p);
		}
		String playerstring = " ";
		for(Player p : playersInSight) {
			playerstring += p.getName() + ", ";
		}
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", String.format("players: %s (%s)", playerstring, playersInSight.size()), null);
	}

	@Provides
	ZalcanoConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ZalcanoConfig.class);
	}

	public int getPlayerCount() {
		if (getPlayersInSight() == null) {
			return 0;
		} else {
			return getPlayersInSight().size();
		}
	}
}
