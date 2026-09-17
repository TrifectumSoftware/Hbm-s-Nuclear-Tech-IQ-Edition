package com.hbm.handler.contagion.condition;

import com.hbm.handler.contagion.DiseaseDefinition;
import com.hbm.handler.contagion.DiseaseInstance;

import java.util.HashMap;
import java.util.Map;

import com.hbm.extprop.HbmBloodstreamProps;

import net.minecraft.entity.EntityLivingBase;


public abstract class PathogenCondition {

	private static final Map<DiseaseDefinition.PathogenType, PathogenCondition> registry = new HashMap<>();
	private static boolean initialized = false;

	public PathogenCondition() {
		registry.put(getType(), this);
	}

	public abstract DiseaseDefinition.PathogenType getType();

	public void tickSymptoms(HbmBloodstreamProps props, EntityLivingBase entity, DiseaseDefinition def, DiseaseInstance instance) { }

	public float getSpreadModifier(DiseaseDefinition.TransmissionAxis axis, DiseaseInstance instance) {
		return 1.0F;
	}

	public boolean isContagiousDuringIncubation() {
		return false;
	}

	public float getSeverityRamp(DiseaseDefinition def) {
		return 1.0F;
	}

	public static PathogenCondition get(DiseaseDefinition.PathogenType type) {
		ensureInit();
		return type == null ? null : registry.get(type);
	}

	public static void ensureInit() {
		if(initialized) return;
		initialized = true;
		new BacteriaCondition();
		new VirusCondition();
		new FungusCondition();
		new ParasiteCondition();
		new PrionCondition();
	}
}
