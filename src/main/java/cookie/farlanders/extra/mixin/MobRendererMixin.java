package cookie.farlanders.extra.mixin;

import cookie.farlanders.extra.interfaces.IPlayerSonar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.client.render.renderer.BlendFactor;
import net.minecraft.client.render.renderer.CompareFunc;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.State;
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

		this.loadEntityTexture(entity);
		GLRenderer.pushFrame();
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

//	@Inject(
//		method = "render(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/entity/Mob;DDDFF)V",
//		at = @At(
//			value = "INVOKE",
//			target = "Lnet/minecraft/client/render/entity/MobRenderer;renderAdditional(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/entity/Mob;F)V"
//		)
//	)
//	private void farlanders_outline(@NotNull TessellatorGeneral tessellator, @NotNull T entity, double x, double y, double z, float yaw, float partialTick, CallbackInfo ci) {
//		if (!farlanders_shouldRenderOutline(entity)) return;
//
//		float alpha = Math.max(0.2F, (float) entity.getHealth() / entity.getMaxHealth());
//		int passes = 8;
//		// preRenderTransform scales by 0.0625, so convert blocks to model units
//		float offset = 0.05f / 0.0625f;
//
//		this.loadEntityTexture(entity);
//
//		GLRenderer.pushFrame();
//		GLRenderer.enableState(State.BLEND);
//		GLRenderer.setBlendFunc(BlendFactor.SRC_ALPHA, BlendFactor.ONE);
//		GLRenderer.setDepthFunc(CompareFunc.ALWAYS);
//		GLRenderer.setDepthMask(false);
//		GLRenderer.setColor4f(0.0F, 0.5F, 1.0F, alpha);
//
//		for (int i = 0; i < passes; i++) {
//			float angle = (float) (i * 2 * Math.PI / passes);
//
//			GLRenderer.pushFrame();
//			GLRenderer.modelM4f().translate((float) Math.cos(angle) * offset, 0.0F, (float) Math.sin(angle) * offset);
//			for (StaticEntityModel model : this.setupModels) {
//				if (model != null) model.render();
//			}
//			GLRenderer.popFrame();
//		}
//
//		GLRenderer.setDepthMask(true);
//		GLRenderer.setDepthFunc(CompareFunc.LESS_EQUAL);
//		GLRenderer.setBlendFunc(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA);
//		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
//		GLRenderer.popFrame();
//	}

//	@Inject(method = "render(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/entity/Mob;DDDFF)V", at = @At("HEAD"))
//	private void farlanders_render(@NotNull TessellatorGeneral tessellator, @NotNull T entity, double x, double y, double z, float yaw, float partialTick, CallbackInfo ci) {
//		if (farlanders_shouldRenderOutline(entity)) {
//			GL11.glPushMatrix();
//			GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
//			this.loadEntityTexture(entity);
//			GL11.glDisable(GL11.GL_DEPTH_TEST);
//			GL11.glDisable(GL11.GL_LIGHTING);
//			GL11.glEnable(GL11.GL_BLEND);
//			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
//			// Outline color
//			GL11.glColor4f(0.0F, 0.5F, 1.0F, Math.max(0.2F, (float) entity.getHealth() / entity.getMaxHealth()));
//
//			GL11.glTranslated(x, y + 1.5, z);
//			GL11.glRotatef(180.0f, 1.0f, 0.0f, 0.0f);
//			// Multiple render passes with slight offsets
//			float offset = 0.05f;
//			int passes = 8;
//
//			for (int i = 0; i < passes; i++) {
//				GL11.glPushMatrix();
//
//				// Create an offset in different directions
//				float angle = (float)(i * 2 * Math.PI / passes);
//				float offsetX = (float)(Math.cos(angle) * offset);
//				float offsetZ = (float)(Math.sin(angle) * offset);
//				GL11.glTranslatef(offsetX, 0, offsetZ);
//
//				// Now render animations
//				float bodyYaw = entity.yBodyRotO + (entity.yBodyRot - entity.yBodyRotO) * partialTick;
//				float headYaw = entity.yRotO + (entity.yRot - entity.yRotO) * partialTick;
//				float headPitch = entity.xRotO + (entity.xRot - entity.xRotO) * partialTick;
//				float limbSway = this.getLimbSwing(entity, partialTick);
//				float walkSpeed = entity.walkAnimSpeedO + (entity.walkAnimSpeed - entity.walkAnimSpeedO) * partialTick;
//				float walkProgress = entity.walkAnimPos - entity.walkAnimSpeed * (1.0F - partialTick);
//				GL11.glRotatef(bodyYaw, 0.0f, 1.0f, 0.0f);
//				this.mainModel.render(walkProgress, walkSpeed, limbSway, headYaw - bodyYaw, headPitch, 0.0625F);
//				GL11.glPopMatrix();
//			}
//			// Restore GL state
//			GL11.glPopAttrib();
//			GL11.glPopMatrix();
//			GL11.glEnable(GL11.GL_DEPTH_TEST);
//		}
//	}

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
