package com.hbm.inventory.fluid.trait;

import java.io.IOException;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.ModBlocks;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class FT_Ink extends FluidTrait {

	private int color;

	private static final int[] PLATEMETAL_META = { 2, 14, 13, 12, 11, 10, 9, -1, 5, 8, 7, 6, -1, 4, 3, 1 };

	public FT_Ink() { }

	public FT_Ink(int color) {
		this.color = color;
	}

	public int getColor() {
		return color;
	}

	public static int getPlatemetalMeta(int color) {
		return PLATEMETAL_META[color];
	}

	public boolean applyColor(World world, int x, int y, int z, Block block) {

		if(block == Blocks.wool) {
			world.setBlock(x, y, z, Blocks.wool, color, 3);
			return true;
		}
		if(block == Blocks.carpet) {
			world.setBlock(x, y, z, Blocks.carpet, color, 3);
			return true;
		}
		if(block == Blocks.glass || block == Blocks.stained_glass) {
			world.setBlock(x, y, z, Blocks.stained_glass, color, 3);
			return true;
		}
		if(block == Blocks.glass_pane || block == Blocks.stained_glass_pane) {
			world.setBlock(x, y, z, Blocks.stained_glass_pane, color, 3);
			return true;
		}
		if(block == Blocks.hardened_clay || block == Blocks.stained_hardened_clay) {
			world.setBlock(x, y, z, Blocks.stained_hardened_clay, color, 3);
			return true;
		}
		if(block == ModBlocks.concrete_smooth || block == ModBlocks.concrete_colored) {
			world.setBlock(x, y, z, ModBlocks.concrete_colored, color, 3);
			return true;
		}
		if(block == ModBlocks.platemetal) {
			int meta = PLATEMETAL_META[color];
			if(meta < 0) return false;
			world.setBlock(x, y, z, ModBlocks.platemetal, meta, 3);
			return true;
		}
		if(block == ModBlocks.sheetmetal || block == ModBlocks.sheetmetal_colored) {
			world.setBlock(x, y, z, ModBlocks.sheetmetal_colored, 15 - color, 3);
			return true;
		}
		return false;
	}

	@Override
	public void addInfo(List<String> info) {
		info.add(EnumChatFormatting.LIGHT_PURPLE + "[" + I18nUtil.resolveKey("hbmfluid.trait.ink") + "]");
	}

	@Override
	public void serializeJSON(JsonWriter writer) throws IOException {
		writer.name("color").value(color);
	}

	@Override
	public void deserializeJSON(JsonObject obj) {
		if(obj.has("color")) this.color = obj.get("color").getAsInt();
	}
}
