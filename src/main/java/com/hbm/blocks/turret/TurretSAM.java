package com.hbm.blocks.turret;

import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.turret.TileEntityTurretSAM;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class TurretSAM extends TurretBaseNT {

	public TurretSAM(Material mat) {
		super(mat);

		this.bounding.add(AxisAlignedBB.getBoundingBox(-1.5D, 0.0D, -1.5D, 1.5D, 0.5D, 1.5D));
		this.bounding.add(AxisAlignedBB.getBoundingBox(-0.5D, 0.0D, -0.5D, 0.5D, 2.0D, 0.5D));
	}

	@Override
	public int[] getDimensions() {
		return new int[] { 0, 0, 1, 1, 1, 1 };
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.999F, 1.0F);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {

		if(meta >= 12)
			return new TileEntityTurretSAM();
		return new TileEntityProxyCombo().inventory().power();
	}
}
