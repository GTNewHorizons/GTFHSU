package eu.usrv.gtsu;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import eu.usrv.gtsu.blocks.GTSUBlock;
import eu.usrv.gtsu.blocks.ItemBlockGTSU;
import eu.usrv.gtsu.gui.GuiHandler;
import eu.usrv.gtsu.multiblock.MultiBlockItemBlock;
import eu.usrv.gtsu.multiblock.MultiBlocks;
import eu.usrv.gtsu.proxy.CommonProxy;
import eu.usrv.gtsu.tileentity.TileEntityGTSU;

@Mod(modid = GTSUMod.GTSU_MODID, name = "GTSU Mod", version = "GRADLETOKEN_VERSION", dependencies = "required-after:IC2")
public class GTSUMod {

    public static final String GTSU_MODID = "GTSU";

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
        
	}

	@EventHandler
	public void init(FMLInitializationEvent event){
        NetworkRegistry.INSTANCE.registerGuiHandler(GTSUMod.instance, new GuiHandler());
	}
}
