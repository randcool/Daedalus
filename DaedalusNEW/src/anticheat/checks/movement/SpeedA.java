package anticheat.checks.movement;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import anticheat.Daedalus;
import anticheat.detections.Checks;
import anticheat.detections.ChecksType;
import anticheat.utils.MathUtils;
import anticheat.utils.MiscUtils;
import anticheat.utils.PlayerUtils;

public class SpeedA extends Checks {
	
	public SpeedA() {
		super("SpeedA", "Speed Type A", ChecksType.MOVEMENT, 6, Daedalus.getAC(), true);
	}

	public static Map<UUID, Map.Entry<Integer, Long>> speedTicks = new HashMap();
    public static Map<UUID, Map.Entry<Integer, Long>> tooFastTicks = new HashMap();
    public static Map<UUID, Long> lastHit = new HashMap();

    public boolean isOnIce(final Player player) {
        final Location a = player.getLocation();
        a.setY(a.getY() - 1.0);
        if (a.getBlock().getType().equals((Object)Material.ICE)) {
            return true;
        }
        a.setY(a.getY() - 1.0);
        return a.getBlock().getType().equals((Object)Material.ICE);
    }
	
	@Override
	protected void onEvent(Event event) {
		if(!this.getState()) {
			return;
		}
		if(event instanceof PlayerMoveEvent) {
			PlayerMoveEvent e = (PlayerMoveEvent) event;
			Player player = e.getPlayer();
	        if ((e.getFrom().getX() == e.getTo().getX()) && (e.getFrom().getY() == e.getTo().getY()) &&
	                (e.getFrom().getZ() == e.getFrom().getZ())) {
	            return;
	        }
	        if (player.getAllowFlight()) {
	            return;
	        }
	        if (player.getVehicle() != null) {
	            return;
	        }
	        
	        long lastHitDiff = this.lastHit.containsKey(player.getUniqueId()) ? this.lastHit.get(player.getUniqueId()) - System.currentTimeMillis() : 2001L;
	        
	        int Count = 0;
	        long Time = System.currentTimeMillis();
	        if (this.speedTicks.containsKey(player.getUniqueId()))
	        {
	            Count = ((Integer)((Map.Entry)this.speedTicks.get(player.getUniqueId())).getKey()).intValue();
	            Time = ((Long)((Map.Entry)this.speedTicks.get(player.getUniqueId())).getValue()).longValue();
	        }
	        int TooFastCount = 0;
	        double percent = 0D;
	        if (this.tooFastTicks.containsKey(player.getUniqueId()))
	        {
	            double OffsetXZ = MathUtils.offset(MathUtils.getHorizontalVector(e.getFrom().toVector()), MathUtils.getHorizontalVector(e.getTo().toVector()));
	            double LimitXZ = 0.0D;
	            if ((PlayerUtils.isReallyOnground(player)) && (player.getVehicle() == null)) {
	                LimitXZ = 0.33D;
	            } else {
	                LimitXZ = 0.38D;
	            }
	            if (lastHitDiff < 800L) {
	                ++LimitXZ;
	            }
	            else if (lastHitDiff < 1600L) {
	                LimitXZ += 0.4;
	            }
	            else if (lastHitDiff < 2000L) {
	                LimitXZ += 0.1;
	            }
	            if (MiscUtils.slabsNear(player.getLocation())) {
	                LimitXZ += 0.05D;
	            }
	            Location b =  player.getEyeLocation();b.add(0.0D, 1.0D, 0.0D);
	            if ((b.getBlock().getType() != Material.AIR) && (!MiscUtils.canStandWithin(b.getBlock()))) {
	                LimitXZ = 0.69D;
	            }
	            Location below = e.getPlayer().getLocation().clone().add(0.0D, -1.0D, 0.0D);
	            
	            if(MiscUtils.isStair(below.getBlock())) {
	            	LimitXZ += 0.6;
	            }

	            if (isOnIce(player)) {
	                if ((b.getBlock().getType() != Material.AIR) && (!MiscUtils.canStandWithin(b.getBlock()))) {
	                    LimitXZ = 1.0D;
	                } else {
	                    LimitXZ = 0.75D;
	                }
	            }
	            float speed = player.getWalkSpeed();LimitXZ += (speed > 0.2F ? speed * 10.0F * 0.33F : 0.0F);
	            for (PotionEffect effect : player.getActivePotionEffects()) {
	                if (effect.getType().equals(PotionEffectType.SPEED)) {
	                    if (player.isOnGround()) {
	                        LimitXZ += 0.061D * (effect.getAmplifier() + 1);
	                    } else {
	                        LimitXZ += 0.031D * (effect.getAmplifier() + 1);
	                    }
	                }
	            }
	            if ((OffsetXZ > LimitXZ) && (!MathUtils.elapsed(((Long)((Map.Entry)this.tooFastTicks.get(player.getUniqueId())).getValue()).longValue(), 150L)))
	            {
	            	percent = (OffsetXZ - LimitXZ) * 100;
	                TooFastCount = ((Integer)((Map.Entry)this.tooFastTicks.get(player.getUniqueId())).getKey()).intValue() + 3;
	            } else {
	            	TooFastCount = TooFastCount > -100 ? TooFastCount-- : -100;
	            }
	        }
	        if (TooFastCount >= 10)
	        {
	            TooFastCount = 0;
	            Count++;
	        }
	        if ((this.speedTicks.containsKey(player.getUniqueId())) &&
	                (MathUtils.elapsed(Time, 30000L)))
	        {
	            Count = 0;
	            Time = MathUtils.nowlong();
	        }
	        if (Count >= 3)
	        {
	            Count = 0;
	            this.Alert(player, "Type A");
	        }
	        this.tooFastTicks.put(player.getUniqueId(), new AbstractMap.SimpleEntry(Integer.valueOf(TooFastCount), Long.valueOf(System.currentTimeMillis())));
	        this.speedTicks.put(player.getUniqueId(), new AbstractMap.SimpleEntry(Integer.valueOf(Count), Long.valueOf(Time)));
		}
		if(event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
	    	if(e.getEntity() instanceof Player) {
	    		Player player = (Player) e.getEntity();
	    		
	    		lastHit.put(player.getUniqueId(), System.currentTimeMillis());
	    	}
		}
		if(event instanceof PlayerQuitEvent) {
			PlayerQuitEvent e = (PlayerQuitEvent) event;
	    	if(speedTicks.containsKey(e.getPlayer().getUniqueId())) {
	    		speedTicks.remove(e.getPlayer().getUniqueId());
	    	}
	    	if(tooFastTicks.containsKey(e.getPlayer().getUniqueId())) {
	    		tooFastTicks.remove(e.getPlayer().getUniqueId());
	    	}
	    	if(lastHit.containsKey(e.getPlayer().getUniqueId())) {
	    		lastHit.remove(e.getPlayer().getUniqueId());
	    	}
		}
	}

}
