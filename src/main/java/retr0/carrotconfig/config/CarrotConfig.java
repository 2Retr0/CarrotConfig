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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CarrotConfig {
    public static final Map<String, Class<?>> configClassMap = new HashMap<>();
    public static final List<EntryInfo> configEntries = new ArrayList<>();

    private static final Gson gson = new GsonBuilder()
        .excludeFieldsWithModifiers(Modifier.TRANSIENT)
        .excludeFieldsWithModifiers(Modifier.PRIVATE)
        .addSerializationExclusionStrategy(new HiddenAnnotationExclusionStrategy())
        .setPrettyPrinting()
        .create();

    public static void init(String modId, Class<?> configClass) {
        var configPath = FabricLoader.getInstance().getConfigDir().resolve(modId + ".json");
        configClassMap.put(modId, configClass);

        for (var field : configClass.getFields()) {
            if (!field.isAnnotationPresent(Entry.class)) continue;

            try {
                var translationKey = modId + "." + CarrotConfigClient.MOD_ID + "." + field.getName();
                var defaultValue = field.get(null);
                configEntries.add(
                    new EntryInfo(translationKey, field, defaultValue, field.getAnnotation(Entry.class).isColor()));
            } catch (IllegalAccessException ignored) {}
        }

        try {
            gson.fromJson(Files.newBufferedReader(configPath), configClass);
        } catch (Exception e) {
            write(modId);
        }
    }

    public static void write(String modId) {
        var path = FabricLoader.getInstance().getConfigDir().resolve(modId + ".json");
        var configClass = configClassMap.get(modId);

        try {
            if (!Files.exists(path)) Files.createFile(path);

            // Write config class values to the config file and notify ConfigSavedCallback listeners.
            Files.write(path, gson.toJson(configClass.getDeclaredConstructor().newInstance()).getBytes());
            ConfigSavedCallback.EVENT.invoker().onConfigSaved(configClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Environment(EnvType.CLIENT)
    public static Screen getScreen(Screen parent, String modId) {
        return new CarrotConfigScreen(parent, modId);
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
}
