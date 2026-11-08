package cookie.farlanders.core.features;

import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.projectile.Projectile;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.util.phys.HitResult;
import net.minecraft.core.world.World;

import static cookie.farlanders.core.Farlanders.FARLANDER_PEARL;

public class ProjectileFarlanderPearl extends Projectile {
	public ProjectileFarlanderPearl(World world) {
		super(world);
		this.modelItem = FARLANDER_PEARL;
	}

	public ProjectileFarlanderPearl(World world, Mob owner) {
		super(world, owner);
		this.modelItem = FARLANDER_PEARL;
	}

	public ProjectileFarlanderPearl(World world, double x, double y, double z) {
		super(world, x, y, z);
		this.modelItem = FARLANDER_PEARL;
	}

	@Override
	public void onHit(HitResult hitResult) {
		if (this.world.isClientSide) super.onHit(hitResult);
		this.owner.setPos(x, y + 2, z);
		this.owner.hurt(null, 5, DamageType.GENERIC);
		this.world.playSoundAtEntity(this.owner, this.owner, "farlanders:mob.farlander.slip", 1.0f, (random.nextFloat() - random.nextFloat()) * 0.2f - 1.0f);
		super.onHit(hitResult);
	}
}
