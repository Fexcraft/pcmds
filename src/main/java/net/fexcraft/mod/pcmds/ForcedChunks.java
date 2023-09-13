package net.fexcraft.mod.pcmds;

import java.util.ArrayList;
import java.util.List;

import net.fexcraft.lib.mc.utils.Static;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

public class ForcedChunks implements LoadingCallback {

	private static ArrayList<Ticket> tickets = new ArrayList<>();

	@Override
	public void ticketsLoaded(List<Ticket> tickets, World world){
		//
	}

	public static void requestLoad(int dim, BlockPos pos){
		ChunkPos ckpos = new ChunkPos(pos);
		Ticket ticket = getTicketForChunk(ckpos, dim);
		if(ticket == null) ticket = requestTicket(dim);
		if(ticket == null){
			System.out.println("PCMDS: Did not receive ticket for chunk loading, sign rent end event will be cancelled. DIM: " + dim + ", POS:" + pos.toString());
			System.err.println("PCMDS: Did not receive ticket for chunk loading, sign rent end event will be cancelled. DIM: " + dim + ", POS:" + pos.toString());
		}
		ForgeChunkManager.forceChunk(ticket, ckpos);
	}

	public static void requestUnload(int dim, BlockPos pos){
		ChunkPos ckpos = new ChunkPos(pos);
		Ticket ticket = getTicketForChunk(ckpos, dim);
		if(ticket != null) ForgeChunkManager.unforceChunk(ticket, ckpos);
	}

	public static void clear(){
		for(Ticket ticket : tickets){
			if(ticket.getChunkList().size() == 0){
				ForgeChunkManager.releaseTicket(ticket);
			}
		}
	}

	private static Ticket requestTicket(int dim){
		Ticket ticket = ForgeChunkManager.requestTicket(PayableCommandSigns.INSTANCE, Static.getServer().getWorld(dim), ForgeChunkManager.Type.NORMAL);
		if(ticket != null) tickets.add(ticket);
		return ticket;
	}

	public static Ticket getTicketForChunk(ChunkPos pos, int dim){
		for(Ticket ticket : tickets){
			if(ticket.world.provider.getDimension() != dim) continue;
			for(net.minecraft.util.math.ChunkPos ckp : ticket.getChunkList()){
				if(pos.equals(ckp)) return ticket;
			}
		}
		return null;
	}

}
