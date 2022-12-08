package net.fexcraft.mod.pcmds;

import static net.fexcraft.mod.pcmds.EditCmd.trs;

import net.fexcraft.lib.mc.capabilities.sign.SignCapability;
import net.fexcraft.lib.mc.utils.Formatter;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.pcmds.PayableCommandSigns.EditMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.server.permission.PermissionAPI;

/**
 * 
 * @author Ferdinand Calo' (FEX___96)
 *
 */
public class SignCapImpl implements SignCapability.Listener {
	
	public static final ResourceLocation REGNAME = new ResourceLocation("pcmds:sign");
	protected SignData data;
	private boolean active;

	@Override
	public ResourceLocation getId(){
		return REGNAME;
	}

	@Override
	public boolean isActive(){
		return active;
	}

	@Override
	public boolean onPlayerInteract(SignCapability cap, PlayerInteractEvent event, IBlockState state,TileEntitySign tile){
		if(event.getWorld().isRemote) return false;
		if(data == null){
			if(!tile.signText[0].getUnformattedText().toLowerCase().equals("[pcmds]")) return false;
			if(!hasPerm(event.getEntityPlayer())){
				Print.chat(event.getEntityPlayer(), trs("no_permission"));
				return false;
			}
			PayableCommandSigns.SELSIGNS.get(event.getEntityPlayer().getGameProfile().getId()).pos = tile.getPos();
			Print.chat(event.getEntityPlayer(), trs("sign_registered"));
			tile.signText[0] = formattedComponent("&0[&6PcmdS&0]");
			tile.signText[1] = formattedComponent("&einactive");
			sendUpdate(tile);
			data = new SignData(tile.signText.length);
			cap.setActive();
			return true;
		}
		if(active){
			EditMode mode = PayableCommandSigns.SELSIGNS.get(event.getEntityPlayer().getGameProfile().getId());
			if(mode.set_edit){
				mode.pos = tile.getPos();
				active = false;
				tile.signText[0] = formattedComponent("&0[&6PcmdS&0]");
				tile.signText[1] = formattedComponent("&einactive");
				sendUpdate(tile);
				Print.chat(event.getEntityPlayer(), trs("sign_deactivated"));
				Print.chat(event.getEntityPlayer(), trs("sign_selected"));
			}
			else data.process(cap, event, state, tile);
			return true;
		}
		else if(hasPerm(event.getEntityPlayer())){
			EditMode mode = PayableCommandSigns.SELSIGNS.get(event.getEntityPlayer().getGameProfile().getId());
			mode.pos = tile.getPos();
			Print.chat(event.getEntityPlayer(), trs("sign_selected"));
			return true;
		}
		else return false;
	}

	private ITextComponent formattedComponent(String string){
		return new TextComponentString(Formatter.format(string));
	}

	private boolean hasPerm(EntityPlayer player){
		return PermissionAPI.hasPermission(player, PayableCommandSigns.EDIT_SIGN_PERM);
	}

	@Override
	public NBTBase writeToNBT(Capability<SignCapability> capability, EnumFacing side){
		NBTTagCompound com = new NBTTagCompound();
		com.setBoolean("sign:active", active);
		if(data == null) return com;
		com.setTag("sign:data", data.save(new NBTTagCompound()));
		return com;
	}

	@Override
	public void readNBT(Capability<SignCapability> capability, EnumFacing side, NBTBase nbt){
		if(nbt == null || !(nbt instanceof NBTTagCompound)){
			data = null;
			return;
		}
		NBTTagCompound com = (NBTTagCompound)nbt;
		if(com.hasKey("sign:data")){
			data = new SignData(4).load(com.getCompoundTag("sign:data"));
		}
		active = com.getBoolean("sign:active");
	}

	public void setActive(TileEntitySign sign){
		active = true;
		for(int i = 0; i < data.text.length; i++){
			if(i >= sign.signText.length) break;
			sign.signText[i] = formattedComponent(data.text[i]);
		}
		sendUpdate(sign);
	}
	
}