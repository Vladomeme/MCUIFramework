package ch.njol.minecraft.uiframework.atlas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.texture.atlas.AtlasSource;
import net.minecraft.client.texture.atlas.AtlasSourceType;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import static ch.njol.minecraft.uiframework.UIFrameworkMod.NAMESPACED;

public class NamespacedAtlasSource implements AtlasSource {
    public static final Codec<NamespacedAtlasSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("source")).forGetter(directoryAtlasSource -> ((NamespacedAtlasSourceImp) directoryAtlasSource).source), ((MapCodec)Codec.STRING.fieldOf("prefix")).forGetter(directoryAtlasSource -> ((NamespacedAtlasSourceImp) directoryAtlasSource).prefix), ((MapCodec)Codec.STRING.fieldOf("namespace")).forGetter(directoryAtlasSource -> ((NamespacedAtlasSourceImp) directoryAtlasSource).namespace)).apply(instance, ((o1, o2, o3) -> new NamespacedAtlasSource((String) o1, (String) o2, (String) o3))));

    private final String namespace;
    private final String source;
    private final String prefix;
    public NamespacedAtlasSource(String source, String prefix, String namespace) {
        this.namespace = namespace;
        this.source = source;
        this.prefix = prefix;
    }

    @Override
    public void load(ResourceManager resourceManager, AtlasSource.SpriteRegions regions) {
        ResourceFinder resourceFinder = new ResourceFinder("textures/" + this.source, ".png");
        resourceFinder.findResources(resourceManager).forEach((identifier, resource) -> {
            if (identifier.getNamespace().equals(namespace)) {
                Identifier identifier2 = resourceFinder.toResourceId(identifier).withPrefixedPath(this.prefix);
                regions.add(identifier2, resource);
            }
        });
    }

    @Override
    public AtlasSourceType getType() {
        return NAMESPACED;
    }

    public interface NamespacedAtlasSourceImp {
        String source = null;
        String namespace = null;
        String prefix = null;
    }
}
