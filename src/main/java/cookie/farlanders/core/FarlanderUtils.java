package cookie.farlanders.core;

import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

public class FarlanderUtils {

	private FarlanderUtils(){}

	public static boolean isStaredAt(@NotNull Mob mob, @NotNull Player player) {
		Vector3d playerLookDirection = new Vector3d(player.getViewVector(1.0f));
		Vector3d entityToPlayerDirection = new Vector3d(mob.x - player.x, mob.bb.minY + mob.bbHeight * 0.5 - (player.y + player.bbHeight), mob.z - player.z);
		playerLookDirection.normalize();
		double entityToPlayerDistance = entityToPlayerDirection.length();
		double angleBetweenDirections = playerLookDirection.dot(entityToPlayerDirection);
		double thresholdAngles = 1.0 - 0.1 / Math.max(entityToPlayerDistance, 1.0);
		return angleBetweenDirections > thresholdAngles && player.canEntityBeSeen(mob);
	}
}
