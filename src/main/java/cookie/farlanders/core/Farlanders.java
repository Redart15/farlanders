package cookie.farlanders.core;

import cookie.farlanders.core.features.BlockLogicSmoker;
import cookie.farlanders.core.features.ItemLogicFarlanderPearl;
import cookie.farlanders.core.features.MobEyes;
import cookie.farlanders.core.features.MobFarlander;
import cookie.farlanders.extra.FarlandersConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.EntityDispatcher;
import net.minecraft.core.enums.HumanArmorShape;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemArmor;
import net.minecraft.core.item.Items;
import net.minecraft.core.item.material.ArmorMaterial;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.util.collection.NamespaceID;
import turniplabs.halplibe.HalpLibe;
import turniplabs.halplibe.event.defs.CommonEvents;
import turniplabs.halplibe.helper.*;
import turniplabs.halplibe.util.dependency.Key;

import static cookie.farlanders.core.FarlandersGlobals.MOD_ID;

@SuppressWarnings({"java:S1104", "java:S1444", "java:S3008", "java:S2696"})
public class Farlanders implements ModInitializer {
	private static int baseIDBlock = FarlandersConfig.cfg.getInt("IDs.startingBlockID");
	private static int baseIDItem = FarlandersConfig.cfg.getInt("IDs.startingItemID");

	public static Item FARLANDER_LENS;
	public static Item FARLANDER_PEARL;
	public static Item FARLANDER_GOGGLES;
	public static Block<?> SMOKER;
	private static final ArmorMaterial material = new ArmorMaterial(NamespaceID.fromPool(MOD_ID, "goggles"), 240);

	@Override
	public void onInitialize() {
		HalpLibe.registerMod(MOD_ID);
		Key key = Key.of(MOD_ID);
		CommonEvents.AFTER_ITEM_INIT.listen(key, this::afterItemInit);
		CommonEvents.AFTER_BLOCK_INIT.listen(key, this::afterBlockInit);
		CommonEvents.RECIPES_READY.listen(key, this::onRecipesReady);
		CommonEvents.RECIPES_NAMESPACE_INIT.listen(key, this::initNamespaces);
		FarlandersGlobals.LOGGER.info("Farlanders has been initialized.");
		EntityDispatcher.getInstance().addMapping(MobFarlander.class, NamespaceID.fromPool(MOD_ID, "farlander"), MobFarlander::new, "guidebook.section.mob.farlanders.farlander");
		EntityDispatcher.getInstance().addMapping(MobEyes.class, NamespaceID.fromPool(MOD_ID, "eyes"), MobEyes::new, "guidebook.section.mob.farlanders.eyes");
	}

	public void afterItemInit() {
		FARLANDER_LENS = new Item("lens", FarlandersGlobals.formatName("lens", "item"), baseIDItem++);
		FARLANDER_PEARL = new ItemLogicFarlanderPearl("pearl", FarlandersGlobals.formatName("pearl", "item"), baseIDItem++);
		FARLANDER_GOGGLES = new ItemArmor<>("armor.helmet.goggles", FarlandersGlobals.formatName("armor_helmet_goggles", "item"), baseIDItem++, material, HumanArmorShape.HEAD);
	}

	public void afterBlockInit() {
		SMOKER = Blocks.register(
				FarlandersGlobals.formatTranslationKey("smoker"),
				FarlandersGlobals.formatName("block_smoker", "block"),
				baseIDBlock++,
				BlockLogicSmoker::new
			)
			.withSound(BlockSounds.STONE)
			.withHardness(2.0F)
			.withTags(BlockTags.MINEABLE_BY_PICKAXE);
	}

	public void onRecipesReady() {
		RecipeBuilder.Shaped(MOD_ID)
			.setShape("111", "212")
			.addInput('1', Items.INGOT_GOLD)
			.addInput('2', FARLANDER_LENS)
			.create("goggles", FARLANDER_GOGGLES);

		RecipeBuilder.Shaped(MOD_ID)
			.setShape("121")
			.addInput('1', "minecraft:cobblestones")
			.addInput('2', FARLANDER_PEARL)
			.create("smoker", SMOKER);
	}

	public void initNamespaces() {
		RecipeBuilder.initNameSpace(MOD_ID);
	}
}
