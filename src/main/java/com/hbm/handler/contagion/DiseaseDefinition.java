package com.hbm.handler.contagion;

import java.util.EnumMap;

import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorRegistry.HazardClass;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

/// S.O.N
public class DiseaseDefinition {

	public enum PathogenType { VIRUS, BACTERIA, FUNGUS, PARASITE, PRION }
	public enum TransmissionAxis { AEROSOL, TOUCH, HIT, PROXIMITY, INJECTED }

	public String id;
	public String displayName;
	public PathogenType type;
	public int incubationTicks;
	public int durationTicks;
	public float baseSeverity;
	public EnumMap<TransmissionAxis, Float> transmission;
	public int transmissionIntervalTicks;
	public float range;
	public float wetBoost;
	public boolean fomite;
	public float baseMutationRate;
	public int mutationIntervalTicks;
	public float antigenMutability;
	public float baseResistance;

   // NOTE: the uncurable effect acts as more as dramatic semantics for prions being incurable, cause you always use antiserum to cure diseases

	public boolean uncurable;

	public DiseaseDefinition() {
		this.id = "";
		this.displayName = "";
		this.type = null;
		this.incubationTicks = 0;
		this.durationTicks = 0;
		this.baseSeverity = 0.5F;
		this.transmission = new EnumMap<TransmissionAxis, Float>(TransmissionAxis.class);
		this.transmissionIntervalTicks = 20;
		this.range = 2.0F;
		this.wetBoost = 8.0F;
		this.fomite = false;
		this.baseMutationRate = 0.05F;
		this.mutationIntervalTicks = 1200;
		this.antigenMutability = 0.0F;
		this.baseResistance = 0.0F;
		this.uncurable = false;
	}

	public DiseaseDefinition(String id, String displayName, PathogenType type, int incubationTicks, int durationTicks, float baseSeverity, EnumMap<TransmissionAxis, Float> transmission, int transmissionIntervalTicks, float range, float wetBoost, boolean fomite, float baseMutationRate, int mutationIntervalTicks, float antigenMutability, float baseResistance, boolean uncurable) {
		this.id = id;
		this.displayName = displayName;
		this.type = type;
		this.incubationTicks = incubationTicks;
		this.durationTicks = durationTicks;
		this.baseSeverity = baseSeverity;
		this.transmission = transmission;
		this.transmissionIntervalTicks = transmissionIntervalTicks;
		this.range = range;
		this.wetBoost = wetBoost;
		this.fomite = fomite;
		this.baseMutationRate = baseMutationRate;
		this.mutationIntervalTicks = mutationIntervalTicks;
		this.antigenMutability = antigenMutability;
		this.baseResistance = baseResistance;
		this.uncurable = uncurable;
	}

	public String getReferenceGenome() {
		return Genome.reference(this.id);
	}

	public HazardClass getHazardClass() {
		return getHazardClass(this.type);
	}

	public static HazardClass getHazardClass(PathogenType type) {
		if(type == null) return null;
		switch(type) {
			case VIRUS:     return HazardClass.VIRUS;
			case BACTERIA:  return HazardClass.BACTERIA;
			case FUNGUS:    return HazardClass.FUNGUS;
			case PARASITE:  return HazardClass.PARASITE;
			case PRION:     return HazardClass.PRION;
			default:        return null;
		}
	}

	public NBTTagCompound toNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("id", this.id);
		nbt.setString("disp", this.displayName == null ? "" : this.displayName);
		if(this.type != null) nbt.setString("type", this.type.name());
		nbt.setInteger("incub", this.incubationTicks);
		nbt.setInteger("dur", this.durationTicks);
		nbt.setFloat("sev", this.baseSeverity);
		nbt.setInteger("transInterval", this.transmissionIntervalTicks);
		nbt.setFloat("range", this.range);
		nbt.setFloat("wet", this.wetBoost);
		nbt.setBoolean("fomite", this.fomite);
		nbt.setFloat("mutRate", this.baseMutationRate);
		nbt.setInteger("mutInterval", this.mutationIntervalTicks);
		nbt.setFloat("ag", this.antigenMutability);
		nbt.setFloat("res", this.baseResistance);
		nbt.setBoolean("uncurable", this.uncurable);

		NBTTagList transList = new NBTTagList();
		if(this.transmission != null) {
			for(TransmissionAxis axis : this.transmission.keySet()) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setString("axis", axis.name());
				tag.setFloat("mult", this.transmission.get(axis));
				transList.appendTag(tag);
			}
		}
		nbt.setTag("trans", transList);
		return nbt;
	}

	public static DiseaseDefinition readFromNBT(NBTTagCompound nbt) {
		if(nbt == null) return null;
		DiseaseDefinition def = new DiseaseDefinition();
		def.id = nbt.getString("id");
		def.displayName = nbt.getString("disp");
		try {
			String typeName = nbt.getString("type");
			if(!typeName.isEmpty()) def.type = PathogenType.valueOf(typeName);
		} catch(IllegalArgumentException e) { }
		def.incubationTicks = nbt.getInteger("incub");
		def.durationTicks = nbt.getInteger("dur");
		def.baseSeverity = nbt.getFloat("sev");
		def.transmissionIntervalTicks = nbt.getInteger("transInterval");
		def.range = nbt.getFloat("range");
		def.wetBoost = nbt.getFloat("wet");
		def.fomite = nbt.getBoolean("fomite");
		def.baseMutationRate = nbt.getFloat("mutRate");
		def.mutationIntervalTicks = nbt.getInteger("mutInterval");
		def.antigenMutability = nbt.getFloat("ag");
		def.baseResistance = nbt.getFloat("res");
		def.uncurable = nbt.getBoolean("uncurable");

		if(nbt.hasKey("trans")) {
			NBTTagList transList = nbt.getTagList("trans", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < transList.tagCount(); i++) {
				NBTTagCompound tag = transList.getCompoundTagAt(i);
				try {
					TransmissionAxis axis = TransmissionAxis.valueOf(tag.getString("axis"));
					def.transmission.put(axis, tag.getFloat("mult"));
				} catch(IllegalArgumentException e) { }
			}
		}
		return def;
	}
}
