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
import net.runelite.client.ui.overlay.components.PanelComponent;

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

	private List<Player> playersInSight;

	@Getter
	private List<Player> playersParticipating = new ArrayList<>();

	private final List<WorldPoint> excludedWorldPoints = new ArrayList<>();

	@Getter
	private int shieldDamageDealt;

	@Getter
	private final int minimumDamageRewardShield = 30;

	@Getter
	private int miningDamageDealt;

	@Getter
	private final int minimumDamageRewardMining = 30;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Zalcano plugin started");
		overlayManager.add(zalcanoOverlay);

		addExcludedWorldPoints();

		playersParticipating = new ArrayList<>();
		playersInSight = new ArrayList<>(client.getPlayers());
		// Clear null values
		while (true) if (!playersInSight.remove(null)) break;

		shieldDamageDealt = 0;
		miningDamageDealt = 0;
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(zalcanoOverlay);
		log.info("Zalcano plugin stopped");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			playersInSight = new ArrayList<>(client.getPlayers());
			// Clear null values
			while (true) if (!playersInSight.remove(null)) break;
		}
	}

	@Subscribe
	public void onPlayerDespawned(PlayerDespawned playerDespawned)
	{
		playersParticipating.remove(playerDespawned.getPlayer());
		playersInSight.remove(playerDespawned.getPlayer());
	}

	@Subscribe
	public void onPlayerSpawned(PlayerSpawned playerSpawned)
	{
		playersInSight.add(playerSpawned.getPlayer());
	}

	@Subscribe
	public void onGameTick(GameTick gameTick) {
		filterPlayersAtGate(playersInSight);
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		NPC npc = npcSpawned.getNpc();
		if (npc.getId() == ZalcanoStates.THROWING || npc.getId() == ZalcanoStates.MINING) updateZalcano(npc);
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned) {
		NPC npc = npcDespawned.getNpc();
		if (npc.isDead() && npc.getId() == ZalcanoStates.MINING) {
			// TODO: use Zalcano states to display these statistics after the kill is over
			shieldDamageDealt = 0;
			miningDamageDealt = 0;
		}
	}


	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied) {
		if (hitsplatApplied.getHitsplat().isMine()) {
			if (hitsplatApplied.getActor() instanceof NPC) {
				NPC npc = (NPC) hitsplatApplied.getActor();
				if (npc.getId() == ZalcanoStates.THROWING) {
					shieldDamageDealt += hitsplatApplied.getHitsplat().getAmount();
				} else if (npc.getId() == ZalcanoStates.MINING) {
					miningDamageDealt += hitsplatApplied.getHitsplat().getAmount();
				}
				updateZalcano(npc);
			}
		}
	}

	private void updateZalcano(NPC zalcano) {
		// Standing Zalcano
		if (zalcano.getId() == ZalcanoStates.THROWING) {
			// client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Standing Zalcano Found, Healthscale: " + zalcano.getHealthScale() + ", Healthratio: " + zalcano.getHealthRatio(), null);
		}
		// Sitting Zalcano
		else if (zalcano.getId() == ZalcanoStates.MINING) {
			// client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Sitting Zalcano Found, Healthscale: " + zalcano.getHealthScale() + ", Healthratio: " + zalcano.getHealthRatio(), null);
		}
	}

	private void filterPlayersAtGate(List<Player> players) {


		for(Player p : players) {
			WorldPoint playerLocation = p.getWorldLocation();
			if (excludedWorldPoints.contains(playerLocation)) {
				playersParticipating.remove(p);
			} else {
				if (!playersParticipating.contains(p)) playersParticipating.add(p);
			}
		}
	}

	private void addExcludedWorldPoints() {
		int plane = 0;
		int minExcludedX = 3033;
		int maxExcludedX = 3034;

		int minExcludedY = 6063;
		int maxExcludedY = 6065;

		for(int x = minExcludedX; x <= maxExcludedX; x++) {
			for(int y =minExcludedY; y <= maxExcludedY; y++) {
				excludedWorldPoints.add(new WorldPoint(x, y, plane));
			}
		}
	}

	@Provides
	ZalcanoConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ZalcanoConfig.class);
	}
}
