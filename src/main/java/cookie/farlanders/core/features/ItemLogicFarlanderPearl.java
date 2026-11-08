package cookie.farlanders.core.features;

import net.minecraft.core.block.entity.TileEntityActivator;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.IDispensable;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class ItemLogicFarlanderPearl extends Item implements IDispensable {
	public ItemLogicFarlanderPearl(String translationKey, String namespaceId, int id) {
		super(translationKey, namespaceId, id);
		setMaxStackSize(8);
	}

	@Override
	public @Nullable ItemStack onUse(@NotNull ItemStack selfStack, @NotNull World world, @NotNull Player player) {
		selfStack.consumeItem(player);
		world.playSoundAtEntity(player, player, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
		if (!world.isClientSide) {
			world.entityJoinedWorld(new ProjectileFarlanderPearl(world, player));
		}
		return selfStack;
	}

	@Override
	public void onUseByActivator(@NotNull ItemStack selfStack, @NotNull World world, @NotNull TileEntityActivator activator, @NotNull Random random, @NotNull TilePosc blockPos, @NotNull Direction direction, double offX, double offY, double offZ) {
		ProjectileFarlanderPearl projectile = new ProjectileFarlanderPearl(world, blockPos.x() + offX, blockPos.y() + offY, blockPos.z() + offZ);
		projectile.setHeading(direction.offsetX() * 0.6, direction.offsetY() == 0 ? 0.1 : direction.offsetY() * 0.6, direction.offsetZ() * 0.6, 1.1F, 6.0F);
		world.entityJoinedWorld(projectile);
		--selfStack.stackSize;
	}

	@Override
	public void onDispensed(@NotNull ItemStack selfStack, @NotNull World world, @NotNull Random random, @NotNull Direction direction, double x, double y, double z) {
		ProjectileFarlanderPearl projectile = new ProjectileFarlanderPearl(world, x, y, z);
		projectile.setHeading(direction.offsetX(), (double)direction.offsetY() + 0.1, direction.offsetZ(), 1.1F, 6.0F);
		world.entityJoinedWorld(projectile);
	}
}
