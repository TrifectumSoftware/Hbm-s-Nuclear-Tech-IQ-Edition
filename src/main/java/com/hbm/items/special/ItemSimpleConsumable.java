package com.hbm.items.special;

import java.util.function.BiConsumer;

import com.hbm.config.VersatileConfig;
import com.hbm.items.ItemCustomLore;
import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.util.EnchantmentUtil;
import com.hbm.util.Tuple.Pair;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class ItemSimpleConsumable extends ItemCustomLore {
	
	//if java is giving me the power of generics and delegates then i'm going to use them, damn it!
	private BiConsumer<ItemStack, EntityPlayer> useAction;
	private BiConsumer<ItemStack, EntityPlayer> useActionServer;
	private BiConsumer<ItemStack, Pair<EntityLivingBase, EntityLivingBase>> hitAction;
	private BiConsumer<ItemStack, Pair<EntityLivingBase, EntityLivingBase>> hitActionServer;
	
	public ItemSimpleConsumable() {
		this.setCreativeTab(MainRegistry.consumableTab);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(this.useAction != null)
			this.useAction.accept(stack, player);
		
		if(!world.isRemote && this.useActionServer != null)
			this.useActionServer.accept(stack, player);
		
		return stack;
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase entity, EntityLivingBase entityPlayer) {
		
		if(this.hitAction != null)
			this.hitAction.accept(stack, new Pair(entity, entityPlayer));
		
		if(!entity.worldObj.isRemote && this.hitActionServer != null)
			this.hitActionServer.accept(stack, new Pair(entity, entityPlayer));
		
		return false;
	}
	
	public static void giveSoundAndDecrement(ItemStack stack, EntityLivingBase entity, String sound, ItemStack container) {
		stack.stackSize--;
		entity.worldObj.playSoundAtEntity(entity, sound, 1.0F, 1.0F);
		ItemSimpleConsumable.tryAddItem(entity, container);
	}
	
	public static void addPotionEffect(EntityLivingBase entity, Potion effect, int duration, int level) {
		
		if(!entity.isPotionActive(effect)) {
			entity.addPotionEffect(new PotionEffect(effect.id, duration, level));
		} else {
			int d = entity.getActivePotionEffect(effect).getDuration() + duration;
			entity.addPotionEffect(new PotionEffect(effect.id, d, level));
		}
	}
	
	public static void tryAddItem(EntityLivingBase entity, ItemStack stack) {
		if(entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entity;
			if(!player.inventory.addItemStackToInventory(stack)) {
				player.dropPlayerItemWithRandomChoice(stack, false);
			}
		}
	}
	
	//this formatting style probably already has a name but i will call it "the greg"
	public ItemSimpleConsumable setUseAction(		BiConsumer<ItemStack, EntityPlayer> delegate) {								this.useAction = delegate;			return this; }
	public ItemSimpleConsumable setUseActionServer(	BiConsumer<ItemStack, EntityPlayer> delegate) {								this.useActionServer = delegate;	return this; }
	public ItemSimpleConsumable setHitAction(		BiConsumer<ItemStack, Pair<EntityLivingBase, EntityLivingBase>> delegate) {	this.hitAction = delegate;			return this; }
	public ItemSimpleConsumable setHitActionServer(	BiConsumer<ItemStack, Pair<EntityLivingBase, EntityLivingBase>> delegate) {	this.hitActionServer = delegate;	return this; }
	
	public static void init() {
		
		/// BLOOD BAGS ///
		ModItems.iv_empty = new ItemSimpleConsumable().setUseActionServer((stack, user) -> {
			giveSoundAndDecrement(stack, user, "hbm:item.syringe", new ItemStack(ModItems.iv_blood));
			user.setHealth(Math.max(user.getHealth() - 5F, 0F));
			com.hbm.extprop.HbmBloodstreamProps.getData(user).drainBlood(100F);
			if(user.getHealth() <= 0) user.onDeath(DamageSource.magic);
		}).setUnlocalizedName("iv_empty").setTextureName(RefStrings.MODID + ":iv_empty");

		ModItems.iv_blood = new ItemSimpleConsumable().setUseActionServer((stack, user) -> {
			giveSoundAndDecrement(stack, user, "hbm:item.radaway", new ItemStack(ModItems.iv_empty));
			user.heal(5F);
			com.hbm.extprop.HbmBloodstreamProps.getData(user).addBlood(100F);
		}).setUnlocalizedName("iv_blood").setTextureName(RefStrings.MODID + ":iv_blood");

		ModItems.iv_xp_empty = new ItemSimpleConsumable().setUseActionServer((stack, user) -> {
			if(EnchantmentUtil.getTotalExperience(user) >= 100) {
				giveSoundAndDecrement(stack, user, "hbm:item.syringe", new ItemStack(ModItems.iv_xp));
				EnchantmentUtil.setExperience(user, EnchantmentUtil.getTotalExperience(user) - 100);
			}
		}).setUnlocalizedName("iv_xp_empty").setTextureName(RefStrings.MODID + ":iv_xp_empty");

		ModItems.iv_xp = new ItemSimpleConsumable().setUseActionServer((stack, user) -> {
			giveSoundAndDecrement(stack, user, "random.orb", new ItemStack(ModItems.iv_xp_empty));
			EnchantmentUtil.addExperience(user, 100, false);
		}).setUnlocalizedName("iv_xp").setTextureName(RefStrings.MODID + ":iv_xp");
	}
}
