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

public class PlayerLoginListener {
    private final VelocityWhitelist plugin;

    public PlayerLoginListener(VelocityWhitelist plugin) {
        this.plugin = plugin;
        plugin.getServer().getEventManager().register(plugin, this);
    }

    @Subscribe
    private void onLogin(LoginEvent event) {
        if (Config.enableWhitelist) {
            boolean contains;
            if (Config.useUUIDForChecking) {
                contains = !WhitelistManager.whitelistUniqueIds.contains(String.valueOf(event.getPlayer().getUniqueId()));
            } else {
                contains = !WhitelistManager.whitelistUserNames.contains(String.valueOf(event.getPlayer().getUsername()));
            }
            if (contains) {
                event.setResult(ResultedEvent.ComponentResult.denied(Component.text(Config.kickMessage).color(TextColor.fromHexString(Config.kickMessageColor))));
                if (Config.notifyInGameNonWhitelistPlayerJoin) {
                    for (RegisteredServer server : plugin.getServer().getAllServers()) {
                        server.sendMessage(VelocityWhitelist.chatPrefix.append(Component.text("Non whitelisted player " + event.getPlayer().getUsername() + " attempted to join the server").color(TextColor.color(255, 255, 0))));
                    }
                }
                WhitelistManager.updateWhitelistPlayerName(event.getPlayer());
            }
        }
    }

}
