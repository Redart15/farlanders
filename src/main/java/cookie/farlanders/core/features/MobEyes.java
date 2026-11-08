package cookie.farlanders.core.features;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.monster.MobMonster;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.collection.NamespaceID;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.world.World;
import net.minecraft.core.world.type.WorldTypes;
import cookie.farlanders.core.FarlanderUtils;

import static cookie.farlanders.core.FarlandersGlobals.MOD_ID;

public class MobEyes extends MobMonster {
	private int stareTimer = 0;
	private int soundTimer = 0;
	private boolean found = false;

	public MobEyes(World world) {
		super(world);
		textureIdentifier = NamespaceID.fromPool(MOD_ID, "eyes");
		scoreValue = 0;
		setSize(1.4f, 1.4f);
		heartsHalvesLife = 1;
		moveSpeed = 0;
		attackStrength = 0;
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (this.world.isClientSide) {
			return;
		}
		Player player = this.world.getClosestPlayerToEntity(this, 16.0);
		if (player == null) {
			return;
		}
		this.lookAt(player, 30.0F, 30.0F);
		if (this.found) {
			++this.stareTimer;
		}
		if (FarlanderUtils.isStaredAt(this, player) && player.getGamemode().hasHostileMobs()) {
			this.found = true;
			if (this.soundTimer == 0) {
				this.world.playSoundAtEntity(null, this, "ambient.cave.cave", 1.0f, 1.0f);
				this.soundTimer = 1;
			}
			if (this.stareTimer > 15) {
				this.remove();
			}
		}
	}

	private boolean validWorldType() {
		if (this.world.isClientSide) {
			return false;
		}
        return this.world.getWorldType() != WorldTypes.FLAT ||
			this.world.getWorldType() != WorldTypes.EMPTY ||
			this.world.getWorldType() != WorldTypes.DRIFT_DEFAULT;
    }

	@Override
	protected Entity findPlayerToAttack() {
		return null;
	}


	@Override
	public boolean canSpawnHere() {
		return super.canSpawnHere() && validWorldType() && y > (double) world.getHeightBlocks() / 3;
	}
    @Override
	protected String getHurtSound() {
		return null;
	}

	@Override
	protected String getDeathSound() {
		return null;
	}

	@Override
	protected void jump() {
		// is not needed
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return 1;
	}

	@Override
	public boolean hurt(Entity attacker, int i, DamageType type) {
		return false;
	}
}
