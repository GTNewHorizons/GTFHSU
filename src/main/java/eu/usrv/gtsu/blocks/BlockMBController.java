package eu.usrv.gtsu.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ic2.core.IC2;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import eu.usrv.gtsu.blocks.itemblocks.ItemBlockGT5EnergyUnit;
import eu.usrv.gtsu.blocks.itemblocks.ItemBlockMBController;
import eu.usrv.gtsu.tileentity.TEGT5EnergyInput;
import eu.usrv.gtsu.tileentity.TEGT5EnergyOutput;
import eu.usrv.gtsu.tileentity.TEMBControllerBlock;

public class BlockMBController extends GTSU_GenericBlock {
	@SideOnly(Side.CLIENT)
	protected IIcon icValid;
	@SideOnly(Side.CLIENT)
	protected IIcon icInvalid;
	
	public BlockMBController() {
		super(ItemBlockMBController.class, "MultiblockController", Material.iron);

		this.setCreativeTab(IC2.tabIC2);
		this.setHardness(3.0F);
		this.setStepSound(soundTypeMetal);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister r)
	{
		icValid= r.registerIcon("GTSU:multiblock/controller_on");
		icInvalid = r.registerIcon("GTSU:multiblock/controller_off");
	}
	
	@Override
	public IIcon getIcon(int p_149691_1_, int p_149691_2_) {
		return icInvalid;
	}
	
	@Override
	public IIcon getIcon(IBlockAccess pWorld, int pX, int pY, int pZ, int pMeta) {
		if (pWorld.getTileEntity(pX, pY, pZ) != null)
			if (pWorld.getTileEntity(pX, pY, pZ) instanceof TEMBControllerBlock)
			{
				TEMBControllerBlock tController = (TEMBControllerBlock) pWorld.getTileEntity(pX, pY, pZ);
				if (tController.isValidMultiBlock())
					return icValid;
			}
		
		return icInvalid;
	}
	
	@Override
	public final boolean hasTileEntity(int metadata){
	    return true;
	}
    
	@Override
	public final TileEntity createTileEntity(World pWorld, int pMeta) {
		return new TEMBControllerBlock();
	}
}
