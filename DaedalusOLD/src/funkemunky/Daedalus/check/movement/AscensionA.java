package funkemunky.Daedalus.check.movement;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import funkemunky.Daedalus.Daedalus;
import funkemunky.Daedalus.check.Check;
import funkemunky.Daedalus.utils.Chance;
import funkemunky.Daedalus.utils.UtilCheat;
import funkemunky.Daedalus.utils.UtilMath;

public class AscensionA
        extends Check
{
    public AscensionA(Daedalus Daedalus)
    {
        super("AscensionA", "Ascension (Type A)", Daedalus);

        this.setBannable(true);
        this.setEnabled(true);
        setMaxViolations(4);
    }

    public static Map<UUID, Map.Entry<Long, Double>> AscensionTicks = new HashMap();
    public static Map<UUID, Double> velocity =  new HashMap();
    
    @EventHandler
    public void CheckAscension(PlayerMoveEvent event)
    {
      Player player = event.getPlayer();
      if (event.getFrom().getY() >= event.getTo().getY()) {
        return;
      }
      if (!getDaedalus().isEnabled()) {
        return;
      }
      if (player.getAllowFlight()) {
        return;
      }
      if (player.getVehicle() != null) {
        return;
      }
      
      if(player.getVelocity().length() < velocity.getOrDefault(player.getUniqueId(), -1.0D)) {
    	  return;
      }
      
      if (this.getDaedalus().getLastVelocity().containsKey(player.getUniqueId())) {
        return;
      }
      long Time = System.currentTimeMillis();
      double TotalBlocks = 0.0D;
      if (this.AscensionTicks.containsKey(player.getUniqueId()))
      {
        Time = ((Long)((Map.Entry)this.AscensionTicks.get(player.getUniqueId())).getKey()).longValue();
        TotalBlocks = Double.valueOf(((Double)((Map.Entry)this.AscensionTicks.get(player.getUniqueId())).getValue()).doubleValue()).doubleValue();
      }
      long MS = System.currentTimeMillis() - Time;
      double OffsetY = UtilMath.offset(UtilMath.getVerticalVector(event.getFrom().toVector()), UtilMath.getVerticalVector(event.getTo().toVector()));
      if (OffsetY > 0.0D) {
        TotalBlocks += OffsetY;
      }
      if (UtilCheat.blocksNear(player)) {
        TotalBlocks = 0.0D;
      }
      Location a = player.getLocation().subtract(0.0D, 1.0D, 0.0D);
      if (UtilCheat.blocksNear(a)) {
        TotalBlocks = 0.0D;
      }
      double Limit = 0.5D;
      if (player.hasPotionEffect(PotionEffectType.JUMP)) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
          if (effect.getType().equals(PotionEffectType.JUMP))
          {
            int level = effect.getAmplifier() + 1;
            Limit += (Math.pow(level + 4.2D, 2.0D) / 16.0D) + 0.3;
            break;
          }
        }
      }
      if (TotalBlocks > Limit)
      {
        if (MS > 150L)
        {
          if(velocity.containsKey(player.getUniqueId())) {
        	  getDaedalus().logCheat(this, player, "Flew up " + UtilMath.trim(1, TotalBlocks) + " blocks", Chance.HIGH, new String[0]);
          }
          Time = System.currentTimeMillis();
        }
      }
      else {
        Time = System.currentTimeMillis();
      }
      this.AscensionTicks.put(player.getUniqueId(), new AbstractMap.SimpleEntry(Long.valueOf(Time), Double.valueOf(TotalBlocks)));
      if(!player.isOnGround()) {
    	  this.velocity.put(player.getUniqueId(), player.getVelocity().length());
      } else {
    	  this.velocity.put(player.getUniqueId(), -1.0D);
      }
    }
    
    @EventHandler
    public void onLog(PlayerQuitEvent e) {
    	if(AscensionTicks.containsKey(e.getPlayer().getUniqueId())) {
    		AscensionTicks.remove(e.getPlayer().getUniqueId());
    	}
    	if(velocity.containsKey(e.getPlayer().getUniqueId())) {
    		velocity.remove(e.getPlayer().getUniqueId());
    	}
    }
}