package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.blocks.BlockEnums.EnumRawOreBlockType;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.util.EnumUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockRawOreBlock extends BlockEnumMulti {
// bro uses two block IDs
	public final int offset;
	public final int count;

	public BlockRawOreBlock(int offset, int count) {
		super(Material.rock, EnumRawOreBlockType.class, true, true);
		this.offset = offset;
		this.count = count;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return icons[Math.min(meta + offset, EnumRawOreBlockType.values().length - 1)];
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		Enum num = EnumUtil.grabEnumSafely(this.theEnum, stack.getItemDamage() + offset);
		return getUnlocalizedMultiName(num);
	}

	@Override
	public int getSubCount() {
		return count;
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);

		if(radForMeta(world, x, y, z) > 0)
			world.scheduleBlockUpdate(x, y, z, this, this.tickRate(world));
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {

		float rad = radForMeta(world, x, y, z);

		if(rad > 0) {
			ChunkRadiationManager.proxy.incrementRad(world, x, y, z, rad);
			world.scheduleBlockUpdate(x, y, z, this, this.tickRate(world));
		}
	}

	@Override
	public int tickRate(World world) {
		return 20;
	}

	private float radForMeta(World world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		return HazardSystem.getHazardLevelFromStack(new ItemStack(this, 1, meta + offset), HazardRegistry.RADIATION) * 0.1F;
	}
}