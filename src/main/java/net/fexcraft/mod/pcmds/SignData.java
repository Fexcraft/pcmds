package net.fexcraft.mod.pcmds;

import java.util.ArrayList;
import java.util.HashMap;

import net.fexcraft.lib.mc.capabilities.sign.SignCapability;
import net.fexcraft.lib.mc.utils.Static;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/**
 * 
 * @author Ferdinand Calo' (FEX___96)
 *
 */
public class SignData {
	
	public String[] text;
	public Type type = Type.BASIC;
	public HashMap<String, ArrayList<String>> events = new HashMap<>();
	public HashMap<String, Object> settings = new HashMap<>();
	public long price;
	
	public SignData(int length){
		text = new String[length];
	}

	public SignData load(NBTTagCompound com){
		
		return this;
	}
	
	public NBTTagCompound save(NBTTagCompound com){
		
		return com;
	}

	public void process(SignCapability cap, PlayerInteractEvent event, IBlockState state, TileEntitySign tile){
		//
		Static.getServer().commandManager.executeCommand(new CommandSender(event.getWorld(), event.getEntityPlayer()), "say test");
	}

	public boolean valid(){
		//
		return true;
	}
	
	public static enum Type {
		
		BASIC("interact"), RENT("start", "end");
		
		public String[] cmd_events;
	
		Type(String... cmds){
			this.cmd_events = cmds;
		}
		
	}

	public boolean notext(){
		if(text == null || text.length == 0) return true;
		for(String str : text) if(str != null && str.length() > 0) return false;
		return true;
	}

}
