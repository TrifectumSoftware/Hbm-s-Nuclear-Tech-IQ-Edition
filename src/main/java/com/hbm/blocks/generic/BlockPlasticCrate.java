package com.hbm.blocks.generic;

import com.hbm.lib.RefStrings;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

public class BlockPlasticCrate extends Block {

	@SideOnly(Side.CLIENT) private IIcon iconTop;
	@SideOnly(Side.CLIENT) private IIcon iconSide;
	@SideOnly(Side.CLIENT) private IIcon iconBottom;

	public BlockPlasticCrate(Material mat) {
		super(mat);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":plastic_crate_top_full");
		this.iconSide = iconRegister.registerIcon(RefStrings.MODID + ":plastic_crate_side_full");
		this.iconBottom = iconRegister.registerIcon(RefStrings.MODID + ":plastic_crate_bottom_full");
		this.blockIcon = iconSide;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		switch(side) {
		case 0: return iconBottom;
		case 1: return iconTop;
		default: return iconSide;
		}
	}
}
