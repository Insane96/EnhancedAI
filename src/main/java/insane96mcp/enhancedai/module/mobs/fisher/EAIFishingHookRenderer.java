package insane96mcp.enhancedai.module.mobs.fisher;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EAIFishingHookRenderer extends EntityRenderer<EAIFishingHook> {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/fishing_hook.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutout(TEXTURE_LOCATION);

    public EAIFishingHookRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(EAIFishingHook fishingHook, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Entity owner = fishingHook.getOwner();
        if (owner instanceof Mob mob) {
            poseStack.pushPose();
            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            PoseStack.Pose posestack$pose = poseStack.last();
            VertexConsumer vertexconsumer = buffer.getBuffer(RENDER_TYPE);
            vertex(vertexconsumer, posestack$pose, packedLight, 0.0F, 0, 0, 1);
            vertex(vertexconsumer, posestack$pose, packedLight, 1.0F, 0, 1, 1);
            vertex(vertexconsumer, posestack$pose, packedLight, 1.0F, 1, 1, 0);
            vertex(vertexconsumer, posestack$pose, packedLight, 0.0F, 1, 0, 0);
            poseStack.popPose();
            int i = mob.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
            ItemStack itemstack = mob.getMainHandItem();
            if (!itemstack.is(Items.FISHING_ROD)) {
                i = -i;
            }

            float f2 = Mth.lerp(partialTicks, mob.yBodyRotO, mob.yBodyRot) * ((float)Math.PI / 180F);
            double d0 = Mth.sin(f2);
            double d1 = Mth.cos(f2);
            double d2 = (double)i * 0.35D;
            double d3 = 0.8D;
            double d4 = Mth.lerp(partialTicks, fishingHook.xo, fishingHook.getX()) - d1 * d2 - d0 * 0.8D;
            double d5 = fishingHook.yo + (double)fishingHook.getEyeHeight() + (fishingHook.getY() - fishingHook.yo) * (double)partialTicks;
            double d6 = Mth.lerp(partialTicks, fishingHook.zo, fishingHook.getZ()) - d0 * d2 + d1 * 0.8D;
            float f3 = fishingHook.isCrouching() ? -0.1875F : 0.0F;


            double d9 = Mth.lerp(partialTicks, fishingHook.xo, fishingHook.getX());
            double d10 = Mth.lerp(partialTicks, fishingHook.yo, fishingHook.getY());
            double d8 = Mth.lerp(partialTicks, fishingHook.zo, fishingHook.getZ());
            float f4 = (float)(d4 - d9);
            float f5 = (float)(d5 - d10) + f3;
            float f6 = (float)(d6 - d8);
            VertexConsumer vertexconsumer1 = buffer.getBuffer(RenderType.lineStrip());
            PoseStack.Pose posestack$pose1 = poseStack.last();
            int j = 16;

            for(int k = 0; k <= 16; ++k) {
                stringVertex(f4, f5, f6, vertexconsumer1, posestack$pose1, fraction(k, 16), fraction(k + 1, 16));
            }

            poseStack.popPose();
            super.render(fishingHook, entityYaw, partialTicks, poseStack, buffer, packedLight);
        }
    }

    private static float fraction(int numerator, int denominator) {
        return (float)numerator / (float)denominator;
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int packedLight, float x, int y, int u, int v) {
        consumer.addVertex(pose, x - 0.5F, (float)y - 0.5F, 0.0F)
                .setColor(-1)
                .setUv((float)u, (float)v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    private static void stringVertex(
            float x, float y, float z, VertexConsumer consumer, PoseStack.Pose pose, float stringFraction, float nextStringFraction
    ) {
        float f = x * stringFraction;
        float f1 = y * (stringFraction * stringFraction + stringFraction) * 0.5F + 0.25F;
        float f2 = z * stringFraction;
        float f3 = x * nextStringFraction - f;
        float f4 = y * (nextStringFraction * nextStringFraction + nextStringFraction) * 0.5F + 0.25F - f1;
        float f5 = z * nextStringFraction - f2;
        float f6 = Mth.sqrt(f3 * f3 + f4 * f4 + f5 * f5);
        f3 /= f6;
        f4 /= f6;
        f5 /= f6;
        consumer.addVertex(pose, f, f1, f2).setColor(-16777216).setNormal(pose, f3, f4, f5);
    }

    /**
     * Returns the location of an entity's texture.
     */
    public ResourceLocation getTextureLocation(EAIFishingHook entity) {
        return TEXTURE_LOCATION;
    }
}