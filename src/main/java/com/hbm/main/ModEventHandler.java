package com.hbm.main;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hbm.blocks.IStepTickReceiver;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockAshes;
import com.hbm.blocks.generic.BlockOre;
import com.hbm.blocks.machine.BlockBeamBase;
import com.hbm.blocks.generic.BlockPedestal;
import com.hbm.config.GeneralConfig;
import com.hbm.config.MobConfig;
import com.hbm.config.RadiationConfig;
import com.hbm.config.ServerConfig;
import com.hbm.config.SpaceConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.CelestialTeleporter;
import com.hbm.dim.SolarSystemWorldSavedData;
import com.hbm.dim.WorldGeneratorCelestial;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.WorldProviderEarth;
import com.hbm.dim.WorldTypeTeleport;
import com.hbm.dim.orbit.OrbitalStation;
import com.hbm.dim.orbit.WorldProviderOrbit;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.dim.trait.CBT_Invasion;
import com.hbm.dim.trait.CBT_Lights;
import com.hbm.dim.trait.CBT_Weather;
import com.hbm.dim.trait.CelestialBodyTrait;
import com.hbm.entity.missile.EntityRideableRocket;
import com.hbm.entity.mob.EntityCreeperTainted;
import com.hbm.entity.mob.EntityCyberCrab;
import com.hbm.entity.mob.EntityDrossDrone;
import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.entity.projectile.EntityBurningFOEQ;
import com.hbm.entity.train.EntityRailCarBase;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.ExplosionEffectWeapon;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.extprop.HbmBloodstreamProps;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.items.special.ItemHumanPart.EnumBodyStat;
import com.hbm.handler.blood.BloodBehavior;
import com.hbm.handler.husk.HuskAutomation;
import com.hbm.items.special.ItemHumanPart.EnumPartTrait;
import com.hbm.handler.ArmorModHandler;
import com.hbm.handler.BobmazonOfferFactory;
import com.hbm.handler.BossSpawnHandler;
import com.hbm.handler.EntityEffectHandler;
import com.hbm.handler.HTTPHandler;
import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.handler.atmosphere.ChunkAtmosphereManager;
import com.hbm.handler.neutron.NeutronNodeWorld;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.handler.SymbolBehaviors;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.hazard.type.HazardTypeNeutron;
import com.hbm.interfaces.IBomb;
import com.hbm.interfaces.Spaghetti;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.IEquipReceiver;
import com.hbm.items.ModItems;
import com.hbm.entity.mob.EntityHusk;
import com.hbm.items.armor.ArmorFSB;
import com.hbm.items.armor.IAttackHandler;
import com.hbm.items.armor.IDamageHandler;
import com.hbm.items.armor.ItemArmorMod;
import com.hbm.items.armor.ItemModDeadMansNeuralyser;
import com.hbm.items.armor.ItemModDefuser;
import com.hbm.items.armor.ItemModRevive;
import com.hbm.items.armor.ItemModShackles;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.factory.XFactory12ga;
import com.hbm.lib.ModDamageSource;
import com.hbm.lib.RefStrings;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.packet.toclient.PermaSyncPacket;
import com.hbm.packet.toclient.PlayerInformPacket;
import com.hbm.packet.toclient.SerializableRecipePacket;
import com.hbm.particle.helper.BlackPowderCreator;
import com.hbm.potion.HbmPotion;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.trait.Injectables;
import com.hbm.items.tool.ItemFluidSyringe;
import com.hbm.saveddata.satellites.SatelliteDetector;
import com.hbm.saveddata.satellites.SatelliteRayScan;
import com.hbm.tileentity.machine.TileEntityMachineRadarNT;
import com.hbm.tileentity.machine.rbmk.RBMKDials;
import com.hbm.tileentity.network.RTTYSystem;
import com.hbm.tileentity.network.RequestNetwork;
import com.hbm.uninos.UniNodespace;
import com.hbm.util.*;
import com.hbm.util.ArmorRegistry.HazardClass;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import com.hbm.world.PlanetGen;
import com.hbm.world.generator.TimedGenerator;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockFire;
import net.minecraft.block.IGrowable;
import net.minecraft.command.CommandGameRule;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandWeather;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.event.ClickEvent;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.FoodStats;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.*;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.FishingHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityEvent.EnteringChunk;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

@Spaghetti("fuck")
public class ModEventHandler {

	private static Random rand = new Random();

