package com.simibubi.create.modules.schematics.client.tools;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllKeys;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.modules.schematics.client.SchematicTransformation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class DeployTool extends PlacementToolBase {

	@Override
	public void init() {
		super.init();
		selectionRange = -1;
	}

	@Override
	public void updateSelection() {
		if (schematicHandler.isActive() && selectionRange == -1) {
			selectionRange = (int) (schematicHandler.getBounds().getCenter().length() / 2);
			selectionRange = MathHelper.clamp(selectionRange, 1, 100);
		}
		selectIgnoreBlocks = AllKeys.ACTIVATE_TOOL.isPressed();
		super.updateSelection();
	}

	@Override
	public void renderTool() {
		super.renderTool();

		if (selectedPos == null)
			return;

		GlStateManager.pushMatrix();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.enableBlend();
		float pt = Minecraft.getInstance().getRenderPartialTicks();
		double x = MathHelper.lerp(pt, lastChasingSelectedPos.x, chasingSelectedPos.x);
		double y = MathHelper.lerp(pt, lastChasingSelectedPos.y, chasingSelectedPos.y);
		double z = MathHelper.lerp(pt, lastChasingSelectedPos.z, chasingSelectedPos.z);

		SchematicTransformation transformation = schematicHandler.getTransformation();
		AxisAlignedBB bounds = schematicHandler.getBounds();
		Vec3d center = bounds.getCenter();
		Vec3d rotationOffset = transformation.getRotationOffset(true);
		int centerX = (int) center.x;
		int centerZ = (int) center.z;
		double xOrigin = bounds.getXSize() / 2f;
		double zOrigin = bounds.getZSize() / 2f;

		GlStateManager.translated(x - centerX, y, z - centerZ);
		GlStateManager.translated(xOrigin + rotationOffset.x, 0, zOrigin + rotationOffset.z);
		GlStateManager.rotated(transformation.getCurrentRotation(), 0, 1, 0);
		GlStateManager.translated(-rotationOffset.x, 0, -rotationOffset.z);
		GlStateManager.translated(-xOrigin, 0, -zOrigin);

		schematicHandler.getOutline().setTextures(AllSpecialTextures.CHECKERED, null);
		schematicHandler.getOutline().render(Tessellator.getInstance().getBuffer());
		schematicHandler.getOutline().setTextures(null, null);
		GlStateManager.popMatrix();
	}

	@Override
	public boolean handleMouseWheel(double delta) {
		if (!selectIgnoreBlocks)
			return super.handleMouseWheel(delta);
		selectionRange += delta;
		selectionRange = MathHelper.clamp(selectionRange, 1, 100);
		return true;
	}

	@Override
	public boolean handleRightClick() {
		if (selectedPos == null)
			return super.handleRightClick();
		Vec3d center = schematicHandler.getBounds().getCenter();
		BlockPos target = selectedPos.add(-((int) center.x), 0, -((int) center.z));

		ItemStack item = schematicHandler.getActiveSchematicItem();
		if (item != null) {
			item.getTag().putBoolean("Deployed", true);
			item.getTag().put("Anchor", NBTUtil.writeBlockPos(target));
		}

		schematicHandler.getTransformation().moveTo(target);
		schematicHandler.markDirty();
		schematicHandler.deploy();
		return true;
	}

}
