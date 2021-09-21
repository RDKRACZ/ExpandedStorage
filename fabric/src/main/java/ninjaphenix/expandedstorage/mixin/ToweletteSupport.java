package ninjaphenix.expandedstorage.mixin;

import ninjaphenix.expandedstorage.block.ChestBlock;
import org.spongepowered.asm.mixin.Mixin;
import virtuoel.towelette.api.Fluidloggable;

// todo: find alternative for this, mixins on mod classes aren't allowed but work.
@Mixin(ChestBlock.class)
public abstract class ToweletteSupport implements Fluidloggable {
}
