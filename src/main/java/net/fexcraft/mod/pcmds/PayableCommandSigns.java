package net.fexcraft.mod.pcmds;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fexcraft.lib.mc.utils.Static;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;

import com.mojang.authlib.GameProfile;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.capabilities.sign.SignCapability;
import net.fexcraft.lib.mc.capabilities.sign.SignCapabilitySerializer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

/**
 * 
 * @author Ferdinand Calo' (FEX___96)
 *
 */
@Mod(modid = PayableCommandSigns.MODID, name = PayableCommandSigns.NAME, version = PayableCommandSigns.VERSION,
	acceptableRemoteVersions = "*", acceptedMinecraftVersions = "*", dependencies = "required-after:fsmm")
public class PayableCommandSigns {
	
    public static final String MODID = "pcmds";
    public static final String NAME = "Payable Command Signs";
    public static final String VERSION = "2.0";
    public static final String EDIT_SIGN_PERM = "pcmds.edit_sign";
    public static Timer TIMER;
    private static Logger logger;
	public static final ConcurrentHashMap<UUID, EditMode> SELSIGNS = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<DimPos, SignData> FLOATING = new ConcurrentHashMap<>();
	@CapabilityInject(SignCapability.class)
	public static final Capability<SignCapability> SIGNCAP = null;
	public static File CFGPATH, ROOTPATH;
	public static HashMap<Integer, HashMap<UUID, FakePlayer>> OP_PLAYERS = new HashMap<>();
	public static UUID OP_PLAYER_ID;
	@Mod.Instance(MODID)
	public static PayableCommandSigns INSTANCE;

	@EventHandler
    public void preInit(FMLPreInitializationEvent event){
        logger = event.getModLog();
		SignCapabilitySerializer.addListener(SignCapImpl.class);
		MinecraftForge.EVENT_BUS.register(new SubEventHandler());
		CFGPATH = new File(ROOTPATH = event.getModConfigurationDirectory(), "/pcmds");
		if(!CFGPATH.exists()) CFGPATH.mkdirs();
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		boolean enabled = config.getBoolean("use_fakeplayer", "general", false, "Should a fake-player be used to handle commands prefixed with 'o!' ?");
		String string = config.getString("fakeplayer_uuid", "general", "null", "Default UUID of the fake-player.");
		if(enabled) OP_PLAYER_ID = UUID.fromString(string);
		config.save();
    }

    @EventHandler
    public void init(FMLInitializationEvent event){
		PermissionAPI.registerNode(EDIT_SIGN_PERM, DefaultPermissionLevel.OP, "Permission to edit [PCMDS] Sings");
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
    }
    
	@EventHandler
	public void serverStarting(FMLServerAboutToStartEvent event){
		File cache = new File(ROOTPATH, "/pcmds_cache.nbt");
		if(!cache.exists()) return;
		try{
			NBTTagCompound com = CompressedStreamTools.read(cache);
			for(String str : com.getKeySet()){
				FLOATING.put(new DimPos(str), new SignData(4).load(null, null, com.getCompoundTag(str)));
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
    
	@EventHandler
	public void serverStarted(FMLServerStartedEvent event){
		LocalDateTime midnight = LocalDateTime.of(LocalDate.now(ZoneOffset.systemDefault()), LocalTime.MIDNIGHT);
		long mid = midnight.toInstant(ZoneOffset.UTC).toEpochMilli();
		long date = Time.getDate();
		while((mid += 60000) < date);
		if(TIMER == null){
			(TIMER = new Timer()).schedule(new ScheduledCheck(), new Date(mid), 60000);
		}
	}
    
	@EventHandler
	public void serverStopping(FMLServerStoppingEvent event){
		File cache = new File(ROOTPATH, "/pcmds_cache.nbt");
		NBTTagCompound com = new NBTTagCompound();
		for(Entry<DimPos, SignData> entry : FLOATING.entrySet()){
			NBTTagCompound tag = new NBTTagCompound();
			entry.getValue().save(tag);
			com.setTag(entry.getKey().toString(), tag);
		}
		try{
			CompressedStreamTools.write(com, cache);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static class SubEventHandler {
		
		@SubscribeEvent
		public void onPlayerJoin(PlayerLoggedInEvent event){
			SELSIGNS.put(event.player.getGameProfile().getId(), new EditMode());
		}
		
		@SubscribeEvent
		public void onPlayerLeave(PlayerLoggedOutEvent event){
			SELSIGNS.remove(event.player.getGameProfile().getId());
		}
		
		@SubscribeEvent
		public void onLoad(WorldEvent.Load event){
			if(OP_PLAYER_ID == null || event.getWorld().isRemote) return;
			int dim = event.getWorld().provider.getDimension();
			if(!OP_PLAYERS.containsKey(dim)) OP_PLAYERS.put(dim, new HashMap<>());
			OP_PLAYERS.get(dim).put(OP_PLAYER_ID, FakePlayerFactory.get((WorldServer)event.getWorld(), new GameProfile(OP_PLAYER_ID, "PCMDS")));
		}
		
		@SubscribeEvent
		public void onUnload(WorldEvent.Unload event){
			if(OP_PLAYER_ID == null || event.getWorld().isRemote) return;
			OP_PLAYERS.remove(event.getWorld().provider.getDimension());
		}
		
	}
	
	public static class EditMode {
		
		public boolean set_edit = false;
		public BlockPos pos;
		public boolean knows;
		
	}
	
	public static class DimPos {
		
		public BlockPos pos;
		public int dim, hash;
		
		public DimPos(String str){
			String[] split = str.split(":");
			dim = Integer.parseInt(split[0]);
			pos = BlockPos.fromLong(Long.parseLong(split[1]));
			hash = Objects.hash(dim, pos.getX(), pos.getY(), pos.getZ());
		}

		public DimPos(TileEntitySign tile){
			dim = tile.getWorld().provider.getDimension();
			pos = tile.getPos();
		}

		@Override
		public boolean equals(Object obj){
			if(obj instanceof DimPos == false) return false;
			DimPos o = (DimPos)obj;
			return o.pos.equals(pos) && o.dim == dim;
		}
		
		@Override
		public String toString(){
			return dim + ":" + pos.toLong();
		}
		
		@Override
		public int hashCode(){
			return hash;
		}
		
	}

	public static ICommandSender getOpPlayer(int dim, UUID exid){
		if(exid == null) return OP_PLAYERS.get(dim).get(OP_PLAYER_ID);
		if(!OP_PLAYERS.get(dim).containsKey(exid)){
			GameProfile gp = Static.getServer().getPlayerProfileCache().getProfileByUUID(exid);
			if(Static.getServer().getPlayerList().canSendCommands(gp)){
				OP_PLAYERS.get(dim).put(OP_PLAYER_ID, FakePlayerFactory.get(Static.getServer().getWorld(dim), new GameProfile(exid, "PCMDS")));
			}
			else return OP_PLAYERS.get(dim).get(OP_PLAYER_ID);
		}
		return OP_PLAYERS.get(dim).get(exid);
	}
    
}
