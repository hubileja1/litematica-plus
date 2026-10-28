package pl.litematicaplus.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.litematicaplus.ContainerTracker;
import pl.litematicaplus.SchematicContainerLookup;
import pl.litematicaplus.SlotHighlight;

@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenMixin {

    @Shadow protected Slot hoveredSlot;

    /** Expected item for a slot, or null when the slot doesn't belong to a schematic container. */
    private ItemStack litematicaPlus$expectedFor(Slot slot) {
        Object self = this;
        if (self instanceof CreativeModeInventoryScreen || self instanceof InventoryScreen) return null;

        var player = Minecraft.getInstance().player;
        if (player == null || slot.container == player.getInventory()) return null;

        var pos = ContainerTracker.getLastClicked();
        if (pos == null) return null;

        return SchematicContainerLookup.expected(pos, slot.getContainerSlot());
    }

    // ---- 1. slot colours + ghost item ----
    @Inject(method = "renderSlot", at = @At("TAIL"))
    private void litematicaPlus$highlight(GuiGraphics graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        ItemStack expected = litematicaPlus$expectedFor(slot);
        if (expected == null) return;

        int color = SlotHighlight.colorFor(expected, slot.getItem());
        if (color == SlotHighlight.NONE) return;

        graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, color);

        if (color == SlotHighlight.MISSING) {
            graphics.renderFakeItem(expected, slot.x, slot.y);
            graphics.renderItemDecorations(Minecraft.getInstance().font, expected, slot.x, slot.y);
        }
    }

    // ---- 2. info panel in the top right corner ----
    @Inject(method = "render", at = @At("TAIL"))
    private void litematicaPlus$info(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        Slot hovered = this.hoveredSlot;
        if (hovered == null) return;

        ItemStack expected = litematicaPlus$expectedFor(hovered);
        if (expected == null) return;

        ItemStack actual = hovered.getItem();
        if (expected.isEmpty() && actual.isEmpty()) return;

        int status = SlotHighlight.colorFor(expected, actual);
        int actualColor = switch (status) {
            case SlotHighlight.MISSING -> 0xFF5555FF; // blue
            case SlotHighlight.WRONG   -> 0xFFFF5555; // red
            case SlotHighlight.EXTRA   -> 0xFFCC66FF; // purple
            default                    -> 0xFF55FF55; // green - matches
        };

        String[] lines = {
            "Schematic:", litematicaPlus$describe(expected),
            "Client:",    litematicaPlus$describe(actual)
        };
        int[] colors = { 0xFFFFFFFF, 0xFFFFAA00, 0xFFFFFFFF, actualColor };
        boolean[] header = { true, false, true, false };

        final float SCALE = 0.75f;

        Font font = Minecraft.getInstance().font;
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();

        graphics.pose().pushMatrix();
        graphics.pose().translate(screenWidth - 6, 4);
        graphics.pose().scale(SCALE, SCALE);

        int y = 0;
        for (int i = 0; i < lines.length; i++) {
            int w = font.width(lines[i]);
            int x = -w;
            graphics.fill(x - 2, y - 1, x + w + 2, y + 11, 0x90000000);
            graphics.drawString(font, lines[i], x, y, colors[i], false);
            if (header[i]) {
                graphics.fill(x, y + 9, x + w, y + 10, colors[i]);
            }
            y += 12;
        }

        graphics.pose().popMatrix();
    }

    private String litematicaPlus$describe(ItemStack stack) {
        if (stack.isEmpty()) return "empty";
        return stack.getHoverName().getString() + " x" + stack.getCount();
    }
}
