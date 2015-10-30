package eu.usrv.gtsu.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.registry.GameRegistry;
import eu.usrv.gtsu.multiblock.IMultiBlockComponent;

public abstract class GTSU_GenericBlock extends Block {
	protected final String _mUnlocalizedName;

	protected GTSU_GenericBlock(Class<? extends ItemBlock> pItemClass, String pName, Material pMaterial) {
		super(pMaterial);
		setBlockName(_mUnlocalizedName = pName);
		GameRegistry.registerBlock(this, pItemClass, getUnlocalizedName());
	}

	@Override
	public boolean onBlockActivated(World aWorld, int aX, int aY, int aZ, EntityPlayer aPlayer, int aSide, float par1, float par2, float par3)
	{
		TileEntity tTileEntity = aWorld.getTileEntity(aX, aY, aZ);
		if (tTileEntity == null) {
			return false;
		}

		if ((tTileEntity instanceof IMultiBlockComponent))
		{
			if ((!aWorld.isRemote) && (!((IMultiBlockComponent)tTileEntity).isUseableByPlayer(aPlayer))) {
				return true;
			}
			return ((IMultiBlockComponent)tTileEntity).onRightclick(aPlayer, (byte)aSide, par1, par2, par3);
		}
		return false;
	}
}
