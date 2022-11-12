package ch.njol.minecraft.uiframework.hud;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

public class Hud {

	public enum ClickResult {
		NONE, HANDLED, DRAG;
	}

	public static final Hud INSTANCE = new Hud();

	private final List<HudElement> elements = new ArrayList<>();

	private HudElement draggedElement = null;

	private Hud() {
	}

	// TODO a way to reorder elements? probably best to put that into the edit screen with some key (maybe mouse wheel + some modifier key?)
	public void addElement(HudElement element) {
		elements.add(element);
	}

	public void removeElement(HudElement element) {
		elements.remove(element);
	}

	public boolean mouseClicked(Screen screen, double mouseX, double mouseY, int button) {
		for (HudElement element : elements) {
			if (!element.isEnabled() || (!element.isVisible() && !(screen instanceof HudEditScreen))) {
				continue;
			}
			Rectangle dimension = element.getDimension();
			if (!dimension.contains(mouseX, mouseY) || !element.isClickable(mouseX - dimension.x, mouseY - dimension.y)) {
				continue;
			}
			ClickResult result = element.mouseClicked(mouseX - dimension.x, mouseY - dimension.y, button);
			switch (result) {
				case NONE:
					continue;
				case HANDLED:
					return true;
				case DRAG:
					draggedElement = element;
					return true;
			}
		}
		return false;
	}

	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (draggedElement != null) {
			Rectangle dimension = draggedElement.getDimension();
			return draggedElement.mouseDragged(mouseX - dimension.x, mouseY - dimension.y, button, deltaX, deltaY);
		}
		return false;
	}

	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (draggedElement != null && button == 0) {
			Rectangle dimension = draggedElement.getDimension();
			draggedElement.mouseReleased(mouseX - dimension.x, mouseY - dimension.y, button);
			draggedElement = null;
			return true;
		}
		return false;
	}

	public void removed() {
		draggedElement = null;
		for (HudElement element : elements) {
			element.removed();
		}
	}

	public void renderTooltip(Screen screen, MatrixStack matrices, int mouseX, int mouseY) {
		for (HudElement element : elements) {
			if (!element.isEnabled() || (!element.isVisible() && !(screen instanceof HudEditScreen))) {
				continue;
			}
			Rectangle dimension = element.getDimension();
			if (!dimension.contains(mouseX, mouseY) || !element.isClickable(mouseX - dimension.x, mouseY - dimension.y)) {
				continue;
			}
			matrices.push();
			matrices.translate(dimension.x, dimension.y, 0);
			element.renderTooltip(screen, matrices, mouseX - dimension.x, mouseY - dimension.y);
			matrices.pop();
			return;
		}
	}

}
