package de.corvonn.betterFoodHud.v1_18_2.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.corvonn.betterFoodHud.utils.SaturationRenderer;
import de.corvonn.betterFoodHud.utils.Utils;
import net.labymod.api.Laby;
import net.labymod.api.client.render.gl.GlStateBridge;
import net.labymod.api.client.render.matrix.Stack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.Gui.HeartType;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Random;

@Mixin(Gui.class)
public abstract class MixinGui extends GuiComponent {

    @Shadow
    private int tickCount;

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
    private Random random;

    @Shadow
    @Final
    private Minecraft minecraft;


    @Inject(method = "renderPlayerHealth", at = @At("HEAD"), cancellable = true)
    private void renderPlayerHealth(PoseStack poseStack, CallbackInfo ci) {
        if(!Utils.isAddOnEnabled()) return;

        Player player = this.getCameraPlayer();
        if (player != null) {

            Stack stack = Stack.create(poseStack);
            //AddOn
            int nutritionValueOfItem = 0;
            float saturationValueOfItem = 0;

            ItemStack itemStack = player.getMainHandItem();
            Item item = itemStack.getItem();
            FoodProperties properties = item.getFoodProperties();
            if(properties != null) {
                saturationValueOfItem = properties.getSaturationModifier() * properties.getNutrition() * 2;
                nutritionValueOfItem = properties.getNutrition();
            }

            int calculatedHealing = Utils.calculateHealing(saturationValueOfItem, nutritionValueOfItem, true);

            int foodLevel = player.getFoodData().getFoodLevel();
            int foodLevelAfterEating = foodLevel + nutritionValueOfItem;
            //End AddOn

            int $$2 = Mth.ceil(player.getHealth());
            boolean $$3 = this.healthBlinkTime > (long)this.tickCount && (this.healthBlinkTime - (long)this.tickCount) / 3L % 2L == 1L;
            long $$4 = Util.getMillis();
            if ($$2 < this.lastHealth && player.invulnerableTime > 0) {
                this.lastHealthTime = $$4;
                this.healthBlinkTime = this.tickCount + 20;
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
            this.random.setSeed(this.tickCount * 312871L);
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
                        this.blit(poseStack, $$20, $$15, 34, 9, 9, 9);
                    }

                    if ($$19 * 2 + 1 == $$17) {
                        this.blit(poseStack, $$20, $$15, 25, 9, 9, 9);
                    }

                    if ($$19 * 2 + 1 > $$17) {
                        this.blit(poseStack, $$20, $$15, 16, 9, 9, 9);
                    }
                }
            }

            this.minecraft.getProfiler().popPush("health");
            this.betterFoodHUD$renderHearts(poseStack, player, $$8, $$10, $$14, $$18, $$11, $$2, $$5, $$12, $$3, calculatedHealing);
            LivingEntity $$21 = this.getPlayerVehicleWithHealth();
            $$20 = this.getVehicleMaxHearts($$21);
            int $$23;
            int $$24;
            int $$25;
            int $$26;
            int $$27;
            if ($$20 == 0) {
                this.minecraft.getProfiler().popPush("food");

                for($$23 = 0; $$23 < 10; ++$$23) {
                    $$24 = $$10;
                    $$25 = 16;
                    $$26 = 0;
                    if (player.hasEffect(MobEffects.HUNGER)) {
                        $$25 += 36;
                        $$26 = 13;
                    }

                    if (player.getFoodData().getSaturationLevel() <= 0.0F && this.tickCount % ($$7 * 3 + 1) == 0) {
                        $$24 += this.random.nextInt(3) - 1;
                    }

                    $$27 = $$9 - $$23 * 8 - 9;
                    this.blit(poseStack, $$27, $$24, 16 + $$26 * 9, 27, 9, 9);
                    if ($$23 * 2 + 1 < $$7) {
                        this.blit(poseStack, $$27, $$24, $$25 + 36, 27, 9, 9);
                    }

                    if ($$23 * 2 + 1 == $$7) {
                        this.blit(poseStack, $$27, $$24, $$25 + 45, 27, 9, 9);
                    }

                    //AddOn

                    if(Utils.showFoodIncrement() && foodLevelAfterEating > player.getFoodData().getFoodLevel()) {
                        GlStateBridge glStateBridge = Laby.references().glStateBridge();
                        glStateBridge.color4f(1, 1, 1, Utils.getBlinkingOpacity());
                        if ($$23 * 2 + 1 < foodLevelAfterEating) {
                            this.blit(poseStack, $$27, $$24, $$25 + 36, 27, 9, 9);
                        }

                        if ($$23 * 2 + 1 == foodLevelAfterEating) {
                            this.blit(poseStack, $$27, $$24, $$25 + 45, 27, 9, 9);
                        }
                        glStateBridge.resetColor();
                    }

                    SaturationRenderer.INSTANCE.renderNextSaturation(stack, $$27, $$24, player.getFoodData().getSaturationLevel(), saturationValueOfItem);

                    RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
                    //End AddOn
                }

                $$16 -= 10;
            }

