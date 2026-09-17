package com.hbm.handler;

import java.util.List;

import com.hbm.items.armor.ItemSymbol;
import com.hbm.lib.ModDamageSource;
import com.hbm.util.EntityDamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class SymbolBehaviors {
	/// GUILT ///
	public static boolean bypassesHazards(EntityLivingBase entity) {
		return SymbolHandler.hasSymbol(entity, ItemSymbol.SymbolType.GUILT);
	}
	public static boolean bypassesSuffocation(EntityLivingBase entity) {
		if(!SymbolHandler.hasSymbol(entity, ItemSymbol.SymbolType.GUILT)) return false;
		if(entity.getAir() < 300) entity.setAir(300);
		return true;
	}
	public static boolean bypassesCorrosion(EntityLivingBase entity) {
		return SymbolHandler.hasSymbol(entity, ItemSymbol.SymbolType.GUILT);
	}
	public static boolean cancelsDamage(DamageSource source, EntityPlayer player) {
		return SymbolHandler.hasSymbol(player, ItemSymbol.SymbolType.GUILT) &&
			(source == ModDamageSource.acid || ModDamageSource.s_acid.equals(source.getDamageType()) || source == ModDamageSource.pc);
	}
	public static boolean tryHandleFireAttack(DamageSource source, EntityPlayer player, float amount) {
		if(!SymbolHandler.hasSymbol(player, ItemSymbol.SymbolType.GUILT) || !source.isFireDamage()) return false;
		if(player.getEntityData().getBoolean("ntmGuiltFire")) return false;
		player.getEntityData().setBoolean("ntmGuiltFire", true);
		try {
			EntityDamageUtil.attackEntityFromNT(player, source, amount, true, false, 1.0D, 100F, 1F);
		} finally {
			player.getEntityData().removeTag("ntmGuiltFire");
		}
		return true;
	}
	public static boolean ignoresFireRes(EntityLivingBase entity) {
		return SymbolHandler.hasSymbol(entity, ItemSymbol.SymbolType.GUILT);
	}
	public static boolean bypassesFireArmor(EntityLivingBase entity) {
		return SymbolHandler.hasSymbol(entity, ItemSymbol.SymbolType.GUILT);
	}
	public static float getFireDamage(EntityLivingBase entity, float amount) {
		return SymbolHandler.hasSymbol(entity, ItemSymbol.SymbolType.GUILT) ? amount * 2F : amount;
	}

	/// JUSTICE ///
	public static boolean hasJustice(EntityPlayer player) {
		return SymbolHandler.hasSymbol(player, ItemSymbol.SymbolType.JUSTICE);
	}

	public static boolean cancelsPvP(EntityPlayer attacker, EntityPlayer target) {
		return hasJustice(attacker) && !isMarked(attacker, target);
	}

	public static boolean isMarked(EntityPlayer player, EntityPlayer attacker) {
		NBTTagCompound justice = player.getEntityData().getCompoundTag("ntmJustice");
		String id = attacker.getUniqueID().toString();
		return justice.hasKey(id) && justice.getInteger(id) > player.ticksExisted;
	}

	public static void recordAttacker(EntityPlayer player, EntityPlayer attacker) {
		NBTTagCompound data = player.getEntityData();
		if(!data.hasKey("ntmJustice")) data.setTag("ntmJustice", new NBTTagCompound());
		data.getCompoundTag("ntmJustice").setInteger(attacker.getUniqueID().toString(), player.ticksExisted + 600);
	}

	public static void handleJusticeHit(LivingHurtEvent event, EntityPlayer player) {
		if(player.worldObj.isRemote || !hasJustice(player) || event.ammount <= 0) return;
		EntityLivingBase target = event.entityLiving;
		if(target == player) return;
		float percent = event.ammount / 100F;
		if(target.getMaxHealth() > player.getMaxHealth()) {
			target.setHealth(target.getHealth() - target.getMaxHealth() * Math.min(percent, 1F));
			if(percent > 1F) player.setHealth(player.getHealth() - player.getMaxHealth() * (percent - 1F));
			event.setCanceled(true);
		} else if(target.getMaxHealth() < player.getMaxHealth()) {
			float overkill = Math.max(0F, event.ammount - target.getMaxHealth()) / 100F;
			player.setHealth(player.getHealth() - player.getMaxHealth() * overkill);
		}
	}

	public static void deflect(Entity projectile, EntityPlayer player) {
		deflect(projectile, player, projectile.posX, projectile.posY, projectile.posZ);
	}

	public static void deflect(Entity projectile, EntityPlayer player, double hx, double hy, double hz) {
		double dx = hx - player.posX;
		double dy = hy - (player.posY + player.height * 0.5D);
		double dz = hz - player.posZ;
		double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if(dist < 1.0E-4D) { dx = 0; dy = 0; dz = 1; dist = 1; }
		dx /= dist; dy /= dist; dz /= dist;
		double dot = projectile.motionX * dx + projectile.motionY * dy + projectile.motionZ * dz;
		projectile.motionX -= 2 * dot * dx;
		projectile.motionY -= 2 * dot * dy;
		projectile.motionZ -= 2 * dot * dz;
		projectile.motionX += dx * 0.3D;
		projectile.motionY += dy * 0.3D;
		projectile.motionZ += dz * 0.3D;
		projectile.setPosition(hx + dx * 0.5D, hy + dy * 0.5D, hz + dz * 0.5D);
		projectile.velocityChanged = true;
	}

	public static void bounceProjectiles(EntityPlayer player) {
		if(!hasJustice(player)) return;
		AxisAlignedBB box = player.boundingBox.expand(0.5D, 0.5D, 0.5D);
		List list = player.worldObj.getEntitiesWithinAABBExcludingEntity(player, box);
		for(Object o : list) {
			if(!(o instanceof IProjectile)) continue;
			Entity projectile = (Entity) o;
			if(projectile.motionX * (player.posX - projectile.posX) + projectile.motionY * (player.posY + player.height * 0.5D - projectile.posY) + projectile.motionZ * (player.posZ - projectile.posZ) <= 0) continue;
			deflect(projectile, player);
		}
	}
}
