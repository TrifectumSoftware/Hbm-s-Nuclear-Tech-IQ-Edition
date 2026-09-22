package com.hbm.items.tool;

import java.util.List;

import com.hbm.entity.mob.EntityHusk;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.handler.SymbolHandler;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.main.NTMSounds;
import com.hbm.main.ServerProxy;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PlayerNBTNeuralyserPacket;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemNeuralyser extends Item {

	public static final double LINK_RANGE = 100D;

	public ItemNeuralyser() {
		super();
		this.setMaxStackSize(1);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if(player.isSneaking()) {
			return stack;
		}

		if(!world.isRemote) {
			EntityHusk husk = getLinkedHusk(stack, world, player, LINK_RANGE);
			if(husk != null && husk.isAdopted()) {
				husk.beginTransfer(player);
			} else if(getLinkedUUID(stack) != null) {
				player.addChatMessage(new ChatComponentTranslation("item.neuralyser.no_target"));
			} else {
				player.addChatMessage(new ChatComponentTranslation("item.neuralyser.not_linked"));
			}
		}

		return stack;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		if(getLinkedUUID(stack) != null) {
			list.add(EnumChatFormatting.GRAY + getLinkedName(stack, player.worldObj));
		} else {
			list.add(EnumChatFormatting.DARK_GRAY + I18nUtil.resolveKey("item.neuralyser.hint"));
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean inHand) {
		if(!world.isRemote || !inHand || !(entity instanceof EntityPlayer)) return;

		EntityPlayer player = (EntityPlayer) entity;
		if(getLinkedUUID(stack) == null) return;

		EntityHusk husk = getLinkedHusk(stack, world, player, Double.MAX_VALUE);
		if(husk != null) {
			MainRegistry.proxy.displayTooltip(I18nUtil.resolveKey("item.neuralyser.hud", husk.getHuskDisplayName(), (int) player.getDistanceToEntity(husk)), ServerProxy.ID_NEURALYSER);
		} else {
			MainRegistry.proxy.displayTooltip(I18nUtil.resolveKey("item.neuralyser.hud.lost"), ServerProxy.ID_NEURALYSER);
		}
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float fx, float fy, float fz) {
		if(player.isSneaking()) return false;

		if(!world.isRemote) {
			EntityHusk husk = getLinkedHusk(stack, world, player, LINK_RANGE);
			if(husk != null && husk.isAdopted()) {
				husk.beginTransfer(player);
				return true;
			}
		}

		return false;
	}





      // for any item that can link to a husk. neat innit?
	public static boolean isNeuralyser(ItemStack stack) {
		return stack != null && (stack.getItem() == ModItems.neuralyser || stack.getItem() == ModItems.deadmans_neuralyser);
	}

	public static void linkTo(ItemStack stack, EntityHusk husk) {
		if(stack.stackTagCompound == null) stack.stackTagCompound = new NBTTagCompound();
		stack.stackTagCompound.setString("linkedUUID", husk.getHuskId());
		stack.stackTagCompound.setString("linkedName", husk.hasCustomNameTag() ? husk.getCustomNameTag() : "");
	}

	public static String getLinkedUUID(ItemStack stack) {
		if(stack.stackTagCompound == null || !stack.stackTagCompound.hasKey("linkedUUID")) return null;
		return stack.stackTagCompound.getString("linkedUUID");
	}

	// live naem
	public static String getLinkedName(ItemStack stack, World world) {
		String uuid = getLinkedUUID(stack);
		if(uuid == null) return "";

		EntityHusk husk = findHusk(world, uuid);
		if(husk != null) return husk.getHuskDisplayName();

		String stored = stack.stackTagCompound.getString("linkedName");
		return stored.isEmpty() ? I18nUtil.resolveKey("entity.hbm.entity_husk.name") : stored;
	}

	public static EntityHusk findHusk(World world, String uuid) {
		for(Object o : world.loadedEntityList) {
			if(o instanceof EntityHusk) {
				EntityHusk husk = (EntityHusk) o;
				if(husk.getHuskId().equals(uuid)) return husk;
			}
		}
		return null;
	}

	public static EntityHusk getLinkedHusk(ItemStack stack, World world, EntityPlayer player, double range) {
		String uuid = getLinkedUUID(stack);
		if(uuid == null) return null;

		EntityHusk husk = findHusk(world, uuid);
		if(husk != null && player.getDistanceToEntity(husk) <= range) return husk;
		return null;
	}


   // general method for swapping
	public static void transfer(EntityPlayer player, EntityHusk husk) {
		NBTTagCompound outgoing = new NBTTagCompound();
		player.writeToNBT(outgoing);

		String targetName = husk.getOwnerName();
		String targetUUID = husk.getOwnerUUID();
		HbmPlayerProps props = HbmPlayerProps.getData(player);
		String bodyName = props.huskUUID.isEmpty() ? player.getCommandSenderName() : props.huskName;
		String bodyUUID = props.huskUUID.isEmpty() ? player.getUniqueID().toString() : props.huskUUID;

		double px = player.posX, py = player.posY, pz = player.posZ;
		float pyaw = player.rotationYaw, ppitch = player.rotationPitch;

		double hx = husk.posX, hy = husk.posY, hz = husk.posZ;
		float hyaw = husk.rotationYaw, hpitch = husk.rotationPitch;


		int symbol = SymbolHandler.getActiveSymbolMeta(player);
		NBTTagCompound incoming = (NBTTagCompound) husk.getPlayerData().copy();
		applyLocation(incoming, hx, hy, hz, hyaw, hpitch);
		incoming.setFloat("Health", husk.getHealth());
		player.readFromNBT(incoming);
		if(player.getHealth() <= 0F) player.setHealth(player.getMaxHealth());
		player.fallDistance = 0F;
		SymbolHandler.setActiveSymbol(player, symbol);
		setHuskForm(player, targetName, targetUUID);

		husk.setPlayerData(outgoing);
		husk.setSkinOwner(bodyName, bodyUUID);
		husk.setLocationAndAngles(px, py, pz, pyaw, ppitch);

		player.worldObj.playSoundAtEntity(player, NTMSounds.UNPACK, 1.0F, 1.0F);
		if(player instanceof EntityPlayerMP) {
			EntityPlayerMP mp = (EntityPlayerMP) player;
			mp.inventoryContainer.detectAndSendChanges();
			mp.inventory.markDirty();
			mp.playerNetServerHandler.setPlayerLocation(hx, hy, hz, hyaw, hpitch);
			PacketDispatcher.wrapper.sendTo(new PlayerNBTNeuralyserPacket(incoming), mp);
		}
	}

	public static void transferOnDeath(EntityPlayer player, EntityHusk husk) {
		double hx = husk.posX, hy = husk.posY, hz = husk.posZ;
		float hyaw = husk.rotationYaw, hpitch = husk.rotationPitch;

		int symbol = SymbolHandler.getActiveSymbolMeta(player);
		NBTTagCompound incoming = (NBTTagCompound) husk.getPlayerData().copy();
		applyLocation(incoming, hx, hy, hz, hyaw, hpitch);
		incoming.setFloat("Health", husk.getHealth());
		player.readFromNBT(incoming);
		if(player.getHealth() <= 0F) player.setHealth(player.getMaxHealth());
		player.fallDistance = 0F;
		SymbolHandler.setActiveSymbol(player, symbol);
		setHuskForm(player, husk.getOwnerName(), husk.getOwnerUUID());
		husk.setDead();

		player.worldObj.playSoundAtEntity(player, NTMSounds.UNPACK, 1.0F, 1.0F);

		if(player instanceof EntityPlayerMP) {
			EntityPlayerMP mp = (EntityPlayerMP) player;
			mp.inventoryContainer.detectAndSendChanges();
			mp.inventory.markDirty();
			mp.playerNetServerHandler.setPlayerLocation(hx, hy, hz, hyaw, hpitch);
			PacketDispatcher.wrapper.sendTo(new PlayerNBTNeuralyserPacket(incoming), mp);
		}
	}

	private static void setHuskForm(EntityPlayer player, String name, String uuid) {
		HbmPlayerProps props = HbmPlayerProps.getData(player);
		props.huskName = name == null ? "" : name;
		props.huskUUID = uuid == null ? "" : uuid;
	}

	private static void applyLocation(NBTTagCompound tag, double x, double y, double z, float yaw, float pitch) {
		NBTTagList pos = new NBTTagList();
		pos.appendTag(new NBTTagDouble(x));
		pos.appendTag(new NBTTagDouble(y));
		pos.appendTag(new NBTTagDouble(z));
		tag.setTag("Pos", pos);

		NBTTagList motion = new NBTTagList();
		motion.appendTag(new NBTTagDouble(0.0D));
		motion.appendTag(new NBTTagDouble(0.0D));
		motion.appendTag(new NBTTagDouble(0.0D));
		tag.setTag("Motion", motion);

		NBTTagList rotation = new NBTTagList();
		rotation.appendTag(new NBTTagFloat(yaw));
		rotation.appendTag(new NBTTagFloat(pitch));
		tag.setTag("Rotation", rotation);

		tag.setFloat("FallDistance", 0.0F);
		tag.setBoolean("OnGround", true);
	}
}
