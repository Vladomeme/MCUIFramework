package ch.njol.minecraft.uiframework;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasHolder;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.util.Identifier;

public class ModSpriteAtlasHolder extends SpriteAtlasHolder {

	// NB: code to add sprites to an existing atlas via Fabric hacks:
	// ClientSpriteRegistryCallback.event(new Identifier("...")).register((atlasTexture, registry) -> registry.register(...));

	private final String namespace;
	private final String atlasName;
	private final List<Identifier> sprites = new ArrayList<>();

	//It is also required to create a json file in your assets folder under /atlases (the name needs to correspond to the atlasName)
	//see the format on the wiki
	private ModSpriteAtlasHolder(TextureManager textureManager, String namespace, String atlasName) {
		super(textureManager, new Identifier(namespace, "textures/" + atlasName + "/atlas.png"), new Identifier(namespace, atlasName));
		this.namespace = namespace;
		this.atlasName = atlasName;
	}

	protected Stream<Identifier> getSprites() {
		return sprites.stream();
	}

	@Override
	public Sprite getSprite(Identifier objectId) {
		return super.getSprite(objectId);
	}

	public String getNamespace() {
		return namespace;
	}

	public String getAtlasName() {
		return atlasName;
	}

	/**
	 * Clears all registered sprites. Useful to register a new batch of sprites on reload in case the sprites are dynamic (e.g. are loaded from properties files or such).
	 */
	public void clearSprites() {
		sprites.clear();
	}

	/**
	 * Registers a sprite on this atlas. Must be called before resource packs are loaded to take effect.
	 *
	 * @param path Path to the sprite, relative to the textures/&lt;atlasName>/ directory
	 * @return The sprite's identifier in this atlas to use with {@link #getSprite(Identifier)}
	 */
	public Identifier registerSprite(String path) {
		Identifier id = new Identifier(namespace, path);
		sprites.add(id);
		return id;
	}

	/**
	 * Creates a sprite atlas.
	 *
	 * @param namespace Namespace of the mod the atlas is for
	 * @param atlasName Name of the atlas, which is also the directory of the sprites in the textures directory
	 * @return The newly created atlas
	 */
	public static ModSpriteAtlasHolder createAtlas(String namespace, String atlasName) {
		MinecraftClient client = MinecraftClient.getInstance();
		ModSpriteAtlasHolder atlas = new ModSpriteAtlasHolder(client.getTextureManager(), namespace, atlasName);
		((ReloadableResourceManagerImpl) client.getResourceManager()).registerReloader(atlas);
		return atlas;
	}

}
