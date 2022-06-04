package insane96mcp.enhancedai.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static ArrayList<MobEffectInstance> parseMobEffectsList(List<? extends String> list) {
        ArrayList<MobEffectInstance> mobEffectInstances = new ArrayList<>();
        for (String s : list) {
            String[] split = s.split(",");
            if (split.length != 3) {
                LogHelper.warn("Invalid line \"%s\" for Mob Effect", s);
                continue;
            }

            ResourceLocation effectRL = ResourceLocation.tryParse(split[0]);
            if (effectRL == null) {
                LogHelper.warn("%s potion effect for Mob Effect is not valid", split[0]);
                continue;
            }
            if (!ForgeRegistries.MOB_EFFECTS.containsKey(effectRL)) {
                LogHelper.warn("%s potion effect for Mob Effect seems to not exist", split[0]);
                continue;
            }
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectRL);

            //Amplifier
            if (!NumberUtils.isParsable(split[1])) {
                LogHelper.warn(String.format("Invalid amplifier \"%s\" for Mob Effect", s));
                continue;
            }
            int amplifier = Integer.parseInt(split[1]);

            //Duration
            if (!NumberUtils.isParsable(split[2])) {
                LogHelper.warn(String.format("Invalid duration \"%s\" for Mob Effect", s));
                continue;
            }
            int duration = Integer.parseInt(split[2]);

            mobEffectInstances.add(new MobEffectInstance(effect, duration, amplifier));
        }
        return mobEffectInstances;
    }
}
