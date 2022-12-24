package insane96mcp.enhancedai.modules.zombie.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import insane96mcp.enhancedai.modules.zombie.entity.projectile.FishingHook;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class FishingHookRenderer extends EntityRenderer<FishingHook> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/fishing_hook.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutout(TEXTURE_LOCATION);
    private static final double VIEW_BOBBING_SCALE = 960.0D;

    public FishingHookRenderer(EntityRendererProvider.Context p_174117_) {
        super(p_174117_);
    }

    public void render(FishingHook fishingHook, float p_114706_, float p_114707_, PoseStack poseStack, MultiBufferSource multiBufferSource, int p_114710_) {
        Entity entity = fishingHook.getOwner();
        if (entity instanceof Zombie zombie) {
            poseStack.pushPose();
            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            PoseStack.Pose posestack$pose = poseStack.last();
            Matrix4f matrix4f = posestack$pose.pose();
            Matrix3f matrix3f = posestack$pose.normal();
            VertexConsumer vertexconsumer = multiBufferSource.getBuffer(RENDER_TYPE);
            vertex(vertexconsumer, matrix4f, matrix3f, p_114710_, 0.0F, 0, 0, 1);
            vertex(vertexconsumer, matrix4f, matrix3f, p_114710_, 1.0F, 0, 1, 1);
            vertex(vertexconsumer, matrix4f, matrix3f, p_114710_, 1.0F, 1, 1, 0);
            vertex(vertexconsumer, matrix4f, matrix3f, p_114710_, 0.0F, 1, 0, 0);
            poseStack.popPose();
            int i = zombie.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
            ItemStack itemstack = zombie.getMainHandItem();
            if (!itemstack.is(Items.FISHING_ROD)) {
                i = -i;
            }

            float f2 = Mth.lerp(p_114707_, zombie.yBodyRotO, zombie.yBodyRot) * ((float)Math.PI / 180F);
            double d0 = Mth.sin(f2);
            double d1 = Mth.cos(f2);
            double d2 = (double)i * 0.35D;
            double d3 = 0.8D;
            double d4 = Mth.lerp(p_114707_, entity.xo, entity.getX()) - d1 * d2 - d0 * 0.8D;
            double d5 = entity.yo + (double)entity.getEyeHeight() + (entity.getY() - entity.yo) * (double)p_114707_;
            double d6 = Mth.lerp(p_114707_, entity.zo, entity.getZ()) - d0 * d2 + d1 * 0.8D;
            float f3 = entity.isCrouching() ? -0.1875F : 0.0F;


            double d9 = Mth.lerp(p_114707_, fishingHook.xo, fishingHook.getX());
            double d10 = Mth.lerp(p_114707_, fishingHook.yo, fishingHook.getY());
            double d8 = Mth.lerp(p_114707_, fishingHook.zo, fishingHook.getZ());
            float f4 = (float)(d4 - d9);
            float f5 = (float)(d5 - d10) + f3;
            float f6 = (float)(d6 - d8);
            VertexConsumer vertexconsumer1 = multiBufferSource.getBuffer(RenderType.lineStrip());
            PoseStack.Pose posestack$pose1 = poseStack.last();
            int j = 16;

            for(int k = 0; k <= 16; ++k) {
                stringVertex(f4, f5, f6, vertexconsumer1, posestack$pose1, fraction(k, 16), fraction(k + 1, 16));
            }

            poseStack.popPose();
            super.render(fishingHook, p_114706_, p_114707_, poseStack, multiBufferSource, p_114710_);
        }
    }

    private static float fraction(int p_114691_, int p_114692_) {
        return (float)p_114691_ / (float)p_114692_;
    }

    private static void vertex(VertexConsumer p_114712_, Matrix4f p_114713_, Matrix3f p_114714_, int p_114715_, float p_114716_, int p_114717_, int p_114718_, int p_114719_) {
        p_114712_.vertex(p_114713_, p_114716_ - 0.5F, (float)p_114717_ - 0.5F, 0.0F).color(255, 255, 255, 255).uv((float)p_114718_, (float)p_114719_).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(p_114715_).normal(p_114714_, 0.0F, 1.0F, 0.0F).endVertex();
    }

    private static void stringVertex(float p_174119_, float p_174120_, float p_174121_, VertexConsumer p_174122_, PoseStack.Pose p_174123_, float p_174124_, float p_174125_) {
        float f = p_174119_ * p_174124_;
        float f1 = p_174120_ * (p_174124_ * p_174124_ + p_174124_) * 0.5F + 0.25F;
        float f2 = p_174121_ * p_174124_;
        float f3 = p_174119_ * p_174125_ - f;
        float f4 = p_174120_ * (p_174125_ * p_174125_ + p_174125_) * 0.5F + 0.25F - f1;
        float f5 = p_174121_ * p_174125_ - f2;
        float f6 = Mth.sqrt(f3 * f3 + f4 * f4 + f5 * f5);
        f3 /= f6;
        f4 /= f6;
        f5 /= f6;
        p_174122_.vertex(p_174123_.pose(), f, f1, f2).color(0, 0, 0, 255).normal(p_174123_.normal(), f3, f4, f5).endVertex();
    }

    public ResourceLocation getTextureLocation(FishingHook p_114703_) {
        return TEXTURE_LOCATION;
    }
}