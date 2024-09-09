package de.corvonn.betterFoodHud.utils;

import de.corvonn.betterFoodHud.BetterFoodHud;
import de.corvonn.betterFoodHud.configs.Config;
import net.labymod.api.Laby;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.client.render.draw.ResourceRenderer;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.generated.ReferenceStorage;
import org.jetbrains.annotations.Nullable;

public class SaturationRenderer {
    public static final SaturationRenderer INSTANCE = new SaturationRenderer();
    private static final ResourceRenderer RESOURCE_RENDERER = Laby.labyAPI().renderPipeline()
        .resourceRenderer();

    private boolean isSaturationLevelEnabled = false;
    private boolean isExpectedSaturationEnabled = false;

    private int printed = 0;

    public void renderNextSaturation(Stack renderStack, int xPos, int yPos, float saturationLevel) {
        this.renderNextSaturation(renderStack, xPos, yPos, Float.MIN_VALUE, saturationLevel);
    }

    public void renderNextSaturation(Stack renderStack, int xPos, int yPos, float saturationValueOfItem, float saturationLevel) {
        float saturationLevelAfterEating = saturationLevel + (saturationValueOfItem == Float.MIN_VALUE ? Utils.getFoodSaturationValue() : saturationValueOfItem);

        Config configuration = BetterFoodHud.getInstance().configuration();
        isSaturationLevelEnabled = configuration.showCurrentSaturation().get();
        isExpectedSaturationEnabled = configuration.showSaturationIncrement().get();

        boolean renderExpectedSaturation = isExpectedSaturationEnabled && saturationLevel != saturationLevelAfterEating;
        if(isSaturationLevelEnabled || renderExpectedSaturation) render(renderStack, saturationLevel, xPos, yPos, 1);
        if(renderExpectedSaturation) {
            render(renderStack, saturationLevelAfterEating, xPos, yPos, Utils.getBlinkingOpacity());
        }
        printed++;
    }

    private void render(Stack renderStack, float sat, int xPos, int yPos, float opacity) {
        Print print = Print.NONE;
        if(printed * 2 + 1.0 < sat) print = Print.FULL;
        //else if(printed * 2 + 1.5 < saturationLevel) print = Print.THREE_QUARTERS;
        else if(printed * 2 < sat) print = Print.TWO_QUARTERS;
        //else if(printed * 2 + 0.5 < saturationLevel) print = Print.ONE_QUARTERS;

        if(print == Print.NONE || opacity < 0.01f) return;

        RESOURCE_RENDERER.texture(print.getLocation()).pos(xPos, yPos, xPos + 9, yPos + 9)
            .sprite(0, 0, 256, 256)
            .color(1f, 1f, 1f, opacity)
            .render(renderStack);
    }

    public void resetPrinted() {
        this.printed = 0;
    }



    private enum Print {
        FULL(ResourceLocation.create(Utils.NAMESPACE, "textures/gui/sprites/saturation_full.png")),
        THREE_QUARTERS(ResourceLocation.create(Utils.NAMESPACE, "textures/gui/sprites/saturation_threequarters.png")),
        TWO_QUARTERS(ResourceLocation.create(Utils.NAMESPACE, "textures/gui/sprites/saturation_twoquarters.png")),
        ONE_QUARTERS(ResourceLocation.create(Utils.NAMESPACE, "textures/gui/sprites/saturation_onequarters.png")),
        NONE(null);

        private final ResourceLocation location;

        Print(ResourceLocation location) {
            this.location = location;
        }

        public ResourceLocation getLocation() {
            return location;
        }
    }
}
