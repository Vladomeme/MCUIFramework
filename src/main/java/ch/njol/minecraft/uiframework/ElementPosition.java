package ch.njol.minecraft.uiframework;

public class ElementPosition {

	public float offsetXRelative;
	public float offsetYRelative;
	public int offsetXAbsolute;
	public int offsetYAbsolute;
	public float alignX;
	public float alignY;

	public ElementPosition() {
	}

	public ElementPosition(float offsetXRelative, int offsetXAbsolute, float offsetYRelative, int offsetYAbsolute, float alignX, float alignY) {
		this.offsetXRelative = offsetXRelative;
		this.offsetXAbsolute = offsetXAbsolute;
		this.offsetYRelative = offsetYRelative;
		this.offsetYAbsolute = offsetYAbsolute;
		this.alignX = alignX;
		this.alignY = alignY;
	}

	public ElementPosition clone() {
		ElementPosition clone = new ElementPosition();
		clone.offsetXRelative = offsetXRelative;
		clone.offsetYRelative = offsetYRelative;
		clone.offsetXAbsolute = offsetXAbsolute;
		clone.offsetYAbsolute = offsetYAbsolute;
		clone.alignX = alignX;
		clone.alignY = alignY;
		return clone;
	}

}
