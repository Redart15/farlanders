package cookie.farlanders.client;

import cookie.farlanders.core.Farlanders;
import cookie.farlanders.core.features.MobEyes;
import cookie.farlanders.core.features.MobFarlander;
import cookie.farlanders.extra.FarlandersConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.mobs.MobInfoRegistry;
import net.minecraft.client.render.EntityRendererDispatcher;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.entity.EntityRendererSprite;
import net.minecraft.client.render.item.model.ItemModelDispatcher;
import net.minecraft.client.render.item.model.ItemModelStandard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.event.defs.ClientEvents;
import turniplabs.halplibe.util.dependency.Key;

import static cookie.farlanders.core.FarlandersGlobals.MOD_ID;
import static net.minecraft.client.render.block.model.BlockModelDispatcher.loadDataModel;

@Environment(EnvType.CLIENT)
public class FarlandersClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		LOGGER.info("Farlanders Client has been initialized.");
		Key key = Key.of(MOD_ID);
		ClientEvents.AFTER_CLIENT_START.listen(key,this::afterClientStart);
		ClientEvents.BLOCK_MODEL_RELOAD.listen(key,this::initBlockModels);
		ClientEvents.ITEM_MODEL_RELOAD.listen(key,this::initItemModels);
		ClientEvents.ENTITY_RENDERER_RELOAD.listen(key, this::initEntityModels);
	}

	public void afterClientStart() {
		MobInfoRegistry.register(MobFarlander.class,
			"guidebook.section.mob.farlanders.farlander.name",
			"guidebook.section.mob.farlanders.farlander.desc",
			FarlandersConfig.cfg.getInt("Farlanders.farlanderHealth"),
			1000,
			new MobInfoRegistry.MobDrop[]{
				new MobInfoRegistry.MobDrop(Farlanders.FARLANDER_LENS.getDefaultStack(), 1.0F, 1, 2),
				new MobInfoRegistry.MobDrop(Farlanders.FARLANDER_PEARL.getDefaultStack(), 12.0F, 1, 1)
			});
	}


	public void initBlockModels(BlockModelDispatcher blockModelDispatcher) {
		blockModelDispatcher.addDispatch(new BlockModelSmoker<>(Farlanders.SMOKER, loadDataModel("farlanders:block/smoker/smoker_bottom")));
	}


	public void initItemModels(ItemModelDispatcher itemModelDispatcher) {
		itemModelDispatcher.addDispatch(new ItemModelStandard(Farlanders.FARLANDER_LENS).setFullBright());
		itemModelDispatcher.addDispatch(new ItemModelStandard(Farlanders.FARLANDER_PEARL));
		itemModelDispatcher.addDispatch(new ItemModelStandard(Farlanders.FARLANDER_GOGGLES));
	}


	public void initEntityModels(EntityRendererDispatcher entityRenderDispatcher) {
		entityRenderDispatcher.assignRenderer(MobEyes.class, new MobRendererEyes());
		entityRenderDispatcher.assignRenderer(MobFarlander.class, new MobRendererFarlander());
		entityRenderDispatcher.assignRenderer(MobEyes.class, new EntityRendererSprite<>(Farlanders.FARLANDER_PEARL));
	}
}
