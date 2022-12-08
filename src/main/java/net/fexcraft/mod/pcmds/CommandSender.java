package net.fexcraft.mod.pcmds;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class CommandSender implements ICommandSender {
	
	private EntityPlayer player;
	private World world;
	
	public CommandSender(World world, EntityPlayer player){
		this.player = player;
		this.world = world;
	}

	@Override
	public String getName(){
		return player == null ? "PCMDS Command Sender" : player.getName();
	}

	@Override
	public boolean canUseCommand(int permLevel, String commandName){
		return true;
	}

	@Override
	public World getEntityWorld(){
		return world;
	}

	@Override
	public MinecraftServer getServer(){
		return FMLCommonHandler.instance().getMinecraftServerInstance();
	}
	
	@Override
	public Entity getCommandSenderEntity(){
		return player;
	}

}
