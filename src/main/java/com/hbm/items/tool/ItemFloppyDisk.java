package com.hbm.items.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hbm.handler.contagion.DiseaseDefinition;
import com.hbm.handler.contagion.DiseaseRegistry;
import com.hbm.lib.RefStrings;
import com.hbm.util.i18n.I18nUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;

public class ItemFloppyDisk extends Item {

	@SideOnly(Side.CLIENT) private IIcon overlayIcon;

	public ItemFloppyDisk() {
		this.setMaxStackSize(1);
		this.setUnlocalizedName("floppy_disk");
		this.setTextureName(RefStrings.MODID + ":floppy");
	}

	private static NBTTagCompound getPathogen(ItemStack stack) {
		if(stack != null && stack.hasTagCompound() && stack.stackTagCompound.hasKey("pathogen")) {
			return stack.stackTagCompound.getCompoundTag("pathogen");
		}
		return null;
	}

	public static String getFrameId(ItemStack stack) {
		NBTTagCompound pathogen = getPathogen(stack);
		return pathogen == null ? null : pathogen.getString("frame");
	}

	public static String getGenome(ItemStack stack) {
		NBTTagCompound pathogen = getPathogen(stack);
		return pathogen == null ? null : pathogen.getString("genome");
	}

	public static boolean isSequenced(ItemStack stack) {
		NBTTagCompound pathogen = getPathogen(stack);
		return pathogen != null && pathogen.getBoolean("sequenced");
	}

	public static void markSequenced(ItemStack stack) {
		NBTTagCompound pathogen = getPathogen(stack);
		if(pathogen != null) pathogen.setBoolean("sequenced", true);
	}

	public static String getDiskName(ItemStack stack) {
		NBTTagCompound pathogen = getPathogen(stack);
		return pathogen == null ? null : (pathogen.hasKey("name") ? pathogen.getString("name") : null);
	}

	public static void setDiskName(ItemStack stack, String name) {
		NBTTagCompound pathogen = getPathogen(stack);
		if(pathogen != null) pathogen.setString("name", name);
	}

	public static void setPathogenData(ItemStack stack, String frameId, float amount, String genome, NBTTagCompound mutationNBT) {
		if(!stack.hasTagCompound()) stack.stackTagCompound = new NBTTagCompound();
		NBTTagCompound pathogen = new NBTTagCompound();
		pathogen.setString("frame", frameId);
		pathogen.setFloat("amount", amount);
		if(genome != null && !genome.isEmpty()) pathogen.setString("genome", genome);
		if(mutationNBT != null) pathogen.setTag("mut", mutationNBT);
		stack.stackTagCompound.setTag("pathogen", pathogen);
	}

	public static void storeDef(ItemStack stack, DiseaseDefinition def) {
		NBTTagCompound pathogen = getPathogen(stack);
		if(pathogen == null || def == null) return;
		pathogen.setTag("stats", def.toNBT());
	}

	public static DiseaseDefinition getStoredDef(ItemStack stack) {
		NBTTagCompound pathogen = getPathogen(stack);
		if(pathogen == null || !pathogen.hasKey("stats")) return null;
		DiseaseDefinition def = DiseaseDefinition.readFromNBT(pathogen.getCompoundTag("stats"));
		if(def != null) def.id = getFrameId(stack);
		return def;
	}

