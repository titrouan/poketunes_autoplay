package fr.titrouan.poketunesautoplay.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.translatable("poketunes.config.title"));

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            ConfigCategory category = builder.getOrCreateCategory(Text.translatable("poketunes.config.category.general"));

            // fadeIn
            category.addEntry(entryBuilder
                    .startIntField(Text.translatable("poketunes.config.fadein"), ConfigManager.fadeIn / 20)
                    .setTooltip(Text.translatable("poketunes.config.fadein.tooltip"))
                    .setDefaultValue(5)
                    .setMin(1)
                    .setSaveConsumer(newValue -> ConfigManager.fadeIn = newValue * 20)
                    .build());

            // fadeOut
            category.addEntry(entryBuilder
                    .startIntField(Text.translatable("poketunes.config.fadeout"), ConfigManager.fadeOut / 20)
                    .setTooltip(Text.translatable("poketunes.config.fadeout.tooltip"))
                    .setDefaultValue(5)
                    .setMin(1)
                    .setSaveConsumer(newValue -> ConfigManager.fadeOut = newValue * 20)
                    .build());

            // minDelay
            category.addEntry(entryBuilder
                    .startIntField(Text.translatable("poketunes.config.mindelay"), ConfigManager.minDelay / 20)
                    .setTooltip(Text.translatable("poketunes.config.mindelay.tooltip"))
                    .setDefaultValue(55)
                    .setMin(10)
                    .setSaveConsumer(newValue -> ConfigManager.minDelay = newValue * 20)
                    .build());

            // maxDelay
            category.addEntry(entryBuilder
                    .startIntField(Text.translatable("poketunes.config.maxdelay"), ConfigManager.maxDelay / 20)
                    .setTooltip(Text.translatable("poketunes.config.maxdelay.tooltip"))
                    .setDefaultValue(65)
                    .setMin(20)
                    .setSaveConsumer(newValue -> ConfigManager.maxDelay = newValue * 20)
                    .build());

            // recharge le fichier de config avec valeurs en secondes
            // reload config file with values in seconds
            builder.setSavingRunnable(ConfigManager::saveConfig);
            return builder.build();
        };
    }
}