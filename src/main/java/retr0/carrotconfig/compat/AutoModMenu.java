package retr0.carrotconfig.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import retr0.carrotconfig.config.CarrotConfig;

import java.util.HashMap;
import java.util.Map;

public class AutoModMenu implements ModMenuApi {


    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        HashMap<String, ConfigScreenFactory<?>> factoryMap = new HashMap<>();
        CarrotConfig.configMap.forEach((modId, configInfo) ->
            factoryMap.put(modId, parent -> CarrotConfig.getScreen(parent, modId)));
        return factoryMap;
    }
}
