package ch.njol.minecraft.uiframework.mixins;

import ch.njol.minecraft.uiframework.hud.Hud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {

	@Unique
	private final Hud hud = Hud.INSTANCE;

	@Shadow
	protected abstract Style getTextStyleAt(double x, double y);

	protected ChatScreenMixin(Text title) {
		super(title);
	}

	@Inject(method = "mouseClicked(DDI)Z", at = @At("RETURN"), cancellable = true)
	public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ()) {
			return;
		}
		cir.setReturnValue(hud.mouseClicked(this, mouseX, mouseY, button));
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (hud.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (hud.mouseReleased(mouseX, mouseY, button)) {
			return true;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Inject(method = "removed()V", at = @At("HEAD"))
	public void removed(CallbackInfo ci) {
		hud.removed();
	}

	@Redirect(method = "render",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ChatScreen;getTextStyleAt(DD)Lnet/minecraft/text/Style;"))
	public Style render(ChatScreen instance, double mouseX, double mouseY) {
		Style style = this.getTextStyleAt(mouseX, mouseY);
		if (style != null && style.getHoverEvent() != null) {
			return style;
		}
		if (client != null && client.inGameHud.getChatHud().getIndicatorAt(mouseX, mouseY) != null) {
			return style;
		}
		return style;
	}

	@Inject(method = "render", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/DrawContext;drawHoverEvent(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Style;II)V"))
	public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		hud.renderTooltip(this, context, mouseX, mouseY);
	}
}
