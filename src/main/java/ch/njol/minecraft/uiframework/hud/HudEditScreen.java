package ch.njol.minecraft.uiframework.hud;

import ch.njol.minecraft.uiframework.UIFrameworkMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.text.Text;

public class HudEditScreen extends Screen {

	private final Screen parent;

	private final Hud hud = Hud.INSTANCE;

	public HudEditScreen(Screen parent) {
		super(Text.of("HUD Edit Screen"));
		this.parent = parent;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return hud.mouseClicked(this, mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		return hud.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return hud.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public void removed() {
		hud.removed();
	}

	@Override
	public void tick() {
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		assert client != null;

		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		context.getMatrices().push();
		context.getMatrices().translate(0, 0, 100);
		context.drawTextWithShadow(client.textRenderer, "Rearrange elements by dragging them around with the mouse. ESC to close.", 5, 5, 0xffffffff);
		context.getMatrices().pop();
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (UIFrameworkMod.openHudEditScreenKeybinding.matchesKey(keyCode, scanCode)) {
			close();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void close() {
		assert client != null;
		client.setScreen(parent);
	}

}
