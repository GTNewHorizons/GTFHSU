package eu.usrv.gtsu.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.oredict.OreDictionary;
import eu.usrv.gtsu.multiblock.BlockPosHelper.BlockPoswID;
import eu.usrv.gtsu.multiblock.IMultiBlockComponent;
import gregtech.api.GregTech_API;
import gregtech.api.util.GT_ModHandler;
import gregtech.api.util.GT_Utility;


/**
 * BaseClass for all Slave-Blocks of a Multiblock Structure
 *
 */
public abstract class GTSUMBSlaveBlockBase extends GTSUTileEntityBase implements IMultiBlockComponent {
	protected boolean _mStructureValid; // Is the MultiBlock valid
	protected BlockPoswID _mMasterBlock; // Reference to our MultiBlock Master

	/**
	 * Get our Multiblock's MasterBlock. 
	 * Returns null on any error
	 * @param pWorld
	 * @return
	 */
	protected final TEMBControllerBlock getMaster()
	{
		try
		{
			if (!_mStructureValid || _mMasterBlock == null) return null;
			TileEntity tTE = worldObj.getTileEntity(_mMasterBlock.x, _mMasterBlock.y, _mMasterBlock.z);

			if (tTE == null)
			{
				_mStructureValid = false;
				return null;
			}
			if (!(tTE instanceof TEMBControllerBlock))
			{
				_mStructureValid = false;
				return null;
			}

			return (TEMBControllerBlock)tTE;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (isClientSide())
			return;
		
		if (getRandomNumber(20) != 0)
			return;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound pNBT) {
		super.writeToNBT(pNBT);
		if (_mStructureValid)
			if (_mMasterBlock != null)
				pNBT.setTag("mMasterBlock", _mMasterBlock.getTagCompound());
	}


	@Override
	public void readFromNBT(NBTTagCompound pNBT) {
		super.readFromNBT(pNBT);
		if (pNBT.hasKey("mMasterBlock"))
		{
			_mMasterBlock = new BlockPoswID(pNBT.getCompoundTag("mMasterBlock"));
			TileEntity tMasterBlock = worldObj.getTileEntity(_mMasterBlock.x, _mMasterBlock.y, _mMasterBlock.z);
			if (tMasterBlock != null && tMasterBlock instanceof TEMBControllerBlock)
			{
				_mStructureValid = ((TEMBControllerBlock)tMasterBlock).isValidMultiBlock();
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
		}
	}

	public final void destructMultiBlock()
	{
		TEMBControllerBlock tMaster = getMaster();
		if (tMaster != null)
			tMaster.destructMultiBlock();
	}
	
	@Override
	public final void updateMBStruct(boolean pStructValid, BlockPoswID pControllerBlock) {
		_mStructureValid = pStructValid;
		_mMasterBlock = pControllerBlock;
	}
	
	// Client Sync to display textures
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tagCompound = new NBTTagCompound();

		tagCompound.setBoolean("isValid", _mStructureValid);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tagCompound);
	}

	@Override
	public void onDataPacket(NetworkManager networkManager, S35PacketUpdateTileEntity packet) {
		NBTTagCompound tComp = packet.func_148857_g();

		if (tComp != null)
		{
			if (tComp.hasKey("isValid"))
			{
				_mStructureValid = tComp.getBoolean("isValid");
			}
		}
	}
}

