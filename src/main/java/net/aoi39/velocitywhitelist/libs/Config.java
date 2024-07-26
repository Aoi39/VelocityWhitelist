package net.aoi39.velocitywhitelist.libs;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.plugin.Plugin;
import net.aoi39.velocitywhitelist.VelocityWhitelist;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class Config {
    private final VelocityWhitelist plugin;
    private final Path dataDirectory;

    public static boolean enableWhitelist;
    public static boolean useUUIDForChecking;
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
        updateConfigVersion(config.getString("System.configVersion"));
        VelocityWhitelist.getLogger().info("Loading config...");
        enableWhitelist = config.getBoolean("General.enableWhitelist");
        useUUIDForChecking = config.getBoolean("General.useUUIDForChecking");
        kickMessage = config.getString("General.kickMessage");
        kickMessageColor = config.getString("General.kickMessageColor");
        notifyInGameNonWhitelistPlayerJoin = config.getBoolean("Logs.notifyInGameNonWhitelistPlayerJoin");
        VelocityWhitelist.getLogger().info("Success config load!");
    }

    public static void saveConfig(Path dataDirectory) {
        File configFile = dataDirectory.resolve("velocity-whitelist.toml").toFile();
        if (!configFile.exists()) {
            VelocityWhitelist.getLogger().error("Config file not found.");
        } else {
            VelocityWhitelist.getLogger().info("Saving config...");
            try {
                List<String> configLines = Files.readAllLines(configFile.toPath());
                for (int i = 0; i < configLines.size(); i++) {
                    if (configLines.get(i).trim().startsWith("enableWhitelist=")) {
                        configLines.set(i, "    enableWhitelist=" + (enableWhitelist ? "true": "false"));
                    }
                }
                Files.write(Paths.get(configFile.getPath()), configLines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                VelocityWhitelist.getLogger().info("Completed save config");
            } catch (Exception e) {
                VelocityWhitelist.getLogger().error("Failed to save config file.");
            }
        }
    }

    private void updateConfigVersion(String configVersion) {
        if (!configVersion.equals(plugin.getClass().getAnnotation(Plugin.class).version())) {
            try {
                Files.move(dataDirectory.resolve("velocity-whitelist.toml"), dataDirectory.resolve("velocity-whitelist-" + configVersion + ".toml"));
                VelocityWhitelist.getLogger().info("Regenerated due to different versions of config and plugin(The original config has been renamed to velocity-whitelist-{}.toml)", configVersion);
                loadConfig();
            } catch (Exception e) {
                VelocityWhitelist.getLogger().error("Failed to update config file\n{}", e.getMessage());
            }
        }
    }

}