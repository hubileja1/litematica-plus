package pl.litematicaplus;

import com.mojang.brigadier.Command;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Optional;

public final class LplusCommand {
    private LplusCommand() {}

    private static void msg(String text) {
        var player = Minecraft.getInstance().player;
        if (player != null) player.displayClientMessage(Component.literal("[Litematica+] " + text), false);
    }

    private static Optional<Block> block(String id) {
        String trimmed = id.trim();
        Identifier parsed = Identifier.tryParse(trimmed.contains(":") ? trimmed : "minecraft:" + trimmed);
        return parsed == null ? Optional.empty() : BuiltInRegistries.BLOCK.getOptional(parsed);
    }

    // ---- selected placement (commands) ----
    public static int replace(String fromId, String toId) {
        SchematicPlacement p = DataManager.getSchematicPlacementManager().getSelectedSchematicPlacement();
        if (p == null) { msg("Select a placement in the Litematica menu first."); return 0; }
        return replaceIn(p, fromId, toId);
    }

    public static int clear() {
        SchematicPlacement p = DataManager.getSchematicPlacementManager().getSelectedSchematicPlacement();
        if (p == null) { msg("Select a placement in the Litematica menu first."); return 0; }
        return clearIn(p);
    }

    // ---- given placement (commands + GUI) ----
    public static int replaceIn(SchematicPlacement placement, String fromId, String toId) {
        var from = block(fromId);
        var to = block(toId);
        if (from.isEmpty()) { msg("Unknown block: " + fromId); return 0; }
        if (to.isEmpty())   { msg("Unknown block: " + toId);   return 0; }

        BlockReplacements.set(placement.getName(), from.get(), to.get());
        refresh(placement);
        msg(fromId + " -> " + toId + " in placement \"" + placement.getName() + "\"");
        return Command.SINGLE_SUCCESS;
    }

    public static int clearIn(SchematicPlacement placement) {
        var map = BlockReplacements.forPlacement(placement.getName());
        if (map != null) new ArrayList<>(map.keySet()).forEach(b -> BlockReplacements.remove(placement.getName(), b));
        refresh(placement);
        msg("Cleared replacements for \"" + placement.getName() + "\"");
        return Command.SINGLE_SUCCESS;
    }

    /** Deletes the block that the given material list item is made from, from the placement shown in the list. */
    public static int deleteForItem(ItemStack stack) {
        var manager = DataManager.getSchematicPlacementManager();
        SchematicPlacement placement = null;

        String name = BlockReplacements.listPlacementName();
        if (name != null) {
            for (SchematicPlacement p : manager.getAllSchematicsPlacements()) {
                if (p.getName().equals(name)) { placement = p; break; }
            }
        }
        if (placement == null) placement = manager.getSelectedSchematicPlacement();
        if (placement == null) { msg("Select a placement in the Litematica menu first."); return 0; }

        Block block = Block.byItem(stack.getItem());
        if (block == Blocks.AIR) { msg("This item can't be deleted from the schematic."); return 0; }

        BlockReplacements.set(placement.getName(), block, Blocks.AIR);
        refresh(placement);
        msg("Deleted " + pl.litematicaplus.gui.BlockSearch.shortId(block) + " from placement \"" + placement.getName() + "\"");
        return Command.SINGLE_SUCCESS;
    }

    public static void refresh() {
        refresh(null);
    }

    /** Rebuilds the schematic render and recalculates the placement's material list. */
    public static void refresh(SchematicPlacement placement) {
        var world = SchematicWorldHandler.getSchematicWorld();
        if (world != null) {
            fi.dy.masa.litematica.render.LitematicaRenderer.getInstance().onSchematicWorldChanged(world);
        }

        if (placement == null) {
            placement = DataManager.getSchematicPlacementManager().getSelectedSchematicPlacement();
        }
        if (placement != null) {
            var list = placement.getMaterialList();
            if (list != null) list.reCreateMaterialList();
        }

        // If the whole schematic disappears until you click "Placement: ON" in Litematica,
        // replace the block above with the toggle trick used in the 1.21.4 version:
        //   if (placement.isEnabled()) { placement.toggleEnabled(); placement.toggleEnabled(); }
    }
}
