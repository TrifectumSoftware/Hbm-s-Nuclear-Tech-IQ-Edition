package com.hbm.blocks.generic;

import java.util.Locale;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.ISBRHUniversal;
import com.hbm.render.util.RenderBlocksNT;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;


public class BlockDetail extends BlockEnumMulti implements ISBRHUniversal {

	public BlockDetail() {
		super(Material.vine, EnumDetailType.class, true, true);
		this.setStepSound(Block.soundTypeGrass);
		this.setHardness(0.0F);
		this.setLightOpacity(0);
	}

	public static enum EnumDetailType {
		ASH,
		BLOOD,
		CORROSION,
		CRACKS,
		DUST,
		FROST,
		GRIME,
		MOLD,
		MOSS,
		MUD,
		OIL,
		RUST,
		SCRATCH,
		SLIME,
		SOOT,
		STAIN;
	}

	public static final int VARIANTS = 16;
	private static final double OFFSET = 0.05D;

	@SideOnly(Side.CLIENT)
	protected IIcon[][] iconsByType;

	@Override public int getRenderType() { return renderID; }
	@Override public boolean isOpaqueCube() { return false; }
	@Override public boolean renderAsNormalBlock() { return false; }

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {

		Enum[] enums = this.theEnum.getEnumConstants();
		this.iconsByType = new IIcon[enums.length][VARIANTS];

		for(int i = 0; i < enums.length; i++) {
			String name = enums[i].name().toLowerCase(Locale.US);
			for(int v = 0; v < VARIANTS; v++) {
				this.iconsByType[i][v] = reg.registerIcon(RefStrings.MODID + ":overlays/" + name + "_" + String.format("%02d", v));
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return this.iconsByType[Math.abs(meta) % this.iconsByType.length][0];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
		int type = Math.abs(world.getBlockMetadata(x, y, z)) % this.iconsByType.length;
		IIcon[] variants = this.iconsByType[type];
		return variants[Math.abs(hash(x, y, z, side)) % variants.length];
	}

	private static int hash(int x, int y, int z, int side) {
		long l = (long)(x * 3129871) ^ (long)y * 116129781L ^ (long)z ^ (long)side * 42317861L;
		l = l * l * 42317861L + l * 11L;
		return (int)(l >> 16 & 0xF);
	}

	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		return super.canPlaceBlockAt(world, x, y, z) && this.canBlockStay(world, x, y, z);
	}

	@Override
	public boolean canBlockStay(World world, int x, int y, int z) {
		return getAttachment(world, x, y, z) != 0;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		if(!world.isRemote && !this.canBlockStay(world, x, y, z)) {
			this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
			world.setBlockToAir(x, y, z);
		}
	}

	public static int getAttachment(IBlockAccess world, int x, int y, int z) {
		int mask = 0;
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if(isAttached(world, x, y, z, dir)) mask |= 1 << dir.ordinal();
		}
		return mask;
	}

