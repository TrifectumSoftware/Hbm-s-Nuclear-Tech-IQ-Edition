package com.hbm.items.tool;

import java.util.List;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.trait.Injectables;
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
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class ItemFluidSyringe extends Item implements IFillableItem {

	@SideOnly(Side.CLIENT) protected IIcon overlayIcon;
	@SideOnly(Side.CLIENT) protected IIcon emptyIcon;

	private final short maxDose;
	private final String overlayTex;
	private final String emptyTex;

	public ItemFluidSyringe(int maxDose, String overlayTex, String emptyTex) {
		this.setMaxStackSize(1);
		this.maxDose = (short) maxDose;
		this.overlayTex = overlayTex;
		this.emptyTex = emptyTex;
	}

	public short getMaxFill() {
		return maxDose;
	}

	public void initNBT(ItemStack stack) {
		stack.stackTagCompound = new NBTTagCompound();
		this.setFill(stack, Fluids.NONE, (short) 0);
		stack.stackTagCompound.setShort("capacity", this.getMaxFill());
	}

	public FluidType getType(ItemStack stack) {
		if(!stack.hasTagCompound()) initNBT(stack);
		return Fluids.fromID(stack.stackTagCompound.getShort("type"));
	}

	public short getCapacity(ItemStack stack) {
		if(!stack.hasTagCompound()) initNBT(stack);
		return stack.stackTagCompound.getShort("capacity");
	}

	public void setFill(ItemStack stack, FluidType type, short fill) {
		if(!stack.hasTagCompound()) initNBT(stack);
		stack.stackTagCompound.setShort("type", (short) type.getID());
		stack.stackTagCompound.setShort("fill", fill);
	}

	@Override
	public int getFill(ItemStack stack) {
		if(!stack.hasTagCompound()) initNBT(stack);
		return stack.stackTagCompound.getShort("fill");
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if(!stack.hasTagCompound()) initNBT(stack);
		int fill = this.getFill(stack);
		if(fill <= 0) return stack;

		if(!world.isRemote) {
			FluidType type = this.getType(stack);
			float intensity = 1.0F;
			int dose;

			if(this.maxDose > 10 && player.isSneaking()) {
				dose = Math.min(10 * 10, fill);
				intensity = 2.0F;
			} else {
				dose = Math.min(10, fill);
			}

			Injectables.process(player, type, dose, intensity, true);
			Injectables.applyStackPayload(stack, player, intensity);
			int newFill = fill - dose;
			if(newFill == 0) this.setFill(stack, Fluids.NONE, (short) 0);
			else this.setFill(stack, type, (short) newFill);
			world.playSoundAtEntity(player, "hbm:item.syringe", 1.0F, 1.0F);
		}

		return stack;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		list.add((maxDose > 10 ? I18nUtil.resolveKey("desc.item.syringe.combat") : I18nUtil.resolveKey("desc.item.syringe.single")) + " " + this.getType(stack).getLocalizedName());
		list.add(I18nUtil.resolveKey("desc.item.syringe.amount") + " " + this.getFill(stack) + "/" + this.getCapacity(stack) + "mB");
		if(maxDose > 10) {
			list.add(I18nUtil.resolveKey("desc.item.syringe.useSelfSneak"));
			list.add(I18nUtil.resolveKey("desc.item.syringe.sneakOverdose"));
		} else {
			list.add(I18nUtil.resolveKey("desc.item.syringe.useSelf"));
		}
		list.add(I18nUtil.resolveKey("desc.item.syringe.useEntity"));
	}

	@Override
	public boolean acceptsFluid(FluidType type, ItemStack stack) {
		return (type == this.getType(stack) || this.getFill(stack) == 0);
	}

	@Override
	public int tryFill(FluidType type, int amount, ItemStack stack) {
		if(!acceptsFluid(type, stack)) return amount;
		if(this.getFill(stack) == 0) this.setFill(stack, type, (short) 0);
		int req = this.getCapacity(stack) - this.getFill(stack);
		int toFill = Math.min(req, amount);
		this.setFill(stack, type, (short) (this.getFill(stack) + toFill));
		return amount - toFill;
	}

	@Override
	public boolean providesFluid(FluidType type, ItemStack stack) {
		return this.getType(stack) == type;
	}

	@Override
	public int tryEmpty(FluidType type, int amount, ItemStack stack) {
		if(providesFluid(type, stack)) {
			int toUnload = Math.min(amount, this.getFill(stack));
			this.setFill(stack, type, (short) (this.getFill(stack) - toUnload));
			if(this.getFill(stack) == 0) this.setFill(stack, Fluids.NONE, (short) 0);
			return toUnload;
		}
		return amount;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister icon) {
		super.registerIcons(icon);
		this.overlayIcon = icon.registerIcon(RefStrings.MODID + ":" + overlayTex);
		this.emptyIcon = icon.registerIcon(RefStrings.MODID + ":" + emptyTex);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ItemStack stack, int pass) {
		if(getFill(stack) == 0 && pass == 1) return this.emptyIcon;
		return pass == 1 ? this.overlayIcon : getIconFromDamageForRenderPass(stack.getItemDamage(), pass);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int pass) {
		if(pass == 0) return 0xffffff;
		if(this.getFill(stack) == 0) return 0xffffff;
		int j = this.getType(stack).getColor();
		if(j < 0) j = 0xffffff;
		return j;
	}

	@Override
	public FluidType getFirstFluidType(ItemStack stack) {
		return this.getType(stack);
	}
}
