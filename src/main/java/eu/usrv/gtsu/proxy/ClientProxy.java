package eu.usrv.gtsu.proxy;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLLog;
import eu.usrv.gtsu.GTSUMod;
import eu.usrv.gtsu.ISBRH.CoreBlockRenderer;
import eu.usrv.gtsu.blocks.CoreBlock;


public class ClientProxy extends CommonProxy {

	public int coreBlockRenderType;
	public int renderPass;

	@Override
	public void setCustomRenderers()
	{
		coreBlockRenderType = RenderingRegistry.getNextAvailableRenderId();
		CoreBlockRenderer tMyRenderer = new CoreBlockRenderer();
		
		RenderingRegistry.registerBlockHandler(coreBlockRenderType, tMyRenderer);
	}
}
