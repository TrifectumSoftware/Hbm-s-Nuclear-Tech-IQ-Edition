package com.hbm.inventory.fluid.trait;

import com.hbm.config.VersatileConfig;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;

import api.hbm.fluidmk2.IFillableItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.util.Constants;

public class Injectables {

	public static void process(EntityLivingBase entity, FluidType type, float amount, float intensity, boolean isInjection) {

		if(entity == null || type == null) return;

		if(isInjection && VersatileConfig.hasPotionSickness(entity))
			return;

		if(isInjection && type.isCorrosive()) {
			entity.attackEntityFrom(DamageSource.generic, amount * intensity * 0.5F);
		}

		if(type.hasTrait(FT_Consumable.class)) {
			type.getTrait(FT_Consumable.class).apply(entity, intensity);
		}

		if(type.hasTrait(FT_Drug.class)) {
			type.getTrait(FT_Drug.class).apply(entity, intensity);
		}

		if(type == Fluids.ENDERJUICE) {
			double x = entity.posX + entity.getRNG().nextGaussian() * 16;
			double y = entity.posY;
			double z = entity.posZ + entity.getRNG().nextGaussian() * 16;
			for(int i = 0; i < 10; i++) {
				if(entity.worldObj.getTopSolidOrLiquidBlock((int)x, (int)z) > 0) break;
				y++;
			}
			entity.setPositionAndUpdate(x, y, z);
		}
		if(type == Fluids.XPJUICE && entity instanceof EntityPlayer) {
			((EntityPlayer) entity).addExperience((int)(10 * intensity));
		}

		if(isInjection) {
			int sickness = type == Fluids.SUPER_STIMPAK ? 15 : 5;
			VersatileConfig.applyPotionSickness(entity, sickness);
		}
	}

	public static void injectEntity(ItemStack stack, EntityLivingBase target, EntityPlayer player, int dose, float intensity) {
		FluidType type = IFillableItem.getFluidType(stack);
		Injectables.process(target, type, dose, intensity, true);
		applyStackPayload(stack, target, intensity);
		short fill = IFillableItem.getFluidFill(stack);
		short newFill = (short) (fill - dose);
		if(newFill == 0) IFillableItem.setFluidFill(stack, Fluids.NONE, (short) 0);
		else IFillableItem.setFluidFill(stack, type, newFill);
		player.worldObj.playSoundAtEntity(player, "hbm:item.syringe", 1.0F, 1.0F);
	}

	public static void applyStackPayload(ItemStack stack, EntityLivingBase target, float intensity) {
		if(stack == null || !stack.hasTagCompound() || target == null) return;
		NBTTagCompound nbt = stack.stackTagCompound;
		FluidType type = IFillableItem.getFluidType(stack);

		if(type != null && type.hasTrait(FT_Pathogen.class) && nbt.hasKey("pathogen")) {
			applyPathogenPayload(target, nbt.getTagList("pathogen", Constants.NBT.TAG_COMPOUND), intensity);
		}

		if(type != null && type.hasTrait(FT_Pharma.class) && nbt.hasKey("pharma")) {
			FT_Pharma.apply(stack, target, intensity);
		}
	}

	public static void applyPathogenPayload(EntityLivingBase target, NBTTagList list, float intensity) {
		if(target == null || list == null) return;
		for(int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound entry = list.getCompoundTagAt(i);
			String frameId = entry.getString("frame");
			if(frameId.isEmpty()) continue;
			String genome = null;
			NBTTagCompound mut = entry.hasKey("mut") ? entry.getCompoundTag("mut") : null;
			if(mut != null && mut.hasKey("genome")) {
				genome = mut.getString("genome");
				// Re-register the frame from its stored def so pre-restart wild syringes still work.
				com.hbm.handler.contagion.DiseaseRegistry.restore(frameId, mut);
			}
			FT_Pathogen.infect(target, frameId, entry.hasKey("amount") ? entry.getFloat("amount") : 10F, intensity, genome);
		}
	}
}
