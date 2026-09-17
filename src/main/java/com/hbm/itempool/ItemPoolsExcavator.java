package com.hbm.itempool;

import static com.hbm.lib.HbmChestContents.weighted;

import com.hbm.items.ModItems;
import com.hbm.items.weapon.grenade.ItemGrenadeUniversal;
import com.hbm.items.weapon.grenade.ItemGrenadeFilling.EnumGrenadeFilling;
import com.hbm.items.weapon.grenade.ItemGrenadeFuze.EnumGrenadeFuze;
import com.hbm.items.weapon.grenade.ItemGrenadeShell.EnumGrenadeShell;

import net.minecraft.util.WeightedRandomChestContent;

public class ItemPoolsExcavator {

	public static final String POOL_GRENADE_TRAP = "POOL_GRENADE_TRAP";
	public static final String POOL_POWDERS_VALUABLE = "POOL_POWDERS_VALUABLE";
	public static final String POOL_ANTIMATTER = "POOL_ANTIMATTER";

	public static void init() {

		new ItemPool(POOL_GRENADE_TRAP) {{
			this.pool = new WeightedRandomChestContent[] {
					weighted(ItemGrenadeUniversal.make(EnumGrenadeShell.FRAG, EnumGrenadeFilling.HE, EnumGrenadeFuze.S3), 1, 3, 10),
					weighted(ItemGrenadeUniversal.make(EnumGrenadeShell.FRAG, EnumGrenadeFilling.DEMO, EnumGrenadeFuze.S3), 1, 2, 8),
					weighted(ItemGrenadeUniversal.make(EnumGrenadeShell.FRAG, EnumGrenadeFilling.INC, EnumGrenadeFuze.S3), 1, 2, 6),
					weighted(ItemGrenadeUniversal.make(EnumGrenadeShell.FRAG, EnumGrenadeFilling.WP, EnumGrenadeFuze.S3), 1, 1, 4),
					weighted(ItemGrenadeUniversal.make(EnumGrenadeShell.FRAG, EnumGrenadeFilling.CLUSTER, EnumGrenadeFuze.S3), 1, 1, 3),
					weighted(ItemGrenadeUniversal.make(EnumGrenadeShell.STICK, EnumGrenadeFilling.HE, EnumGrenadeFuze.S3), 1, 3, 10),
					weighted(ItemGrenadeUniversal.make(EnumGrenadeShell.STICK, EnumGrenadeFilling.DEMO, EnumGrenadeFuze.S3), 1, 2, 8)
			};
		}};

		new ItemPool(POOL_POWDERS_VALUABLE) {{
			this.pool = new WeightedRandomChestContent[] {
					weighted(ModItems.powder_boron, 0, 2, 4, 8),
					weighted(ModItems.powder_boron_tiny, 0, 4, 8, 10),
					weighted(ModItems.powder_niobium, 0, 1, 3, 6),
					weighted(ModItems.powder_niobium_tiny, 0, 3, 6, 8),
					weighted(ModItems.powder_neodymium, 0, 2, 4, 7),
					weighted(ModItems.powder_neodymium_tiny, 0, 3, 6, 9),
					weighted(ModItems.powder_cobalt, 0, 1, 3, 5),
					weighted(ModItems.powder_cobalt_tiny, 0, 2, 4, 7),
					weighted(ModItems.powder_cerium, 0, 1, 3, 5),
					weighted(ModItems.powder_cerium_tiny, 0, 2, 4, 7),
					weighted(ModItems.powder_lanthanium, 0, 1, 2, 4),
					weighted(ModItems.powder_lanthanium_tiny, 0, 2, 4, 6),
					weighted(ModItems.powder_actinium, 0, 1, 1, 2),
					weighted(ModItems.powder_actinium_tiny, 0, 1, 2, 3),
					weighted(ModItems.powder_thorium, 0, 1, 2, 4),
					weighted(ModItems.powder_uranium, 0, 1, 2, 3),
					weighted(ModItems.powder_titanium, 0, 2, 4, 6),
					weighted(ModItems.powder_tungsten, 0, 1, 3, 5),
					weighted(ModItems.powder_lithium, 0, 1, 2, 4),
					weighted(ModItems.powder_lithium_tiny, 0, 2, 4, 6)
			};
		}};

		new ItemPool(POOL_ANTIMATTER) {{
			this.pool = new WeightedRandomChestContent[] {
					weighted(ModItems.cell_antimatter, 0, 1, 1, 3),
					weighted(ModItems.pellet_antimatter, 0, 1, 1, 1)
			};
		}};
	}
}
