package ninjaphenix.expandedstorage.barrel.block.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import ninjaphenix.expandedstorage.barrel.block.BarrelBlock;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.AbstractOpenableStorageBlockEntity;

public class BarrelBlockEntity extends AbstractOpenableStorageBlockEntity {
    public BarrelBlockEntity(BlockEntityType<BarrelBlockEntity> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state, ((BarrelBlock) state.getBlock()).getBlockId());
    }

    @Override
    protected void onOpen(Level world, BlockPos pos, BlockState state) {
        BarrelBlockEntity.playSound(world, state, pos, SoundEvents.BARREL_OPEN);
        BarrelBlockEntity.updateBlockState(world, state, pos, true);
    }

    @Override
    protected void onClose(Level world, BlockPos pos, BlockState state) {
        BarrelBlockEntity.playSound(world, state, pos, SoundEvents.BARREL_CLOSE);
        BarrelBlockEntity.updateBlockState(world, state, pos, false);
    }

    private static void playSound(Level world, BlockState state, BlockPos pos, SoundEvent sound) {
        Vec3i facingVector = state.getValue(BlockStateProperties.FACING).getNormal();
        double X = pos.getX() + 0.5D + facingVector.getX() / 2.0D;
        double Y = pos.getY() + 0.5D + facingVector.getY() / 2.0D;
        double Z = pos.getZ() + 0.5D + facingVector.getZ() / 2.0D;
        world.playSound(null, X, Y, Z, sound, SoundSource.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
    }

    private static void updateBlockState(Level world, BlockState state, BlockPos pos, boolean open) {
        world.setBlock(pos, state.setValue(BlockStateProperties.OPEN, open), Block.UPDATE_ALL);
    }
}
