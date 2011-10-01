package me.ahniolator.plugins.sunburn;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.util.config.Configuration;

public final class BurningConfig {

    private String dir;
    private String confDir = "sunburn.";
    private File configFile;
    public Configuration yml;
    private List<String> defaultWorlds = new ArrayList<String>();

    public BurningConfig(String dir) {
        this.dir = dir;
        this.configFile = new File(dir + "config.yml");
        System.out.println("[SunBurn] Attempting to load config file");
        load();
    }

    public void load() {
        if (!this.configFile.exists()) {
            try {
                System.out.println("[SunBurn] Could not find the config file! Making a new one.");
                new File(this.dir).mkdir();
                this.configFile.createNewFile();
                this.yml = new Configuration(this.configFile);
                setDefaults();
                System.out.println("[SunBurn] Config file loaded successfully!");
            } catch (Exception e) {
                System.out.println("[SunBurn] Could not read config file!");
                e.printStackTrace();
            }
        } else {
            try {
                this.yml = new Configuration(this.configFile);
                this.yml.load();
                System.out.println("[SunBurn] Config file loaded successfully!");
            } catch (Exception e) {
                System.out.println("[SunBurn] Could not read config file!");
                e.printStackTrace();
            }
        }
    }

    public void save() {
        System.out.println("[SunBurn] Saving Config.");
        this.yml.save();
    }

    public void reload() {
        System.out.println("[SunBurn] Reloading config.");
        try {
            this.yml.save();
            this.yml.load();
            System.out.println("[SunBurn] Reload complete.");
        } catch (Exception e) {
            System.out.println("[SunBurn] Could not read config file!");
            e.printStackTrace();
        }
    }

    public void setDefaults() {
        System.out.println("[SunBurn] Setting default config values");
        try {
            new File(this.dir).mkdir();
            this.configFile.delete();
            this.configFile.createNewFile();
            this.yml = new Configuration(this.configFile);
        } catch (Exception e) {
            System.out.println("[SunBurn] Could not read config file!");
            e.printStackTrace();
        }
        this.defaultWorlds.clear();
        this.defaultWorlds.add("world1");
        this.defaultWorlds.add("world2");
        this.defaultWorlds.add("world3");
        this.yml.setProperty(this.confDir + "enabled worlds", this.defaultWorlds);
        this.yml.setProperty(this.confDir + "burn.players", true);
        this.yml.setProperty(this.confDir + "burn.mobs", true);
        this.yml.setProperty(this.confDir + "protective armor.type", "gold");
        this.yml.setProperty(this.confDir + "protective armor.time", 5);
        this.yml.setProperty(this.confDir + "protective armor.damage", true);
        this.yml.setProperty(this.confDir + "protective armor.enable", true);
        this.yml.setProperty(this.confDir + "update.notifications", true);
        reload();
    }
}