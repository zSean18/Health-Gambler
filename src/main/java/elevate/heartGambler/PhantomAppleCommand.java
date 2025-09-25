package elevate.heartGambler;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PhantomAppleCommand implements CommandExecutor {

    private final HeartGambler plugin;

    public PhantomAppleCommand(HeartGambler plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            int chance = plugin.getConfig().getInt("drop_chance", 10);
            sender.sendMessage(ChatColor.YELLOW + "Current drop chance: " + chance + "%");
            sender.sendMessage(ChatColor.GRAY + "Usage:");
            sender.sendMessage(ChatColor.GRAY + "  /" + label + " chance <0-100>");
            sender.sendMessage(ChatColor.GRAY + "  /" + label + " give <player> [amount]");
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("chance")) {
            if (!sender.hasPermission("phantomapple.admin")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission.");
                return true;
            }
            try {
                int v = Integer.parseInt(args[1]);
                if (v < 0 || v > 100) throw new NumberFormatException();
                plugin.getConfig().set("drop_chance", v);
                plugin.saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Set phantom apple drop chance to " + v + "%");
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Please provide a number between 0 and 100.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("phantomapple.admin")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " give <player> [amount]");
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                return true;
            }

            int amount = 1;
            if (args.length >= 3) {
                try {
                    amount = Math.max(1, Math.min(64, Integer.parseInt(args[2])));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Amount must be a number between 1 and 64.");
                    return true;
                }
            }

            ItemStack apple = plugin.createGambleApple();
            apple.setAmount(amount);
            target.getInventory().addItem(apple);
            sender.sendMessage(ChatColor.GREEN + "Gave " + amount + " gamble apple(s) to " + target.getName() + ".");

            if (sender != target) {
                target.sendMessage(ChatColor.LIGHT_PURPLE + "You received " + amount + " gamble apple(s)!");
            }
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Usage: /" + label + " chance <0-100> | give <player> [amount]");
        return true;
    }
}
