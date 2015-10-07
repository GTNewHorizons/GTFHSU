package eu.usrv.gtsu.ISBRH;

import org.apache.logging.log4j.Level;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.common.FMLLog;
import eu.usrv.gtsu.GTSUMod;
import eu.usrv.gtsu.blocks.CoreBlock;
import eu.usrv.gtsu.proxy.ClientProxy;
import eu.usrv.gtsu.proxy.CommonProxy;

public class CoreBlockRenderer implements ISimpleBlockRenderingHandler {

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId,
			RenderBlocks renderer) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
			Block block, int modelId, RenderBlocks renderer) {
		
		ClientProxy tProxy = (ClientProxy)GTSUMod.proxy;
		
		CoreBlock tBlock = (CoreBlock)block;
		//which render pass are we doing?
		FMLLog.log(Level.INFO, "RenderPass: %d", tProxy.renderPass);
		if(tProxy.renderPass == 0)
		{
			IIcon tBackgroundLayer = tBlock.getAlpaIcon();
			float u = tBackgroundLayer.getMinU();
			float v = tBackgroundLayer.getMinV();
			float U = tBackgroundLayer.getMaxU();
			float V = tBackgroundLayer.getMaxV();
			
			Tessellator tes = Tessellator.instance;
			tes.addTranslation(x, y, z);
			
			tes.addVertexWithUV(0, 1, 1, u, v);
			tes.addVertexWithUV(1, 1, 1, u, V);
			tes.addVertexWithUV(1, 1, 0, U, V);
			tes.addVertexWithUV(0, 1, 0, U, v);
			
			tes.addTranslation(-x, -y, -z);
			FMLLog.log(Level.INFO, "RenderPass 0");
		}
		else                    
		{
			IIcon tBackgroundLayer = tBlock.getNormalIcon();
			float u = tBackgroundLayer.getMinU();
			float v = tBackgroundLayer.getMinV();
			float U = tBackgroundLayer.getMaxU();
			float V = tBackgroundLayer.getMaxV();
			
			Tessellator tes = Tessellator.instance;
			tes.addTranslation(x, y, z);
			
			tes.addVertexWithUV(0, 1, 1, u, v);
			tes.addVertexWithUV(1, 1, 1, u, V);
			tes.addVertexWithUV(1, 1, 0, U, V);
			tes.addVertexWithUV(0, 1, 0, U, v);
			
			tes.addTranslation(-x, -y, -z);
			FMLLog.log(Level.INFO, "RenderPass 1");
		}

		return false;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getRenderId() {
		return ((ClientProxy)GTSUMod.proxy).coreBlockRenderType;
	}

}
