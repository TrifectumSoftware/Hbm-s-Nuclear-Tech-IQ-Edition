package com.hbm.handler.contagion.condition;

import com.hbm.handler.contagion.DiseaseDefinition;
import com.hbm.handler.contagion.DiseaseInstance;

import com.hbm.extprop.HbmBloodstreamProps;
import com.hbm.lib.ModDamageSource;
import com.hbm.potion.HbmPotion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;

public class PrionCondition extends PathogenCondition {

	@Override
	public DiseaseDefinition.PathogenType getType() {
		return DiseaseDefinition.PathogenType.PRION;
	}

	@Override
	public float getSeverityRamp(DiseaseDefinition def) {
		return (1.0F - def.baseSeverity) / Math.max(1, def.durationTicks - def.incubationTicks) * 100000F;
	}

	@Override
	public void tickSymptoms(HbmBloodstreamProps props, EntityLivingBase entity, DiseaseDefinition def, DiseaseInstance instance) {

		long worldTick = entity.worldObj.getTotalWorldTime();
		long infectedFor = worldTick - instance.infectedAtTick;
		float progress = (float) infectedFor / (float) Math.max(1, def.durationTicks);
		if(progress > 1.0F) progress = 1.0F;

		entity.addPotionEffect(new PotionEffect(HbmPotion.symptomSchizophrenia.id, 40, (int) (instance.currentSeverity * 2)));
		entity.addPotionEffect(new PotionEffect(HbmPotion.symptomParalysis.id, 40, (int) (instance.currentSeverity * 1.5F)));

		if(entity.getRNG().nextInt(Math.max(20, (int) (80 - progress * 60))) == 0) {
			entity.attackEntityFrom(ModDamageSource.digamma, 1.0F + instance.currentSeverity * 2.0F);
		}
	}
}
