package de.corvonn.betterFoodHud.configs;

import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.loader.annotation.ShowSettingInParent;
import net.labymod.api.configuration.loader.property.ConfigProperty;

public class BlinkingSettings extends Config {
    @ShowSettingInParent
    @SwitchSetting
    private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(true);

    @SliderSetting(min = 250, max = 5000, steps = 125)
    private final ConfigProperty<Integer> speed = new ConfigProperty<>(1250);

    public ConfigProperty<Boolean> isEnabled() {return enabled;}

    public ConfigProperty<Integer> speed() {return speed;}
}
