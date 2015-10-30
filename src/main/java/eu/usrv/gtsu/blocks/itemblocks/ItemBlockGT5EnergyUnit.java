package eu.usrv.gtsu.blocks.itemblocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import eu.usrv.gtsu.blocks.BlockGT5EnergyUnit;
import eu.usrv.gtsu.multiblock.MultiBlocks;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ItemBlockGT5EnergyUnit extends ItemBlock {
	public ItemBlockGT5EnergyUnit(Block p_i45328_1_) {
		super(p_i45328_1_);
		setHasSubtypes(true);
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		String tName = "";
		
		if (itemstack.getItemDamage() == 0)
		{
			tName += "Producer";
		}
		else if (itemstack.getItemDamage() == 1)
		{
			tName += "Acceptor";
		}

		return getUnlocalizedName() + "." + tName;
	}

	@Override
	public int getMetadata(int par1)
	{
		return par1;
	}
}
