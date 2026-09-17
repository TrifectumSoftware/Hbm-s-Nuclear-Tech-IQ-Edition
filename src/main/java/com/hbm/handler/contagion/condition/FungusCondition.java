package com.hbm.handler.contagion.condition;

import com.hbm.handler.contagion.DiseaseDefinition;
import com.hbm.handler.contagion.DiseaseInstance;

import com.hbm.extprop.HbmBloodstreamProps;
import com.hbm.potion.HbmPotion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;


public class FungusCondition extends PathogenCondition {

	@Override
	public DiseaseDefinition.PathogenType getType() {
		return DiseaseDefinition.PathogenType.FUNGUS;
	}

	@Override
	public float getSeverityRamp(DiseaseDefinition def) {
		return 0.4F;
	}

	@Override
	public void tickSymptoms(HbmBloodstreamProps props, EntityLivingBase entity, DiseaseDefinition def, DiseaseInstance instance) {
		entity.addPotionEffect(new PotionEffect(HbmPotion.symptomSneeze.id, 40, 0));
		entity.addPotionEffect(new PotionEffect(HbmPotion.symptomCough.id, 40, 0));
			if(entity.getRNG().nextInt(40) == 0) {
			entity.addPotionEffect(new PotionEffect(Potion.hunger.id, 200, 0));
		}
	}
}
