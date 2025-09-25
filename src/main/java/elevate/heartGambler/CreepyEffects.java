package elevate.heartGambler;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class CreepyEffects {

    //remove soon
    private CreepyEffects() {}

    public static void playBloodEffects(Player p, int heartsDelta) {
        boolean lucky = heartsDelta > 0;
        World w = p.getWorld();
        Location loc = p.getLocation().add(0, 1, 0);
        w.playSound(loc, Sound.ENTITY_PHANTOM_AMBIENT, 0.7f, 0.6f);
        w.playSound(loc, Sound.ENTITY_WITHER_AMBIENT, 0.4f, 0.8f);
        w.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.3f, 0.7f);

        //status blips
        p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 0, false, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA,   60, 0, false, false, false));

        //particles
        particleRing(w, loc, Color.fromRGB(150, 0, 0), 1.0f, 24);
        w.spawnParticle(Particle.LARGE_SMOKE, loc, 15, 0.4, 0.4, 0.4, 0.01);
        w.spawnParticle(Particle.SOUL,        loc, 10, 0.3, 0.1, 0.3, 0.01);

        //title: 10 tick fade in, 60 tick stay (3s), 20 tick fade out
        if (lucky) {
            p.sendTitle(
                    ChatColor.DARK_RED + "The blood favors you…",
                    ChatColor.GREEN + "+" + heartsDelta + " heart(s)",
                    10, 60, 20
            );
            w.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.6f, 0.9f);
        } else {
            int lost = Math.abs(heartsDelta);
            p.sendTitle(
                    ChatColor.DARK_RED + "The blood takes its due…",
                    ChatColor.RED + "-" + lost + " heart(s)",
                    10, 60, 20
            );
            w.playSound(loc, Sound.BLOCK_BEACON_DEACTIVATE, 0.7f, 0.6f);
        }
    }

    public static void playStewEffects(Player p, int heartsRestored) {
        World w = p.getWorld();
        Location loc = p.getLocation().add(0, 1, 0);
        w.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.8f, 0.9f);
        w.playSound(loc, Sound.BLOCK_ENCHANTMENT_TABLE_USE,   0.7f, 1.2f);
        p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 30, 0, false, false, false));
        particleRing(w, loc, Color.fromRGB(255, 170, 30), 1.0f, 24);
        w.spawnParticle(Particle.END_ROD, loc, 10, 0.2, 0.3, 0.2, 0.0);

        //stew title
        p.sendTitle(
                ChatColor.GOLD + "Warmth returns",
                ChatColor.GREEN + "+" + heartsRestored + " heart(s)",
                10, 50, 15
        );
    }

    private static void particleRing(World w, Location center, Color color, float size, int points) {
        Particle.DustOptions dust = new Particle.DustOptions(color, size);
        double radius = 0.8;
        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI * i) / points;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            w.spawnParticle(Particle.DUST, new Location(w, x, center.getY(), z), 1, dust);
        }
    }
}