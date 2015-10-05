package eu.usrv.gtsu.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import eu.usrv.gtsu.proxy.ClientProxy;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class CoreBlock extends Block {

	@SideOnly(Side.CLIENT)
	protected IIcon blockIconForRender;

	public CoreBlock(Material m) {
		super(m);
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public int getRenderType() {
		return ClientProxy.coreBlockRenderType;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister r) {

		blockIconForRender = r.registerIcon("GTSU:multiblock/core_alpha");

	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(IBlockAccess w, int x, int y, int z,
			int side) {

		return blockIconForRender;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public int getRenderBlockPass() {
		return 1;
	}

	@Override
	public boolean canRenderInPass(int pass) {
		ClientProxy.renderPass = pass;
		return true;
	}
}
