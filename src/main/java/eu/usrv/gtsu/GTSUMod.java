package eu.usrv.gtsu;

import java.security.MessageDigest;

import org.apache.logging.log4j.Level;

import scala.util.control.Exception;
import net.minecraft.block.material.Material;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import eu.usrv.gtsu.blocks.CoreBlock;
import eu.usrv.gtsu.blocks.GTSUBlock;
import eu.usrv.gtsu.blocks.itemblocks.ItemBlockCoreBlock;
import eu.usrv.gtsu.blocks.itemblocks.ItemBlockGTSU;
import eu.usrv.gtsu.gui.GuiHandler;
import eu.usrv.gtsu.multiblock.MultiBlockItemBlock;
import eu.usrv.gtsu.multiblock.MultiBlocks;
import eu.usrv.gtsu.proxy.CommonProxy;
import eu.usrv.gtsu.tileentity.TileEntityGTSU;

@Mod(modid = GTSUMod.GTSU_MODID, name = "GTSU Mod", version = "GRADLETOKEN_VERSION", dependencies = "required-after:IC2")
public class GTSUMod {

	public static final String GTSU_MODID = "GTSU";
	public static boolean developerMode = false;

	@Instance(GTSUMod.GTSU_MODID)
	public static GTSUMod instance;

	@SidedProxy(clientSide="eu.usrv.gtsu.proxy.ClientProxy", serverSide="eu.usrv.gtsu.proxy.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event){

		GameRegistry.registerTileEntity(TileEntityGTSU.class, "GTSU_TE");

		for (int i = 0; i < TierHelper.V.length; i++)
		{
			GameRegistry.registerBlock(new GTSUBlock(i), ItemBlockGTSU.class, String.format("GTSU_Tier_%d", i));
		}
		GameRegistry.registerBlock(new MultiBlocks(), MultiBlockItemBlock.class, String.format("GTSU_MultiBlock"));
		GameRegistry.registerBlock(new CoreBlock(Material.iron), ItemBlockCoreBlock.class, "CoreBlock");

	
		if (MD5(System.getProperty("user.name") + " lel random salt").equalsIgnoreCase("95d87ca6de3bc91f159d78f6321f5607"))
		{
			FMLLog.log(Level.INFO, "Super secret developer mode activated. Have some candy!");
			developerMode = true;
		}
	}

	@EventHandler
	public void init(FMLInitializationEvent event){
		proxy.setCustomRenderers();
		NetworkRegistry.INSTANCE.registerGuiHandler(GTSUMod.instance, new GuiHandler());
	}

	public String MD5(String md5) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
		}
		return null;
	}
}
