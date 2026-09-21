package com.hbm.packet.toclient;

import com.hbm.render.util.NeuralyserFade;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.WorldSettings;


public class PlayerNBTNeuralyserPacket implements IMessage {

	private PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());

	public PlayerNBTNeuralyserPacket() { }

	public PlayerNBTNeuralyserPacket(NBTTagCompound tag) {
		try {
			buffer.writeNBTTagCompoundToBuffer(tag);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		if(buffer == null) buffer = new PacketBuffer(Unpooled.buffer());
		buffer.writeBytes(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		if(buffer == null) buffer = new PacketBuffer(Unpooled.buffer());
		buf.writeBytes(buffer);
	}


	// small rant but i am afraid this might ?balloon to some large nbt sizes later, so this is temporary
	// what even is the package size limit?
	public static class Handler implements IMessageHandler<PlayerNBTNeuralyserPacket, IMessage> {

		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(PlayerNBTNeuralyserPacket m, MessageContext ctx) {
			try {
				NBTTagCompound tag = m.buffer.readNBTTagCompoundFromBuffer();
				if(tag == null) return null;

				EntityPlayer player = Minecraft.getMinecraft().thePlayer;
				if(player == null) return null;

				player.readFromNBT(tag);
				if(player.isEntityAlive()) player.deathTime = 0;

				if(tag.hasKey("playerGameType")) {
					Minecraft.getMinecraft().playerController.setGameType(WorldSettings.GameType.getByID(tag.getInteger("playerGameType")));
				}
				NeuralyserFade.finish();
			} catch(Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}
