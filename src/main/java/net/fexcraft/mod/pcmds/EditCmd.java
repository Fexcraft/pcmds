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
        		if(list == null){
            		Print.chat(sender, trs("cmd.status.event.none"));
        		}
        		else{
        			for(String entry : list){
                		Print.chat(sender, trs("cmd.status.event.cmd", entry));
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
    			for(Entry<String, Object> entry : data.settings.entrySet()){
            		Print.chat(sender, trs("cmd.status.settings.entry", entry.getKey(), entry.getValue()));
    			}
    		}
			return;
		}
		case "help":
    		Print.chat(sender, "&0[&6PcmdS&0]&e>>&2==== === == =");
    		ITextComponent comp0 = new TextComponentString(Formatter.format("&2WIKI: "));
    		ITextComponent comp1 = new TextComponentString(Formatter.format("&7&nhttps://fexcraft.net/wiki/mod/pcmds"));
    		comp1.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://fexcraft.net/wiki/mod/pcmds"));
    		comp0.appendSibling(comp1);
    		sender.sendMessage(comp0);
    		Print.chat(sender, "/pcmds editmode");
    		Print.chat(sender, "/pcmds activate");
    		Print.chat(sender, "/pcmds status");
    		Print.chat(sender, "&2---- --- -- -");
    		Print.chat(sender, "/pcmds cmd <event> <cmd + args>");
    		Print.chat(sender, "/pcmds text <line> <text>");
    		Print.chat(sender, "/pcmds type <process-type>");
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
