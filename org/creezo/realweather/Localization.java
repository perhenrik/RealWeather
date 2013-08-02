package org.creezo.realweather;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;

/**
 *
 * @author Dodec
 */
public class Localization {
    public String Language;
    public String FreezingWarnMessage;
    public String FreezingLoginMessage;
    public String FreezingInIceBlock;
    public String ExhaustingWarnMessage;
    public String Refreshed;
    public String LanguageDescription;
    public String Temperature;
    public String TemperatureShow;
    public String TemperatureHide;
    public String YourStamina;
    public String CurrentTemperature;
    public String FCBlizzard;
    public String FCStorm;
    public String FCFreeze;
    public String FCRainSnow;
    public String FCCold;
    public String FCShowers;
    public String FCClear;
    public String FCWarm;
    public String FCSummerStorm;
    public String FCHot;
    public String FCTropic;
    public String FCToday;
    public String FCTomorrow;
    
    //private static Configuration Config = RealWeather.Config;
    private String MissingEntry = "Missing field in localization";
    private FileConfiguration Localization;
    private File LocFile;
    private final RealWeather plugin;
    
    public Localization(RealWeather plugin) {
        this.plugin = plugin;
    }
    
    public void FirstLoadLanguage() {
        LocFile = new File(plugin.getDataFolder(), "localization.yml");
        Localization = new YamlConfiguration();
        InitLocalFile();
        Language = Localization.getString("UseLanguage", "english");
        LoadSpecificLang(Language);
    }
    
    private void LoadSpecificLang(String language) {
        FreezingLoginMessage = Localization.getString(language + ".effect.freezing.PlayerWarnOnLogin", MissingEntry).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        FreezingWarnMessage = Localization.getString(language + ".effect.freezing.WarningMessage", MissingEntry).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        FreezingInIceBlock = Localization.getString(language + ".effect.freezing.FrozenInIce", MissingEntry).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        ExhaustingWarnMessage = Localization.getString(language + ".effect.exhausting.WarningMessage", MissingEntry).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        Refreshed = Localization.getString(language + ".effect.exhausting.Refreshed", MissingEntry).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        LanguageDescription = Localization.getString(language + ".description", MissingEntry).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        Temperature = Localization.getString(language + ".command.Temperature", MissingEntry).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        TemperatureShow = Localization.getString(language + ".command.TemperatureShow", MissingEntry).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        TemperatureHide = Localization.getString(language + ".command.TemperatureHide", MissingEntry).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        YourStamina = Localization.getString(language + ".command.YourStamina", MissingEntry).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        FCBlizzard = Localization.getString(language + ".forecast.BLIZZARD", MissingEntry).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        FCStorm = Localization.getString(language + ".forecast.STORM", MissingEntry).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        FCFreeze = Localization.getString(language + ".forecast.FREEZE", MissingEntry).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        FCRainSnow = Localization.getString(language + ".forecast.RAINSNOW", MissingEntry).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        FCCold = Localization.getString(language + ".forecast.COLD", MissingEntry).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        FCShowers = Localization.getString(language + ".forecast.SHOWERS", MissingEntry).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        FCClear = Localization.getString(language + ".forecast.CLEAR", MissingEntry).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        FCWarm = Localization.getString(language + ".forecast.WARM", MissingEntry).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        FCSummerStorm = Localization.getString(language + ".forecast.SUMMERSTORM", MissingEntry).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        FCHot = Localization.getString(language + ".forecast.HOT", MissingEntry).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        FCTropic = Localization.getString(language + ".forecast.TROPIC", MissingEntry).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        FCToday = Localization.getString(language + ".forecast.Today", MissingEntry).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        FCTomorrow = Localization.getString(language + ".forecast.Tomorrow", MissingEntry).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        CurrentTemperature = Localization.getString(language + ".command.CurrentTemperature", MissingEntry).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        if(plugin.Config.getVariables().isDebugMode()) plugin.log.log(Level.INFO, "[RealWeather] Localization loaded: " + language);
    }
    
    private void InitLocalFile() {
        PluginDescriptionFile pdfFile = plugin.getDescription();
        if(!LocFile.exists()) {
            LocFile.getParentFile().mkdirs();
            plugin.Utils.copy(plugin.getResource("localization.yml"), LocFile);
            plugin.log.log(Level.INFO, "[RealWeather] Localization file copied.");
        }
        LoadLocalizationFields();
        File oldLocalFile = new File("plugins/RealWeather/localization_" + Localization.getString("version", "old") + ".yml");
        if(!pdfFile.getVersion().equals(Localization.getString("version"))) {
            plugin.log.log(Level.INFO, "[RealWeather] Version of localization file doesn't match with current plugin version.");
            try {
                Localization.save(oldLocalFile);
                plugin.log.log(Level.INFO, "[RealWeather] Localization version: " + Localization.getString("version"));
                plugin.log.log(Level.INFO, "[RealWeather] Plugin version: " + pdfFile.getVersion());
                plugin.log.log(Level.INFO, "[RealWeather] Old localization.yml saved.");
            } catch(IOException ex) {
                plugin.log.log(Level.INFO, "[RealWeather] Saving of old config file failed. " + ex.getMessage());
            }
            LocFile.delete();
            plugin.Utils.copy(plugin.getResource("localization.yml"), LocFile);
            LoadLocalizationFields();
        } else {
            plugin.log("Localization file             OK.");
        }
    }
    
    public boolean LangExists(String lang) {
        if(Localization.isConfigurationSection(lang)) {
            return true;
        } else {
            return false;
        }
    }
    
    public HashMap<String, String> GetLangList() {
        HashMap<String, String> langs = new HashMap<String, String>();
        for (String lang : Localization.getKeys(false)) {
            if(lang.equals("version") || lang.equals("UseLanguage")) continue;
            if(Localization.contains(lang+".description")) langs.put(lang, Localization.getString(lang+".description"));
        }
        return langs;
    }
    
    public boolean SetLanguage(String lang) {
        try {
            Localization.set("UseLanguage", (String)lang);
            SaveLocalizationFields();
            LoadSpecificLang(Localization.getString("UseLanguage"));
            Language = Localization.getString("UseLanguage");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void LoadLocalizationFields() {
        try {
            Localization.load(LocFile);
        } catch (Exception e) {
            plugin.log.log(Level.WARNING, null, e);
        }
    }
    
    public void SaveLocalizationFields() {
        try {
            Localization.save(LocFile);
        } catch (Exception e) {
            plugin.log.log(Level.WARNING, null, e);
        }
    }
}
