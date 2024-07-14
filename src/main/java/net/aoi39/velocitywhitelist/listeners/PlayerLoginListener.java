package net.aoi39.velocitywhitelist.listeners;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.aoi39.velocitywhitelist.VelocityWhitelist;
import net.aoi39.velocitywhitelist.libs.Config;
import net.aoi39.velocitywhitelist.libs.WhitelistManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.nio.file.Path;

public class PlayerLoginListener {
    private final VelocityWhitelist plugin;
    private final Path dataDirectory;

    public PlayerLoginListener(VelocityWhitelist plugin, Path dataDirectory) {
        this.plugin = plugin;
        this.dataDirectory = dataDirectory;
        plugin.getServer().getEventManager().register(plugin, this);
    }

    @Subscribe
    private void onLogin(LoginEvent event) {
        if (Config.enableWhitelist) {
            if (!WhitelistManager.whitelistUniqueIds.contains(String.valueOf(event.getPlayer().getUniqueId()))) {
                event.setResult(ResultedEvent.ComponentResult.denied(Component.text(Config.kickMessage).color(TextColor.fromHexString(Config.kickMessageColor))));
                if (Config.notifyInGameNonWhitelistPlayerJoin) {
                    for (RegisteredServer server : plugin.getServer().getAllServers()) {
                        server.sendMessage(VelocityWhitelist.chatPrefix.append(Component.text("Non whitelisted player " + event.getPlayer().getUsername() + " attempted to join the server").color(TextColor.color(255, 255, 0))));
                    }
                }
                return;
            }
        }
        WhitelistManager.updateWhitelistPlayerName(event.getPlayer());
    }

}
