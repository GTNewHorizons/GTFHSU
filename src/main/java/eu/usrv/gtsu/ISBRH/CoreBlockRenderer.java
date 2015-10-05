package eu.usrv.gtsu.ISBRH;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import eu.usrv.gtsu.proxy.ClientProxy;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;

public class CoreBlockRenderer implements ISimpleBlockRenderingHandler {

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID,
			RenderBlocks renderer) {
		renderer.renderBlockAsItem(Blocks.stone, 1, 1f);

		// you may get complications when you are rendering it in the inventory, because I think that you can't use passes in there, so here it is rendered as a stone block
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
			Block block, int modelId, RenderBlocks renderer) {

		if(ClientProxy.renderPass == 0)
		{// Renders the stoneblock first ^ pass 0
			renderer.renderStandardBlock(Blocks.stone, x, y, z);
		}
		else 
		{// renders the block ontop of a stone block (texture wise I guess :3)
			renderer.renderStandardBlock(Mainclass.copperStone, x, y, z);
		}

		return true;
	}

	@Override
	public int getRenderId() {

		return ClientProxy.coreBlockRenderType;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}

}
