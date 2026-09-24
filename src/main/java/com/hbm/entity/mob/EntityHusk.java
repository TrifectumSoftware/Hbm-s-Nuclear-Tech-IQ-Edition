package com.hbm.entity.mob;

import java.util.UUID;

import com.hbm.extprop.HbmBloodstreamProps;
import com.hbm.handler.blood.BloodBehavior;
import com.hbm.handler.husk.HuskAutomation;
import com.hbm.handler.husk.HuskInventory;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemHumanPart;
import com.hbm.items.special.ItemHumanPart.EnumBodyStat;
import com.hbm.items.special.ItemHumanPart.EnumPartTrait;
import com.hbm.items.special.ItemRoboticHead;
import com.hbm.items.tool.ItemNeuralyser;
import com.hbm.main.NTMSounds;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.NeuralyserFadePacket;
import com.hbm.util.i18n.I18nUtil;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

public class EntityHusk extends EntityLiving {

		private static final int DW_OWNER_NAME = 24;
	private static final int DW_OWNER_UUID = 25;
	private static final int DW_HELD = 26;
	private static final int DW_BOOTS = 27;
	private static final int DW_LEGGINGS = 28;
	private static final int DW_CHEST = 29;
	private static final int DW_HELMET = 30;
	private static final int DW_BLOOD = 31;

	private NBTTagCompound playerData = new NBTTagCompound();
	private HuskInventory inventory;
	private String ownerName = "";
	private String ownerUUID = "";
	private boolean adopted = false;

	private final HuskAutomation automation = new HuskAutomation(this);

	private static final int TRANSFER_DELAY = 26;
	private int transferDelay = 0;
	private String transferPlayer = "";
	private boolean transferOnDeath = false;

	public EntityHusk(World world) {
		super(world);
		this.setSize(0.6F, 1.8F);
		this.experienceValue = 0;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataWatcher().addObject(DW_OWNER_NAME, "");
		this.getDataWatcher().addObject(DW_OWNER_UUID, "");
		this.getDataWatcher().addObject(DW_BLOOD, 0);
		this.getDataWatcher().addObjectByDataType(DW_HELD, 5);
		this.getDataWatcher().addObjectByDataType(DW_BOOTS, 5);
		this.getDataWatcher().addObjectByDataType(DW_LEGGINGS, 5);
		this.getDataWatcher().addObjectByDataType(DW_CHEST, 5);
		this.getDataWatcher().addObjectByDataType(DW_HELMET, 5);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20.0D);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(!this.worldObj.isRemote && this.ticksExisted % 20 == 0) {
			int blood = HbmBloodstreamProps.getData(this).getBloodType().getID();
			if(this.getDataWatcher().getWatchableObjectInt(DW_BLOOD) != blood) this.getDataWatcher().updateObject(DW_BLOOD, blood);
		}

		if(!this.worldObj.isRemote && !this.getRobotChannel().isEmpty()) this.automation.update();

		if(!this.worldObj.isRemote && !this.adopted) {
			EntityPlayer player = this.worldObj.getClosestPlayer(this.posX, this.posY, this.posZ, 16.0D);
			if(player != null) {
				this.adopt(player);
			}
		}

