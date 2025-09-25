package elevate.heartGambler;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HeartsCommand implements CommandExecutor {

    private final HeartGambler plugin;

    public HeartsCommand(HeartGambler plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("hearts.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
            return true;
        }

        if (args.length != 3) {
            sendUsage(sender, label);
            return true;
        }

        String sub = args[0];
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
            return true;
        }

        int hearts;
        try {
            hearts = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Hearts must be a whole number.");
            return true;
        }
        if (hearts <= 0) {
            sender.sendMessage(ChatColor.RED + "Hearts must be > 0.");
            return true;
        }

        double delta = PlayerHealthUtil.getSavedDelta(target, plugin.getPlayerDeltaKey());
        double base = 20.0;
        double amountHp = hearts * 2.0;

        switch (sub.toLowerCase()) {
            case "give": {
                delta += amountHp;
                PlayerHealthUtil.saveAndApplyDelta(target, plugin.getPlayerDeltaKey(), delta);
                clampCurrentHealthToMax(target);
                sender.sendMessage(ChatColor.GREEN + "Gave " + hearts + " heart(s) to " + target.getName() + ".");
                if (sender != target) {
                    target.sendMessage(ChatColor.GREEN + "You received +" + hearts + " permanent heart(s).");
                }
                return true;
            }
            case "take": {
                double newTotal = base + delta - amountHp;
                if (newTotal < 8.0) {
                    amountHp = (base + delta) - 8.0;
                    if (amountHp < 0) amountHp = 0;
                }
                delta -= amountHp;
                PlayerHealthUtil.saveAndApplyDelta(target, plugin.getPlayerDeltaKey(), delta);
                clampCurrentHealthToMax(target);
                int actuallyRemovedHearts = (int) Math.round(amountHp / 2.0);
                sender.sendMessage(ChatColor.YELLOW + "Took " + actuallyRemovedHearts + " heart(s) from " + target.getName() + ".");
                if (sender != target) {
                    target.sendMessage(ChatColor.RED + "You lost " + actuallyRemovedHearts + " permanent heart(s).");
                }
                return true;
            }
            default:
                sendUsage(sender, label);
                return true;
        }
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(ChatColor.RED + "Usage:");
        sender.sendMessage(ChatColor.GRAY + "  /" + label + " give <player> <hearts>");
        sender.sendMessage(ChatColor.GRAY + "  /" + label + " take <player> <hearts>");
    }

    private void clampCurrentHealthToMax(Player player) {
        AttributeInstance maxAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxAttr == null) return;
        double newMax = maxAttr.getBaseValue();
        if (player.getHealth() > newMax) {
            player.setHealth(newMax);
        }
    }
}
