package com.hbm.items.tool;

import java.util.List;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;

import api.hbm.fluidmk2.IFillableItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemNotchSyringe extends ItemMedicalSyringe {

	private static final String OWNER_UUID = "069a79f4-44e9-4726-a5be-fca90e38aaf5";
	private static final String OWNER_NAME = "Notch";

	public ItemNotchSyringe() {
		super();
		this.setUnlocalizedName("medical_syringe_notch");
		this.setTextureName(RefStrings.MODID + ":medical_syringe");
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		ItemStack stack = new ItemStack(this);
		applySample(stack);
		list.add(stack);
	}

	private static void applySample(ItemStack stack) {
		IFillableItem.setFluidFill(stack, Fluids.HUMAN_BLOOD, (short) MAX_DOSE);
		stack.stackTagCompound.setString(KEY_OWNER_UUID, OWNER_UUID);
		stack.stackTagCompound.setString(KEY_OWNER_NAME, OWNER_NAME);
	}
}
