package de.corvonn.betterFoodHud.configs;

import net.labymod.api.addon.AddonConfig;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.annotation.ConfigName;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.annotation.SettingSection;

@ConfigName("settings")
public class Config extends AddonConfig {

    @SwitchSetting
    private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(true);

    @SettingSection("settings")
    @SwitchSetting
    private final ConfigProperty<Boolean> showFoodIncrement = new ConfigProperty<>(true);

    @SwitchSetting
    private final ConfigProperty<Boolean> showEstimatedHealthIncrement = new ConfigProperty<>(true);

    @SwitchSetting
    private final ConfigProperty<Boolean> showSaturationIncrement = new ConfigProperty<>(true);

    @SwitchSetting
    private final ConfigProperty<Boolean> showCurrentSaturation = new ConfigProperty<>(true);

    private final BlinkingSettings blinkingSettings = new BlinkingSettings();

    @SliderSetting(min = 10, max = 100, steps = 5)
    private final ConfigProperty<Integer> maxOpacity = new ConfigProperty<>(100);

    @Override
    public ConfigProperty<Boolean> enabled() {
    return this.enabled;
    }

    public ConfigProperty<Boolean> showFoodIncrement() { return showFoodIncrement;}

    public ConfigProperty<Boolean> showEstimatedHealthIncrement() {return showEstimatedHealthIncrement;}

    public BlinkingSettings blinkingSettings() {return blinkingSettings;}

    public ConfigProperty<Integer> maxOpacity() {return maxOpacity;}

    public ConfigProperty<Boolean> showSaturationIncrement() {
        return showSaturationIncrement;
    }

    public ConfigProperty<Boolean> showCurrentSaturation() {
        return showCurrentSaturation;
    }
}
