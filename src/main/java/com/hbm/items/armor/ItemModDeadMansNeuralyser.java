package com.hbm.items.armor;

import java.util.List;

import com.hbm.entity.mob.EntityHusk;
import com.hbm.handler.ArmorModHandler;
import com.hbm.items.tool.ItemNeuralyser;
import com.hbm.main.MainRegistry;
import com.hbm.main.ServerProxy;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemModDeadMansNeuralyser extends ItemArmorMod {

	public ItemModDeadMansNeuralyser() {
		super(ArmorModHandler.extra, true, true, true, true);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		if(ItemNeuralyser.getLinkedUUID(stack) != null) {
			list.add(EnumChatFormatting.GRAY + ItemNeuralyser.getLinkedName(stack, player.worldObj));
		} else {
			list.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("item.neuralyser.hint"));
		}
		list.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("item.deadmans_neuralyser.desc"));

		super.addInformation(stack, player, list, bool);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean inHand) {
		if(!world.isRemote || !inHand || !(entity instanceof EntityPlayer)) return;
		if(ItemNeuralyser.getLinkedUUID(stack) == null) return;

		EntityPlayer player = (EntityPlayer) entity;
		EntityHusk husk = ItemNeuralyser.getLinkedHusk(stack, world, player, Double.MAX_VALUE);
		if(husk != null) {
			MainRegistry.proxy.displayTooltip(I18nUtil.resolveKey("item.neuralyser.hud", husk.getHuskDisplayName(), (int) player.getDistanceToEntity(husk)), ServerProxy.ID_NEURALYSER);
		} else {
			MainRegistry.proxy.displayTooltip(I18nUtil.resolveKey("item.neuralyser.hud.lost"), ServerProxy.ID_NEURALYSER);
		}
	}

	public static EntityHusk getLinkedHusk(EntityLivingBase entity, ItemStack modStack) {
		String uuid = ItemNeuralyser.getLinkedUUID(modStack);
		if(uuid == null) return null;

		EntityHusk husk = ItemNeuralyser.findHusk(entity.worldObj, uuid);
		return (husk != null && husk.isAdopted()) ? husk : null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addDesc(List list, ItemStack stack, ItemStack armor) {
		list.add(EnumChatFormatting.GRAY + "  " + stack.getDisplayName());
	}
}
