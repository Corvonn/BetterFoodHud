package de.corvonn.betterFoodHud.utils;

import de.corvonn.betterFoodHud.BetterFoodHud;
import net.labymod.api.Laby;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.client.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class SaturationRenderer {
    private int printed = 0;
    private final float saturationLevel;
    private final float saturationLevelAfterEating;

    private boolean isSaturationLevelEnabled = false;
    private boolean isExpectedSaturationEnabled = false;


    public SaturationRenderer(@Nullable Float saturationValueOfItem) { //Für Version <= 1.20.4
        ClientPlayer player = Laby.labyAPI().minecraft().getClientPlayer();
        if(player == null) throw new RuntimeException("Client player is null!");

        saturationLevel = player.foodData().getSaturationLevel();
        if(saturationValueOfItem == null) saturationLevelAfterEating = saturationLevel + Utils.getFoodSaturationValue();
        else saturationLevelAfterEating = saturationValueOfItem + saturationLevel;

        BetterFoodHud instance = BetterFoodHud.getInstance();
        if (instance != null) {
            isSaturationLevelEnabled = instance.configuration().enabled().get() && instance.configuration().showCurrentSaturation().get();
            isExpectedSaturationEnabled = instance.configuration().enabled().get() && instance.configuration().showSaturationIncrement().get();
        }
    }

    public SaturationRenderer() { //Für Version >= 1.20.5
        this(null);
    }


    public void renderNextSaturation(Stack renderStack, int xPos, int yPos) {
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

        Laby.references().renderPipeline().resourceRenderer().texture(print.getLocation()).pos(xPos, yPos, xPos + 9, yPos + 9)
            .sprite(0, 0, 256, 256)
            .color(1f, 1f, 1f, opacity)
            .render(renderStack);
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
