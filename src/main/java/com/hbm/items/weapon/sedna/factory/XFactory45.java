package com.hbm.items.weapon.sedna.factory;

import com.hbm.items.ItemEnums.EnumCasingType;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.factory.GunFactory.EnumAmmo;
import com.hbm.particle.SpentCasing;
import com.hbm.particle.SpentCasing.CasingType;

public class XFactory45 {

	public static BulletConfig p45_sp;
	public static BulletConfig p45_fmj;
	public static BulletConfig p45_jhp;
	public static BulletConfig p45_ap;
	public static BulletConfig p45_du;
	public static BulletConfig p45_desh;
	public static BulletConfig p45_magnetic;
	public static BulletConfig p45_laced;
	
	public static void init() {
		SpentCasing casing9 = new SpentCasing(CasingType.STRAIGHT).setColor(SpentCasing.COLOR_CASE_BRASS).setScale(1F, 1F, 0.75F);
		p45_sp = new BulletConfig().setItem(EnumAmmo.P45_SP).setCasing(EnumCasingType.SMALL, 8)
				.setCasing(casing9.clone().register("p45"));
		p45_fmj = new BulletConfig().setItem(EnumAmmo.P45_FMJ).setCasing(EnumCasingType.SMALL, 8).setDamage(0.8F).setThresholdNegation(2F).setArmorPiercing(0.1F)
				.setCasing(casing9.clone().register("p45fmj"));
		p45_jhp = new BulletConfig().setItem(EnumAmmo.P45_JHP).setCasing(EnumCasingType.SMALL, 8).setDamage(1.5F).setHeadshot(1.5F).setArmorPiercing(-0.25F)
				.setCasing(casing9.clone().register("p45jhp"));
		p45_ap = new BulletConfig().setItem(EnumAmmo.P45_AP).setCasing(EnumCasingType.SMALL_STEEL, 8).setDoesPenetrate(true).setDamageFalloffByPen(false).setDamage(1.25F).setThresholdNegation(5F).setArmorPiercing(0.15F)
				.setCasing(casing9.clone().setColor(SpentCasing.COLOR_CASE_44).register("p45ap"));
		p45_du = new BulletConfig().setItem(EnumAmmo.P45_DU).setCasing(EnumCasingType.SMALL_STEEL, 8).setDoesPenetrate(true).setDamageFalloffByPen(false).setDamage(2.5F).setThresholdNegation(15F).setArmorPiercing(0.25F)
				.setCasing(casing9.clone().setColor(SpentCasing.COLOR_CASE_44).register("p45du"));
		p45_desh = new BulletConfig().setItem(EnumAmmo.P45_DESH).setCasing(EnumCasingType.SMALL_STEEL, 8).setDoesPenetrate(true).setDamageFalloffByPen(false).setDamage(1.5F).setThresholdNegation(15F).setArmorPiercing(0.5F).setRicochetAngle(90F).setRicochetCount(100).setLife(800)
				.setCasing(casing9.clone().setColor(SpentCasing.COLOR_CASE_44).register("p45desh"));
			p45_magnetic = new BulletConfig().setItem(EnumAmmo.P45_MAGNETIC).setDamage(1.0F).setVel(6F).setLife(600).setRicochetAngle(90F).setRicochetCount(100).setOnUpdate(BulletConfig.LAMBDA_MAGNETIC_HOMING);
		p45_laced = new BulletConfig().setItem(EnumAmmo.P45_LACED).setCasing(EnumCasingType.SMALL, 8).setDamage(1F).setLaced().setOnEntityHit(BulletConfig.LAMBDA_LACED_ENTITY_HIT);
	}
}
