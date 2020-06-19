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
        makeTitle();
        makePlayerCount();
        return super.render(graphics2D);
    }

    private void makeTitle() {
        panelComponent.getChildren().add(TitleComponent.builder().text("Zalcano plugin").build());
    }

    private void makePlayerCount() {
        int playercount = plugin.getPlayerCount();
        panelComponent.getChildren().add(LineComponent.builder().left("Players: " + playercount).build());
    }
}
