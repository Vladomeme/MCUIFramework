package ch.njol.minecraft.uiframework.mixins;

import ch.njol.minecraft.uiframework.UIFrameworkMod;
import net.minecraft.client.texture.atlas.AtlasSourceManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AtlasSourceManager.class)
public class AtlasSourceManagerMixin {
    static {
        UIFrameworkMod.registerNAMESPACED();
    }
}
