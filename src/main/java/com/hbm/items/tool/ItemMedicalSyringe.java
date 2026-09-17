package com.hbm.items.tool;

import java.util.List;

import com.hbm.extprop.HbmBloodstreamProps;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.handler.contagion.BloodEntry;
import com.hbm.handler.contagion.DiseaseInstance;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.fluidmk2.IFillableItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class ItemMedicalSyringe extends Item implements IFillableItem {

	public static final int MAX_DOSE = 1000;
	public static final String KEY_OWNER_UUID = "ownerUUID";
	public static final String KEY_OWNER_NAME = "ownerName";
	public static final String KEY_PATHOGENS = "pathogen";

	@SideOnly(Side.CLIENT) private IIcon overlayIcon;

	public ItemMedicalSyringe() {
		this.setMaxStackSize(1);
		this.setUnlocalizedName("medical_syringe");
		this.setTextureName(RefStrings.MODID + ":medical_syringe");
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(world.isRemote) return stack;

		if(player.isSneaking()) {
			if(getFill(stack) > 0) inject(stack, player);
		} else {
			if(getFill(stack) == 0) extractSample(stack, player);
		}
		return stack;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {

		if(entity.worldObj.isRemote || !(entity instanceof EntityLivingBase)) return true;

		if(player.isSneaking()) {
			if(getFill(stack) > 0) inject(stack, (EntityLivingBase) entity);
		} else {
			if(getFill(stack) == 0) extractSample(stack, (EntityLivingBase) entity);
		}
		return true;
	}

	private void inject(ItemStack stack, EntityLivingBase target) {

		FluidType type = IFillableItem.getFluidType(stack);
		int fill = getFill(stack);

		com.hbm.inventory.fluid.trait.Injectables.process(target, type, fill, 1.0F, true);
		com.hbm.inventory.fluid.trait.Injectables.applyStackPayload(stack, target, 1.0F);

		stack.stackTagCompound = null;
		IFillableItem.setFluidFill(stack, Fluids.NONE, (short) 0);
		target.worldObj.playSoundAtEntity(target, "hbm:item.syringe", 1.0F, 1.0F);
	}

	private void extractSample(ItemStack stack, EntityLivingBase target) {

		NBTTagList pathogenList = new NBTTagList();

		for(BloodEntry entry : HbmBloodstreamProps.getData(target).getEntries()) {
			if(entry.frameId == null) continue;
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("frame", entry.frameId);
			tag.setFloat("amount", entry.amount);
			if(entry.mutationNBT != null) tag.setTag("mut", entry.mutationNBT);
			pathogenList.appendTag(tag);
		}

		for(DiseaseInstance instance : HbmLivingProps.getDiseases(target).values()) {
			if(instance.frameId == null) continue;
			NBTTagCompound mut = new NBTTagCompound();
			instance.writeToNBT(mut);
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("frame", instance.frameId);
			tag.setFloat("amount", 10F);
			tag.setTag("mut", mut);
			pathogenList.appendTag(tag);
		}

		IFillableItem.setFluidFill(stack, Fluids.HUMAN_BLOOD, (short) MAX_DOSE);
		if(!stack.hasTagCompound()) stack.stackTagCompound = new NBTTagCompound();
		stack.stackTagCompound.setString(KEY_OWNER_UUID, target.getUniqueID().toString());
		stack.stackTagCompound.setString(KEY_OWNER_NAME, target.getCommandSenderName());
		stack.stackTagCompound.setTag(KEY_PATHOGENS, pathogenList);
		target.attackEntityFrom(DamageSource.generic, 1.0F);
		target.worldObj.playSoundAtEntity(target, "hbm:item.syringe", 1.0F, 1.0F);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		list.add(I18nUtil.resolveKey("desc.item.medicalSyringe.useSelf"));
		list.add(I18nUtil.resolveKey("desc.item.medicalSyringe.useEntity"));
		list.add(I18nUtil.resolveKey("desc.item.medicalSyringe.sneakSelf"));
		list.add(I18nUtil.resolveKey("desc.item.medicalSyringe.sneakEntity"));
		if(getFill(stack) > 0) {
			FluidType type = IFillableItem.getFluidType(stack);
			list.add(type.getLocalizedName() + ": " + getFill(stack) + "mB");
			if(stack.hasTagCompound() && stack.stackTagCompound.hasKey(KEY_OWNER_NAME)) {
				list.add(I18nUtil.resolveKey("desc.item.medicalSyringe.owner", stack.stackTagCompound.getString(KEY_OWNER_NAME)));
			}
		}
	}

	@Override
	public boolean acceptsFluid(FluidType type, ItemStack stack) {
		return getFill(stack) == 0 || IFillableItem.getFluidType(stack) == type;
	}

	@Override
	public int tryFill(FluidType type, int amount, ItemStack stack) {
		if(!acceptsFluid(type, stack)) return amount;
		int add = Math.min(amount, MAX_DOSE - getFill(stack));
		IFillableItem.setFluidFill(stack, type, (short) (getFill(stack) + add));
		return amount - add;
	}

	@Override
	public boolean providesFluid(FluidType type, ItemStack stack) {
		return IFillableItem.getFluidType(stack) == type;
	}

	@Override
	public int tryEmpty(FluidType type, int amount, ItemStack stack) {
		if(providesFluid(type, stack)) {
			int toUnload = Math.min(amount, getFill(stack));
			IFillableItem.setFluidFill(stack, type, (short) (getFill(stack) - toUnload));
			return toUnload;
		}
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
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister icon) {
		super.registerIcons(icon);
		this.overlayIcon = icon.registerIcon(RefStrings.MODID + ":medical_syringe_overlay");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ItemStack stack, int pass) {
		if(pass == 1 && getFill(stack) > 0) return this.overlayIcon;
		return getIconFromDamageForRenderPass(stack.getItemDamage(), pass);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int pass) {
		if(pass == 1 && getFill(stack) > 0) {
			int color = IFillableItem.getFluidType(stack).getColor();
			return color < 0 ? 0xffffff : color;
		}
		return 0xffffff;
	}
}
