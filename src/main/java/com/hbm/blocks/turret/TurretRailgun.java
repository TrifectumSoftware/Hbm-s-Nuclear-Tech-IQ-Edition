package com.hbm.blocks.turret;

import com.hbm.blocks.BlockDummyable;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.turret.TileEntityTurretRailgun;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TurretRailgun extends BlockDummyable {
	public TurretRailgun(Material mat) {
		super(mat);
	}

	@Override
	public int[] getDimensions() {
		return new int[] { 2, 0, 2, 2, 2, 2 };
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int i) {
		if (i >= 12) return new TileEntityTurretRailgun();
		return new TileEntityProxyCombo().inventory().power();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		return this.standardOpenBehavior(world, x, y, z, player, 0);
	}
}
