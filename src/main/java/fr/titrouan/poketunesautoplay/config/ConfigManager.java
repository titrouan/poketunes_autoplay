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

    private static final int DEFAULT_FADEIN = 5; //100 ticks
    private static final int DEFAULT_FADEOUT = 5; //100 ticks
    private static final int DEFAULT_MINDELAY = 55; //1100 ticks
    private static final int DEFAULT_MAXDELAY = 65; //1300 ticks
    public static int fadeIn = DEFAULT_FADEIN;
    public static int fadeOut = DEFAULT_FADEOUT;
    public static int minDelay = DEFAULT_MINDELAY;
    public static int maxDelay = DEFAULT_MAXDELAY;

    public static void loadConfig() {
        if (!CONFIG_FILE.exists()) {
            saveDefaultConfig();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            fadeIn = json.has("fadeIn") ? json.get("fadeIn").getAsInt() * 20 : DEFAULT_FADEIN * 20;
            fadeOut = json.has("fadeOut") ? json.get("fadeOut").getAsInt() * 20 : DEFAULT_FADEOUT * 20;
            minDelay = json.has("minDelay") ? json.get("minDelay").getAsInt() * 20 : DEFAULT_MINDELAY * 20;
            maxDelay = json.has("maxDelay") ? json.get("maxDelay").getAsInt() * 20 : DEFAULT_MAXDELAY * 20;
        } catch (IOException e) {
            System.out.println(LangHelper.get("log.config.error.play"));
        }
    }

    public static void saveConfig() {
        if (!CONFIG_DIR.exists() && !CONFIG_DIR.mkdirs()) {
            System.err.println(LangHelper.get("log.config.error.create.configfolder"));
            return;
        }

        JsonObject json = new JsonObject();
        json.addProperty("fadeIn", fadeIn / 20);
        json.addProperty("fadeOut", fadeOut / 20);
        json.addProperty("minDelay", minDelay / 20);
        json.addProperty("maxDelay", maxDelay / 20);

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(json, writer);
        } catch (IOException e) {
            System.err.println(LangHelper.get("log.config.error.write.configfile"));
        }
    }

    public static void saveDefaultConfig() {
        if (!CONFIG_DIR.exists() && !CONFIG_DIR.mkdirs()) {
            System.err.println(LangHelper.get("log.config.error.create.configfolder"));
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
            System.err.println(LangHelper.get("log.config.error.write.configfile"));
        }

        saveInfoFile();
    }

    private static void saveInfoFile() {
        String infoContent = "Configuration de PokeTunes AutoPlay / PokeTunes AutoPlay configuration\n" +
                "-------------------------------------\n" +
                "fadeIn : Durée du fondu d'entrée en secondes (défaut : 5s = 100 ticks) / Fade-in duration in seconds (default : 5s = 100 ticks)\n" +
                "fadeOut : Durée du fondu de sortie en secondes (défaut : 5s = 100 ticks) / Fade-in duration in seconds (default : 5s = 100 ticks)\n" +
                "minDelay : Temps minimal entre deux musiques en secondes (défaut : 55s = 1100 ticks) / Minimal time between two musics in seconds (default : 55s = 1100 ticks)\n" +
                "maxDelay : Temps maximal entre deux musiques en secondes (défaut : 65s = 1300 ticks) / Maximal time between two musics in seconds (default : 65s = 1300 ticks)\n" +
                "\nLes temps en secondes doivent correspondre à des entiers en ticks, les décimales ne fonctionnent pas (ex : 55s c'est bon, 55.30s c'est pas bon).\n/The times in seconds must correspond to integers in ticks, decimals do not work (ex: 55s is good, 55.30s is not good).\n" +
                "Modifiez config.json pour changer ces valeurs. / Modify config.json to change those values.\n";

        try (FileWriter writer = new FileWriter(INFO_FILE)) {
            writer.write(infoContent);
        } catch (IOException e) {
            System.err.println(LangHelper.get("log.config.error.write.infofile"));
        }
    }
}