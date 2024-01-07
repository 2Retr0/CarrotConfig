package retr0.carrotconfig.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import retr0.carrotconfig.CarrotConfigClient;
import retr0.carrotconfig.entries.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class CarrotConfigScreen extends Screen {
    public static final Identifier SEARCH_TEXTURE = new Identifier("textures/gui/sprites/icon/search.png");

    public final Screen parent;
    public final String modId;
    public ButtonWidget doneButton;
    private final Set<String> invalidEntries = new HashSet<>();

    public ConfigEntryList entryList;
    public List<CarrotConfig.EntryInfo> entries;
    private final Map<AbstractConfigEntry, Field> entryMap = new HashMap<>();

    protected CarrotConfigScreen(Screen parent, String modId, List<CarrotConfig.EntryInfo> entries) {
        super(Text.translatable(modId + ".carrotconfig.title").formatted(Formatting.BOLD, Formatting.GRAY));

        this.parent = parent;
        this.modId = modId;
        this.entries = entries;
    }



    @Override
    protected final void init() {
        super.init();
        // Launch thread to watch for external changes

        // --- UI initialization ---
        entryList = new ConfigEntryList(this, width, height - 64, 32,height - 32, 25);

        entries.forEach(entryInfo -> {
            AbstractConfigEntry entry;
            try {
                var translationKey = entryInfo.translationKey();
                var defaultValue = entryInfo.defaultValue();
                var value = entryInfo.field().get(null);

                if (defaultValue instanceof Integer defaultInt)
                    entry = createIntEntry(translationKey, defaultInt, (int) value, entryInfo.isColor());
                else if (defaultValue instanceof Float defaultFloat)
                    entry = createFloatEntry(translationKey, defaultFloat, (float) value);
                else if (defaultValue instanceof Boolean defaultBoolean)
                    entry = createBooleanEntry(translationKey, defaultBoolean, (boolean) value);
                else if (entryInfo.field().isAnnotationPresent(CarrotConfig.Comment.class))
                    entry = createComment(translationKey);
                else if (defaultValue instanceof HashMap<?, ?> defaultMap)
                    return;
                else
                    throw new IllegalStateException("Failed to deserialize entry: " + defaultValue);

                entryMap.put(entry, entryInfo.field());
                entryList.addEntry(entry);
            } catch (Exception e) {
                CarrotConfigClient.LOGGER.error(e.getMessage());
            }
        });
        //        entryList.addEntry(createMapEntry("test", 0.0f, 0.0f));
        addDrawableChild(entryList);

        // --- Add Done and Cancel Buttons ---
        int headerX = width / 2 - 155, headerY = height - 28;
        this.doneButton = this.addDrawableChild(new ButtonWidget.Builder(ScreenTexts.DONE, button -> {
            entryMap.forEach((entry, field) -> {
                try {
                    if (entry instanceof ConfigEntry configEntry)
                        field.set(null, configEntry.getValue());
                } catch (IllegalAccessException ignored) { }
            });
            CarrotConfig.writeConfig(modId);
            Objects.requireNonNull(client).setScreen(parent);
        }).dimensions(headerX, headerY, 150, 20).build());

        this.addDrawableChild(new ButtonWidget.Builder(ScreenTexts.CANCEL, button -> {
            Objects.requireNonNull(client).setScreen(parent);
        }).dimensions(headerX + 160, headerY, 150, 20).build());
    }



    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackgroundTexture(context);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 0xFFFFFF);
    }



    @Override
    public void tick() {
        entryList.tick();
    }



    public void updateEntryValidity(String entryKey, boolean isValid) {
        if (isValid)
            invalidEntries.remove(entryKey);
        else
            invalidEntries.add(entryKey);

        this.doneButton.active = this.invalidEntries.isEmpty();
    }


    public ConfigCategoryEntry createComment(String key) {
        return new ConfigCategoryEntry(key);
    }


    public ConfigTextEntry createIntEntry(String key, int defaultValue, int initialValue, boolean isColor) {
        final var intParser = createParser(
            isColor ? value -> (Object) Integer.parseInt(value.substring(1), 16) : Integer::parseInt,
            isValid -> updateEntryValidity(key, isValid));
        final Function<Object, Text> intTextProvider = isColor ?
            value -> Text.literal("#" + Integer.toHexString((int) value).toUpperCase()) :
            value -> Text.literal(String.valueOf(value));

        var textEntry = new ConfigTextEntry(key, width, defaultValue, initialValue, intTextProvider, intParser);
        if (isColor) {
            textEntry.textField.setMaxLength(7);
            textEntry.textField.setTextPredicate(value -> value.startsWith("#"));
            textEntry.textField.setRenderTextProvider((string, index) ->
                OrderedText.styledForwardsVisitedString(string.toUpperCase(), Style.EMPTY));
        }

        return textEntry;
    }



    public ConfigTextEntry createFloatEntry(String key, float defaultValue, float initialValue) {
        final Function<String, Object> floatParser = createParser(
            Float::parseFloat, isValid -> updateEntryValidity(key, isValid));
        final Function<Object, Text> floatTextProvider = value -> Text.literal(String.valueOf((float) value));

        return new ConfigTextEntry(key, width, defaultValue, initialValue, floatTextProvider, floatParser);
    }



    public ConfigEntry createBooleanEntry(String key, boolean defaultValue, boolean initialValue) {
        final Function<Object, Text> textProvider = value -> ((boolean) value ? ScreenTexts.YES : ScreenTexts.NO)
            .copy().formatted((boolean) value ? Formatting.GREEN : Formatting.RED);

        var buttonEntry = new ConfigEntry(key, width, defaultValue, initialValue, textProvider);

        buttonEntry.children().add(new ButtonWidget.Builder(textProvider.apply(initialValue), button -> {
            buttonEntry.setValue(!(boolean) buttonEntry.getValue());
            button.setMessage(textProvider.apply(buttonEntry.getValue()));
        }).dimensions(width - 160, 0, 150, 20).build());

        return buttonEntry;
    }



    private static <T> Function<String, T> createParser(Function<String, T> validator, Consumer<Boolean> callback) {
        return string -> {
            T parsedValue = null;
            try {
                parsedValue = validator.apply(string);
            } catch (Exception ignored) { }
            callback.accept(parsedValue != null);
            return parsedValue;
        };
    }
}
