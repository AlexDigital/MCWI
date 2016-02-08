package de.alexdigital.mcwi.util;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public abstract class YamlConfig {

    @Getter(AccessLevel.PROTECTED)
    private FileConfiguration config;

    private File file;

    public YamlConfig(String name) {
        File folder = new File("plugins/MCWI");
        this.file = new File(folder, name + ".yml");

        folder.mkdirs();
        if (!this.file.exists()) {
            try {
                this.file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    public void save() {
        try {
            this.config.save(this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
