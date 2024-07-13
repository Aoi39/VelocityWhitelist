package net.aoi39.velocitywhitelist;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.aoi39.velocitywhitelist.commands.WhitelistCommand;
import net.aoi39.velocitywhitelist.libs.Config;
import net.aoi39.velocitywhitelist.libs.WhitelistManager;
import net.aoi39.velocitywhitelist.listeners.PlayerLoginListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

@Plugin(
        id = "velocitywhitelist",
        name = "VelocityWhitelist",
        version = "1.0.0",
        description = "Set up whitelist on the Velocity side",
        url = "https://github.com/Aoi39/VelocityWhitelist",
        authors = { "Aoi39" }
)
public class VelocityWhitelist {
    private static final Logger logger = LoggerFactory.getLogger("VelocityWhitelist");
    private final ProxyServer server;
    private final Path dataDirectory;
    public static final Component chatPrefix = Component.text("[VelocityWhitelist] ").color(TextColor.color(0, 255, 0));

    public static Logger getLogger() {
        return logger;
    }

    public ProxyServer getServer() {
        return server;
    }

    @Inject
    public VelocityWhitelist(ProxyServer server, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.dataDirectory = dataDirectory;
        new Config(this, dataDirectory);
        WhitelistManager.loadWhitelist(dataDirectory);
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        logger.info("VelocityWhitelist initialized!");
        new PlayerLoginListener(this, dataDirectory);
        new WhitelistCommand(this, dataDirectory);
    }

}
