package com.hbm.handler.blood;

import java.util.HashMap;
import java.util.Map;

import com.hbm.entity.mob.EntityHusk;
import com.hbm.explosion.ExplosionNT;
import com.hbm.extprop.HbmBloodstreamProps;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.special.ItemHumanPart.EnumBodyStat;
import com.hbm.potion.HbmPotion;
import com.hbm.packet.PermaSyncHandler;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public abstract class BloodBehavior {

	private static final Map<Integer, BloodBehavior> BEHAVIORS = new HashMap<Integer, BloodBehavior>();
	private static boolean initialized = false;

	public static void register(FluidType type, BloodBehavior behavior) {
		if(type != null && type != Fluids.NONE) BEHAVIORS.put(type.getID(), behavior);
	}

	public static BloodBehavior get(int fluidId) {
		if(!initialized) {
			initialized = true;
			registerAll();
		}
		return BEHAVIORS.get(fluidId);
	}

	public static BloodBehavior get(FluidType type) {
		return type == null ? null : get(type.getID());
	}

	public static BloodBehavior get(EntityLivingBase entity) {
		if(entity instanceof EntityHusk) return get(((EntityHusk) entity).getBloodId());

		if(entity instanceof EntityPlayer) {
			Integer synced = PermaSyncHandler.huskBlood.get(entity.getEntityId());
			if(synced != null) return get(synced.intValue());
		}

		return get(HbmBloodstreamProps.getData(entity).getBloodType());
	}

	public void applyStats(int[] totals) { }

	public void onHurt(EntityLivingBase entity, LivingHurtEvent event) { }

	public void onTick(EntityLivingBase entity) { }

	public static class StatBonus extends BloodBehavior {

		private final EnumBodyStat stat;
		private final int amount;

		public StatBonus(EnumBodyStat stat, int amount) {
			this.stat = stat;
			this.amount = amount;
		}

		@Override
		public void applyStats(int[] totals) {
			totals[this.stat.ordinal()] += this.amount;
		}
	}

	public static class Steam extends BloodBehavior {

		private final int rate;

		public Steam(int rate) {
			this.rate = rate;
		}

		@Override
		public void onTick(EntityLivingBase entity) {
			if(!entity.worldObj.isRemote || entity.worldObj.rand.nextInt(this.rate) != 0) return;

			double angle = Math.toRadians(entity.rotationYaw);
			double side = entity.worldObj.rand.nextBoolean() ? 0.22D : -0.22D;
			double x = entity.posX + Math.cos(angle) * side;
			double z = entity.posZ + Math.sin(angle) * side;
			double y = entity.posY + entity.getEyeHeight();

			entity.worldObj.spawnParticle("cloud", x, y, z, 0D, 0.015D, 0D);
		}
	}

	public static class Nitroglycerin extends BloodBehavior {

		private static final float STRENGTH = 4F;

		@Override
		public void onHurt(EntityLivingBase entity, LivingHurtEvent event) {
			if(event.source.isExplosion()) return;

			entity.extinguish();
			new ExplosionNT(entity.worldObj, entity, entity.posX, entity.posY + 0.5D, entity.posZ, STRENGTH).explode();
		}
	}

	public static class Hellish extends BloodBehavior {

		@Override
		public void onHurt(EntityLivingBase entity, LivingHurtEvent event) {
			if(event.source.isFireDamage()) event.setCanceled(true);
		}

		@Override
		public void onTick(EntityLivingBase entity) {
			if(entity.worldObj.isRemote) return;
			if(entity.isBurning()) entity.extinguish();
		}
	}

	public static class Concrete extends BloodBehavior {

		private static final float RESISTANCE = 0.6F;

		@Override
		public void onHurt(EntityLivingBase entity, LivingHurtEvent event) {
			event.ammount *= 1F - RESISTANCE;
		}

		@Override
		public void onTick(EntityLivingBase entity) {
			if(entity.worldObj.isRemote) return;
			if(entity.ticksExisted % 40 == 0) entity.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 60, 1));
		}
	}

	public static class Cay extends BloodBehavior {

		private static final int DURATION = 100;

		@Override
		public void onTick(EntityLivingBase entity) {
			if(entity.worldObj.isRemote) return;
			if(entity.ticksExisted % 20 == 0) entity.addPotionEffect(new PotionEffect(HbmPotion.turkishRage.id, DURATION, 0));
		}
	}

	public static class Helium extends BloodBehavior {

		private static final double RISE = 0.1D;

		@Override
		public void onTick(EntityLivingBase entity) {
			entity.fallDistance = 0F;
			if(entity.isSneaking()) return;

			entity.motionY = Math.max(entity.motionY, RISE);
		}
	}

	private static void registerAll() {
		register(Fluids.MEDX, new StatBonus(EnumBodyStat.VITALITY, 60));
		register(Fluids.STIMPAK, new StatBonus(EnumBodyStat.STRENGTH, 60));
		register(Fluids.SUPER_STIMPAK, new StatBonus(EnumBodyStat.STRENGTH, 90));
		register(Fluids.PSYCHO, new StatBonus(EnumBodyStat.AGILITY, 80));
		register(Fluids.RADAWAY, new StatBonus(EnumBodyStat.RECOVERY, 60));
		register(Fluids.BLOOD, new StatBonus(EnumBodyStat.AGILITY, 40));
		register(Fluids.SCUTTERBLOOD, new StatBonus(EnumBodyStat.TOUGHNESS, 60));
		register(Fluids.fromName("BLOOD_HOT"), new Steam(6));
		register(Fluids.CURDLING_BLOOD, new Steam(2));
		register(Fluids.SATANS_BLOOD, new Hellish());
		register(Fluids.CONCRETE, new Concrete());
		register(Fluids.HELIUM3, new Helium());
		register(Fluids.HELIUM4, new Helium());
		register(Fluids.CAY, new Cay());
		register(Fluids.NITROGLYCERIN, new Nitroglycerin());
	}
}
