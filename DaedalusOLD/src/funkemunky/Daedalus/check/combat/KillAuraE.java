package funkemunky.Daedalus.check.combat;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import funkemunky.Daedalus.Daedalus;
import funkemunky.Daedalus.check.Check;
import funkemunky.Daedalus.utils.Chance;

public class KillAuraE extends Check
{
    public static Map<Player, Map.Entry<Integer, Long>> lastAttack;

    public KillAuraE(Daedalus Daedalus) {
        super("KillAuraE", "Kill Aura (MultiAura)", Daedalus);
        this.lastAttack = new HashMap<Player, Map.Entry<Integer, Long>>();
        
        this.setEnabled(true);
        this.setBannable(false);
        
        this.setViolationsToNotify(2);
        setMaxViolations(7);
        this.setViolationResetTime(1800000L);
    }

    @EventHandler
    public void onLog(PlayerQuitEvent e) {
    	if(lastAttack.containsKey(e.getPlayer())) {
    		lastAttack.remove(e.getPlayer());
    	}
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void Damage(EntityDamageByEntityEvent e) {
        if (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return;
        }
        if (!((e.getEntity()) instanceof Player)) {
            return;
        }
        if (!(e.getDamager() instanceof Player)) {
            return;
        }
    	if(getDaedalus().isSotwMode()) {
    		return;
    	}
        Player player = (Player)e.getDamager();
        if (this.lastAttack.containsKey(player)) {
            Integer entityid = this.lastAttack.get(player).getKey();
            Long time = this.lastAttack.get(player).getValue();
            if (entityid != e.getEntity().getEntityId() && System.currentTimeMillis() - time < 6L) {
                this.getDaedalus().logCheat(this, player, null, Chance.LIKELY, new String[0]);
            }
            this.lastAttack.remove(player);
        }
        else {
            this.lastAttack.put(player, new AbstractMap.SimpleEntry<Integer, Long>(e.getEntity().getEntityId(), System.currentTimeMillis()));
        }
    }
}