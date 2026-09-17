package com.hbm.extprop;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.hbm.handler.contagion.BloodEntry;
import com.hbm.handler.contagion.DiseaseDefinition;
import com.hbm.handler.contagion.DiseaseHandler;
import com.hbm.handler.contagion.DiseaseInstance;
import com.hbm.handler.contagion.DiseaseRegistry;
import com.hbm.handler.contagion.Genome;
import com.hbm.handler.contagion.PharmaProfile;
import com.hbm.handler.contagion.SymptomPool;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.trait.FT_Consumable;
import com.hbm.inventory.fluid.trait.FT_Drug;
import com.hbm.lib.ModDamageSource;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.potion.HbmPotion;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.util.Constants;

public class HbmBloodstreamProps implements IExtendedEntityProperties {

	public static final String key = "NTM_EXT_BLOOD";

	// this is 10 hearts
	public static final float BASE_BLOOD = 10000F;
	private static final float CROSS_IMMUNITY_SIM = 0.9F;

	private final EntityLivingBase entity;
	private final List<BloodEntry> entries = new ArrayList<>();
	private final Set<String> immuneGenomes = new HashSet<>();
	private int coughBoost = 0;

	public HbmBloodstreamProps(EntityLivingBase entity) {
		this.entity = entity;
	}

	public static HbmBloodstreamProps registerData(EntityLivingBase entity) {

		entity.registerExtendedProperties(key, new HbmBloodstreamProps(entity));
		return (HbmBloodstreamProps) entity.getExtendedProperties(key);
	}

	public static HbmBloodstreamProps getData(EntityLivingBase entity) {

		HbmBloodstreamProps props = (HbmBloodstreamProps) entity.getExtendedProperties(key);
		if(props == null) props = registerData(entity);
		props.ensureBaseBlood();
		return props;
	}

	private void ensureBaseBlood() {
		for(BloodEntry entry : entries) {
			if(entry.frameId != null) continue;
			if(entry.fluidId == Fluids.HUMAN_BLOOD.getID()) {
				entry.amount = BASE_BLOOD;
				return;
			}
		}
		entries.add(new BloodEntry(Fluids.HUMAN_BLOOD.getID(), BASE_BLOOD, null, null));
	}

	@Override
	public void init(Entity entity, World world) { }

	public void addPathogen(DiseaseDefinition frame, float dose, String genome) {
		if(frame == null || frame.id == null) return;
		for(BloodEntry entry : entries) {
			if(entry.matches(-1, frame.id)) {
				entry.amount += dose;
				return;
			}
		}
		BloodEntry entry = new BloodEntry(-1, dose, frame.id, new NBTTagCompound());
		DiseaseInstance instance = new DiseaseInstance(frame.id, entity.worldObj.getTotalWorldTime(), frame.durationTicks);
		String g = Genome.ensure(genome, frame.id);
		instance.genome = g;
		instance.attenuation = getCrossImmunity(g);
		instance.frameDef = frame.toNBT();
		Genome.derive(instance, frame);
		instance.writeToNBT(entry.mutationNBT);
		entries.add(entry);
	}

	private float getCrossImmunity(String genome) {
		if(genome == null || immuneGenomes.isEmpty()) return 1.0F;
		float best = 0;
		for(String g : immuneGenomes) {
			float sim = Genome.similarity(genome, g);
			if(sim > best) best = sim;
		}
		if(best <= CROSS_IMMUNITY_SIM) return 1.0F;
		float attenuation = (1.0F - best) / (1.0F - CROSS_IMMUNITY_SIM);
		return Math.max(0.0F, Math.min(1.0F, attenuation));
	}

	public void addDrug(int fluidId, float dose) {
		for(BloodEntry entry : entries) {
			if(entry.matches(fluidId, null)) {
				entry.amount += dose;
				return;
			}
		}
		entries.add(new BloodEntry(fluidId, dose, null, new NBTTagCompound()));
	}

	public void addBlood(float amount) {
		for(BloodEntry entry : entries) {
			if(entry.frameId == null && entry.fluidId == Fluids.HUMAN_BLOOD.getID()) {
				entry.amount = Math.min(BASE_BLOOD, entry.amount + amount);
				return;
			}
		}
	}