	public static List<String> getAnalysisLines(ItemStack stack) {
		List<String> lines = new ArrayList<String>();

		String frameId = getFrameId(stack);
		if(frameId == null) return lines;

		DiseaseDefinition def = DiseaseRegistry.get(frameId);
		if(def == null) def = getStoredDef(stack);
		String diskName = getDiskName(stack);

		String defaultName = def != null && !def.displayName.isEmpty() ? def.displayName : frameId;
		lines.add("NAME: " + (diskName != null && !diskName.isEmpty() ? diskName : defaultName));
		lines.add("");

		if(def != null) {
			lines.add("TYPE: " + def.type.name());
			lines.add("INCUBATION: " + fmt(def.incubationTicks));
			lines.add("DURATION: " + fmt(def.durationTicks));
			lines.add("SEVERITY: " + pct(def.baseSeverity));
			lines.add("RESISTANCE: " + pct(def.baseResistance));
			lines.add("MUTATION: " + df(def.baseMutationRate));
			lines.add("ANTIGEN: " + pct(def.antigenMutability));
		}

		Map<DiseaseDefinition.TransmissionAxis, Float> transmission = def != null ? def.transmission : null;
		if(transmission != null && !transmission.isEmpty()) {
			lines.add("");
			lines.add("TRANSMISSION");
			for(Map.Entry<DiseaseDefinition.TransmissionAxis, Float> entry : transmission.entrySet()) {
				lines.add("  " + entry.getKey().name() + ": " + pct(entry.getValue()));
			}
		}

		if(def != null) {
			lines.add("");
			lines.add(def.uncurable ? "CURE: IMPOSSIBLE" : "CURE: ANTISERUM");
		}

		return lines;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		String frameId = getFrameId(stack);
		if(frameId != null) {
			String genome = getGenome(stack);
			if(genome != null && !genome.isEmpty()) {
				list.add(EnumChatFormatting.ITALIC + I18nUtil.resolveKey("desc.item.floppy.strain", colorStrain(genome, 0)));
			} else {
				list.add(EnumChatFormatting.YELLOW + frameId);
			}
		} else {
			list.add(EnumChatFormatting.GRAY + I18nUtil.resolveKey("desc.item.floppy.blank"));
		}
	}

	private static String colorStrain(String hex, int offset) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < hex.length(); i++) {
			int bit = offset + i;
			EnumChatFormatting c = bit < 8 ? EnumChatFormatting.RED : bit < 16 ? EnumChatFormatting.AQUA : EnumChatFormatting.GREEN;
			sb.append(c).append(hex.charAt(i));
		}
		return sb.toString();
	}

	private static String fmt(int ticks) {
		int sec = ticks / 20;
		int min = sec / 60;
		sec %= 60;
		if(min > 0) return min + "m " + sec + "s";
		return sec + "s";
	}

	private static String pct(float f) {
		return (int) (f * 100) + "%";
	}

	private static String df(float f) {
		return String.format("%.3f", f);
	}

	public static int getPathogenColor(String frameId) {
		if(frameId == null || frameId.isEmpty()) return 0xFFFFFF;
		int hash = frameId.hashCode();
		float hue = ((hash % 360) + 360) % 360 / 360.0F;
		float sat = 0.7F + (Math.abs(hash >> 8) % 30) / 100.0F;
		int rgb = hsvToRgb(hue, sat, 1.0F);
		return rgb;
	}

	private static int hsvToRgb(float h, float s, float v) {
		int i = (int) (h * 6);
		float f = h * 6 - i;
		float p = v * (1 - s);
		float q = v * (1 - f * s);
		float t = v * (1 - (1 - f) * s);
		float r = 0, g = 0, b = 0;
		switch(i % 6) {
		case 0: r = v; g = t; b = p; break;
		case 1: r = q; g = v; b = p; break;
		case 2: r = p; g = v; b = t; break;
		case 3: r = p; g = q; b = v; break;
		case 4: r = t; g = p; b = v; break;
		case 5: r = v; g = p; b = q; break;
		}
		return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg) {
		this.itemIcon = reg.registerIcon(RefStrings.MODID + ":floppy");
		this.overlayIcon = reg.registerIcon(RefStrings.MODID + ":floppy_overlay");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ItemStack stack, int pass) {
		if(pass == 1 && getFrameId(stack) != null) return this.overlayIcon;
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
		if(pass == 1) {
			String frameId = getFrameId(stack);
			if(frameId != null) return getPathogenColor(frameId);
		}
		return 0xFFFFFF;
	}
}
