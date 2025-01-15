package de.corvonn.betterFoodHud.v1_17_1.mixins;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.corvonn.betterFoodHud.utils.SaturationRenderer;
import de.corvonn.betterFoodHud.utils.Utils;
import net.labymod.api.Laby;
import net.labymod.api.client.gfx.GFXBridge;
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
    private Minecraft minecraft;

    @Shadow
    @Final
    private Random random;

    @Inject(method = "renderPlayerHealth", at = @At("HEAD"), cancellable = true)
    private void renderPlayerHealth(PoseStack poseStack, CallbackInfo ci) {
        if(!Utils.isAddOnEnabled()) return;

        Player player = this.getCameraPlayer();
        if (player != null) {
            Stack stack = Stack.create(poseStack);

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


            int lvt_3_1_ = Mth.ceil(player.getHealth());
            boolean lvt_4_1_ = this.healthBlinkTime > (long)this.tickCount && (this.healthBlinkTime - (long)this.tickCount) / 3L % 2L == 1L;
            long lvt_5_1_ = Util.getMillis();
            if (lvt_3_1_ < this.lastHealth && player.invulnerableTime > 0) {
                this.lastHealthTime = lvt_5_1_;
                this.healthBlinkTime = (long)(this.tickCount + 20);
            } else if (lvt_3_1_ > this.lastHealth && player.invulnerableTime > 0) {
                this.lastHealthTime = lvt_5_1_;
                this.healthBlinkTime = (long)(this.tickCount + 10);
            }

            if (lvt_5_1_ - this.lastHealthTime > 1000L) {
                this.lastHealth = lvt_3_1_;
                this.displayHealth = lvt_3_1_;
                this.lastHealthTime = lvt_5_1_;
            }

            this.lastHealth = lvt_3_1_;
            int lvt_7_1_ = this.displayHealth;
            this.random.setSeed((long)(this.tickCount * 312871));
            FoodData lvt_8_1_ = player.getFoodData();
            int lvt_9_1_ = lvt_8_1_.getFoodLevel();
            int lvt_10_1_ = this.screenWidth / 2 - 91;
            int lvt_11_1_ = this.screenWidth / 2 + 91;
            int lvt_12_1_ = this.screenHeight - 39;
            float lvt_13_1_ = Math.max((float)player.getAttributeValue(Attributes.MAX_HEALTH), (float)Math.max(lvt_7_1_, lvt_3_1_));
            int lvt_14_1_ = Mth.ceil(player.getAbsorptionAmount());
            int lvt_15_1_ = Mth.ceil((lvt_13_1_ + (float)lvt_14_1_) / 2.0F / 10.0F);
            int lvt_16_1_ = Math.max(10 - (lvt_15_1_ - 2), 3);
            int lvt_17_1_ = lvt_12_1_ - (lvt_15_1_ - 1) * lvt_16_1_ - 10;
            int lvt_18_1_ = lvt_12_1_ - 10;
            int lvt_19_1_ = player.getArmorValue();
            int lvt_20_1_ = -1;
            if (player.hasEffect(MobEffects.REGENERATION)) {
                lvt_20_1_ = this.tickCount % Mth.ceil(lvt_13_1_ + 5.0F);
            }

            this.minecraft.getProfiler().push("armor");

            int lvt_22_1_;
            for(int lvt_21_1_ = 0; lvt_21_1_ < 10; ++lvt_21_1_) {
                if (lvt_19_1_ > 0) {
                    lvt_22_1_ = lvt_10_1_ + lvt_21_1_ * 8;
                    if (lvt_21_1_ * 2 + 1 < lvt_19_1_) {
                        this.blit(poseStack, lvt_22_1_, lvt_17_1_, 34, 9, 9, 9);
                    }

                    if (lvt_21_1_ * 2 + 1 == lvt_19_1_) {
                        this.blit(poseStack, lvt_22_1_, lvt_17_1_, 25, 9, 9, 9);
                    }

                    if (lvt_21_1_ * 2 + 1 > lvt_19_1_) {
                        this.blit(poseStack, lvt_22_1_, lvt_17_1_, 16, 9, 9, 9);
                    }
                }
            }

            this.minecraft.getProfiler().popPush("health");
            this.betterFoodHUD$renderHearts(poseStack, player, lvt_10_1_, lvt_12_1_, lvt_16_1_, lvt_20_1_, lvt_13_1_, lvt_3_1_, lvt_7_1_, lvt_14_1_, lvt_4_1_, calculatedHealing);
            LivingEntity lvt_21_2_ = this.getPlayerVehicleWithHealth();
            lvt_22_1_ = this.getVehicleMaxHearts(lvt_21_2_);
            int lvt_23_1_;
            int lvt_24_1_;
            int lvt_25_1_;
            int lvt_26_1_;
            int lvt_27_1_;
            if (lvt_22_1_ == 0) {
                this.minecraft.getProfiler().popPush("food");

                for(lvt_23_1_ = 0; lvt_23_1_ < 10; ++lvt_23_1_) {
                    lvt_24_1_ = lvt_12_1_;
                    lvt_25_1_ = 16;
                    lvt_26_1_ = 0;
                    if (player.hasEffect(MobEffects.HUNGER)) {
                        lvt_25_1_ += 36;
                        lvt_26_1_ = 13;
                    }

                    if (player.getFoodData().getSaturationLevel() <= 0.0F && this.tickCount % (lvt_9_1_ * 3 + 1) == 0) {
                        lvt_24_1_ += this.random.nextInt(3) - 1;
                    }

                    lvt_27_1_ = lvt_11_1_ - lvt_23_1_ * 8 - 9;
                    this.blit(poseStack, lvt_27_1_, lvt_24_1_, 16 + lvt_26_1_ * 9, 27, 9, 9);
                    if (lvt_23_1_ * 2 + 1 < lvt_9_1_) {
                        this.blit(poseStack, lvt_27_1_, lvt_24_1_, lvt_25_1_ + 36, 27, 9, 9);
                    }

                    if (lvt_23_1_ * 2 + 1 == lvt_9_1_) {
                        this.blit(poseStack, lvt_27_1_, lvt_24_1_, lvt_25_1_ + 45, 27, 9, 9);
                    }

                    //AddOn Methods


                    if(Utils.showFoodIncrement() && foodLevelAfterEating > player.getFoodData().getFoodLevel()) {
                        GFXBridge bridge = Laby.gfx();
                        bridge.color4f(1, 1, 1, Utils.getBlinkingOpacity());
                        if (lvt_23_1_ * 2 + 1 < foodLevelAfterEating) {
                            this.blit(poseStack, lvt_27_1_, lvt_24_1_, lvt_25_1_ + 36, 27, 9, 9);
                        }

                        if (lvt_23_1_ * 2 + 1 == foodLevelAfterEating) {
                            this.blit(poseStack, lvt_27_1_, lvt_24_1_, lvt_25_1_ + 45, 27, 9, 9);
                        }
                        bridge.color4f(1, 1, 1, 1);
                    }

                    SaturationRenderer.INSTANCE.applyRenderJob(lvt_27_1_, lvt_24_1_, player.getFoodData().getSaturationLevel(), saturationValueOfItem);
                    RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
                }

                SaturationRenderer.INSTANCE.flush(stack);

                lvt_18_1_ -= 10;
            }

            this.minecraft.getProfiler().popPush("air");
            lvt_23_1_ = player.getMaxAirSupply();
            lvt_24_1_ = Math.min(player.getAirSupply(), lvt_23_1_);
            if (player.isEyeInFluid(FluidTags.WATER) || lvt_24_1_ < lvt_23_1_) {
                lvt_25_1_ = this.getVisibleVehicleHeartRows(lvt_22_1_) - 1;
                lvt_18_1_ -= lvt_25_1_ * 10;
                lvt_26_1_ = Mth.ceil((double)(lvt_24_1_ - 2) * 10.0 / (double)lvt_23_1_);
                lvt_27_1_ = Mth.ceil((double)lvt_24_1_ * 10.0 / (double)lvt_23_1_) - lvt_26_1_;

                for(int lvt_28_1_ = 0; lvt_28_1_ < lvt_26_1_ + lvt_27_1_; ++lvt_28_1_) {
                    if (lvt_28_1_ < lvt_26_1_) {
                        this.blit(poseStack, lvt_11_1_ - lvt_28_1_ * 8 - 9, lvt_18_1_, 16, 18, 9, 9);
                    } else {
                        this.blit(poseStack, lvt_11_1_ - lvt_28_1_ * 8 - 9, lvt_18_1_, 25, 18, 9, 9);
                    }
                }
            }

            this.minecraft.getProfiler().pop();
        }

        ci.cancel();
    }

    @Unique
    private void betterFoodHUD$renderHearts(PoseStack poseStack, Player player, int lvt_3_1_, int lvt_4_1_,
        int lvt_5_1_, int lvt_6_1_, float lvt_7_1_, int ceiledHealth, int displayHealth, int lvt_10_1_, boolean lvt_11_1_,
        int calculatedHealing) {
        HeartType lvt_12_1_ = Gui.HeartType.forPlayer(player);
        int lvt_13_1_ = 9 * (player.level.getLevelData().isHardcore() ? 5 : 0);
        int lvt_14_1_ = Mth.ceil((double)lvt_7_1_ / 2.0);
        int lvt_15_1_ = Mth.ceil((double)lvt_10_1_ / 2.0);
        int lvt_16_1_ = lvt_14_1_ * 2;

        for(int lvt_17_1_ = lvt_14_1_ + lvt_15_1_ - 1; lvt_17_1_ >= 0; --lvt_17_1_) {
            int lvt_18_1_ = lvt_17_1_ / 10;
            int lvt_19_1_ = lvt_17_1_ % 10;
            int lvt_20_1_ = lvt_3_1_ + lvt_19_1_ * 8;
            int lvt_21_1_ = lvt_4_1_ - lvt_18_1_ * lvt_5_1_;
            if (ceiledHealth + lvt_10_1_ <= 4) {
                lvt_21_1_ += this.random.nextInt(2);
            }

            if (lvt_17_1_ < lvt_14_1_ && lvt_17_1_ == lvt_6_1_) {
                lvt_21_1_ -= 2;
            }

            this.betterFoodHUD$renderHeart(poseStack, Gui.HeartType.CONTAINER, lvt_20_1_, lvt_21_1_, lvt_13_1_, lvt_11_1_, false);
            int lvt_22_1_ = lvt_17_1_ * 2;
            boolean lvt_23_1_ = lvt_17_1_ >= lvt_14_1_;
            if (lvt_23_1_) {
                int lvt_24_1_ = lvt_22_1_ - lvt_16_1_;
                if (lvt_24_1_ < lvt_10_1_) {
                    boolean lvt_25_1_ = lvt_24_1_ + 1 == lvt_10_1_;
                    this.betterFoodHUD$renderHeart(poseStack, lvt_12_1_ == Gui.HeartType.WITHERED ? lvt_12_1_ : Gui.HeartType.ABSORBING, lvt_20_1_, lvt_21_1_, lvt_13_1_, false, lvt_25_1_);
                }
            }

            boolean renderHalfHeart;
            if (lvt_11_1_ && lvt_22_1_ < displayHealth) {
                renderHalfHeart = lvt_22_1_ + 1 == displayHealth;
                this.betterFoodHUD$renderHeart(poseStack, lvt_12_1_, lvt_20_1_, lvt_21_1_, lvt_13_1_, true, renderHalfHeart);
            }

            if (lvt_22_1_ < ceiledHealth) {
                renderHalfHeart = lvt_22_1_ + 1 == ceiledHealth;
                this.betterFoodHUD$renderHeart(poseStack, lvt_12_1_, lvt_20_1_, lvt_21_1_, lvt_13_1_, false, renderHalfHeart);
            }

            //AddOnMethod
            if(Utils.showEstimatedHealthIncrement() && calculatedHealing != 0) {
                GFXBridge bridge = Laby.gfx();
                bridge.color4f(1, 1, 1, Utils.getBlinkingOpacity());
                int healthAfterHealing = calculatedHealing + displayHealth;
                if (lvt_11_1_ && lvt_22_1_ < healthAfterHealing) {
                    renderHalfHeart = lvt_22_1_ + 1 == healthAfterHealing;
                    this.betterFoodHUD$renderHeart(poseStack, lvt_12_1_, lvt_20_1_, lvt_21_1_, lvt_13_1_, true, renderHalfHeart);
                }

                if (lvt_22_1_ < healthAfterHealing) {
                    renderHalfHeart = lvt_22_1_ + 1 == healthAfterHealing;
                    this.betterFoodHUD$renderHeart(poseStack, lvt_12_1_, lvt_20_1_, lvt_21_1_, lvt_13_1_, false, renderHalfHeart);
                }
                bridge.color4f(1, 1, 1, 1);
            }
        }

    }



    @Unique //Workaround, da LabyMod in dieser Methode ein Mixin hat, was das Rendern des Alphakanals stört (1.7 Herzen werden dadurch deaktiviert)
    private void betterFoodHUD$renderHeart(PoseStack lvt_1_1_, HeartType lvt_2_1_, int lvt_3_1_, int lvt_4_1_, int lvt_5_1_, boolean lvt_6_1_, boolean lvt_7_1_) {
        this.blit(lvt_1_1_, lvt_3_1_, lvt_4_1_, lvt_2_1_.getX(lvt_7_1_, lvt_6_1_), lvt_5_1_, 9, 9);
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
