package de.corvonn.betterFoodHud.v1_21_3.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import de.corvonn.betterFoodHud.utils.SaturationRenderer;
import de.corvonn.betterFoodHud.utils.Utils;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.client.render.matrix.VanillaStackAccessor;
import net.labymod.api.util.color.format.ColorFormat;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.Gui.HeartType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class MixinGui {

    @Shadow
    @Final
    private static ResourceLocation FOOD_EMPTY_HUNGER_SPRITE;

    @Shadow
    @Final
    private static ResourceLocation FOOD_HALF_HUNGER_SPRITE;

    @Shadow
    @Final
    private static ResourceLocation FOOD_FULL_HUNGER_SPRITE;

    @Shadow
    @Final
    private static ResourceLocation FOOD_EMPTY_SPRITE;

    @Shadow
    @Final
    private static ResourceLocation FOOD_HALF_SPRITE;

    @Shadow
    @Final
    private static ResourceLocation FOOD_FULL_SPRITE;

    @Shadow
    private int tickCount;

    @Shadow
    @Final
    private RandomSource random;

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void mixinRenderFood(GuiGraphics guiGraphics, Player player, int $$2, int $$3, CallbackInfo ci) {
        if(!Utils.isAddOnEnabled()) return;

        FoodData foodData = player.getFoodData();
        int foodLevel = foodData.getFoodLevel();

        int foodLevelAfterEating = foodLevel + Utils.getFoodNutritionValue();
        int blinkingOpacity = ColorFormat.ARGB32.pack(1, 1, 1, Utils.getBlinkingOpacity());

        for(int printed = 0; printed < 10; ++printed) {
            int yPos = $$2;
            ResourceLocation emptySprite;
            ResourceLocation halfSprite;
            ResourceLocation fullSprite;
            if (player.hasEffect(MobEffects.HUNGER)) {
                emptySprite = FOOD_EMPTY_HUNGER_SPRITE;
                halfSprite = FOOD_HALF_HUNGER_SPRITE;
                fullSprite = FOOD_FULL_HUNGER_SPRITE;
            } else {
                emptySprite = FOOD_EMPTY_SPRITE;
                halfSprite = FOOD_HALF_SPRITE;
                fullSprite = FOOD_FULL_SPRITE;
            }

            if (foodData.getSaturationLevel() <= 0.0F && this.tickCount % (foodLevel * 3 + 1) == 0) {
                yPos += this.random.nextInt(3) - 1;
            }

            int xPos = $$3 - printed * 8 - 9;

            //Default rendering - Minecraft

            guiGraphics.blitSprite(RenderType::guiTextured, emptySprite, xPos, yPos, 9, 9);
            if (printed * 2 + 1 < foodLevel) {
                guiGraphics.blitSprite(RenderType::guiTextured, fullSprite, xPos, yPos, 9, 9);
            }

            if (printed * 2 + 1 == foodLevel) {
                guiGraphics.blitSprite(RenderType::guiTextured, halfSprite, xPos, yPos, 9, 9);
            }

            //Additional rendering - AddOn (blinking saturation bar)

            if(Utils.showFoodIncrement() && foodLevelAfterEating != foodLevel) {
                if (printed * 2 + 1 < foodLevelAfterEating) {
                    guiGraphics.blitSprite(RenderType::guiTextured, fullSprite, xPos, yPos, 9, 9, blinkingOpacity);
                }

                if (printed * 2 + 1 == foodLevelAfterEating) {
                    guiGraphics.blitSprite(RenderType::guiTextured, halfSprite, xPos, yPos, 9, 9, blinkingOpacity);
                }
            }

            //Additional rendering - AddOn (golden outlines)

            SaturationRenderer.INSTANCE.applyRenderJob(xPos, yPos, foodData.getSaturationLevel());

            //End AddOn
        }

        guiGraphics.flush();
        SaturationRenderer.INSTANCE.flush(((VanillaStackAccessor) guiGraphics.pose()).stack());
        ci.cancel();
    }



    @Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true)
    private void renderHeartsMixin(GuiGraphics guiGraphics, Player player, int x, int y, int height, int offsetHeartIndex,
        float maxHealth, int playerHealth, int displayHealth, int absorptionAmount, boolean renderHighlight, CallbackInfo ci) {
        if(!Utils.isAddOnEnabled() || !Utils.showEstimatedHealthIncrement()) return;

        HeartType type = Gui.HeartType.forPlayer(player);
        boolean hardcore = player.level().getLevelData().isHardcore();
        int maxHearts = Mth.ceil((double)maxHealth / 2.0);
        int absorptionHearts = Mth.ceil((double)absorptionAmount / 2.0);
        int maxLivingAmount = maxHearts * 2;
        int calculatedHealing = Utils.calculateHealing();

        int blinkingOpacity = ColorFormat.ARGB32.pack(1, 1, 1, Utils.getBlinkingOpacity());
        for(int i = maxHearts + absorptionHearts - 1; i >= 0; --i) {
            int row = i / 10;
            int column = i % 10;
            int xPos = x + column * 8;
            int yPos = y - row * height;
            if (playerHealth + absorptionAmount <= 4) {
                yPos += this.random.nextInt(2);
            }

            if (i < maxHearts && i == offsetHeartIndex) {
                yPos -= 2;
            }

            this.renderHeart(guiGraphics, Gui.HeartType.CONTAINER, xPos, yPos, hardcore, renderHighlight, false);
            int currentHeart = i * 2;
            boolean overflow = i >= maxHearts;
            if (overflow) {
                int remainingHearts = currentHeart - maxLivingAmount;
                if (remainingHearts < absorptionAmount) {
                    boolean $$24 = remainingHearts + 1 == absorptionAmount;
                    this.renderHeart(guiGraphics, type == Gui.HeartType.WITHERED ? type : Gui.HeartType.ABSORBING, xPos, yPos, hardcore, false, $$24);
                }
            }

            boolean renderHalfHeart;
            if (renderHighlight && currentHeart < displayHealth) {
                renderHalfHeart = currentHeart + 1 == displayHealth;
                this.renderHeart(guiGraphics, type, xPos, yPos, hardcore, true, renderHalfHeart);
            }

            if (currentHeart < playerHealth) {
                renderHalfHeart = currentHeart + 1 == playerHealth;
                this.renderHeart(guiGraphics, type, xPos, yPos, hardcore, false, renderHalfHeart);
            }


            //AddOn Method

            if(currentHeart < calculatedHealing + playerHealth && calculatedHealing != 0) {
                renderHalfHeart = currentHeart + 1 == calculatedHealing + playerHealth;
                guiGraphics.blitSprite(RenderType::guiTextured, type.getSprite(hardcore, renderHalfHeart, false), xPos, yPos, 9, 9, blinkingOpacity);
            }

            //End AddOn
        }

        ci.cancel();
    }

    @Shadow
    protected abstract void renderHeart(GuiGraphics par1, HeartType par2, int par3, int par4,
        boolean par5, boolean par6, boolean par7);
}
