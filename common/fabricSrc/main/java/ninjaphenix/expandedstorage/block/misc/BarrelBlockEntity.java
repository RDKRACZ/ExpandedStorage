package ninjaphenix.expandedstorage.block.misc;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import ninjaphenix.expandedstorage.block.BarrelBlock;
import ninjaphenix.expandedstorage.internal_api.block.misc.AbstractOpenableStorageBlockEntity;

public class BarrelBlockEntity extends AbstractOpenableStorageBlockEntity {
    public BarrelBlockEntity(BlockEntityType<BarrelBlockEntity> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state, ((BarrelBlock) state.getBlock()).getBlockId());
    }

    @Override
    protected void onOpen(World world, BlockPos pos, BlockState state) {
        BarrelBlockEntity.playSound(world, state, pos, SoundEvents.BLOCK_BARREL_OPEN);
        BarrelBlockEntity.updateBlockState(world, state, pos, true);
    }

    @Override
    protected void onClose(World world, BlockPos pos, BlockState state) {
        BarrelBlockEntity.playSound(world, state, pos, SoundEvents.BLOCK_BARREL_CLOSE);
        BarrelBlockEntity.updateBlockState(world, state, pos, false);
    }

    private static void playSound(World world, BlockState state, BlockPos pos, SoundEvent sound) {
        Vec3i facingVector = state.get(Properties.FACING).getVector();
        double X = pos.getX() + 0.5D + facingVector.getX() / 2.0D;
        double Y = pos.getY() + 0.5D + facingVector.getY() / 2.0D;
        double Z = pos.getZ() + 0.5D + facingVector.getZ() / 2.0D;
        world.playSound(null, X, Y, Z, sound, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
    }

    private static void updateBlockState(World world, BlockState state, BlockPos pos, boolean open) {
        world.setBlockState(pos, state.with(Properties.OPEN, open), Block.NOTIFY_ALL);
    }
}
