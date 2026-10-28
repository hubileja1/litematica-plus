package pl.litematicaplus.gui;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class BlockSearch {
    private BlockSearch() {}

    public static String shortId(Block b) {
        String id = BuiltInRegistries.BLOCK.getKey(b).toString();
        return id.startsWith("minecraft:") ? id.substring(10) : id;
    }

    /** Blocks matching a name fragment: names starting with it first, then names containing it. */
    public static List<Block> search(String query, int limit) {
        String q = query.trim().toLowerCase(Locale.ROOT);
        if (q.startsWith("minecraft:")) q = q.substring(10);
        if (q.isEmpty()) return List.of();

        List<Block> found = new ArrayList<>();
        for (ResourceLocation id : BuiltInRegistries.BLOCK.keySet()) {
            String path = id.getPath();
            if (!path.contains(q)) continue;
            Block block = BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
            if (block == null || block.asItem() == Items.AIR) continue; // skip water, fire, air...
            found.add(block);
        }

        final String fq = q;
        found.sort(Comparator
                .comparing((Block b) -> !shortId(b).startsWith(fq))
                .thenComparing(BlockSearch::shortId));

        // exact name typed and nothing else matches - no need to suggest
        if (found.size() == 1 && shortId(found.get(0)).equals(q)) return List.of();
        return found.size() > limit ? new ArrayList<>(found.subList(0, limit)) : found;
    }
}
