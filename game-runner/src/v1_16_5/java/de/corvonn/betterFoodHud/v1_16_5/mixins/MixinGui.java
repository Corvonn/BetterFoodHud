package de.corvonn.betterFoodHud.v1_16_5.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import de.corvonn.betterFoodHud.utils.SaturationRenderer;
import de.corvonn.betterFoodHud.utils.Utils;
import net.labymod.api.Laby;
import net.labymod.api.client.gfx.GFXBridge;
import net.labymod.api.client.render.matrix.Stack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
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
            if(itemStack != null) {
                Item item = itemStack.getItem();
                FoodProperties properties = item.getFoodProperties();
                if(properties != null) {
                    saturationValueOfItem = properties.getSaturationModifier() * properties.getNutrition() * 2;
                    nutritionValueOfItem = properties.getNutrition();
                }
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
            float lvt_13_1_ = (float)player.getAttributeValue(Attributes.MAX_HEALTH);
            int lvt_14_1_ = Mth.ceil(player.getAbsorptionAmount());
            int lvt_15_1_ = Mth.ceil((lvt_13_1_ + (float)lvt_14_1_) / 2.0F / 10.0F);
            int lvt_16_1_ = Math.max(10 - (lvt_15_1_ - 2), 3);
            int lvt_17_1_ = lvt_12_1_ - (lvt_15_1_ - 1) * lvt_16_1_ - 10;
            int lvt_18_1_ = lvt_12_1_ - 10;
            int lvt_19_1_ = lvt_14_1_;
            int lvt_20_1_ = player.getArmorValue();
            int lvt_21_1_ = -1;
            if (player.hasEffect(MobEffects.REGENERATION)) {
                lvt_21_1_ = this.tickCount % Mth.ceil(lvt_13_1_ + 5.0F);
            }

            this.minecraft.getProfiler().push("armor");

            int lvt_22_2_;
            int lvt_23_2_;
            for(lvt_22_2_ = 0; lvt_22_2_ < 10; ++lvt_22_2_) {
                if (lvt_20_1_ > 0) {
                    lvt_23_2_ = lvt_10_1_ + lvt_22_2_ * 8;
                    if (lvt_22_2_ * 2 + 1 < lvt_20_1_) {
                        this.blit(poseStack, lvt_23_2_, lvt_17_1_, 34, 9, 9, 9);
                    }

                    if (lvt_22_2_ * 2 + 1 == lvt_20_1_) {
                        this.blit(poseStack, lvt_23_2_, lvt_17_1_, 25, 9, 9, 9);
                    }

                    if (lvt_22_2_ * 2 + 1 > lvt_20_1_) {
                        this.blit(poseStack, lvt_23_2_, lvt_17_1_, 16, 9, 9, 9);
                    }
                }
            }

            this.minecraft.getProfiler().popPush("health");

            int lvt_24_2_;
            int lvt_25_2_;
            int lvt_26_1_;
            int lvt_27_1_;
            int lvt_28_1_;
            for(lvt_22_2_ = Mth.ceil((lvt_13_1_ + (float)lvt_14_1_) / 2.0F) - 1; lvt_22_2_ >= 0; --lvt_22_2_) {
                lvt_23_2_ = 16;
                if (player.hasEffect(MobEffects.POISON)) {
                    lvt_23_2_ += 36;
                } else if (player.hasEffect(MobEffects.WITHER)) {
                    lvt_23_2_ += 72;
                }

                lvt_24_2_ = 0;
                if (lvt_4_1_) {
                    lvt_24_2_ = 1;
                }

                lvt_25_2_ = Mth.ceil((float)(lvt_22_2_ + 1) / 10.0F) - 1;
                lvt_26_1_ = lvt_10_1_ + lvt_22_2_ % 10 * 8;
                lvt_27_1_ = lvt_12_1_ - lvt_25_2_ * lvt_16_1_;
                if (lvt_3_1_ <= 4) {
                    lvt_27_1_ += this.random.nextInt(2);
                }

                if (lvt_19_1_ <= 0 && lvt_22_2_ == lvt_21_1_) {
                    lvt_27_1_ -= 2;
                }

                lvt_28_1_ = 0;
                if (player.level.getLevelData().isHardcore()) {
                    lvt_28_1_ = 5;
                }

                this.blit(poseStack, lvt_26_1_, lvt_27_1_, 16 + lvt_24_2_ * 9, 9 * lvt_28_1_, 9, 9);
                if (lvt_4_1_) {
                    if (lvt_22_2_ * 2 + 1 < lvt_7_1_) {
                        this.blit(poseStack, lvt_26_1_, lvt_27_1_, lvt_23_2_ + 54, 9 * lvt_28_1_, 9, 9);
                    }

                    if (lvt_22_2_ * 2 + 1 == lvt_7_1_) {
                        this.blit(poseStack, lvt_26_1_, lvt_27_1_, lvt_23_2_ + 63, 9 * lvt_28_1_, 9, 9);
                    }
                }

                if (lvt_19_1_ > 0) {
                    if (lvt_19_1_ == lvt_14_1_ && lvt_14_1_ % 2 == 1) {
                        this.blit(poseStack, lvt_26_1_, lvt_27_1_, lvt_23_2_ + 153, 9 * lvt_28_1_, 9, 9);
                        --lvt_19_1_;
                    } else {
                        this.blit(poseStack, lvt_26_1_, lvt_27_1_, lvt_23_2_ + 144, 9 * lvt_28_1_, 9, 9);
                        lvt_19_1_ -= 2;
                    }
                } else {
                    if (lvt_22_2_ * 2 + 1 < lvt_3_1_) {
                        this.blit(poseStack, lvt_26_1_, lvt_27_1_, lvt_23_2_ + 36, 9 * lvt_28_1_, 9, 9);
                    }

                    if (lvt_22_2_ * 2 + 1 == lvt_3_1_) {
                        this.blit(poseStack, lvt_26_1_, lvt_27_1_, lvt_23_2_ + 45, 9 * lvt_28_1_, 9, 9);
                    }

                    //AddOnMethod
                    if(Utils.showEstimatedHealthIncrement()) {
                        GFXBridge bridge = Laby.gfx();
                        bridge.color4f(1, 1, 1, Utils.getBlinkingOpacity());
                        if (lvt_22_2_ * 2 + 1 < calculatedHealing + player.getHealth()) {
                            this.blit(poseStack, lvt_26_1_, lvt_27_1_, lvt_23_2_ + 36, 9 * lvt_28_1_, 9, 9);
                        }

                        if (lvt_22_2_ * 2 + 1 == calculatedHealing + player.getHealth()) {
                            this.blit(poseStack, lvt_26_1_, lvt_27_1_, lvt_23_2_ + 45, 9 * lvt_28_1_, 9, 9);
                        }
                        bridge.color4f(1,1,1,1);
                    }
                }
            }

            LivingEntity lvt_22_3_ = this.getPlayerVehicleWithHealth();
            lvt_23_2_ = this.getVehicleMaxHearts(lvt_22_3_);
            if (lvt_23_2_ == 0) {
                this.minecraft.getProfiler().popPush("food");

                for(lvt_24_2_ = 0; lvt_24_2_ < 10; ++lvt_24_2_) {
                    lvt_25_2_ = lvt_12_1_;
                    lvt_26_1_ = 16;
                    int lvt_27_2_ = 0;
                    if (player.hasEffect(MobEffects.HUNGER)) {
                        lvt_26_1_ += 36;
                        lvt_27_2_ = 13;
                    }

                    if (player.getFoodData().getSaturationLevel() <= 0.0F && this.tickCount % (lvt_9_1_ * 3 + 1) == 0) {
                        lvt_25_2_ += this.random.nextInt(3) - 1;
                    }

                    lvt_28_1_ = lvt_11_1_ - lvt_24_2_ * 8 - 9;
                    this.blit(poseStack, lvt_28_1_, lvt_25_2_, 16 + lvt_27_2_ * 9, 27, 9, 9);
                    if (lvt_24_2_ * 2 + 1 < lvt_9_1_) {
                        this.blit(poseStack, lvt_28_1_, lvt_25_2_, lvt_26_1_ + 36, 27, 9, 9);
                    }

                    if (lvt_24_2_ * 2 + 1 == lvt_9_1_) {
                        this.blit(poseStack, lvt_28_1_, lvt_25_2_, lvt_26_1_ + 45, 27, 9, 9);
                    }

                    //AddOn Methods

                    if(Utils.showFoodIncrement()) {
                        GFXBridge bridge = Laby.gfx();
                        bridge.color4f(1, 1, 1, Utils.getBlinkingOpacity());
                        if (lvt_24_2_ * 2 + 1 < foodLevelAfterEating) {
                            this.blit(poseStack, lvt_28_1_, lvt_25_2_, lvt_26_1_ + 36, 27, 9, 9);
                        }

                        if (lvt_24_2_ * 2 + 1 == foodLevelAfterEating) {
                            this.blit(poseStack, lvt_28_1_, lvt_25_2_, lvt_26_1_ + 45, 27, 9, 9);
                        }
                        bridge.color4f(1,1,1,1);
                    }

                    SaturationRenderer.INSTANCE.applyRenderJob(lvt_28_1_, lvt_25_2_, player.getFoodData().getSaturationLevel(), saturationValueOfItem);
                }

                SaturationRenderer.INSTANCE.flush(stack);

                //End AddOn

                lvt_18_1_ -= 10;
            }

            this.minecraft.getProfiler().popPush("air");
            lvt_24_2_ = player.getMaxAirSupply();
            lvt_25_2_ = Math.min(player.getAirSupply(), lvt_24_2_);
            if (player.isEyeInFluid(FluidTags.WATER) || lvt_25_2_ < lvt_24_2_) {
                lvt_26_1_ = this.getVisibleVehicleHeartRows(lvt_23_2_) - 1;
                lvt_18_1_ -= lvt_26_1_ * 10;
                lvt_27_1_ = Mth.ceil((double)(lvt_25_2_ - 2) * 10.0 / (double)lvt_24_2_);
                lvt_28_1_ = Mth.ceil((double)lvt_25_2_ * 10.0 / (double)lvt_24_2_) - lvt_27_1_;

                for(int lvt_29_1_ = 0; lvt_29_1_ < lvt_27_1_ + lvt_28_1_; ++lvt_29_1_) {
                    if (lvt_29_1_ < lvt_27_1_) {
                        this.blit(poseStack, lvt_11_1_ - lvt_29_1_ * 8 - 9, lvt_18_1_, 16, 18, 9, 9);
                    } else {
                        this.blit(poseStack, lvt_11_1_ - lvt_29_1_ * 8 - 9, lvt_18_1_, 25, 18, 9, 9);
                    }
                }
            }

            this.minecraft.getProfiler().pop();
        }

        ci.cancel();
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
