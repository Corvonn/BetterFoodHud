package de.corvonn.betterFoodHud.utils;

import de.corvonn.betterFoodHud.BetterFoodHud;
import de.corvonn.betterFoodHud.configs.BlinkingSettings;
import de.corvonn.betterFoodHud.configs.Config;
import net.labymod.api.Laby;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.client.world.food.FoodData;
import net.labymod.api.client.world.item.ItemStack;
import net.labymod.api.component.data.BuiltinDataComponents;
import net.labymod.api.nbt.NBTTag;
import net.labymod.api.nbt.tags.NBTTagCompound;

public class Utils {
    public static final String ADDON_ICONS_LOCATION = "icons.png";
    public static final String NAMESPACE = "betterfoodhud";
    private static final Config CONFIG = BetterFoodHud.getInstance().configuration();

    //////////////////////////////////////////////////////////////////////////////////////////////// Blinking

    public static float getBlinkingOpacity() {;
        BlinkingSettings blinkingSettings = CONFIG.blinkingSettings();
        int blinkingSpeed = blinkingSettings.speed().get();
        float maxOpacity = CONFIG.maxOpacity().get() / 100f;

        if (!blinkingSettings.isEnabled().get()) return maxOpacity;

        float f = ((System.currentTimeMillis() % blinkingSpeed) / (float) blinkingSpeed * 2);
        if(f > 1) f = 2 - f;

        return Math.max(Math.min(1, (f * 1.5f) - 0.25f), 0) * maxOpacity;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////// Calculations

    /**
     * Calculates the estimated healed value through the food in players hand. Note that the value is only estimated and may differ accordingly.
     */
    public static int calculateHealing(float saturationValueOfItem, int nutritionValueOfItem, boolean newExhaustionSystem) { //Für Version <= 1.20.4
        ClientPlayer player = Laby.labyAPI().minecraft().getClientPlayer();
        if(player == null) return 0;

        if(saturationValueOfItem == 0) return 0;

        FoodData foodData = player.foodData();
        float foodLevel = Math.min(foodData.getFoodLevel() + nutritionValueOfItem, 20);
        float saturationLevel = Math.min(foodData.getSaturationLevel() + saturationValueOfItem, 20);

        int healing = 0;
        float consumptionRate = newExhaustionSystem ? 1.5f : 0.75f;
        float foodExcess = foodLevel - 17.9f; //Es sind mindestens 18 Herzen zum regenerieren nötig.

        if(foodExcess >= 0) {
            float tmp = foodExcess / consumptionRate;
            healing = (int) Math.ceil(tmp);
            tmp = saturationLevel / consumptionRate;
            healing += (int) Math.ceil(tmp);
        }

        return healing;
    }

    //Neues Erschöpfungssystem -> Ab 1.11
    public static int calculateHealing(float saturationValueOfItem, int nutritionValueOfItem) {
        return calculateHealing(saturationValueOfItem, nutritionValueOfItem, true);
    }

    public static int calculateHealing() { //Für Version >= 1.20.5
        return calculateHealing(getFoodSaturationValue(), getFoodNutritionValue());
    }

    public static int getFoodNutritionValue() {
        NBTTagCompound value = getFoodNBTTagCompound();
        if(value == null) return 0;
        NBTTag<?> tag = value.get("nutrition");
        if(tag == null) return 0;
        return (int) tag.value();
    }

    public static float getFoodSaturationValue() {
        NBTTagCompound value = getFoodNBTTagCompound();
        if(value == null) return 0;
        NBTTag<?> tag = value.get("saturation");
        if(tag == null) return 0;
        return (float) tag.value();
    }

    private static NBTTagCompound getFoodNBTTagCompound() {
        ClientPlayer player = Laby.labyAPI().minecraft().getClientPlayer();
        if(player == null) return null;

        ItemStack item = player.getMainHandItemStack();
        if(item == null) return null;
        if(!item.isFood()) return null;

        return item.getDataComponentContainer().get(BuiltinDataComponents.FOOD);
    }


    //////////////////////////////////////////////////////////////////////////////////////////////// Settings Utils

    public static boolean showEstimatedHealthIncrement() {
        return CONFIG.showEstimatedHealthIncrement().get();
    }

    public static boolean showFoodIncrement() {
        return CONFIG.showFoodIncrement().get();
    }

    public static boolean isAddOnEnabled() {
        return CONFIG.enabled().get();
    }
}
