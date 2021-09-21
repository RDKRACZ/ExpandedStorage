package ninjaphenix.expandedstorage;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;

import java.util.Set;

public interface RegistrationConsumer<B extends Block, I extends Item, E extends BlockEntity> {
    void accept(Set<B> blocks, Set<I> items, BlockEntityType<E> blockEntityType);
}
