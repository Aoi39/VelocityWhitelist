package net.aoi39.velocitywhitelist.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.plugin.Plugin;
import net.aoi39.velocitywhitelist.VelocityWhitelist;
import net.aoi39.velocitywhitelist.libs.Config;
import net.aoi39.velocitywhitelist.libs.WhitelistManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class WhitelistCommand {

    public WhitelistCommand(VelocityWhitelist plugin, Path dataDirectory) {
        plugin.getServer().getCommandManager().register(
                plugin.getServer().getCommandManager().metaBuilder("velocitywhitelist")
                        .aliases("velowhitelist", "vwhitelist", "vw")
                        .build(), new SimpleCommand() {
                    @Override
                    public void execute(Invocation invocation) {
                        if (invocation.arguments().length < 1) {
                            invocation.source().sendMessage(VelocityWhitelist.chatPrefix.append(Component.text("Running VelocityWhitelist v" + plugin.getClass().getAnnotation(Plugin.class).version() + ".").color(TextColor.color(255, 255, 255))));
                            invocation.source().sendMessage(VelocityWhitelist.chatPrefix.append(Component.text("Use /velocitywhitelist help to view available commands.").color(TextColor.color(255, 255, 255))));
                            plugin.getServer().getScheduler().buildTask(plugin, () -> {
                                invocation.source().sendMessage(VelocityWhitelist.chatPrefix.append(Component.text("If you need help or want to contact me, please join my ").color(TextColor.color(255, 255, 255)).append(Component.text("Discord Server").color(TextColor.color(0, 255, 0)).clickEvent(ClickEvent.openUrl("https://discord.gg/yefxV4839M")))));
                            }).delay(3, TimeUnit.SECONDS).schedule();
                            return;
                        }

                        if (invocation.arguments()[0].equals("help")) {
                            invocation.source().sendMessage(VelocityWhitelist.chatPrefix.append(Component.text(
                                    "Available commands\n" +
                                    "/velocitywhitelist help\n" +
                                    "/velocitywhitelist on\n" +
                                    "/velocitywhitelist off\n" +
                                    "/velocitywhitelist add <PlayerName>\n" +
                                    "/velocitywhitelist remove <PlayerName>\n" +
                                    "/velocitywhitelist list\n" +
                                    "/velocitywhitelist reload"
                            ).color(TextColor.color(255, 255, 255))));
                            return;
                        }

                        if (invocation.arguments()[0].equals("list")) {
                            invocation.source().sendMessage(VelocityWhitelist.chatPrefix.append(Component.text(WhitelistManager.whitelistUserNames.size() + " players are in the whitelist. " + String.join(", ", WhitelistManager.whitelistUserNames)).color(TextColor.color(255, 255, 255))));
                            return;
                        }

                        if (!invocation.source().hasPermission("velocitywhitelist")) {
                            return;
                        }

                        if (invocation.arguments()[0].equals("on")) {
                            if (Config.enableWhitelist) {
                                invocation.source().sendMessage(VelocityWhitelist.chatPrefix.append(Component.text("Whitelist is already enable").color(TextColor.color(255, 255, 255))));
                                return;
                            }
                            Config.enableWhitelist = true;
                            invocation.source().sendMessage(VelocityWhitelist.chatPrefix.append(Component.text("Whitelist enabled(After rebooting, config file settings will be used)").color(TextColor.color(255, 255, 255))));
                            return;
                        }

                        if (invocation.arguments()[0].equals("off")) {
                            if (!Config.enableWhitelist) {
                                invocation.source().sendMessage(VelocityWhitelist.chatPrefix.append(Component.text("Whitelist is already disable").color(TextColor.color(255, 255, 255))));
                                return;
                            }
                            Config.enableWhitelist = false;
                            invocation.source().sendMessage(VelocityWhitelist.chatPrefix.append(Component.text("Whitelist disable(After rebooting, config file settings will be used)").color(TextColor.color(255, 255, 255))));
                            return;
                        }

                        if (invocation.arguments()[0].equals("reload")) {
                            WhitelistManager.loadWhitelist(dataDirectory);
                            invocation.source().sendMessage(VelocityWhitelist.chatPrefix.append(Component.text("Whitelist was reloaded.").color(TextColor.color(255, 255, 255))));
                            return;
                        }

                        if (invocation.arguments().length < 2) {
                            return;
                        }

                        if (invocation.arguments()[0].equals("add")) {
                            CompletableFuture.runAsync(() -> {
                                String message;
                                int response = WhitelistManager.addWhitelist(invocation.arguments()[1]);
                                if (response == 200) {
                                    message = "Added " + invocation.arguments()[1] + " to whitelist.";
                                } else if (response == 404) {
                                    message = invocation.arguments()[1] + " was not found";
                                } else if (response == 409) {
                                    message = invocation.arguments()[1] + " has already been added to whitelist.";
                                } else {
                                    message = "Failed to add " + invocation.arguments()[1] + " to whitelist";
                                }
                                invocation.source().sendMessage(VelocityWhitelist.chatPrefix.append(Component.text(message).color(TextColor.color(255, 255, 255))));
                            });
                            return;
                        }

                        if (invocation.arguments()[0].equals("remove")) {
                            CompletableFuture.runAsync(() -> {
                                String message;
                                int response = WhitelistManager.removeWhitelist(invocation.arguments()[1]);
                                if (response == 200) {
                                    message = "Removed " + invocation.arguments()[1] + " from whitelist";
                                } else if (response == 404) {
                                    message = invocation.arguments()[1] + " is not included in whitelist";
                                } else {
                                    message = "Failed to remove" + invocation.arguments()[1] + " from whitelist";
                                }
                                invocation.source().sendMessage(VelocityWhitelist.chatPrefix.append(Component.text(message).color(TextColor.color(255, 255, 255))));
                            });
                            return;
                        }
                    }

                    @Override
                    public List<String> suggest(Invocation invocation) {
                        if (invocation.arguments().length == 0) {
                            return Arrays.asList("help", "on", "off", "add", "remove", "list", "reload");
                        }

                        return new ArrayList<>();
                    }
                });
    }

}
