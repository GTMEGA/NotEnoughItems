package codechicken.nei.api;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

public interface IStackStringifyHandler {

    public NBTTagCompound convertItemStackToNBT(ItemStack stack, boolean saveStackSize);

    public ItemStack convertNBTToItemStack(NBTTagCompound nbtTag);

    public FluidStack getFluid(ItemStack stack);
}
