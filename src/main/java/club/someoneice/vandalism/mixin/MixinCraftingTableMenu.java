package club.someoneice.vandalism.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.compress.utils.Lists;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(CraftingMenu.class)
public abstract class MixinCraftingTableMenu extends AbstractCraftingMenu {
  @Shadow @Final private ContainerLevelAccess access;
  
  public MixinCraftingTableMenu(MenuType<?> menuType, int containerId, int width, int height) {
    super(menuType, containerId, width, height);
  }
  
  @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
  public void checkStillValid(Player player, CallbackInfoReturnable<Boolean> cir) {
    final boolean flag = AbstractCraftingMenu.stillValid(this.access, player, Blocks.CRAFTING_TABLE);
    if (flag) {
      cir.setReturnValue(true);
      return;
    }
    
    final List<ItemStack> items = Lists.newArrayList();
    final int slotSize = this.craftSlots.getWidth() * this.craftSlots.getHeight();
    for(int i = 0; i < slotSize; i++) {
      final ItemStack item = this.craftSlots.getItem(i);
      if (item.isEmpty()) {
        continue;
      }
      
      items.add(item.copy());
      this.craftSlots.setItem(i, ItemStack.EMPTY);
    }
    
    this.access.execute((world, pos) -> {
      for (ItemStack item : items) {
        final ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY() + 5, pos.getZ(), item);
        itemEntity.setPickUpDelay(0);
        world.addFreshEntity(itemEntity);
      }
    });
    cir.setReturnValue(flag);
    return;
  }
}
