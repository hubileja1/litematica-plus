package pl.litematicaplus.mixin;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.world.ChunkSchematic;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.litematicaplus.BlockReplacements;

@Mixin(ChunkSchematic.class)
public abstract class ChunkSchematicMixin {

    @Inject(method = "getBlockState", at = @At("RETURN"), cancellable = true)
    private void litematicaPlus$read(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        BlockState state = cir.getReturnValue();
        if (!BlockReplacements.isSource(state.getBlock())) return; // fast path

        try {
            for (SchematicPlacement placement : DataManager.getSchematicPlacementManager().getAllSchematicsPlacements()) {
                var map = BlockReplacements.forPlacement(placement.getName());
                if (map == null) continue;
                Block to = map.get(state.getBlock());
                if (to != null && litematicaPlus$inside(placement.getEclosingBox(), pos)) {
                    cir.setReturnValue(BlockReplacements.apply(state, to));
                    return;
                }
            }
        } catch (java.util.ConcurrentModificationException ignored) {
            // placements are changing right now - keep the original state, the chunk is rebuilt soon anyway
        }
    }

    private static boolean litematicaPlus$inside(fi.dy.masa.litematica.selection.Box box, BlockPos p) {
        if (box == null) return true; // Litematica gives no area - assume the block belongs to the placement
        BlockPos a = box.getPos1();
        BlockPos b = box.getPos2();
        if (a == null || b == null) return true;
        return p.getX() >= Math.min(a.getX(), b.getX()) && p.getX() <= Math.max(a.getX(), b.getX())
                && p.getY() >= Math.min(a.getY(), b.getY()) && p.getY() <= Math.max(a.getY(), b.getY())
                && p.getZ() >= Math.min(a.getZ(), b.getZ()) && p.getZ() <= Math.max(a.getZ(), b.getZ());
    }
}
