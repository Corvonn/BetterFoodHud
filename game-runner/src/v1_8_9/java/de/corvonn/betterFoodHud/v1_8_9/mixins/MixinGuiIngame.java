package de.corvonn.betterFoodHud.v1_8_9.mixins;

import de.corvonn.betterFoodHud.utils.SaturationRenderer;
import de.corvonn.betterFoodHud.utils.Utils;
import net.labymod.api.Laby;
import net.labymod.v1_8_9.client.render.matrix.VersionedStackProvider;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.FoodStats;
import net.minecraft.util.MathHelper;
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
    private void renderPlayerStats(ScaledResolution lvt_1_1_, CallbackInfo ci) {
        if(!Utils.isAddOnEnabled()) return;

        if (this.mc.getRenderViewEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)this.mc.getRenderViewEntity();

            int nutritionValueOfItem = 0;
            float saturationValueOfItem = 0;

            ItemStack itemStack = player.getHeldItem();
            if(itemStack != null) {
                Item item = itemStack.getItem();
                if(item instanceof ItemFood food) {
                    saturationValueOfItem = food.getSaturationModifier(null) * food.getHealAmount(null) * 2;
                    nutritionValueOfItem = food.getHealAmount(null);
                }
            }

            int calculatedHealing = Utils.calculateHealing(saturationValueOfItem, nutritionValueOfItem, false);
            SaturationRenderer renderer = new SaturationRenderer(saturationValueOfItem);


            int foodLevel = player.getFoodStats().getFoodLevel();
            int foodLevelAfterEating = foodLevel + nutritionValueOfItem;

            int lvt_3_1_ = MathHelper.ceiling_float_int(player.getHealth());
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
            boolean lvt_6_1_ = false;
            FoodStats lvt_7_1_ = player.getFoodStats();
            int lvt_8_1_ = lvt_7_1_.getFoodLevel();
            int lvt_9_1_ = lvt_7_1_.getPrevFoodLevel();
            IAttributeInstance lvt_10_1_ = player.getEntityAttribute(SharedMonsterAttributes.maxHealth);
            int lvt_11_1_ = lvt_1_1_.getScaledWidth() / 2 - 91;
            int lvt_12_1_ = lvt_1_1_.getScaledWidth() / 2 + 91;
            int lvt_13_1_ = lvt_1_1_.getScaledHeight() - 39;
            float lvt_14_1_ = (float)lvt_10_1_.getAttributeValue();
            float lvt_15_1_ = player.getAbsorptionAmount();
            int lvt_16_1_ = MathHelper.ceiling_float_int((lvt_14_1_ + lvt_15_1_) / 2.0F / 10.0F);
            int lvt_17_1_ = Math.max(10 - (lvt_16_1_ - 2), 3);
            int lvt_18_1_ = lvt_13_1_ - (lvt_16_1_ - 1) * lvt_17_1_ - 10;
            float lvt_19_1_ = lvt_15_1_;
            int lvt_20_1_ = player.getTotalArmorValue();
            int lvt_21_1_ = -1;
            if (player.isPotionActive(Potion.regeneration)) {
                lvt_21_1_ = this.updateCounter % MathHelper.ceiling_float_int(lvt_14_1_ + 5.0F);
            }

            this.mc.mcProfiler.startSection("armor");

            int lvt_22_2_;
            int lvt_23_2_;
            for(lvt_22_2_ = 0; lvt_22_2_ < 10; ++lvt_22_2_) {
                if (lvt_20_1_ > 0) {
                    lvt_23_2_ = lvt_11_1_ + lvt_22_2_ * 8;
                    if (lvt_22_2_ * 2 + 1 < lvt_20_1_) {
                        this.drawTexturedModalRect(lvt_23_2_, lvt_18_1_, 34, 9, 9, 9);
                    }

                    if (lvt_22_2_ * 2 + 1 == lvt_20_1_) {
                        this.drawTexturedModalRect(lvt_23_2_, lvt_18_1_, 25, 9, 9, 9);
                    }

                    if (lvt_22_2_ * 2 + 1 > lvt_20_1_) {
                        this.drawTexturedModalRect(lvt_23_2_, lvt_18_1_, 16, 9, 9, 9);
                    }
                }
            }

            this.mc.mcProfiler.endStartSection("health");

            int lvt_24_2_;
            int lvt_25_2_;
            int lvt_26_1_;
            int lvt_27_1_;
            int lvt_28_1_;
            for(lvt_22_2_ = MathHelper.ceiling_float_int((lvt_14_1_ + lvt_15_1_) / 2.0F) - 1; lvt_22_2_ >= 0; --lvt_22_2_) {
                lvt_23_2_ = 16;
                if (player.isPotionActive(Potion.poison)) {
                    lvt_23_2_ += 36;
                } else if (player.isPotionActive(Potion.wither)) {
                    lvt_23_2_ += 72;
                }

                lvt_24_2_ = 0;
                if (lvt_4_1_) {
                    lvt_24_2_ = 1;
                }

                lvt_25_2_ = MathHelper.ceiling_float_int((float)(lvt_22_2_ + 1) / 10.0F) - 1;
                lvt_26_1_ = lvt_11_1_ + lvt_22_2_ % 10 * 8;
                lvt_27_1_ = lvt_13_1_ - lvt_25_2_ * lvt_17_1_;
                if (lvt_3_1_ <= 4) {
                    lvt_27_1_ += this.rand.nextInt(2);
                }

                if (lvt_22_2_ == lvt_21_1_) {
                    lvt_27_1_ -= 2;
                }

                lvt_28_1_ = 0;
                if (player.worldObj.getWorldInfo().isHardcoreModeEnabled()) {
                    lvt_28_1_ = 5;
                }

                this.drawTexturedModalRect(lvt_26_1_, lvt_27_1_, 16 + lvt_24_2_ * 9, 9 * lvt_28_1_, 9, 9);
                if (lvt_4_1_) {
                    if (lvt_22_2_ * 2 + 1 < lvt_5_1_) {
                        this.drawTexturedModalRect(lvt_26_1_, lvt_27_1_, lvt_23_2_ + 54, 9 * lvt_28_1_, 9, 9);
                    }

                    if (lvt_22_2_ * 2 + 1 == lvt_5_1_) {
                        this.drawTexturedModalRect(lvt_26_1_, lvt_27_1_, lvt_23_2_ + 63, 9 * lvt_28_1_, 9, 9);
                    }
                }

                if (lvt_19_1_ > 0.0F) {
                    if (lvt_19_1_ == lvt_15_1_ && lvt_15_1_ % 2.0F == 1.0F) {
                        this.drawTexturedModalRect(lvt_26_1_, lvt_27_1_, lvt_23_2_ + 153, 9 * lvt_28_1_, 9, 9);
                    } else {
                        this.drawTexturedModalRect(lvt_26_1_, lvt_27_1_, lvt_23_2_ + 144, 9 * lvt_28_1_, 9, 9);
                    }

                    lvt_19_1_ -= 2.0F;
                } else {
                    if (lvt_22_2_ * 2 + 1 < lvt_3_1_) {
                        this.drawTexturedModalRect(lvt_26_1_, lvt_27_1_, lvt_23_2_ + 36, 9 * lvt_28_1_, 9, 9);
                    }

                    if (lvt_22_2_ * 2 + 1 == lvt_3_1_) {
                        this.drawTexturedModalRect(lvt_26_1_, lvt_27_1_, lvt_23_2_ + 45, 9 * lvt_28_1_, 9, 9);
                    }

                    //AddOn Method

                    if(Utils.showEstimatedHealthIncrement()) {
                        Laby.references().glStateBridge().color4f(1, 1, 1, Utils.getBlinkingOpacity());
                        if (lvt_22_2_ * 2 + 1 < calculatedHealing + playerHealth) {
                            this.drawTexturedModalRect(lvt_26_1_, lvt_27_1_, lvt_23_2_ + 36, 9 * lvt_28_1_, 9, 9);
                        }

                        if (lvt_22_2_ * 2 + 1 == calculatedHealing + playerHealth) {
                            this.drawTexturedModalRect(lvt_26_1_, lvt_27_1_, lvt_23_2_ + 45, 9 * lvt_28_1_, 9, 9);
                        }
                        Laby.references().glStateBridge().resetColor();
                    }
                }
            }

            Entity lvt_22_3_ = player.ridingEntity;
            if (lvt_22_3_ == null) {
                this.mc.mcProfiler.endStartSection("food");

                for(lvt_23_2_ = 0; lvt_23_2_ < 10; ++lvt_23_2_) {
                    lvt_24_2_ = lvt_13_1_;
                    lvt_25_2_ = 16;
                    int lvt_26_2_ = 0;
                    if (player.isPotionActive(Potion.hunger)) {
                        lvt_25_2_ += 36;
                        lvt_26_2_ = 13;
                    }

                    if (player.getFoodStats().getSaturationLevel() <= 0.0F && this.updateCounter % (lvt_8_1_ * 3 + 1) == 0) {
                        lvt_24_2_ += this.rand.nextInt(3) - 1;
                    }

                    if (lvt_6_1_) {
                        lvt_26_2_ = 1;
                    }

                    lvt_27_1_ = lvt_12_1_ - lvt_23_2_ * 8 - 9;
                    this.drawTexturedModalRect(lvt_27_1_, lvt_24_2_, 16 + lvt_26_2_ * 9, 27, 9, 9);
                    if (lvt_6_1_) {
                        if (lvt_23_2_ * 2 + 1 < lvt_9_1_) {
                            this.drawTexturedModalRect(lvt_27_1_, lvt_24_2_, lvt_25_2_ + 54, 27, 9, 9);
                        }

                        if (lvt_23_2_ * 2 + 1 == lvt_9_1_) {
                            this.drawTexturedModalRect(lvt_27_1_, lvt_24_2_, lvt_25_2_ + 63, 27, 9, 9);
                        }
                    }

                    if (lvt_23_2_ * 2 + 1 < lvt_8_1_) {
                        this.drawTexturedModalRect(lvt_27_1_, lvt_24_2_, lvt_25_2_ + 36, 27, 9, 9);
                    }

                    if (lvt_23_2_ * 2 + 1 == lvt_8_1_) {
                        this.drawTexturedModalRect(lvt_27_1_, lvt_24_2_, lvt_25_2_ + 45, 27, 9, 9);
                    }

                    //AddOn Methods

                    if(Utils.showFoodIncrement()) {
                        Laby.references().glStateBridge().color4f(1, 1, 1, Utils.getBlinkingOpacity());
                        if (lvt_23_2_ * 2 + 1 < foodLevelAfterEating) {
                            this.drawTexturedModalRect(lvt_27_1_, lvt_24_2_, lvt_25_2_ + 36, 27, 9, 9);
                        }

                        if (lvt_23_2_ * 2 + 1 == foodLevelAfterEating) {
                            this.drawTexturedModalRect(lvt_27_1_, lvt_24_2_, lvt_25_2_ + 45, 27, 9, 9);
                        }
                        Laby.references().glStateBridge().resetColor();
                    }

                    renderer.renderNextSaturation(VersionedStackProvider.DEFAULT_STACK, lvt_27_1_, lvt_24_2_);

                    //End AddOn
                }
            } else if (lvt_22_3_ instanceof EntityLivingBase) {
                this.mc.mcProfiler.endStartSection("mountHealth");
                EntityLivingBase lvt_23_4_ = (EntityLivingBase)lvt_22_3_;
                lvt_24_2_ = (int)Math.ceil((double)lvt_23_4_.getHealth());
                float lvt_25_3_ = lvt_23_4_.getMaxHealth();
                lvt_26_1_ = (int)(lvt_25_3_ + 0.5F) / 2;
                if (lvt_26_1_ > 30) {
                    lvt_26_1_ = 30;
                }

                lvt_27_1_ = lvt_13_1_;

                for(lvt_28_1_ = 0; lvt_26_1_ > 0; lvt_28_1_ += 20) {
                    int lvt_29_1_ = Math.min(lvt_26_1_, 10);
                    lvt_26_1_ -= lvt_29_1_;

                    for(int lvt_30_1_ = 0; lvt_30_1_ < lvt_29_1_; ++lvt_30_1_) {
                        int lvt_31_1_ = 52;
                        int lvt_32_1_ = 0;
                        if (lvt_6_1_) {
                            lvt_32_1_ = 1;
                        }

                        int lvt_33_1_ = lvt_12_1_ - lvt_30_1_ * 8 - 9;
                        this.drawTexturedModalRect(lvt_33_1_, lvt_27_1_, lvt_31_1_ + lvt_32_1_ * 9, 9, 9, 9);
                        if (lvt_30_1_ * 2 + 1 + lvt_28_1_ < lvt_24_2_) {
                            this.drawTexturedModalRect(lvt_33_1_, lvt_27_1_, lvt_31_1_ + 36, 9, 9, 9);
                        }

                        if (lvt_30_1_ * 2 + 1 + lvt_28_1_ == lvt_24_2_) {
                            this.drawTexturedModalRect(lvt_33_1_, lvt_27_1_, lvt_31_1_ + 45, 9, 9, 9);
                        }
                    }

                    lvt_27_1_ -= 10;
                }
            }

            this.mc.mcProfiler.endStartSection("air");
            if (player.isInsideOfMaterial(Material.water)) {
                lvt_23_2_ = this.mc.thePlayer.getAir();
                lvt_24_2_ = MathHelper.ceiling_double_int((double)(lvt_23_2_ - 2) * 10.0 / 300.0);
                lvt_25_2_ = MathHelper.ceiling_double_int((double)lvt_23_2_ * 10.0 / 300.0) - lvt_24_2_;

                for(lvt_26_1_ = 0; lvt_26_1_ < lvt_24_2_ + lvt_25_2_; ++lvt_26_1_) {
                    if (lvt_26_1_ < lvt_24_2_) {
                        this.drawTexturedModalRect(lvt_12_1_ - lvt_26_1_ * 8 - 9, lvt_18_1_, 16, 18, 9, 9);
                    } else {
                        this.drawTexturedModalRect(lvt_12_1_ - lvt_26_1_ * 8 - 9, lvt_18_1_, 25, 18, 9, 9);
                    }
                }
            }

            this.mc.mcProfiler.endSection();
        }

        ci.cancel();
    }

    //@Shadow
    //public abstract void drawTexturedModalRect(int lvt_1_1_, int lvt_2_1_, int lvt_3_1_, int lvt_4_1_, int lvt_5_1_, int lvt_6_1_);

}
