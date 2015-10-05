package eu.usrv.gtsu.multiblock;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class MultiBlockItemBlock extends ItemBlock {
	public MultiBlockItemBlock(Block block)
	{
		super(block);
		setHasSubtypes(true);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		String name = MultiBlocks._mMultiBlockNames[itemstack.getItemDamage()];
		return getUnlocalizedName() + "." + name;
	}

	@Override
	public int getMetadata(int par1)
	{
		return par1;
	}

}
