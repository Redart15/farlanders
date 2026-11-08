package cookie.farlanders.core.features;

import com.mojang.nbt.tags.CompoundTag;
import cookie.farlanders.extra.FarlandersConfig;
import cookie.farlanders.core.FarlanderUtils;
import net.minecraft.core.WeightedRandomLootObject;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.monster.MobMonster;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.NotNull;

import static cookie.farlanders.core.Farlanders.FARLANDER_LENS;
import static cookie.farlanders.core.Farlanders.FARLANDER_PEARL;
import static cookie.farlanders.core.FarlandersGlobals.MOD_ID;

public class MobFarlander extends MobMonster {
	public static final String FARLANDER_SLIP = "farlanders:mob.farlander.slip";
	private Player player;
	private boolean isAngry = false;
	private int soundTicks;
	private int ticksNotLooking;
	private int slipTimer;
	private int randomSlipTimer;

	public MobFarlander(World world) {
		super(world);
		this.setTextureIdentifier(MOD_ID, "farlander");
		this.scoreValue = 1000;
		this.setSize(0.6f, 2.5f);
		this.heartsHalvesLife = FarlandersConfig.cfg.getInt("Farlanders.farlanderHealth");
		this.moveSpeed = 0;
		this.mobDrops.add(new WeightedRandomLootObject(FARLANDER_LENS.getDefaultStack(), 0, 2));
	}

	private void smoke() {
		if (!this.world.isClientSide) {
			for (int i = 0; i < 20; ++i) {
				double motX = random.nextGaussian() * 0.02;
				double motY = random.nextGaussian() * 0.02;
				double motZ = random.nextGaussian() * 0.02;
				this.world.spawnParticle("smoke",
					x + (this.random.nextFloat() * this.bbWidth * 2.0F) - this.bbWidth,
					y + (this.random.nextFloat() * this.bbHeight),
					z + (this.random.nextFloat() * this.bbWidth * 2.0F) - this.bbWidth,
					motX, motY, motZ, 0, 64.0F, false
				);
			}
		}
	}

	public void randomlySlip(int randPosX, int randPosY, int randPosZ) {
		if (this.world.isClientSide) {
			return;
		}
		int randX;
		int randY;
		int randZ;
		// Check 5 times for a valid position
		if (!dead) {
			for (int i = 0; i < 5; i++) {
				randX = random.nextInt(randPosX) + MathHelper.floor(this.x);
				randY = random.nextInt(randPosY) + MathHelper.floor(this.y);
				randZ = random.nextInt(randPosZ) + MathHelper.floor(this.z);
				if (randY < 80
					&& world.isAirBlock(new TilePos(randX, MathHelper.floor(randY + bb.minY), randZ))
					&& world.getBlockLightValue(new TilePos(randX, randY, randZ)) <= 7
				) {
					this.world.playSoundAtEntity(player, this, FARLANDER_SLIP, 1.0f, (random.nextFloat() - random.nextFloat()) * 0.2f - 1.0f);
					this.smoke();
					this.setPos(randX, randY, randZ);
					this.smoke();
				}
			}
		}
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (this.world.isClientSide) {
			return;
		}
		this.player = this.world.getClosestPlayerToEntity(this, 64.0);
		this.slipCalm();
		this.slipBright();
		this.slipPlayer();
		this.canBeLooted();
		this.slipStuck();
	}

	private void slipPlayer() {
		if ((this.player == null) || !this.player.gamemode.hasHostileMobs()) {
			return;
		}

		if (this.isAngry) {
			this.lookAt(this.player, 30.0F, 30.0F);
			this.setTarget(this.player);
		}

		// A check to see if the Farlander is stared at.
		// If it is, then face the entity and set to angry.
		// Otherwise, it teleports a random distance closer and starts a 'not looking' timer.
		if (FarlanderUtils.isStaredAt(this, this.player)) {
			this.isAngry = true;
		} else if (!FarlanderUtils.isStaredAt(this, this.player) && this.isAngry && !this.dead) {
			++this.ticksNotLooking;
			if (this.soundTicks <= 0) {
				this.world.playSoundAtEntity(this.player, this.player, "farlanders:mob.farlander.whispering", 1.0f, 1.0f);
				this.soundTicks = 540;
			}
			double newX = (this.x + this.player.x) / 2.0;
			double newZ = (this.z + this.player.z) / 2.0;
			double newY = this.world.getHeightValue(MathHelper.floor(newX), MathHelper.floor(newZ)) + 0.5;

			// If the 'slip' timer hits 0, then this tries to spawn an item, spawn smoke particles,
			// teleport, and then reset the timer back to 80 ticks.
			if (this.slipTimer <= 0) {
				this.smoke();
				// This should only spawn pearls if the random int is 0.
				if (this.random.nextInt(12) == 0) {
					this.dropItem(FARLANDER_PEARL.id, 1);
				}
				this.setPos(newX, newY, newZ);
				this.smoke();
				this.world.playSoundAtEntity(player, player, FARLANDER_SLIP, 1.0f, (random.nextFloat() - random.nextFloat()) * 0.2f - 1.0f);
				this.slipTimer = 30;
			}
		}
	}