	public void drainBlood(float amount) {
		for(BloodEntry entry : entries) {
			if(entry.frameId == null && entry.fluidId == Fluids.HUMAN_BLOOD.getID()) {
				entry.amount = Math.max(0, entry.amount - amount);
				return;
			}
		}
	}

	public float getBloodAmount() {
		for(BloodEntry entry : entries) {
			if(entry.frameId == null && entry.fluidId == Fluids.HUMAN_BLOOD.getID()) {
				return entry.amount;
			}
		}
		return 0;
	}

	public List<BloodEntry> getEntries() {
		return entries;
	}

	// permanent immunity
	public boolean hasImmunity(String genome) {
		return genome != null && immuneGenomes.contains(genome);
	}

	public boolean hasPathogen(String frameId) {
		for(BloodEntry entry : entries) {
			if(entry.frameId != null && entry.frameId.equals(frameId)) return true;
		}
		return false;
	}

	public List<DiseaseInstance> getPathogenInstances() {
		List<DiseaseInstance> instances = new ArrayList<>();
		for(BloodEntry entry : entries) {
			if(entry.frameId == null) continue;
			DiseaseInstance instance = entry.mutationNBT != null ? DiseaseInstance.readFromNBT(entry.mutationNBT) : new DiseaseInstance();
			if(instance.frameId == null || instance.frameId.isEmpty()) {
				instance.frameId = entry.frameId;
				DiseaseDefinition def = DiseaseRegistry.resolve(entry.frameId, instance.frameDef);
				instance.ticksRemaining = def == null ? 0 : def.durationTicks;
			}
			instances.add(instance);
		}
		return instances;
	}


	public int getCoughBoost() {
		return coughBoost;
	}

	public void applyCure(PharmaProfile profile, float intensity) {
		if(profile == null || profile.target == null) return;

		cureEntries(profile, intensity);

		DiseaseInstance mobInstance = HbmLivingProps.getDiseases(entity).get(profile.target);
		if(mobInstance == null) return;
		DiseaseDefinition mobDef = DiseaseRegistry.resolve(profile.target, mobInstance.frameDef);
		if(mobDef == null || mobDef.uncurable) return;

		String genome = Genome.ensure(mobInstance.genome, mobDef.id);
		float match = profile.targetGenome == null ? 1.0F : Genome.similarity(profile.targetGenome, genome);
		float resistanceFactor = 1.0F / (1.0F + mobInstance.resistanceLevel);
		mobInstance.ticksRemaining -= (long) (mobInstance.ticksRemaining * intensity * match * resistanceFactor);
		if(mobInstance.ticksRemaining <= 0) HbmLivingProps.removeDisease(entity, profile.target);
	}

	private void cureEntries(PharmaProfile profile, float intensity) {
		Iterator<BloodEntry> iter = entries.iterator();
		while(iter.hasNext()) {
			BloodEntry entry = iter.next();
			if(entry.frameId == null || !entry.frameId.equals(profile.target)) continue;

			DiseaseInstance instance = entry.mutationNBT != null ? DiseaseInstance.readFromNBT(entry.mutationNBT) : new DiseaseInstance();
			instance.frameId = entry.frameId;
			DiseaseDefinition def = DiseaseRegistry.resolve(entry.frameId, instance.frameDef);
			if(def == null) {
				iter.remove();
				break;
			}
			if(def.uncurable) break;

			instance.genome = Genome.ensure(instance.genome, def.id);

			float match = profile.targetGenome == null ? 1.0F : Genome.similarity(profile.targetGenome, instance.genome);
			float resistanceFactor = 1.0F / (1.0F + instance.resistanceLevel);
			entry.amount -= entry.amount * intensity * match * resistanceFactor;

			if(entry.amount <= 0) {
				immuneGenomes.add(Genome.ensure(instance.genome, def.id));
				iter.remove();
			} else {
				instance.resistanceLevel = Math.min(1.0F, instance.resistanceLevel + (1.0F - match) * 0.3F);
				if(entry.mutationNBT == null) entry.mutationNBT = new NBTTagCompound();
				instance.writeToNBT(entry.mutationNBT);
			}
			break;
		}
	}

