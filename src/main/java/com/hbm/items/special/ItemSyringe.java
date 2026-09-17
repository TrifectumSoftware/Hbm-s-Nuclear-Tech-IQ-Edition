package com.hbm.items.special;

import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.Level;

import com.hbm.config.GeneralConfig;
import com.hbm.config.VersatileConfig;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.handler.ArmorModHandler;
import com.hbm.interfaces.Spaghetti;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.trait.FT_Drug;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.potion.HbmPotion;

import api.hbm.fluidmk2.IFillableItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

@Spaghetti("fuck")
public class ItemSyringe extends Item {

	Random rand = new Random();

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(this == ModItems.med_bag && !VersatileConfig.hasPotionSickness(player)) {
			if(!world.isRemote) {
				player.setHealth(player.getMaxHealth());

				player.removePotionEffect(Potion.blindness.id);
				player.removePotionEffect(Potion.confusion.id);
				player.removePotionEffect(Potion.digSlowdown.id);
				player.removePotionEffect(Potion.hunger.id);
				player.removePotionEffect(Potion.moveSlowdown.id);
				player.removePotionEffect(Potion.poison.id);
				player.removePotionEffect(Potion.weakness.id);
				player.removePotionEffect(Potion.wither.id);
				player.removePotionEffect(HbmPotion.radiation.id);

				VersatileConfig.applyPotionSickness(player, 15);

				stack.stackSize--;
			}
		}

		if(this == ModItems.syringe_taint) {
			if(!world.isRemote) {
				player.addPotionEffect(new PotionEffect(HbmPotion.taint.id, 60 * 20, 0));
				player.addPotionEffect(new PotionEffect(Potion.confusion.id, 5 * 20, 0));

				stack.stackSize--;
				world.playSoundAtEntity(player, "hbm:item.syringe", 1.0F, 1.0F);
			}

			if(!player.inventory.addItemStackToInventory(new ItemStack(ModItems.combat_syringe))) {
				player.dropPlayerItemWithRandomChoice(new ItemStack(ModItems.combat_syringe, 1, 0), false);
			}

			if(!player.inventory.addItemStackToInventory(new ItemStack(ModItems.bottle2_empty))) {
				player.dropPlayerItemWithRandomChoice(new ItemStack(ModItems.bottle2_empty, 1, 0), false);
			}
		}

		if(this == ModItems.gas_mask_filter_mono && player.inventory.armorInventory[3] != null && player.inventory.armorInventory[3].getItem() == ModItems.gas_mask_mono) {
			if(!world.isRemote) {
				if(player.inventory.armorInventory[3].getItemDamage() == 0)
					return stack;

				player.inventory.armorInventory[3].setItemDamage(0);

				world.playSoundAtEntity(player, "hbm:item.gasmaskScrew", 1.0F, 1.0F);
				stack.stackSize--;
			}
		}

		if(this == ModItems.jetpack_tank && player.inventory.armorInventory[2] != null) {

			if(!world.isRemote) {

				ItemStack jetpack = player.inventory.armorInventory[2];

				if(jetpack == null)
					return stack;

				if(jetpack.getItem() instanceof ItemArmor && ArmorModHandler.hasMods(jetpack)) {
					jetpack = ArmorModHandler.pryMods(jetpack)[ArmorModHandler.plate_only];
				}

				if(jetpack == null || !(jetpack.getItem() instanceof IFillableItem))
					return stack;

				IFillableItem fillable = (IFillableItem) jetpack.getItem();

				if(!fillable.acceptsFluid(Fluids.KEROSENE, jetpack))
					return stack;

				if(fillable.tryFill(Fluids.KEROSENE, 1000, jetpack) < 1000) {
					world.playSoundAtEntity(player, "hbm:item.jetpackTank", 1.0F, 1.0F);
					stack.stackSize--;
				}

				if(jetpack.getItem() != player.inventory.armorInventory[2].getItem())
					ArmorModHandler.applyMod(player.inventory.armorInventory[2], jetpack);
			}
		}

		if(this == ModItems.lox_tank && player.inventory.armorInventory[3] != null) {
			if(!world.isRemote) {
				ItemStack helmet = player.inventory.armorInventory[3];

				if(ArmorModHandler.hasMods(helmet)) {
					ItemStack tankStack = ArmorModHandler.pryMods(helmet)[ArmorModHandler.plate_only];
					if(tankStack == null || !(tankStack.getItem() instanceof IFillableItem))
						return stack;

					IFillableItem fillable = (IFillableItem) tankStack.getItem();

					if(!fillable.acceptsFluid(Fluids.OXYGEN, tankStack))
						return stack;

					if(fillable.tryFill(Fluids.OXYGEN, 1000, tankStack) < 1000) {
						world.playSoundAtEntity(player, "hbm:item.jetpackTank", 1.0F, 1.0F);
						stack.stackSize--;
					}

					ArmorModHandler.applyMod(player.inventory.armorInventory[3], tankStack);
				}
			}
		}

		if(this == ModItems.cbt_device) {
			if(!world.isRemote) {
				player.addPotionEffect(new PotionEffect(HbmPotion.bang.id, 30, 0));

				stack.stackSize--;
				world.playSoundAtEntity(player, "hbm:item.vice", 1.0F, 1.0F);
			}
		}

		return stack;
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase entity, EntityLivingBase entityPlayer) {
		World world = entity.worldObj;

		if(this == ModItems.syringe_taint) {
			if(!world.isRemote) {
				entity.addPotionEffect(new PotionEffect(HbmPotion.taint.id, 60 * 20, 0));
				entity.addPotionEffect(new PotionEffect(Potion.confusion.id, 5 * 20, 0));

				stack.stackSize--;
				world.playSoundAtEntity(entity, "hbm:item.syringe", 1.0F, 1.0F);

				if(entityPlayer instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) entityPlayer;
					if(!player.inventory.addItemStackToInventory(new ItemStack(ModItems.combat_syringe))) {
						player.dropPlayerItemWithRandomChoice(new ItemStack(ModItems.combat_syringe, 1, 0), false);
					}
					if(!player.inventory.addItemStackToInventory(new ItemStack(ModItems.bottle2_empty))) {
						player.dropPlayerItemWithRandomChoice(new ItemStack(ModItems.bottle2_empty, 1, 0), false);
					}
				}
			}
		}

		return false;
	}

	private FluidType getDrugFluid() {
		return null;
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		if(this == ModItems.med_bag) {
			list.add("Full heal, regardless of max health");
			list.add("Removes negative effects");
		}
		if(this == ModItems.syringe_taint) {
			list.add("Tainted I for 60 seconds");
			list.add("Nausea I for 5 seconds");
			list.add("Cloud damage + taint = tainted heart effect");
		}
		if(this == ModItems.gas_mask_filter) {
			list.add("Repairs worn gas mask");
		}
		if(this == ModItems.gas_mask_filter_mono) {
			list.add("Repairs worn monoxide mask");
		}
		if(this == ModItems.jetpack_tank) {
			list.add("Fills worn jetpack with up to 1000mB of kerosene");
		}
		if(this == ModItems.lox_tank) {
			list.add("Fills a worn PLSS with 1000mB of oxygen");
		}
		if(this == ModItems.gun_kit_1) {
			list.add("Repairs all weapons in hotbar by 10%");
		}
		if(this == ModItems.gun_kit_2) {
			list.add("Repairs all weapons in hotbar by 50%");
		}
	}
}
