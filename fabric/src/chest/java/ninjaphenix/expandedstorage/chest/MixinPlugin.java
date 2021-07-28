package ninjaphenix.expandedstorage.chest;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class MixinPlugin implements IMixinConfigPlugin {
    @Override
    public boolean shouldApplyMixin(String targetClassPath, String mixinClassPath) {
        String className = mixinClassPath.substring(40);
        return switch (className) {
            case "ToweletteSupport" -> FabricLoader.getInstance().isModLoaded("towelette");
            case "HTMChestSupport", "HTMOpenableBlockEntitySupport" -> FabricLoader.getInstance().isModLoaded("htm");
            default -> true;
        };
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myConfigTargets, Set<String> othersConfigTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassPath, ClassNode targetClass, String mixinClassPath, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassPath, ClassNode targetClass, String mixinClassPath, IMixinInfo mixinInfo) {

    }
}
