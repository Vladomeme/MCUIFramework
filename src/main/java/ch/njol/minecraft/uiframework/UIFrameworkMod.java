package ch.njol.minecraft.uiframework;

import ch.njol.minecraft.uiframework.atlas.NamespacedAtlasSource;
import ch.njol.minecraft.uiframework.hud.HudEditScreen;
import ch.njol.minecraft.uiframework.mixins.AtlasSourceManagerAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.texture.atlas.AtlasSourceType;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class UIFrameworkMod implements ClientModInitializer {
	public static AtlasSourceType NAMESPACED = null;

	public static void registerNAMESPACED() {
		NAMESPACED = AtlasSourceManagerAccessor.doRegister("namespace", NamespacedAtlasSource.CODEC);
	}

	public static KeyBinding openHudEditScreenKeybinding;

	@Override
	public void onInitializeClient() {

		openHudEditScreenKeybinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"njols-ui-framework.key.editHud",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_UNKNOWN,
			KeyBinding.UI_CATEGORY
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (openHudEditScreenKeybinding.wasPressed() && !client.options.hudHidden) {
				if (client.currentScreen instanceof HudEditScreen) {
					client.currentScreen.close();
				} else {
					client.setScreen(new HudEditScreen(client.currentScreen));
				}
			}
		});

	}

}
