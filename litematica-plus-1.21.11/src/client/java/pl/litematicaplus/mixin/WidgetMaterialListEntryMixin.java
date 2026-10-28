package pl.litematicaplus.mixin;

import fi.dy.masa.litematica.gui.widgets.WidgetListMaterialList;
import fi.dy.masa.litematica.gui.widgets.WidgetMaterialListEntry;
import fi.dy.masa.litematica.materials.MaterialListBase;
import fi.dy.masa.litematica.materials.MaterialListEntry;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.widgets.WidgetListEntrySortable;
import fi.dy.masa.malilib.util.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.litematicaplus.LplusCommand;

@Mixin(WidgetMaterialListEntry.class)
public abstract class WidgetMaterialListEntryMixin extends WidgetListEntrySortable<MaterialListEntry> {

    // required constructor (Mixin ignores it)
    protected WidgetMaterialListEntryMixin(int x, int y, int width, int height, MaterialListEntry entry, int listIndex) {
        super(x, y, width, height, entry, listIndex);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void litematicaPlus$deleteButton(int x, int y, int width, int height, boolean isOdd,
                                              MaterialListBase materialList, MaterialListEntry entry,
                                              int listIndex, WidgetListMaterialList listWidget, CallbackInfo ci) {
        if (entry == null) return; // header row

        // width of the "Ignore" button (text + 10), so "Delete" can sit right next to it
        String ignoreLabel = StringUtils.translate("litematica.gui.button.material_list.ignore", new Object[0]);
        int ignoreWidth = StringUtils.getStringWidth(ignoreLabel) + 10;
        int xRight = x + width - ignoreWidth - 2;

        ButtonGeneric delete = new ButtonGeneric(xRight, y + 1, -1, true, "Delete", new Object[0]);
        this.addButton(delete, (button, mouseButton) -> {
            LplusCommand.deleteForItem(entry.getStack());
            listWidget.refreshEntries();
        });
    }
}
