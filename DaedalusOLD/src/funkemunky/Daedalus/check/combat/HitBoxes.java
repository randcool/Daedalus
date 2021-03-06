package funkemunky.Daedalus.check.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import funkemunky.Daedalus.Daedalus;
import funkemunky.Daedalus.check.Check;
import funkemunky.Daedalus.check.other.Latency;
import funkemunky.Daedalus.utils.Chance;
import funkemunky.Daedalus.utils.UtilCheat;

public class HitBoxes extends Check {

	public HitBoxes(Daedalus Daedalus) {
		super("HitBoxes", "Hitboxes", Daedalus);

		this.setEnabled(true);
		this.setBannable(false);

		setMaxViolations(5);
	}

	public static Map<UUID, Integer> count = new HashMap();
	public static Map<UUID, Player> lastHit = new HashMap();
	public static Map<UUID, Double> yawDif = new HashMap();

	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent e) {
		if (count.containsKey(e.getPlayer().getUniqueId())) {
			count.remove(e.getPlayer().getUniqueId());
		}
		if (yawDif.containsKey(e.getPlayer().getUniqueId())) {
			yawDif.remove(e.getPlayer().getUniqueId());
		}
		if (lastHit.containsKey(e.getPlayer().getUniqueId())) {
			this.lastHit.remove(e.getPlayer().getUniqueId());
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onMove(PlayerMoveEvent e) {
		double yawDif = Math.abs(e.getFrom().getYaw() - e.getTo().getYaw());
		this.yawDif.put(e.getPlayer().getUniqueId(), yawDif);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onUse(EntityDamageByEntityEvent e) {
		if (e.getCause() != DamageCause.ENTITY_ATTACK) {
			return;
		}
		if (!(e.getEntity() instanceof Player) || !(e.getDamager() instanceof Player)) {
			return;
		}
		if (getDaedalus().isSotwMode()) {
			return;
		}
		Player player = (Player) e.getDamager();
		Player attacked = (Player) e.getEntity();
		if (player.hasPermission("daedalus.bypass")) {
			return;
		}

		int Count = 0;
		double yawDif = 0;
		Player lastPlayer = attacked;

		if (this.lastHit.containsKey(player.getUniqueId())) {
			lastPlayer = this.lastHit.get(player.getUniqueId());
		}

		if (count.containsKey(player.getUniqueId())) {
			Count = count.get(player.getUniqueId());
		}
		if (this.yawDif.containsKey(player.getUniqueId())) {
			yawDif = this.yawDif.get(player.getUniqueId());
		}

		if (lastPlayer != attacked) {
			this.lastHit.put(player.getUniqueId(), attacked);
			return;
		}

		double offset = UtilCheat.getOffsetOffCursor(player, attacked);
		double Limit = 58D;
		double distance = UtilCheat.getHorizontalDistance(player.getLocation(), attacked.getLocation());
		Limit += distance * 14;
		Limit += (attacked.getVelocity().length() + player.getVelocity().length()) * 48;
		Limit += yawDif * 1.51;

		if (Latency.getLag(player) > 80 || Latency.getLag(attacked) > 80) {
			return;
		}

		if (offset > Limit) {
			Count++;
		} else {
			Count = 0;
		}

		if (Count > 1) {
			getDaedalus().logCheat(this, player, offset + " > " + Limit, Chance.LIKELY,
					new String[] { "Experimental" });
			Count = 0;
		}

		this.count.put(player.getUniqueId(), Count);
		this.lastHit.put(player.getUniqueId(), attacked);
	}

}
