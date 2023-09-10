package net.fexcraft.mod.pcmds;

import java.util.ArrayList;

import net.fexcraft.lib.mc.gui.GenericGui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;

public class PCMDS_UI extends GenericGui<PCMDS_CON> {

	private static final ResourceLocation TEXTURE = new ResourceLocation("pcmds:textures/gui/main.png");
	private static ArrayList<String> info = new ArrayList<>();

	public PCMDS_UI(EntityPlayer player, int x, int y, int z){
		super(TEXTURE, new PCMDS_CON(player, x, y, z), player);
		xSize = 256;
		ySize = 208;
	}
	
	@Override
	public void init(){
		//
		container.gui = this;
		NBTTagCompound com = new NBTTagCompound();
		com.setBoolean("sync", true);
		container.send(Side.SERVER, com);
	}

	@Override
	public void predraw(float ticks, int mx, int my){
		//
	}
	
	@Override
	public void drawbackground(float ticks, int mx, int my){
		//
	}
	
	@Override
	protected void drawlast(float pticks, int mx, int my){
		info.clear();
		//
		if(info.size() > 0) drawHoveringText(info, mx, my);
	}
	
}