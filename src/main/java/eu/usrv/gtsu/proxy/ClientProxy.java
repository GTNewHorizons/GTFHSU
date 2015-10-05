package eu.usrv.gtsu.proxy;

import cpw.mods.fml.client.registry.RenderingRegistry;
import eu.usrv.gtsu.ISBRH.CoreBlockRenderer;


public class ClientProxy extends CommonProxy {

	public static int coreBlockRenderType;
	public static int renderPass;



	public static void setCustomRenderers()
	{
		coreBlockRenderType = RenderingRegistry.getNextAvailableRenderId();

		RenderingRegistry.registerBlockHandler(new CoreBlockRenderer());
	}
}
