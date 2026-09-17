package com.hbm.handler.contagion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hbm.config.ServerConfig;
import com.hbm.extprop.HbmBloodstreamProps;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.handler.contagion.condition.PathogenCondition;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorRegistry.HazardClass;
import com.hbm.util.ArmorUtil;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;


public class DiseaseHandler {


	private static final int FILTER_DISEASE_DAMAGE = 100;

	public static void tick(EntityLivingBase host) {
		if(!ServerConfig.ENABLE_DISEASES.get()) return;
		if(host == null || host.worldObj == null || host.worldObj.isRemote) return;

		List<String> fomiteFrames = ItemContagion.tick(host);

		for(DiseaseInstance instance : getInstances(host)) {
			if(instance == null || instance.frameId == null) continue;
			DiseaseDefinition def = DiseaseRegistry.resolve(instance.frameId, instance.frameDef);
			if(def == null) continue;
			PathogenCondition condition = PathogenCondition.get(def.type);

			if(isIncubating(host, def, instance, condition)) continue;

			spread(host, def, instance, condition);
		}
		ItemContagion.tagNearbyItems(host.worldObj, host, fomiteFrames);

		if(!(host instanceof EntityPlayer)) progressMobDisease(host);
	}

	public static boolean tickInstance(EntityLivingBase entity, DiseaseDefinition def, DiseaseInstance instance) {
		PathogenCondition condition = PathogenCondition.get(def.type);
		if(condition == null) return false;

		if(instance.genome == null) {
			instance.genome = def.getReferenceGenome();
			Genome.derive(instance, def);
		}

		if(entity.worldObj.getTotalWorldTime() - instance.infectedAtTick >= def.incubationTicks) {
			condition.tickSymptoms(HbmBloodstreamProps.getData(entity), entity, def, instance);
			instance.currentSeverity = Math.min(1.0F, instance.currentSeverity + condition.getSeverityRamp(def) / 100000F * instance.attenuation);
		}
		Genome.tickMutation(instance, def, entity);

		if(!def.uncurable) {
			if(instance.ticksRemaining > 0) instance.ticksRemaining--;
			if(instance.ticksRemaining <= 0) return false;
		}
		return true;
	}

	private static void progressMobDisease(EntityLivingBase host) {
		if(host == null || host.worldObj == null || host.worldObj.isRemote) return;

		Map<String, DiseaseInstance> diseases = HbmLivingProps.getDiseases(host);
		if(diseases.isEmpty()) return;

		Iterator<Map.Entry<String, DiseaseInstance>> iter = diseases.entrySet().iterator();
		while(iter.hasNext()) {
			DiseaseInstance instance = iter.next().getValue();
			if(instance == null || instance.frameId == null) {
				iter.remove();
				continue;
			}

			DiseaseDefinition def = DiseaseRegistry.get(instance.frameId);
			if(def == null) def = DiseaseRegistry.restoreDef(instance.frameId, instance.frameDef);
			if(def == null) continue;

			if(!tickInstance(host, def, instance)) iter.remove();
		}
	}

	private static boolean isIncubating(EntityLivingBase host, DiseaseDefinition def, DiseaseInstance instance, PathogenCondition condition) {
		if(condition != null && condition.isContagiousDuringIncubation()) return false;
		long infectedFor = host.worldObj.getTotalWorldTime() - instance.infectedAtTick;
		return infectedFor < def.incubationTicks;
	}

	private static List<DiseaseInstance> getInstances(EntityLivingBase host) {
		if(host instanceof EntityPlayer) {
			return HbmBloodstreamProps.getData(host).getPathogenInstances();
		}
		Map<String, DiseaseInstance> map = HbmLivingProps.getDiseases(host);
		return new ArrayList<>(map.values());
	}

	private static void spread(EntityLivingBase host, DiseaseDefinition def, DiseaseInstance instance, PathogenCondition condition) {
		if(ServerConfig.DISEASE_AEROSOL.get()) spreadAxis(host, def, instance, condition, DiseaseDefinition.TransmissionAxis.AEROSOL, def.transmissionIntervalTicks, 1.0D, true);
		if(ServerConfig.DISEASE_TOUCH.get()) spreadAxis(host, def, instance, condition, DiseaseDefinition.TransmissionAxis.TOUCH, 10, 0.0D, false);
		if(ServerConfig.DISEASE_PROXIMITY.get()) spreadAxis(host, def, instance, condition, DiseaseDefinition.TransmissionAxis.PROXIMITY, 40, 2.0D, true);
	}

