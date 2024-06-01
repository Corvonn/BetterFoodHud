package de.corvonn.betterFoodHud.v1_20_2.mixins;

import de.corvonn.betterFoodHud.utils.SaturationRenderer;
import de.corvonn.betterFoodHud.utils.Utils;
import net.labymod.api.Laby;
import net.labymod.api.client.render.matrix.Stack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.Gui.HeartType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class MixinGui {

    @Shadow
    protected abstract void renderPlayerHealth(GuiGraphics par1);

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
    @Final
    private static ResourceLocation ARMOR_FULL_SPRITE;

    @Shadow
    @Final
    private static ResourceLocation ARMOR_HALF_SPRITE;

    @Shadow
    @Final
    private static ResourceLocation ARMOR_EMPTY_SPRITE;

    @Shadow
    @Final
    private static ResourceLocation AIR_SPRITE;

    @Shadow
    @Final
    private static ResourceLocation AIR_BURSTING_SPRITE;

    @Shadow
    private int tickCount;

    @Shadow
    @Final
    private RandomSource random;

    @Shadow
    private int screenWidth;

    @Shadow
    private int screenHeight;

    @Shadow
    private long healthBlinkTime;

    @Shadow
    private int lastHealth;

    @Shadow
    private long lastHealthTime;

    @Shadow
    private int displayHealth;

    @Shadow
    @Final
    private Minecraft minecraft;



    @Inject(method = "renderPlayerHealth", at = @At("HEAD"), cancellable = true)
    private void renderPlayerHealthMixin(GuiGraphics guiGraphics, CallbackInfo ci) {
        if(!Utils.isAddOnEnabled()) return;

        Player player = this.getCameraPlayer();
        if (player != null) {
            int $$2 = Mth.ceil(player.getHealth());
            boolean $$3 = this.healthBlinkTime > (long)this.tickCount && (this.healthBlinkTime - (long)this.tickCount) / 3L % 2L == 1L;
            long $$4 = Util.getMillis();
            if ($$2 < this.lastHealth && player.invulnerableTime > 0) {
                this.lastHealthTime = $$4;
                this.healthBlinkTime = (long)(this.tickCount + 20);
            } else if ($$2 > this.lastHealth && player.invulnerableTime > 0) {
                this.lastHealthTime = $$4;
                this.healthBlinkTime = (long)(this.tickCount + 10);
            }

            if ($$4 - this.lastHealthTime > 1000L) {
                this.lastHealth = $$2;
                this.displayHealth = $$2;
                this.lastHealthTime = $$4;
            }

            this.lastHealth = $$2;
            int $$5 = this.displayHealth;
            this.random.setSeed((long)(this.tickCount * 312871));
            FoodData $$6 = player.getFoodData();
            int $$7 = $$6.getFoodLevel();
            int $$8 = this.screenWidth / 2 - 91;
            int $$9 = this.screenWidth / 2 + 91;
            int $$10 = this.screenHeight - 39;
            float $$11 = Math.max((float)player.getAttributeValue(Attributes.MAX_HEALTH), (float)Math.max($$5, $$2));
            int $$12 = Mth.ceil(player.getAbsorptionAmount());
            int $$13 = Mth.ceil(($$11 + (float)$$12) / 2.0F / 10.0F);
            int $$14 = Math.max(10 - ($$13 - 2), 3);
            int $$15 = $$10 - ($$13 - 1) * $$14 - 10;
            int $$16 = $$10 - 10;
            int $$17 = player.getArmorValue();
            int $$18 = -1;
            if (player.hasEffect(MobEffects.REGENERATION)) {
                $$18 = this.tickCount % Mth.ceil($$11 + 5.0F);
            }

            this.minecraft.getProfiler().push("armor");

            int $$20;
            for(int $$19 = 0; $$19 < 10; ++$$19) {
                if ($$17 > 0) {
                    $$20 = $$8 + $$19 * 8;
                    if ($$19 * 2 + 1 < $$17) {
                        guiGraphics.blitSprite(ARMOR_FULL_SPRITE, $$20, $$15, 9, 9);
                    }

                    if ($$19 * 2 + 1 == $$17) {
                        guiGraphics.blitSprite(ARMOR_HALF_SPRITE, $$20, $$15, 9, 9);
                    }

                    if ($$19 * 2 + 1 > $$17) {
                        guiGraphics.blitSprite(ARMOR_EMPTY_SPRITE, $$20, $$15, 9, 9);
                    }
                }
            }

            FoodProperties properties = player.getMainHandItem().getItem().getFoodProperties();
            int nutritionValueOfItem = 0;
            float saturationValueOfItem = 0;
            if(properties != null) {
                nutritionValueOfItem = properties.getNutrition();
                saturationValueOfItem = properties.getSaturationModifier() * properties.getNutrition() * 2;
            }

            this.minecraft.getProfiler().popPush("health");
            this.betterFoodHUD$renderHearts(guiGraphics, player, $$8, $$10, $$14, $$18, $$11, $$2, $$5, $$12, $$3, saturationValueOfItem, nutritionValueOfItem);
            LivingEntity $$21 = this.getPlayerVehicleWithHealth();
            $$20 = this.getVehicleMaxHearts($$21);
            int printed;
            int yPos;
            int xPos;
            if ($$20 == 0) {
                this.minecraft.getProfiler().popPush("food");

                SaturationRenderer saturationRenderer = new SaturationRenderer(saturationValueOfItem);
                int foodLevel = player.getFoodData().getFoodLevel();
                int foodLevelAfterEating = foodLevel + nutritionValueOfItem;

                for(printed = 0; printed < 10; ++printed) {
                    yPos = $$10;
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

                    if (player.getFoodData().getSaturationLevel() <= 0.0F && this.tickCount % ($$7 * 3 + 1) == 0) {
                        yPos += this.random.nextInt(3) - 1;
                    }

                    //Minecraft code

                    xPos = $$9 - printed * 8 - 9;
                    guiGraphics.blitSprite(emptySprite, xPos, yPos, 9, 9);
                    if (printed * 2 + 1 < $$7) {
                        guiGraphics.blitSprite(fullSprite, xPos, yPos, 9, 9);
                    }

                    if (printed * 2 + 1 == $$7) {
                        guiGraphics.blitSprite(halfSprite, xPos, yPos, 9, 9);
                    }

                    //Additional rendering - AddOn (blinking saturation bar)

                    if(Utils.showFoodIncrement() && foodLevelAfterEating != foodLevel) {
                        Laby.references().glStateBridge().color4f(1, 1, 1, Utils.getBlinkingOpacity());
                        if (printed * 2 + 1 < foodLevelAfterEating) {
                            guiGraphics.blitSprite(fullSprite, xPos, yPos, 9, 9);
                        }

                        if (printed * 2 + 1 == foodLevelAfterEating) {
                            guiGraphics.blitSprite(halfSprite, xPos, yPos, 9, 9);
                        }

                        Laby.references().glStateBridge().resetColor();
                    }

                    //Additional rendering - AddOn (golden outlines)

                    saturationRenderer.renderNextSaturation(Stack.create(guiGraphics.pose()), xPos, yPos);

                    //End AddOn
                }

                $$16 -= 10;
            }

            this.minecraft.getProfiler().popPush("air");
            printed = player.getMaxAirSupply();
            yPos = Math.min(player.getAirSupply(), printed);
            if (player.isEyeInFluid(FluidTags.WATER) || yPos < printed) {
                int $$34 = this.getVisibleVehicleHeartRows($$20) - 1;
                $$16 -= $$34 * 10;
                int $$35 = Mth.ceil((double)(yPos - 2) * 10.0 / (double)printed);
                int $$36 = Mth.ceil((double)yPos * 10.0 / (double)printed) - $$35;

                for(xPos = 0; xPos < $$35 + $$36; ++xPos) {
                    if (xPos < $$35) {
                        guiGraphics.blitSprite(AIR_SPRITE, $$9 - xPos * 8 - 9, $$16, 9, 9);
                    } else {
                        guiGraphics.blitSprite(AIR_BURSTING_SPRITE, $$9 - xPos * 8 - 9, $$16, 9, 9);
                    }
                }
            }

            this.minecraft.getProfiler().pop();
        }

        ci.cancel();
    }


    @Unique
    private void betterFoodHUD$renderHearts(GuiGraphics guiGraphics, Player player, int x, int y, int height, int offsetHeartIndex,
        float maxHealth, int playerHealth, int displayHealth, int absorptionAmount, boolean renderHighlight, float saturationValueOfItem, int nutritionValueOfItem) {

        HeartType type = Gui.HeartType.forPlayer(player);
        boolean hardcore = player.level().getLevelData().isHardcore();
        int maxHearts = Mth.ceil((double)maxHealth / 2.0);
        int absorptionHearts = Mth.ceil((double)absorptionAmount / 2.0);
        int maxLivingAmount = maxHearts * 2;
        int calculatedHealing = Utils.calculateHealing(saturationValueOfItem, nutritionValueOfItem);

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

            if(Utils.showEstimatedHealthIncrement() && calculatedHealing != 0) {
                if (currentHeart < calculatedHealing + playerHealth) {
                    Laby.references().glStateBridge().color4f(1, 1, 1, Utils.getBlinkingOpacity());
                    renderHalfHeart = currentHeart + 1 == calculatedHealing + playerHealth;
                    this.renderHeart(guiGraphics, type, xPos, yPos, hardcore, false,
                        renderHalfHeart);
                    Laby.references().glStateBridge().resetColor();
                }
            }

            //End AddOn
        }

    }

    @Shadow
    protected abstract void renderHeart(GuiGraphics $$0, HeartType $$1, int $$2, int $$3,
        boolean $$4, boolean $$5, boolean $$6);

    @Shadow
    protected abstract Player getCameraPlayer();

    @Shadow
    protected abstract LivingEntity getPlayerVehicleWithHealth();

    @Shadow
    protected abstract int getVehicleMaxHearts(@Nullable LivingEntity $$0);

    @Shadow
    protected abstract int getVisibleVehicleHeartRows(int $$0);
}
