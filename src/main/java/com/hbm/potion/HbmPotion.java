package com.hbm.potion;

import java.lang.reflect.Field;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.PotionConfig;
import com.hbm.config.ServerConfig;
import com.hbm.entity.effect.EntityGreenLightning;
import com.hbm.entity.effect.EntityRedLightning;
import com.hbm.entity.mob.EntityTaintCrab;
import com.hbm.entity.mob.EntityCreeperTainted;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.ResourceManager;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

public class HbmPotion extends Potion {

	public static HbmPotion taint;
	public static HbmPotion radiation;
	public static HbmPotion bang;
	public static HbmPotion mutation;
	public static HbmPotion radx;
	public static HbmPotion lead;
	public static HbmPotion radaway;
	//public static HbmPotion telekinesis;
	public static HbmPotion phosphorus;
	public static HbmPotion stability;
	public static HbmPotion potionsickness;
	public static HbmPotion death;

	public static HbmPotion run;

	public static HbmPotion nitan;
	public static HbmPotion flashbang;

	public static HbmPotion slippery; //t
	//these are for the new consumable fx
	public static HbmPotion stimulated;
	public static HbmPotion medx;
	public static HbmPotion roidRage;
	public static HbmPotion wobble;

	//symptoms
	public static HbmPotion symptomFever;
	public static HbmPotion symptomCough;
	public static HbmPotion symptomSneeze;
	public static HbmPotion symptomSchizophrenia;
	public static HbmPotion symptomParalysis;
	public static HbmPotion symptomHemorrhage;
	public static HbmPotion symptomSeptic;
	public static HbmPotion symptomComa;
	public static HbmPotion symptomRash;
	public static HbmPotion symptomVomit;
	public static HbmPotion symptomSeizure;
	public static HbmPotion symptomAortic;
	public static HbmPotion symptomNecrosis;
	public static HbmPotion symptomCardiac;

	//drugs
	public static HbmPotion turkishRage;
	public static HbmPotion ganja;

	public HbmPotion(int id, boolean isBad, int color) {
		super(id, isBad, color);
	}

