package net.fexcraft.mod.pcmds;

import static net.fexcraft.mod.pcmds.PayableCommandSigns.SIGNCAP;

import java.util.ArrayList;
import java.util.Map.Entry;

import net.fexcraft.lib.mc.api.registry.fCommand;
import net.fexcraft.lib.mc.capabilities.sign.SignCapability;
import net.fexcraft.lib.mc.utils.Formatter;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.pcmds.PayableCommandSigns.EditMode;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.server.permission.PermissionAPI;

@fCommand
public class EditCmd extends CommandBase {
	
    public EditCmd(){}
    
	@Override
	public String getName(){
		return "pcmds";
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/" + getName();
	}
    
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender){
    	return sender.getCommandSenderEntity() instanceof EntityPlayer;
    }

    @Override 
    public void execute(MinecraftServer server, ICommandSender sender, String[] args){ 
    	if(sender instanceof EntityPlayer == false) return;
    	EntityPlayer player = (EntityPlayer)sender.getCommandSenderEntity();
    	if(!PermissionAPI.hasPermission(player, PayableCommandSigns.EDIT_SIGN_PERM)){
			Print.chat(player, trs("no_permission"));
    		return;
    	}
    	if(args.length < 1){
    		Print.chat(sender, "/pcmds help");
    		return;
    	}
    	EditMode mode = PayableCommandSigns.SELSIGNS.get(player.getGameProfile().getId());
    	SignCapability cap = getCap(sender.getEntityWorld(), mode);
    	SignCapImpl impl = cap == null ? null : cap.getListener(SignCapImpl.class, SignCapImpl.REGNAME);
    	SignData data = impl == null ? null : impl.data;
		switch(args[0]){
		case "editmode":{
			mode.set_edit = !mode.set_edit;
			Print.chat(sender, trs("cmd.editmode_" + (mode.set_edit ? "on" : "off")));
    		return;
		}
		case "activate":{
	    	if(impl == null){
	    		Print.chat(sender, trs("sign_none_selected"));
	    	}
	    	else{
	    		if(!impl.data.valid()){
		    		impl.setActive(cap.getTileEntity());
		    		Print.chat(sender, trs("sign_incomplete"));
	    		}
	    		else{
		    		impl.setActive(cap.getTileEntity());
		    		Print.chat(sender, trs("sign_activated"));
	    		}
	    	}
    		return;
		}
		case "status":{
    		Print.chat(sender, "&0[&6PcmdS&0]&e>>&2==== === == =");
    		if(err(sender, impl, data)) return;
    		Print.chat(sender, trs("cmd.status.type", data.type.name().toLowerCase()));
    		for(String str : data.type.cmd_events){
        		Print.chat(sender, trs("cmd.status.event", str));
        		ArrayList<String> list = data.events.get(str);
        		if(list == null || list.isEmpty()){
            		Print.chat(sender, trs("cmd.status.event.none"));
        		}
        		else{
        			for(int i = 0; i < list.size(); i++){
                		Print.chat(sender, i + " " + trs("cmd.status.event.cmd", list.get(i)));
        			}
        		}
    		}
    		Print.chat(sender, trs("cmd.status.price", Config.getWorthAsString(data.price)));
    		Print.chat(sender, trs("cmd.status.settings"));
    		if(data.settings.isEmpty()){
        		Print.chat(sender, trs("cmd.status.settings.none"));
        		Print.chat(sender, trs("cmd.status.settings.none_info"));
    		}
    		else{
    			for(Entry<String, String> entry : data.settings.entrySet()){
            		Print.chat(sender, trs("cmd.status.settings.entry", entry.getKey(), entry.getValue()));
    			}
    		}
			return;
		}
		case "text":{
			if(args.length < 3){
	    		Print.chat(sender, "&a---- --- -- -");
	    		Print.chat(sender, "/pcmds text <line> <text>");
	    		Print.chat(sender, "/pcmds text 0 example text");
				return;
			}
			int idx = Integer.parseInt(args[1]);
			String str = args[2];
			if(args.length > 3) for(int i = 3; i < args.length; i++) str += " " + args[i];
			data.text[idx] = str;
			Print.chat(sender, trs("cmd.text.updated", idx));
			return;
		}
		case "type":{
			if(args.length < 2){
	    		Print.chat(sender, "&a---- --- -- -");
	    		Print.chat(sender, "/pcmds type <process-type>");
	    		Print.chat(sender, "/pcmds type list");
	    		return;
			}
			if(args[1].equals("list")){
	    		Print.chat(sender, "&a---- --- -- -");
	    		Print.chat(sender, trs("cmd.type.available"));
	    		for(SignData.Type type : SignData.Type.values()){
	    			Print.chat(sender, "&a- " + type.name().toLowerCase());
	    			Print.chat(sender, trs("cmd.type." + type.name().toLowerCase()));
	    		}
				return;
			}
			data.type = SignData.Type.valueOf(args[1].toUpperCase());
			Print.chat(sender, trs("cmd.type.updated"));
			return;
		}
		case "set":{
			if(args.length < 2){
	    		Print.chat(sender, "&a---- --- -- -");
	    		Print.chat(sender, "/pcmds set <setting> <value>");
	    		Print.chat(sender, "/pcmds set list");
	    		return;
			}
			if(args[1].equals("list")){
	    		Print.chat(sender, "&a---- --- -- -");
	    		Print.chat(sender, trs("cmd.set.available"));
	    		for(String set : data.type.settings){
	    			Print.chat(sender, "&a- " + set);
	    			Print.chat(sender, trs("cmd.set." + set));
	    		}
				return;
			}
			if(args.length < 3){
				Print.chat(sender, "cmd.set.missing_value");
				return;
			}
			String set = args[1];
			boolean found = false;
			for(String str : data.type.settings){
				if(str.equals(set)){
					found = true;
					break;
				}
			}
			if(!found){
				Print.chat(sender, "cmd.set.not_found");
				return;
			}
			String val = args[2];
			if(set.equals("fee")){
				data.price = Long.parseLong(val);
			}
			else data.settings.put(set, val);
			Print.chat(sender, trs("cmd.set.updated", set, val));
			return;
		}
		case "iknow":{
			mode.knows = true;
			Print.chat(sender, trs("cmd.knows"));
			return;
		}
		case "add":{
			if(args.length < 3){
	    		Print.chat(sender, "&a---- --- -- -");
	    		Print.chat(sender, "/pcmds add <event> <cmd + args>");
	    		return;
			}
			String event = findEvent(sender, data, args[1]);
			if(event == null) return;
			if(!mode.knows){
				Print.chat(sender, trs("cmd.warning"));
				Print.chat(sender, trs("cmd.iknow"));
			}
			String rest = args[2];
			if(args.length > 3) for(int i = 3; i < args.length; i++) rest += " " + args[i];
			if(!data.events.containsKey(event)) data.events.put(event, new ArrayList<>());
			data.events.get(event).add(rest);
			Print.chat(sender, trs("cmd.cmd.added"));
			return;
		}
		case "rem": case "remove":{
			if(args.length < 3){
	    		Print.chat(sender, "&a---- --- -- -");
	    		Print.chat(sender, "/pcmds remove <event> <index>");
	    		return;
			}
			String event = findEvent(sender, data, args[1]);
			if(event == null) return;
			int idx = Integer.parseInt(args[2]);
			ArrayList<String> list = data.events.get(event);
			if(list == null || idx < 0 || idx >= list.size()) return;
			list.remove(idx);
			Print.chat(sender, trs("cmd.cmd.removed"));
			return;
		}
		case "clear":{
			if(args.length < 2){
	    		Print.chat(sender, "&a---- --- -- -");
	    		Print.chat(sender, "/pcmds clear <event>");
	    		return;
			}
			String event = findEvent(sender, data, args[1]);
			if(event == null) return;
			data.events.remove(event);
			Print.chat(sender, trs("cmd.cmd.cleared"));
			return;
		}
		case "help":
    		Print.chat(sender, "&0[&6PcmdS&0]&2==== === == =");
    		ITextComponent comp0 = new TextComponentString(Formatter.format("&2WIKI: "));
    		ITextComponent comp1 = new TextComponentString(Formatter.format("&7&nhttps://fexcraft.net/wiki/mod/pcmds"));
    		comp1.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://fexcraft.net/wiki/mod/pcmds"));
    		comp0.appendSibling(comp1);
    		sender.sendMessage(comp0);
    		Print.chat(sender, "/pcmds editmode");
    		Print.chat(sender, "/pcmds activate");
    		Print.chat(sender, "/pcmds status");
    		Print.chat(sender, "&2---- --- -- -");
    		Print.chat(sender, "/pcmds add <event> <cmd + args>");
    		Print.chat(sender, "/pcmds remove <event> <index>");
    		Print.chat(sender, "/pcmds clear <event>");
    		Print.chat(sender, "/pcmds text <line> <text>");
    		Print.chat(sender, "/pcmds type <process-type>");
    		Print.chat(sender, "/pcmds type list");
    		Print.chat(sender, "/pcmds set <setting> <value>");
    		Print.chat(sender, "/pcmds set list");
    		Print.chat(sender, "&2---- --- -- -");
    		Print.chat(sender, "/pcmds save <id>");
    		Print.chat(sender, "/pcmds load <id> <args>");
    		Print.chat(sender, "/pcmds export");
    		Print.chat(sender, "/pcmds import <args>");
			return;
		default:
    		Print.chat(sender, trs("cmd.unknown_arg"));
			return;
		}
    }

	private String findEvent(ICommandSender sender, SignData data, String string){
		boolean found = false;
		for(String str : data.type.cmd_events){
			if(str.equals(string)){
				found = true;
				break;
			}
		}
		if(!found){
			Print.chat(sender, "cmd.cmd.event_not_found");
			return null;
		}
		return string;
	}

	private boolean err(ICommandSender sender, SignCapImpl impl, SignData data){
		if(impl == null){
			Print.chat(sender, "error, no cap impl");
			return true;
		}
		if(data == null){
			Print.chat(sender, "error, no sign data");
			return true;
		}
		return false;
	}

	private SignCapability getCap(World world, EditMode mode){
		if(mode.pos == null) return null;
		TileEntity tile = world.getTileEntity(mode.pos);
		if(tile == null) return null;
		return tile.getCapability(SIGNCAP, null);
	}

	@SuppressWarnings("deprecation")
	public static String trs(String string){
		return I18n.translateToLocal("pcmds." + string);
	}

	@SuppressWarnings("deprecation")
	public static String trs(String string, Object... strs){
		return I18n.translateToLocalFormatted("pcmds." + string, strs);
	}

}
