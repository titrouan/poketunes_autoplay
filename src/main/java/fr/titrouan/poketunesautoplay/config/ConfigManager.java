package fr.titrouan.poketunesautoplay.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_DIR = new File("config/PokeTunes_AutoPlay");
    private static final File CONFIG_FILE = new File(CONFIG_DIR, "config.json");
    private static final File INFO_FILE = new File(CONFIG_DIR, "config_info.txt");

    public static int fadeIn = 20;
    public static int fadeOut = 100;
    public static int minDelay = 1100;
    public static int maxDelay = 1300;

    public static void loadConfig() {
        if (!CONFIG_FILE.exists()) {
            saveDefaultConfig();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            fadeIn = json.has("fadeIn") ? json.get("fadeIn").getAsInt() : fadeIn;
            fadeOut = json.has("fadeOut") ? json.get("fadeOut").getAsInt() : fadeOut;
            minDelay = json.has("minDelay") ? json.get("minDelay").getAsInt() : minDelay;
            maxDelay = json.has("maxDelay") ? json.get("maxDelay").getAsInt() : maxDelay;
        } catch (IOException e) {
            System.err.println("[PokeTunes AutoPlay] Erreur de lecture du fichier de configuration");
            System.err.println("[PokeTunes AutoPlay] Error reading the configuration file");
        }
    }

    public static void saveDefaultConfig() {
        if (!CONFIG_DIR.exists() && !CONFIG_DIR.mkdirs()) {
            System.err.println("[PokeTunes AutoPlay] Impossible de créer le dossier de configuration");
            System.err.println("[PokeTunes AutoPlay] Unable to create the configuration folder");
            return;
        }

        JsonObject json = new JsonObject();
        json.addProperty("fadeIn", fadeIn);
        json.addProperty("fadeOut", fadeOut);
        json.addProperty("minDelay", minDelay);
        json.addProperty("maxDelay", maxDelay);

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(json, writer);
        } catch (IOException e) {
            System.err.println("[PokeTunes AutoPlay] Erreur d'écriture du fichier de configuration");
            System.err.println("[PokeTunes AutoPlay] Error writing the configuration file");
        }

        saveInfoFile();
    }

    private static void saveInfoFile() {
        String infoContent = "Configuration de PokeTunes AutoPlay / PokeTunes AutoPlay configuration\n" +
                "-------------------------------------\n" +
                "fadeIn : Durée du fondu d'entrée en ticks (défaut : 20 = 1s) / Fade-in duration in ticks (default : 20 = 1s)\n" +
                "fadeOut : Durée du fondu de sortie en  ticks (défaut : 100 = 5s) / Fade-out duration in ticks (default : 100 = 5s)\n" +
                "minDelay : Temps minimal entre deux musiques en ticks (défaut : 1100 = 55s) / Minimal time between two musics in ticks (default : 1100 = 55s)\n" +
                "maxDelay : Temps maximal entre deux musiques en ticks (défaut : 1300 = 65s) / Maximal time between two musics in ticks (default : 1300 = 65s)\n" +
                "\nLes temps en ticks doivent correspondre à des entiers en secondes, les décimales ne fonctionnent pas (ex : 55s c'est bon, 55.30s c'est pas bon).\n/The times in ticks must correspond to integers in seconds, decimals do not work (ex: 55s is good, 55.30s is not good).\n" +
                "Modifiez config.json pour changer ces valeurs. / Modify config.json to change those values.\n";

        try (FileWriter writer = new FileWriter(INFO_FILE)) {
            writer.write(infoContent);
        } catch (IOException e) {
            System.err.println("[PokeTunes AutoPlay] Erreur d'écriture du fichier d'informations");
            System.err.println("[PokeTunes AutoPlay] Error writing the information file");
        }
    }
}