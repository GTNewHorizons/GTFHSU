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

	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
			Block block, int modelId, RenderBlocks renderer) {
		
		ClientProxy tProxy = (ClientProxy)GTSUMod.proxy;
		
		CoreBlock tBlock = (CoreBlock)block;
		if(tProxy.renderPass == 0)
		{
			renderer.renderStandardBlockWithAmbientOcclusion(block, x, y, z, 1, 1, 1);
			
			IIcon ico = tBlock.getAlpaIcon();
//			Tessellator tes = Tessellator.instance;
//			tes.setBrightness(240);
//			tes.setColorOpaque_F(255, 255, 255);
			renderer.renderFaceXNeg(block, x, y, z, ico);
			renderer.renderFaceXPos(block, x, y, z, ico);
			renderer.renderFaceYNeg(block, x, y, z, ico);
			renderer.renderFaceYPos(block, x, y, z, ico);
			renderer.renderFaceZNeg(block, x, y, z, ico);
			renderer.renderFaceZPos(block, x, y, z, ico);

		}

		return true;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return false;
	}

	@Override
	public int getRenderId() {
		return ((ClientProxy)GTSUMod.proxy).coreBlockRenderType;
	}

}
