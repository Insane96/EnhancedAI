package insane96mcp.enhancedai.modules.skeleton;

import insane96mcp.enhancedai.modules.skeleton.feature.SkeletonFleeTarget;
import insane96mcp.enhancedai.modules.skeleton.feature.SkeletonShoot;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;

@Label(name = "Skeleton")
public class SkeletonModule extends Module {

	public SkeletonShoot skeletonShoot;
	public SkeletonFleeTarget skeletonFleeTarget;

	public SkeletonModule() {
		super(Config.builder);
		this.pushConfig(Config.builder);
		skeletonShoot = new SkeletonShoot(this);
		skeletonFleeTarget = new SkeletonFleeTarget(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		skeletonShoot.loadConfig();
		skeletonFleeTarget.loadConfig();
	}
}
