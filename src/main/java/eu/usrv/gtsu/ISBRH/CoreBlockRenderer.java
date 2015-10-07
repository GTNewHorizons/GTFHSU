package eu.usrv.gtsu.ISBRH;

import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;

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
	public void renderInventoryBlock(Block pBlock, int pMetadata, int pModelId,
			RenderBlocks pRenderer) {
		Tessellator tessellator = Tessellator.instance;

		pRenderer.renderMaxX = 1;
		pRenderer.renderMinY = 0;
		pRenderer.renderMaxZ = 1;
		pRenderer.renderMinX = 0;
		pRenderer.renderMinZ = 0;
		pRenderer.renderMaxY = 1;

		IIcon ico = pBlock.getIcon(0, pMetadata);

		GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, -1.0F, 0.0F);
		pRenderer.renderFaceYNeg(pBlock, 0.0D, 0.0D, 0.0D, ico);
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		pRenderer.renderFaceYPos(pBlock, 0.0D, 0.0D, 0.0D, ico);
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, -1.0F);
		pRenderer.renderFaceZNeg(pBlock, 0.0D, 0.0D, 0.0D, ico);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		pRenderer.renderFaceZPos(pBlock, 0.0D, 0.0D, 0.0D, ico);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(-1.0F, 0.0F, 0.0F);
		pRenderer.renderFaceXNeg(pBlock, 0.0D, 0.0D, 0.0D, ico);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		pRenderer.renderFaceXPos(pBlock, 0.0D, 0.0D, 0.0D, ico);
		tessellator.draw();

		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess pWorld, int x, int y, int z,
			Block block, int modelId, RenderBlocks renderer) {
		
		ClientProxy tProxy = (ClientProxy)GTSUMod.proxy;
		
		CoreBlock tBlock = (CoreBlock)block;
		if(tProxy.renderPass == 0)
		{
			renderer.renderStandardBlockWithAmbientOcclusion(block, x, y, z, 1, 1, 1);
			
			int tMeta = pWorld.getBlockMetadata(x, y, z);
			if (tMeta > 0)
			{
				Tessellator v5 = Tessellator.instance;
				IIcon ico = tBlock.getAlpaIcon();
				v5.setBrightness(240);
				v5.setColorOpaque_F(255, 255, 255);
				renderer.renderFaceXNeg(block, x, y, z, ico);
				renderer.renderFaceXPos(block, x, y, z, ico);
				renderer.renderFaceYNeg(block, x, y, z, ico);
				renderer.renderFaceYPos(block, x, y, z, ico);
				renderer.renderFaceZNeg(block, x, y, z, ico);
				renderer.renderFaceZPos(block, x, y, z, ico);
			}
		}

		return true;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}

	@Override
	public int getRenderId() {
		return ((ClientProxy)GTSUMod.proxy).coreBlockRenderType;
	}

}
