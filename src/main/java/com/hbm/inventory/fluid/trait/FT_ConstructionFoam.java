package com.hbm.inventory.fluid.trait;

import com.hbm.blocks.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class FT_ConstructionFoam extends FluidTrait {

	public final int foamMeta;

	public FT_ConstructionFoam() {
		this(0);
	}

	public FT_ConstructionFoam(int foamMeta) {
		this.foamMeta = foamMeta;
	}

	public void applyFoam(World world, MovingObjectPosition mop) {

		int side = mop.sideHit;
		int x = mop.blockX;
		int y = mop.blockY;
		int z = mop.blockZ;

		int dx = side == 5 ? 1 : side == 4 ? -1 : 0;
		int dy = side == 1 ? 1 : side == 0 ? -1 : 0;
		int dz = side == 3 ? 1 : side == 2 ? -1 : 0;

		int bx = x + dx;
		int by = y + dy;
		int bz = z + dz;

		int ox = mop.hitVec.xCoord - x < 0.5 ? -1 : 0;
		int oy = mop.hitVec.yCoord - y < 0.5 ? -1 : 0;
		int oz = mop.hitVec.zCoord - z < 0.5 ? -1 : 0;

		if(dx != 0) {
			place(world, bx, by + oy, bz + oz);
			place(world, bx, by + oy + 1, bz + oz);
			place(world, bx, by + oy, bz + oz + 1);
			place(world, bx, by + oy + 1, bz + oz + 1);
		} else if(dy != 0) {
			place(world, bx + ox, by, bz + oz);
			place(world, bx + ox + 1, by, bz + oz);
			place(world, bx + ox, by, bz + oz + 1);
			place(world, bx + ox + 1, by, bz + oz + 1);
		} else {
			place(world, bx + ox, by + oy, bz);
			place(world, bx + ox + 1, by + oy, bz);
			place(world, bx + ox, by + oy + 1, bz);
			place(world, bx + ox + 1, by + oy + 1, bz);
		}
	}

	private void place(World world, int x, int y, int z) {
		Block block = world.getBlock(x, y, z);
		if(block.isAir(world, x, y, z) || block.isReplaceable(world, x, y, z)) {
			world.setBlock(x, y, z, ModBlocks.construction_foam, this.foamMeta, 3);
		}
	}
}

