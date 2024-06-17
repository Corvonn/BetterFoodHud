package de.corvonn.betterFoodHud;

import de.corvonn.betterFoodHud.configs.Config;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.models.addon.annotation.AddonMain;
import org.jetbrains.annotations.Nullable;

@AddonMain
public class BetterFoodHud extends LabyAddon<Config> {
    private static BetterFoodHud instance;

  @Override
  protected void enable() {
    this.registerSettingCategory();
    instance = this;
  }

  @Override
  protected Class<Config> configurationClass() {
    return Config.class;
  }

  public static @Nullable BetterFoodHud getInstance() {
      return instance;
  }
}
