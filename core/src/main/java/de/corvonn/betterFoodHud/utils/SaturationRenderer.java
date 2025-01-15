package de.corvonn.betterFoodHud.utils;

import de.corvonn.betterFoodHud.BetterFoodHud;
import de.corvonn.betterFoodHud.configs.Config;
import net.labymod.api.Laby;
import net.labymod.api.client.render.draw.ResourceRenderer;
import net.labymod.api.client.render.draw.batch.BatchResourceRenderer;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.client.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SaturationRenderer {
    public static final SaturationRenderer INSTANCE = new SaturationRenderer();

    private static final ResourceRenderer RESOURCE_RENDERER = Laby.labyAPI().renderPipeline()
        .resourceRenderer();

    private int printed = 0;

    private final Map<ResourceLocation, List<RenderJob>> jobs = new HashMap<>();

    /**
     * Adds a rendering job to visualize current saturation levels using the provided parameters.
     * <p>
     * <b>The method can only be used from version 1.20.5!</b>
     *
     * @param xPos The x-coordinate where the saturation should be rendered on the screen.
     * @param yPos The y-coordinate where the saturation should be rendered on the screen.
     * @param saturationLevel The current saturation level of the player.
     */
    public void applyRenderJob(int xPos, int yPos, float saturationLevel) {
        applyRenderJob(xPos, yPos, saturationLevel, Float.MIN_VALUE);
    }

    /**
     * Adds rendering jobs to visualize current and expected saturation levels based on given parameters.
     *
     * @param xPos The x-coordinate where the saturation should be rendered on the screen.
     * @param yPos The y-coordinate where the saturation should be rendered on the screen.
     * @param saturationLevel The current saturation level of the player.
     * @param saturationValueOfItem The saturation value associated with the item being consumed. If the value is set to Float.MIN_VALUE, the value is determined automatically <b>if version 1.20.5 or higher is used</b>.
     */
    public void applyRenderJob(int xPos, int yPos, float saturationLevel, float saturationValueOfItem) {
        float saturationLevelAfterEating = saturationLevel + (saturationValueOfItem == Float.MIN_VALUE ? Utils.getFoodSaturationValue() : saturationValueOfItem);

        Config config = BetterFoodHud.getInstance().configuration();
        boolean renderSaturation = config.showCurrentSaturation().get();
        boolean renderExpectedSaturation = config.showSaturationIncrement().get();
        renderExpectedSaturation &= saturationLevel != saturationLevelAfterEating;

        if(renderSaturation || renderExpectedSaturation) {
            addToPipeline(saturationLevel, xPos, yPos, 1);
        }
        if(renderExpectedSaturation) {
            addToPipeline(saturationLevelAfterEating, xPos, yPos, Utils.getBlinkingOpacity());
        }
        this.printed++;
    }

    public void addToPipeline(float saturation, int xPos, int yPos, float opacity) {
        Texture texture = Texture.NONE;
        if(printed * 2 + 1.0 < saturation) texture = Texture.FULL;
        else if(printed * 2 < saturation) texture = Texture.TWO_QUARTERS;

        if(texture == Texture.NONE || opacity < 0.01f) return;

        this.jobs.compute(texture.getLocation(), (t, jobs) -> {
            if(jobs == null) jobs = new ArrayList<>();
            jobs.add(new RenderJob(t, xPos, yPos, opacity));
            return jobs;
        });
    }

    public void flush(Stack renderStack) {
        this.jobs.forEach((resourceLocation, renderJobs) -> {
            if(renderJobs.isEmpty()) return; //Failsafe, but should not happen

            BatchResourceRenderer renderer = RESOURCE_RENDERER.beginBatch(renderStack, resourceLocation);
            for (RenderJob j : renderJobs) {
                renderer.pos(j.xPos, j.yPos, j.xPos + 9, j.yPos + 9)
                    .sprite(0, 0, 256, 256)
                    .color(1f, 1f, 1f, j.opacity)
                    .build();
            }
            renderer.upload();
        });
        this.jobs.clear();
        this.printed = 0;
    }

    private enum Texture {
        FULL(ResourceLocation.create(Utils.NAMESPACE, "textures/gui/sprites/saturation_full.png")),
        THREE_QUARTERS(ResourceLocation.create(Utils.NAMESPACE, "textures/gui/sprites/saturation_threequarters.png")),
        TWO_QUARTERS(ResourceLocation.create(Utils.NAMESPACE, "textures/gui/sprites/saturation_twoquarters.png")),
        ONE_QUARTERS(ResourceLocation.create(Utils.NAMESPACE, "textures/gui/sprites/saturation_onequarters.png")),
        NONE(null);

        private final ResourceLocation location;

        Texture(ResourceLocation location) {
            this.location = location;
        }

        public ResourceLocation getLocation() {
            return location;
        }
    }

    private record RenderJob(ResourceLocation location, int xPos, int yPos, float opacity) {}
}
