package retr0.carrotconfig.config;

import com.google.gson.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.NotNull;
import retr0.carrotconfig.CarrotConfigClient;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static retr0.carrotconfig.CarrotConfigClient.LOGGER;

public abstract class CarrotConfig {
    public static final Map<String, ConfigInfo> configMap = new HashMap<>();

    private static final Gson gson = new GsonBuilder()
        .excludeFieldsWithModifiers(Modifier.TRANSIENT)
        .excludeFieldsWithModifiers(Modifier.PRIVATE)
        .addSerializationExclusionStrategy(new ExclusionStrategy() {
            public boolean shouldSkipClass(Class<?> _class) { return false; }

            public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                return fieldAttributes.getAnnotation(Entry.class) == null;
            }
        })
        .setPrettyPrinting()
        .create();

    public static void init(String modId, Class<?> configClass) {
        var configPath = FabricLoader.getInstance().getConfigDir().resolve(modId + ".json");
        var entries = getEntries(modId, configClass);
        configMap.put(modId, new ConfigInfo(configClass, configPath, List.copyOf(entries), -1));

        try {
            readConfig(modId, configClass);
        } catch (Exception e) {
            writeConfig(modId);
        }
        LOGGER.info("Loaded configuration for " + modId + "!");
    }

    public static void readConfig(String modId, Class<?> configClass) throws IOException, IllegalStateException {
        var reader = Files.newBufferedReader(configMap.get(modId).path());
        gson.fromJson(reader, configClass);
        // Enforce JSON to have all fields present in class (I'm not quite sure why this works lol)
        JsonParser.parseReader(reader).getAsJsonObject();
    }

    public static void writeConfig(String modId) {
        if (!configMap.containsKey(modId)) {
            LOGGER.error("Configuration for " + modId + " was requested but could not be found!");
            return;
        }

        var configInfo = configMap.get(modId);
        var configPath = configInfo.path();
        var configClass = configInfo.configClass();
        try {
            if (!Files.exists(configPath)) Files.createFile(configPath);

            // Write config class values to the config file and notify ConfigSavedCallback listeners.
            Files.write(configPath, gson.toJson(configClass.getDeclaredConstructor().newInstance()).getBytes());
            ConfigSavedCallback.EVENT.invoker().onConfigSaved(configClass);

            // Update last modified time
            var modifiedTime = Files.readAttributes(configPath, BasicFileAttributes.class).lastModifiedTime().toMillis();
            configMap.put(modId, configInfo.withModifiedTime(modifiedTime));
        } catch (Exception e) { //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @Environment(EnvType.CLIENT)
    public static Screen getScreen(Screen parent, String modId) {
        if (!configMap.containsKey(modId)) {
            LOGGER.error("A config associated with modid " + modId + " was requested but could not be found!");
            return parent;
        }

        var configInfo = configMap.get(modId);
        try {
            // Update entries if config was externally modified
            var actualModifiedTime = Files.readAttributes(configInfo.path(), BasicFileAttributes.class).lastModifiedTime().toMillis();
            if (actualModifiedTime != configInfo.modifiedTime()) {
                try {
                    readConfig(modId, configInfo.configClass());
                } catch (Exception ignored) {}
                configInfo = configInfo.withModifiedTime(actualModifiedTime);
                configMap.put(modId, configInfo);
            }
        } catch (Exception e) { //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }

        return new CarrotConfigScreen(parent, modId, configInfo.entries());
    }

    @NotNull
    private static ArrayList<EntryInfo> getEntries(String modId, Class<?> configClass) {
        var entries = new ArrayList<EntryInfo>();

        for (var field : configClass.getFields()) {
            if (!field.isAnnotationPresent(Entry.class) && !field.isAnnotationPresent(Comment.class)) continue;

            try {
                var translationKey = modId + "." + CarrotConfigClient.MOD_ID + "." + field.getName();
                var defaultValue = field.get(null);
                entries.add(new EntryInfo(translationKey, field, defaultValue, !field.isAnnotationPresent(Comment.class) && field.getAnnotation(Entry.class).isColor()));
            } catch (IllegalAccessException ignored) {}
        }
        return entries;
    }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)
    public @interface Entry {
        double min() default Double.MIN_NORMAL;
        double max() default Double.MAX_VALUE;
        boolean isColor() default false;
    }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface Comment {}

    public record EntryInfo(String translationKey, Field field, Object defaultValue, boolean isColor) { }
    public record ConfigInfo(Class<?> configClass, Path path, List<EntryInfo> entries, long modifiedTime) {
        public ConfigInfo withModifiedTime(long modifiedTime) {
            return new ConfigInfo(configClass(), path(), entries(), modifiedTime);
        }
    }
}
