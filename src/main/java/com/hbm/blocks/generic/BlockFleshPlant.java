package com.hbm.blocks.generic;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.ModSoundType;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class BlockFleshPlant extends BlockEnumMulti {

	public BlockFleshPlant() {
		super(Material.plants, EnumFleshPlantType.class, true, true);
		this.setStepSound(ModSoundType.mod("plantsquish", 1.0F, 1.0F));
	}

	public static enum EnumFleshPlantType {
		TENDON,
		PEEPING,
		BULB_BIG,
		BULB_SMALL,
		FEVER,
		FEVER_SMALL,
		GLYPHID_VENT,
		HAIR_SHORT,
		HAIR_TALL,
		LUNG,
		SPINE_FERN;
	}

	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		return super.canPlaceBlockAt(world, x, y, z) && this.canBlockStay(world, x, y, z);
	}

	protected boolean canPlaceBlockOn(Block block) {
		return block == Blocks.netherrack || block == Blocks.soul_sand || block == ModBlocks.glyphid_base || block == ModBlocks.nether_glyphid;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		super.onNeighborBlockChange(world, x, y, z, block);
		this.checkAndDropBlock(world, x, y, z);
	}

	protected void checkAndDropBlock(World world, int x, int y, int z) {
		if(!this.canBlockStay(world, x, y, z)) {
			this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
			world.setBlock(x, y, z, Blocks.air, 0, 2);
		}
	}

	@Override
	public boolean canBlockStay(World world, int x, int y, int z) {
		return canPlaceBlockOn(world.getBlock(x, y - 1, z));
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return null;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public int getRenderType() {
		return 1;
	}

	@Override
	public int damageDropped(int meta) {
		return meta;
	}

	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		return null;
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> drops = super.getDrops(world, x, y, z, metadata, fortune);
		drops.add(new ItemStack(ModItems.heartfruit_seeds, 1 + world.rand.nextInt(2)));
		return drops;
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int meta) {
		if(world.isRemote) return;

		if(player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() instanceof ItemShears) {
			world.spawnEntityInWorld(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, new ItemStack(this, 1, damageDropped(meta))));
		} else {
			super.harvestBlock(world, player, x, y, z, meta);
		}
	}

	@Override
	public String getTextureMultiName(Enum num) {
		switch((EnumFleshPlantType) num) {
		case TENDON:  return RefStrings.MODID + ":tendonweed";
		case PEEPING: return RefStrings.MODID + ":tendonweed_eye";
		case BULB_BIG: return RefStrings.MODID + ":bulbplantbig";
		case BULB_SMALL: return RefStrings.MODID + ":bulbplantsmall";
		case FEVER: return RefStrings.MODID + ":fevershroom";
		case FEVER_SMALL: return RefStrings.MODID + ":fevershroomsmall";
		case GLYPHID_VENT: return RefStrings.MODID + ":glyphidvent";
		case HAIR_SHORT: return RefStrings.MODID + ":hairplantshort";
		case HAIR_TALL: return RefStrings.MODID + ":hairplanttall";
		case LUNG: return RefStrings.MODID + ":lungplant";
		case SPINE_FERN: return RefStrings.MODID + ":spinefern";
		}
		return super.getTextureMultiName(num);
	}

	@Override
	public String getUnlocalizedMultiName(Enum num) {
		return super.getUnlocalizedName() + "." + num.name().toLowerCase(Locale.US);
	}
}