	public void tick() {

		ensureBaseBlood();
		entries.removeIf(e -> e.frameId == null && e.fluidId != Fluids.HUMAN_BLOOD.getID() && e.amount < 0.5F);
		for(BloodEntry entry : entries) {
			if(entry.frameId == null && entry.fluidId != Fluids.HUMAN_BLOOD.getID()) {
				FluidType type = Fluids.fromID(entry.fluidId);
				if(type == null || !type.hasTrait(FT_Drug.class)) continue;
				FT_Drug drug = type.getTrait(FT_Drug.class);
				float total = getTotalFluid();
				float drugPct = entry.amount / total;
				float cap = drug.threshold > 0 ? drug.threshold : 0.01F;
				entry.amount = Math.max(0, entry.amount * drug.getDecayFactor(drugPct / cap));
				if(entry.amount <= 0) continue;
				drugPct = entry.amount / total;
				int level = (int) (drugPct / (cap * 0.25F));
				for(FT_Consumable.ConsumableEffect effect : drug.getEffects()) {
					int amp = effect.amplifier + level;
					int ticks = (effect.potionId == Potion.harm.id || effect.potionId == Potion.heal.id) ? 1 : 20;
					entity.addPotionEffect(new PotionEffect(effect.potionId, ticks, Math.max(amp, 0)));
				}
				if(drugPct > cap) {
					int tier = (int) ((drugPct - cap) / (cap * 0.25F));
					for(String key : drug.getSideEffects()) {
						SymptomPool.Symptom symptom = SymptomPool.getSymptom(key);
						if(symptom != null) {
							for(FT_Consumable.ConsumableEffect effect : symptom.effects) {
								entity.addPotionEffect(new PotionEffect(effect.potionId, 20, effect.amplifier + tier));
							}
						}
					}
				}
			}
		}
		applyConditionTickers();
		applySymptomTickers();
		ensureBaseBlood();
		syncHealthToBlood();
	}


	private void applyConditionTickers() {
		if(entity == null || entity.worldObj.isRemote) return;

		for(BloodEntry entry : entries) {
			if(entry.frameId == null) continue;
			DiseaseDefinition def = DiseaseRegistry.get(entry.frameId);
			if(def == null) def = DiseaseRegistry.restore(entry.frameId, entry.mutationNBT);
			if(def == null) continue;

			DiseaseInstance instance = entry.mutationNBT != null ? DiseaseInstance.readFromNBT(entry.mutationNBT) : new DiseaseInstance();
			if(instance.frameId == null || instance.frameId.isEmpty()) {
				instance.frameId = entry.frameId;
				instance.infectedAtTick = entity.worldObj.getTotalWorldTime();
				instance.ticksRemaining = def.durationTicks;
			}

			if(!DiseaseHandler.tickInstance(entity, def, instance)) {
				immuneGenomes.add(Genome.ensure(instance.genome, def.id));
				entry.amount = 0;
			}

			if(entry.mutationNBT == null) entry.mutationNBT = new NBTTagCompound();
			instance.writeToNBT(entry.mutationNBT);
		}

		entries.removeIf(entry -> entry.frameId != null && entry.amount <= 0);
	}

	// symptoms
	private void applySymptomTickers() {
		if(entity == null || entity.worldObj.isRemote) return;
		long tick = entity.worldObj.getTotalWorldTime();
		int id = entity.getEntityId();

		// rash is in the event handler, not here

		//sneeze
		if(entity.isPotionActive(HbmPotion.symptomSneeze)) {
			coughBoost = Math.max(coughBoost, 180);

			if(tick % 120 == id % 120) {
				entity.worldObj.playSoundEffect(entity.posX, entity.posY, entity.posZ, "hbm:player.sneeze", 1.0F, 1.2F);

				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("type", "vomit");
				nbt.setString("mode", "sneeze");
				nbt.setInteger("count", 15);
				nbt.setInteger("entity", id);
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(nbt, 0, 0, 0),
						new TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 25));
			}