            this.minecraft.getProfiler().popPush("air");
            $$23 = player.getMaxAirSupply();
            $$24 = Math.min(player.getAirSupply(), $$23);
            if (player.isEyeInFluid(FluidTags.WATER) || $$24 < $$23) {
                $$25 = this.getVisibleVehicleHeartRows($$20) - 1;
                $$16 -= $$25 * 10;
                $$26 = Mth.ceil((double)($$24 - 2) * 10.0 / (double)$$23);
                $$27 = Mth.ceil((double)$$24 * 10.0 / (double)$$23) - $$26;

                for(int $$33 = 0; $$33 < $$26 + $$27; ++$$33) {
                    if ($$33 < $$26) {
                        this.blit(poseStack, $$9 - $$33 * 8 - 9, $$16, 16, 18, 9, 9);
                    } else {
                        this.blit(poseStack, $$9 - $$33 * 8 - 9, $$16, 25, 18, 9, 9);
                    }
                }
            }

            this.minecraft.getProfiler().pop();
        }
        SaturationRenderer.INSTANCE.resetPrinted();

        ci.cancel();
    }

    @Unique
    private void betterFoodHUD$renderHearts(PoseStack $$0, Player $$1, int $$2, int $$3, int $$4, int $$5, float $$6, int $$7, int $$8, int $$9, boolean $$10, int calculatedHealing) {
        HeartType $$11 = Gui.HeartType.forPlayer($$1);
        int $$12 = 9 * ($$1.level.getLevelData().isHardcore() ? 5 : 0);
        int $$13 = Mth.ceil((double)$$6 / 2.0);
        int $$14 = Mth.ceil((double)$$9 / 2.0);
        int $$15 = $$13 * 2;

        for(int $$16 = $$13 + $$14 - 1; $$16 >= 0; --$$16) {
            int $$17 = $$16 / 10;
            int $$18 = $$16 % 10;
            int $$19 = $$2 + $$18 * 8;
            int $$20 = $$3 - $$17 * $$4;
            if ($$7 + $$9 <= 4) {
                $$20 += this.random.nextInt(2);
            }

            if ($$16 < $$13 && $$16 == $$5) {
                $$20 -= 2;
            }

            this.betterFoodHUD$renderHeart($$0, Gui.HeartType.CONTAINER, $$19, $$20, $$12, $$10, false);
            int $$21 = $$16 * 2;
            boolean $$22 = $$16 >= $$13;
            if ($$22) {
                int $$23 = $$21 - $$15;
                if ($$23 < $$9) {
                    boolean $$24 = $$23 + 1 == $$9;
                    this.betterFoodHUD$renderHeart($$0, $$11 == Gui.HeartType.WITHERED ? $$11 : Gui.HeartType.ABSORBING, $$19, $$20, $$12, false, $$24);
                }
            }

            boolean $$26;
            if ($$10 && $$21 < $$8) {
                $$26 = $$21 + 1 == $$8;
                this.betterFoodHUD$renderHeart($$0, $$11, $$19, $$20, $$12, true, $$26);
            }

            if ($$21 < $$7) {
                $$26 = $$21 + 1 == $$7;
                this.betterFoodHUD$renderHeart($$0, $$11, $$19, $$20, $$12, false, $$26);
            }

            //AddOn

            if(Utils.showEstimatedHealthIncrement() && calculatedHealing != 0) {
                GlStateBridge glStateBridge = Laby.references().glStateBridge();
                glStateBridge.color4f(1, 1, 1, Utils.getBlinkingOpacity());
                int healthAfterHealing = calculatedHealing + displayHealth;
                if ($$10 && $$21 < healthAfterHealing) {
                    $$26 = $$21 + 1 == healthAfterHealing;
                    this.betterFoodHUD$renderHeart($$0, $$11, $$19, $$20, $$12, true, $$26);
                }

                if ($$21 < healthAfterHealing) {
                    $$26 = $$21 + 1 == healthAfterHealing;
                    this.betterFoodHUD$renderHeart($$0, $$11, $$19, $$20, $$12, false, $$26);
                }
                glStateBridge.resetColor();
            }

            //End AddOn
        }
    }

    @Unique //Workaround, da LabyMod in dieser Methode ein Mixin hat, was das Rendern des Alphakanals stört (1.7 Herzen werden dadurch deaktiviert)
    private void betterFoodHUD$renderHeart(PoseStack $$0, HeartType $$1, int $$2, int $$3, int $$4, boolean $$5, boolean $$6) {
        this.blit($$0, $$2, $$3, $$1.getX($$6, $$5), $$4, 9, 9);
    }

    @Shadow
    protected abstract Player getCameraPlayer();

    @Shadow
    protected abstract LivingEntity getPlayerVehicleWithHealth();

    @Shadow
    protected abstract int getVehicleMaxHearts(@Nullable LivingEntity $$0);

    @Shadow
    protected abstract int getVisibleVehicleHeartRows(int $$0);


}
