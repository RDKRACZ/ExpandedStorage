package ninjaphenix.expandedstorage.base;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.util.registry.Registry;
import ninjaphenix.expandedstorage.base.internal_api.BaseApi;
import ninjaphenix.expandedstorage.base.internal_api.ModuleInitializer;

import java.util.Comparator;
import java.util.List;

public final class Main implements ModInitializer {
    @Override
    public void onInitialize() {
        BaseCommon.initialize();
        BaseApi.getInstance().getAndClearItems().forEach((id, item) -> Registry.register(Registry.ITEM, id, item));

        List<EntrypointContainer<ModuleInitializer>> entries = FabricLoader.getInstance().getEntrypointContainers("expandedstorage-module", ModuleInitializer.class);
        // Note, you should not rely on this sorting, this is purely for creative tab ordering.
        entries.sort(Comparator.comparing(entrypoint -> entrypoint.getProvider().getMetadata().getName()));
        entries.forEach(e -> e.getEntrypoint().initialize());

        /* GOALS
         *
         * Provide a centralised api for kubejs and java to register new tiers and therefore blocks.
         *  will probably make my own json loaded content at some point...
         * Probably a bunch of other stuff I can't think of.
         */
    }
}
