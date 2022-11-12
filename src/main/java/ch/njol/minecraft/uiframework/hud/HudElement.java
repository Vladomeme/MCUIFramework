package ch.njol.minecraft.uiframework.hud;

import ch.njol.minecraft.uiframework.ElementPosition;
import ch.njol.minecraft.uiframework.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Rectangle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class HudElement {

	private static final Logger LOGGER = LogManager.getLogger();

	protected final MinecraftClient client;

	public HudElement() {
		client = MinecraftClient.getInstance();
	}

	public void renderAbsolute(MatrixStack matrices, float tickDelta) {
		if (!isEnabled() || (!isVisible() && !isInEditMode())) {
			return;
		}
		Rectangle dimension = getDimension();
		matrices.push();
		matrices.translate(dimension.x, dimension.y, getZOffset());
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		try {
			render(matrices, tickDelta);
		} catch (Exception e) {
			LOGGER.error("Error rendering " + getClass().getCanonicalName(), e);
		} finally {
			matrices.pop();
		}
	}

	public Rectangle getDimension() {
		ElementPosition position = getPosition();
		int width = getWidth();
		int height = getHeight();
		int x = Math.round(client.getWindow().getScaledWidth() * position.offsetXRelative + position.offsetXAbsolute - position.alignX * width);
		int y = Math.round(client.getWindow().getScaledHeight() * position.offsetYRelative + position.offsetYAbsolute - position.alignY * height);
		return new Rectangle(x, y, width, height);
	}

	/**
	 * Returns whether this HUD element is enabled at all.
	 * If not enabled, it will not appear when the reorder screen is opened.
	 */
	protected abstract boolean isEnabled();

	/**
	 * Returns whether this HUD element is currently visible, given that it {@link #isEnabled() is enabled}.
	 * This is ignored when the reorder screen is opened.
	 */
	protected abstract boolean isVisible();

	public boolean isInEditMode() {
		return client.currentScreen instanceof HudEditScreen;
	}

	protected abstract int getWidth();

	protected abstract int getHeight();

	protected abstract ElementPosition getPosition();

	protected abstract int getZOffset();

	protected abstract void render(MatrixStack matrices, float tickDelta);

	// helper methods

	/**
	 * Draws text with a full black border around it
	 */
	public void drawOutlinedText(MatrixStack matrices, String text, int x, int y, int color) {
		matrices.push();
		matrices.translate(0, 0, 1);
		client.textRenderer.draw(matrices, text, x - 1, y, 0);
		client.textRenderer.draw(matrices, text, x, y - 1, 0);
		client.textRenderer.draw(matrices, text, x + 1, y, 0);
		client.textRenderer.draw(matrices, text, x, y + 1, 0);
		matrices.translate(0, 0, 0.03f);
		client.textRenderer.draw(matrices, text, x, y, color);
		matrices.pop();
	}

	public static void drawTextureSmooth(MatrixStack matrices, float x, float y, float width, float height) {
		drawTexturedQuadSmooth(matrices.peek().getPositionMatrix(), x, y, x + width, y + height, 0, 0, 0, 1, 1);
	}

	public static void drawTextureSmooth(MatrixStack matrices, float x, float y, float width, float height, float u0, float u1, float v0, float v1) {
		drawTexturedQuadSmooth(matrices.peek().getPositionMatrix(), x, y, x + width, y + height, 0, u0, v0, u1, v1);
	}

	public static void drawTexturedQuadSmooth(Matrix4f matrices, float x0, float y0, float x1, float y1, float z, float u0, float v0, float u1, float v1) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		bufferBuilder.vertex(matrices, x0, y1, z).texture(u0, v1).next();
		bufferBuilder.vertex(matrices, x1, y1, z).texture(u1, v1).next();
		bufferBuilder.vertex(matrices, x1, y0, z).texture(u1, v0).next();
		bufferBuilder.vertex(matrices, x0, y0, z).texture(u0, v0).next();
		bufferBuilder.end();
		BufferRenderer.draw(bufferBuilder);
	}

	public static void drawSprite(MatrixStack matrices, Sprite sprite, float x, float y, float width, float height) {
		RenderSystem.setShaderTexture(0, sprite.getAtlas().getId());
		drawTexturedQuadSmooth(matrices.peek().getPositionMatrix(), x, y, x + width, y + height, 0, sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV());
	}

	/**
	 * Draws a sub-rectangle of the given sprite into the given area (thus filling the area only partially).
	 * The 'cut' coordinates are relative offsets, e.g. a cutX0=0.1 and cutX1=0.8 will cut off the leftmost 10% of the sprite and the rightmost 20%.
	 */
	public static void drawPartialSprite(MatrixStack matrices, Sprite sprite, float x, float y, float width, float height, float cutX0, float cutY0, float cutX1, float cutY1) {
		if (cutX0 >= cutX1 || cutY0 >= cutX1 || width == 0 || height == 0) {
			return;
		}
		RenderSystem.setShaderTexture(0, sprite.getAtlas().getId());
		drawTexturedQuadSmooth(matrices.peek().getPositionMatrix(), x + width * cutX0, y + height * cutY0, x + width * cutX1, y + height * cutY1, 0,
			sprite.getMinU() + cutX0 * (sprite.getMaxU() - sprite.getMinU()), sprite.getMinV() + cutY0 * (sprite.getMaxV() - sprite.getMinV()),
			sprite.getMinU() + cutX1 * (sprite.getMaxU() - sprite.getMinU()), sprite.getMinV() + cutY1 * (sprite.getMaxV() - sprite.getMinV()));
	}

	// need to draw lots of quads for this as the sprite texture is in an atlas and thus wrapping UV isn't possible
	public static void drawRepeatingSprite(MatrixStack matrices, Sprite sprite, float x, float y, float width, float height, float xRepetitions, float yRepetitions) {
		if (xRepetitions * yRepetitions > 1000) {
			throw new IllegalArgumentException("Too many sprite repetitions requested! (" + xRepetitions + ", " + yRepetitions + ")");
		}
		RenderSystem.setShaderTexture(0, sprite.getAtlas().getId());
		for (int xrep = 0; xrep < xRepetitions; xrep++) {
			for (int yrep = 0; yrep < yRepetitions; yrep++) {
				float xfactor = Math.min(1.0f, xRepetitions - xrep);
				float yfactor = Math.min(1.0f, yRepetitions - yrep);
				drawTexturedQuadSmooth(matrices.peek().getPositionMatrix(),
					x + width * xrep, y + height * yrep, x + width * (xrep + xfactor), y + height * (yrep + yfactor), 0,
					sprite.getMinU(), sprite.getMinV(), sprite.getMinU() + (sprite.getMaxU() - sprite.getMinU()) * xfactor, sprite.getMinV() + (sprite.getMaxV() - sprite.getMinV()) * yfactor);
			}
		}
	}

	protected boolean dragging = false;

	private double dragXAbsolute;
	private double dragYAbsolute;

	private double dragXRelative;
	private double dragYRelative;

	/**
	 * Checks whether the given mouse coordinates (relative to this element) are valid for mouse operations.
	 */
	protected boolean isClickable(double mouseX, double mouseY) {
		return true;
	}

	protected boolean isPixelTransparent(Sprite sprite, double relativeX, double relativeY) {
		return sprite.isPixelTransparent(0, Utils.clamp(0, (int) (relativeX * sprite.getWidth()), sprite.getWidth() - 1),
			Utils.clamp(0, (int) (relativeY * sprite.getHeight()), sprite.getHeight() - 1));
	}

	/**
	 * The mouse has been clicked while the cursor was above this element.
	 *
	 * @param mouseX Mouse X coordinate relative to this element
	 * @param mouseY Mouse Y coordinate relative to this element
	 * @param button Which mouse button was pressed
	 * @return A click result specifying which action has been taken
	 */
	public Hud.ClickResult mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && isInEditMode() != Screen.hasControlDown()) {
			startDragging(mouseX, mouseY);
			return Hud.ClickResult.DRAG;
		} else {
			return Hud.ClickResult.NONE;
		}
	}

	protected void startDragging(double mouseX, double mouseY) {
		dragging = true;
		Rectangle dimension = getDimension();
		dragXAbsolute = dimension.x;
		dragYAbsolute = dimension.y;
		dragXRelative = mouseX;
		dragYRelative = mouseY;
	}

	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (dragging) {
			Rectangle dimension = getDimension();
			ElementPosition position = getPosition();

			int scaledWidth = client.getWindow().getScaledWidth();
			int scaledHeight = client.getWindow().getScaledHeight();

			double newX = mouseX + dimension.x - dragXRelative;
			double newY = mouseY + dimension.y - dragYRelative;

			if (Screen.hasShiftDown()) {
				if (Math.abs(newX - dragXAbsolute) > Math.abs(newY - dragYAbsolute)) {
					newY = dragYAbsolute;
				} else {
					newX = dragXAbsolute;
				}
			}

			// Offsets to the sides and centers of the screen. The smallest offset of each direction will be used as anchor point.
			// TODO maybe choose anchor point relative to nearby elements as well? maybe even snap to other elements?
			double left = newX;
			double horizontalMiddle = Math.abs(newX + dimension.width / 2.0 - scaledWidth / 2.0);
			double right = scaledWidth - (newX + dimension.width);
			double top = newY;
			double verticalMiddle = Math.abs(newY + dimension.height / 2.0 - scaledHeight / 2.0);
			double bottom = scaledHeight - (newY + dimension.height);

			position.offsetXRelative = left < horizontalMiddle && left < right ? 0 : horizontalMiddle < right ? 0.5f : 1;
			position.offsetYRelative = top < verticalMiddle && top < bottom ? 0 : verticalMiddle < bottom ? 0.5f : 1;
			position.alignX = position.offsetXRelative;
			position.alignY = position.offsetYRelative;
			position.offsetXAbsolute = (int) Math.round(newX - scaledWidth * position.offsetXRelative + position.alignX * dimension.width);
			position.offsetYAbsolute = (int) Math.round(newY - scaledHeight * position.offsetYRelative + position.alignY * dimension.height);

			// snap to center
			if (!Screen.hasAltDown()) {
				if (position.offsetXRelative == 0.5f && Math.abs(position.offsetXAbsolute) < 10) {
					position.offsetXAbsolute = 0;
				}
				if (position.offsetYRelative == 0.5f && Math.abs(position.offsetYAbsolute) < 10) {
					position.offsetYAbsolute = 0;
				}
			}
			return true;
		}
		return false;
	}

	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (dragging) {
			dragging = false;
			return true;
		}
		return false;
	}

	public void removed() {
		dragging = false;
	}

	public void renderTooltip(Screen screen, MatrixStack matrices, int mouseX, int mouseY) {

	}

}
