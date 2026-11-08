package cookie.farlanders.extra.mixin;

import cookie.farlanders.extra.interfaces.IPlayerSonar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.client.render.renderer.*;
import net.minecraft.client.render.tessellator.Tessellator;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.useless.dragonfly.models.entity.StaticEntityModel;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(value = MobRenderer.class, remap = false)
public abstract class MobRendererMixin<T extends Mob> extends EntityRenderer<T> {
//	@Shadow
//	protected ModelBase mainModel;

	@Shadow
	protected abstract float getLimbSwing(T entity, float partialTick);

	@Shadow
	public abstract void loadEntityTexture(T entity);

	@Shadow
	@Final
	private @NotNull List<@Nullable StaticEntityModel> setupModels;

	@Shadow protected abstract void preRenderTransform(T entity, double x, double y, double z, float yaw, float partialTick);
	@Shadow protected abstract int maxRenderLayer(T entity);
	@Shadow protected abstract StaticEntityModel getAndSetupModelForLayer(T entity, float scale, float partialTick, int layer);

	@Inject(method = "render(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/entity/Mob;DDDFF)V", at = @At("HEAD"))
	private void farlanders_outline(@NotNull TessellatorGeneral tessellator, @NotNull T entity, double x, double y, double z, float yaw, float partialTick, CallbackInfo ci) {
		if (!farlanders_shouldRenderOutline(entity)) return;

		float alpha = Math.max(1.0F, (float) entity.getHealth() / entity.getMaxHealth());
		int maxLayer = this.maxRenderLayer(entity);
		int passes = 8;
		float offset = 0.05f;

//		this.loadEntityTexture(entity);
		this.renderDispatcher.textureManager.loadTexture("/assets/minecraft/textures/misc/white.png").bind();

		GLRenderer.pushFrame();
		GLRenderer.setLightmapCoord2i(15, 15);
		GLRenderer.setShader(Shaders.COLOR_WORLD);
		GLRenderer.enableState(State.BLEND);
		GLRenderer.disableState(State.CULL_FACE);
		GLRenderer.setBlendFunc(BlendFactor.SRC_ALPHA, BlendFactor.ONE);
		GLRenderer.setDepthFunc(CompareFunc.ALWAYS);   // still unverified enum value
		GLRenderer.setDepthMask(false);
		GLRenderer.setColor4f(0.0F, 0.5F, 1.0F, alpha);

		for (int i = 0; i < passes; i++) {
			float angle = (float) (i * 2 * Math.PI / passes);
			GLRenderer.pushFrame();
			this.preRenderTransform(entity, x + Math.cos(angle) * offset, y, z + Math.sin(angle) * offset, yaw, partialTick);
			for (int layer = 0; layer <= maxLayer; layer++) {
				try {
					GLRenderer.pushFrame();
					StaticEntityModel model = this.getAndSetupModelForLayer(entity, 1.0F, partialTick, layer);
					if (model != null) model.render();
					GLRenderer.popFrame();
				} catch (Exception ignored) {}
			}
			GLRenderer.popFrame();
		}

		GLRenderer.setDepthMask(true);
		GLRenderer.setDepthFunc(CompareFunc.LESS_EQUAL);
		GLRenderer.setBlendFunc(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA);
		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GLRenderer.popFrame();
	}

	@Unique
	private boolean farlanders_shouldRenderOutline(T entity) {
		Player player = Minecraft.getMinecraft().thePlayer;
		if (player == null || entity == null) {
			return false;
		}
		if (entity instanceof Player otherPlayer && otherPlayer.uuid.equals(player.uuid)) {
			return false;
		}
		boolean hasSonar = ((IPlayerSonar) player).farlanders$hasSonar();
		if (!hasSonar) {
			return false;
		}
		return player.distanceToSqr(entity) <= 1024.0;
	}
}
