package com.hbm.itempool;

import static com.hbm.lib.HbmChestContents.filledSyringe;
import static com.hbm.lib.HbmChestContents.weighted;

import com.hbm.blocks.BlockEnums.EnumCrystalBlockType;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemHumanPart;
import com.hbm.items.special.ItemHumanPart.EnumHumanPart;
import com.hbm.items.weapon.grenade.ItemGrenadeExtra.EnumGrenadeExtra;
import com.hbm.items.weapon.grenade.ItemGrenadeFilling.EnumGrenadeFilling;
import com.hbm.items.weapon.grenade.ItemGrenadeFuze.EnumGrenadeFuze;
import com.hbm.items.weapon.grenade.ItemGrenadeShell.EnumGrenadeShell;
import com.hbm.items.weapon.grenade.ItemGrenadeUniversal;
import com.hbm.items.weapon.sedna.factory.GunFactory.EnumAmmo;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;

public class ItemPoolsPile {

	public static final String POOL_PILE_HIVE = "POOL_PILE_HIVE";
	public static final String POOL_PILE_NETHER_HIVE = "POOL_PILE_NETHER_HIVE";
	public static final String POOL_PILE_BONES = "POOL_PILE_BONES";
	public static final String POOL_PILE_CAPS = "POOL_PILE_CAPS";
	public static final String POOL_PILE_MED_SYRINGE = "POOL_PILE_MED_SYRINGE";
	public static final String POOL_PILE_MED_PILLS = "POOL_PILE_MED_PILLS";
	public static final String POOL_PILE_MAKESHIFT_GUN = "POOL_PILE_MAKESHIFT_GUN";
	public static final String POOL_PILE_MAKESHIFT_WRENCH = "POOL_PILE_MAKESHIFT_WRENCH";
	public static final String POOL_PILE_MAKESHIFT_PLATES = "POOL_PILE_MAKESHIFT_PLATES";
	public static final String POOL_PILE_MAKESHIFT_WIRE = "POOL_PILE_MAKESHIFT_WIRE";
	public static final String POOL_PILE_NUKE_STORAGE = "POOL_PILE_NUKE_STORAGE";
	public static final String POOL_PILE_OF_GARBAGE = "POOL_PILE_OF_GARBAGE";
	public static final String POOL_PILE_MECHANICAL = "POOL_PILE_MECHANICAL";
	public static final String POOL_PILE_GEAR = "POOL_PILE_GEAR";
	public static final String POOL_PILE_SUPPLIES = "POOL_PILE_SUPPLIES";


