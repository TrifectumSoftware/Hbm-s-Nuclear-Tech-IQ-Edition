package com.hbm.packet.toclient;

import com.hbm.render.util.NeuralyserFade;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

public class NeuralyserFadePacket implements IMessage {

	public NeuralyserFadePacket() { }

	@Override
	public void fromBytes(ByteBuf buf) { }

	@Override
	public void toBytes(ByteBuf buf) { }

	public static class Handler implements IMessageHandler<NeuralyserFadePacket, IMessage> {

		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(NeuralyserFadePacket m, MessageContext ctx) {
			NeuralyserFade.start();
			return null;
		}
	}
}
