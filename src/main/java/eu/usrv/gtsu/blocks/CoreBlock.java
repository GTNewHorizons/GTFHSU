package eu.usrv.gtsu.blocks;

import ic2.core.IC2;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import eu.usrv.gtsu.GTSUMod;
import eu.usrv.gtsu.proxy.ClientProxy;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class CoreBlock extends Block {

	@SideOnly(Side.CLIENT)
	protected IIcon icAlpha;
	protected IIcon icAnimation;
	protected IIcon icDefault;

	public CoreBlock(Material m) {
		super(m);
		setBlockName("CoreBlock");
		this.setCreativeTab(IC2.tabIC2);
		this.setHardness(1.5F);
		this.setStepSound(soundTypeMetal);
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType() {
		return ((ClientProxy)GTSUMod.proxy).coreBlockRenderType;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister r) {

		icDefault = r.registerIcon("GTSU:multiblock/core");
		icAlpha = r.registerIcon("GTSU:multiblock/core_alpha");
		icAnimation = r.registerIcon("GTSU:multiblock/core_underlay_empty");

	}

	public IIcon getNormalIcon()
	{
		return icDefault;
	}
	
	public IIcon getAlpaIcon()
	{
		return icAlpha;
	}
	
	public IIcon getAnimationIcon()
	{
		return icAnimation;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(IBlockAccess w, int x, int y, int z,
			int side) {

		return icAnimation;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public int getRenderBlockPass() {
		return 1;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean canRenderInPass(int pass) {
		((ClientProxy)GTSUMod.proxy).renderPass = pass;
		return true;
	}
}
