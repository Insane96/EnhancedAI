package insane96mcp.enhancedai.modules.skeleton;

import insane96mcp.enhancedai.modules.skeleton.feature.SkeletonAIFeature;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;

@Label(name = "Skeleton")
public class SkeletonModule extends Module {

	SkeletonAIFeature skeletonAIFeature;

	public SkeletonModule() {
		super(Config.builder);
		this.pushConfig(Config.builder);
		skeletonAIFeature = new SkeletonAIFeature(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		skeletonAIFeature.loadConfig();
	}
}
