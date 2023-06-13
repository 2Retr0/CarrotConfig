package retr0.carrotconfig.config;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import retr0.carrotconfig.CarrotConfigClient;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
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
        .addSerializationExclusionStrategy(new HiddenAnnotationExclusionStrategy())
        .setPrettyPrinting()
        .create();

    public static void init(String modId, Class<?> configClass) {
        var configPath = FabricLoader.getInstance().getConfigDir().resolve(modId + ".json");
        var entries = new ArrayList<EntryInfo>();

        for (var field : configClass.getFields()) {
            if (!field.isAnnotationPresent(Entry.class)) continue;

            try {
                var translationKey = modId + "." + CarrotConfigClient.MOD_ID + "." + field.getName();
                var defaultValue = field.get(null);
                entries.add(
                    new EntryInfo(translationKey, field, defaultValue, field.getAnnotation(Entry.class).isColor()));
            } catch (IllegalAccessException ignored) {}
        }
        configMap.put(modId, new ConfigInfo(configClass, configPath, List.copyOf(entries)));

        try {
            gson.fromJson(Files.newBufferedReader(configPath), configClass);
        } catch (Exception e) {
            write(modId);
        }

        LOGGER.info("Loaded configuration for " + modId + "!");
    }



    public static void write(String modId) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Environment(EnvType.CLIENT)
    public static Screen getScreen(Screen parent, String modId) {
        if (!configMap.containsKey(modId)) {
            LOGGER.error("A config associated with modid " + modId + " was requested but could not be found!");
            return parent;
        }

        return new CarrotConfigScreen(parent, modId, configMap.get(modId).entries);
    }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)
    public @interface Entry {
        double min() default Double.MIN_NORMAL;
        double max() default Double.MAX_VALUE;
        boolean isColor() default false;
    }

    public static class HiddenAnnotationExclusionStrategy implements ExclusionStrategy {
        public boolean shouldSkipClass(Class<?> _class) { return false; }

        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            return fieldAttributes.getAnnotation(Entry.class) == null;
        }
    }

    @FunctionalInterface
    public interface ConfigSavedCallback {
        Event<ConfigSavedCallback> EVENT = EventFactory.createArrayBacked(ConfigSavedCallback.class,
            (listeners) -> (configClass) -> {
                for (var listener : listeners) listener.onConfigSaved(configClass);
            });

        void onConfigSaved(Class<?> configClass);
    }

    public record EntryInfo(String translationKey, Field field, Object defaultValue, boolean isColor) { }
    public record ConfigInfo(Class<?> configClass, Path path, List<EntryInfo> entries) { }
}
