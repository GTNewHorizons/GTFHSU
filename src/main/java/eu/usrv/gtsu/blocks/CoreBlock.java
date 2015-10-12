package eu.usrv.gtsu.blocks;

import org.apache.logging.log4j.Level;

import ic2.core.IC2;
import cpw.mods.fml.common.FMLLog;
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
	protected IIcon[] icAnimations = new IIcon[16];

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

	@Override
	public void onBlockClicked(net.minecraft.world.World world, int x, int y, int z, net.minecraft.entity.player.EntityPlayer player) 
	{
		if (!GTSUMod.developerMode)
			return;
		
		int tCurrMeta = world.getBlockMetadata(x, y, z);
		tCurrMeta++;
		if (tCurrMeta >= 15)
			tCurrMeta = 0;
		world.setBlockMetadataWithNotify(x, y, z, tCurrMeta, 3);
	};
	
	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType() {
		return ((ClientProxy)GTSUMod.proxy).coreBlockRenderType;
	}

	@Override
	public int damageDropped(int pMetadata)
	{ // Always drop the black block
		return 0;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister r)
	{
		icAlpha = r.registerIcon("GTSU:multiblock/coreblock/core_alpha");
		
		for (int i = 0; i < 16; i++)		
			icAnimations[i] = r.registerIcon(String.format("GTSU:multiblock/coreblock/core_underlay_%d", i));
	}
	
	public IIcon getAlpaIcon()
	{
		return icAlpha;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int pSide, int pMeta)
	{
		return icAnimations[pMeta];
	}

	@Override
	public boolean isOpaqueCube() {
		return true;
	}

	@Override
	public int getRenderBlockPass() {
		return 0;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean canRenderInPass(int pass) {
		return (pass==0);
	}
}
