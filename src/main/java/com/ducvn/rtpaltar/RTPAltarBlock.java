package com.ducvn.rtpaltar;

import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class RTPAltarBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public RTPAltarBlock(Properties p_49795_) {
        super(p_49795_);
    }

    private static final VoxelShape SHAPE =
            Block.box(0, 0, 0, 16, 24, 16);

    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public boolean isValidSpawn(BlockState state, BlockGetter level, BlockPos pos, SpawnPlacements.Type type, EntityType<?> entityType) {
        return false;
    }

    @Override
    public InteractionResult use(BlockState p_60503_, Level level, BlockPos pos, Player activeBlockPlayer, InteractionHand p_60507_, BlockHitResult p_60508_) {
        if (activeBlockPlayer.level instanceof ServerLevel){
            ServerLevel serverLevel = (ServerLevel) activeBlockPlayer.level;
            MinecraftServer minecraftserver = serverLevel.getServer();
            ResourceKey<Level> resourcekey = activeBlockPlayer.level.OVERWORLD;
            ServerLevel overworldLevel = minecraftserver.getLevel(resourcekey);
            if ((activeBlockPlayer.level.dimension() != Level.OVERWORLD) && (overworldLevel != null)){
                AABB altarArea = new AABB(pos.getX() - 3D, pos.getY() - 2D, pos.getZ() - 3D,
                        pos.getX() + 3D, pos.getY() + 3D, pos.getZ() + 3D);
                List<Player> playersNearAltar = level.getEntitiesOfClass(Player.class, altarArea);
                BlockPos teleportLocation;
                if (RTPAltarConfig.search_for_surface.get()){
                    teleportLocation = getValidLocation(getRandomLocation(overworldLevel), overworldLevel);
                }
                else {
                    teleportLocation = getRandomLocation(overworldLevel);
                }
                for (Player player : playersNearAltar ){
                    if (player.isAlive() && !player.isSpectator() && player instanceof ServerPlayer){
                        ((ServerPlayer) player).teleportTo(overworldLevel,
                                teleportLocation.getX(),teleportLocation.getY(),teleportLocation.getZ(),
                                0F, 0F);
                        overworldLevel.addDuringTeleport(player);
                        if (!RTPAltarConfig.search_for_surface.get()){
                            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 600, 0));
                        }
                    }
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState p_220827_, Level level, BlockPos pos, RandomSource randomSource) {
        level.addParticle(ParticleTypes.ENCHANT,
                (pos.getX() - 0.2D) + (randomSource.nextDouble() * 1.4D),
                pos.getY() + 1.7D,
                (pos.getZ() - 0.2D) + (randomSource.nextDouble() * 1.4D),
                0D, 0D, 0D);
        
        super.animateTick(p_220827_, level, pos, randomSource);
    }

    private BlockPos getRandomLocation(Level level){
        WorldBorder worldBorder = level.getWorldBorder();
        Random randomLocation = new Random();
        double randomX = randomLocation.nextDouble(worldBorder.getMaxX());
        if (randomLocation.nextBoolean()){
            randomX = -randomX;
        }
        double randomZ = randomLocation.nextDouble(worldBorder.getMaxZ());
        if (randomLocation.nextBoolean()){
            randomZ = -randomZ;
        }
        return new BlockPos(randomX, 319D, randomZ);
    }

    private BlockPos getValidLocation(BlockPos pos, Level level){
        while (true){
            if (level.getBlockState(pos).getMaterial().isSolid() && !(level.getBlockState(pos).getBlock() instanceof LeavesBlock)){
                if (level.getBlockState(pos.above()).isAir() && level.getBlockState(pos.above().above()).isAir()){
                    pos = pos.above();
                    break;
                }
            }
            if (level.getBlockState(pos).getMaterial().isLiquid()){
                pos = getRandomLocation(level);
            }
            pos = pos.below();
            if (pos.getY() < -64){
                pos = getRandomLocation(level);
            }
        }
        return pos.above();
    }

    private boolean hasEnteredNether(Player player){
        ServerAdvancementManager serveradvancementmanager = player.getServer().getAdvancements();
        Advancement advancement = serveradvancementmanager.getAdvancement(new ResourceLocation("minecraft:story/enter_the_nether"));
        return ((ServerPlayer) player).getAdvancements().getOrStartProgress(advancement).isDone();
    }
}
