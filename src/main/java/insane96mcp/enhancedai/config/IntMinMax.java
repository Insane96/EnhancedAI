package insane96mcp.enhancedai.config;

import net.minecraft.util.Mth;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Random;

public class IntMinMax {
    public int min, max;

    public IntMinMax(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public IntMinMax(int value) {
        this.min = value;
        this.max = value;
    }

    public int getRandBetween(Random random) {
        return Mth.nextInt(random, this.min, this.max);
    }

    public static class Config {
        private final ForgeConfigSpec.Builder builder;

        private ForgeConfigSpec.ConfigValue<Integer> minConfig;
        private ForgeConfigSpec.ConfigValue<Integer> maxConfig;

        public Config(ForgeConfigSpec.Builder builder, String optionName, String description) {
            this.builder = builder;
            builder.comment(description).push(optionName);
        }

        public Config setMin(int rangeMin, int rangeMax, int defaultValue) {
            minConfig = builder.defineInRange("Minimum", defaultValue, rangeMin, rangeMax);
            return this;
        }

        public Config setMax(int rangeMin, int rangeMax, int defaultValue) {
            maxConfig = builder.defineInRange("Maximum", defaultValue, rangeMin, rangeMax);
            return this;
        }

        public Config setMinMax(int rangeMin, int rangeMax, IntMinMax defaultValue) {
            minConfig = builder.defineInRange("Minimum", defaultValue.min, rangeMin, rangeMax);
            maxConfig = builder.defineInRange("Maximum", defaultValue.max, rangeMin, rangeMax);
            return this;
        }

        public Config build() {
            builder.pop();
            return this;
        }

        public IntMinMax get() {
            return new IntMinMax(minConfig.get(), maxConfig.get());
        }
    }
}
