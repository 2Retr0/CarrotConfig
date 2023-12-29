package retr0.carrotconfig.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;
import retr0.carrotconfig.CarrotConfigClient;
import retr0.carrotconfig.config.CarrotConfig;

import java.util.HashMap;
import java.util.Map;

public class AutoModMenu implements ModMenuApi {
    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        // Create a factory for every mod that is dependent on CarrotConfig (assuming it will
        // initialize a configuration at some point.
        // TODO: This method is not very robust!
        var factoryMap = new HashMap<String, ConfigScreenFactory<?>>();
        FabricLoader.getInstance().getAllMods().forEach(mod -> {
            var isDependent = mod.getMetadata().getDependencies().stream()
                    .anyMatch(dependency -> dependency.getModId().equals(CarrotConfigClient.MOD_ID));

            if (!isDependent) return;

            var modId = mod.getMetadata().getId();
            factoryMap.put(modId, parent -> CarrotConfig.getScreen(parent, modId));
        });

//        CarrotConfig.configMap.forEach((modId, configInfo) ->
//            factoryMap.put(modId, parent -> CarrotConfig.getScreen(parent, modId)));
        return factoryMap;
    }

//    private void waitForDependentModInitialization() {
//        for (var mod : FabricLoader.getInstance().getAllMods()) {
//            var isDependent = mod.getMetadata().getDependencies().stream()
//                    .anyMatch(dependency -> dependency.getModId().equals(CarrotConfigClient.MOD_ID));
//
//            if (!isDependent) continue;
//
//            CarrotConfigClient.LOGGER.error(FabricLoader.getInstance().isModLoaded(mod.getMetadata().getId()) + " for " + mod.getMetadata().getId());
//
//            while (!FabricLoader.getInstance().isModLoaded(mod.getMetadata().getId())) {
//                CarrotConfigClient.LOGGER.error("waiting on " + mod.getMetadata().getId() + "...");
//            }
//        }
//    }
}
