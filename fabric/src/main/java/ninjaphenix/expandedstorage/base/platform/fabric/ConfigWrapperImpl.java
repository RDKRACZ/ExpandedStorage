package ninjaphenix.expandedstorage.base.platform.fabric;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import ninjaphenix.expandedstorage.base.BaseCommon;
import ninjaphenix.expandedstorage.base.config.*;
import ninjaphenix.expandedstorage.base.internal_api.Utils;
import ninjaphenix.expandedstorage.base.platform.ConfigWrapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

public final class ConfigWrapperImpl implements ConfigWrapper {
    private Type mapType;
    private Gson gson;
    private Path configPath;
    private ConfigV0 config;

    private ConfigWrapperImpl() {

    }

    public static ConfigWrapper getInstance() {
        return new ConfigWrapperImpl();
    }

    public void initialise() {
        mapType = new TypeToken<Map<String, Object>>() {
        }.getType();
        configPath = FabricLoader.getInstance().getConfigDir().resolve(Utils.CONFIG_PATH);
        gson = new GsonBuilder().registerTypeAdapter(ResourceLocation.class, new ResourceLocationTypeAdapter())
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setPrettyPrinting()
                .setLenient()
                .create();
        config = this.getConfig();
    }

    public boolean isScrollingUnrestricted() {
        return !config.isScrollingRestricted();
    }

    public void setScrollingRestricted(boolean value) {
        if (config.isScrollingRestricted() == value) {
            config.setScrollingRestricted(!value);
            this.saveConfig(config);
        }
    }

    public ResourceLocation getPreferredContainerType() {
        return config.getContainerType();
    }

    public boolean setPreferredContainerType(ResourceLocation containerType) {
        if ((Utils.UNSET_CONTAINER_TYPE.equals(containerType) || Utils.PAGE_CONTAINER_TYPE.equals(containerType)
                || Utils.SCROLL_CONTAINER_TYPE.equals(containerType) || Utils.SINGLE_CONTAINER_TYPE.equals(containerType))
                && containerType != config.getContainerType()) {
            config.setContainerType(containerType);
            this.saveConfig(config);
            return true;
        }
        return false;
    }

    private <T extends Config> void saveConfig(T config) {
        try (BufferedWriter writer = Files.newBufferedWriter(configPath)) {
            Map<String, Object> configValues = config.getConverter().toSource(config);
            configValues.put("config_version", config.getVersion());
            gson.toJson(configValues, mapType, gson.newJsonWriter(writer));
        } catch (IOException e) {
            BaseCommon.LOGGER.warn("Failed to save Expanded Storage's config.", e);
        }
    }

    private ConfigV0 getConfig() {
        Path oldPath = FabricLoader.getInstance().getConfigDir().resolve(Utils.FABRIC_LEGACY_CONFIG_PATH);
        boolean triedLoadingOldConfig = false;
        Path newPath = configPath;
        boolean triedLoadingNewConfig = false;
        ConfigV0 config = null;
        if (Files.exists(newPath)) {
            triedLoadingNewConfig = true;
            config = this.loadConfig(newPath, ConfigV0.Factory.INSTANCE, false);
        }
        if (Files.exists(oldPath)) {
            triedLoadingOldConfig = true;
            try (BufferedReader reader = Files.newBufferedReader(oldPath)) {
                String configLines = reader.lines().collect(Collectors.joining());
                if (config == null) {
                    ConfigV0 oldConfig = this.convertToConfig(configLines, LegacyFactory.INSTANCE, oldPath);
                    if (oldConfig != null) {
                        config = oldConfig;
                        this.saveConfig(config);
                    }
                }
                this.backupFile(oldPath, String.format("Failed to backup legacy Expanded Storage config, '%s'.", oldPath.getFileName().toString()), configLines);
            } catch (IOException e) {
                if (config == null) {
                    BaseCommon.LOGGER.warn("Failed to load legacy Expanded Storage Config, new default config will be used.", e);
                }
            }
        }
        if (config == null) {
            if (triedLoadingOldConfig || triedLoadingNewConfig) {
                BaseCommon.LOGGER.warn("Could not load an existing config, Expanded Storage is using it's default config.");
            }
            config = new ConfigV0();
            this.saveConfig(config);
        }
        return config;
    }

    // Tries to load a config file, returns null if loading fails.
    // Will need to be reworked to allow converting between ConfigV0 and ConfigV1
    // essentially converter will need to be decided in this method based on the value of "config_version"
    private <T extends Config> T convertToConfig(String lines, Converter<Map<String, Object>, T> converter, Path configPath) {
        try {
            Map<String, Object> configMap = gson.fromJson(lines, mapType);
            // Do not edit, gson returns a double, we want an int.
            // This is so fucking cursed.
            int configVersion = Mth.floor((Double) configMap.getOrDefault("config_version", (double) -1));
            if (configVersion == converter.getSourceVersion()) {
                T returnValue = converter.fromSource(configMap);
                if (returnValue.getVersion() == converter.getTargetVersion()) {
                    return returnValue;
                } else {
                    throw new IllegalStateException(String.format("CODE ERROR: Converter converted to an invalid config, expected version %s, got %s.", converter.getTargetVersion(), returnValue.getVersion()));
                }
            } else {
                throw new IllegalStateException(String.format("CODE ERROR: Converter converted to an invalid config, expected version %s, got %s.", converter.getSourceVersion(), configVersion));
            }
        } catch (JsonParseException e) {
            String configFileName = configPath.getFileName().toString();
            BaseCommon.warnThrowableMessage("Failed to convert config, backing config '{}'.", e, configFileName);
            this.backupFile(configPath, String.format("Failed to backup expanded storage config which failed to read, '%s'.%n", configFileName), lines);
            return null;
        }
    }

    private void backupFile(Path path, String failureMessage, String contents) {
        try {
            Path backupPath = path.resolveSibling(path.getFileName().toString() + ".backup");
            Files.move(path, backupPath);
        } catch (IOException e2) {
            BaseCommon.LOGGER.warn(failureMessage, e2);
            if (contents != null) {
                BaseCommon.LOGGER.warn(contents);
            }
        }
    }

    private <T extends Config> T loadConfig(Path configPath, Converter<Map<String, Object>, T> converter, boolean isLegacy) {
        try (BufferedReader reader = Files.newBufferedReader(configPath)) {
            String configLines = reader.lines().collect(Collectors.joining());
            return this.convertToConfig(configLines, converter, configPath);
        } catch (IOException e) {
            String configFileName = configPath.getFileName().toString();
            BaseCommon.warnThrowableMessage("Failed to read {}Expanded Storage config, '{}'.", e, isLegacy ? "legacy " : "", configFileName);
            e.printStackTrace();
            this.backupFile(configPath, String.format("Failed to backup %sExpanded Storage config, '%s'.%n", isLegacy ? "legacy " : "", configFileName), null);
        }
        return null;
    }
}