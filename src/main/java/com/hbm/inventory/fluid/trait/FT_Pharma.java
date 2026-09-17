package com.hbm.inventory.fluid.trait;

import java.io.IOException;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.handler.contagion.PharmaProfile;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumChatFormatting;

public class FT_Pharma extends FluidTrait {

	@Override
	public void addInfoHidden(List<String> info) {
		info.add(EnumChatFormatting.GREEN + "[" + I18nUtil.resolveKey("hbmfluid.trait.pharma") + "]");
	}

	public static void apply(ItemStack stack, EntityLivingBase target, float intensity) {
		PharmaProfile profile = stack.hasTagCompound() && stack.stackTagCompound.hasKey("pharma") ? PharmaProfile.readFromNBT(stack.stackTagCompound.getCompoundTag("pharma")) : null;
		apply(profile, target, intensity);
	}

	public static void apply(PharmaProfile profile, EntityLivingBase target, float intensity) {
		if(profile == null || target == null) return;

		com.hbm.extprop.HbmBloodstreamProps.getData(target).applyCure(profile, intensity);

		if(intensity > 1.5F) {
			int tier = Math.min(3, (int) ((intensity - 1.5F) / 0.5F));
			target.addPotionEffect(new PotionEffect(Potion.confusion.id, 200, tier));
			target.addPotionEffect(new PotionEffect(Potion.hunger.id, 400, tier));
		}
	}

	@Override public void serializeJSON(JsonWriter writer) throws IOException { }

	@Override public void deserializeJSON(JsonObject obj) { }
}