	private static boolean isAttached(IBlockAccess world, int x, int y, int z, ForgeDirection dir) {
		int nx = x + dir.offsetX;
		int ny = y + dir.offsetY;
		int nz = z + dir.offsetZ;
		Block b = world.getBlock(nx, ny, nz);
		return b.isSideSolid(world, nx, ny, nz, dir.getOpposite()) || b.isOpaqueCube() || (b.renderAsNormalBlock() && !b.isAir(world, nx, ny, nz));
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {

		float f = 0.0625F;
		float minX = 0F, minY = 0F, minZ = 0F;
		float maxX = 1F, maxY = 1F, maxZ = 1F;

		if(isAttached(world, x, y, z, ForgeDirection.DOWN)) minY = f;
		if(isAttached(world, x, y, z, ForgeDirection.UP)) maxY = 1F - f;
		if(isAttached(world, x, y, z, ForgeDirection.NORTH)) minZ = f;
		if(isAttached(world, x, y, z, ForgeDirection.SOUTH)) maxZ = 1F - f;
		if(isAttached(world, x, y, z, ForgeDirection.WEST)) minX = f;
		if(isAttached(world, x, y, z, ForgeDirection.EAST)) maxX = 1F - f;

		this.setBlockBounds(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
		this.setBlockBoundsBasedOnState(world, x, y, z);
		return AxisAlignedBB.getBoundingBox(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return null;
	}

	@Override
	public void renderInventoryBlock(Block block, int meta, int modelId, Object renderBlocks) {

		GL11.glPushMatrix();
		RenderBlocks renderer = (RenderBlocks) renderBlocks;
		GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		renderer.setRenderBounds(0D, 0D, 0D, 1D, 0.0625D, 1D);
		RenderBlocksNT.renderStandardInventoryBlock(block, meta, renderer);
		GL11.glPopMatrix();
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, Object renderBlocks) {

		Tessellator tess = Tessellator.instance;
		tess.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
		tess.setColorOpaque_F(1F, 1F, 1F);

		int mask = getAttachment(world, x, y, z);

		if((mask & 1 << ForgeDirection.DOWN.ordinal()) != 0) {
			double Y = y + OFFSET;
			drawFace(tess, block.getIcon(world, x, y, z, 0), x, Y, z, x + 1, Y, z, x + 1, Y, z + 1, x, Y, z + 1);
		}
		if((mask & 1 << ForgeDirection.UP.ordinal()) != 0) {
			double Y = y + 1 - OFFSET;
			drawFace(tess, block.getIcon(world, x, y, z, 1), x, Y, z, x, Y, z + 1, x + 1, Y, z + 1, x + 1, Y, z);
		}
		if((mask & 1 << ForgeDirection.NORTH.ordinal()) != 0) {
			double Z = z + OFFSET;
			drawFace(tess, block.getIcon(world, x, y, z, 2), x, y, Z, x + 1, y, Z, x + 1, y + 1, Z, x, y + 1, Z);
		}
		if((mask & 1 << ForgeDirection.SOUTH.ordinal()) != 0) {
			double Z = z + 1 - OFFSET;
			drawFace(tess, block.getIcon(world, x, y, z, 3), x, y, Z, x, y + 1, Z, x + 1, y + 1, Z, x + 1, y, Z);
		}
		if((mask & 1 << ForgeDirection.WEST.ordinal()) != 0) {
			double X = x + OFFSET;
			drawFace(tess, block.getIcon(world, x, y, z, 4), X, y, z, X, y, z + 1, X, y + 1, z + 1, X, y + 1, z);
		}
		if((mask & 1 << ForgeDirection.EAST.ordinal()) != 0) {
			double X = x + 1 - OFFSET;
			drawFace(tess, block.getIcon(world, x, y, z, 5), X, y, z, X, y + 1, z, X, y + 1, z + 1, X, y, z + 1);
		}

		return true;
	}

	@SideOnly(Side.CLIENT)
	private static void drawFace(Tessellator tess, IIcon icon,
			double x1, double y1, double z1,
			double x2, double y2, double z2,
			double x3, double y3, double z3,
			double x4, double y4, double z4) {

		double u0 = icon.getMinU(), v0 = icon.getMinV();
		double u1 = icon.getMaxU(), v1 = icon.getMaxV();

		tess.addVertexWithUV(x1, y1, z1, u0, v1);
		tess.addVertexWithUV(x2, y2, z2, u0, v0);
		tess.addVertexWithUV(x3, y3, z3, u1, v0);
		tess.addVertexWithUV(x4, y4, z4, u1, v1);

		tess.addVertexWithUV(x4, y4, z4, u1, v1);
		tess.addVertexWithUV(x3, y3, z3, u1, v0);
		tess.addVertexWithUV(x2, y2, z2, u0, v0);
		tess.addVertexWithUV(x1, y1, z1, u0, v1);
	}
}
