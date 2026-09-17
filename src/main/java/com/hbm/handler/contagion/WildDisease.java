package com.hbm.handler.contagion;

import java.util.EnumMap;
import java.util.Random;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemMedicalSyringe;

import api.hbm.fluidmk2.IFillableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;


// Wild disease class, it contains methods for generating random wild diseases

public class WildDisease {

	private static final DiseaseDefinition.PathogenType[] TYPES = {
		// everything but prions
		DiseaseDefinition.PathogenType.VIRUS,
		DiseaseDefinition.PathogenType.BACTERIA,
		DiseaseDefinition.PathogenType.FUNGUS,
		DiseaseDefinition.PathogenType.PARASITE
	};

	public static DiseaseDefinition generate(Random rand) {

		String id = "wild_" + Integer.toHexString(rand.nextInt(0xFFFFFF)) + "_" + Integer.toHexString(rand.nextInt(0xFFFF));
		DiseaseDefinition.PathogenType type = TYPES[rand.nextInt(TYPES.length)];

		int incubation = (60 + rand.nextInt(1140)) * 20;
		int duration = (1800 + rand.nextInt(12600)) * 20;
		float severity = 0.15F + rand.nextFloat() * 0.35F;
		float mutationRate = 0.02F + rand.nextFloat() * 0.08F;

		EnumMap<DiseaseDefinition.TransmissionAxis, Float> transmission = new EnumMap<>(DiseaseDefinition.TransmissionAxis.class);
		transmission.put(DiseaseDefinition.TransmissionAxis.AEROSOL, 0.02F + rand.nextFloat() * 0.08F);
		if(rand.nextBoolean()) transmission.put(DiseaseDefinition.TransmissionAxis.TOUCH, 0.05F + rand.nextFloat() * 0.2F);
		if(rand.nextBoolean()) transmission.put(DiseaseDefinition.TransmissionAxis.PROXIMITY, 0.01F + rand.nextFloat() * 0.05F);

		boolean fomite = type == DiseaseDefinition.PathogenType.FUNGUS || type == DiseaseDefinition.PathogenType.PARASITE || rand.nextBoolean();
		if(type == DiseaseDefinition.PathogenType.PARASITE) {
			transmission.put(DiseaseDefinition.TransmissionAxis.HIT, 0.05F + rand.nextFloat() * 0.15F);
		}

		DiseaseDefinition def = new DiseaseDefinition(
				id, generateName(rand), type,
				incubation, duration,
				severity,
				transmission, 40 + rand.nextInt(120), 1.5F + rand.nextFloat() * 2.0F, 4.0F, fomite,
				mutationRate, 600 + rand.nextInt(1200), rand.nextFloat() * 0.4F,
				rand.nextFloat() * 0.2F, false);

		return def;
	}

	private static final String NAME_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private static String generateName(Random rand) {
		StringBuilder sb = new StringBuilder();
		int letters = 3 + rand.nextInt(2);
		for(int i = 0; i < letters; i++) sb.append(NAME_CHARS.charAt(rand.nextInt(NAME_CHARS.length())));
		sb.append(String.format("%02d", rand.nextInt(100)));
		return sb.toString();
	}

// makes a syringe with the pathogen frame, used in the vector lab kit for testing rn
	public static ItemStack makeSyringe(Random rand) {

		DiseaseDefinition def = generate(rand);
		String genome = def.getReferenceGenome();

		ItemStack syringe = new ItemStack(ModItems.medical_syringe);
		IFillableItem.setFluidFill(syringe, Fluids.HUMAN_BLOOD, (short) ItemMedicalSyringe.MAX_DOSE);

		NBTTagCompound mut = new NBTTagCompound();
		mut.setString("genome", genome);
		mut.setTag("def", def.toNBT());

		NBTTagList list = new NBTTagList();
		NBTTagCompound entry = new NBTTagCompound();
		entry.setString("frame", def.id);
		entry.setFloat("amount", 100F);
		entry.setTag("mut", mut);
		list.appendTag(entry);

		if(!syringe.hasTagCompound()) syringe.stackTagCompound = new NBTTagCompound();
		syringe.stackTagCompound.setTag(ItemMedicalSyringe.KEY_PATHOGENS, list);
		return syringe;
	}
}