	public static void init() {

		//items found in glyphid hives
		new ItemPool(POOL_PILE_HIVE) {{
			this.pool = new WeightedRandomChestContent[] {
					//Materials
					weighted(Items.iron_ingot, 0, 1, 3, 10),
					weighted(ModItems.ingot_steel, 0, 1, 2, 10),
					weighted(ModItems.ingot_aluminium, 0, 1, 2, 10),
					weighted(ModItems.scrap, 0, 3, 6, 10),
					//Armor
					weighted(ModItems.gas_mask_m65, 0, 1, 1, 10),
					weighted(ModItems.steel_plate, 0, 1, 1, 5),
					weighted(ModItems.steel_legs, 0, 1, 1, 5),
					//Gear
					weighted(ModItems.steel_pickaxe, 0, 1, 1, 5),
					weighted(ModItems.steel_shovel, 0, 1, 1, 5),
					//Weapons
					weighted(ModItems.gun_maresleg, 0, 1, 1, 5),
					weighted(ModItems.gun_light_revolver, 0, 1, 1, 1),
					weighted(ItemGrenadeUniversal.make(EnumGrenadeShell.FRAG, EnumGrenadeFilling.HE, EnumGrenadeFuze.S3, EnumGrenadeExtra.FRAG_SLEEVE), 1, 2, 5),
					weighted(ItemGrenadeUniversal.make(EnumGrenadeShell.STICK, EnumGrenadeFilling.DEMO, EnumGrenadeFuze.IMPACT), 1, 2, 3),
					weighted(ModItems.ammo_standard, EnumAmmo.G12.ordinal(), 4, 4, 10),
					weighted(ModItems.ammo_standard, EnumAmmo.M357_SP.ordinal(), 6, 12, 10),
					weighted(ModItems.ammo_standard, EnumAmmo.G40_HE.ordinal(), 1, 1, 2),
					//Consumables
					weighted(ModItems.bottle_nuka, 0, 1, 2, 20),
					weighted(ModItems.bottle_quantum, 0, 1, 2, 1),
					weighted(ModItems.definitelyfood, 0, 5, 12, 20),
					weighted(ModItems.egg_glyphid, 0, 1, 3, 30),
					weighted(filledSyringe(ModItems.combat_syringe, Fluids.STIMPAK), 1, 1, 2),
					weighted(filledSyringe(ModItems.combat_syringe, Fluids.MEDX), 1, 1, 1),
					weighted(filledSyringe(ModItems.combat_syringe, Fluids.PSYCHO), 1, 1, 1),
					weighted(filledSyringe(ModItems.combat_syringe, Fluids.SUPER_STIMPAK), 1, 1, 1),
					weighted(filledSyringe(ModItems.syringe, Fluids.RADAWAY), 1, 1, 10),
					weighted(Items.experience_bottle, 0, 1, 3, 5),
					weighted(ItemHumanPart.make(EnumHumanPart.HEAD, 8, 45), 1, 1, 1),
					weighted(ItemHumanPart.make(EnumHumanPart.TORSO, 8, 45), 1, 1, 1),
					weighted(ItemHumanPart.make(EnumHumanPart.ARMS, 8, 45), 1, 1, 1),
					weighted(ItemHumanPart.make(EnumHumanPart.LEGS, 8, 45), 1, 1, 1),
					weighted(ItemHumanPart.make(EnumHumanPart.HEART, 8, 45), 1, 1, 1),
			};
		}};

		//items found in nether glyphid nests
		new ItemPool(POOL_PILE_NETHER_HIVE) {{
			this.pool = new WeightedRandomChestContent[] {
					//Materials
					weighted(Items.gold_ingot, 0, 2, 6, 15),
					weighted(Items.quartz, 0, 4, 12, 15),
					weighted(Items.glowstone_dust, 0, 4, 10, 15),
					weighted(Items.nether_wart, 0, 2, 8, 10),
					weighted(Items.diamond, 0, 1, 2, 2),
					weighted(ModItems.symbol_guilt, 0, 1, 1, 2),
					weighted(ModItems.scrap, 0, 4, 10, 10),
					weighted(Items.blaze_powder, 0, 2, 6, 12),
					weighted(Items.ghast_tear, 0, 1, 2, 4),
					weighted(Items.skull, 1, 1, 1, 2),
					//Balefire
					weighted(ModItems.cell_balefire, 0, 1, 1, 2),
					weighted(ModItems.balefire_scrambled, 0, 1, 1, 2),
					//Glyphid
					weighted(ModItems.glyphid_meat, 0, 2, 5, 12),
					weighted(new ItemStack(ModItems.glyphid_gland, 1, Fluids.BLOATMUSK.getID()), 1, 2, 5),
					weighted(new ItemStack(ModItems.glyphid_gland, 1, Fluids.MUSKY_PHEROMONE.getID()), 1, 1, 3),
					//Weapons
					weighted(ModItems.gun_light_revolver, 0, 1, 1, 2),
					weighted(ModItems.gun_carbine, 0, 1, 1, 3),
					weighted(ModItems.gun_am180, 0, 1, 1, 2),
					weighted(ModItems.gun_spas12, 0, 1, 1, 2),
					weighted(ModItems.gun_minigun, 0, 1, 1, 1),
					weighted(ModItems.gun_fatman, 0, 1, 1, 1),
					weighted(ModItems.ammo_standard, EnumAmmo.G12.ordinal(), 4, 8, 10),
					weighted(ModItems.ammo_standard, EnumAmmo.M357_SP.ordinal(), 6, 12, 10),
					weighted(ModItems.ammo_standard, EnumAmmo.R762_FMJ.ordinal(), 4, 8, 10),
					weighted(ModItems.ammo_standard, EnumAmmo.R762_DU.ordinal(), 2, 4, 4),
					//Cobalt & Starmetal
					weighted(new ItemStack(ModBlocks.block_crystal_2, 1, EnumCrystalBlockType.COBALT.ordinal() - 16), 1, 2, 3),
					weighted(new ItemStack(ModBlocks.block_crystal_2, 1, EnumCrystalBlockType.STARMETAL.ordinal() - 16), 1, 2, 2),
					weighted(ModItems.ingot_starmetal, 0, 1, 2, 3),
					//Consumables
					weighted(ModItems.bottle_nuka, 0, 1, 3, 20),
					weighted(ModItems.definitelyfood, 0, 6, 16, 20),
					weighted(filledSyringe(ModItems.combat_syringe, Fluids.PSYCHO), 1, 2, 4),
					weighted(filledSyringe(ModItems.combat_syringe, Fluids.STIMPAK), 1, 2, 4),

					//Rare
					weighted(ModItems.ammo_standard, EnumAmmo.NUKE_BALEFIRE.ordinal(), 1, 1, 3),
					weighted(ItemHumanPart.make(EnumHumanPart.HEAD, 45, 95), 1, 1, 1),
					weighted(ItemHumanPart.make(EnumHumanPart.TORSO, 45, 95), 1, 1, 1),
					weighted(ItemHumanPart.make(EnumHumanPart.ARMS, 45, 95), 1, 1, 1),
					weighted(ItemHumanPart.make(EnumHumanPart.LEGS, 45, 95), 1, 1, 1),
					weighted(ItemHumanPart.make(EnumHumanPart.HEART, 45, 95), 1, 1, 1),
			};
		}};

		//items found in glyphid bone piles
		new ItemPool(POOL_PILE_BONES) {{
			this.pool = new WeightedRandomChestContent[] {
					weighted(Items.bone, 0, 1, 1, 10),
					weighted(Items.rotten_flesh, 0, 1, 1, 5),
					weighted(ModItems.biomass, 0, 1, 1, 2)
			};
		}};

		//bottlecap stashess
		new ItemPool(POOL_PILE_CAPS) {{
			this.pool = new WeightedRandomChestContent[] {
					weighted(ModItems.cap_nuka, 0, 4, 4, 20),
					weighted(ModItems.cap_quantum, 0, 4, 4, 3),
					weighted(ModItems.cap_sparkle, 0, 4, 4, 1),
			};
		}};

		//medicine stashes
		new ItemPool(POOL_PILE_MED_SYRINGE) {{
			this.pool = new WeightedRandomChestContent[] {
					weighted(filledSyringe(ModItems.combat_syringe, Fluids.STIMPAK), 1, 1, 4),
					weighted(filledSyringe(ModItems.combat_syringe, Fluids.MEDX), 1, 1, 3),
					weighted(filledSyringe(ModItems.combat_syringe, Fluids.PSYCHO), 1, 1, 2),
					weighted(filledSyringe(ModItems.combat_syringe, Fluids.SUPER_STIMPAK), 1, 1, 1),
					weighted(filledSyringe(ModItems.syringe, Fluids.STIMPAK), 1, 1, 2),
					weighted(filledSyringe(ModItems.syringe, Fluids.MEDX), 1, 1, 1),
					weighted(filledSyringe(ModItems.syringe, Fluids.PSYCHO), 1, 1, 1),
					weighted(filledSyringe(ModItems.syringe, Fluids.SUPER_STIMPAK), 1, 1, 1),
			};
		}};
		new ItemPool(POOL_PILE_MED_PILLS) {{
			this.pool = new WeightedRandomChestContent[] {
					weighted(filledSyringe(ModItems.syringe, Fluids.RADAWAY), 1, 1, 10),
					weighted(filledSyringe(ModItems.syringe, Fluids.RADX), 1, 1, 10),
					weighted(filledSyringe(ModItems.syringe, Fluids.RADAWAY), 1, 1, 15),
					weighted(ModItems.siox, 0, 1, 1, 5),
			};
		}};

		//makeshift gun
		new ItemPool(POOL_PILE_MAKESHIFT_GUN) {{ this.pool = new WeightedRandomChestContent[] { weighted(ModItems.gun_maresleg, 0, 1, 1, 10) }; }};
		new ItemPool(POOL_PILE_MAKESHIFT_WRENCH) {{ this.pool = new WeightedRandomChestContent[] { weighted(ModItems.wrench, 0, 1, 1, 10) }; }};
		new ItemPool(POOL_PILE_MAKESHIFT_PLATES) {{ this.pool = new WeightedRandomChestContent[] { weighted(ModItems.plate_steel, 0, 1, 1, 10) }; }};
		new ItemPool(POOL_PILE_MAKESHIFT_WIRE) {{ this.pool = new WeightedRandomChestContent[] { weighted(ModItems.wire_fine, Mats.MAT_ALUMINIUM.id, 1, 1, 10) }; }};

		new ItemPool(POOL_PILE_NUKE_STORAGE) {{
			this.pool = new WeightedRandomChestContent[] {
					weighted(ModItems.ammo_standard, EnumAmmo.NUKE_STANDARD.ordinal(), 1, 1, 50),
					weighted(ModItems.ammo_standard, EnumAmmo.NUKE_HIGH.ordinal(), 1, 1, 10),
					weighted(ModItems.ammo_standard, EnumAmmo.NUKE_TOTS.ordinal(), 1, 1, 10),

			};
		}};

		new ItemPool(POOL_PILE_OF_GARBAGE) {{
			this.pool = new WeightedRandomChestContent[] {
				weighted(ModItems.pipe, 2600, 0, 2, 20),
				weighted(ModItems.scrap, 0, 1, 5, 20),
				weighted(ModItems.dust, 0, 1, 3, 40),
				weighted(ModItems.dust_tiny, 0, 1, 7, 40),
				weighted(ModItems.powder_cement, 0, 1, 6, 40),
				weighted(ModItems.nugget_lead, 0, 0, 3, 20),
				weighted(ModItems.wire_fine, Mats.MAT_LEAD.id, 1, 2, 20),
				weighted(ModItems.powder_ash, 0, 0, 1, 15),
				weighted(ModItems.plate_lead, 0, 0, 1, 15),
				weighted(Items.string, 0, 0, 1, 15),
				weighted(ModItems.bolt, 8200, 0, 2, 15),
				weighted(ModItems.pin, 0, 0, 2, 15),
				weighted(ModItems.cap_nuka, 0, 0, 8, 15),
				weighted(ModItems.plate_iron, 0, 0, 2, 15),
				weighted(ModItems.fallout, 0, 0, 2, 15),
				weighted(ModItems.coil_tungsten, 0, 0, 2, 15),
				weighted(ModItems.can_empty, 0, 0, 1, 15),
				weighted(ModItems.ingot_asbestos, 0, 0, 1, 15),
				weighted(filledSyringe(ModItems.combat_syringe, Fluids.STIMPAK), 0, 1, 6),
				weighted(filledSyringe(ModItems.combat_syringe, Fluids.MEDX), 0, 1, 4),
				weighted(filledSyringe(ModItems.combat_syringe, Fluids.PSYCHO), 0, 1, 3),
				weighted(filledSyringe(ModItems.combat_syringe, Fluids.SUPER_STIMPAK), 0, 1, 2),
				weighted(filledSyringe(ModItems.syringe, Fluids.STIMPAK), 0, 1, 6),
				weighted(filledSyringe(ModItems.syringe, Fluids.MEDX), 0, 1, 4),
				weighted(filledSyringe(ModItems.syringe, Fluids.PSYCHO), 0, 1, 3),
				weighted(filledSyringe(ModItems.syringe, Fluids.SUPER_STIMPAK), 0, 1, 2),
				weighted(ModItems.pipe_lead, 0, 0, 1, 5),
				weighted(ModItems.motor, 0, 0, 1, 5),
				weighted(ModItems.canned_conserve, 2, 0, 1, 5),
			};
		}};

		new ItemPool(POOL_PILE_MECHANICAL) {{
			this.pool = new WeightedRandomChestContent[] {
				weighted(ModItems.defuser, 0, 1, 1, 30),
				weighted(ModItems.screwdriver, 0, 1, 1, 30),
				weighted(ModItems.wire_fine, Mats.MAT_COPPER.id, 8, 12, 120),
				weighted(ModItems.plate_steel, 0, 3, 8, 40),
				weighted(ModItems.plate_copper, 0, 2, 5, 40),
				weighted(ModItems.coil_copper, 0, 2, 5, 40),
				weighted(ModItems.coil_tungsten, 0, 2, 5, 40)
			};
		}};

		new ItemPool(POOL_PILE_GEAR) {{
			this.pool = new WeightedRandomChestContent[] {
				weighted(ModItems.defuser, 0, 1, 1, 40),
				weighted(ModItems.screwdriver, 0, 1, 1, 30),
				weighted(ModItems.canteen_vodka,0, 1, 1, 40),
				weighted(ModItems.casing, ItemEnums.EnumCasingType.SMALL_STEEL.ordinal(), 1, 4, 30),
				weighted(ModItems.casing, ItemEnums.EnumCasingType.SMALL.ordinal(), 3, 8, 40),
				weighted(ModItems.casing, ItemEnums.EnumCasingType.BUCKSHOT.ordinal(), 3, 8, 40),
				weighted(ModItems.canned_conserve, 0, 2, 5, 40),
				weighted(ModItems.taurun_helmet, 0, 1, 1, 20),
				weighted(ModItems.taurun_plate, 0, 1, 1, 20),
				weighted(ModItems.taurun_legs, 0, 1, 1, 20),
				weighted(ModItems.taurun_boots, 0, 1, 1, 20)
			};
		}};
		new ItemPool(POOL_PILE_SUPPLIES) {{
			this.pool = new WeightedRandomChestContent[] {
				weighted(ItemGrenadeUniversal.make(EnumGrenadeShell.FRAG, EnumGrenadeFilling.HE, EnumGrenadeFuze.S3, EnumGrenadeExtra.FRAG_SLEEVE), 3, 5, 10),
				weighted(ItemGrenadeUniversal.make(EnumGrenadeShell.FRAG, EnumGrenadeFilling.HE, EnumGrenadeFuze.S3, EnumGrenadeExtra.FRAG_SLEEVE), 3, 5, 10),
				weighted(ItemGrenadeUniversal.make(EnumGrenadeShell.FRAG, EnumGrenadeFilling.HE, EnumGrenadeFuze.S3, EnumGrenadeExtra.FRAG_SLEEVE), 3, 5, 10),
				weighted(filledSyringe(ModItems.syringe, Fluids.STIMPAK), 3, 5, 30),
				weighted(filledSyringe(ModItems.syringe, Fluids.PSYCHO), 3, 5, 30),
				weighted(filledSyringe(ModItems.syringe, Fluids.ANTIDOTE), 1, 2, 30),
				weighted(ModItems.ammo_container, 0, 2, 3, 40)
			};
		}};
	}
}
