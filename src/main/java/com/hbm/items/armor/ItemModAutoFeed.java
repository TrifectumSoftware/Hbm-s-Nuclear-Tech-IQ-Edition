package com.hbm.items.armor;

import java.util.List;

import com.hbm.entity.effect.EntityMist;
import com.hbm.handler.ArmorModHandler;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.trait.FT_Consumable;
import com.hbm.inventory.fluid.trait.FT_Corrosive;
import com.hbm.inventory.fluid.trait.FT_Drug;
import com.hbm.inventory.fluid.trait.Injectables;
import com.hbm.lib.ModDamageSource;
import com.hbm.lib.RefStrings;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.fluidmk2.IFillableItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;

public class ItemModAutoFeed extends ItemArmorMod implements IFillableItem {

	public static final int MAX_FLUID = 4000;

	@SideOnly(Side.CLIENT)
	private IIcon emptyIcon;
	@SideOnly(Side.CLIENT)
	private IIcon fullIcon;
	@SideOnly(Side.CLIENT)
	private IIcon overlayIcon;

	public ItemModAutoFeed() {
		super(ArmorModHandler.helmet_only, true, false, false, false);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		FluidType type = IFillableItem.getFluidType(stack);
		short fill = IFillableItem.getFluidFill(stack);
		list.add(EnumChatFormatting.LIGHT_PURPLE + I18nUtil.resolveKey("item.auto_drip.fill", type.getLocalizedName(), fill, MAX_FLUID));
		list.add(EnumChatFormatting.GREEN + I18nUtil.resolveKey("item.auto_drip.desc"));
		super.addInformation(stack, player, list, bool);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addDesc(List list, ItemStack stack, ItemStack armor) {
		FluidType type = IFillableItem.getFluidType(stack);
		short fill = IFillableItem.getFluidFill(stack);
		list.add(EnumChatFormatting.RED + "  " + stack.getDisplayName() + " (" + type.getLocalizedName() + ": " + fill + " mB)");
	}

	@Override
	public boolean acceptsFluid(FluidType type, ItemStack stack) {
		if(type == com.hbm.inventory.fluid.Fluids.HUMAN_BLOOD)
			return false;
		if(type.hasTrait(com.hbm.inventory.fluid.trait.FT_Pathogen.class) || type.hasTrait(com.hbm.inventory.fluid.trait.FT_Pharma.class))
			return false;
		return type.hasTrait(FT_Consumable.class) || type.hasTrait(FT_Drug.class);
	}

	@Override
	public int tryFill(FluidType type, int amount, ItemStack stack) {
		if(!acceptsFluid(type, stack))
			return amount;
		short fill = IFillableItem.getFluidFill(stack);
		int maxFill = MAX_FLUID - fill;
		int toFill = Math.min(amount, maxFill);
		IFillableItem.setFluidFill(stack, type, (short) (fill + toFill));
		return amount - toFill;
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

	private static float getHungerAccum(ItemStack stack) {
		return stack.hasTagCompound() ? stack.stackTagCompound.getFloat("hungerAccum") : 0F;
	}

	private static void setHungerAccum(ItemStack stack, float val) {
		if(!stack.hasTagCompound()) stack.stackTagCompound = new NBTTagCompound();
		stack.stackTagCompound.setFloat("hungerAccum", val);
	}

	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {
		if(entity.worldObj.isRemote)
			return;
		ItemStack helmet = ArmorModHandler.pryMods(armor)[ArmorModHandler.helmet_only];
		if(helmet == null)
			return;
		FluidType type = IFillableItem.getFluidType(helmet);
		short fill = IFillableItem.getFluidFill(helmet);
		if(fill <= 0 || type == Fluids.NONE)
			return;
		FT_Consumable consTrait = type.getTrait(FT_Consumable.class);
		FT_Drug drugTrait = type.getTrait(FT_Drug.class);
		if(consTrait == null && drugTrait == null)
			return;

		if(type.temperature >= 100 && entity.ticksExisted % 20 == 0) {
			entity.attackEntityFrom(ModDamageSource.boiled, 2.0F);
		}

		FT_Corrosive corrosive = type.getTrait(FT_Corrosive.class);
		if(corrosive != null && entity.ticksExisted % 20 == 0) {
			entity.attackEntityFrom(ModDamageSource.acid, corrosive.getRating() / 10F);
		}

		int consumption = consTrait != null ? consTrait.consumption : drugTrait.consumption;

		if(entity.ticksExisted % 20 == 0) {
			IFillableItem.setFluidFill(helmet, type, (short) Math.max(0, fill - consumption));

			if(consTrait != null) {
				float accum = getHungerAccum(helmet) + consTrait.foodLevel;
				int whole = (int) accum;
				if(whole > 0 && entity instanceof EntityPlayer) {
					((EntityPlayer) entity).getFoodStats().addStats(whole, consTrait.saturation * whole);
					accum -= whole;
				}
				setHungerAccum(helmet, accum);

				consTrait.applyEffects(entity);
			}

			if(drugTrait != null) {
				Injectables.process(entity, type, 0, 1.0F, false);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg) {
		super.registerIcons(reg);
		this.emptyIcon = reg.registerIcon(RefStrings.MODID + ":auto_drip_empty");
		this.fullIcon = reg.registerIcon(RefStrings.MODID + ":auto_drip_full");
		this.overlayIcon = reg.registerIcon(RefStrings.MODID + ":auto_drip_overlay");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ItemStack stack, int pass) {
		short fill = IFillableItem.getFluidFill(stack);
		if(pass == 0)
			return fill >= MAX_FLUID ? this.fullIcon : this.emptyIcon;
		return fill > 0 ? this.overlayIcon : this.emptyIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int pass) {
		if(pass == 0)
			return 0xFFFFFF;
		FluidType type = IFillableItem.getFluidType(stack);
		short fill = IFillableItem.getFluidFill(stack);
		if(fill == 0)
			return 0xFFFFFF;
		return type.getColor();
	}
}
