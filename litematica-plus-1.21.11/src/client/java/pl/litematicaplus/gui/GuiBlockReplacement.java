package pl.litematicaplus.gui;

import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.interfaces.ITextFieldListener;
import fi.dy.masa.malilib.gui.wrappers.TextFieldType;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import pl.litematicaplus.BlockReplacements;
import pl.litematicaplus.LplusCommand;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class GuiBlockReplacement extends GuiBase {
    private static final int ROW_H = 18;
    private static final int FIELD_W = 220;
    private static final int MAX_SUGGESTIONS = 8;

    private final SchematicPlacement placement;
    private String fromText = "";
    private String toText = "";

    // text fields and suggestions
    private GuiTextFieldGeneric fromField;
    private GuiTextFieldGeneric toField;
    private int fieldX, fromY, toY;
    private int activeField = 0; // 1 = From, 2 = To, 0 = none
    private List<Block> suggestions = new ArrayList<>();

    public GuiBlockReplacement(SchematicPlacement placement, Screen parent) {
        this.placement = placement;
        this.setParent(parent);
        this.title = "Replace blocks - " + placement.getName();
    }

    private void updateSuggestions(int field, String text) {
        this.activeField = field;
        this.suggestions = BlockSearch.search(text, MAX_SUGGESTIONS);
    }

    @Override
    public void initGui() {
        super.initGui();
        this.suggestions = new ArrayList<>();
        this.activeField = 0;

        int x = 12;
        int y = 30;
        this.fieldX = x;

        // ---- text fields ----
        this.addLabel(x, y, -1, 20, -1, new String[]{"From block (e.g. stone):"});
        y += 14;
        this.fromY = y;
        this.fromField = new GuiTextFieldGeneric(x, y, FIELD_W, 16, this.font);
        this.fromField.setMaxLengthWrapper(128);
        this.fromField.setValueWrapper(this.fromText);
        ITextFieldListener<GuiTextFieldGeneric> fromListener = tf -> {
            this.fromText = tf.getValueWrapper();
            this.updateSuggestions(1, this.fromText);
            return false;
        };
        this.addTextField(this.fromField, fromListener, TextFieldType.STRING);

        y += 24;
        this.addLabel(x, y, -1, 20, -1, new String[]{"To block (e.g. cobblestone):"});
        y += 14;
        this.toY = y;
        this.toField = new GuiTextFieldGeneric(x, y, FIELD_W, 16, this.font);
        this.toField.setMaxLengthWrapper(128);
        this.toField.setValueWrapper(this.toText);
        ITextFieldListener<GuiTextFieldGeneric> toListener = tf -> {
            this.toText = tf.getValueWrapper();
            this.updateSuggestions(2, this.toText);
            return false;
        };
        this.addTextField(this.toField, toListener, TextFieldType.STRING);

        // ---- buttons ----
        y += 24;
        ButtonGeneric apply = new ButtonGeneric(x, y, 70, 20, "Apply", new String[0]);
        this.addButton(apply, (b, m) -> {
            int ok = LplusCommand.replaceIn(this.placement, this.fromText, this.toText);
            if (ok > 0) { this.fromText = ""; this.toText = ""; }
            this.initGui();
        });

        ButtonGeneric reset = new ButtonGeneric(x + 74, y, 120, 20, "Reset all", new String[0]);
        this.addButton(reset, (b, m) -> { LplusCommand.clearIn(this.placement); this.initGui(); });

        ButtonGeneric back = new ButtonGeneric(x + 198, y, 60, 20, "Back", new String[0]);
        this.addButton(back, (b, m) -> GuiBase.openGui(this.getParent()));

        // ---- list of current replacements ----
        y += 34;
        this.addLabel(x, y, -1, 20, -1, new String[]{"Current replacements:"});
        y += 16;

        Map<Block, Block> map = BlockReplacements.forPlacement(this.placement.getName());
        if (map == null || map.isEmpty()) {
            this.addLabel(x + 6, y, -1, 20, 0xFFAAAAAA, new String[]{"(none)"});
            return;
        }

        List<Map.Entry<Block, Block>> entries = new ArrayList<>(map.entrySet());
        entries.sort(Comparator.comparing(e -> BlockSearch.shortId(e.getKey())));

        int shown = 0;
        for (Map.Entry<Block, Block> e : entries) {
            if (y > this.getScreenHeight() - 30) break;
            Block from = e.getKey();
            this.addLabel(x + 6, y + 5, -1, 20, -1,
                    new String[]{BlockSearch.shortId(from) + "  ->  " + BlockSearch.shortId(e.getValue())});
            ButtonGeneric del = new ButtonGeneric(x + 260, y, 20, 20, "x", new String[0]);
            this.addButton(del, (b, m) -> {
                BlockReplacements.remove(this.placement.getName(), from);
                LplusCommand.refresh(this.placement);
                this.initGui();
            });
            y += 22;
            shown++;
        }
        if (shown < entries.size()) {
            this.addLabel(x + 6, y + 5, -1, 20, 0xFFAAAAAA, new String[]{"... and " + (entries.size() - shown) + " more"});
        }
    }

    // ---- suggestions drawn on top of the window ----
    private int listTop() {
        return (this.activeField == 1 ? this.fromY : this.toY) + 18;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (this.suggestions.isEmpty() || this.activeField == 0) return;

        int top = this.listTop();
        int height = this.suggestions.size() * ROW_H;
        graphics.fill(this.fieldX - 1, top - 1, this.fieldX + FIELD_W + 1, top + height + 1, 0xFF000000);
        graphics.fill(this.fieldX, top, this.fieldX + FIELD_W, top + height, 0xFF202020);

        for (int i = 0; i < this.suggestions.size(); i++) {
            int rowY = top + i * ROW_H;
            boolean hovered = mouseX >= this.fieldX && mouseX < this.fieldX + FIELD_W
                    && mouseY >= rowY && mouseY < rowY + ROW_H;
            if (hovered) {
                graphics.fill(this.fieldX, rowY, this.fieldX + FIELD_W, rowY + ROW_H, 0xFF404060);
            }
            Block block = this.suggestions.get(i);
            graphics.drawString(this.font, BlockSearch.shortId(block), this.fieldX + 22, rowY + 5, 0xFFFFFFFF, false);
            graphics.renderFakeItem(new ItemStack(block), this.fieldX + 2, rowY + 1);
        }
    }

    // ---- click on a suggestion ----
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!this.suggestions.isEmpty() && this.activeField != 0) {
            double mx = event.x();
            double my = event.y();
            int top = this.listTop();
            if (mx >= this.fieldX && mx < this.fieldX + FIELD_W
                    && my >= top && my < top + this.suggestions.size() * ROW_H) {
                int index = (int) ((my - top) / ROW_H);
                Block chosen = this.suggestions.get(index);
                String id = BlockSearch.shortId(chosen);

                if (this.activeField == 1) { this.fromText = id; this.fromField.setValueWrapper(id); }
                else                       { this.toText = id;   this.toField.setValueWrapper(id); }

                this.suggestions = new ArrayList<>();
                this.activeField = 0;
                return true;
            }
        }
        boolean result = super.mouseClicked(event, doubleClick);
        // a click outside the list closes it
        this.suggestions = new ArrayList<>();
        this.activeField = 0;
        return result;
    }
}
