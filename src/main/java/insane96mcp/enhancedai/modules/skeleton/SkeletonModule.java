package insane96mcp.enhancedai.modules.skeleton;

import insane96mcp.enhancedai.modules.skeleton.feature.SkeletonAI;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;

@Label(name = "Skeleton")
public class SkeletonModule extends Module {

	public SkeletonAI skeletonAI;

	public SkeletonModule() {
		super(Config.builder);
		this.pushConfig(Config.builder);
		skeletonAI = new SkeletonAI(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		skeletonAI.loadConfig();
	}
}
