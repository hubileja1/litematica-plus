package pl.litematicaplus.mixin;

import fi.dy.masa.litematica.materials.MaterialListUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import pl.litematicaplus.BlockReplacements;

import java.util.Map;

@Mixin(MaterialListUtils.class)
public abstract class MaterialListUtilsMixin {

    @ModifyVariable(method = "convertStatesToStacks", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static Object2IntOpenHashMap<BlockState> litematicaPlus$remap(Object2IntOpenHashMap<BlockState> in) {
        Map<Block, Block> map = BlockReplacements.activeListMap();
        if (map == null || map.isEmpty()) return in;

        Object2IntOpenHashMap<BlockState> out = new Object2IntOpenHashMap<>();
        for (Object2IntMap.Entry<BlockState> e : in.object2IntEntrySet()) {
            BlockState state = e.getKey();
            Block to = map.get(state.getBlock());
            out.addTo(to != null ? BlockReplacements.apply(state, to) : state, e.getIntValue());
        }
        return out;
    }
}
