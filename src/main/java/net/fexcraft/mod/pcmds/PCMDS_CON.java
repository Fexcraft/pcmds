package net.fexcraft.mod.pcmds;

import java.util.UUID;

import com.mojang.authlib.GameProfile;
import net.fexcraft.lib.mc.gui.GenericContainer;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.uni.util.FCLCapabilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.server.permission.PermissionAPI;

public class PCMDS_CON extends GenericContainer {

	public final int x, y, z;
	@SideOnly(Side.CLIENT)
	public PCMDS_UI gui;
	protected SignData data;
	protected TileEntitySign sign;
	protected String opname = "";

	public PCMDS_CON(EntityPlayer entity, int x, int y, int z){
		super(entity);
		this.x = x;
		this.y = y;
		this.z = z;
		sign = (TileEntitySign)entity.world.getTileEntity(new BlockPos(x, y, z));
		if(entity.world.isRemote) return;
		SignData fldt = PayableCommandSigns.FLOATING.get(sign.getPos());
		if(fldt != null && data != fldt) data = fldt;
		data = sign.getCapability(FCLCapabilities.SIGN_CAPABILITY, null).getListener(SignCapImpl.class, SignCapImpl.REGNAME).data;
		if(!PermissionAPI.hasPermission(player, PayableCommandSigns.EDIT_SIGN_PERM)){
			Print.chat(player, EditCmd.trs("no_permission"));
			player.closeScreen();
		}
	}

	@Override
	protected void packet(Side side, NBTTagCompound packet, EntityPlayer player){
		if(side.isServer()){
			if(packet.getBoolean("sync")){
				sendSync();
				return;
			}
			if(packet.hasKey("update")){
				if(!PermissionAPI.hasPermission(player, PayableCommandSigns.EDIT_SIGN_PERM)){
					Print.chat(player, EditCmd.trs("no_permission"));
					return;
				}
				SignCapImpl impl = sign.getCapability(FCLCapabilities.SIGN_CAPABILITY, null).getListener(SignCapImpl.class, SignCapImpl.REGNAME);
				data.load(impl, sign, packet.getCompoundTag("update"));
				if(packet.hasKey("exec")){
					UUID uuid = null;
					try{
						uuid = UUID.fromString(packet.getString("exec"));
					}
					catch(Exception e){
						e.printStackTrace();
					}
					if(uuid == null){
						GameProfile gp = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(packet.getString("exec"));
						if(gp != null) uuid = gp.getId();
					}
					if(uuid == null){
						Print.chat(player, EditCmd.trs("exec_not_found"));
					}
					else{
						if(!uuid.equals(PayableCommandSigns.DEFAULT_OP_PLAYER)) data.exid = uuid;
					}
				}
				if(packet.getBoolean("activate")){
					EditCmd.activate(player, impl, sign);
				}
				player.closeScreen();
			}
		}
		else{
			if(packet.hasKey("opname")){
				opname = packet.getString("opname");
			}
			if(packet.hasKey("signdata")){
				data = new SignData(sign.signText.length).load(null, null, packet.getCompoundTag("signdata"));
				gui.update(true);
			}
		}
	}

	private void sendSync(){
		NBTTagCompound sync = new NBTTagCompound();
		sync.setTag("signdata", data.save(new NBTTagCompound(), false));
		try{
			sync.setString("opname", PayableCommandSigns.DEFAULT_OP_PLAYER == null ? "" : Static.getServer().getPlayerProfileCache().getProfileByUUID(PayableCommandSigns.DEFAULT_OP_PLAYER).getName());
		}
		catch(Exception e){
			e.printStackTrace();
		}
		send(Side.CLIENT, sync);
	}

}