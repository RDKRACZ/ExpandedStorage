package ninjaphenix.expandedstorage.base.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import ninjaphenix.expandedstorage.base.IntBiPredicate;
import ninjaphenix.expandedstorage.base.client.gui.widget.ScreenPickButton;
import ninjaphenix.expandedstorage.base.internal_api.Utils;
import ninjaphenix.expandedstorage.base.wrappers.ConfigWrapper;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public final class PickScreen extends Screen {
    private static final Map<ResourceLocation, Triple<ResourceLocation, Component, IntBiPredicate>> BUTTON_SETTINGS = new HashMap<>();
    private final Set<ResourceLocation> options;
    private final Screen returnToScreen;
    private final List<ScreenPickButton> optionWidgets;
    private final Consumer<ResourceLocation> onOptionPicked;
    private int topPadding;

    public PickScreen(Set<ResourceLocation> options, Screen returnToScreen, Consumer<ResourceLocation> onOptionPicked) {
        super(new TranslatableComponent("screen.expandedstorage.screen_picker_title"));
        this.options = options;
        this.optionWidgets = new ArrayList<>(options.size());
        this.onOptionPicked = onOptionPicked;
        this.returnToScreen = returnToScreen;
    }

    public static void declareButtonSettings(ResourceLocation screenType, ResourceLocation texture, Component text, IntBiPredicate warnTest) {
        PickScreen.BUTTON_SETTINGS.putIfAbsent(screenType, Triple.of(texture, text, warnTest));
    }

    @Override
    public void onClose() {
        //noinspection ConstantConditions
        minecraft.setScreen(returnToScreen);
    }

    @Override
    public boolean isPauseScreen() {
        //noinspection ConstantConditions
        return minecraft.level == null;
    }

    @Override
    protected void init() {
        super.init();
        int choices = options.size();
        int columns = Math.min(Mth.intFloorDiv(width, 96), choices);
        int innerPadding = Math.min((width - columns * 96) / (columns + 1), 20); // 20 is smallest gap for any screen.
        int outerPadding = (width - (((columns - 1) * innerPadding) + (columns * 96))) / 2;
        int x = 0;
        int topPadding = (height - 96) / 2;
        this.topPadding = topPadding;
        optionWidgets.clear();
        for (ResourceLocation option : options) {
            Triple<ResourceLocation, Component, IntBiPredicate> settings = PickScreen.BUTTON_SETTINGS.get(option);
            boolean warn = settings.getRight().test(width, height);
            Button.OnTooltip tooltip;
            if (warn) {
                tooltip = new Button.OnTooltip() {
                    final Component warnText = Utils.translation("screen.expandedstorage.off_screen_warning").withStyle(ChatFormatting.GRAY);
                    @Override
                    public void onTooltip(Button button, PoseStack stack, int x, int y) {
                        PickScreen.this.renderTooltip(stack, List.of(button.getMessage(), warnText), Optional.empty(), x, y);
                    }

                    @Override
                    public void narrateTooltip(Consumer<Component> consumer) {
                        consumer.accept(warnText);
                    }
                };
            } else {
                tooltip = (button, stack, x1, y1) -> PickScreen.this.renderTooltip(stack, button.getMessage(), x1, y1);
            }
            optionWidgets.add(this.addRenderableWidget(new ScreenPickButton(outerPadding + (innerPadding + 96) * x, topPadding, 96, 96,
                    settings.getLeft(), settings.getMiddle(), warn, button -> this.updatePlayerPreference(option), tooltip)));
            x++;
        }
    }

    private void updatePlayerPreference(ResourceLocation selection) {
        ConfigWrapper.getInstance().setPreferredScreenType(selection);
        onOptionPicked.accept(selection);
        this.onClose();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, delta);
        optionWidgets.forEach(button -> button.renderTooltip(stack, mouseX, mouseY));
        GuiComponent.drawCenteredString(stack, font, title, width / 2, Math.max(topPadding / 2, 0), 0xFFFFFFFF);
    }
}
