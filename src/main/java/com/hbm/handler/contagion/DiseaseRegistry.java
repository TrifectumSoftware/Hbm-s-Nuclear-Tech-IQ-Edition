package com.hbm.handler.contagion;

import java.util.EnumMap;
import java.util.HashMap;

import com.hbm.main.MainRegistry;

import net.minecraft.nbt.NBTTagCompound;


public class DiseaseRegistry {

	private static final HashMap<String, DiseaseDefinition> REGISTRY = new HashMap<String, DiseaseDefinition>();

	public static void register(DiseaseDefinition def) {
		if(REGISTRY.containsKey(def.id)) {
			MainRegistry.logger.warn("Disease id '{}' is being replaced (was: {} now: {})", def.id, REGISTRY.get(def.id), def);
		}
		REGISTRY.put(def.id, def);
	}

	public static DiseaseDefinition get(String id) {
		return REGISTRY.get(id);
	}

	public static DiseaseDefinition resolve(String frameId, NBTTagCompound frameDef) {
		DiseaseDefinition def = get(frameId);
		if(def != null) return def;
		return restoreDef(frameId, frameDef);
	}

	public static DiseaseDefinition restore(String frameId, NBTTagCompound instanceNBT) {
		if(instanceNBT == null || !instanceNBT.hasKey("def")) return null;
		return restoreDef(frameId, instanceNBT.getCompoundTag("def"));
	}

	public static DiseaseDefinition restoreDef(String frameId, NBTTagCompound frameDef) {
		if(frameId == null || frameDef == null) return null;
		DiseaseDefinition existing = REGISTRY.get(frameId);
		if(existing != null) return existing;

		DiseaseDefinition def = DiseaseDefinition.readFromNBT(frameDef);
		if(def != null && frameId.equals(def.id)) {
			register(def);
			return def;
		}
		return null;
	}

	public static void seedDefaults() {

		if(!REGISTRY.isEmpty()) return;

		EnumMap<DiseaseDefinition.TransmissionAxis, Float> mkuTrans = new EnumMap<>(DiseaseDefinition.TransmissionAxis.class);
		mkuTrans.put(DiseaseDefinition.TransmissionAxis.AEROSOL, 0.05F);
		mkuTrans.put(DiseaseDefinition.TransmissionAxis.TOUCH, 0.0F);
		mkuTrans.put(DiseaseDefinition.TransmissionAxis.HIT, 0.25F);
		mkuTrans.put(DiseaseDefinition.TransmissionAxis.PROXIMITY, 0.02F);
		mkuTrans.put(DiseaseDefinition.TransmissionAxis.INJECTED, 1.0F);

		register(new DiseaseDefinition(
				"mku", "MKU", DiseaseDefinition.PathogenType.PRION,
				144000, 864000,
				0.7F,
				mkuTrans, 60, 3.0F, 4.0F, false,
				0.02F, 1800, 0.3F,
				0.2F, true));
	}
}
