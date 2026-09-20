package com.hbm.items;

import java.util.List;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.tool.ItemMedicalSyringe;
import com.hbm.lib.RefStrings;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.fluidmk2.IFillableItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;

public class ItemVial extends Item implements IFillableItem {

	public static final int MAX_FLUID = 100;

	@SideOnly(Side.CLIENT) private IIcon overlayIcon;

	public ItemVial() {
		this.setUnlocalizedName("vial");
		this.setTextureName(RefStrings.MODID + ":vial");
		this.setCreativeTab(null);
		this.setMaxStackSize(64);
	}

	public static String readFrame(ItemStack vial) {
		if(vial == null || !vial.hasTagCompound()) return null;
		NBTTagCompound nbt = vial.stackTagCompound;
		if(nbt.hasKey("frame")) return nbt.getString("frame");
		if(nbt.hasKey("pathogen")) return nbt.getCompoundTag("pathogen").getString("frame");
		return null;
	}

	@Override
	public boolean acceptsFluid(FluidType type, ItemStack stack) {
		return true;
	}

	@Override
	public int tryFill(FluidType type, int amount, ItemStack stack) {
		short fill = IFillableItem.getFluidFill(stack);
		if(fill > 0 && IFillableItem.getFluidType(stack) != type) return amount;
		int add = Math.min(amount, MAX_FLUID - fill);
		IFillableItem.setFluidFill(stack, type, (short) (fill + add));
		return amount - add;
	}

	@Override
	public boolean providesFluid(FluidType type, ItemStack stack) {
		return false;
	}

	@Override
	public int tryEmpty(FluidType type, int amount, ItemStack stack) {
		return amount;
	}

	@Override
	public FluidType getFirstFluidType(ItemStack stack) {
		FluidType type = IFillableItem.getFluidType(stack);
		return type == Fluids.NONE ? null : type;
	}

	@Override
	public int getFill(ItemStack stack) {
		return IFillableItem.getFluidFill(stack);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		FluidType type = IFillableItem.getFluidType(stack);
		int fill = IFillableItem.getFluidFill(stack);
		list.add(type.getLocalizedName() + ": " + fill + "mB");
		String sample = com.hbm.handler.contagion.GenomeSample.getKey(stack);
		if(sample != null) {
			list.add(EnumChatFormatting.AQUA + I18nUtil.resolveKey("item.vial.genomeSample", sample, com.hbm.handler.contagion.GenomeSample.getValue(stack)));
		}
		if(stack.hasTagCompound() && stack.stackTagCompound.hasKey(ItemMedicalSyringe.KEY_OWNER_NAME)) {
			list.add(I18nUtil.resolveKey("desc.item.medicalSyringe.owner", stack.stackTagCompound.getString(ItemMedicalSyringe.KEY_OWNER_NAME)));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg) {
		this.itemIcon = reg.registerIcon(RefStrings.MODID + ":vial");
		this.overlayIcon = reg.registerIcon(RefStrings.MODID + ":vial_overlay");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ItemStack stack, int pass) {
		if(pass == 1 && IFillableItem.getFluidFill(stack) > 0) return overlayIcon;
		return getIconFromDamageForRenderPass(stack.getItemDamage(), pass);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int pass) {
		if(pass == 1 && IFillableItem.getFluidFill(stack) > 0) {
			int color = IFillableItem.getFluidType(stack).getColor();
			return color < 0 ? 0xffffff : color;
		}
		return 0xffffff;
	}
}
