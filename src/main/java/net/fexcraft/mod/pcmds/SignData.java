package net.fexcraft.mod.pcmds;

import static net.fexcraft.mod.pcmds.EditCmd.trs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.capabilities.sign.SignCapability;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.pcmds.PayableCommandSigns.DimPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.world.World;
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
	public Settings settings = new Settings();
	public long price = 10000;
	public DimPos pos;
	//
	public long refresh = 0;
	public int timer = 0;
	public HashMap<UUID, Integer> uses = new HashMap<>();
	
	public SignData(int length){
		text = new String[length];
	}

	public SignData load(NBTTagCompound com){
		if(com.hasKey("type")) type = Type.valueOf(com.getString("type"));
		price = com.getLong("fee");
		settings.clear();
		events.clear();
		uses.clear();
		for(String str : type.settings){
			if(com.hasKey("set:" + str)) settings.put(str, com.getInteger("set:" + str));
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
		if(com.hasKey("pos")) pos = new DimPos(com.getString("pos"));
		if(com.hasKey("refresh")){
			refresh = com.getLong("refresh");
			timer = (int)((refresh - Time.getDate()) / 60000);
			if(timer < 0) setupTimer(true);
		}
		if(com.hasKey("uses")){
			NBTTagCompound use = com.getCompoundTag("uses");
			for(String str : use.getKeySet()){
				uses.put(UUID.fromString(str), use.getInteger(str));
			}
		}
		return this;
	}

	public NBTTagCompound save(NBTTagCompound com){
		com.setString("type", type.toString());
		com.setLong("fee", price);
		for(Entry<String, Integer> entry : settings.entrySet()){
			com.setInteger("set:" + entry.getKey(), entry.getValue());
		}
		for(Entry<String, ArrayList<String>> cmds : events.entrySet()){
			NBTTagList list = new NBTTagList();
			for(String str : cmds.getValue()) list.appendTag(new NBTTagString(str));
			com.setTag("event:" + cmds.getKey(), list);
		}
		for(int i = 0; i < text.length; i++){
			if(text[i] != null && text[i].length() > 0) com.setString("text:" + i, text[i]);
		}
		if(pos != null) com.setString("pos", pos.toString());
		if(refresh > 0) com.setLong("refresh", refresh);
		if(uses.size() > 0){
			NBTTagCompound use = new NBTTagCompound();
			for(Entry<UUID, Integer> entry : uses.entrySet()){
				use.setInteger(entry.getKey().toString(), entry.getValue());
			}
			com.setTag("uses", use);
		}
		return com;
	}

	public void process(SignCapability cap, PlayerInteractEvent event, IBlockState state, TileEntitySign tile){
		EntityPlayer player = event.getEntityPlayer();
		UUID uuid = player.getGameProfile().getId();
		if(type == Type.BASIC && settings.get("limit", 0) > 0){
			Integer l = uses.get(uuid);
			if(l != null && l >= settings.get("limit", 0)){
				boolean renew = settings.get("renew", 0) > 0;
				Print.chat(player, renew ? trs("reached_use_limit_wait", timer) : trs("reached_use_limit"));
				return;
			}
		}
		if(type == Type.RENT && uses.size() > 0){
			Print.chat(player, trs("sign_already_rented", timer));
			return;
		}
		if(price > 0){
			Account account = DataManager.getAccount("player:" + uuid.toString(), false, false);
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
			cmd = format(cmd, tile, state, player, uuid);
			if(cmd.startsWith("p!")) cmdman.executeCommand(player, cmd.substring(2));
			else cmdman.executeCommand(new CommandSender(event.getWorld(), event.getEntityPlayer()), format(cmd, tile, state, player, uuid));
		}
		if(type == Type.BASIC && settings.get("limit", 0) > 0){
			Integer i = uses.get(uuid);
			uses.put(uuid, i == null ? 1 : i + 1);
			if(timer <= 0 && settings.get("renew", 0) > 0) setupTimer(false);
		}
		else if(type == Type.RENT){
			uses.put(uuid, -1);
			setupTimer(false);
		}
	}

	private String format(String cmd, TileEntitySign tile, IBlockState state, EntityPlayer player, UUID uuid){
		if(player != null) cmd = cmd.replace("{name}", player.getName());
		else{
			String name = Static.getPlayerNameByUUID(uuid);
			if(name != null) cmd = cmd.replace("{name}", name);
		}
		return cmd.replace("{uuid}", uuid.toString());
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

		private String durtag(){
			return this == BASIC ? settings[2] : settings[1];
		}
		
	}
	
	public static class Settings extends HashMap<String, Integer> {
		
		public int get(String key, int def){
			Integer entry = this.get(key);
			if(entry == null) return def;
			return entry;
		}
	}

	public boolean notext(){
		if(text == null || text.length == 0) return true;
		for(String str : text) if(str != null && str.length() > 0) return false;
		return true;
	}
	
	public void setupTimer(boolean min){
		if(min){
			refresh = Time.getDate() + 60000;
			timer = 1;
		}
		else{
			refresh = Time.getDate() + (settings.get(type.durtag(), 1) * 60000);
			timer = (int)((refresh - Time.getDate()) / 60000);
			if(timer < 1) timer = 1;
		}
		if(!PayableCommandSigns.FLOATING.containsKey(pos)) PayableCommandSigns.FLOATING.put(pos, this);
	}

	public void processTimed(){
		if(type == Type.RENT){
			UUID uuid = uses.size() == 0 ? null : uses.keySet().toArray(new UUID[0])[0];
			if(uuid == null) return;
			ICommandManager cmdman = Static.getServer().commandManager;
			ArrayList<String> cmds = events.get(type.cmd_events[1]);
			if(cmds == null || cmds.isEmpty()) return;
			World world = Static.getServer().getWorld(pos.dim);
			for(String cmd : cmds){
				cmd = format(cmd, null, null, null, uuid);
				cmdman.executeCommand(new CommandSender(world, null), format(cmd, null, null, null, uuid));
			}
		}
		uses.clear();
	}

}