			if(entity.worldObj.rand.nextInt(80) == 0)
				entity.attackEntityFrom(ModDamageSource.generic, 0.5F);
		}

		//cough
		if(entity.isPotionActive(HbmPotion.symptomCough)) {
			coughBoost = Math.max(coughBoost, 120);

			if(tick % 120 == id % 120) {
				entity.worldObj.playSoundEffect(entity.posX, entity.posY, entity.posZ, "hbm:player.cough", 1.0F, 1.0F);

				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("type", "vomit");
				nbt.setString("mode", "smoke");
				nbt.setInteger("count", 10);
				nbt.setInteger("entity", id);
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(nbt, 0, 0, 0),
						new TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 25));
			}

			if(entity.worldObj.rand.nextInt(80) == 0)
				entity.attackEntityFrom(ModDamageSource.generic, 0.5F);
		}

		if(!entity.isPotionActive(HbmPotion.symptomSneeze) && !entity.isPotionActive(HbmPotion.symptomCough) && coughBoost > 0) {
			coughBoost--;
		}

		//fever
		if(entity.isPotionActive(HbmPotion.symptomFever)) {
			if(entity.worldObj.rand.nextInt(40) == 0)
				entity.attackEntityFrom(ModDamageSource.fever, 1F);
			if(entity.isInWater() && tick % 20 == 0)
				entity.heal(1F);
		}

		// nausea
		if(entity.isPotionActive(Potion.confusion) && entity instanceof EntityPlayer && entity.worldObj.rand.nextInt(20) == 0)
			((EntityPlayer) entity).getFoodStats().addStats(-1, 0);

		//vomit
		if(entity.isPotionActive(HbmPotion.symptomVomit)) {
			if(entity instanceof EntityPlayer && entity.worldObj.rand.nextInt(10) == 0)
				((EntityPlayer) entity).getFoodStats().addStats(-2, 0);
			coughBoost = Math.max(coughBoost, 100);

			if(tick % 60 == id % 60) {
				entity.worldObj.playSoundEffect(entity.posX, entity.posY, entity.posZ, "hbm:player.vomit", 1.0F, 1.0F);
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("type", "vomit");
				nbt.setString("mode", "normal");
				nbt.setInteger("count", 15);
				nbt.setInteger("entity", id);
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(nbt, 0, 0, 0),
						new TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 25));
			}
		}

		//haemorrhage
		if(entity.isPotionActive(HbmPotion.symptomHemorrhage)) {
			if(tick % 80 == id % 80) {
				entity.worldObj.playSoundEffect(entity.posX, entity.posY, entity.posZ, "hbm:player.vomit", 1.0F, 0.8F);
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("type", "vomit");
				nbt.setString("mode", "blood");
				nbt.setInteger("count", 20);
				nbt.setInteger("entity", id);
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(nbt, 0, 0, 0),
						new TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 25));
			}
			if(entity.worldObj.rand.nextInt(30) == 0)
				entity.attackEntityFrom(ModDamageSource.bleed, 6F);
		}

		//schizophrenia
		if(entity.isPotionActive(HbmPotion.symptomSchizophrenia)) {
			if(tick % 40 == 0)
				entity.getEntityData().setInteger("schizoShader", entity.worldObj.rand.nextInt(4));
		}

		// paralaysis
		if(entity.isPotionActive(HbmPotion.symptomParalysis)) {
			entity.motionX = 0;
			entity.motionZ = 0;
		}

		//seizure
		if(entity.isPotionActive(HbmPotion.symptomSeizure)) {
			entity.motionX += entity.worldObj.rand.nextGaussian() * 0.15;
			entity.motionZ += entity.worldObj.rand.nextGaussian() * 0.15;
			if(entity instanceof EntityPlayer && entity.worldObj.rand.nextInt(100) == 0) {
				EntityPlayer player = (EntityPlayer) entity;
				int a = entity.worldObj.rand.nextInt(36);
				int b = entity.worldObj.rand.nextInt(36);
				ItemStack temp = player.inventory.mainInventory[a];
				player.inventory.mainInventory[a] = player.inventory.mainInventory[b];
				player.inventory.mainInventory[b] = temp;
			}
		}

		// septic
		if(entity.isPotionActive(HbmPotion.symptomSeptic)) {
			if(entity.worldObj.rand.nextInt(20) == 0)
				entity.attackEntityFrom(ModDamageSource.septic, 2F);
		}

		// necrosis
		if(entity.isPotionActive(HbmPotion.symptomNecrosis)) {
			if(entity.worldObj.rand.nextInt(20) == 0)
				entity.attackEntityFrom(ModDamageSource.generic, 3F);
		}

		//artery explosion
		if(entity.isPotionActive(HbmPotion.symptomAortic))
			entity.attackEntityFrom(ModDamageSource.aorta, 1000F);

		// coma
		if(entity.isPotionActive(HbmPotion.symptomComa)) {
			entity.motionX = 0;
			entity.motionZ = 0;
		}

		// cardiac
		if(entity.isPotionActive(HbmPotion.symptomCardiac)) {
			entity.motionX = 0;
			entity.motionZ = 0;
		}
	}

	private void syncHealthToBlood() {
		if(entity == null || entity.worldObj.isRemote) return;
		if(!(entity instanceof EntityPlayer)) return;
		if(entity.isPotionActive(HbmPotion.symptomCardiac)) {
			net.minecraft.entity.ai.attributes.IAttributeInstance attr = entity.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.maxHealth);
			if(attr != null && attr.getBaseValue() != 1.0D) {
				attr.setBaseValue(1.0D);
				if(entity.getHealth() > 1.0F) entity.setHealth(1.0F);
			}
			return;
		}
		float blood = getBloodAmount();
		double target = blood / 500D;
		net.minecraft.entity.ai.attributes.IAttributeInstance attr = entity.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.maxHealth);
		if(attr == null) return;
		if(attr.getBaseValue() != target) {
			attr.setBaseValue(target);
			if(entity.getHealth() > target) entity.setHealth((float) target);
		}
	}

	public float getTotalFluid() {
		float total = BASE_BLOOD;
		for(BloodEntry entry : entries) {
			if(entry.frameId == null && entry.fluidId != Fluids.HUMAN_BLOOD.getID() && entry.amount > 0) {
				total += entry.amount;
			}
		}
		return total;
	}

	@Deprecated
	@Override
	public void saveNBTData(NBTTagCompound nbt) {

		NBTTagCompound props = new NBTTagCompound();

		NBTTagList entryList = new NBTTagList();
		for(BloodEntry entry : entries) {
			NBTTagCompound entryTag = new NBTTagCompound();
			entry.writeToNBT(entryTag);
			entryList.appendTag(entryTag);
		}
		props.setTag("entries", entryList);

		NBTTagList immunityList = new NBTTagList();
		for(String genome : immuneGenomes) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("genome", genome);
			immunityList.appendTag(tag);
		}
		props.setTag("immunity", immunityList);

		nbt.setTag("HbmBloodstreamProps", props);
	}

	@Deprecated
	@Override
	public void loadNBTData(NBTTagCompound nbt) {

		NBTTagCompound props = (NBTTagCompound) nbt.getTag("HbmBloodstreamProps");

		if(props != null) {
			entries.clear();
			NBTTagList entryList = props.getTagList("entries", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < entryList.tagCount(); i++) {
				NBTTagCompound entryTag = entryList.getCompoundTagAt(i);
				BloodEntry entry = BloodEntry.readFromNBT(entryTag);
				entries.add(entry);
				if(entry.frameId != null) DiseaseRegistry.restore(entry.frameId, entry.mutationNBT);
			}

			immuneGenomes.clear();
			NBTTagList immunityList = props.getTagList("immunity", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < immunityList.tagCount(); i++) {
				NBTTagCompound tag = immunityList.getCompoundTagAt(i);
				if(tag.hasKey("genome")) {
					immuneGenomes.add(tag.getString("genome"));
				}
			}
		}
	}
}
