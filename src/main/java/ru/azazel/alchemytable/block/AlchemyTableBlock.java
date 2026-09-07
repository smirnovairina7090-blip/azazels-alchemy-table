package ru.azazel.alchemytable.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import ru.azazel.alchemytable.block.entity.AlchemyTableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import ru.azazel.alchemytable.block.entity.ModBlockEntities;

public class AlchemyTableBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING =
            BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE_NORTH =
            Block.box(-16, 0, 0, 16, 25, 16);
    private static final VoxelShape SHAPE_EAST =
            Block.box(0, 0, -16, 16, 25, 16);
    private static final VoxelShape SHAPE_SOUTH =
            Block.box(0, 0, 0, 32, 25, 16);
    private static final VoxelShape SHAPE_WEST =
            Block.box(0, 0, 0, 16, 25, 32);

    public AlchemyTableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlchemyTableBlockEntity(pos, state);
    }
    @Override
public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
        Level level,
        BlockState state,
        BlockEntityType<T> type
) {

    // Этот ticker предназначен только
    // для нашего алхимического стола.
    if (type != ModBlockEntities.ALCHEMY_TABLE_BLOCK_ENTITY) {
        return null;
    }

    return (
            tickerLevel,
            tickerPos,
            tickerState,
            blockEntity
    ) -> {

        if (blockEntity instanceof AlchemyTableBlockEntity table) {

            AlchemyTableBlockEntity.tick(
                    tickerLevel,
                    tickerPos,
                    tickerState,
                    table
            );
        }
    };
}

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(
                        FACING,
                        context.getHorizontalDirection().getOpposite()
                );
    }

    private void openAlchemyTableMenu(
            Level level,
            BlockPos pos,
            Player player
    ) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof AlchemyTableBlockEntity alchemyTable) {
                player.openMenu(alchemyTable);
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        openAlchemyTableMenu(level, pos, player);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (hand == InteractionHand.MAIN_HAND) {
            openAlchemyTableMenu(level, pos, player);
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void onRemove(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState newState,
            boolean movedByPiston
    ) {
        if (!level.isClientSide() && !state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof AlchemyTableBlockEntity alchemyTable) {
                Containers.dropContents(level, pos, alchemyTable);
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(FACING);
    }

    private VoxelShape getTableShape(BlockState state) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return getTableShape(state);
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return getTableShape(state);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(
                FACING,
                rotation.rotate(state.getValue(FACING))
        );
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(
                mirror.getRotation(state.getValue(FACING))
        );
    }
}