	private void slipCalm() {
		// If it isn't angry and the random teleport timer surpasses 600, reset timer and slip away randomly.
		if (!this.isAngry && this.randomSlipTimer++ >= 600) {
			this.randomlySlip(16, 16, 16);
			this.randomSlipTimer = 0;
		}
	}

	private void slipBright() {
		// Checks the light level in a 3x3 area around the Farlander.
		// If it's above light level 7, then do damage and randomly slip away.
		for (double _x = this.x - 1; _x <= this.x + 1; _x++) {
			for (double _z = this.z - 1; _z <= this.z + 1; _z++) {
				if (this.world.getBlockLightValue(new TilePos(MathHelper.floor(_x), MathHelper.floor(y), MathHelper.floor(_z))) > 7) {
					this.hurt(null, 5, DamageType.GENERIC);
					this.randomlySlip(16, 16, 16);
				}
			}
		}
	}

	private void canBeLooted() {
		// A check to see if the 'ticksNotLooking' int is above or equal to 600.
		// If it is, then set angry to 0 and reset the int back to 0.
		if (this.target != null) {
			if (this.slipTimer > 0) {
				this.slipTimer--;
			}
			if (this.soundTicks > 0) {
				this.soundTicks--;
			}
			if (this.distanceToSqr(this.target) <= 8.0) {
				this.target.hurt(this, 3, DamageType.COMBAT);
				this.attackTime = 20;
			}
			if (this.ticksNotLooking >= 600 && this.isAngry && distanceToSqr(this.target) >= 256.0) {
				this.ticksNotLooking = 0;
				this.isAngry = false;
			}
		}
	}

	private void slipStuck() {
		// Stuck prevention, in case they slip into blocks.
		if (this.world.getBlockType(new TilePos(MathHelper.floor(this.x), MathHelper.floor(this.bb.minY + this.bb.maxY), MathHelper.floor(this.z))) != Blocks.AIR) {
			this.setPos(this.x, this.bb.minY + 1, this.z);
		}
	}

	@Override
	public boolean hurt(Entity attacker, int i, DamageType type) {
		if (this.world.isClientSide) {
			return false;
		}
		if (super.hurt(attacker, i, type)) {
			// If the damage type is 'combat'...
			// Then teleport closer to the attacker by a random amount and play whispers.
			if (type == DamageType.COMBAT) {
				this.attackStrength += FarlandersConfig.cfg.getInt("Farlanders.farlanderDamage");
			}
			if (attacker != null && !dead) {
				double newX = this.random.nextFloat() * (attacker.x - x) + attacker.x;
				double newZ = this.random.nextFloat() * (attacker.z - z) + player.z;
				double newY = this.world.getHeightValue(MathHelper.floor(newX), MathHelper.floor(newZ)) + 0.5;
				if (this.random.nextInt(8) == 0) {
					this.world.playSoundAtEntity(player, this, "mob.chickenplop", 1.0f, (random.nextFloat() - random.nextFloat()) * 0.2f - 1.0f);
					this.dropItem(FARLANDER_PEARL.id, 1);
				}
				this.smoke();
				this.setPos(newX, newY, newZ);
				this.smoke();
				this.world.playSoundAtEntity(player, attacker, FARLANDER_SLIP, 1.0f, (random.nextFloat() - random.nextFloat()) * 0.2f - 1.0f);
			}
			return true;
		}
		return false;
	}

	@Override
	public void knockBack(Entity entity, int i, double d, double d1) {
		// cannot be knockbacked
	}

	@Override
	protected String getHurtSound() {
		return null; // no hurt sound
	}

	@Override
	protected String getDeathSound() {
		return null; // no death sound
	}

	@Override
	protected Entity findPlayerToAttack() {
		return this.isAngry ? this.player : null;
	}

	@Override
	protected void jump() {
		// cannot jump
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return 2;
	}

	@Override
	public void addAdditionalSaveData(@NotNull CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putByte("isAngry", isAngry() ? (byte) 1 : (byte) 0);
		tag.putInt("TicksNotLooking", this.ticksNotLooking);
		tag.putInt("SlipTimer", this.slipTimer);
		tag.putInt("RandomSlipTimer", this.randomSlipTimer);
	}

	@Override
	public void readAdditionalSaveData(@NotNull CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		setAngry(tag.getByte("IsAngry") == (byte) 1);
		this.ticksNotLooking = tag.getInteger("TicksNotLooking");
		this.slipTimer = tag.getInteger("SlipTimer");
		this.randomSlipTimer = tag.getInteger("RandomSlipTimer");
	}

	public boolean isAngry() {
		return this.isAngry;
	}

	public void setAngry(boolean angry) {
		this.isAngry = angry;
	}
}
