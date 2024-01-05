package retr0.carrotconfig.entries;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ElementListWidget;
import retr0.carrotconfig.config.CarrotConfigScreen;

public class ConfigEntryList extends ElementListWidget<AbstractConfigEntry> {
    private final CarrotConfigScreen configScreen;
    private AbstractConfigEntry activeEntry;

    public ConfigEntryList(CarrotConfigScreen parent, int width, int height, int top, int bottom, int itemHeight) {
        super(MinecraftClient.getInstance(), width, height, top, bottom, itemHeight);
        configScreen = parent;
    }



    public void tick() { children().forEach(AbstractConfigEntry::tick); }



    public int addEntry(AbstractConfigEntry entry) {
        return super.addEntry(entry);
    }



    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        var configEntry = getHoveredEntry();

        if (configEntry != null) configScreen.setTooltip(configEntry.tooltip);
    }



    /**
     * Deselects any entries which are not entry at the click position (consequently deselects <em>all</em> entries
     * if no entries exist at the click position).
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        var hoveredEntry = getHoveredEntry();

        if (activeEntry != hoveredEntry && activeEntry instanceof ConfigTextEntry textEntry)
            textEntry.textField.setFocused(false);
        activeEntry = hoveredEntry;

        return super.mouseClicked(mouseX, mouseY, button);
    }


    @Override public int getScrollbarPositionX() { return width - 7; }

    @Override public int getRowWidth() { return 10000; }
}
