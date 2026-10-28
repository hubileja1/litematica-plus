package pl.litematicaplus;

import net.minecraft.world.item.ItemStack;

/** Slot colour logic, independent of the Minecraft version (no drawing here). */
public final class SlotHighlight {
    public static final int MISSING = 0x400000FF; // blue   - item is missing
    public static final int WRONG   = 0x50FF0000; // red    - wrong item
    public static final int EXTRA   = 0x50AA00FF; // purple - item where the slot should be empty
    public static final int NONE    = 0;

    private SlotHighlight() {}

    public static int colorFor(ItemStack expected, ItemStack actual) {
        if (!expected.isEmpty() && actual.isEmpty()) return MISSING;
        if (!expected.isEmpty() && !ItemStack.isSameItem(expected, actual)) return WRONG;
        if (expected.isEmpty() && !actual.isEmpty()) return EXTRA;
        return NONE;
    }
}
