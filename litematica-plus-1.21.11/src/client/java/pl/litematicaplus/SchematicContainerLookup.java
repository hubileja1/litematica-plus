package pl.litematicaplus;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

/** Reads the expected container contents from the schematic loaded in Litematica. */
public final class SchematicContainerLookup {
    private SchematicContainerLookup() {}

    /**
     * @return the expected ItemStack for a container slot (EMPTY = the slot should be empty)
     *         or null when there is no schematic or no container at this position.
     */
    public static ItemStack expected(BlockPos pos, int containerSlot) {
        WorldSchematic world = SchematicWorldHandler.getSchematicWorld();
        if (world == null || pos == null) return null;

        BlockState state = world.getBlockState(pos);
        BlockPos primary = pos;
        int slot = containerSlot;

        // Double chest: slots 0-26 = RIGHT half, 27-53 = LEFT half.
        if (state.getBlock() instanceof ChestBlock && state.hasProperty(ChestBlock.TYPE)
                && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            BlockPos other = pos.relative(ChestBlock.getConnectedDirection(state));
            boolean clickedIsRight = state.getValue(ChestBlock.TYPE) == ChestType.RIGHT;
            BlockPos right = clickedIsRight ? pos : other;
            BlockPos left = clickedIsRight ? other : pos;
            primary = slot < 27 ? right : left;
            slot = slot % 27;
        }

        BlockEntity be = world.getBlockEntity(primary);
        if (be instanceof Container container && slot >= 0 && slot < container.getContainerSize()) {
            return container.getItem(slot);
        }
        return null;
    }
}
