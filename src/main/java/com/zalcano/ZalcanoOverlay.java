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
        showTitle();
        showPlayerCount();
        showDamageDealt();
        return super.render(graphics2D);
    }

    private void showTitle() {
        panelComponent.getChildren().add(TitleComponent.builder().text("Zalcano plugin").build());
    }

    private void showPlayerCount() {
        int playercount = plugin.getPlayersParticipating().size();
        panelComponent.getChildren().add(LineComponent.builder().left("Players: " + playercount).build());
    }

    private void showDamageDealt() {
        panelComponent.getChildren().add(LineComponent.builder().left("Shield Damage dealt: " + plugin.getShieldDamageDealt() + " / " + plugin.getMinimumDamageRewardShield()).build());
        panelComponent.getChildren().add(LineComponent.builder().left("Mining Damage dealt: " + plugin.getMiningDamageDealt() + " / " + plugin.getMinimumDamageRewardMining()).build());
    }
}
