package retr0.carrotconfig.entries;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.Function;

public class ConfigTextEntry extends ConfigEntry {
    public final TextFieldWidget textField;
    private final String defaultString;

    public ConfigTextEntry(
        String translationKey, int width, Object defaultValue, Object initialValue, Function<Object, Text> textProvider,
        Function<String, Object> validator)
    {
        super(translationKey, width, defaultValue, initialValue, textProvider);

        defaultString = textProvider.apply(defaultValue).getString();
        textField = new TextFieldWidget(textRenderer, width - 160, 0, 150, 20, null);

        textField.setText(textProvider.apply(initialValue).getString());
        textField.setChangedListener(value -> {
            var parsedValue = validator.apply(value);

            if (parsedValue != null) setValue(parsedValue);
            textField.setEditableColor(parsedValue != null ? 0xFFFFFF : 0xFF5555);
        });
        children().add(textField);
    }



    @Override
    public void render(
        DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY,
        boolean hovered, float tickDelta)
    {
        super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);

        // We add a square character at the end of the text field to give visual feedback on the last valid color.
        if (defaultString.startsWith("#"))
            context.drawTextWithShadow(textRenderer, Text.literal("⬛"), width - 22, y + 5, (int) getValue());
    }
}
