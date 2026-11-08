package cookie.farlanders.extra.mixin;

import cookie.farlanders.extra.interfaces.IPlayerSonar;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.HumanArmorShape;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.ContainerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static cookie.farlanders.core.Farlanders.FARLANDER_GOGGLES;

@Mixin(value = Player.class, remap = false)
public abstract class PlayerMixin implements IPlayerSonar {

	@Shadow
	public ContainerInventory inventory;

	@Unique
	private int durabilityTimer = 0;

	@Inject(method = "onLivingUpdate", at = @At("TAIL"))
	private void whileWearingGlasses(CallbackInfo ci) {
		Player player = (Player) (Object) this;
		if (player.world.isClientSide) {
			return;
		}
		if (this.farlanders$hasSonar()) {
			if (durabilityTimer++ >= 100) {
				this.durabilityTimer = 0;
				player.damageArmor(1, HumanArmorShape.HEAD);
			}
		} else {
			this.durabilityTimer = 0;
		}
	}

	@Override
	public boolean farlanders$hasSonar() {
		int slotIndex = HumanArmorShape.HEAD.getSlotIndex();
		ItemStack stack = inventory.armorInventory[slotIndex];
		return stack != null && stack.itemID == FARLANDER_GOGGLES.id;
	}
}
