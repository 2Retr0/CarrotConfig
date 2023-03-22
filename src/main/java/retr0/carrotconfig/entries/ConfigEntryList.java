package retr0.carrotconfig.entries;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;
import retr0.carrotconfig.config.CarrotConfigScreen;

public class ConfigEntryList extends ElementListWidget<AbstractConfigEntry> {
    private final CarrotConfigScreen configScreen;
    private AbstractConfigEntry activeEntry;

    public ConfigEntryList(CarrotConfigScreen parent, int width, int height, int top, int bottom, int itemHeight) {
        super(MinecraftClient.getInstance(), width, height, top, bottom, itemHeight);
        configScreen = parent;
    }

    public ConfigEntryList(
        CarrotConfigScreen parent, String translationKey, int width, int height, int top, int bottom, int itemHeight)
    {
        this(parent, width, height, top, bottom, itemHeight);
        super.addEntry(new ConfigCategoryEntry(translationKey));
    }



    public void tick() { children().forEach(AbstractConfigEntry::tick); }



    public int addEntry(AbstractConfigEntry entry) {
        return super.addEntry(entry);
    }



    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        var configEntry = getHoveredEntry();
        configScreen.setTooltip(configEntry != null ? configEntry.tooltip : null);
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
