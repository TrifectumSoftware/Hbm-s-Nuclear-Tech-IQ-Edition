package com.hbm.inventory.fluid.trait;

import java.io.IOException;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.handler.contagion.DiseaseDefinition;
import com.hbm.handler.contagion.DiseaseInstance;
import com.hbm.handler.contagion.DiseaseRegistry;
import com.hbm.handler.contagion.Genome;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;

public class FT_Pathogen extends FluidTrait {

	@Override
	public void addInfoHidden(List<String> info) {
		info.add(EnumChatFormatting.RED + "[" + I18nUtil.resolveKey("hbmfluid.trait.contagious") + "]");
	}

	public static void infect(EntityLivingBase target, String frameId, float dose, float intensity) {
		infect(target, frameId, dose, intensity, null);
	}

	public static void infect(EntityLivingBase target, String frameId, float dose, float intensity, String genome) {
		if(!com.hbm.config.ServerConfig.DISEASE_INJECTED.get()) return;
		DiseaseDefinition frame = DiseaseRegistry.get(frameId);
		if(frame == null) return;

		if(target instanceof EntityPlayer) {
			com.hbm.extprop.HbmBloodstreamProps.getData(target).addPathogen(frame, dose, genome);
		} else {
			// mobs keep their infections in the lightweight map so they progress and spread on the axes
			DiseaseInstance instance = new DiseaseInstance(frame.id, target.worldObj.getTotalWorldTime(), frame.durationTicks);
			instance.genome = Genome.ensure(genome, frame.id);
			instance.frameDef = frame.toNBT();
			Genome.derive(instance, frame);
			com.hbm.extprop.HbmLivingProps.addDisease(target, instance);
		}
	}

	@Override public void serializeJSON(JsonWriter writer) throws IOException { }

	@Override public void deserializeJSON(JsonObject obj) { }
}
