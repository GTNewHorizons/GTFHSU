package eu.usrv.gtsu.blocks.itemblocks;

import eu.usrv.gtsu.multiblock.MultiBlocks;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

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