	public static void init() {
		taint = registerPotion(PotionConfig.taintID, true, 0x800080, "potion.hbm_taint", 0, 0);
		radiation = registerPotion(PotionConfig.radiationID, true, 0x84C128, "potion.hbm_radiation", 1, 0);
		bang = registerPotion(PotionConfig.bangID, true, 0x111111, "potion.hbm_bang", 3, 0);
		mutation = registerPotion(PotionConfig.mutationID, false, 0x800080, "potion.hbm_mutation", 2, 0);
		radx = registerPotion(PotionConfig.radxID, false, 0xBB4B00, "potion.hbm_radx", 5, 0);
		lead = registerPotion(PotionConfig.leadID, true, 0x767682, "potion.hbm_lead", 6, 0);
		radaway = registerPotion(PotionConfig.radawayID, false, 0xBB4B00, "potion.hbm_radaway", 7, 0);
		//telekinesis = registerPotion(PotionConfig.telekinesisID, true, 0x00F3FF, "potion.hbm_telekinesis", 0, 1);
		phosphorus = registerPotion(PotionConfig.phosphorusID, true, 0xFFFF00, "potion.hbm_phosphorus", 1, 1);
		stability = registerPotion(PotionConfig.stabilityID, false, 0xD0D0D0, "potion.hbm_stability", 2, 1);
		potionsickness = registerPotion(PotionConfig.potionsicknessID, false, 0xff8080, "potion.hbm_potionsickness", 3, 1);
		death = registerPotion(PotionConfig.deathID, false, 1118481, "potion.hbm_death", 4, 1);
		run = registerPotion(PotionConfig.runID, true, 1118481, "potion.hbm_run", 14, 0);
		nitan = registerPotion(PotionConfig.nitanID, false, 8388736, "potion.hbm_nitan", 3, 1);
		flashbang = registerPotion(PotionConfig.flashbangID, false, 0xD0D0D0, "potion.hbm_flashbang", 15, 1);
		slippery = registerPotion(PotionConfig.slipperyID, false, 0xD0D0D0, "potion.hbm_slippery", 15, 0);
		stimulated = registerPotion(PotionConfig.stimulatedID, false, 0x44FF44, "potion.hbm_stimulated", 2, 2);
		roidRage = registerPotion(PotionConfig.roidRageID, false, 0xFF4444, "potion.hbm_roid_rage", 0, 2);
		medx = registerPotion(PotionConfig.medxID, false, 0x8888FF, "potion.hbm_medx", 1, 2);
		wobble = registerPotion(PotionConfig.wobbleID, false, 0xFFD800, "potion.hbm_wobble", 3, 2);

	/// SYMPTOMS
		symptomRash = registerPotion(PotionConfig.symptomRashID, true, 0xFF8899, "potion.hbm_symptom_rash", 0, 0);
		symptomCough = registerPotion(PotionConfig.symptomCoughID, true, 0xCCCCBB, "potion.hbm_symptom_cough", 1, 0);
		symptomSneeze = registerPotion(PotionConfig.symptomSneezeID, true, 0xFFFFFF, "potion.hbm_symptom_sneeze", 13, 0);
		symptomFever = registerPotion(PotionConfig.symptomFeverID, true, 0xFF4422, "potion.hbm_symptom_fever", 2, 0);
		symptomVomit = registerPotion(PotionConfig.symptomVomitID, true, 0xAA8844, "potion.hbm_symptom_vomit", 3, 0);
		symptomSchizophrenia = registerPotion(PotionConfig.symptomSchizophreniaID, true, 0xAA44FF, "potion.hbm_symptom_schizophrenia", 4, 0);
		symptomHemorrhage = registerPotion(PotionConfig.symptomHemorrhageID, true, 0xCC2222, "potion.hbm_symptom_hemorrhage", 5, 0);
		symptomParalysis = registerPotion(PotionConfig.symptomParalysisID, true, 0x888888, "potion.hbm_symptom_paralysis", 6, 0);
		symptomSeizure = registerPotion(PotionConfig.symptomSeizureID, true, 0xFF88FF, "potion.hbm_symptom_seizure", 7, 0);
		symptomSeptic = registerPotion(PotionConfig.symptomSepticID, true, 0x445522, "potion.hbm_symptom_septic", 8, 0);
		symptomComa = registerPotion(PotionConfig.symptomComaID, true, 0x222244, "potion.hbm_symptom_coma", 9, 0);
		symptomAortic = registerPotion(PotionConfig.symptomAorticID, true, 0xFF0000, "potion.hbm_symptom_aortic", 10, 0);
		symptomNecrosis = registerPotion(PotionConfig.symptomNecrosisID, true, 0x222222, "potion.hbm_symptom_necrosis", 11, 0);
		symptomCardiac = registerPotion(PotionConfig.symptomCardiacID, true, 0x4444FF, "potion.hbm_symptom_cardiac", 12, 0);

		//sillies
		turkishRage = registerPotion(PotionConfig.turkishRageID, false, 0xFF0000, "potion.hbm_turkish_rage", 4, 2);
		ganja = registerPotion(PotionConfig.ganjaID, false, 0x3A5F0B, "potion.hbm_high", 5, 2);

	}

