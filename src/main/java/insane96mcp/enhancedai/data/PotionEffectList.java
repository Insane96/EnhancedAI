package insane96mcp.enhancedai.data;

import insane96mcp.insanelib.core.feature.config.ConfigOption;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PotionEffectList {
    public ArrayList<PotionOrMobEffect> entries;

    public PotionEffectList(ArrayList<PotionOrMobEffect> entries) {
        this.entries = entries;
    }

    public static PotionEffectList of(List<? extends String> strings) {
        return new PotionEffectList(PotionOrMobEffect.parseList(strings));
    }

    public List<String> getAsStringList() {
        List<String> list = new ArrayList<>();
        for (PotionOrMobEffect entry : this.entries) {
            list.add(entry.serialize());
        }
        return list;
    }

    public static class COption extends ConfigOption<PotionEffectList> {
        private final ModConfigSpec.ConfigValue<List<? extends String>> listConfig;

        public COption(ModConfigSpec.Builder builder, String name, String description, PotionEffectList defaultValue) {
            super(builder, name, description);
            listConfig = builder.defineList(name, defaultValue.getAsStringList(), () -> "", o -> o instanceof String);
        }

        @Override
        public PotionEffectList get() {
            return PotionEffectList.of(listConfig.get());
        }

        @Override
        public void set(Object value) {
            listConfig.set(((PotionEffectList) value).getAsStringList());
        }

        @Nullable
        @Override
        public List<String> getConfigPath() {
            return listConfig.getPath();
        }
    }
}