	@SubscribeEvent
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {

		if(!event.player.worldObj.isRemote) {

			if(GeneralConfig.enableMOTD) {
				event.player.addChatMessage(new ChatComponentText("Loaded world with NTM: IQ Edition " + RefStrings.VERSION + " for Minecraft 1.7.10!"));

				if(HTTPHandler.newVersion) {
					event.player.addChatMessage(
							new ChatComponentText("New version " + HTTPHandler.versionNumber + " is available! Click ")
							.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW))
							.appendSibling(new ChatComponentText("[here]")
									.setChatStyle(new ChatStyle()
										.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/TrifectumSoftware/Hbm-s-Nuclear-Tech-IQ-Edition/releases"))
										.setUnderlined(true)
										.setColor(EnumChatFormatting.RED)
									)
								)
							.appendSibling(new ChatComponentText(" to download!").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
							);
				}
			}

			if(MobConfig.enableDucks && event.player instanceof EntityPlayerMP && !event.player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getBoolean("hasDucked"))
				PacketDispatcher.wrapper.sendTo(new PlayerInformPacket("Press O to Duck!", ServerProxy.ID_DUCK, 30_000), (EntityPlayerMP) event.player);


			/*if(GeneralConfig.enableGuideBook) {
				HbmPlayerProps props = HbmPlayerProps.getData(event.player);

				if(!props.hasReceivedBook) {
					event.player.inventory.addItemStackToInventory(new ItemStack(ModItems.book_guide, 1, BookType.STARTER.ordinal()));
					event.player.inventoryContainer.detectAndSendChanges();
					props.hasReceivedBook = true;
				}
			}*/

			if(event.player.worldObj.getWorldInfo().getTerrainType() instanceof WorldTypeTeleport) {
				HbmPlayerProps props = HbmPlayerProps.getData(event.player);

				if(!props.hasWarped) {
					WorldTypeTeleport teleport = (WorldTypeTeleport) event.player.worldObj.getWorldInfo().getTerrainType();
					teleport.onPlayerJoin(event.player);
					props.hasWarped = true;
				}
			}

			if(GeneralConfig.enableServerRecipeSync && FMLCommonHandler.instance().getSide() == Side.SERVER && event.player instanceof EntityPlayerMP) {
				File recDir = new File(MainRegistry.configDir.getAbsolutePath() + File.separatorChar + "hbmRecipes");

				MainRegistry.logger.info("Sending recipes to client!");

				boolean hasSent = false;

				for(SerializableRecipe recipe : SerializableRecipe.recipeHandlers) {
					File recFile = new File(recDir.getAbsolutePath() + File.separatorChar + recipe.getFileName());
					if(recFile.exists() && recFile.isFile()) {
						MainRegistry.logger.info("Sending recipe file: " + recFile.getName());
						PacketDispatcher.wrapper.sendTo(new SerializableRecipePacket(recFile), (EntityPlayerMP) event.player);
						hasSent = true;
					}
				}

				if(hasSent) {
					PacketDispatcher.wrapper.sendTo(new SerializableRecipePacket(true), (EntityPlayerMP) event.player);
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerLeftClient(ClientDisconnectionFromServerEvent event) {
		SerializableRecipe.clearReceivedRecipes();
	}

	@SubscribeEvent
	public void onEntityConstructing(EntityEvent.EntityConstructing event) {

		if(event.entity instanceof EntityPlayer) {

			EntityPlayer player = (EntityPlayer) event.entity;
			HbmPlayerProps.getData(player); //this already calls the register method if it's null so no further action required
			com.hbm.extprop.HbmBloodstreamProps.getData(player); //ditto

			if(event.entity == MainRegistry.proxy.me())
				BlockAshes.ashes = 0;
		}

		if(event.entity instanceof EntityLivingBase) {

			EntityLivingBase living = (EntityLivingBase) event.entity;
			HbmLivingProps.getData(living); //ditto
		}
	}

	@SubscribeEvent
	public void onPlayerChangeDimension(PlayerChangedDimensionEvent event) {
		EntityPlayer player = event.player;
		HbmPlayerProps data = HbmPlayerProps.getData(player);
		data.setKeyPressed(EnumKeybind.JETPACK, false);
		data.setKeyPressed(EnumKeybind.DASH, false);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityDeathFirst(LivingDeathEvent event) {

		for(int i = 1; i < 5; i++) {

			ItemStack stack = event.entityLiving.getEquipmentInSlot(i);

			if(stack != null && stack.getItem() instanceof ItemArmor && ArmorModHandler.hasMods(stack)) {

				ItemStack revive = ArmorModHandler.pryMods(stack)[ArmorModHandler.extra];

				if(revive != null) {

					//Classic revive
					if(revive.getItem() instanceof ItemModRevive) {
						revive.setItemDamage(revive.getItemDamage() + 1);

						if(revive.getItemDamage() >= revive.getMaxDamage()) {
							ArmorModHandler.removeMod(stack, ArmorModHandler.extra);
						} else {
							ArmorModHandler.applyMod(stack, revive);
						}

						event.entityLiving.setHealth(event.entityLiving.getMaxHealth());
						event.entityLiving.addPotionEffect(new PotionEffect(Potion.resistance.id, 60, 99));
						event.setCanceled(true);
						return;
					}

					//Shackles
					if(revive.getItem() instanceof ItemModShackles && HbmLivingProps.getRadiation(event.entityLiving) < 1000F) {

						revive.setItemDamage(revive.getItemDamage() + 1);

						int dmg = revive.getItemDamage();
						ArmorModHandler.applyMod(stack, revive);

						event.entityLiving.setHealth(event.entityLiving.getMaxHealth());
						HbmLivingProps.incrementRadiation(event.entityLiving, dmg * dmg);
						event.setCanceled(true);
						return;
					}

					if(revive.getItem() instanceof ItemModDeadMansNeuralyser && event.entityLiving instanceof EntityPlayerMP && !event.entityLiving.worldObj.isRemote) {

						EntityHusk husk = ItemModDeadMansNeuralyser.getLinkedHusk(event.entityLiving, revive);

						if(husk != null) {
							event.entityLiving.setHealth(event.entityLiving.getMaxHealth());
							event.setCanceled(true);
							husk.beginDeathTransfer((EntityPlayer) event.entityLiving);
							return;
						}
					}
				}
			}
		}

	}

	@SubscribeEvent
	public void onEntityDeath(LivingDeathEvent event) {

		HbmLivingProps.setRadiation(event.entityLiving, 0);

		if(event.entity.worldObj.isRemote)
			return;

		if(event.entityLiving instanceof EntityPlayer) {
			HbmPlayerProps props = HbmPlayerProps.getData((EntityPlayer) event.entityLiving);
			props.huskName = "";
			props.huskUUID = "";
		}

		if(GeneralConfig.enableCataclysm) {
			EntityBurningFOEQ foeq = new EntityBurningFOEQ(event.entity.worldObj);
			foeq.setPositionAndRotation(event.entity.posX, 500, event.entity.posZ, 0.0F, 0.0F);
			event.entity.worldObj.spawnEntityInWorld(foeq);
		}

		if(event.entity.getUniqueID().toString().equals(SONUtil.HbMinecraft) || event.entity.getCommandSenderName().equals("HbMinecraft")) {
			event.entity.dropItem(ModItems.book_of_, 1);
		}

		if(event.entity.getUniqueID().toString().equals(SONUtil.MellowRPG8)) {
			event.entity.entityDropItem(new ItemStack(ModBlocks.block_meteor, 1 + rand.nextInt(10)), 0.0F);
		}

		if(event.entity.getUniqueID().toString().equals(SONUtil.Iristhepianist)) {
			event.entity.entityDropItem(new ItemStack(ModItems.nostalgic_gears, 1 + rand.nextInt(10)), 0.0F);
		}

		if(event.entity.getUniqueID().toString().equals(SONUtil.jengatower)) {
			event.entity.entityDropItem(new ItemStack(ModItems.fluid_tank_full, 1 + rand.nextInt(10), Fluids.CAY.getID()), 0.0F);
		}

		if(event.entity.getUniqueID().toString().equals(SONUtil.xenonio)) {
			event.entity.entityDropItem(new ItemStack(ModItems.fluid_tank_full, 1 + rand.nextInt(10), Fluids.XENON.getID()), 0.0F);
		}


		if(event.entity instanceof EntityCreeperTainted && event.source == ModDamageSource.boxcar) {

			for(Object o : event.entity.worldObj.getEntitiesWithinAABB(EntityPlayer.class, event.entity.boundingBox.expand(50, 50, 50))) {
				EntityPlayer player = (EntityPlayer)o;
				player.triggerAchievement(MainRegistry.bobHidden);
			}
		}
		CelestialBody body = CelestialBody.getBody(event.entity.worldObj);

		CBT_Invasion alien = body.getTrait(CBT_Invasion.class);
		if(alien != null) {
			alien.onKill(event.entityLiving, body);
		}


		if(!event.entityLiving.worldObj.isRemote && event.source == ModDamageSource.eve) {
			for(int i = -1; i < 2; i++) {
				for(int j = -1; j < 2; j++) {
					for(int k = -1; k < 2; k++) {
						if(event.entityLiving.worldObj.getBlock((int)event.entityLiving.posX + i, (int)event.entityLiving.posY + j, (int)event.entityLiving.posZ + k) == Blocks.air) {
							if(ModBlocks.flesh_block.canPlaceBlockAt(event.entityLiving.worldObj, (int)event.entityLiving.posX + i, (int)event.entityLiving.posY + j, (int)event.entityLiving.posZ + k)) {
								event.entityLiving.worldObj.setBlock((int)event.entityLiving.posX + i, (int)event.entityLiving.posY + j, (int)event.entityLiving.posZ + k, ModBlocks.flesh_block);
							}
						}
					}
				}
			}
		}


		if(!event.entityLiving.worldObj.isRemote && event.entityLiving.worldObj.getGameRules().getGameRuleBooleanValue("doMobLoot")) {

			if(event.source instanceof EntityDamageSource && ((EntityDamageSource)event.source).getEntity() instanceof EntityPlayer
					 && !(((EntityDamageSource)event.source).getEntity() instanceof FakePlayer)) {

				Random rng = event.entityLiving.getRNG();

				if(event.entityLiving instanceof EntitySpider && rng.nextInt(500) == 0) {
					event.entityLiving.dropItem(ModItems.spider_milk, 1);
				}

				if(event.entityLiving instanceof EntityCaveSpider && rng.nextInt(100) == 0) {
					event.entityLiving.dropItem(ModItems.serum, 1);
				}

				if(event.entityLiving instanceof EntityAnimal && rng.nextInt(500) == 0) {
					event.entityLiving.dropItem(ModItems.bandaid, 1);
				}

				if(event.entityLiving instanceof IMob) {
					if(rng.nextInt(1000) == 0) event.entityLiving.dropItem(ModItems.heart_piece, 1);
					if(rng.nextInt(250) == 0) event.entityLiving.dropItem(ModItems.key_red_cracked, 1);
					if(rng.nextInt(250) == 0) event.entityLiving.dropItem(ModItems.launch_code_piece, 1);
				}

				if(event.entityLiving instanceof EntityCyberCrab && rng.nextInt(500) == 0) {
					event.entityLiving.dropItem(ModItems.wd40, 1);
				}

				if(event.entityLiving instanceof EntityVillager && event.entityLiving.getRNG().nextInt(1) == 0) {
					event.entityLiving.dropItem(ModItems.flesh, 5);
				}

				if(event.entityLiving instanceof EntityZombie) {
					if(rng.nextInt(200) == 0) event.entityLiving.dropItem(ModItems.ingot_copper, 1);
					if(rng.nextInt(200) == 0) event.entityLiving.dropItem(ModItems.ingot_aluminium, 1);
					if(rng.nextInt(200) == 0) event.entityLiving.dropItem(ModItems.ingot_titanium, 1);
				}
		}
	}
}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onEntityDeathLast(LivingDeathEvent event) {

		EntityLivingBase entity = event.entityLiving;

		if(EntityDamageUtil.wasAttackedByV1(event.source)) {

			NBTTagCompound vdat = new NBTTagCompound();
			vdat.setString("type", "giblets");
			vdat.setInteger("ent", entity.getEntityId());
			PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(vdat, entity.posX, entity.posY + entity.height * 0.5, entity.posZ), new TargetPoint(entity.dimension, entity.posX, entity.posY + entity.height * 0.5, entity.posZ, 150));

			entity.worldObj.playSoundEffect(entity.posX, entity.posY, entity.posZ, "mob.zombie.woodbreak", 2.0F, 0.95F + entity.worldObj.rand.nextFloat() * 0.2F);

			EntityPlayer attacker = (EntityPlayer) ((EntityDamageSource)event.source).getEntity();

			if(attacker.getDistanceSqToEntity(entity) < 100) {
				attacker.heal(entity.getMaxHealth() * 0.25F);
			}
		}

		if(event.entityLiving instanceof EntityPlayer) {

			EntityPlayer player = (EntityPlayer) event.entityLiving;

			for(int i = 0; i < player.inventory.getSizeInventory(); i++) {

				ItemStack stack = player.inventory.getStackInSlot(i);

				if(stack != null && stack.getItem() == ModItems.detonator_deadman) {

					if(stack.stackTagCompound != null) {

						int x = stack.stackTagCompound.getInteger("x");
						int y = stack.stackTagCompound.getInteger("y");
						int z = stack.stackTagCompound.getInteger("z");

						if(!player.worldObj.isRemote && player.worldObj.getBlock(x, y, z) instanceof IBomb) {

							((IBomb) player.worldObj.getBlock(x, y, z)).explode(player.worldObj, x, y, z);

							if(GeneralConfig.enableExtendedLogging)
								MainRegistry.logger.info("[DET] Tried to detonate block at " + x + " / " + y + " / " + z + " by dead man's switch from " + player.getDisplayName() + "!");
						}

						player.inventory.setInventorySlotContents(i, null);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void decorateMob(LivingSpawnEvent event) {
		EntityLivingBase entity = event.entityLiving;
		World world = event.world;

		if(!MobConfig.enableMobGear || entity.isChild() || world.isRemote) return;

		Map<Integer, List<WeightedRandomObject>> slotPools = new HashMap<>();

		float soot = PollutionHandler.getPollution(entity.worldObj, MathHelper.floor_double(event.x), MathHelper.floor_double(event.y), MathHelper.floor_double(event.z), PollutionType.SOOT); //uhfgfg

		if(entity instanceof EntityZombie) {
			if(world.rand.nextFloat() < 0.005F && soot > 2) { // full hazmat zombine
				MobUtil.equipFullSet(entity, ModItems.hazmat_helmet, ModItems.hazmat_plate, ModItems.hazmat_legs, ModItems.hazmat_boots);
				return;
			}
			slotPools = MobUtil.slotPoolCommonS;

		} else if(entity instanceof EntitySkeleton) {
			slotPools = MobUtil.slotPoolRangedS;
			ItemStack bowReplacement = getSkelegun(soot, world.rand);
			slotPools.put(0, createSlotPool(50, bowReplacement != null ? new Object[][]{{bowReplacement, 1}} : new Object[][]{}));
		}

		MobUtil.assignItemsToEntity(entity, slotPools, rand);
	}

	private List<WeightedRandomObject> createSlotPool(int nullWeight, Object[][] items) {
		List<WeightedRandomObject> pool = new ArrayList<>();
		pool.add(new WeightedRandomObject(null, nullWeight));
		for (Object[] item : items) {
			Object obj = item[0];
			int weight = (int) item[1];

			if (obj instanceof Item) {
				pool.add(new WeightedRandomObject(new ItemStack((Item) obj), weight));
			} else if (obj instanceof ItemStack) {		//lol just make it pass ItemStack aswell
				pool.add(new WeightedRandomObject(obj, weight));
			}
		}
		return pool;
	}

	private static ItemStack getSkelegun(float soot, Random rand) {
		if(!MobConfig.enableMobWeapons) return null;

		soot -= MobConfig.mobWeaponSootReduction;
		if(rand.nextDouble() > Math.log(soot) * 0.25) return null;

		ArrayList<WeightedRandomObject> pool = new ArrayList<>();

		if(soot < 0.3){
			pool.add(new WeightedRandomObject(new ItemStack(ModItems.gun_pepperbox), 5));
			pool.add(new WeightedRandomObject(null, 20));
		} else if(soot > 0.3 && soot < 1) {
			pool.addAll(MobUtil.slotPoolGuns.get(0.3));
		} else if(soot < 3) {
			pool.addAll(MobUtil.slotPoolGuns.get(1D));
		} else if(soot < 5) {
			pool.addAll(MobUtil.slotPoolGuns.get(3D));
		} else {
			pool.addAll(MobUtil.slotPoolGuns.get(5D));
		}

		WeightedRandomObject selected = (WeightedRandomObject) WeightedRandom.getRandomItem(rand, pool);

		return selected.asStack();
	}

	@SubscribeEvent
	public void addAITasks(EntityJoinWorldEvent event) {
		if(event.world.isRemote || !(event.entity instanceof EntityLiving)) return;

		EntityLiving living = (EntityLiving) event.entity;
		ItemStack held = living.getHeldItem();

		if(held != null && held.getItem() instanceof ItemGunBaseNT) {
			MobUtil.addFireTask(living);
		}
	}

	@SubscribeEvent
	public void onItemToss(ItemTossEvent event) {

		ItemStack yeet = event.entityItem.getEntityItem();

		if(yeet.getItem() instanceof ItemArmor && ArmorModHandler.hasMods(yeet)) {

			ItemStack[] mods = ArmorModHandler.pryMods(yeet);
			ItemStack cladding = mods[ArmorModHandler.cladding];

			if(cladding != null && cladding.getItem() == ModItems.cladding_obsidian) {
				ReflectionHelper.setPrivateValue(Entity.class, event.entityItem, true, "field_149119_a", "field_83001_bt", "field_149500_a", "invulnerable");
			}
		}

		if(yeet.getItem() == ModItems.bismuth_tool) {
			ReflectionHelper.setPrivateValue(Entity.class, event.entityItem, true, "field_149119_a", "field_83001_bt", "field_149500_a", "invulnerable");
		}
	}

	@SubscribeEvent
	public void onBlockPlaced(PlaceEvent event) {
		if(event.world.isRemote) return;
		boolean placeCancelled = ChunkAtmosphereManager.proxy.runEffectsOnBlock(event.world, event.block, event.x, event.y, event.z);
		if(placeCancelled) return;

		if(event.block instanceof IGrowable) {
			ChunkAtmosphereManager.proxy.trackPlant(event.world, event.x, event.y, event.z);
		}

		if(SpaceConfig.allowNetherPortals && event.world.provider.dimensionId > 1 && event.block instanceof BlockFire) {
			Blocks.portal.func_150000_e(event.world, event.x, event.y, event.z);
		}

		// sneaky sneaky space furnace
		if(event.block == Blocks.furnace) {
			event.world.setBlock(event.x, event.y, event.z, ModBlocks.furnace, 0, 2);
			ModBlocks.furnace.onBlockPlacedBy(event.world, event.x, event.y, event.z, event.player, event.itemInHand);
		}

		if(event.y >= event.world.provider.getHorizon()) {
			if(event.block.getLightValue() > 10) {
				CelestialBody body = CelestialBody.getBody(event.world);
				CBT_Lights lights = body.getTrait(CBT_Lights.class);

				if(lights == null) lights = new CBT_Lights();
				lights.addLight(event.block, event.x, event.y, event.z);

				body.modifyTraits(lights);
			}
		}
	}

	@SubscribeEvent
	public void onBucketUse(FillBucketEvent event) {
		if(event.world.isRemote) return;
		if(event.target.typeOfHit != MovingObjectType.BLOCK) return;
		if(!(event.world.provider instanceof WorldProviderCelestial) && !(event.world.provider instanceof WorldProviderOrbit)) return;

		if(event.current != null && event.current.getItem() == Items.water_bucket) {
			ForgeDirection dir = ForgeDirection.getOrientation(event.target.sideHit);
			CBT_Atmosphere atmosphere = ChunkAtmosphereManager.proxy.getAtmosphere(event.world, event.target.blockX + dir.offsetX, event.target.blockY + dir.offsetY, event.target.blockZ + dir.offsetZ);
			boolean hasLiquidPressure = ChunkAtmosphereManager.proxy.hasLiquidPressure(atmosphere);

			if(Loader.isModLoaded(Compat.MOD_COFH)) {
				if(!hasLiquidPressure) {
					event.setCanceled(true);
				}
			} else {
				if(hasLiquidPressure) {
					event.world.provider.isHellWorld = false;
				}
			}
		}
	}

	@SubscribeEvent
	public void onLivingDrop(LivingDropsEvent event) {

		if(!event.entityLiving.worldObj.isRemote) {
			List<String> frames = com.hbm.handler.contagion.ItemContagion.getFomiteFrames(event.entityLiving);

			if(!frames.isEmpty()) {

				for(EntityItem item : event.drops) {
					com.hbm.handler.contagion.ItemContagion.tagEntityItem(item, frames);
				}
			}

			if(event.entityLiving instanceof EntityZombie && event.entityLiving.getRNG().nextInt(500) == 0) {
				event.drops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, com.hbm.handler.contagion.WildDisease.makeSyringe(event.entityLiving.getRNG())));
			}
		}
	}

	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event) {

		if(event.entityLiving instanceof EntityCreeper && event.entityLiving.getEntityData().getBoolean("hfr_defused")) {
			ItemModDefuser.castrateCreeper((EntityCreeper) event.entityLiving, null, false);
		}

		if(!event.entity.worldObj.isRemote && event.entityLiving instanceof EntityPlayer) {
			SymbolBehaviors.bounceProjectiles((EntityPlayer) event.entityLiving);
		}

		if(!event.entity.worldObj.isRemote && event.entityLiving.isPotionActive(HbmPotion.slippery.id)) {
			if (event.entityLiving.onGround) {
				double slipperiness = 0.6;
				double inertia = 0.1;
				boolean isMoving = event.entityLiving.moveForward != 0.0 || event.entityLiving.moveStrafing != 0.0;

				double angle = Math.atan2(event.entityLiving.motionZ, event.entityLiving.motionX);

				double targetXMotion = Math.cos(angle) * slipperiness;
				double targetZMotion = Math.sin(angle) * slipperiness;

				double diffX = targetXMotion - event.entityLiving.motionX;
				double diffZ = targetZMotion - event.entityLiving.motionZ;

				event.entityLiving.motionX += diffX * inertia; //god weeps
				event.entityLiving.motionZ += diffZ * inertia;

				if (!isMoving) {
					event.entityLiving.motionX *= (1.0 - 0.1);

					double totalVelocity = Math.sqrt(event.entityLiving.motionX * event.entityLiving.motionX + event.entityLiving.motionZ * event.entityLiving.motionZ);
					double smoothingAmount = totalVelocity * 0.02;
						event.entityLiving.motionX -= event.entityLiving.motionX / totalVelocity * smoothingAmount;
						event.entityLiving.motionZ -= event.entityLiving.motionZ / totalVelocity * smoothingAmount;
				}
			}
		}

		boolean isFlying = event.entity instanceof EntityPlayer ? ((EntityPlayer) event.entity).capabilities.isFlying : false;

		if(!isFlying) {
			if(event.entity instanceof EntityFlying) {
				return;
			}
			float gravity = CelestialBody.getGravity(event.entityLiving);

			if(gravity == 0) {
				event.entityLiving.motionY /= 0.98F;
				event.entityLiving.motionY += (AstronomyUtil.STANDARD_GRAVITY / 20F);

				if(event.entityLiving instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) event.entityLiving;
					if(player.isSneaking()) event.entityLiving.motionY -= 0.01F;
					if(player.isJumping) event.entityLiving.motionY += 0.01F;
				} else if(event.entity instanceof EntityChicken) {
					event.entityLiving.motionY = 0;
				}

				event.entityLiving.motionY *= 0.91F;
			} else if(!event.entityLiving.isInWater() && event.entityLiving.ticksExisted > 20 && (gravity < 1.5F || gravity > 1.7F)) {
				// If gravity is basically the same as normal, do nothing
				// Also do nothing in water, or if we've been alive less than a second (so we don't glitch into the ground)

				// Minimum gravity to prevent floating bug
				if(gravity < 0.2F) gravity = 0.2F;

				// Undo falling, and add our intended falling speed
				// On high gravity planets, only apply falling speed when descending, so we can still jump up single blocks
				if((gravity < 1.5F || event.entityLiving.motionY < 0) && !(event.entity instanceof EntityChicken)) {
					event.entityLiving.motionY /= 0.98F;
					event.entityLiving.motionY += (AstronomyUtil.STANDARD_GRAVITY / 20F);
					event.entityLiving.motionY -= (gravity / 20F);
					event.entityLiving.motionY *= 0.98F;
				}
			}
		}

		ItemStack[] prevArmor = event.entityLiving.previousEquipment;

		if(event.entityLiving instanceof EntityPlayerMP && prevArmor != null && event.entityLiving.getHeldItem() != null
				&& (prevArmor[0] == null || prevArmor[0].getItem() != event.entityLiving.getHeldItem().getItem())
				&& event.entityLiving.getHeldItem().getItem() instanceof IEquipReceiver) {

			((IEquipReceiver)event.entityLiving.getHeldItem().getItem()).onEquip((EntityPlayer) event.entityLiving, event.entityLiving.getHeldItem());
		}

		for(int i = 1; i < 5; i++) {

			ItemStack prev = prevArmor != null ? prevArmor[i] : null;
			ItemStack armor = event.entityLiving.getEquipmentInSlot(i);

			boolean reapply = prevArmor != null && !ItemStack.areItemStacksEqual(prev, armor);

			if(reapply) {

				if(prev != null && ArmorModHandler.hasMods(prev)) {

					for(ItemStack mod : ArmorModHandler.pryMods(prev)) {

						if(mod != null && mod.getItem() instanceof ItemArmorMod) {

							Multimap map = ((ItemArmorMod)mod.getItem()).getModifiers(prev);

							if(map != null)
								event.entityLiving.getAttributeMap().removeAttributeModifiers(map);
						}
					}
				}
			}

			if(armor != null && ArmorModHandler.hasMods(armor)) {

				for(ItemStack mod : ArmorModHandler.pryMods(armor)) {

					if(mod != null && mod.getItem() instanceof ItemArmorMod) {
						((ItemArmorMod)mod.getItem()).modUpdate(event.entityLiving, armor);
						HazardSystem.applyHazards(mod, event.entityLiving);

						if(reapply) {

							Multimap map = ((ItemArmorMod)mod.getItem()).getModifiers(armor);

							if(map != null)
								event.entityLiving.getAttributeMap().applyAttributeModifiers(map);
						}
					}
				}
			}
		}

		EntityEffectHandler.onUpdate(event.entityLiving);

		if(!event.entity.worldObj.isRemote && !(event.entityLiving instanceof EntityPlayer)) {
			HazardSystem.updateLivingInventory(event.entityLiving);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLoad(WorldEvent.Load event) {
		BobmazonOfferFactory.init();

		updateWaterOpacity(event.world);

		if(!(event.world.provider instanceof WorldProviderEarth)) {
			PlanetGen.overrideOverworldProvider();
		}
	}

	@SubscribeEvent
	public void onUnload(WorldEvent.Unload event) {
		NeutronNodeWorld.streamWorlds.remove(event.world);
	}

	public static boolean didSit = false;
	public static Field reference = null;

	@SubscribeEvent
	public void worldTick(WorldTickEvent event) {

		World world = event.world;
		long time = world.getTotalWorldTime();

		if(world != null && !world.isRemote) {

			if(world.provider.dimensionId == SpaceConfig.drossDimension && time % 100 == 0) {
				int count = 0;
				for(Object e : world.loadedEntityList) {
					if(e instanceof EntityDrossDrone) count++;
				}
				if(count < 24) {
					int sx = world.rand.nextInt(200) - 100;
					int sz = world.rand.nextInt(200) - 100;
					Object p = null;
					if(!world.playerEntities.isEmpty()) {
						p = world.playerEntities.get(world.rand.nextInt(world.playerEntities.size()));
					}
					if(p != null) {
						EntityPlayer pp = (EntityPlayer) p;
						sx = (int) pp.posX + world.rand.nextInt(60) - 30;
						sz = (int) pp.posZ + world.rand.nextInt(60) - 30;
					}
					int sy = 220 + world.rand.nextInt(21);
					EntityDrossDrone drone = new EntityDrossDrone(world);
					drone.setPosition(sx, sy, sz);
					world.spawnEntityInWorld(drone);
				}
			}

			if(reference != null) {
				for(Object player : world.playerEntities) {
					if(((EntityPlayer) player).ridingEntity != null && time % (1 * 60 * 20) == 0) {
						((EntityPlayer) player).mountEntity(null);
						didSit = true;
					}
				}
				/*if(didSit && event.world.getTotalWorldTime() % (1 * 20 * 20) == 0) {
					try { reference.setFloat(null, (float) (rand.nextGaussian() * 0.1 + Math.PI)); } catch(Throwable e) { }
				}*/
			}

			List loadedEntityList = new ArrayList();
			loadedEntityList.addAll(event.world.loadedEntityList); // ConcurrentModificationException my balls

			int tickrate = Math.max(1, ServerConfig.ITEM_HAZARD_DROP_TICKRATE.get());

			for(Object e : loadedEntityList) {

				if(e instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) e;

					// handle dismount events, or our players will splat upon leaving tall rockets
					if(player.ridingEntity != null && player.ridingEntity instanceof EntityRideableRocket && player.isSneaking()) {
						EntityRideableRocket rocket = (EntityRideableRocket) player.ridingEntity;

						if(player.isSneaking()) {
							// Prevent leaving a rocket in motion, for safety
							if(rocket.canExitCapsule() || rocket.forceExitTimer >= 60) {
								boolean inOrbit = event.world.provider instanceof WorldProviderOrbit;
								Entity ridingEntity = player.ridingEntity;
								float prevHeight = ridingEntity.height;

								ridingEntity.height = inOrbit ? ridingEntity.height + 1.0F : 1.0F;
								player.mountEntity(null);
								if(!inOrbit) player.setPositionAndUpdate(player.posX + 2, player.posY, player.posZ);
								ridingEntity.height = prevHeight;
							} else {
								rocket.forceExitTimer++;
							}

							player.setSneaking(false);
						} else {
							rocket.forceExitTimer = 0;
						}
					}
				}

				if(event.phase == Phase.END && event.world.getTotalWorldTime() % tickrate == 0) {
					if(e instanceof EntityItem) {
						EntityItem item = (EntityItem) e;
						HazardSystem.updateDroppedItem(item);
					}
				}
			}

			if(event.phase == Phase.END) {
				EntityRailCarBase.updateMotion(event.world);

				CelestialTeleporter.runQueuedTeleport();

				// Once per second, run atmospheric chemistry
				if(event.world.getTotalWorldTime() % 20 == 0) {
					CelestialBody.updateChemistry(event.world);
				}
			}

			if(time % 20 == 0) {
				BlockPedestal.checkPedestalEntries(world.provider.dimensionId, time);
			}

			// Tick our per celestial body timer
			if(event.phase == Phase.START && event.world.provider instanceof WorldProviderCelestial && event.world.provider.dimensionId != 0) {
				if(event.world.getGameRules().getGameRuleBooleanValue("doDaylightCycle")) {
					event.world.provider.setWorldTime(event.world.provider.getWorldTime() + 1L);
				}
			}

		}

		if(event.phase == Phase.START) {
			BossSpawnHandler.rollTheDice(world);
			TimedGenerator.automaton(world, 100);

			SatelliteDetector.updateSystem(world);
			if(world.getTotalWorldTime() % 20 == 10)
				SatelliteRayScan.updateSystem(world);

			updateWaterOpacity(event.world);
		}

		if(event.phase == Phase.START && event.world.provider.dimensionId == SpaceConfig.orbitDimension) {
			for(Object o : event.world.loadedEntityList) {
				if(o instanceof EntityItem) {
					EntityItem item = (EntityItem) o;
					item.motionX *= 0.9D;
					item.motionY = 0.03999999910593033D; // when entity gravity is applied, this becomes exactly 0
					item.motionZ *= 0.9D;
				}
			}
		}
		//if(event.phase == Phase.END) {
			//CelestialBody body = CelestialBody.getBody(event.world);
			//CBT_Invasion alien = body.getTrait(CBT_Invasion.class);

			//if(alien == null) return;
			//alien.SpawnAttempt(event.world);

			//body.modifyTraits(alien);
		//}

	}

	private void updateWaterOpacity(World world) {
		// Per world water opacity!
		int waterOpacity = 3;
		if(world.provider instanceof WorldProviderCelestial) {
			waterOpacity = ((WorldProviderCelestial) world.provider).getWaterOpacity();
		}

		Blocks.water.setLightOpacity(waterOpacity);
		Blocks.flowing_water.setLightOpacity(waterOpacity);
	}

	@SubscribeEvent
	public void onHarvestDrops(HarvestDropsEvent event) {
		BlockOre ore = BlockOre.vanillaMap.get(event.block);
		if(ore == null) return;
		if(event.isSilkTouching) return;

		int rawMeta = ore.getRawOreMeta();
		if(rawMeta < 0) return;

		event.drops.clear();
		int count = ore.quantityDroppedWithBonus(event.fortuneLevel, event.world.rand);
		event.drops.add(new ItemStack(ModItems.raw_ore, count, rawMeta));
	}

	@SubscribeEvent
	public void onGenerateOre(GenerateMinable event) {
		if(event.world.provider instanceof WorldProviderCelestial && event.world.provider.dimensionId != 0) {
			WorldGeneratorCelestial.onGenerateOre(event);
		}
	}

	@SubscribeEvent
	public void onEntityAttacked(LivingAttackEvent event) {

		EntityLivingBase e = event.entityLiving;

		if(e instanceof EntityPlayer) {

			EntityPlayer player = (EntityPlayer) e;

		if(SymbolBehaviors.cancelsDamage(event.source, player)) {
			event.setCanceled(true);
		}

			if(SymbolBehaviors.tryHandleFireAttack(event.source, player, event.ammount)) {
				event.setCanceled(true);
				return;
			}

			if(SymbolBehaviors.hasJustice(player) && event.source.isProjectile()) {
				event.setCanceled(true);
			}

			if(event.source.getEntity() instanceof EntityPlayer) {
				EntityPlayer attacker = (EntityPlayer) event.source.getEntity();
				if(SymbolBehaviors.cancelsPvP(attacker, player)) {
					event.setCanceled(true);
				} else if(SymbolBehaviors.hasJustice(player)) {
					SymbolBehaviors.recordAttacker(player, attacker);
				}
			}

			if(ArmorUtil.checkArmor(player, ModItems.euphemium_helmet, ModItems.euphemium_plate, ModItems.euphemium_legs, ModItems.euphemium_boots)) {
				HbmPlayerProps.plink(player, "random.break", 0.5F, 1.0F + e.getRNG().nextFloat() * 0.5F);
				event.setCanceled(true);
			}

			if(player.inventory.armorInventory[2] != null && player.inventory.armorInventory[2].getItem() instanceof ArmorFSB)
				((ArmorFSB)player.inventory.armorInventory[2].getItem()).handleAttack(event);

		for(ItemStack stack : player.inventory.armorInventory) {
			if(stack != null && stack.getItem() instanceof IAttackHandler) {
				((IAttackHandler)stack.getItem()).handleAttack(event, stack);
			}
		}
		}

		if(event.source.getEntity() instanceof EntityLivingBase && !event.source.isProjectile()) {
			com.hbm.handler.contagion.DiseaseHandler.handleHit((EntityLivingBase) event.source.getEntity(), event.entityLiving);
		}
	}

	@SubscribeEvent
	public void onEntityInteract(AttackEntityEvent event) {
		if(event.entityPlayer.worldObj.isRemote) return;
		ItemStack stack = event.entityPlayer.getHeldItem();
		if(stack == null || !(stack.getItem() instanceof ItemFluidSyringe)) return;
		if(!(event.target instanceof EntityLivingBase)) return;

		ItemFluidSyringe syringe = (ItemFluidSyringe) stack.getItem();
		int fill = syringe.getFill(stack);
		int dose = Math.min(10, fill);
		if(dose > 0) {
			Injectables.injectEntity(stack, (EntityLivingBase) event.target, event.entityPlayer, dose, 1.0F);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onEntityDamaged(LivingHurtEvent event) {

		EntityLivingBase e = event.entityLiving;

		if((e.isPotionActive(HbmPotion.turkishRage) || e.isPotionActive(HbmPotion.ganja)) && "lightningBolt".equals(event.source.getDamageType()))
			event.ammount = 0;

		if(e instanceof EntityPlayer) {

			EntityPlayer player = (EntityPlayer) e;

			HbmPlayerProps props = HbmPlayerProps.getData(player);
			if(props.shield > 0 |props.nitanHealth > 0) {
				float reduce = Math.min(props.shield+props.nitanHealth, event.ammount);
				props.shield -= reduce;
				props.nitanHealth -= reduce;
				event.ammount -= reduce;
			}
			props.lastDamage = player.ticksExisted;
		}

		if(e.isPotionActive(HbmPotion.symptomRash) || e.isPotionActive(HbmPotion.symptomFever))
			event.ammount *= 1.25F;

		if(event.source.getEntity() instanceof EntityLivingBase) {
			EntityLivingBase attacker = (EntityLivingBase) event.source.getEntity();
			if(attacker.isPotionActive(HbmPotion.symptomParalysis))
				event.ammount *= 0.1F;
		}

		if(e.isPotionActive(HbmPotion.symptomSeptic) && event.ammount > 0 && event.ammount < 100) {
			HbmBloodstreamProps bloodstream = HbmBloodstreamProps.getData(e);
			bloodstream.drainBlood(event.ammount * 5F);
		}

		if(e.isPotionActive(HbmPotion.symptomCardiac) && event.ammount > 0)
			event.ammount = Math.max(event.ammount, e.getMaxHealth());

		if(e.isPotionActive(HbmPotion.roidRage)) {
			e.removePotionEffect(HbmPotion.roidRage.id);
		}

		// roid rage logick
		if(event.source.getEntity() instanceof EntityLivingBase) {
			EntityLivingBase attacker = (EntityLivingBase) event.source.getEntity();
			if(attacker.isPotionActive(HbmPotion.roidRage)) {
				PotionEffect roidRage = attacker.getActivePotionEffect(HbmPotion.roidRage);
				int amp = roidRage.getAmplifier();
				int nextAmp = Math.min(amp + 1, 9);
				event.ammount *= Math.pow(1.25, nextAmp);
				attacker.addPotionEffect(new PotionEffect(HbmPotion.roidRage.id, roidRage.getDuration(), nextAmp));
			}
		}

		if(event.source.getEntity() instanceof EntityLivingBase) {
			EntityLivingBase attacker = (EntityLivingBase) event.source.getEntity();
			if(attacker.isPotionActive(HbmPotion.turkishRage) || attacker.isPotionActive(HbmPotion.ganja))
				event.ammount *= 2.0F;
		}

		// med ex logick
		if(e.isPotionActive(HbmPotion.medx)) {
			PotionEffect medxEffect = e.getActivePotionEffect(HbmPotion.medx);
			int startTime = e.getEntityData().getInteger("medxStartTime");
			if(startTime == 0) {
				startTime = e.ticksExisted;
				e.getEntityData().setInteger("medxStartTime", startTime);
			}
			int elapsed = e.ticksExisted - startTime;
			float reduction;
			if(elapsed < 200) {
				reduction = 0.95F - 0.70F * (elapsed / 200.0F);
			} else {
				reduction = 0.25F;
			}
			reduction = Math.max(0.25F, Math.min(0.95F, reduction));
			event.ammount *= (1.0F - reduction);
		} else {
			e.getEntityData().removeTag("medxStartTime");
		}

		/// V1 ///
		if(EntityDamageUtil.wasAttackedByV1(event.source)) {
			EntityPlayer attacker = (EntityPlayer) ((EntityDamageSource)event.source).getEntity();

			NBTTagCompound data = new NBTTagCompound();
			data.setString("type", "vanillaburst");
			data.setInteger("count", (int)Math.min(e.getMaxHealth() / 2F, 250));
			data.setDouble("motion", 0.1D);
			data.setString("mode", "blockdust");
			data.setInteger("block", Block.getIdFromBlock(Blocks.redstone_block));
			PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data, e.posX, e.posY + e.height * 0.5, e.posZ), new TargetPoint(e.dimension, e.posX, e.posY, e.posZ, 50));

			if(attacker.getDistanceSqToEntity(e) < 25) {
				attacker.heal(event.ammount * 0.5F);
			}
		}

		/// ARMOR MODS ///
		for(int i = 1; i < 5; i++) {

			ItemStack armor = e.getEquipmentInSlot(i);

			if(armor != null && ArmorModHandler.hasMods(armor)) {

				for(ItemStack mod : ArmorModHandler.pryMods(armor)) {

					if(mod != null && mod.getItem() instanceof ItemArmorMod) {
						((ItemArmorMod)mod.getItem()).modDamage(event, armor);
					}
				}
			}
		}

		if(e instanceof EntityPlayer) {

			EntityPlayer player = (EntityPlayer) e;

			/// FSB ARMOR ///
			if(player.inventory.armorInventory[2] != null && player.inventory.armorInventory[2].getItem() instanceof ArmorFSB)
				((ArmorFSB)player.inventory.armorInventory[2].getItem()).handleHurt(event);


			for(ItemStack stack : player.inventory.armorInventory) {
				if(stack != null && stack.getItem() instanceof IDamageHandler) {
					((IDamageHandler)stack.getItem()).handleDamage(event, stack);
				}
			}
		}

		if(e instanceof EntityPlayer && event.source.isProjectile() && SymbolBehaviors.hasJustice((EntityPlayer) e)) {
			event.ammount = 0;
		}

		if(event.source.getEntity() instanceof EntityPlayer) {
			EntityPlayer attacker = (EntityPlayer) event.source.getEntity();
			if(e instanceof EntityPlayer && SymbolBehaviors.cancelsPvP(attacker, (EntityPlayer) e)) {
				event.setCanceled(true);
			} else {
				SymbolBehaviors.handleJusticeHit(event, attacker);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerFall(PlayerFlyableFallEvent event) {

		EntityPlayer e = event.entityPlayer;

		if(e.inventory.armorInventory[2] != null && e.inventory.armorInventory[2].getItem() instanceof ArmorFSB)
			((ArmorFSB)e.inventory.armorInventory[2].getItem()).handleFall(e, event.distance);
	}

	// only for the ballistic gauntlet! contains dangerous conditional returns!
	@SubscribeEvent
	public void onPlayerPunch(AttackEntityEvent event) {

		EntityPlayer player = event.entityPlayer;
		ItemStack chestplate = player.inventory.armorInventory[2];

		if(!player.worldObj.isRemote && chestplate != null && ArmorModHandler.hasMods(chestplate)) {

			if(player.getHeldItem() != null && player.getHeldItem().getAttributeModifiers().containsKey(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName())) return;

			ItemStack[] mods = ArmorModHandler.pryMods(chestplate);
			ItemStack servo = mods[ArmorModHandler.servos];

			if(servo != null && servo.getItem() == ModItems.ballistic_gauntlet) {

				BulletConfig firedConfig = null;
				BulletConfig[] gauntletConfigs = new BulletConfig[] {XFactory12ga.g12_bp, XFactory12ga.g12_bp_magnum, XFactory12ga.g12_bp_slug, XFactory12ga.g12, XFactory12ga.g12_slug, XFactory12ga.g12_flechette, XFactory12ga.g12_magnum, XFactory12ga.g12_explosive, XFactory12ga.g12_phosphorus};

				for(BulletConfig config : gauntletConfigs) {

					if(InventoryUtil.doesPlayerHaveAStack(player, config.ammo, true, true)) {
						firedConfig = config;
						break;
					}
				}

				if(firedConfig != null) {
					int bullets = firedConfig.projectilesMin;

					if(firedConfig.projectilesMax > firedConfig.projectilesMin) {
						bullets += player.getRNG().nextInt(firedConfig.projectilesMax - firedConfig.projectilesMin);
					}

					for(int i = 0; i < bullets; i++) {
						EntityBulletBaseMK4 mk4 = new EntityBulletBaseMK4(player, firedConfig, 15F, 0F, -0.1875, -0.0625, 0.5);
						player.worldObj.spawnEntityInWorld(mk4);
						if(i == 0 && firedConfig.blackPowder) BlackPowderCreator.composeEffect(player.worldObj, mk4.posX, mk4.posY, mk4.posZ, mk4.motionX, mk4.motionY, mk4.motionZ, 10, 0.25F, 0.5F, 10, 0.25F);
					}

					player.worldObj.playSoundAtEntity(player, "hbm:weapon.shotgunShoot", 1.0F, 1.0F);
				}
			}
		}
	}


	private static int getBodyStat(EntityPlayer player, EnumBodyStat stat) {
		int[] stats = HbmPlayerProps.getData(player).bodyStats;
		return stats != null && stats.length > stat.ordinal() ? stats[stat.ordinal()] : 0;
	}

	private static boolean hasBodyTrait(EntityPlayer player, EnumPartTrait trait) {
		if(player.worldObj.isRemote) return false;
		return ("," + HbmPlayerProps.getData(player).bodyTraits + ",").contains("," + trait.name() + ",");
	}

	@SubscribeEvent
	public void onHuskBodyTick(TickEvent.PlayerTickEvent event) {
		EntityPlayer player = event.player;

		if(player.worldObj.isRemote || !(player instanceof EntityPlayerMP)) return;
		if(event.phase != TickEvent.Phase.END) return;

		HbmPlayerProps props = HbmPlayerProps.getData(player);
		if(!props.bodyRobot.isEmpty()) {
			if(props.robot == null) props.robot = new HuskAutomation(player);
			props.robot.update();
		}

		NBTTagCompound data = player.getEntityData();
		float last = data.hasKey("hbmLastHealth") ? data.getFloat("hbmLastHealth") : player.getHealth();
		float healed = player.getHealth() - last;
		if(healed > 0F) {
			int recovery = getBodyStat(player, EnumBodyStat.RECOVERY);
			if(recovery > 0) player.heal(healed * recovery / 100F);
		}
		data.setFloat("hbmLastHealth", player.getHealth());

		if(player.getHealth() < player.getMaxHealth() && hasBodyTrait(player, EnumPartTrait.CLOTTING)
				&& player.ticksExisted % 40 == 0 && player.motionX * player.motionX + player.motionZ * player.motionZ < 0.001D) {
			player.heal(1F);
		}

		if(hasBodyTrait(player, EnumPartTrait.HARDY) && player.ticksExisted % 100 == 0) {
			FoodStats food = player.getFoodStats();
			if(food.getSaturationLevel() < food.getFoodLevel())
				food.setFoodSaturationLevel(Math.min(food.getSaturationLevel() + 0.5F, food.getFoodLevel()));
		}

		if(hasBodyTrait(player, EnumPartTrait.SECOND_WIND) && player.getHealth() < 4F && player.ticksExisted % 200 == 0) {
			player.addPotionEffect(new PotionEffect(Potion.damageBoost.id, 100, 0));
		}
	}

	@SubscribeEvent
	public void onBloodBehaviorTick(LivingUpdateEvent event) {
		EntityLivingBase entity = event.entityLiving;
		if(!(entity instanceof EntityPlayer) && !(entity instanceof EntityHusk)) return;

		BloodBehavior behavior = BloodBehavior.get(entity);
		if(behavior != null) behavior.onTick(entity);
	}

	@SubscribeEvent
	public void onHuskBodyHurt(LivingHurtEvent event) {

		if(event.entity.worldObj.isRemote) return;

		BloodBehavior behavior = BloodBehavior.get(event.entityLiving);
		if(behavior != null) behavior.onHurt(event.entityLiving, event);

		if(event.entityLiving instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entityLiving;

			if(hasBodyTrait(player, EnumPartTrait.NUMB) && player.getRNG().nextFloat() < 0.25F) {
				event.setCanceled(true);
				return;
			}

			if(hasBodyTrait(player, EnumPartTrait.TWITCH)) player.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, 60, 0));
			if(hasBodyTrait(player, EnumPartTrait.GRISTLE)) event.ammount *= 0.75F;
		}

		Entity attacker = event.source.getEntity();

		if(attacker instanceof EntityPlayer) {
			EntityPlayer hitter = (EntityPlayer) attacker;

			if(hasBodyTrait(hitter, EnumPartTrait.BLOODTHIRST)) hitter.heal(1F);

			if(hasBodyTrait(hitter, EnumPartTrait.WHITE_KNUCKLES)) {
				double dx = event.entityLiving.posX - hitter.posX;
				double dz = event.entityLiving.posZ - hitter.posZ;
				double d = Math.sqrt(dx * dx + dz * dz);
				if(d > 0.01D) event.entityLiving.addVelocity(dx / d * 0.4D, 0.1D, dz / d * 0.4D);
			}
		}
	}

	@SubscribeEvent
	public void onHuskBodyFall(LivingFallEvent event) {
		if(event.entity.worldObj.isRemote || !(event.entityLiving instanceof EntityPlayer)) return;
		if(hasBodyTrait((EntityPlayer) event.entityLiving, EnumPartTrait.SURE_FOOTED)) event.setCanceled(true);
	}

	@SubscribeEvent
	public void onHuskBodyJump(LivingJumpEvent event) {
		if(event.entity.worldObj.isRemote || !(event.entityLiving instanceof EntityPlayer)) return;
		if(hasBodyTrait((EntityPlayer) event.entityLiving, EnumPartTrait.SPRING_HEEL)) event.entityLiving.motionY += 0.2D;
	}

	@SubscribeEvent
	public void onEntityJump(LivingJumpEvent event) {

		EntityLivingBase e = event.entityLiving;

		if(e instanceof EntityPlayer && ((EntityPlayer)e).inventory.armorInventory[2] != null && ((EntityPlayer)e).inventory.armorInventory[2].getItem() instanceof ArmorFSB)
			((ArmorFSB)((EntityPlayer)e).inventory.armorInventory[2].getItem()).handleJump((EntityPlayer)e);
	}

	@SubscribeEvent
	public void onEntityFall(LivingFallEvent event) {

		EntityLivingBase e = event.entityLiving;

		float gravity = CelestialBody.getGravity(e);


		// Reduce fall damage on low gravity bodies
		if(gravity < 0.3F) {
			event.distance = 0;
		} else if(gravity < 1.5F) {
			event.distance *= gravity / AstronomyUtil.STANDARD_GRAVITY;
		}

		if(e instanceof EntityPlayer && ((EntityPlayer)e).inventory.armorInventory[2] != null && ((EntityPlayer)e).inventory.armorInventory[2].getItem() instanceof ArmorFSB)
			((ArmorFSB)((EntityPlayer)e).inventory.armorInventory[2].getItem()).handleFall((EntityPlayer)e, event.distance);
	}

	//this exists!?
	@SubscribeEvent
	public void onUseHoe(UseHoeEvent event) {
		World world = event.world;
		int x = event.x;
		int y = event.y;
		int z = event.z;

		Block block = world.getBlock(x, y, z);

		if(block == ModBlocks.rubber_grass || block == ModBlocks.rubber_silt) {
			world.setBlock(x, y, z, ModBlocks.rubber_farmland);
			event.current.damageItem(1, event.entityPlayer);
			event.setResult(Result.ALLOW);
		}
	}


	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {

		EntityPlayer player = event.player;
		if(player.posY > 300 && player.posY <1000) {
			Vec3 vec = Vec3.createVectorHelper(3 * rand.nextDouble(), 0, 0);
			CBT_Atmosphere thatmosphere = CelestialBody.getTrait(player.worldObj, CBT_Atmosphere.class);

			if(thatmosphere != null && thatmosphere.getPressure() > 0.05 && !player.isRiding()) {
				if(Math.abs(player.motionX) > 1 || Math.abs(player.motionY) > 1 || Math.abs(player.motionZ) > 1) {
					ParticleUtil.spawnGasFlame(player.worldObj, player.posX - 1 + vec.xCoord, player.posY + vec.yCoord, player.posZ + vec.zCoord, 0, 0, 0);
				}
			}
		}

		if(player.isPotionActive(HbmPotion.slippery.id) && !player.capabilities.isFlying) {
			if (player.onGround) {
				double slipperiness = 0.6;
				double inertia = 0.1;
				boolean isMoving = player.moveForward != 0.0 || player.moveStrafing != 0.0;
				// double playerMotion = Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);

				double angle = Math.atan2(player.motionZ, player.motionX);

				double targetXMotion = Math.cos(angle) * slipperiness;
				double targetZMotion = Math.sin(angle) * slipperiness;

				double diffX = targetXMotion - player.motionX;
				double diffZ = targetZMotion - player.motionZ;

				player.motionX += diffX * inertia; //god weeps
				player.motionZ += diffZ * inertia;

				if (!isMoving) {
					player.motionX *= (1.0 - 0.1);

					double totalVelocity = Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
					double smoothingAmount = totalVelocity * 0.02;
					player.motionX -= player.motionX / totalVelocity * smoothingAmount;
					player.motionZ -= player.motionZ / totalVelocity * smoothingAmount;
				}
			}
		}

		if(player.inventory.armorInventory[2] != null && player.inventory.armorInventory[2].getItem() instanceof ArmorFSB)
			((ArmorFSB)player.inventory.armorInventory[2].getItem()).handleTick(event);

		if(player.ticksExisted == 100 || player.ticksExisted == 200)
			CraftingManager.crumple();

		if(event.phase == TickEvent.Phase.START) {
			int x = MathHelper.floor_double(player.posX);
			int y = MathHelper.floor_double(player.posY - player.yOffset - 0.01);
			int z = MathHelper.floor_double(player.posZ);
			Block b = player.worldObj.getBlock(x, y, z);

			if(b instanceof IStepTickReceiver && !player.capabilities.isFlying) {
				IStepTickReceiver step = (IStepTickReceiver) b;
				step.onPlayerStep(player.worldObj, x, y, z, player);
			}
		}

		if(!player.worldObj.isRemote && event.phase == TickEvent.Phase.START) {
			// Check for players attempting to cross over to another orbital grid
			if(player.worldObj.provider instanceof WorldProviderOrbit && !(player.ridingEntity instanceof EntityRideableRocket)) {
				double rx = Math.abs(player.posX) % OrbitalStation.STATION_SIZE;
				double rz = Math.abs(player.posZ) % OrbitalStation.STATION_SIZE;

				int minBuffer = OrbitalStation.BUFFER_SIZE;
				int maxBuffer = OrbitalStation.STATION_SIZE - minBuffer;

				int minWarning = OrbitalStation.BUFFER_SIZE + OrbitalStation.WARNING_SIZE;
				int maxWarning = OrbitalStation.STATION_SIZE - minWarning;

				if(player instanceof EntityPlayerMP && (rx < minWarning || rx > maxWarning || rz < minWarning || rz > maxWarning)) {
					PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(ChatBuilder.start("").nextTranslation("info.orbitfall").color(EnumChatFormatting.RED).flush(), ServerProxy.ID_GAS_HAZARD, 3000), (EntityPlayerMP) player);
				}

				if(rx < minBuffer || rx > maxBuffer || rz < minBuffer || rz > maxBuffer) {
					OrbitalStation station = OrbitalStation.getStationFromPosition((int)player.posX, (int)player.posZ);
					CelestialTeleporter.teleport(player, station.orbiting.dimensionId, rand.nextInt(SpaceConfig.maxProbeDistance * 2) - SpaceConfig.maxProbeDistance, 800, rand.nextInt(SpaceConfig.maxProbeDistance * 2) - SpaceConfig.maxProbeDistance, false);
				}
			}

			// keep Nether teleports localized
			// this effectively turns the Nether into a shared pocket dimension, but disallows using it to travel between celestial bodies
			if(player.inPortal) {
				MinecraftServer minecraftserver = ((WorldServer)player.worldObj).func_73046_m();
				int maxTime = player.getMaxInPortalTime();
				if(minecraftserver.getAllowNether() && player.ridingEntity == null && player.portalCounter + 1 >= maxTime) {
					player.portalCounter = maxTime;
					player.timeUntilPortal = player.getPortalCooldown();

					HbmPlayerProps props = HbmPlayerProps.getData(player);
					int targetDimension = -1;
					if(player.worldObj.provider.dimensionId == -1) {
						targetDimension = props.lastDimension;
					} else {
						props.lastDimension = player.worldObj.provider.dimensionId;
					}

					player.travelToDimension(targetDimension);
					player.inPortal = false;
				}
			}

			/// GHOST FIX START ///

			if(!Float.isFinite(player.getHealth()) || !Float.isFinite(player.getAbsorptionAmount())) {
				player.addChatComponentMessage(new ChatComponentText("Your health has been restored!"));
				player.worldObj.playSoundAtEntity(player, "hbm:item.syringe", 1.0F, 1.0F);
				player.setHealth(player.getMaxHealth());
				player.setAbsorptionAmount(0);
			}

			/// GHOST FIX END ///

			/// BETA HEALTH START ///
			if(player.inventory.hasItem(ModItems.beta)) {

				if(player.getFoodStats().getFoodLevel() > 10) {
					player.heal(player.getFoodStats().getFoodLevel() - 10);
				}

				if(player.getFoodStats().getFoodLevel() != 10) {

					// Why can't you be normal??
					try {
						Field food = ReflectionHelper.findField(FoodStats.class, "field_75127_a", "foodLevel");
						food.setInt(player.getFoodStats(), 10);
					} catch(Exception e) { }
				}
			}
			/// BETA HEALTH END ///

			for(int i = 0; i < player.inventory.mainInventory.length; i++) {
				ItemStack stack2 = player.inventory.getStackInSlot(i);

				//oh yeah remind me...
				if(stack2 != null) {
					if(stack2.hasTagCompound() && HazardSystem.getHazardLevelFromStack(stack2, HazardRegistry.RADIATION) == 0) {
						float activation = stack2.stackTagCompound.getFloat(HazardTypeNeutron.NEUTRON_KEY);
						ContaminationUtil.contaminate(player, HazardType.RADIATION, ContaminationType.CREATIVE, activation / 20);
					}
				}
			}
			/// NEW ITEM SYS START ///
			HazardSystem.updatePlayerInventory(player);
			/// NEW ITEM SYS END ///

			/// SYNC START ///
			if(!player.worldObj.isRemote && player instanceof EntityPlayerMP) PacketDispatcher.wrapper.sendTo(new PermaSyncPacket((EntityPlayerMP) player), (EntityPlayerMP) player);
			/// SYNC END ///
		}

		if(player.worldObj.isRemote && event.phase == event.phase.START && !player.isInvisible() && !player.isSneaking()) {

		}

		/*if(!player.worldObj.isRemote && event.phase == TickEvent.Phase.END && player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemGunBaseNT && player instanceof EntityPlayerMP) {
			HeldItemNBTPacket packet = new HeldItemNBTPacket(player.getHeldItem());
			PacketDispatcher.wrapper.sendTo(packet, (EntityPlayerMP) player);
		}*/
	}

	@SubscribeEvent
	public void preventOrganicSpawn(DecorateBiomeEvent.Decorate event) {
		// In space, no one can hear you shroom
		if(!(event.world.provider instanceof WorldProviderCelestial)) return;

		WorldProviderCelestial celestial = (WorldProviderCelestial) event.world.provider;
		if(celestial.hasLife()) return; // Except on Laythe

		switch(event.type) {
		case BIG_SHROOM:
		case CACTUS:
		case DEAD_BUSH:
		case LILYPAD:
		case FLOWERS:
		case GRASS:
		case PUMPKIN:
		case REED:
		case SHROOM:
		case TREE:
			event.setResult(Result.DENY);
		default:
		}
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {

		if(event.phase == Phase.START) {
			CBT_Weather.updateGlobalWeather();
			for(CelestialBody body : CelestialBody.getAllBodies()) {
				List<CelestialBodyTrait> traits = new ArrayList<>(body.getTraits().values());
				for(CelestialBodyTrait trait : traits) {
					trait.update(false, body);
				}
			}

			// do other shit I guess?
			RTTYSystem.updateBroadcastQueue();
			// Logistics drone network
			RequestNetwork.updateEntries();
			// Radar entry handling
			TileEntityMachineRadarNT.updateSystem();
			// Networks! All of them!
			UniNodespace.updateNodespace();
			// Dyson Swarms
			CelestialBody.updateSwarms();
		}


		// There is an issue here somewhere...
		// I cannot, for the life of me, figure out why a single certain bug happens.
		// Every 20-30 or so ticks, players will receive wrong/outdated/weird information in packets
		// I have tried everything to see if I can get this to stop, but it just doesn't seem to work.

		// ^ Update ^ - I figured it out, when the packets were being made for some machines they were being created inside the thread,
		// meaning sometimes the machine would change data *after* the packet was supposed to be sent, meaning incorrect data was being sent.
		// This has since been fixed.

		if(event.phase == Phase.END) {
			// As ByteBufs are added to the queue in `com.hbm.packet.toclient.PacketThreading`, they are processed by the packet thread.
			// This waits until the thread is finished, which most of the time will be instantly since it has plenty of time to process in parallel to everything else.
			PacketThreading.waitUntilThreadFinished();

			NetworkHandler.flush(); // Flush ALL network packets.
		}
	}

	@SubscribeEvent
	public void commandEvent(CommandEvent event) {
		ICommand command = event.command;
		ICommandSender sender = event.sender;
		if(command instanceof CommandWeather) {
			World world = sender.getEntityWorld();
			if(world != null && (world.provider instanceof WorldProviderCelestial || world.provider instanceof WorldProviderOrbit)) {
				handlePlanetaryWeatherCommand(sender, event.parameters, command);
				event.setCanceled(true);
			}
		} else if(command instanceof CommandGameRule) {
			if(command.canCommandSenderUseCommand(sender)) {
				command.processCommand(sender,event.parameters);
				RBMKDials.refresh(sender.getEntityWorld()); // Refresh RBMK gamerules.
				event.setCanceled(true);
			}
		}
	}

	private void handlePlanetaryWeatherCommand(ICommandSender sender, String[] parameters, ICommand command) {
		if(!command.canCommandSenderUseCommand(sender)) {
			return;
		}

		if(parameters.length < 1 || parameters.length > 2) {
			throw new WrongUsageException("commands.weather.usage", new Object[0]);
		}

		int duration = (300 + new Random().nextInt(600)) * 20;
		if(parameters.length >= 2) {
			duration = CommandBase.parseIntBounded(sender, parameters[1], 1, 1000000) * 20;
		}

		World world = sender.getEntityWorld();
		ChunkCoordinates pos = sender.getPlayerCoordinates();
		CelestialBody body = CelestialBody.getTarget(world, pos.posX, pos.posZ).body;
		CBT_Weather weather = CBT_Weather.ensureTrait(body);
		if(weather == null || !CBT_Weather.supportsWeather(body)) {
			throw new CommandException("This celestial body has no weather cycle.");
		}

		if("clear".equalsIgnoreCase(parameters[0])) {
			weather.forceClear(world.rand, duration);
			CommandBase.func_152373_a(sender, command, "commands.weather.clear", new Object[0]);
		} else if("rain".equalsIgnoreCase(parameters[0])) {
			weather.forceRain(world.rand, duration);
			CommandBase.func_152373_a(sender, command, "commands.weather.rain", new Object[0]);
		} else if("thunder".equalsIgnoreCase(parameters[0])) {
			weather.forceThunder(duration);
			CommandBase.func_152373_a(sender, command, "commands.weather.thunder", new Object[0]);
		} else {
			throw new WrongUsageException("commands.weather.usage", new Object[0]);
		}

		SolarSystemWorldSavedData.get(world).markDirty();
	}

	@SubscribeEvent
	public void enteringChunk(EnteringChunk evt) {

		/*if(evt.entity instanceof EntityMissileBaseNT) {
			((EntityMissileBaseNT) evt.entity).loadNeighboringChunks(evt.newChunkX, evt.newChunkZ);
		}

		if(evt.entity instanceof EntityMissileCustom) {
			((EntityMissileCustom) evt.entity).loadNeighboringChunks(evt.newChunkX, evt.newChunkZ);
		}*/
	}

	@SubscribeEvent
	public void onChunkLoad(ChunkEvent.Load event) {

		//test for automatic in-world block replacement

		/*for(int x = 0; x < 16; x++) for(int y = 0; y < 255; y++) for(int z = 0; z < 16; z++) {
			if(event.getChunk().getBlock(x, y, z) instanceof MachineArcFurnace) {
				event.getChunk().func_150807_a(x, y, z, Blocks.air, 0);
			}
		}*/
	}

	@SubscribeEvent
	public void onPlayerClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {

		ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer();
		HbmPlayerProps.getData(event.original).serialize(buf);
		HbmPlayerProps.getData(event.entityPlayer).deserialize(buf);
		buf.release();
	}

	@SubscribeEvent
	public void itemCrafted(PlayerEvent.ItemCraftedEvent e) {
		AchievementHandler.fire(e.player, e.crafting);
	}

	@SubscribeEvent
	public void itemSmelted(PlayerEvent.ItemSmeltedEvent e) {
		AchievementHandler.fire(e.player, e.smelting);

		if(!e.player.worldObj.isRemote && e.smelting.getItem() == Items.iron_ingot && e.player.getRNG().nextInt(64) == 0) {

			if(!e.player.inventory.addItemStackToInventory(new ItemStack(ModItems.lodestone)))
				e.player.dropPlayerItemWithRandomChoice(new ItemStack(ModItems.lodestone), false);
			else
				e.player.inventoryContainer.detectAndSendChanges();
		}

		if(!e.player.worldObj.isRemote && e.smelting.getItem() == ModItems.ingot_uranium && e.player.getRNG().nextInt(64) == 0) {

			if(!e.player.inventory.addItemStackToInventory(new ItemStack(ModItems.quartz_plutonium)))
				e.player.dropPlayerItemWithRandomChoice(new ItemStack(ModItems.quartz_plutonium), false);
			else
				e.player.inventoryContainer.detectAndSendChanges();
		}
	}

	@SubscribeEvent
	public void onItemPickup(PlayerEvent.ItemPickupEvent event) {
		if(event.pickedUp.getEntityItem().getItem() == Items.slime_ball)
			event.player.triggerAchievement(MainRegistry.achSlimeball);
		if(event.pickedUp.getEntityItem().getItem() == ModItems.egg_balefire)
			event.player.triggerAchievement(MainRegistry.rotConsum);
	}

	@SubscribeEvent
	public void onBlockBreak(BreakEvent event) {

		EntityPlayer player = event.getPlayer();

		if(!(player instanceof EntityPlayerMP))
			return;

		if(event.block == ModBlocks.stone_gneiss && !((EntityPlayerMP) player).func_147099_x().hasAchievementUnlocked(MainRegistry.achStratum)) {
			event.getPlayer().triggerAchievement(MainRegistry.achStratum);
			event.setExpToDrop(500);
		}

		if(event.block == Blocks.coal_ore || event.block == Blocks.coal_block || event.block == ModBlocks.ore_lignite) {

			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {

				int x = event.x + dir.offsetX;
				int y = event.y + dir.offsetY;
				int z = event.z + dir.offsetZ;

				if(event.world.rand.nextInt(2) == 0 && event.world.getBlock(x, y, z) == Blocks.air)
					event.world.setBlock(x, y, z, ModBlocks.gas_coal);
			}
		}

		if(event.y > event.world.provider.getHorizon()) {
			if(event.block.getLightValue() > 10) {
				// Placing blocks onto beams counts as a break, but doesn't reduce light count
				if(!(event.block instanceof BlockBeamBase)) {
					CelestialBody body = CelestialBody.getBody(event.world);
					CBT_Lights lights = body.getTrait(CBT_Lights.class);

					if(lights == null) lights = new CBT_Lights();
					lights.removeLight(event.block, event.x, event.y, event.z);

					body.modifyTraits(lights);
				}

			}
		}


		if(RadiationConfig.enablePollution && RadiationConfig.enableLeadFromBlocks) {
			if(!ArmorRegistry.hasProtection(player, 3, HazardClass.PARTICLE_FINE)) {

				float metal = PollutionHandler.getPollution(player.worldObj, event.x, event.y, event.z, PollutionType.HEAVYMETAL);

				if(metal < 5) return;

				if(metal < 10) {
					player.addPotionEffect(new PotionEffect(HbmPotion.lead.id, 100, 0));
				} else if(metal < 25) {
					player.addPotionEffect(new PotionEffect(HbmPotion.lead.id, 100, 1));
				} else {
					player.addPotionEffect(new PotionEffect(HbmPotion.lead.id, 100, 2));
				}
			}
		}
	}

	@SubscribeEvent
	public void onAnvilRepair(AnvilRepairEvent event) {

		// Anvil renaming no longer increments the repair cost
		// Note: Forge has a bug, the names are wrong. Right is output, output is left, left is right
		if(event.left == null && event.right != null && event.output != null) {
			int oldRepairCost = event.output.getRepairCost();

			if (oldRepairCost > 0) {
				event.right.setRepairCost(oldRepairCost);
			} else if (event.right.hasTagCompound()) {
				NBTTagCompound nbt = event.right.getTagCompound();
				nbt.removeTag("RepairCost");
				if (nbt.hasNoTags()) {
					event.right.setTagCompound(null);
				}
			}
		}
	}

	@SubscribeEvent
	public void onClickBlock(PlayerInteractEvent event) {

		int x = event.x;
		int y = event.y;
		int z = event.z;
		World world = event.world;

		if(GeneralConfig.enable528ExplosiveEnergistics && !world.isRemote && event.action == Action.RIGHT_CLICK_BLOCK) {
			Block b = world.getBlock(x, y, z);
			String name = Block.blockRegistry.getNameForObject(b);
			if(name != null && name.startsWith("appliedenergistics2")) {
				world.func_147480_a(x, y, z, false);
				ExplosionVNT vnt = new ExplosionVNT(world, x + 0.5, y + 0.5, z + 0.5, 5, null);
				vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, 20).setupPiercing(5, 0.2F));
				vnt.setPlayerProcessor(new PlayerProcessorStandard());
				vnt.setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F));
				vnt.explode();
				event.setCanceled(true);
			}
		}

		x = event.x;
		y = event.z;
		z = event.y;

		if(!world.isRemote && event.action == Action.RIGHT_CLICK_BLOCK && world.getTileEntity(x, y, z) instanceof TileEntitySign) {

			TileEntitySign sign = (TileEntitySign)world.getTileEntity(x, y, z);

			String result = SONUtil.smoosh(sign.signText[0], sign.signText[1], sign.signText[2], sign.signText[3]);

			if(SONUtil.hashes.contains(result)) {
				world.func_147480_a(x, y, z, false);
				EntityItem entityitem = new EntityItem(world, x, y, z, new ItemStack(ModItems.bobmazon_hidden));
				entityitem.delayBeforeCanPickup = 1;
				world.spawnEntityInWorld(entityitem);
				MainRegistry.logger.fatal("THE HIDDENCAT HAS BEEN OBTAINED " + " x: " + x + " / "	+ " y: " + + y + " / "+ "z: " + + z + " by " + event.entityPlayer.getDisplayName() + "!");

			}
		}
	}

	// This is really fucky, but ensures we can respawn safely on celestial bodies
	// and prevents beds exploding
	@SubscribeEvent
	public void onTrySleep(PlayerInteractEvent event) {
		if(event.world.isRemote) return;
		if(event.world.provider.dimensionId == 0) return;
		if(!(event.world.provider instanceof WorldProviderCelestial) && !(event.world.provider instanceof WorldProviderOrbit)) return;

		if(event.action == Action.RIGHT_CLICK_BLOCK && event.world.getBlock(event.x, event.y, event.z) instanceof BlockBed) {
			WorldProviderCelestial.attemptingSleep = true;
		}
	}

	// PULL THE LEVER KRONK
	@SubscribeEvent
	public void onPull(PlayerInteractEvent event) {
		int x = event.x;
		int y = event.y;
		int z = event.z;
		World world = event.world;

		if(!world.isRemote && event.action == Action.RIGHT_CLICK_BLOCK && world.getBlock(x, y, z) == Blocks.lever && GeneralConfig.enableExtendedLogging == true) {
			MainRegistry.logger.info("[DET] pulled lever at " + x + " / " + y + " / " + z + " by " + event.entityPlayer.getDisplayName() + "!");
		}
	}



	@SubscribeEvent
	public void chatEvent(ServerChatEvent event) {

		EntityPlayerMP player = event.player;
		String message = event.message;

		//boolean conditions for the illiterate, edition 1
		//bellow you can see the header of an if-block. inside the brackets, there is a boolean statement.
		//that means nothing other than its value totaling either 'true' or 'false'
		//examples: 'true' would just mean true
		//'1 > 3' would equal false
		//'i < 10' would equal true if 'i' is smaller than 10, if equal or greater, it will result in false

		//let's start from the back:

		//this part means that the message's first character has to equal a '!': ----------------------------+
		//                                                                                                   |
		//this is a logical AND operator: ----------------------------------------------------------------+  |
		//                                                                                                |  |
		//this is a reference to a field in                                                               |  |
		//Library.java containing a reference UUID: -----------------------------------------+            |  |
		//                                                                                   |            |  |
		//this will compare said UUID with                                                   |            |  |
		//the string representation of the                                                   |            |  |
		//current player's UUID: -----------+                                                |            |  |
		//                                  |                                                |            |  |
		//another AND operator: ---------+  |                                                |            |  |
		//                               |  |                                                |            |  |
		//this is a reference to a       |  |                                                |            |  |
		//boolean called                 |  |                                                |            |  |
		//'enableDebugMode' which is     |  |                                                |            |  |
		//only set once by the mod's     |  |                                                |            |  |
		//config and is disabled by      |  |                                                |            |  |
		//default. "debug" is not a      |  |                                                |            |  |
		//substring of the message, nor  |  |                                                |            |  |
		//something that can be toggled  |  |                                                |            |  |
		//in any other way except for    |  |                                                |            |  |
		//the config file: |             |  |                                                |            |  |
		//                 V             V  V                                                V            V  V
		if(GeneralConfig.enableDebugMode && message.startsWith("!")) {

			String[] msg = message.split(" ");

			String m = msg[0].substring(1, msg[0].length()).toLowerCase(Locale.US);

			if("gv".equals(m)) {

				int id = 0;
				int size = 1;
				int meta = 0;

				if(msg.length > 1 && NumberUtils.isNumber(msg[1])) {
					id = (int)(double)NumberUtils.createDouble(msg[1]);
				}

				if(msg.length > 2 && NumberUtils.isNumber(msg[2])) {
					size = (int)(double)NumberUtils.createDouble(msg[2]);
				}

				if(msg.length > 3 && NumberUtils.isNumber(msg[3])) {
					meta = (int)(double)NumberUtils.createDouble(msg[3]);
				}

				Item item = Item.getItemById(id);

				if(item != null && size > 0 && meta >= 0) {
					player.inventory.addItemStackToInventory(new ItemStack(item, size, meta));
				}
			}

			player.inventoryContainer.detectAndSendChanges();
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onFoodEaten(PlayerUseItemEvent.Finish event) {

		ItemStack stack = event.item;

		if(stack != null && stack.getItem() instanceof ItemFood) {

			if(stack.hasTagCompound()) {
				if(stack.getTagCompound().getBoolean("ntmCyanide")) for(int i = 0; i < 10; i++) {
					event.entityPlayer.attackEntityFrom(rand.nextBoolean() ? ModDamageSource.euthanizedSelf : ModDamageSource.euthanizedSelf2, 1000);
				}
				if(stack.getTagCompound().getBoolean("ntmRedPill")) for(int i = 0; i < 10; i++) {
					event.entityPlayer.addPotionEffect(new PotionEffect(HbmPotion.death.id, 60 * 60 * 20, 0));
				}
			}
		}
	}

	@SubscribeEvent
	public void setFish(EntityJoinWorldEvent event) {
		if(!(event.entity instanceof EntityFishHook)) return;

		updateFish(event.world);
	}

	private static ArrayList<WeightedRandomFishable> overworldFish;
	private static ArrayList<WeightedRandomFishable> overworldJunk;
	private static ArrayList<WeightedRandomFishable> overworldTreasure;

	// Removes all the existing values from the fishing loot tables and replaces them per dimension
	public static void updateFish(World world) {
		if(overworldFish == null) {
			overworldFish = new ArrayList<>();
			overworldJunk = new ArrayList<>();
			overworldTreasure = new ArrayList<>();

			FishingHooks.removeFish((fishable) -> { overworldFish.add(fishable); return false; });
			FishingHooks.removeJunk((fishable) -> { overworldJunk.add(fishable); return false; });
			FishingHooks.removeTreasure((fishable) -> { overworldTreasure.add(fishable); return false; });
		} else {
			FishingHooks.removeFish((fishable) -> { return false; });
			FishingHooks.removeJunk((fishable) -> { return false; });
			FishingHooks.removeTreasure((fishable) -> { return false; });
		}

		if(world.provider instanceof WorldProviderCelestial && world.provider.dimensionId != 0) {
			WorldProviderCelestial provider = (WorldProviderCelestial) world.provider;
			ArrayList<WeightedRandomFishable> fish = provider.getFish();
			ArrayList<WeightedRandomFishable> junk = provider.getJunk();
			ArrayList<WeightedRandomFishable> treasure = provider.getTreasure();
			if(fish == null) fish = overworldFish;
			if(junk == null) junk = overworldJunk;
			if(treasure == null) treasure = overworldTreasure;
			for(WeightedRandomFishable fishable : fish) FishingHooks.addFish(fishable);
			for(WeightedRandomFishable fishable : junk) FishingHooks.addJunk(fishable);
			for(WeightedRandomFishable fishable : treasure) FishingHooks.addTreasure(fishable);
		} else {
			for(WeightedRandomFishable fishable : overworldFish) FishingHooks.addFish(fishable);
			for(WeightedRandomFishable fishable : overworldJunk) FishingHooks.addJunk(fishable);
			for(WeightedRandomFishable fishable : overworldTreasure) FishingHooks.addTreasure(fishable);
		}
	}

	@SubscribeEvent
	public void filterBrokenEntity(EntityJoinWorldEvent event) {

		Entity entity = event.entity;
		Entity[] parts = entity.getParts();

		if(parts != null) {

			for(int i = 0; i < parts.length; i++) {
				if(parts[i] == null) {
					MainRegistry.logger.error("Prevented spawning of multipart entity " + entity.getClass().getCanonicalName() + " due to parts being null!");
					event.setCanceled(true);
					return;
				}
			}
		}
	}


}
