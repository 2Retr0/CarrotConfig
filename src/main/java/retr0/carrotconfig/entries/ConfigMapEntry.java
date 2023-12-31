package retr0.carrotconfig.entries;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import retr0.carrotconfig.config.CarrotConfigScreen;

import java.util.function.Function;

public class ConfigMapEntry extends AbstractConfigEntry {
    private static class MappingEntry extends ConfigEntry {
        public final TextFieldWidget key;
        public final TextFieldWidget value;

        public MappingEntry(
                int width, Object defaultValue, Object initialValue, Function<Object, Text> textProvider,
                Function<String, Object> validator)
        {
            super("", width, defaultValue, initialValue, textProvider);

            key = new TextFieldWidget(textRenderer, width - 160 - 160 - 10, 0, 150, 12, null);
            value = new TextFieldWidget(textRenderer, width - 160, 0, 150, 12, null);

            children().add(key);
            children().add(value);
        }

        @Override
        public void render(
                DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY,
                boolean hovered, float tickDelta)
        {
            super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);

            context.drawCenteredTextWithShadow(textRenderer, "→", width - 160 - 9, y + 3, 0xFFFFFF);
        }
    }

//    public final TextFieldWidget key;
//    public final TextFieldWidget value;
    public final ConfigEntryList list;


    private final String defaultString;

    public ConfigMapEntry(
            String translationKey, int width, Object defaultValue, Object initialValue, Function<Object, Text> textProvider,
            Function<String, Object> validator, CarrotConfigScreen parent)
    {
        super(Text.translatable(translationKey + ".name"));

        defaultString = textProvider.apply(defaultValue).getString();
//        key = new TextFieldWidget(textRenderer, width - 160 - 160 - 10, 0, 150, 12, null);
//        value = new TextFieldWidget(textRenderer, width - 160, 0, 150, 12, null);
        list = new ConfigEntryList(parent, width, 400, 0, 25);


        list.addEntry(new MappingEntry(width, defaultValue, initialValue, textProvider, validator));


//        key.setText(textProvider.apply(initialValue).getString());
//        key.setChangedListener(value -> {
//            var parsedValue = validator.apply(value);
//
//            if (parsedValue != null) setValue(parsedValue);
//            key.setEditableColor(parsedValue != null ? 0xFFFFFF : 0xFF5555);
//        });
//        children().add(key);
//        children().add(value);
//        children().add(list);

        //*** RESET BUTTON SETUP ***//
//        children().set(0, new ButtonWidget.Builder(Text.translatable(MOD_ID + ".reset").formatted(Formatting.RED),
//                button -> {
//                    setValue(defaultValue);
//                    children().subList(1, children().size()).forEach(widget -> {
//                        if (widget instanceof TextFieldWidget textField)
//                            textField.setText(textProvider.apply(defaultValue).getString());
//                        else if (widget instanceof ButtonWidget buttonWidget)
//                            buttonWidget.setMessage(textProvider.apply(defaultValue));
//                    });
//                }).dimensions(width - 205 - 160 - 50, 0, 40, 20).build());

    }



    @Override
    public void render(
            DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY,
            boolean hovered, float tickDelta)
    {
        list.setY(y);
        list.renderWidget(context, mouseX, mouseY, tickDelta);
    }


    @Override
    public void tick() {
        super.tick();
        list.tick();
    }

}
