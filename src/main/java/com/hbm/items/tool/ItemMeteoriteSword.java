package com.hbm.items.tool;

import java.util.List;

import com.hbm.handler.ability.IWeaponAbility;
import com.hbm.util.i18n.I18nUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.util.EnumChatFormatting;

public class ItemMeteoriteSword extends ItemSwordAbility implements IMeteoriteTool {

	public final int tier;

	public ItemMeteoriteSword(int tier, float damage, double movement, ToolMaterial material) {
		super(damage, movement, material);
		this.tier = tier;
		this.addAbility(IWeaponAbility.BOBBLE, 0);
		this.addAbility(IWeaponAbility.CLEAVE, ItemMeteoriteBase.cleaveLevel(tier));
		this.setMaxDamage(4000 + tier * 500);
	}

	@Override
	public int getTier() {
		return tier;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		super.addInformation(stack, player, list, ext);
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase victim, EntityLivingBase attacker) {
		if(!attacker.worldObj.isRemote && attacker instanceof EntityPlayer && canOperate(stack)) {
			IWeaponAbility.CLEAVE.onHit(ItemMeteoriteBase.cleaveLevel(tier), attacker.worldObj, (EntityPlayer) attacker, victim, this);
		}
		stack.damageItem(1, attacker);
		return true;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String base = I18nUtil.resolveKey("item.meteorite_sword.name");
		String tierName = ItemMeteoriteBase.tierName(tier);
		if(tierName.isEmpty()) return base;
		return I18nUtil.resolveKey("item.meteorite_tier." + tierName) + " " + base;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		String tierName = ItemMeteoriteBase.tierName(tier);
		if(tierName.isEmpty()) return super.getUnlocalizedName(stack);
		return super.getUnlocalizedName() + "." + tierName;
	}
}
