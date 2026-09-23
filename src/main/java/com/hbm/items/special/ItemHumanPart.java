package com.hbm.items.special;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.hbm.items.ItemEnumMulti;
import com.hbm.items.ModItems;
import com.hbm.util.i18n.I18nUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

public class ItemHumanPart extends ItemEnumMulti {

	private static final Random rand = new Random();
	private static final float TRAIT_CHANCE = 0.2F;

	public ItemHumanPart() {
		super(EnumHumanPart.class, true, true);
	}

	public static ItemStack make(EnumHumanPart part, int min, int max) {
		ItemStack stack = new ItemStack(ModItems.human_part, 1, part.ordinal());
		stack.stackTagCompound = new NBTTagCompound();

		for(EnumBodyStat stat : EnumBodyStat.values()) {
			int value = max <= min ? min : min + rand.nextInt(max - min + 1);
			stack.stackTagCompound.setInteger(stat.name(), Math.min(100, value));
		}

		List<EnumPartTrait> traits = EnumPartTrait.of(part);
		if(rand.nextFloat() < TRAIT_CHANCE) {
			stack.stackTagCompound.setString("trait", traits.get(rand.nextInt(traits.size())).name());
		}

		return stack;
	}

	public static int getStat(ItemStack stack, EnumBodyStat stat) {
		return stack != null && stack.hasTagCompound() ? stack.stackTagCompound.getInteger(stat.name()) : 0;
	}

	public static EnumPartTrait getTrait(ItemStack stack) {
		if(stack == null || !stack.hasTagCompound()) return null;

		String name = stack.stackTagCompound.getString("trait");
		if(name.isEmpty()) return null;

		try {
			return EnumPartTrait.valueOf(name);
		} catch(IllegalArgumentException ex) {
			return null;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		for(EnumHumanPart part : EnumHumanPart.values()) list.add(make(part, 0, 100));
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {

		EnumPartTrait trait = getTrait(stack);
		if(trait != null) {
			list.add(EnumChatFormatting.RED + I18nUtil.resolveKey("item.human_part.trait." + trait.name().toLowerCase(Locale.US)));
		}

		for(EnumBodyStat stat : EnumBodyStat.values()) {
			list.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("item.human_part.stat." + stat.name().toLowerCase(Locale.US)) + ": " + getStat(stack, stat) + "%");
		}
	}

	public enum EnumHumanPart {
		HEAD, TORSO, ARMS, LEGS, HEART;
	}

	public enum EnumBodyStat {
		VITALITY, STRENGTH, AGILITY, TOUGHNESS, RECOVERY;
	}

	public enum EnumPartTrait {
		TWITCH(EnumHumanPart.HEAD), NUMB(EnumHumanPart.HEAD),
		HARDY(EnumHumanPart.TORSO), GRISTLE(EnumHumanPart.TORSO),
		BLOODTHIRST(EnumHumanPart.ARMS), WHITE_KNUCKLES(EnumHumanPart.ARMS),
		SURE_FOOTED(EnumHumanPart.LEGS), SPRING_HEEL(EnumHumanPart.LEGS),
		SECOND_WIND(EnumHumanPart.HEART), CLOTTING(EnumHumanPart.HEART);

		public final EnumHumanPart part;

		private EnumPartTrait(EnumHumanPart part) {
			this.part = part;
		}

		public static List<EnumPartTrait> of(EnumHumanPart part) {
			List<EnumPartTrait> list = new ArrayList<EnumPartTrait>();
			for(EnumPartTrait trait : values()) if(trait.part == part) list.add(trait);
			return list;
		}
	}
}
