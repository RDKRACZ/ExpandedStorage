package ninjaphenix.expandedstorage.block.misc;

import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;

import java.util.Locale;

public enum FaceRotation implements StringIdentifiable {
    NORTH(0),
    EAST(90),
    SOUTH(180),
    WEST(270);

    private final int rotationAngle;

    FaceRotation(int rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    @Override
    public String asString() {
        return name().toLowerCase(Locale.ROOT);
    }

    public int asRotationAngle() {
        return rotationAngle;
    }

    public Direction asDirection(Direction.Axis axis) {
        if (axis != Direction.Axis.Y) {
            throw new IllegalArgumentException("axis can only be Y");
        }
        return switch (this) {
            case NORTH -> Direction.NORTH;
            case EAST -> Direction.EAST;
            case SOUTH -> Direction.SOUTH;
            case WEST -> Direction.WEST;
        };
    }

    /**
     * Direction to FaceRotation, Y axis only
     */
    public static FaceRotation of(Direction direction) {
        return switch (direction) {
            case NORTH -> FaceRotation.NORTH;
            case EAST -> FaceRotation.EAST;
            case SOUTH -> FaceRotation.SOUTH;
            case WEST -> FaceRotation.WEST;
            default -> throw new IllegalStateException("Unexpected value: " + direction);
        };
    }

    public FaceRotation rotated(BlockRotation rotation) {
        return switch (rotation) {
            case NONE -> this;
            case CLOCKWISE_90 -> this.rotatedClockwise();
            case CLOCKWISE_180 -> this.opposite();
            case COUNTERCLOCKWISE_90 -> this.rotatedCounterClockwise();
        };
    }

    public FaceRotation rotatedClockwise() {
        return switch (this) {
            case NORTH -> FaceRotation.EAST;
            case EAST -> FaceRotation.SOUTH;
            case SOUTH -> FaceRotation.WEST;
            case WEST -> FaceRotation.NORTH;
        };
    }

    public FaceRotation rotatedCounterClockwise() {
        return switch (this) {
            case NORTH -> FaceRotation.WEST;
            case EAST -> FaceRotation.NORTH;
            case SOUTH -> FaceRotation.EAST;
            case WEST -> FaceRotation.SOUTH;
        };
    }

    public FaceRotation opposite() {
        return switch (this) {
            case NORTH -> FaceRotation.SOUTH;
            case EAST -> FaceRotation.WEST;
            case SOUTH -> FaceRotation.NORTH;
            case WEST -> FaceRotation.EAST;
        };
    }

    private static Direction rotate(Direction input, Direction.Axis axis, FaceRotation rotation) {
        return switch (rotation) {
            case NORTH -> input;
            case EAST -> input.rotateClockwise(axis);
            case SOUTH -> input.getOpposite();
            case WEST -> input.rotateCounterclockwise(axis);
        };
    }

    public static Direction getRelativeDirection(Direction direction, FaceRotation face, FaceRotation y, FaceRotation perpendicular) {
        Direction.Axis faceAxis;
        Direction.Axis perpendicularAxis;
        if (y == FaceRotation.NORTH || y == FaceRotation.SOUTH) {
            faceAxis = Direction.Axis.Z;
            perpendicularAxis = Direction.Axis.X;
        } else {
            faceAxis = Direction.Axis.X;
            perpendicularAxis = Direction.Axis.Z;
        }
        if (direction.getAxis().isHorizontal()) {
            direction = FaceRotation.rotate(direction, Direction.Axis.Y, y);
        }
        if (direction.getAxis() != perpendicularAxis) {
            direction = FaceRotation.rotate(direction, faceAxis, face);
        }
        if (direction.getAxis() != faceAxis) {
            direction = FaceRotation.rotate(direction, perpendicularAxis, perpendicular);
        }
        return direction;
    }

    // Returned directions is relative to north, facing, north
    public static Direction getDefinitiveDirection(Direction direction, FaceRotation face, FaceRotation y, FaceRotation perpendicular) {
        return null;
    }
}
