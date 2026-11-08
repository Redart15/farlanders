package cookie.farlanders.core.features;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntityActivator;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.PackedField;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

import java.util.Random;

public class BlockLogicSmoker extends BlockLogic {
	public static final PackedField ORIENTATION = new PackedField(0, 3);
	public static final PackedField ACTIVE = new PackedField(3, 1);
	public static final float W = 0.1875f;
	public static final float W_1 = 1 - W;
	public static final String SMOKE = "smoke";

	public BlockLogicSmoker(Block<?> block) {
		super(block, Materials.STONE);
	}

	@Override
	public boolean onInteracted(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Player player, @Nullable Side side, double xHit, double yHit) {
		if(player.getHeldItem() == null){
			return this.invertSmoker(world, tilePos);
		}
		return false;
	}

	@Override
	public void onActivatorInteracted(@NotNull World world, @NotNull TilePosc tilePos, @NotNull TileEntityActivator activator, @NotNull Direction direction) {
		this.invertSmoker(world, tilePos);
	}

	private boolean invertSmoker(@NotNull World world, @NotNull TilePosc tilePos) {
		int meta = world.getBlockData(tilePos);
		world.setBlockData(tilePos, ACTIVE.set(meta, 1 - ACTIVE.get(meta)));
		return true;
	}

	@Override
	public void onPlacedByMob(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Side side, @NotNull Mob mob, double xHit, double yHit) {
		this.onPlacedOnSide(world, tilePos, side, xHit, yHit);
	}



	@Override
	public @NotNull AABBdc getSelectionAABB(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		return this.getBoundingBox(source, tilePos);
	}

	@Override
	public @Nullable AABBdc getCollisionAABB(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		return this.getBoundingBox(source, tilePos);
	}


	private AABBdc getBoundingBox(@NotNull WorldSource world, @NotNull TilePosc tilePos) {
		final double minX = tilePos.x();
		final double minY = tilePos.y();
		final double minZ = tilePos.z();
		final double maxX = minX + 1;
		final double maxY = minY + 1;
		final double maxZ = minZ + 1;
		int meta = world.getBlockData(tilePos);
		Side side = Side.fromId(ORIENTATION.get(meta));
		return switch (side) {
			case TOP -> new AABBd(minX, minY + W_1, minZ, maxX, maxY, maxZ);        // UP
			case NORTH -> new AABBd(minX, minY, minZ, maxX, maxY, minZ + W);    // NORTH
			case SOUTH -> new AABBd(minX, minY, minZ + W_1, maxX, maxY, maxZ);        // SOUTH
			case WEST -> new AABBd(minX, minY, minZ, minX + W, maxY, maxZ);        // WEST
			case EAST -> new AABBd(minX + W_1, minY, minZ, maxX, maxY, maxZ);        // EAST
			default -> new AABBd(minX, minY, minZ, maxX, minY + W, maxZ);        // DOWN
		};
	}

	@Override
	public boolean isCubeShaped() {
		return false;
	}

	@Override
	public boolean isSolidRender() {
		return false;
	}


	@Override
	public void onNeighborChanged(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> block) {
		int meta = world.getBlockData(tilePos);
		int bitSet = ACTIVE.get(meta);
		boolean hasSignal = world.hasDirectSignal(tilePos) || world.hasNeighborSignal(tilePos);
		if (hasSignal != (bitSet == 1)) {
			world.setBlockDataNotify(tilePos, ACTIVE.set(meta, 1 - bitSet));
		}
	}

	@Override
	public void onPlacedOnSide(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Side side, double xHit, double yHit) {
		world.setBlockDataNotify(tilePos, side.opposite().id);
		world.scheduleBlockUpdate(tilePos, block, 0);
		int meta = world.getBlockData(tilePos);
		int bitSet = ACTIVE.get(meta);
		boolean hasSignal = world.hasDirectSignal(tilePos) || world.hasNeighborSignal(tilePos);
		if (hasSignal != (bitSet == 1)) {
			world.setBlockDataNotify(tilePos, ACTIVE.set(meta, 1 - bitSet));
		}
	}

	@Override
	public void animationTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand) {
		int meta = world.getBlockData(tilePos);
		if (world.isClientSide || (ACTIVE.get(meta)) != 1) {
			return;
		}
		double xPos = tilePos.x() + 0.5;
		double yPos = tilePos.y() + 0.5;
		double zPos = tilePos.z() + 0.5;
		Side side = Side.fromId(ORIENTATION.get(meta));
		for (int i = 0; i < 24; ++i) {
			this.getParticleData(world, rand, side, xPos, yPos, zPos);
		}
	}

	private void getParticleData(@NotNull World world, @NotNull Random rand, @NotNull Side side, double x, double y, double z) {
		double mtOff = 0.08;
		switch (side) {
			case TOP ->
				world.spawnParticle(
					SMOKE,
					x + (rand.nextFloat() - 0.5) * 0.8,
					y + 0.25,
					z + (rand.nextFloat() - 0.5) * 0.8,
					(rand.nextFloat() - 0.5) * 0.02,
					-mtOff + rand.nextFloat() * 0.05,
					(rand.nextFloat() - 0.5) * 0.02,
					0, 64.0F, false
				);
			case NORTH ->
				world.spawnParticle(
					SMOKE,
					x + (rand.nextFloat() - 0.5) * 0.8,
					y + (rand.nextFloat() - 0.5) * 0.8,
					z - 0.25,
					(rand.nextFloat() - 0.5) * 0.02,
					(rand.nextFloat() - 0.5) * 0.02,
					mtOff + rand.nextFloat() * 0.05,
					0, 64.0F, false
				);
			case SOUTH ->
				world.spawnParticle(
					SMOKE,
					x + (rand.nextFloat() - 0.5) * 0.8,
					y + (rand.nextFloat() - 0.5) * 0.8,
					z + 0.25,
					(rand.nextFloat() - 0.5) * 0.02,
					(rand.nextFloat() - 0.5) * 0.02,
					-mtOff + rand.nextFloat() * 0.05,
					0, 64.0F, false
				);
			case WEST ->
				world.spawnParticle(
					SMOKE,
					x - 0.25,
					y + (rand.nextFloat() - 0.5) * 0.8,
					z + (rand.nextFloat() - 0.5) * 0.8,
					mtOff - rand.nextFloat() * 0.05,
					(rand.nextFloat() - 0.5) * 0.02,
					(rand.nextFloat() - 0.5) * 0.02,
					0, 64.0F, false
				);
			case EAST ->
				world.spawnParticle(
					SMOKE,
					x + 0.25,
					y + (rand.nextFloat() - 0.5) * 0.8,
					z + (rand.nextFloat() - 0.5) * 0.8,
					-mtOff + rand.nextFloat() * 0.05,
					(rand.nextFloat() - 0.5) * 0.02,
					(rand.nextFloat() - 0.5) * 0.02,
					0, 64.0F, false
				);
			default ->
				world.spawnParticle(
					SMOKE,
					x + (rand.nextFloat() - 0.5) * 0.8,
					y - 0.25F,
					z + (rand.nextFloat() - 0.5) * 0.8,
					(rand.nextFloat() - 0.5) * 0.02,
					mtOff - rand.nextFloat() * 0.05,
					(rand.nextFloat() - 0.5) * 0.02,
					0, 64.0F, false
				);
		}
	}
}
