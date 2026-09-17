package com.hbm.handler.contagion.condition;

import com.hbm.handler.contagion.DiseaseDefinition;
import com.hbm.handler.contagion.DiseaseInstance;

import com.hbm.extprop.HbmBloodstreamProps;
import com.hbm.lib.ModDamageSource;
import com.hbm.potion.HbmPotion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;


public class BacteriaCondition extends PathogenCondition {

	@Override
	public DiseaseDefinition.PathogenType getType() {
		return DiseaseDefinition.PathogenType.BACTERIA;
	}

	@Override
	public float getSeverityRamp(DiseaseDefinition def) {
		return 1.0F;
	}

	@Override
	public void tickSymptoms(HbmBloodstreamProps props, EntityLivingBase entity, DiseaseDefinition def, DiseaseInstance instance) {

		float sev = instance.currentSeverity;

		entity.addPotionEffect(new PotionEffect(HbmPotion.symptomFever.id, 40, (int) (sev * 2)));
		if(sev > 0.5F) {
			entity.addPotionEffect(new PotionEffect(HbmPotion.symptomSeptic.id, 40, (int) ((sev - 0.5F) * 2)));
		}

		if(entity.getRNG().nextInt(Math.max(10, 60 - (int) (sev * 40))) == 0) {
			entity.attackEntityFrom(ModDamageSource.generic, 0.5F + sev);
		}
	}

	@Override
	public float getSpreadModifier(DiseaseDefinition.TransmissionAxis axis, DiseaseInstance instance) {
		if(axis == DiseaseDefinition.TransmissionAxis.TOUCH) return 1.5F;
		if(axis == DiseaseDefinition.TransmissionAxis.AEROSOL) return 0.5F;
		return 1.0F;
	}
}
