package com.hbm.blocks.generic;

import com.hbm.dim.SolarSystem;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.render.block.RenderBlockMultipass;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class BlockConcreteOil extends BlockOreFluid {
	@SideOnly(Side.CLIENT)
	private IIcon bedrockIcon;

	public BlockConcreteOil(Material mat, Block empty, ReserveType type) {
		super(mat, empty, type);
	}

	@Override
	public void registerBlockIcons(IIconRegister reg) {
		super.registerBlockIcons(reg);
		this.bedrockIcon = reg.registerIcon("bedrock");
	}

	@Override
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
		return RenderBlockMultipass.currentPass == 0 ? this.bedrockIcon : this.blockIcon;
	}

	@Override
	public IIcon getIcon(int side, int meta) {
		return RenderBlockMultipass.currentPass == 0 ? this.bedrockIcon : this.blockIcon;
	}

	@Override
	public int getSubCount() {
		return 1;
	}

	@Override
	public FluidType getPrimaryFluid(int meta) {
		return Fluids.NATCRETE;
	}
	@Override
	public FluidType getSecondaryFluid(int meta) {
		return Fluids.NONE;
	}

	@Override
	public int getPrimaryFluidAmount(int meta) {
		return 100;
	}
	@Override
	public int getSecondaryFluidAmount(int meta) {
		return 0;
	}

	@Override
	public int getBlockFluidAmount(int meta) {
		return 1000;
	}
}
