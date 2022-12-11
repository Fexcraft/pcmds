package net.fexcraft.mod.pcmds;

import static net.fexcraft.mod.pcmds.EditCmd.trs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import net.fexcraft.lib.mc.capabilities.sign.SignCapability;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
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
	public HashMap<String, String> settings = new HashMap<>();
	public long price = 10000;
	
	public SignData(int length){
		text = new String[length];
	}

	public SignData load(NBTTagCompound com){
		if(com.hasKey("type")) type = Type.valueOf(com.getString("type"));
		price = com.getLong("fee");
		settings.clear();
		events.clear();
		for(String str : type.settings){
			if(com.hasKey("set:" + str)) settings.put(str, com.getString(str));
		}
		for(String event : type.cmd_events){
			if(com.hasKey("event:" + event)){
				NBTTagList list = (NBTTagList)com.getTag("event:" + event);
				ArrayList<String> alist = new ArrayList<>();
				for(NBTBase base : list){
					alist.add(((NBTTagString)base).getString());
				}
				events.put(event, alist);
			}
		}
		return this;
	}
	
	public NBTTagCompound save(NBTTagCompound com){
		com.setString("type", type.toString());
		com.setLong("fee", price);
		for(Entry<String, String> entry : settings.entrySet()){
			com.setString("set:" + entry.getKey(), entry.getValue());
		}
		for(Entry<String, ArrayList<String>> cmds : events.entrySet()){
			NBTTagList list = new NBTTagList();
			for(String str : cmds.getValue()) list.appendTag(new NBTTagString(str));
			com.setTag("event:" + cmds.getKey(), list);
		}
		for(int i = 0; i < text.length; i++){
			if(text[i] != null && text[i].length() > 0) com.setString("text:" + i, text[i]);
		}
		return com;
	}

	public void process(SignCapability cap, PlayerInteractEvent event, IBlockState state, TileEntitySign tile){
		EntityPlayer player = event.getEntityPlayer();
		if(price > 0){
			Account account = DataManager.getAccount("player:" + event.getEntityPlayer(), false, false);
			if(account == null || account.getBalance() < price){
				Print.chat(player, trs("not_enough_money"));
				return;
			}
			account.setBalance(account.getBalance() - price);
		}
		ICommandManager cmdman = Static.getServer().commandManager;
		ArrayList<String> cmds = events.get(type.cmd_events[0]);
		if(cmds == null || cmds.isEmpty()) return;
		for(String cmd : cmds){
			cmd = format(cmd, tile, state, player);
			if(cmd.startsWith("p!")) cmdman.executeCommand(player, cmd.substring(2));
			else cmdman.executeCommand(new CommandSender(event.getWorld(), event.getEntityPlayer()), format(cmd, tile, state, player));
		}
	}

	private String format(String cmd, TileEntitySign tile, IBlockState state, EntityPlayer player){
		return cmd.replace("{name}", player.getName()).replace("{uuid}", player.getGameProfile().getId().toString());
	}

	public boolean valid(){
		if(type.cmd_events.length == 1){
			ArrayList<String> cmds = events.get(type.cmd_events[0]);
			return cmds != null && cmds.size() > 0;
		}
		else if(type == Type.RENT){
			ArrayList<String> cmds = events.get("start");
			if(cmds == null || cmds.size() == 0) return false;
			return (cmds = events.get("end")) != null && cmds.size() > 0;
		}
		return true;
	}
	
	public static enum Type {
		
		BASIC(0, "interact"), RENT(1, "start", "end");
		
		public String[] cmd_events;
		public String[] settings;
	
		Type(int sidx, String... cmds){
			cmd_events = cmds;
			settings = genset(sidx);
		}

		private String[] genset(int sidx){
			if(sidx == 0){
				return new String[]{ "fee", "limit", "renew" };
			}
			if(sidx == 1){
				return new String[]{ "fee", "duration" };
			}
			return new String[]{ "fee" };
		}
		
	}

	public boolean notext(){
		if(text == null || text.length == 0) return true;
		for(String str : text) if(str != null && str.length() > 0) return false;
		return true;
	}

}
