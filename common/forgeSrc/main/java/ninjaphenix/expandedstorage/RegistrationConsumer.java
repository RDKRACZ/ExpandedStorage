package ninjaphenix.expandedstorage;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public interface RegistrationConsumer<B extends Block, I extends Item, E extends BlockEntity> {
    void accept(B[] blocks, I[] items, BlockEntityType<E> blockEntityType);
}