	public static HbmPotion registerPotion(int id, boolean isBad, int color, String name, int x, int y) {

		if (id >= Potion.potionTypes.length) {

			Potion[] newArray = new Potion[Math.max(256, id)];
			System.arraycopy(Potion.potionTypes, 0, newArray, 0, Potion.potionTypes.length);

			Field field = ReflectionHelper.findField(Potion.class, new String[] { "field_76425_a", "potionTypes" });
			field.setAccessible(true);

			try {

				Field modfield = Field.class.getDeclaredField("modifiers");
				modfield.setAccessible(true);
				modfield.setInt(field, field.getModifiers() & 0xFFFFFFEF);
				field.set(null, newArray);

			} catch (Exception e) {

			}
		}

		HbmPotion effect = new HbmPotion(id, isBad, color);
		effect.setPotionName(name);
		effect.setIconIndex(x, y);

		return effect;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getStatusIconIndex() {
		ResourceLocation loc = new ResourceLocation("hbm", "textures/gui/potions.png");
		Minecraft.getMinecraft().renderEngine.bindTexture(loc);
		if(isSymptom()) return 100;
		return super.getStatusIconIndex();
	}

	@SideOnly(Side.CLIENT)
	public boolean isSymptom() {
		return this == symptomFever || this == symptomCough || this == symptomSneeze || this == symptomSchizophrenia
				|| this == symptomParalysis || this == symptomHemorrhage || this == symptomSeptic || this == symptomComa
				|| this == symptomRash || this == symptomVomit || this == symptomSeizure
				|| this == symptomAortic || this == symptomNecrosis || this == symptomCardiac;
	}

	@SideOnly(Side.CLIENT)
	public int[] getEffectCell() {
		if(this == symptomCardiac) return new int[] {0, 0};
		if(this == symptomComa) return new int[] {18, 0};
		if(this == symptomCough) return new int[] {36, 0};
		if(this == symptomFever) return new int[] {54, 0};
		if(this == symptomNecrosis) return new int[] {0, 18};
		if(this == symptomParalysis) return new int[] {18, 18};
		if(this == symptomRash) return new int[] {36, 18};
		if(this == symptomSchizophrenia) return new int[] {54, 18};
		if(this == symptomSeizure) return new int[] {0, 36};
		if(this == symptomSeptic) return new int[] {18, 36};
		if(this == symptomSneeze) return new int[] {36, 36};
		if(this == symptomVomit) return new int[] {54, 36};
		if(this == symptomHemorrhage) return new int[] {0, 54};
		if(this == symptomAortic) return new int[] {18, 54};
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
		if(isSymptom()) {
			int[] cell = getEffectCell();
			if(cell != null) {
				mc.renderEngine.bindTexture(ResourceManager.effect_icons);
				Gui.func_146110_a(x + 6, y + 7, (float)cell[0], (float)cell[1], 18, 18, 72.0F, 72.0F);
			}
			return;
		}
		super.renderInventoryEffect(x, y, effect, mc);
	}

	public void performEffect(EntityLivingBase entity, int level) {

		if(entity.worldObj.isRemote) return;

		if(this == taint) {

			if(!(entity instanceof EntityCreeperTainted) && !(entity instanceof EntityTaintCrab) && entity.worldObj.rand.nextInt(40) == 0)
				entity.attackEntityFrom(ModDamageSource.taint, (level + 1));

			if(ServerConfig.TAINT_TRAILS.get() && !entity.worldObj.isRemote) {

				int x = (int) Math.floor(entity.posX);
				int y = (int) Math.floor(entity.posY);
				int z = (int) Math.floor(entity.posZ);

				Block b = entity.worldObj.getBlock(x, y - 1, z);
				if(y > 1 && b.isNormalCube() && !b.isAir(entity.worldObj, x, y - 1, z)) {
					entity.worldObj.setBlock(x, y - 1, z, ModBlocks.taint, 14, 2);
				}
			}
		}
		if(this == radiation) {
			ContaminationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, (float)(level + 1F) * 0.05F);
		}
		if(this == radaway) {
			HbmLivingProps.incrementRadiation(entity, -(level + 1));

		}
		if(this == slippery) {
			entity.motionY += 0.026D;

		}
		if(this == bang) {

			entity.attackEntityFrom(ModDamageSource.bang, 1000);
			entity.setHealth(0.0F);

			if (!(entity instanceof EntityPlayer))
				entity.setDead();

			entity.worldObj.playSoundEffect(entity.posX, entity.posY, entity.posZ, "hbm:weapon.laserBang", 100.0F, 1.0F);
			ExplosionLarge.spawnParticles(entity.worldObj, entity.posX, entity.posY, entity.posZ, 10);

			if(entity instanceof EntityCow) {
				EntityCow cow = (EntityCow) entity;
				int toDrop = cow.isChild() ? 10 : 3;
				cow.entityDropItem(new ItemStack(ModItems.cheese, toDrop), 1.0F);
			}
		}
		if(this == run) {

			entity.attackEntityFrom(ModDamageSource.run, 1000);
			entity.setHealth(0.0F);
			//World world = Minecraft.getMinecraft().theWorld;

			new ExplosionVNT(entity.worldObj, entity.posX, entity.posY, entity.posZ, 12).makeAmat().explode();
			entity.worldObj.playSoundEffect(entity.posX, entity.posY, entity.posZ, "hbm:weapon.mukeExplosion", 100.0F, 1.0F);

			if (!(entity instanceof EntityPlayer))
				entity.setDead();
		}
		if(this == lead) {
			entity.attackEntityFrom(ModDamageSource.lead, (level + 1));
		}
		if(this == phosphorus) {
			entity.setFire(1);
		}
        if(this == nitan && !entity.worldObj.isRemote) {
        	if(entity instanceof EntityPlayer) {
				HbmPlayerProps props = HbmPlayerProps.getData((EntityPlayer) entity);

				if (props.nitanCount == 3) {
					entity.attackEntityFrom(ModDamageSource.nitan, 1000);
				}
			}

		}
		if(this == flashbang && !entity.worldObj.isRemote){
			if(entity instanceof EntityZombie || entity instanceof EntitySkeleton){
				entity.setFire(20);
				}
			entity.addPotionEffect(new PotionEffect(moveSlowdown.id,5,10));
		}
		if(this == stimulated) {
			entity.heal((level + 1) * 2);
		}

		if(this == turkishRage && entity.ticksExisted % 200 == 0) {
			entity.worldObj.spawnEntityInWorld(new EntityRedLightning(entity.worldObj, entity.posX, entity.posY, entity.posZ));
			entity.worldObj.playSoundEffect(entity.posX, entity.posY, entity.posZ, "ambient.weather.thunder", 100.0F, 0.8F + entity.worldObj.rand.nextFloat() * 0.2F);
		}
		if(this == ganja && entity.ticksExisted % 200 == 0) {
			entity.worldObj.spawnEntityInWorld(new EntityGreenLightning(entity.worldObj, entity.posX, entity.posY, entity.posZ));
			entity.worldObj.playSoundEffect(entity.posX, entity.posY, entity.posZ, "ambient.weather.thunder", 100.0F, 0.8F + entity.worldObj.rand.nextFloat() * 0.2F);
		}

		//symptoms are markers only - their downsides apply in HbmBloodstreamProps.tick (safe, post-potion-iteration).
		//NEVER addPotionEffect/attackEntityFrom-lethal here: performEffect runs inside vanilla's potion map iteration.
		if(this == symptomFever || this == symptomCough || this == symptomSneeze || this == symptomSchizophrenia
				|| this == symptomParalysis || this == symptomHemorrhage || this == symptomSeptic || this == symptomComa
				|| this == symptomRash || this == symptomVomit || this == symptomSeizure
				|| this == symptomAortic || this == symptomNecrosis || this == symptomCardiac) {
			// handled in HbmBloodstreamProps.tick
		}
	}

