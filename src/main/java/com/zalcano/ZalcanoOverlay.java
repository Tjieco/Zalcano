package com.zalcano;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;


public class ZalcanoOverlay extends OverlayPanel {
    private final Client client;
    private final ZalcanoPlugin plugin;
    private final ZalcanoConfig config;

    @Inject
    private ZalcanoOverlay(Client client, ZalcanoPlugin plugin, ZalcanoConfig config)
    {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.LOW);

        this.client = client;
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics2D) {
        if (!shouldShowOverlay()) return null;
        showTitle();
        if (config.showPlayerCount()) showPlayerCount();
        if (config.showHealth()) showHealth();
        if (config.showDamageDealt()) showDamageDealt();
        if (config.showToolSeedCalculations()) showToolSeedCalculations();
        return super.render(graphics2D);
    }

    private boolean shouldShowOverlay() {
        return plugin.playerInZalcanoArea();
    }

    private void showTitle() {
        panelComponent.getChildren().add(TitleComponent.builder().text("Zalcano").build());
    }

    private void showPlayerCount() {
        int playercount = plugin.getPlayersParticipating().size();
        panelComponent.getChildren().add(LineComponent.builder().left("Players: " + playercount).build());
    }

    private void showHealth() {
        String miningHp = plugin.getMiningHp() == null ? "Unknown" : plugin.getMiningHp();
        panelComponent.getChildren().add(LineComponent.builder().left("Mining HP:  " + miningHp).build());
        String throwingHp = plugin.getThrowingHp() == null ? "Unknown" : plugin.getThrowingHp();
        if (plugin.getZalcanoState() == ZalcanoStates.THROWING) panelComponent.getChildren().add(LineComponent.builder().left("Throwing HP:  " + throwingHp).build());
    }
    private void showDamageDealt() {
        Color color = decideColorBasedOnThreshold(plugin.getShieldDamageDealt(), plugin.getMinimumDamageRewardShield());
        panelComponent.getChildren().add(LineComponent.builder().left("Shield Damage dealt: " + plugin.getShieldDamageDealt() + " / " + plugin.getMinimumDamageRewardShield()).leftColor(color).build());
        color = decideColorBasedOnThreshold(plugin.getMiningDamageDealt(), plugin.getMinimumDamageRewardMining());
        panelComponent.getChildren().add(LineComponent.builder().left("Mining Damage dealt: " + plugin.getMiningDamageDealt() + " / " + plugin.getMinimumDamageRewardMining()).leftColor(color).build());
    }

    private Color decideColorBasedOnThreshold(int damage, int threshold) {
        if (damage >= threshold) {
            return Color.GREEN;
        } else if (damage > 0) {
            return Color.YELLOW;
        } else {
            return Color.RED;
        }
    }

    private void showToolSeedCalculations() {
        panelComponent.getChildren().add(LineComponent.builder().left("Chance of tool seed: " + String.format("%.3g", plugin.getChanceOfToolSeedTable() * 100) + "%").build());
    }
}
