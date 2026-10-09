package com.hbm.handler.overlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.lwjgl.opengl.Display;

import com.hbm.config.GeneralConfig;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.inventory.FluidContainer;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.armor.Infotab;

import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;

@SideOnly(Side.CLIENT)
public class InfotabHandler {

	public static ItemStack hovered;
	public static Fluid hoveredFluid;
	public static String hoveredName;
	public static String hoveredMod;
	public static EntityLivingBase hoveredMob;
	public static final List<String> hoveredLines = new ArrayList<>();

	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {

		if(event.phase != Phase.END) return;

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;

		if(player == null || mc.currentScreen != null || !Display.isActive() || !HbmPlayerProps.getData(player).enableHUD || Infotab.getWorn(player) == null && !GeneralConfig.infotabAlwaysOn) {
			clear();
			return;
		}

		clear();

		MovingObjectPosition mop = mc.objectMouseOver;

		if(mop == null || mop.typeOfHit == MovingObjectType.BLOCK) {
			boolean submerged = player.isInWater() || player.isInsideOfMaterial(Material.lava);

			if(!submerged || mop == null) {
				double reach = mc.playerController.getBlockReachDistance();
				Vec3 from = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
				Vec3 look = player.getLook(1F);
				Vec3 to = Vec3.createVectorHelper(from.xCoord + look.xCoord * reach, from.yCoord + look.yCoord * reach, from.zCoord + look.zCoord * reach);
				MovingObjectPosition hit = mc.theWorld.func_147447_a(from, to, true, false, false);

				if(hit != null) mop = hit;
			}
		}

		if(mop == null) return;

		if(mop.typeOfHit == MovingObjectType.BLOCK) {
			Block block = mc.theWorld.getBlock(mop.blockX, mop.blockY, mop.blockZ);
			Fluid fluid = fluid(block);

			if(fluid != null) {
				hoveredFluid = fluid;
				hovered = fluidContainer(fluid);
				hoveredName = fluidName(fluid);
				hoveredMod = modName(GameRegistry.findUniqueIdentifierFor(block));
				return;
			}

			ItemStack stack = block.getPickBlock(mop, mc.theWorld, mop.blockX, mop.blockY, mop.blockZ);

			if(stack == null || stack.getItem() == null) {
				Item item = Item.getItemFromBlock(block);
				if(item == null) return;
				stack = new ItemStack(item, 1, block.getDamageValue(mc.theWorld, mop.blockX, mop.blockY, mop.blockZ));
			}

			hovered = stack;
			hoveredName = stack.getDisplayName();
			hoveredMod = modName(GameRegistry.findUniqueIdentifierFor(stack.getItem()));

			int meta = mc.theWorld.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ);
			TileEntity te = mc.theWorld.getTileEntity(mop.blockX, mop.blockY, mop.blockZ);

			if(te != null && MachineHover.isMachine(te)) {
				hoveredLines.addAll(MachineHover.info(te, true));
			} else {
				String tool = block.getHarvestTool(meta);
				float hardness = block.getBlockHardness(mc.theWorld, mop.blockX, mop.blockY, mop.blockZ);

				if(tool != null) {
					hoveredLines.add("Harvest: " + Character.toUpperCase(tool.charAt(0)) + tool.substring(1) + levelName(block.getHarvestLevel(meta)));
				} else if(hardness > 0F) {
					hoveredLines.add("Hardness: " + hardness);
				}
			}

			return;
		}

		if(mop.typeOfHit == MovingObjectType.ENTITY && mop.entityHit != null) {
			Entity entity = mop.entityHit;
			hoveredMob = entity instanceof EntityLivingBase ? (EntityLivingBase) entity : null;

			if(entity instanceof EntityItem) {
				ItemStack stack = ((EntityItem) entity).getEntityItem();
				hovered = stack;
				hoveredName = stack.getDisplayName();
				hoveredMod = modName(GameRegistry.findUniqueIdentifierFor(stack.getItem()));
				return;
			}

			hovered = spawnEgg(entity);
			hoveredName = entity.getCommandSenderName();
			hoveredMod = entityModName(entity);
		}
	}

	private static void clear() {
		hovered = null;
		hoveredFluid = null;
		hoveredName = null;
		hoveredMod = null;
		hoveredMob = null;
		hoveredLines.clear();
	}

	private static String levelName(int level) {

		switch(level) {
		case 0: return " (Wood)";
		case 1: return " (Stone)";
		case 2: return " (Iron)";
		case 3: return " (Diamond)";
		default: return "";
		}
	}

	private static Fluid fluid(Block block) {

		Fluid fluid = FluidRegistry.lookupFluidForBlock(block);
		if(fluid != null) return fluid;
		if(!block.getMaterial().isLiquid()) return null;

		String name = block.getUnlocalizedName();
		if(name.startsWith("tile.")) name = name.substring(5);
		if(name.startsWith("flowing_")) name = name.substring(8);
		if(name.endsWith("_flowing")) name = name.substring(0, name.length() - 8);

		return FluidRegistry.getFluid(name);
	}

	private static ItemStack spawnEgg(Entity entity) {

		int id = EntityList.getEntityID(entity);
		if(!EntityList.entityEggs.containsKey(id)) return null;

		return new ItemStack(Items.spawn_egg, 1, id);
	}

	private static ItemStack fluidContainer(Fluid fluid) {

		for(FluidContainerRegistry.FluidContainerData data : FluidContainerRegistry.getRegisteredFluidContainerData()) {
			if(data.fluid != null && data.fluid.getFluid() == fluid) return data.filledContainer.copy();
		}

		FluidType type = fluidType(fluid);

		if(type != null) {
			for(FluidContainer container : com.hbm.inventory.FluidContainerRegistry.allContainers) {
				if(container.type == type) return container.fullContainer.copy();
			}
		}

		return null;
	}

	private static FluidType fluidType(Fluid fluid) {

		if(fluid == FluidRegistry.WATER) return Fluids.WATER;
		if(fluid == FluidRegistry.LAVA) return Fluids.LAVA;

		FluidType type = FluidType.getEnumFromName(fluid.getName().replace("_fluid", "").toUpperCase(Locale.US));
		return type == Fluids.NONE ? null : type;
	}

	private static String fluidName(Fluid fluid) {

		FluidType type = fluidType(fluid);

		if(type != null) {
			String name = type.getLocalizedName();
			if(name != null && !name.startsWith("hbmfluid.")) return name;
		}

		String name = fluid.getLocalizedName();
		if(name != null && !name.startsWith("fluid.")) return name;

		name = fluid.getName().replace("_fluid", "").replace('_', ' ');
		return name.isEmpty() ? name : Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

	private static String modName(GameRegistry.UniqueIdentifier uid) {

		if(uid == null) return "Minecraft";

		ModContainer mod = Loader.instance().getIndexedModList().get(uid.modId);
		return mod != null ? mod.getName() : uid.modId;
	}

	private static String entityModName(Entity entity) {

		EntityRegistry.EntityRegistration reg = EntityRegistry.instance().lookupModSpawn(entity.getClass(), true);
		if(reg == null || reg.getContainer() == null) return "Minecraft";

		return reg.getContainer().getName();
	}

	public static void openNEI(ItemStack stack, boolean uses) {

		if(!Loader.isModLoaded("NotEnoughItems")) return;

		try {
			if(uses) GuiUsageRecipe.openRecipeGui("item", stack.copy());
			else GuiCraftingRecipe.openRecipeGui("item", stack.copy());
		} catch(Throwable ex) { }
	}
}