	public boolean isReady(int par1, int par2) {

		if(this == taint) {
			return par1 % 2 == 0;
		}


		if(this == radiation || this == radaway || this == phosphorus || this == nitan || this == slippery) {

			return true;
		}

		if(this == bang) {
			return par1 <= 10;
		}
		if(this == run) {
			return par1 <= 10;
		}

		if(this == lead) {
			int k = 60;
			return k > 0 ? par1 % k == 0 : true;
		}

		if(this == stimulated || this == roidRage || this == medx) {
			return par1 % 20 == 0;
		}

		if(this == turkishRage || this == ganja) {
			return par1 % 20 == 0;
		}

		if(this == symptomFever || this == symptomCough || this == symptomSneeze || this == symptomSchizophrenia
				|| this == symptomParalysis || this == symptomHemorrhage || this == symptomSeptic || this == symptomComa
				|| this == symptomRash || this == symptomVomit || this == symptomSeizure
				|| this == symptomAortic || this == symptomNecrosis || this == symptomCardiac) {
			return par1 % 10 == 0;
		}

		return false;
	}

	public static boolean getIsBadEffect(Potion potion) {

		try {
			Field isBadEffect = ReflectionHelper.findField(Potion.class, "isBadEffect", "field_76418_K"); //TODO: use an AT for this
			boolean ret = isBadEffect.getBoolean(potion);
			return ret;

		} catch (Exception x) {
			return false;
		}
	}
}
