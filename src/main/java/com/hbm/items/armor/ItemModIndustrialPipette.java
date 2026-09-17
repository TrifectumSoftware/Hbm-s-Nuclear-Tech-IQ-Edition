package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PlayerInformPacket;
import com.hbm.util.ChatBuilder;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.fluidmk2.IFillableItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IIcon;

public class ItemModIndustrialPipette extends ItemArmorMod implements IFillableItem {

	public static final int MAX_FLUID = 16000;

	@SideOnly(Side.CLIENT) private IIcon overlayIcon;

	public ItemModIndustrialPipette() {
		super(ArmorModHandler.helmet_only, true, false, false, false);
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
		return IFillableItem.getFluidType(stack) == type && IFillableItem.getFluidFill(stack) > 0;
	}

	@Override
	public int tryEmpty(FluidType type, int amount, ItemStack stack) {
		if(!providesFluid(type, stack)) return 0;
		int take = Math.min(amount, IFillableItem.getFluidFill(stack));
		int left = IFillableItem.getFluidFill(stack) - take;
		if(left <= 0) stack.stackTagCompound = null;
		else IFillableItem.setFluidFill(stack, type, (short) left);
		return take;
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
		list.add(EnumChatFormatting.LIGHT_PURPLE + I18nUtil.resolveKey("item.industrial_pipette.fill", type.getLocalizedName(), fill, MAX_FLUID));
		list.add(EnumChatFormatting.GREEN + I18nUtil.resolveKey("item.industrial_pipette.desc"));
		list.add(EnumChatFormatting.GREEN + I18nUtil.resolveKey("item.industrial_pipette.desc2"));
		list.add("");
		super.addInformation(stack, player, list, bool);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addDesc(List list, ItemStack stack, ItemStack armor) {
		FluidType type = IFillableItem.getFluidType(stack);
		int fill = IFillableItem.getFluidFill(stack);
		list.add(EnumChatFormatting.LIGHT_PURPLE + "  " + stack.getDisplayName() + " (" + type.getLocalizedName() + ": " + fill + " mB)");
	}

	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {
		if(entity.worldObj.isRemote || !(entity instanceof EntityPlayerMP)) return;
		if(entity.worldObj.getTotalWorldTime() % 5 != 0) return;

		ItemStack mod = ArmorModHandler.pryMods(armor)[ArmorModHandler.helmet_only];
		if(mod == null || mod.getItem() != this) return;

		FluidType type = IFillableItem.getFluidType(mod);
		int fill = IFillableItem.getFluidFill(mod);

		if(fill <= 0 || type == Fluids.NONE) {
			PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(I18nUtil.resolveKey("item.industrial_pipette.empty"), 975, 4000), (EntityPlayerMP) entity);
			return;
		}

		IChatComponent hud = ChatBuilder.startTranslation(type.getUnlocalizedName()).color(EnumChatFormatting.AQUA).next(": ").color(EnumChatFormatting.WHITE).next(String.valueOf(fill) + "mB / " + MAX_FLUID + "mB").flush();
		PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(hud, 975, 4000), (EntityPlayerMP) entity);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg) {
		this.itemIcon = reg.registerIcon(RefStrings.MODID + ":industrial_pipette");
		this.overlayIcon = reg.registerIcon(RefStrings.MODID + ":industrial_pipette_overlay");
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
