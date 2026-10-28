package pl.litematicaplus;

import net.minecraft.core.BlockPos;

public final class ContainerTracker {
    private static BlockPos lastClicked;

    private ContainerTracker() {}

    public static void setLastClicked(BlockPos pos) { lastClicked = pos.immutable(); }
    public static BlockPos getLastClicked() { return lastClicked; }
}