		if(!this.worldObj.isRemote && this.transferDelay > 0) {
			this.transferDelay--;
			if(this.transferDelay == 0) {
				EntityPlayer player = this.worldObj.getPlayerEntityByName(this.transferPlayer);
				this.transferPlayer = "";
				boolean onDeath = this.transferOnDeath;
				this.transferOnDeath = false;
				if(player != null && this.adopted) {
					if(onDeath) {
						ItemNeuralyser.transferOnDeath(player, this);
					} else {
						ItemNeuralyser.transfer(player, this);
					}
				}
			}
		}
	}

	public void beginTransfer(EntityPlayer player) {
		this.beginTransfer(player, false);
	}

	public void beginDeathTransfer(EntityPlayer player) {
		this.beginTransfer(player, true);
	}

	private void beginTransfer(EntityPlayer player, boolean onDeath) {
		if(this.transferDelay > 0) return;

		this.transferDelay = TRANSFER_DELAY;
		this.transferPlayer = player.getCommandSenderName();
		this.transferOnDeath = onDeath;
		if(player instanceof EntityPlayerMP) {
			PacketDispatcher.wrapper.sendTo(new NeuralyserFadePacket(), (EntityPlayerMP) player);
		}
	}

	protected void adopt(EntityPlayer player) {
		NBTTagCompound tag = new NBTTagCompound();
		player.writeToNBT(tag);
		this.setPlayerData(tag);

		this.ownerName = player.getCommandSenderName();
		this.ownerUUID = player.getUniqueID().toString();
		this.getDataWatcher().updateObject(DW_OWNER_NAME, this.ownerName);
		this.getDataWatcher().updateObject(DW_OWNER_UUID, this.ownerUUID);
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(player.getMaxHealth());
		this.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));
		this.adopted = true;
	}

	public void setupClone(String uuid, String name, ItemStack[] parts, FluidType blood) {
		this.ownerName = name == null ? "" : name;
		this.ownerUUID = uuid == null ? "" : uuid;
		this.getDataWatcher().updateObject(DW_OWNER_NAME, this.ownerName);
		this.getDataWatcher().updateObject(DW_OWNER_UUID, this.ownerUUID);
		this.getDataWatcher().updateObject(DW_BLOOD, blood == null ? 0 : blood.getID());

		int[] totals = new int[EnumBodyStat.values().length];
		StringBuilder traits = new StringBuilder();
		String robotChannel = "";
		String robotOutput = "";

		if(parts != null) for(ItemStack part : parts) {
			for(EnumBodyStat stat : EnumBodyStat.values()) totals[stat.ordinal()] += ItemHumanPart.getStat(part, stat);

			EnumPartTrait trait = ItemHumanPart.getTrait(part);
			if(trait != null) {
				if(traits.length() > 0) traits.append(",");
				traits.append(trait.name());
			}

			if(part != null && part.getItem() == ModItems.robotic_head) {
				robotChannel = ItemRoboticHead.getChannel(part);
				robotOutput = ItemRoboticHead.getOutputChannel(part);
			}
		}

		BloodBehavior behavior = BloodBehavior.get(blood);
		if(behavior != null) behavior.applyStats(totals);

		HbmBloodstreamProps.getData(this).setBloodType(blood);

		NBTTagCompound data = new NBTTagCompound();
		data.setTag("Inventory", new NBTTagList());
		data.setTag("Attributes", bodyAttributes(totals));
		data.setIntArray("huskStats", totals);
		data.setString("huskTraits", traits.toString());
		data.setString("robotChannel", robotChannel);
		data.setString("robotOutput", robotOutput);
		this.setPlayerData(data);

		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(health(totals));
		this.setHealth((float) this.getEntityAttribute(SharedMonsterAttributes.maxHealth).getAttributeValue());

		this.adopted = true;
	}

	private static NBTTagList bodyAttributes(int[] totals) {
		NBTTagList list = new NBTTagList();
		list.appendTag(attribute("generic.maxHealth", health(totals)));
		list.appendTag(attribute("generic.attackDamage", totals[EnumBodyStat.STRENGTH.ordinal()] / 100D + 1D));
		list.appendTag(attribute("generic.movementSpeed", totals[EnumBodyStat.AGILITY.ordinal()] / 20000D + 0.1D));
		list.appendTag(attribute("generic.knockbackResistance", totals[EnumBodyStat.TOUGHNESS.ordinal()] / 1000D));
		return list;
	}

	private static double health(int[] totals) {
		return totals[EnumBodyStat.VITALITY.ordinal()] / 20D + 20D;
	}

	private static NBTTagCompound attribute(String name, double base) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("Name", name);
		tag.setDouble("Base", base);
		return tag;
	}

	public void setSkinOwner(String name, String uuid) {
		this.ownerName = name == null ? "" : name;
		this.ownerUUID = uuid == null ? "" : uuid;
		this.getDataWatcher().updateObject(DW_OWNER_NAME, this.ownerName);
		this.getDataWatcher().updateObject(DW_OWNER_UUID, this.ownerUUID);
	}

	public void setPlayerData(NBTTagCompound tag) {
		this.playerData = (tag == null ? new NBTTagCompound() : tag);
		this.getInventory().read();
		this.refreshEquipment();
	}

	public void onInventoryChanged() {
		this.refreshEquipment();
	}

	private void refreshEquipment() {
		if(this.worldObj.isRemote) return;

		HuskInventory inv = this.inventory;
		ItemStack boots = inv == null ? null : inv.armorInventory[0];
		ItemStack leggings = inv == null ? null : inv.armorInventory[1];
		ItemStack chest = inv == null ? null : inv.armorInventory[2];
		ItemStack helmet = inv == null ? null : inv.armorInventory[3];
		ItemStack held = inv == null ? null : inv.getStackInSlot(this.getSelectedSlot());

		this.getDataWatcher().updateObject(DW_HELD, held);
		this.getDataWatcher().updateObject(DW_BOOTS, boots);
		this.getDataWatcher().updateObject(DW_LEGGINGS, leggings);
		this.getDataWatcher().updateObject(DW_CHEST, chest);
		this.getDataWatcher().updateObject(DW_HELMET, helmet);
	}

	public NBTTagCompound getPlayerData() {
		return this.playerData;
	}

	public HuskInventory getInventory() {
		if(this.inventory == null) this.inventory = new HuskInventory(this);
		return this.inventory;
	}

	public int[] getBodyStats() {
		return this.playerData.getIntArray("huskStats");
	}

	public String getBodyTraits() {
		return this.playerData.getString("huskTraits");
	}

	public boolean isAdopted() {
		return this.adopted;
	}

	public String getOwnerName() {
		return this.getDataWatcher().getWatchableObjectString(DW_OWNER_NAME);
	}

	public String getOwnerUUID() {
		return this.getDataWatcher().getWatchableObjectString(DW_OWNER_UUID);
	}

	public String getHuskId() {
		UUID uuid = this.getUniqueID();
		return uuid == null ? "" : uuid.toString();
	}

	public int getBloodId() {
		return this.getDataWatcher().getWatchableObjectInt(DW_BLOOD);
	}

	public String getRobotChannel() {
		return this.playerData.getString("robotChannel");
	}

	public int getSelectedSlot() {
		return this.playerData.getInteger("SelectedItemSlot");
	}

	public String getRobotOutputChannel() {
		String output = this.playerData.getString("robotOutput");
		if(!output.isEmpty()) return output;

		String channel = this.getRobotChannel();
		return channel.isEmpty() ? "" : channel + HuskAutomation.OUTPUT_SUFFIX;
	}

	public String getHuskDisplayName() {
		return this.hasCustomNameTag() ? this.getCustomNameTag() : I18nUtil.resolveKey("entity.hbm.entity_husk.name");
	}

	@Override
	public ItemStack getHeldItem() {
		return this.getDataWatcher().getWatchableObjectItemStack(DW_HELD);
	}

	@Override
	public ItemStack getEquipmentInSlot(int slot) {
		switch(slot) {
			case 4: return this.getDataWatcher().getWatchableObjectItemStack(DW_HELMET);
			case 3: return this.getDataWatcher().getWatchableObjectItemStack(DW_CHEST);
			case 2: return this.getDataWatcher().getWatchableObjectItemStack(DW_LEGGINGS);
			case 1: return this.getDataWatcher().getWatchableObjectItemStack(DW_BOOTS);
			default: return this.getDataWatcher().getWatchableObjectItemStack(DW_HELD);
		}
	}

	@Override
	public ItemStack func_130225_q(int slot) {
		switch(slot) {
			case 3: return this.getDataWatcher().getWatchableObjectItemStack(DW_HELMET);
			case 2: return this.getDataWatcher().getWatchableObjectItemStack(DW_CHEST);
			case 1: return this.getDataWatcher().getWatchableObjectItemStack(DW_LEGGINGS);
			case 0: return this.getDataWatcher().getWatchableObjectItemStack(DW_BOOTS);
			default: return null;
		}
	}

	@Override
	public void setCurrentItemOrArmor(int slot, ItemStack stack) {
		switch(slot) {
			case 4: this.getDataWatcher().updateObject(DW_HELMET, stack); break;
			case 3: this.getDataWatcher().updateObject(DW_CHEST, stack); break;
			case 2: this.getDataWatcher().updateObject(DW_LEGGINGS, stack); break;
			case 1: this.getDataWatcher().updateObject(DW_BOOTS, stack); break;
			default: this.getDataWatcher().updateObject(DW_HELD, stack); break;
		}
	}

	@Override
	protected boolean interact(EntityPlayer player) {
		ItemStack held = player.getHeldItem();

		if(held != null && ItemNeuralyser.isNeuralyser(held)) {

			if(player.isSneaking()) {
				if(!this.worldObj.isRemote) {
					ItemNeuralyser.linkTo(held, this);
					this.worldObj.playSoundAtEntity(player, NTMSounds.TECH_BLEEP, 1.0F, 1.0F);
					player.addChatMessage(new ChatComponentTranslation("item.neuralyser.linked"));
				}
				return true;
			}

			if(held.getItem() == ModItems.neuralyser && !this.worldObj.isRemote && this.adopted && ItemNeuralyser.getLinkedHusk(held, this.worldObj, player, ItemNeuralyser.LINK_RANGE) == this) {
				this.beginTransfer(player);
				return true;
			}
		}

		return super.interact(player);
	}

	@Override
	public void onDeath(DamageSource source) {
		this.broadcastDeathMessage(source);
		this.dropStoredInventory();
		super.onDeath(source);
	}

	private void broadcastDeathMessage(DamageSource source) {
		if(this.worldObj.isRemote) return;

		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if(server == null) return;

		String name = this.getOwnerName();
		if(name == null || name.isEmpty()) {
			name = this.hasCustomNameTag() ? this.getCustomNameTag() : this.getCommandSenderName();
		}

		Entity attacker = source.getEntity();
		IChatComponent msg;
            // this was truly the only way to do this
		if(source == DamageSource.outOfWorld) {
			msg = new ChatComponentTranslation("death.attack.outOfWorld", name);
		} else if(attacker instanceof EntityPlayer) {
			msg = new ChatComponentTranslation("death.attack.player", name, attacker.getCommandSenderName());
		} else if(attacker != null) {
			msg = new ChatComponentTranslation("death.attack.mob", name, attacker.getCommandSenderName());
		} else if(source.isExplosion()) {
			msg = new ChatComponentTranslation("death.attack.explosion", name);
		} else if(source.isFireDamage()) {
			msg = new ChatComponentTranslation("death.attack.onFire", name);
		} else if(source == DamageSource.inWall) {
			msg = new ChatComponentTranslation("death.attack.inWall", name);
		} else if(source == DamageSource.drown) {
			msg = new ChatComponentTranslation("death.attack.drown", name);
		} else if(source == DamageSource.starve) {
			msg = new ChatComponentTranslation("death.attack.starve", name);
		} else if(source == DamageSource.magic) {
			msg = new ChatComponentTranslation("death.attack.magic", name);
		} else {
			msg = new ChatComponentTranslation("death.attack.generic", name);
		}

		server.getConfigurationManager().sendChatMsg(msg);
	}

	public void dropStoredInventory() {
		if(this.worldObj.isRemote) return;

		NBTTagList inv = this.playerData.getTagList("Inventory", 10);

		for(int i = 0; i < inv.tagCount(); i++) {
			ItemStack stack = ItemStack.loadItemStackFromNBT(inv.getCompoundTagAt(i));
			if(stack != null && stack.stackSize > 0) {
				this.entityDropItem(stack, 0.0F);
			}
		}

		this.playerData.removeTag("Inventory");
	}

	@Override
	protected boolean isAIEnabled() {
		return false;
	}

	@Override
	protected boolean canDespawn() {
		return false;
	}

	@Override
	protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) { }

	@Override
	public void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		tag.setTag("huskData", this.playerData);
		tag.setString("huskOwner", this.ownerName);
		tag.setString("huskOwnerUUID", this.ownerUUID);
		tag.setBoolean("huskAdopted", this.adopted);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);
		this.playerData = tag.getCompoundTag("huskData");
		this.ownerName = tag.getString("huskOwner");
		this.ownerUUID = tag.getString("huskOwnerUUID");
		this.adopted = tag.getBoolean("huskAdopted");

		if(!this.worldObj.isRemote) {
			this.getDataWatcher().updateObject(DW_OWNER_NAME, this.ownerName);
			this.getDataWatcher().updateObject(DW_OWNER_UUID, this.ownerUUID);
			this.setPlayerData(this.playerData);
		}
	}
}
