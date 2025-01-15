package de.corvonn.betterFoodHud.v1_12_2.mixins;

import de.corvonn.betterFoodHud.utils.SaturationRenderer;
import de.corvonn.betterFoodHud.utils.Utils;
import net.labymod.api.Laby;
import net.labymod.api.client.gfx.GFXBridge;
import net.labymod.api.client.render.gl.GlStateBridge;
import net.labymod.v1_12_2.client.render.matrix.VersionedStackProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Random;

@Mixin(GuiIngame.class)
public class MixinGuiIngame extends Gui {

    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    private long healthUpdateCounter;

    @Shadow
    private int updateCounter;

    @Shadow
    private int playerHealth;

    @Shadow
    private long lastSystemTime;

    @Shadow
    private int lastPlayerHealth;

    @Shadow
    @Final
    private Random rand;

    @Inject(method = "renderPlayerStats", at = @At("HEAD"), cancellable = true)
    private void mixinRenderPlayerStats(ScaledResolution lvt_1_1_, CallbackInfo ci) {
        if(!Utils.isAddOnEnabled()) return;

        if (this.mc.getRenderViewEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)this.mc.getRenderViewEntity();

            int nutritionValueOfItem = 0;
            float saturationValueOfItem = 0;

            ItemStack itemStack = player.getHeldItemMainhand();
            if(itemStack != null) {
                Item item = itemStack.getItem();
                if(item instanceof ItemFood food) {
                    saturationValueOfItem = food.getSaturationModifier(null) * food.getHealAmount(null) * 2;
                    nutritionValueOfItem = food.getHealAmount(null);
                }
            }

            int calculatedHealing = Utils.calculateHealing(saturationValueOfItem, nutritionValueOfItem, true);

            int foodLevel = player.getFoodStats().getFoodLevel();
            int foodLevelAfterEating = foodLevel + nutritionValueOfItem;

            int lvt_3_1_ = MathHelper.ceil(player.getHealth());
            boolean lvt_4_1_ = this.healthUpdateCounter > (long)this.updateCounter && (this.healthUpdateCounter - (long)this.updateCounter) / 3L % 2L == 1L;
            if (lvt_3_1_ < this.playerHealth && player.hurtResistantTime > 0) {
                this.lastSystemTime = Minecraft.getSystemTime();
                this.healthUpdateCounter = (long)(this.updateCounter + 20);
            } else if (lvt_3_1_ > this.playerHealth && player.hurtResistantTime > 0) {
                this.lastSystemTime = Minecraft.getSystemTime();
                this.healthUpdateCounter = (long)(this.updateCounter + 10);
            }

            if (Minecraft.getSystemTime() - this.lastSystemTime > 1000L) {
                this.playerHealth = lvt_3_1_;
                this.lastPlayerHealth = lvt_3_1_;
                this.lastSystemTime = Minecraft.getSystemTime();
            }

            this.playerHealth = lvt_3_1_;
            int lvt_5_1_ = this.lastPlayerHealth;
            this.rand.setSeed((long)(this.updateCounter * 312871));
            FoodStats lvt_6_1_ = player.getFoodStats();
            int lvt_7_1_ = lvt_6_1_.getFoodLevel();
            IAttributeInstance lvt_8_1_ = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
            int lvt_9_1_ = lvt_1_1_.getScaledWidth() / 2 - 91;
            int lvt_10_1_ = lvt_1_1_.getScaledWidth() / 2 + 91;
            int lvt_11_1_ = lvt_1_1_.getScaledHeight() - 39;
            float lvt_12_1_ = (float)lvt_8_1_.getAttributeValue();
            int lvt_13_1_ = MathHelper.ceil(player.getAbsorptionAmount());
            int lvt_14_1_ = MathHelper.ceil((lvt_12_1_ + (float)lvt_13_1_) / 2.0F / 10.0F);
            int lvt_15_1_ = Math.max(10 - (lvt_14_1_ - 2), 3);
            int lvt_16_1_ = lvt_11_1_ - (lvt_14_1_ - 1) * lvt_15_1_ - 10;
            int lvt_17_1_ = lvt_11_1_ - 10;
            int lvt_18_1_ = lvt_13_1_;
            int lvt_19_1_ = player.getTotalArmorValue();
            int lvt_20_1_ = -1;
            if (player.isPotionActive(MobEffects.REGENERATION)) {
                lvt_20_1_ = this.updateCounter % MathHelper.ceil(lvt_12_1_ + 5.0F);
            }

            this.mc.profiler.startSection("armor");

            int lvt_21_2_;
            int lvt_22_2_;
            for(lvt_21_2_ = 0; lvt_21_2_ < 10; ++lvt_21_2_) {
                if (lvt_19_1_ > 0) {
                    lvt_22_2_ = lvt_9_1_ + lvt_21_2_ * 8;
                    if (lvt_21_2_ * 2 + 1 < lvt_19_1_) {
                        this.drawTexturedModalRect(lvt_22_2_, lvt_16_1_, 34, 9, 9, 9);
                    }

                    if (lvt_21_2_ * 2 + 1 == lvt_19_1_) {
                        this.drawTexturedModalRect(lvt_22_2_, lvt_16_1_, 25, 9, 9, 9);
                    }

                    if (lvt_21_2_ * 2 + 1 > lvt_19_1_) {
                        this.drawTexturedModalRect(lvt_22_2_, lvt_16_1_, 16, 9, 9, 9);
                    }
                }
            }

            this.mc.profiler.endStartSection("health");

            int lvt_23_2_;
            int lvt_24_2_;
            int lvt_25_1_;
            int lvt_26_1_;
            for(lvt_21_2_ = MathHelper.ceil((lvt_12_1_ + (float)lvt_13_1_) / 2.0F) - 1; lvt_21_2_ >= 0; --lvt_21_2_) {
                lvt_22_2_ = 16;
                if (player.isPotionActive(MobEffects.POISON)) {
                    lvt_22_2_ += 36;
                } else if (player.isPotionActive(MobEffects.WITHER)) {
                    lvt_22_2_ += 72;
                }

                lvt_23_2_ = 0;
                if (lvt_4_1_) {
                    lvt_23_2_ = 1;
                }

                lvt_24_2_ = MathHelper.ceil((float)(lvt_21_2_ + 1) / 10.0F) - 1;
                lvt_25_1_ = lvt_9_1_ + lvt_21_2_ % 10 * 8;
                lvt_26_1_ = lvt_11_1_ - lvt_24_2_ * lvt_15_1_;
                if (lvt_3_1_ <= 4) {
                    lvt_26_1_ += this.rand.nextInt(2);
                }

                if (lvt_18_1_ <= 0 && lvt_21_2_ == lvt_20_1_) {
                    lvt_26_1_ -= 2;
                }

                int lvt_27_1_ = 0;
                if (player.world.getWorldInfo().isHardcoreModeEnabled()) {
                    lvt_27_1_ = 5;
                }

                this.drawTexturedModalRect(lvt_25_1_, lvt_26_1_, 16 + lvt_23_2_ * 9, 9 * lvt_27_1_, 9, 9);
                if (lvt_4_1_) {
                    if (lvt_21_2_ * 2 + 1 < lvt_5_1_) {
                        this.drawTexturedModalRect(lvt_25_1_, lvt_26_1_, lvt_22_2_ + 54, 9 * lvt_27_1_, 9, 9);
                    }

                    if (lvt_21_2_ * 2 + 1 == lvt_5_1_) {
                        this.drawTexturedModalRect(lvt_25_1_, lvt_26_1_, lvt_22_2_ + 63, 9 * lvt_27_1_, 9, 9);
                    }
                }

                if (lvt_18_1_ > 0) {
                    if (lvt_18_1_ == lvt_13_1_ && lvt_13_1_ % 2 == 1) {
                        this.drawTexturedModalRect(lvt_25_1_, lvt_26_1_, lvt_22_2_ + 153, 9 * lvt_27_1_, 9, 9);
                        --lvt_18_1_;
                    } else {
                        this.drawTexturedModalRect(lvt_25_1_, lvt_26_1_, lvt_22_2_ + 144, 9 * lvt_27_1_, 9, 9);
                        lvt_18_1_ -= 2;
                    }
                } else {
                    if (lvt_21_2_ * 2 + 1 < lvt_3_1_) {
                        this.drawTexturedModalRect(lvt_25_1_, lvt_26_1_, lvt_22_2_ + 36, 9 * lvt_27_1_, 9, 9);
                    }

                    if (lvt_21_2_ * 2 + 1 == lvt_3_1_) {
                        this.drawTexturedModalRect(lvt_25_1_, lvt_26_1_, lvt_22_2_ + 45, 9 * lvt_27_1_, 9, 9);
                    }

                    if(Utils.showEstimatedHealthIncrement()) {
                        GFXBridge bridge = Laby.gfx();
                        bridge.color4f(1, 1, 1, Utils.getBlinkingOpacity());
                        if (lvt_21_2_ * 2 + 1 < calculatedHealing + playerHealth) {
                            this.drawTexturedModalRect(lvt_25_1_, lvt_26_1_, lvt_22_2_ + 36, 9 * lvt_27_1_, 9, 9);
                        }

                        if (lvt_21_2_ * 2 + 1 == calculatedHealing + playerHealth) {
                            this.drawTexturedModalRect(lvt_25_1_, lvt_26_1_, lvt_22_2_ + 45, 9 * lvt_27_1_, 9, 9);
                        }
                        bridge.color4f(1,1,1,1);
                    }
                }
            }

            Entity lvt_21_3_ = player.getRidingEntity();
            if (lvt_21_3_ == null || !(lvt_21_3_ instanceof EntityLivingBase)) {
                this.mc.profiler.endStartSection("food");

                for(lvt_22_2_ = 0; lvt_22_2_ < 10; ++lvt_22_2_) {
                    lvt_23_2_ = lvt_11_1_;
                    lvt_24_2_ = 16;
                    int lvt_25_2_ = 0;
                    if (player.isPotionActive(MobEffects.HUNGER)) {
                        lvt_24_2_ += 36;
                        lvt_25_2_ = 13;
                    }

                    if (player.getFoodStats().getSaturationLevel() <= 0.0F && this.updateCounter % (lvt_7_1_ * 3 + 1) == 0) {
                        lvt_23_2_ += this.rand.nextInt(3) - 1;
                    }

                    lvt_26_1_ = lvt_10_1_ - lvt_22_2_ * 8 - 9;
                    this.drawTexturedModalRect(lvt_26_1_, lvt_23_2_, 16 + lvt_25_2_ * 9, 27, 9, 9);
                    if (lvt_22_2_ * 2 + 1 < lvt_7_1_) {
                        this.drawTexturedModalRect(lvt_26_1_, lvt_23_2_, lvt_24_2_ + 36, 27, 9, 9);
                    }

                    if (lvt_22_2_ * 2 + 1 == lvt_7_1_) {
                        this.drawTexturedModalRect(lvt_26_1_, lvt_23_2_, lvt_24_2_ + 45, 27, 9, 9);
                    }

                    //AddOn Methods

                    if(Utils.showFoodIncrement()) {
                        GFXBridge bridge = Laby.gfx();
                        bridge.color4f(1, 1, 1, Utils.getBlinkingOpacity());
                        if (lvt_22_2_ * 2 + 1 < foodLevelAfterEating) {
                            this.drawTexturedModalRect(lvt_26_1_, lvt_23_2_, lvt_24_2_ + 36, 27, 9, 9);
                        }

                        if (lvt_22_2_ * 2 + 1 == foodLevelAfterEating) {
                            this.drawTexturedModalRect(lvt_26_1_, lvt_23_2_, lvt_24_2_ + 45, 27, 9, 9);
                        }
                        bridge.color4f(1,1,1,1);
                    }

                    SaturationRenderer.INSTANCE.applyRenderJob(lvt_26_1_, lvt_23_2_, player.getFoodStats().getSaturationLevel(), saturationValueOfItem);

                }

                SaturationRenderer.INSTANCE.flush(VersionedStackProvider.DEFAULT_STACK);
                //End AddOn
            }

            this.mc.profiler.endStartSection("air");
            if (player.isInsideOfMaterial(Material.WATER)) {
                lvt_22_2_ = this.mc.player.getAir();
                lvt_23_2_ = MathHelper.ceil((double)(lvt_22_2_ - 2) * 10.0 / 300.0);
                lvt_24_2_ = MathHelper.ceil((double)lvt_22_2_ * 10.0 / 300.0) - lvt_23_2_;

                for(lvt_25_1_ = 0; lvt_25_1_ < lvt_23_2_ + lvt_24_2_; ++lvt_25_1_) {
                    if (lvt_25_1_ < lvt_23_2_) {
                        this.drawTexturedModalRect(lvt_10_1_ - lvt_25_1_ * 8 - 9, lvt_17_1_, 16, 18, 9, 9);
                    } else {
                        this.drawTexturedModalRect(lvt_10_1_ - lvt_25_1_ * 8 - 9, lvt_17_1_, 25, 18, 9, 9);
                    }
                }
            }

            this.mc.profiler.endSection();
        }
        ci.cancel();
    }
}
