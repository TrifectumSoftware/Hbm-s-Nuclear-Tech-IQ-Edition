package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.Receiver;

import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

import java.util.Objects;

public class WeaponModVanadiumDamage extends WeaponModBase {

	public static final float MAX_BONUS = 3F;
	public static final float WEAR_MULTIPLIER = 10F;

	public WeaponModVanadiumDamage(int id) {
		super(id, "GENERIC_DAMAGE");
		this.setPriority(PRIORITY_MULTIPLICATIVE);
	}

	@Override
	public <T> T eval(T base, ItemStack gun, String key, Object parent) {

		if(parent instanceof GunConfig && Objects.equals(key, GunConfig.F_WEAR) && base instanceof Float) {
			return cast((Float) base * WEAR_MULTIPLIER, base);
		}

		if(parent instanceof Receiver && Objects.equals(key, Receiver.F_BASEDAMAGE) && base instanceof Float) {
			GunConfig cfg = ((Receiver) parent).getParent();
			if(cfg == null) return base;

			float max = cfg.getDurability(gun);
			if(max <= 0F) return base;

			float condition = MathHelper.clamp_float((max - ItemGunBaseNT.getWear(gun, cfg.index)) / max, 0F, 1F);
			if(condition <= 0F) return cast(0F, base);

			return cast((Float) base * (1F + (1F - condition) * MAX_BONUS), base);
		}

		return base;
	}
}
