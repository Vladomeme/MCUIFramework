package ch.njol.minecraft.uiframework;

public abstract class Utils {

	private Utils() {
	}

	public static float smoothStep(float f) {
		if (f <= 0) {
			return 0;
		}
		if (f >= 1) {
			return 1;
		}
		return f * f * (3 - 2 * f);
	}

	public static float ease(float currentValue, float oldValue, float speedFactor, float minSpeed) {
		if (!Float.isFinite(oldValue) || Math.abs(currentValue - oldValue) <= minSpeed) {
			return currentValue;
		} else {
			float speed = (currentValue - oldValue) * speedFactor;
			if (speed > 0 && speed < minSpeed) {
				speed = minSpeed;
			} else if (speed < 0 && speed > -minSpeed) {
				speed = -minSpeed;
			}
			return oldValue + speed;
		}
	}

	public static int clamp(int lowerBound, int value, int upperBound) {
		return Math.max(lowerBound, Math.min(value, upperBound));
	}

	public static float clamp(float lowerBound, float value, float upperBound) {
		return Math.max(lowerBound, Math.min(value, upperBound));
	}

}
