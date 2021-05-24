package me.simplicitee.project.addons.util;

import java.util.function.BiPredicate;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;

public final class BendingPredicate {

	private static BiPredicate<BendingPlayer, CoreAbility> CAN_BEND = (b, a) -> b != null && a != null;
	
	static {
		CAN_BEND = CAN_BEND.and((b, a) -> b.hasElement(a.getElement()));
		CAN_BEND = CAN_BEND.and((b, a) -> a.getElement() instanceof SubElement ? b.isElementToggled(((SubElement) a.getElement()).getParentElement()) : b.isElementToggled(a.getElement()));
		CAN_BEND = CAN_BEND.and((b, a) -> b.getBoundAbilityName().equals(a.getName()));
		CAN_BEND = CAN_BEND.and((b, a) -> !GeneralMethods.isRegionProtectedFromBuild(b.getPlayer(), a.getName(), b.getPlayer().getLocation()));
		CAN_BEND = CAN_BEND.and((b, a) -> !GeneralMethods.isRegionProtectedFromBuild(b.getPlayer(), a.getName(), b.getPlayer().getEyeLocation()));
	}
	
	public static boolean canBend(BendingPlayer player, CoreAbility dummy) {
		return CAN_BEND.test(player, dummy);
	}
	
	public static boolean canBend(BendingPlayer player, Class<? extends CoreAbility> clazz) {
		return CAN_BEND.test(player, CoreAbility.getAbility(clazz));
	}
}
