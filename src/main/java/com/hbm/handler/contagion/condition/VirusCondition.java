package com.hbm.handler.contagion.condition;

import com.hbm.handler.contagion.DiseaseDefinition;
import com.hbm.handler.contagion.DiseaseInstance;

import com.hbm.extprop.HbmBloodstreamProps;
import com.hbm.potion.HbmPotion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class VirusCondition extends PathogenCondition {

	@Override
	public DiseaseDefinition.PathogenType getType() {
		return DiseaseDefinition.PathogenType.VIRUS;
	}

	@Override
	public boolean isContagiousDuringIncubation() {
		return true;
	}

	@Override
	public float getSeverityRamp(DiseaseDefinition def) {
		return 1.5F;
	}

	@Override
	public void tickSymptoms(HbmBloodstreamProps props, EntityLivingBase entity, DiseaseDefinition def, DiseaseInstance instance) {

		long worldTick = entity.worldObj.getTotalWorldTime();

		entity.addPotionEffect(new PotionEffect(HbmPotion.symptomCough.id, 40, 0));
		if(worldTick % 80 < 40) {
			entity.addPotionEffect(new PotionEffect(Potion.confusion.id, 40, 0));
		}
		if(props != null && worldTick % 60 == 0) {
			props.drainBlood(0.5F + instance.currentSeverity);
		}
	}

	@Override
	public float getSpreadModifier(DiseaseDefinition.TransmissionAxis axis, DiseaseInstance instance) {
		if(axis == DiseaseDefinition.TransmissionAxis.AEROSOL) return 1.5F;
		return 1.0F;
	}
}
