package cookie.farlanders.client;

import cookie.farlanders.core.features.MobFarlander;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.client.render.renderer.BlendFactor;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.State;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.core.util.helper.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.useless.dragonfly.models.entity.BoneTransform;
import org.useless.dragonfly.models.entity.StaticEntityModel;


@Environment(EnvType.CLIENT)
public class MobRendererFarlander extends MobRenderer<MobFarlander> {
	public MobRendererFarlander() {
		super(0.5F);
	}

	@Override
	protected @Nullable StaticEntityModel getAndSetupModelForLayer(@NotNull MobFarlander entity, float brightness, float partialTick, int layer) {
		if (layer == 1) {
			this.bindTexture("/assets/farlanders/textures/entity/eyes/eyes.png");
			GLRenderer.setLightmapCoord2i(15, 15);
			GLRenderer.enableState(State.BLEND);
			GLRenderer.setBlendFunc(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA);
			GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		}

		StaticEntityModel model = this.getModel("main");
		model.resetBones();

		final float bodyYaw = getBodyYaw(entity, partialTick);
		final float headYaw = getHeadYaw(entity, partialTick) - bodyYaw;
		final float headPitch = getHeadPitch(entity, partialTick);
		final float limbPitch = getLimbPitch(entity, partialTick);

		final BoneTransform head = model.getTransform("head");
		final BoneTransform armLeft = model.getTransform("armLeft");
		final BoneTransform armRight = model.getTransform("armRight");
		final BoneTransform legLeft = model.getTransform("legLeft");
		final BoneTransform legRight = model.getTransform("legRight");

		head.rotX = headPitch;
		head.rotY = headYaw;

		armRight.rotZ += MathHelper.cos(limbPitch * 0.09F) * 0.05F + 0.05F;
		armRight.rotX += MathHelper.sin(limbPitch * 0.067F) * 0.05F;
		armLeft.rotZ -= MathHelper.cos(limbPitch * 0.09F) * 0.05F + 0.05F;
		armLeft.rotX -= MathHelper.sin(limbPitch * 0.067F) * 0.05F;

		legLeft.rotZ = -2.5 * MathHelper.DEG_TO_RAD;
		legRight.rotZ = 2.5 * MathHelper.DEG_TO_RAD;

		return model;
	}

	@Override
	protected int maxRenderLayer(@NotNull MobFarlander entity) {
		return 1;
	}

	@Override
	public void renderPreview(@NotNull TessellatorGeneral tessellator, @NotNull MobFarlander entity, double x, double y, double z, float yaw, float partialTick) {
		super.renderPreview(tessellator, entity, x, y - 2.0, z, yaw, partialTick);
	}
}
