package insane96mcp.enhancedai.config;

import net.minecraft.util.Mth;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Random;

public class DoubleMinMax {
    public double min, max;

    public DoubleMinMax(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public DoubleMinMax(double value) {
        this.min = value;
        this.max = value;
    }

    public double getRandBetween(Random random) {
        return Mth.nextDouble(random, this.min, this.max);
    }

    public static class Config {
        private final ForgeConfigSpec.Builder builder;

        private ForgeConfigSpec.ConfigValue<Double> minConfig;
        private ForgeConfigSpec.ConfigValue<Double> maxConfig;

        public Config(ForgeConfigSpec.Builder builder, String optionName, String description) {
            this.builder = builder;
            builder.comment(description).push(optionName);
        }

        public Config setMin(double rangeMin, double rangeMax, double defaultValue) {
            minConfig = builder.defineInRange("Minimum", defaultValue, rangeMin, rangeMax);
            return this;
        }

        public Config setMax(double rangeMin, double rangeMax, double defaultValue) {
            maxConfig = builder.defineInRange("Maximum", defaultValue, rangeMin, rangeMax);
            return this;
        }

        public Config setMinMax(double rangeMin, double rangeMax, DoubleMinMax defaultValue) {
            minConfig = builder.defineInRange("Minimum", defaultValue.min, rangeMin, rangeMax);
            maxConfig = builder.defineInRange("Maximum", defaultValue.max, rangeMin, rangeMax);
            return this;
        }

        public Config build() {
            builder.pop();
            return this;
        }

        public DoubleMinMax get() {
            return new DoubleMinMax(minConfig.get(), maxConfig.get());
        }
    }
}