	private static void spreadAxis(EntityLivingBase host, DiseaseDefinition def, DiseaseInstance instance, PathogenCondition condition, DiseaseDefinition.TransmissionAxis axis, int interval, double rangeMul, boolean armorBlocks) {
		Float base = def.transmission == null ? null : def.transmission.get(axis);
		if(base == null || base <= 0) return;
		if(host.worldObj.getTotalWorldTime() % interval != 0) return;

		double range = axis == DiseaseDefinition.TransmissionAxis.AEROSOL && host.isWet() ? def.range * def.wetBoost : def.range * rangeMul;

		for(Object o : host.worldObj.getEntitiesWithinAABBExcludingEntity(host, host.boundingBox.expand(range, range, range))) {
			if(!(o instanceof EntityLivingBase)) continue;
			transmit(host, (EntityLivingBase) o, def, instance, condition, axis, base, armorBlocks);
		}
	}

	private static void transmit(EntityLivingBase host, EntityLivingBase target, DiseaseDefinition def, DiseaseInstance instance, PathogenCondition condition, DiseaseDefinition.TransmissionAxis axis, float base, boolean armorBlocks) {
		if(isImmuneOrInfected(target, instance.frameId, instance.genome)) return;

		float spread = condition == null ? 1.0F : condition.getSpreadModifier(axis, instance);
		if(axis == DiseaseDefinition.TransmissionAxis.AEROSOL) {
			spread *= 1.0F + Math.min(180, HbmBloodstreamProps.getData(host).getCoughBoost()) / 150.0F;
		}

		if(host.worldObj.rand.nextFloat() >= base * spread) return;
		if(armorBlocks && isArmorProtected(target, instance)) {
			ArmorUtil.damageGasMaskFilter(target, FILTER_DISEASE_DAMAGE);
			return;
		}
		transfer(host.worldObj, target, def, instance);
	}


	public static boolean isImmuneOrInfected(EntityLivingBase target, String frameId, String genome) {

		if(target == null || frameId == null) return false;

		if(target instanceof EntityPlayer) {
			HbmBloodstreamProps props = HbmBloodstreamProps.getData(target);
			if(props.hasImmunity(Genome.ensure(genome, frameId))) return true;
			return props.hasPathogen(frameId);
		} else {
			return HbmLivingProps.hasDisease(target, frameId);
		}
	}

		public static boolean isArmorProtected(EntityLivingBase target, DiseaseInstance instance) {

		if(target == null || instance == null || instance.frameId == null) return false;

		DiseaseDefinition def = DiseaseRegistry.resolve(instance.frameId, instance.frameDef);
		if(def == null) return false;

		HazardClass haz = def.getHazardClass();
		return haz != null && ArmorRegistry.hasProtection(target, 3, haz);
	}


	public static void handleHit(EntityLivingBase attacker, EntityLivingBase target) {

		if(!ServerConfig.DISEASE_HIT.get()) return;
		if(attacker == null || target == null || attacker.worldObj == null || attacker.worldObj.isRemote) return;

		for(DiseaseInstance instance : getInstances(attacker)) {
			if(instance == null || instance.frameId == null) continue;
			DiseaseDefinition def = DiseaseRegistry.resolve(instance.frameId, instance.frameDef);
			if(def == null) continue;
			Float base = def.transmission == null ? null : def.transmission.get(DiseaseDefinition.TransmissionAxis.HIT);
			if(base == null || base <= 0) continue;
			PathogenCondition condition = PathogenCondition.get(def.type);
			if(isIncubating(attacker, def, instance, condition)) continue;
			transmit(attacker, target, def, instance, condition, DiseaseDefinition.TransmissionAxis.HIT, base, true);
		}
	}

	public static void transfer(World world, EntityLivingBase target, DiseaseDefinition def, DiseaseInstance source) {

		float dose = 10 + def.baseSeverity * 10;

		// EVOLUTUION
		String genome = source != null ? Genome.mutate(Genome.ensure(source.genome, def.id), 1, world.rand) : def.getReferenceGenome();

		if(target instanceof EntityPlayer) {
			HbmBloodstreamProps.getData(target).addPathogen(def, dose, genome);
		} else {
			DiseaseInstance mobInstance = new DiseaseInstance(def.id, world.getTotalWorldTime(), def.durationTicks);
			mobInstance.genome = genome;
			mobInstance.frameDef = def.toNBT();
			Genome.derive(mobInstance, def);
			HbmLivingProps.addDisease(target, mobInstance);
		}
	}
}
