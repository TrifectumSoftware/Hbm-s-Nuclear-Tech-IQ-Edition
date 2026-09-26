package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.tileentity.TileEntityProxyDyn;
import com.hbm.tileentity.machine.TileEntityMachineNanoprobe;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class MachineNanoprobe extends BlockDummyable {

	public MachineNanoprobe(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityMachineNanoprobe();
		if(meta >= 6) return new TileEntityProxyDyn().inventory().power().fluid();
		return null;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		return this.standardOpenBehavior(world, x, y, z, player, 0);
	}

	@Override public int[] getDimensions() { return new int[] {8, 0, 4, 4, 4, 4}; }
	@Override public int getOffset() { return 4; }

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);

		int cx = x + dir.offsetX * o;
		int cz = z + dir.offsetZ * o;

		for(int i = -4; i <= 4; i++) {
			this.makeExtra(world, cx + i, y, cz - 4);
			this.makeExtra(world, cx + i, y, cz + 4);
			this.makeExtra(world, cx - 4, y, cz + i);
			this.makeExtra(world, cx + 4, y, cz + i);
		}
	}
}
