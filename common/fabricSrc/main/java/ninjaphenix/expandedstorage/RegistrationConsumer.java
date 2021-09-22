package ninjaphenix.expandedstorage;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;

public interface RegistrationConsumer<B extends Block, I extends Item, E extends BlockEntity> {
    void accept(B[] blocks, I[] items, BlockEntityType<E> blockEntityType);
}
