package retr0.carrotconfig.entries;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget.Entry;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractConfigEntry extends Entry<AbstractConfigEntry> {
    protected final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    protected final MutableText name;
    protected final List<OrderedText> tooltip;

    public AbstractConfigEntry(MutableText name) {
        this.name = name;
        this.tooltip = new ArrayList<>();
    }

    public void tick() { }

    public List<? extends net.minecraft.text.OrderedText> getTooltip() {
        return tooltip;
    }

    @Override public List<? extends Selectable> selectableChildren() { return List.of(); }
    @Override public List<? extends Element> children() { return List.of(); }
}
