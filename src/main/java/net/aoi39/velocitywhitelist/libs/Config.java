package net.aoi39.velocitywhitelist.libs;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.plugin.Plugin;
import net.aoi39.velocitywhitelist.VelocityWhitelist;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private final VelocityWhitelist plugin;
    private final Path dataDirectory;

    public static boolean enableWhitelist;
    public static String kickMessage;
    public static String kickMessageColor;
    public static boolean notifyInGameNonWhitelistPlayerJoin;

    public Config(VelocityWhitelist plugin, Path dataDirectory) {
        this.plugin = plugin;
        this.dataDirectory = dataDirectory;
        loadConfig();
    }

    private void loadConfig() {
        File configFile = dataDirectory.resolve("velocity-whitelist.toml").toFile();
        if (!configFile.exists()) {
            try {
                Files.createDirectories(dataDirectory);
                Files.copy(VelocityWhitelist.class.getResourceAsStream("/velocity-whitelist.toml"), dataDirectory.resolve("velocity-whitelist.toml"));
                VelocityWhitelist.getLogger().info("Success generate config file");
            } catch (Exception e) {
                VelocityWhitelist.getLogger().error("Failed to generate config file\n{}", e.getMessage());
                System.exit(1);
            }
        }
        Toml config = new Toml().read(configFile);
        updateConfig(config.getString("System.configVersion"));
        VelocityWhitelist.getLogger().info("Loading config...");
        enableWhitelist = config.getBoolean("General.enableWhitelist");
        kickMessage = config.getString("General.kickMessage");
        kickMessageColor = config.getString("General.kickMessageColor");
        notifyInGameNonWhitelistPlayerJoin = config.getBoolean("Logs.notifyInGameNonWhitelistPlayerJoin");
        VelocityWhitelist.getLogger().info("Success config load!");
    }

    private void updateConfig(String configVersion) {
        if (!configVersion.equals(plugin.getClass().getAnnotation(Plugin.class).version())) {
            try {
                Files.move(dataDirectory.resolve("velocity-whitelist.toml"), dataDirectory.resolve("velocity-whitelist-" + configVersion + ".toml"));
                VelocityWhitelist.getLogger().info("Regenerated due to different versions of config and plugin(The original config has been renamed to velocity-whitelist-{}.toml)", plugin.getClass().getAnnotation(Plugin.class).version());
                loadConfig();
            } catch (Exception e) {
                VelocityWhitelist.getLogger().error("Failed to update config file\n{}", e.getMessage());
            }
        }
    }

}
