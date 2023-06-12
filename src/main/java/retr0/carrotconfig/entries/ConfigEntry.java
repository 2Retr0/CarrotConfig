package retr0.carrotconfig.entries;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static retr0.carrotconfig.CarrotConfigClient.MOD_ID;

public class ConfigEntry extends AbstractConfigEntry {
    private final List<ClickableWidget> children = new ArrayList<>();
    protected final Object defaultValue;
    protected final Function<Object, Text> textProvider;
    protected final int width;

    private Object value;

    public ConfigEntry(String translationKey, int width, Object defaultValue, Object initialValue, Function<Object, Text> textProvider) {
        super(Text.translatable(translationKey + ".name"));
        this.defaultValue = defaultValue;
        this.textProvider = textProvider;
        this.width = width;
        this.value = initialValue;

        //*** TOOLTIP SETUP ***//
        // We should add a name, as multi-line tooltips always have a small spacing between lines 1 and 2.
        tooltip.add(name.copy().formatted(Formatting.YELLOW).asOrderedText());

        var descriptionKey = translationKey + ".tooltip";
        if (!Text.translatable(descriptionKey).getString().equals(descriptionKey))
            tooltip.addAll(textRenderer.wrapLines(Text.translatable(descriptionKey), 260));

        // Add a section in the tooltip containing the default value.
        var defaultText = Text.literal(textProvider.apply(defaultValue).getString());
        tooltip.add(Text.translatable("editGamerule.default", defaultText).formatted(Formatting.GRAY).asOrderedText());


        //*** RESET BUTTON SETUP ***//
        children.add(new ButtonWidget.Builder(Text.translatable(MOD_ID + ".reset").formatted(Formatting.RED),
            button -> {
                setValue(defaultValue);
                children.subList(1, children.size()).forEach(widget -> {
                    if (widget instanceof TextFieldWidget textField)
                        textField.setText(textProvider.apply(defaultValue).getString());
                    else if (widget instanceof ButtonWidget buttonWidget)
                        buttonWidget.setMessage(textProvider.apply(defaultValue));
                });
            }).dimensions(width - 205, 0, 40, 20).build());
    }



    public Object getValue() {
        return value;
    }



    public void setValue(Object value) {
        this.value = value;
    }



    @Override
    public void render(
        DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY,
        boolean hovered, float tickDelta)
    {
        children.forEach(widget -> { widget.setY(y); widget.render(context, mouseX, mouseY, tickDelta); });
        context.drawTextWithShadow(textRenderer, name, 12, y + 5, 0xFFFFFF);
    }



    @Override
    public void tick() {
        // Ensure reset button is deactivated if the value is not the default value.
        children.get(0).active = !value.equals(defaultValue);
        // Tick any widgets which are textFieldWidgets to allow input update.
        children.forEach(widget -> { if (widget instanceof TextFieldWidget textField) textField.tick(); });
    }



    @Override public List<? extends Selectable> selectableChildren() { return children; }

    @Override public List<ClickableWidget> children() { return children; }
}
