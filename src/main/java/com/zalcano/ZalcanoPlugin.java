package com.zalcano;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.components.PanelComponent;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

	private Widget hpBar;
	private boolean hpBarHidden = true;

	@Getter
	private int zalcanoState;
	@Getter
	private String throwingHp;
	@Getter
	private String miningHp;

	private List<Player> playersInSight;
	@Getter
	private List<Player> playersParticipating = new ArrayList<>();

	private final List<WorldPoint> excludedWorldPoints = new ArrayList<>();
	private static final int ZALCANO_REGION = 12126;

	@Getter
	private int shieldDamageDealt;
	@Getter
	private final int minimumDamageRewardShield = 30;

	@Getter
	private int miningDamageDealt;
	@Getter
	private final int minimumDamageRewardMining = 30;

	@Getter
	private float chanceOfToolSeedTable = 0;

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
		zalcanoState = ZalcanoStates.THROWING;

	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(zalcanoOverlay);
		log.info("Zalcano plugin stopped");
	}

	@Subscribe
	public void onWidgetHiddenChanged(WidgetHiddenChanged widgetHiddenChanged) {
		Widget widget = widgetHiddenChanged.getWidget();
		if (widget.getId() == 19857428) {
			hpBarHidden = widget.isHidden();
			if (!hpBarHidden) hpBar = widget;
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
		updateZalcanoStatus();
		updateZalcanoHealth();
	}

	private void updateZalcanoStatus() {
		for (NPC npc: client.getNpcs()) {
			if (npc.getId() == ZalcanoStates.THROWING) {
				if (zalcanoState == ZalcanoStates.MINING) {
					// Reset shield hp after transition from mining to throwing
					throwingHp = "300 / 300";
				}
				zalcanoState = npc.getId();
			}
			if (npc.getId() == ZalcanoStates.MINING) {
				zalcanoState = npc.getId();
			}
		}
	}

	private void updateZalcanoHealth() {
		if (!hpBarHidden) {
			if (zalcanoState == ZalcanoStates.THROWING) {
				throwingHp = hpBar.getText();
			} else if (zalcanoState == ZalcanoStates.MINING) {
				miningHp = hpBar.getText();
			}
		}
		else {
			for (NPC npc : client.getNpcs()) {
				int healthRatio = npc.getHealthRatio();
				if (healthRatio >= 0) {
					if (npc.getId() == ZalcanoStates.THROWING) {
						throwingHp = healthRatio * 3 + " / 300";
					} else if (npc.getId() == ZalcanoStates.MINING) {
						miningHp = healthRatio * 10 + " / 1000";
					}
				}
			}
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		NPC npc = npcSpawned.getNpc();
		if (npc.getId() == ZalcanoStates.THROWING || npc.getId() == ZalcanoStates.MINING) {
			if (zalcanoState == ZalcanoStates.DEAD) {
				shieldDamageDealt = 0;
				miningDamageDealt = 0;
				miningHp = "1000 / 1000";
				throwingHp = "300 / 300";
				chanceOfToolSeedTable = 0;
			}
			zalcanoState = npc.getId();
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned) {
		NPC npc = npcDespawned.getNpc();
		if (npc.isDead() && npc.getId() == ZalcanoStates.MINING) {
			zalcanoState = ZalcanoStates.DEAD;
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
				calculateToolSeedTableChances();
			}
		}
	}

	private void calculateToolSeedTableChances() {
		int cap = 1000;
		float points =  miningDamageDealt + (shieldDamageDealt * 2);
		if (points > cap) points = cap;
		float contribution = points / 2800;
		chanceOfToolSeedTable = contribution / 200;
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

	public boolean playerInZalcanoArea() {
		return Objects.requireNonNull(client.getLocalPlayer()).getWorldLocation().getRegionID() == ZALCANO_REGION;
	}
}
