package retr0.carrotconfig.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import retr0.carrotconfig.config.CarrotConfig;

import java.util.HashMap;
import java.util.Map;

import static retr0.carrotconfig.CarrotConfigClient.MOD_ID;

public class AutoModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> CarrotConfig.getScreen(parent, MOD_ID);
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        HashMap<String, ConfigScreenFactory<?>> factoryMap = new HashMap<>();
        CarrotConfig.configMap.forEach((modId, configInfo) ->
            factoryMap.put(modId, parent -> CarrotConfig.getScreen(parent, modId)));
        return factoryMap;
    }
}
