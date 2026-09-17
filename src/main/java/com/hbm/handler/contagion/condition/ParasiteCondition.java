package com.hbm.handler.contagion.condition;

import com.hbm.handler.contagion.DiseaseDefinition;
import com.hbm.handler.contagion.DiseaseInstance;

import com.hbm.extprop.HbmBloodstreamProps;
import com.hbm.lib.ModDamageSource;
import com.hbm.potion.HbmPotion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;


public class ParasiteCondition extends PathogenCondition {

	@Override
	public DiseaseDefinition.PathogenType getType() {
		return DiseaseDefinition.PathogenType.PARASITE;
	}

	@Override
	public boolean isContagiousDuringIncubation() {
		return true;
	}

	@Override
	public float getSeverityRamp(DiseaseDefinition def) {
		return 2.0F;
	}

	@Override
	public void tickSymptoms(HbmBloodstreamProps props, EntityLivingBase entity, DiseaseDefinition def, DiseaseInstance instance) {

		float sev = instance.currentSeverity;

		entity.addPotionEffect(new PotionEffect(Potion.hunger.id, 40, (int) (sev * 2)));
		entity.addPotionEffect(new PotionEffect(HbmPotion.symptomFever.id, 40, (int) (sev * 2)));
		if(sev > 0.4F) {
			entity.addPotionEffect(new PotionEffect(HbmPotion.symptomHemorrhage.id, 40, (int) ((sev - 0.4F) * 2)));
		}
		if(sev > 0.7F) {
			entity.addPotionEffect(new PotionEffect(HbmPotion.symptomSeptic.id, 40, (int) ((sev - 0.7F) * 2)));
		}

		if(entity.getRNG().nextInt(Math.max(8, 50 - (int) (sev * 40))) == 0) {
			entity.attackEntityFrom(ModDamageSource.generic, sev * 2);
		}
	}
}
