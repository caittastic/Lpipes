package caittastic.LPipes.blockentity;

import caittastic.LPipes.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class HupwardserBlockEntity extends BlockEntity {
   public static int SLOT_COUNT = 1; //the amount of slots in our inventory, count starts at 1
   static String INVENTORY_KEY = "inventory";
   ;
   //the size is for every number of slots in the inventory
   private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
      @Override
      protected void onContentsChanged(int slot) {
         setChanged();
      }
   };
   private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty(); //no clue what this does

   //constructor
   public HupwardserBlockEntity(BlockPos blockPos, BlockState blockState) {
      super(Registries.HUPWARDSER_BLOCK_ENTITY.get(), blockPos, blockState);
   }

   //code that gets ran every tick, the main logic for our block entity
   public static void tick(Level level, BlockPos pos, BlockState state, HupwardserBlockEntity blockEntity) {
      ItemStackHandler inventory = blockEntity.itemHandler;
      int flow = 1;
      //----- try extraction -----
      Direction extractDirection = Direction.DOWN;
      BlockPos extractPos = pos.relative(extractDirection);
      if(inventory.getStackInSlot(0).isEmpty()){
         BlockEntity tile = level.getBlockEntity(extractPos);
         if(tile != null){
            IItemHandler extractItemHandler = tile.getCapability(ForgeCapabilities.ITEM_HANDLER, extractDirection.getOpposite()).orElse(null);
            if(extractItemHandler != null){
               for (int i = 0; i < extractItemHandler.getSlots(); i++) {
                  //simulates extracting from the current slot
                  ItemStack targetItem = extractItemHandler.extractItem(i, flow, true);
                  //if the results of extraction is empty, skip this loop
                  if(targetItem.isEmpty())
                     continue;
                  //i barely understand this bit
                  targetItem = extractItemHandler.extractItem(i, flow, false); //extract quantity from the currently iterated slot
                  ItemStack result = inventory.insertItem(0, targetItem.copy(), false); //insert into ourselves
                  targetItem.setCount(result.getCount()); //set the currently iterated slot into leftovers
                  return; //stop looping
               }
            }
         }
      }
      //----- try insertion -----
      Direction insertDirection = Direction.UP;
      if(!level.isClientSide()){
         if(inventory != null){
            Direction themFacingMe = insertDirection.getOpposite();
            BlockPos posTarget = pos.relative(insertDirection);
            BlockEntity tileTarget = level.getBlockEntity(posTarget);
            if(tileTarget != null){
               IItemHandler handlerOutput = tileTarget.getCapability(ForgeCapabilities.ITEM_HANDLER, themFacingMe).orElse(null);
               if(handlerOutput != null){
                  ItemStack drain = inventory.extractItem(0, flow, true); //simulate draining items
                  int sizeStarted = drain.getCount();
                  //if drain is not an empty stack
                  if(!drain.isEmpty()){
                     //iterate over all the slots in the output itemhandler
                     for (int slot = 0; slot < handlerOutput.getSlots(); slot++) {
                        drain = handlerOutput.insertItem(slot, drain, false); //actually drain items
                        if(drain.isEmpty()){
                           break; //loop over the output inventory until drain has been emptied or we get to the end of the inventory
                        }
                     }
                  }
                  int sizeAfter = sizeStarted - drain.getCount(); //the amount of items left over after attempting to insert into all of the output inventory slots
                  if(sizeAfter > 0)
                     inventory.extractItem(0, sizeAfter, false);
               }
            }
         }
      }
   }

   @Nonnull
   @Override //is something to do with helping other mods interact with my block
   public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @javax.annotation.Nullable Direction side) {
      if(cap == ForgeCapabilities.ITEM_HANDLER){
         return lazyItemHandler.cast();
      }
      return super.getCapability(cap, side);
   }

   @Override //idk what this does
   public void onLoad() {
      super.onLoad();
      lazyItemHandler = LazyOptional.of(() -> itemHandler);
   }

   @Override //idk what this does
   public void invalidateCaps() {
      super.invalidateCaps();
      lazyItemHandler.invalidate();
   }

   @Override //saves the inventory and crafting progress data to NBT on saving the game
   protected void saveAdditional(@NotNull CompoundTag tag) {
      tag.put(INVENTORY_KEY, itemHandler.serializeNBT());
      super.saveAdditional(tag);
   }

   //loads the inventory and crafting progress data from NBT on saving the game
   @Override
   public void load(CompoundTag nbt) {
      super.load(nbt);
      itemHandler.deserializeNBT(nbt.getCompound(INVENTORY_KEY));
   }

   public void drops() {
      SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots()); //creates a simplecontainer containing the amount of slots in our itemhandler
      for (int i = 0; i < itemHandler.getSlots(); i++) { //loops through every item in the itemhandler
         inventory.setItem(i, itemHandler.getStackInSlot(i)); //puts every item in the itemhandler into the simplecontainer
      }

      Containers.dropContents(this.level, this.worldPosition, inventory); //drops the contents of the simplecontainer
   }
}
