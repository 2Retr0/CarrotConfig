package retr0.carrotconfig.entries;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ConfigCategoryEntry extends AbstractConfigEntry {
    protected static final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    public ConfigCategoryEntry(String translationKey) {
        super(Text.translatable(translationKey + ".name").formatted(Formatting.BOLD, Formatting.YELLOW));
    }

    @Override
    public void render(
        DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY,
        boolean hovered, float tickDelta)
    {
        context.drawCenteredTextWithShadow(textRenderer, this.name, x + entryWidth / 2, y + 5, 0xFFFFFF);
    }
}
