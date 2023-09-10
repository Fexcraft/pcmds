package net.fexcraft.mod.pcmds;

import net.fexcraft.lib.mc.capabilities.FCLCapabilities;
import net.fexcraft.lib.mc.gui.GenericContainer;
import net.fexcraft.lib.mc.utils.Static;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
		data = sign.getCapability(FCLCapabilities.SIGN_CAPABILITY, null).getListener(SignCapImpl.class, SignCapImpl.REGNAME).data;
	}

	@Override
	protected void packet(Side side, NBTTagCompound packet, EntityPlayer player){
		if(side.isServer()){
			if(packet.getBoolean("sync")){
				sendSync();
				return;
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
		sync.setTag("signdata", data.save(new NBTTagCompound()));
		try{
			sync.setString("opname", PayableCommandSigns.OP_PLAYER_ID == null ? "" : Static.getServer().getPlayerProfileCache().getProfileByUUID(PayableCommandSigns.OP_PLAYER_ID).toString());
		}
		catch(Exception e){
			e.printStackTrace();
		}
		send(Side.CLIENT, sync);
	}

}