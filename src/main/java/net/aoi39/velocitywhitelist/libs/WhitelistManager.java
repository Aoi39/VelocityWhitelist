package net.aoi39.velocitywhitelist.libs;

import com.google.gson.*;
import com.velocitypowered.api.proxy.Player;
import net.aoi39.velocitywhitelist.VelocityWhitelist;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WhitelistManager {
    public static File whitelistFile;
    public static JsonArray whitelist = new JsonArray();
    public static List<String> whitelistUserNames = new ArrayList<>();
    public static List<String> whitelistUniqueIds = new ArrayList<>();

    public static void loadWhitelist(Path dataDirectory) {
        CompletableFuture.runAsync(() -> {
            whitelistFile = dataDirectory.resolve("whitelist.json").toFile();
            if (!whitelistFile.exists()) {
                try {
                    Files.copy(VelocityWhitelist.class.getResourceAsStream("/whitelist.json"), dataDirectory.resolve("whitelist.json"));
                    VelocityWhitelist.getLogger().info("Success generate whitelist.json");
                } catch (Exception e) {
                    VelocityWhitelist.getLogger().info("Failed to generate whitelist.json\n{}", e.getMessage());
                    System.exit(1);
                }
            }
            try (FileReader reader = new FileReader(whitelistFile, StandardCharsets.UTF_8)) {
                whitelist = new Gson().fromJson(reader, JsonArray.class);
                refreshWhitelistValuesCache();
            } catch (Exception e) {
                VelocityWhitelist.getLogger().error("Failed to load whitelist\n{}", e.getMessage());
            }
        });
    }

    private static void saveWhitelist() {
        CompletableFuture.runAsync(() -> {
           try (FileWriter writer = new FileWriter(whitelistFile)) {
               writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(whitelist));
            }  catch (Exception e) {
               VelocityWhitelist.getLogger().error("Failed to write whitelist\n{}", e.getMessage());
           }
        });
    }

    private static void refreshWhitelistValuesCache() {
        whitelistUserNames.clear();
        whitelistUniqueIds.clear();
        for (JsonElement element : whitelist) {
            if (element.isJsonObject()) {
                JsonObject playerData = element.getAsJsonObject();
                whitelistUserNames.add(playerData.get("name").getAsString());
                whitelistUniqueIds.add(playerData.get("uuid").getAsString());
            }
        }
    }

    public static int addWhitelist(String playerName) {
        try {
            if (whitelistUserNames.contains(playerName)) {
                return 409;
            }
            HttpURLConnection connection = (HttpURLConnection) new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName).openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() == 200) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuffer buffer = new StringBuffer();
                    for (String line; (line = reader.readLine()) != null; ) {
                        buffer.append(line);
                    }
                    JsonObject response = new Gson().fromJson(String.valueOf(buffer), JsonObject.class);
                    String uuid = String.valueOf(new StringBuilder(response.get("id").getAsString()).insert(20, "-").insert(16, "-").insert(12, "-").insert(8, "-"));
                    if (connection.getResponseCode() == 200) {
                        if (whitelistUniqueIds.contains(uuid)) {
                            connection.disconnect();
                            return 409;
                        }
                        JsonObject playerData = new JsonObject();
                        playerData.addProperty("uuid", uuid);
                        playerData.addProperty("name", response.get("name").getAsString());
                        whitelist.add(playerData);
                        saveWhitelist();
                        refreshWhitelistValuesCache();
                        connection.disconnect();
                        return 200;
                    }
                }
                return 500;
            } else if (connection.getResponseCode() == 404) {
                connection.disconnect();
                return 404;
            } else {
                connection.disconnect();
                return 500;
            }
        } catch (Exception e) {
            VelocityWhitelist.getLogger().error("Failed to add player to whitelist\n{}", e.getMessage());
            return 500;
        }
    }

    public static int removeWhitelist(String playerName) {
        if (!whitelistUserNames.contains(playerName)) {
            return 404;
        }
        for (JsonElement element : whitelist) {
            if (element.isJsonObject()) {
                JsonObject playerData = element.getAsJsonObject();
                if (playerName.equals(playerData.get("name").getAsString())) {
                    whitelist.remove(playerData);
                    saveWhitelist();
                    refreshWhitelistValuesCache();
                    return 200;
                }
            }
        }
        return 500;
    }

    public static void updateWhitelistPlayerName(Player player) {
        if ( Config.useUUIDForChecking && !whitelistUserNames.contains(player.getUsername()) && whitelistUniqueIds.contains(String.valueOf(player.getUniqueId()))) {
            for (JsonElement element : whitelist) {
                if (element.isJsonObject()) {
                    JsonObject playerData = element.getAsJsonObject();
                    if (String.valueOf(player.getUniqueId()).equals(playerData.get("uuid").getAsString())) {
                        playerData.addProperty("name", player.getUsername());
                        saveWhitelist();
                        refreshWhitelistValuesCache();
                        return;
                    }
                }
            }
        }
    }

}
