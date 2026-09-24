package com.hbm.handler.husk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.hbm.entity.mob.EntityHusk;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.mags.IMagazine;
import com.hbm.tileentity.network.RTTYSystem;
import com.hbm.tileentity.network.RTTYSystem.RTTYChannel;

import api.hbm.redstoneoverradio.IRORInfo;
import api.hbm.redstoneoverradio.IRORInteractive;
import api.hbm.redstoneoverradio.IRORValueProvider;
import api.hbm.redstoneoverradio.RORFunctionException;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class HuskAutomation implements IRORInteractive, IRORValueProvider {

	public static final String OUTPUT_SUFFIX = ".out";
	public static final int SAY_COOLDOWN = 200;
	public static final double DEFAULT_REACH = 3D;
	public static final double DEFAULT_SURVEY = 10D;
	public static final int REPORT_LIMIT = 8;

	private final HuskBody body;
	private final HuskMovement movement;
	private final HuskActuators actuators;
	private long lastStamp;
	private int sayCooldown;

	public HuskAutomation(EntityLivingBase actor) {
		this.body = new HuskBody(actor);
		this.movement = new HuskMovement(actor);
		this.actuators = new HuskActuators(this.body);
	}

	public static String getChannel(EntityLivingBase actor) {
		if(actor instanceof EntityHusk) return ((EntityHusk) actor).getRobotChannel();
		if(actor instanceof EntityPlayer) return HbmPlayerProps.getData((EntityPlayer) actor).bodyRobot;
		return "";
	}

	public static String getOutputChannel(EntityLivingBase actor) {
		if(actor instanceof EntityHusk) return ((EntityHusk) actor).getRobotOutputChannel();
		if(actor instanceof EntityPlayer) return HbmPlayerProps.getData((EntityPlayer) actor).bodyRobotOut;
		return "";
	}

	public void update() {
		if(this.sayCooldown > 0) this.sayCooldown--;

		this.movement.update();

		try {
			this.body.tickHeld();
		} catch(Throwable ex) {
			this.broadcast("held tick failed: " + ex);
		}

		String channel = getChannel(this.body.actor);
		if(channel.isEmpty()) return;

		RTTYChannel chan = RTTYSystem.listen(this.body.actor.worldObj, channel);
		if(chan == null) return;
		if(chan.timeStamp < this.body.actor.worldObj.getTotalWorldTime() - 1) return;
		if(chan.timeStamp <= this.lastStamp) return;
		this.lastStamp = chan.timeStamp;

		String signal = "" + chan.signal;

		try {
			String result = this.runRORFunction(IRORInteractive.getCommand(signal), IRORInteractive.getParams(signal));
			if(result != null && !result.isEmpty()) this.broadcast(result);
		} catch(RORFunctionException ex) {
		} catch(Throwable ex) {
			this.broadcast("error: " + ex);
		}

		if(this.body.wasUsed()) {
			this.body.persist();
			this.body.clearUsed();
		}
	}

	@Override
	public String runRORFunction(String name, String[] params) {
		if(name == null || name.isEmpty()) return null;
		if(name.startsWith(IRORInfo.PREFIX_FUNCTION)) name = name.substring(IRORInfo.PREFIX_FUNCTION.length());
		name = name.toLowerCase(Locale.US);

		if("say".equals(name)) return this.say(params);
		if("move".equals(name)) return this.movement.move(params);
		if("look".equals(name)) return this.movement.look(params);
		if("jump".equals(name)) return this.movement.jump(params);
		if("follow".equals(name)) return this.movement.follow(params);
		if("stop".equals(name)) { this.movement.stop(); return "ok"; }
		if("scan".equals(name)) return this.scan(params);
		if("around".equals(name)) return this.around(params);
		if("entities".equals(name)) return this.entities(params);
		if("inv".equals(name)) return this.inv();
		if("hold".equals(name)) return this.actuators.hold(params);
		if("pickup".equals(name)) return this.actuators.pickup(params);
		if("drop".equals(name)) return this.actuators.drop(params);
		if("dropall".equals(name)) return this.actuators.dropAll(params);
		if("interact".equals(name)) return this.actuators.interact(params);
		if("attack".equals(name)) return this.actuators.attack(params);
		if("hit".equals(name)) return this.actuators.hit(params);
		if("key".equals(name)) return this.actuators.key(params);
		if("val".equals(name)) return params != null && params.length > 0 ? this.provideRORValue(params[0]) : "";

		return null;
	}

	private String say(String[] params) {
		if(this.sayCooldown > 0 || params == null || params.length == 0) return null;

		StringBuilder message = new StringBuilder();
		for(int i = 0; i < params.length; i++) {
			if(i > 0) message.append(":");
			message.append(params[i]);
		}

		MinecraftServer server = MinecraftServer.getServer();
		if(server != null) server.getConfigurationManager().sendChatMsg(new ChatComponentTranslation("chat.type.text", this.speakerName(), message.toString()));

		this.sayCooldown = SAY_COOLDOWN;
		return null;
	}

	private String speakerName() {
		if(this.body.actor instanceof EntityLiving) {
			String tag = ((EntityLiving) this.body.actor).getCustomNameTag();
			if(tag != null && !tag.isEmpty()) return tag;
		}

		if(this.body.actor instanceof EntityHusk) {
			String owner = ((EntityHusk) this.body.actor).getOwnerName();
			if(owner != null && !owner.isEmpty()) return owner;
		}

		return this.body.actor.getCommandSenderName();
	}

	private String scan(String[] params) {
		double reach = DEFAULT_REACH;
		if(params != null && params.length > 0) {
			try { reach = Double.parseDouble(params[0]); } catch(NumberFormatException ex) { }
		}
		if(reach <= 0D || reach > 32D) reach = DEFAULT_REACH;

		int[] pos = this.scanPos(reach);
		World world = this.body.actor.worldObj;

		return Block.blockRegistry.getNameForObject(world.getBlock(pos[0], pos[1], pos[2])) + ":" + world.getBlockMetadata(pos[0], pos[1], pos[2]) + ":" + pos[0] + ":" + pos[1] + ":" + pos[2];
	}

	private int[] scanPos(double reach) {
		Vec3 look = this.body.actor.getLookVec();
		return new int[] {
				MathHelper.floor_double(this.body.actor.posX + look.xCoord * reach),
				MathHelper.floor_double(this.body.actor.posY + this.body.actor.getEyeHeight() + look.yCoord * reach),
				MathHelper.floor_double(this.body.actor.posZ + look.zCoord * reach)
		};
	}

	private String around(String[] params) {
		int radius = 2;
		if(params != null && params.length > 0) {
			try { radius = Integer.parseInt(params[0]); } catch(NumberFormatException ex) { }
		}
		if(radius < 1 || radius > 8) radius = 2;

		World world = this.body.actor.worldObj;
		int ox = MathHelper.floor_double(this.body.actor.posX);
		int oy = MathHelper.floor_double(this.body.actor.posY);
		int oz = MathHelper.floor_double(this.body.actor.posZ);

		Map<String, Integer> counts = new LinkedHashMap<String, Integer>();

		for(int x = ox - radius; x <= ox + radius; x++) {
			for(int y = oy - radius; y <= oy + radius; y++) {
				for(int z = oz - radius; z <= oz + radius; z++) {
					Block block = world.getBlock(x, y, z);
					if(block.isAir(world, x, y, z)) continue;

					String name = "" + Block.blockRegistry.getNameForObject(block);
					Integer count = counts.get(name);
					counts.put(name, count == null ? 1 : count + 1);
				}
			}
		}

		if(counts.isEmpty()) return "none";

		List<Map.Entry<String, Integer>> entries = new ArrayList<Map.Entry<String, Integer>>(counts.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
			@Override public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) { return b.getValue() - a.getValue(); }
		});

		StringBuilder report = new StringBuilder();
		for(int i = 0; i < entries.size() && i < REPORT_LIMIT; i++) {
			if(report.length() > 0) report.append(",");
			report.append(entries.get(i).getKey()).append("=").append(entries.get(i).getValue());
		}

		return report.toString();
	}

	private String entities(String[] params) {
		double radius = DEFAULT_SURVEY;
		String filter = "all";

		if(params != null && params.length > 0) {
			try { radius = Double.parseDouble(params[0]); } catch(NumberFormatException ex) { }
		}
		if(params != null && params.length > 1) filter = params[1].toLowerCase(Locale.US);
		if(radius <= 0D || radius > 64D) radius = DEFAULT_SURVEY;

		List<Entity> found = new ArrayList<Entity>();

		for(Object o : this.body.actor.worldObj.loadedEntityList) {
			if(!(o instanceof Entity)) continue;
			Entity entity = (Entity) o;
			if(entity == this.body.actor || entity.isDead) continue;

			if("living".equals(filter) && !(entity instanceof EntityLivingBase)) continue;
			if("mob".equals(filter) && (!(entity instanceof EntityLivingBase) || entity instanceof EntityPlayer)) continue;
			if("player".equals(filter) && !(entity instanceof EntityPlayer)) continue;

			if(this.body.actor.getDistanceToEntity(entity) > radius) continue;
			found.add(entity);
		}

		if(found.isEmpty()) return "none";

		final EntityLivingBase self = this.body.actor;
		Collections.sort(found, new Comparator<Entity>() {
			@Override public int compare(Entity a, Entity b) { return Double.compare(self.getDistanceSqToEntity(a), self.getDistanceSqToEntity(b)); }
		});

		StringBuilder report = new StringBuilder();
		for(int i = 0; i < found.size() && i < REPORT_LIMIT * 2; i++) {
			Entity entity = found.get(i);
			String name;
			if(entity instanceof EntityPlayer) {
				name = entity.getCommandSenderName();
			} else {
				name = EntityList.getEntityString(entity);
				if(name == null) name = entity.getClass().getSimpleName();
			}

			if(report.length() > 0) report.append(",");
			report.append(name).append("@").append(MathHelper.floor_double(entity.posX)).append(":").append(MathHelper.floor_double(entity.posY)).append(":").append(MathHelper.floor_double(entity.posZ));
		}

		return report.toString();
	}

	private String inv() {
		IInventory inv = this.body.inventory();
		if(inv == null) return "empty";

		StringBuilder report = new StringBuilder();

		for(int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if(stack == null || stack.stackSize <= 0) continue;
			if(report.length() > 0) report.append(",");
			report.append(i).append("=").append(HuskActuators.itemName(stack)).append("x").append(stack.stackSize);
		}

		return report.length() == 0 ? "empty" : report.toString();
	}

	private void broadcast(String signal) {
		String channel = getOutputChannel(this.body.actor);
		if(channel.isEmpty()) return;
		RTTYSystem.broadcast(this.body.actor.worldObj, channel, signal);
	}

	@Override
	public String[] getFunctionInfo() {
		return new String[] {
				IRORInfo.PREFIX_FUNCTION + "say:message",
				IRORInfo.PREFIX_FUNCTION + "move:direction:blocks",
				IRORInfo.PREFIX_FUNCTION + "move:x:y:z",
				IRORInfo.PREFIX_FUNCTION + "look:direction|yaw:pitch|target",
				IRORInfo.PREFIX_FUNCTION + "jump:height",
				IRORInfo.PREFIX_FUNCTION + "follow:target:distance",
				IRORInfo.PREFIX_FUNCTION + "stop",
				IRORInfo.PREFIX_FUNCTION + "scan:reach",
				IRORInfo.PREFIX_FUNCTION + "around:radius",
				IRORInfo.PREFIX_FUNCTION + "entities:radius:filter",
				IRORInfo.PREFIX_FUNCTION + "inv",
				IRORInfo.PREFIX_FUNCTION + "hold:slot",
				IRORInfo.PREFIX_FUNCTION + "pickup:radius",
				IRORInfo.PREFIX_FUNCTION + "drop:slot:count",
				IRORInfo.PREFIX_FUNCTION + "dropall",
				IRORInfo.PREFIX_FUNCTION + "interact:reach",
				IRORInfo.PREFIX_FUNCTION + "attack:reach",
				IRORInfo.PREFIX_FUNCTION + "hit:reach",
				IRORInfo.PREFIX_FUNCTION + "key:name",
				IRORInfo.PREFIX_FUNCTION + "val:name",
				IRORInfo.PREFIX_VALUE + "pos",
				IRORInfo.PREFIX_VALUE + "facing",
				IRORInfo.PREFIX_VALUE + "block",
				IRORInfo.PREFIX_VALUE + "name",
				IRORInfo.PREFIX_VALUE + "channel",
				IRORInfo.PREFIX_VALUE + "health",
				IRORInfo.PREFIX_VALUE + "held",
				IRORInfo.PREFIX_VALUE + "ammo",
				IRORInfo.PREFIX_VALUE + "follow",
				IRORInfo.PREFIX_VALUE + "keys"
		};
	}

	@Override
	public String provideRORValue(String name) {
		if(name == null) return "";
		name = name.toLowerCase(Locale.US);
		if(name.startsWith(IRORInfo.PREFIX_VALUE)) name = name.substring(IRORInfo.PREFIX_VALUE.length());

		if("pos".equals(name)) return MathHelper.floor_double(this.body.actor.posX) + ":" + MathHelper.floor_double(this.body.actor.posY) + ":" + MathHelper.floor_double(this.body.actor.posZ);
		if("facing".equals(name)) return "" + MathHelper.floor_double(this.body.actor.rotationYaw);
		if("name".equals(name)) return this.speakerName();
		if("channel".equals(name)) return getChannel(this.body.actor);
		if("health".equals(name)) return "" + this.body.actor.getHealth();
		if("follow".equals(name)) return this.movement.followTarget();
		if("held".equals(name)) {
			ItemStack held = this.body.held();
			return held == null ? "empty hand" : HuskActuators.itemName(held) + "x" + held.stackSize;
		}
		if("keys".equals(name)) {
			StringBuilder keys = new StringBuilder();
			for(EnumKeybind keybind : EnumKeybind.values()) {
				if(keys.length() > 0) keys.append(",");
				keys.append(keybind.name());
			}
			return keys.toString();
		}
		if("ammo".equals(name)) {
			ItemStack held = this.body.held();
			if(held == null || !(held.getItem() instanceof ItemGunBaseNT)) return "0/0";

			try {
				IMagazine mag = ((ItemGunBaseNT) held.getItem()).getConfig(held, 0).getReceivers(held)[0].getMagazine(held);
				return mag.getAmount(held, this.body.inventory()) + "/" + mag.getCapacity(held);
			} catch(Throwable ex) {
				return "0/0";
			}
		}
		if("block".equals(name)) {
			int[] pos = this.scanPos(DEFAULT_REACH);
			return Block.blockRegistry.getNameForObject(this.body.actor.worldObj.getBlock(pos[0], pos[1], pos[2])) + ":" + this.body.actor.worldObj.getBlockMetadata(pos[0], pos[1], pos[2]);
		}

		return "";
	}
}
