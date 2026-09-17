package com.hbm.items.weapon;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityCloudFleijaRainbow;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK3;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.entity.projectile.EntityRailgunProjectile;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.*;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.particle.helper.ExplosionCreator;
import com.hbm.util.BobMathUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.util.List;

public class ItemAmmoRailgun extends Item {

	public static RailgunSabot[] itemTypes = new RailgunSabot[ /* >>> */ 5 /* <<< */ ];
	public static final int TUNGSTEN = 0;
	public static final int DU = 1;
	public static final int NUKE = 2;
	public static final int DESH = 3;
	public static final int STARMETAL = 4;

	public ItemAmmoRailgun() {
		this.setHasSubtypes(true);
		this.setCreativeTab(MainRegistry.weaponTab);
		init();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		list.add(new ItemStack(item, 1, TUNGSTEN));
		list.add(new ItemStack(item, 1, DU));
		list.add(new ItemStack(item, 1, NUKE));
		list.add(new ItemStack(item, 1, DESH));
		list.add(new ItemStack(item, 1, STARMETAL));
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {

		String r = EnumChatFormatting.RED + "";
		String y = EnumChatFormatting.YELLOW + "";

		switch(stack.getItemDamage()) {
			case TUNGSTEN:
				list.add(y + "Tungsten core APFSDS");
				break;
			case DU:
				list.add(y + "Depleted uranium core APFSDS");
				break;
			case NUKE:
				list.add(r + "bigass nuke FSDS");
				break;
			case DESH:
				list.add(y + "First impact results in a ricochet.");
				list.add(y + "APFSDS");
				break;
			case STARMETAL:
				list.add(y + "More aerodynamic T-APDSFS");
				break;
		}
		list.add("===================================");
		list.add(y + "Initial velocity: " + BobMathUtil.getShortNumber((long) itemTypes[stack.getItemDamage()].v0) + "m/s");
		list.add(y + "Ballistic coeff.: " + itemTypes[stack.getItemDamage()].bc + "kgf/m²");
	}

	private IIcon[] icons = new IIcon[itemTypes.length];

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg) {

		this.icons = new IIcon[itemTypes.length];

		for(int i = 0; i < icons.length; i++) {
			this.icons[i] = reg.registerIcon(RefStrings.MODID + ":" + itemTypes[i].name);
		}
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconIndex(ItemStack stack) {
		return this.getIconFromDamage(stack.getItemDamage());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int meta) {
		return this.icons[meta];
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "item." + itemTypes[Math.abs(stack.getItemDamage()) % itemTypes.length].name;
	}

	public static void standardExplosion(EntityRailgunProjectile shell, MovingObjectPosition mop, float size, float rangeMod, boolean breaksBlocks) {
		Vec3 vec = Vec3.createVectorHelper(shell.motionX, shell.motionY, shell.motionZ).normalize();
		ExplosionVNT xnt = new ExplosionVNT(shell.worldObj, mop.hitVec.xCoord - vec.xCoord, mop.hitVec.yCoord - vec.yCoord, mop.hitVec.zCoord - vec.zCoord, size);
		if(breaksBlocks) {
			xnt.setBlockAllocator(new BlockAllocatorStandard(48));
			xnt.setBlockProcessor(new BlockProcessorStandard().setNoDrop().withBlockEffect(new BlockMutatorDebris(ModBlocks.block_slag, 1)));
		}
		xnt.setEntityProcessor(new EntityProcessorCross(7.5D).withRangeMod(rangeMod));
		xnt.setPlayerProcessor(new PlayerProcessorStandard());
		xnt.explode();
		shell.killAndClear();
	}

	public abstract class RailgunSabot {
		String name;
		public double bc;
		public float v0;

		public RailgunSabot(String name, double bc, float v0) {
			this.name = name;
			this.bc = bc; // ballistic coefficient [kgf*m^-2]
			this.v0 = v0; // initial velocity [m/s]
		}

		public abstract void onImpact(EntityRailgunProjectile sabot, MovingObjectPosition mop);

		public void onUpdate(EntityRailgunProjectile sabot) { }
	}

	private void init() {
		// tungsten sabot
		this.itemTypes[TUNGSTEN] = new RailgunSabot("ammo_railgun_tungsten", 0.2D, 1000F) {
			public void onImpact(EntityRailgunProjectile shell, MovingObjectPosition mop) {
				standardExplosion(shell, mop, 10F, 3F, true);
				ExplosionCreator.composeEffect(shell.worldObj, mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord, 10, 2F, 0.5F, 25F, 5, 0, 20, 0.75F, 1F, -2F, 150);
			}
		};

		// depleted uranium sabot
		this.itemTypes[DU] = new RailgunSabot("ammo_railgun_du", 0.2D, 1100F) {
			public void onImpact(EntityRailgunProjectile shell, MovingObjectPosition mop) {
				standardExplosion(shell, mop, 15F, 3F, true);
				ExplosionCreator.composeEffect(shell.worldObj, mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord, 10, 2F, 0.5F, 25F, 7, 0, 20, 0.85F, 1F, -2F, 200);
			}
		};

		// nuke sabot
		this.itemTypes[NUKE] = new RailgunSabot("ammo_railgun_nuke", 0.2D, 750F) {
			public void onImpact(EntityRailgunProjectile sabot, MovingObjectPosition mop) {
				EntityNukeExplosionMK3 ex = new EntityNukeExplosionMK3(sabot.worldObj);
				ex.posX = mop.hitVec.xCoord + 0.5;
				ex.posY = mop.hitVec.yCoord + 0.5;
				ex.posZ = mop.hitVec.zCoord + 0.5;
				ex.destructionRange = 50;
				ex.speed = BombConfig.blastSpeed;
				ex.coefficient = 1.0F;
				ex.waste = false;
				sabot.worldObj.spawnEntityInWorld(ex);

				sabot.worldObj.playSoundEffect(mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord, "random.explode", 100000.0F, 1.0F);

				EntityCloudFleijaRainbow cloud = new EntityCloudFleijaRainbow(sabot.worldObj, 50);
				cloud.posX = mop.hitVec.xCoord;
				cloud.posY = mop.hitVec.yCoord;
				cloud.posZ = mop.hitVec.zCoord;
				sabot.worldObj.spawnEntityInWorld(cloud);

				sabot.worldObj.spawnEntityInWorld(EntityNukeExplosionMK5.statFac(sabot.worldObj, BombConfig.missileRadius, mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord));
				EntityNukeTorex.statFacStandard(sabot.worldObj, mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord, 150);
				sabot.setDead();
			}
		};

		// desh sabot, ricochets at the first landing
		this.itemTypes[DESH] = new RailgunSabot("ammo_railgun_desh", 0.2D, 1000F) {
			boolean firstLanding = false;

			public void onImpact(EntityRailgunProjectile shell, MovingObjectPosition mop) {
				if (!this.firstLanding) {
					this.firstLanding = true;

					Vec3 face = null;

					switch(mop.sideHit) {
						case 0: face = Vec3.createVectorHelper(0, -1, 0); break;
						case 1: face = Vec3.createVectorHelper(0, 1, 0); break;
						case 2: face = Vec3.createVectorHelper(0, 0, 1); break;
						case 3: face = Vec3.createVectorHelper(0, 0, -1); break;
						case 4: face = Vec3.createVectorHelper(-1, 0, 0); break;
						case 5: face = Vec3.createVectorHelper(1, 0, 0); break;
					}

					if (face != null) {
						Vec3 vel = Vec3.createVectorHelper(shell.motionX, shell.motionY, shell.motionZ);
						vel.normalize();

						double angle = Math.toRadians(BobMathUtil.getCrossAngle(vel, face));
						shell.motionX *= -1D;
						shell.motionY = shell.motionY * -1D * Math.sin(2D * angle);
						shell.motionZ *= -1D;

						shell.setPosition(mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);
						shell.worldObj.playSoundAtEntity(shell, "hbm:weapon.ricochet", 40F, 1F);
					}
				} else {
					standardExplosion(shell, mop, 15F, 3F, true);
					ExplosionCreator.composeEffect(shell.worldObj, mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord, 10, 2F, 0.5F, 25F, 7, 0, 20, 0.85F, 1F, -2F, 200);
				}
			}
		};

		// starmetal sabot
		this.itemTypes[STARMETAL] = new RailgunSabot("ammo_railgun_starmetal", 0.1D, 1500F) {
			public void onImpact(EntityRailgunProjectile shell, MovingObjectPosition mop) {
				standardExplosion(shell, mop, 15F, 3F, true);
				ExplosionCreator.composeEffect(shell.worldObj, mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord, 10, 2F, 0.5F, 25F, 7, 0, 20, 0.85F, 1F, -2F, 200);
			}
		};
	}
}
