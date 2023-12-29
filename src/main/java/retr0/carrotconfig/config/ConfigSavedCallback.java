package retr0.carrotconfig.config;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@FunctionalInterface
public interface ConfigSavedCallback {
    Event<ConfigSavedCallback> EVENT = EventFactory.createArrayBacked(ConfigSavedCallback.class,
            (listeners) -> (configClass) -> {
                for (var listener : listeners) listener.onConfigSaved(configClass);
            });

    void onConfigSaved(Class<?> configClass);
}
