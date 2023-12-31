package retr0.carrotconfig.entries;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.function.Function;

public class ConfigCategoryEntry extends AbstractConfigEntry {
    protected static final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    public ConfigCategoryEntry(String translationKey, Function<MutableText, MutableText> formatter) {
        super(formatter.apply(Text.translatable(translationKey + ".text")));
    }

    public ConfigCategoryEntry(String translationKey) {
        this(translationKey, text -> text);
    }

    @Override
    public void render(
        DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY,
        boolean hovered, float tickDelta)
    {
        context.drawCenteredTextWithShadow(textRenderer, this.name, x + entryWidth / 2, y + 5, 0xFFFFFF);
    }
}
