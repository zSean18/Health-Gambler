package elevate.heartGambler;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.persistence.PersistentDataType;

public class HeartGambler extends JavaPlugin {

    public static final String PLAYER_HEALTH_DELTA_KEY = "health_delta";
    public static final String APPLE_KEY = "phantom_apple";
    public static final String STEW_KEY  = "phantom_stew";
    private NamespacedKey playerDeltaKey;
    private NamespacedKey appleKey;
    private NamespacedKey stewKey;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        playerDeltaKey = new NamespacedKey(this, PLAYER_HEALTH_DELTA_KEY);
        appleKey       = new NamespacedKey(this, APPLE_KEY);
        stewKey        = new NamespacedKey(this, STEW_KEY);

        //listeners
        Bukkit.getPluginManager().registerEvents(new PhantomDropListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GambleAppleListener(this), this); // already handles Phantom Blood
        Bukkit.getPluginManager().registerEvents(new HeartPersistListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PhantomStewListener(this), this); // NEW

        //commands
        getCommand("phantomapple").setExecutor(new elevate.healthgambler.PhantomAppleCommand(this));
        getCommand("hearts").setExecutor(new HeartsCommand(this));

        //re apply saved max health for players already online (/reload)
        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerHealthUtil.applySavedDelta(p, getPlayerDeltaKey());
        }

        registerPhantomStewRecipe();
    }

    public ItemStack createGambleApple() {
        ItemStack item = new ItemStack(Material.POTION, 1);
        PotionMeta meta = (PotionMeta) item.getItemMeta();

        meta.setDisplayName(ChatColor.DARK_RED + "Phantom Blood");
        meta.setLore(java.util.List.of(
                ChatColor.GRAY + "A volatile essence torn from phantoms.",
                ChatColor.GRAY + "Right-click to gamble your life away"
        ));
        meta.setColor(Color.fromRGB(128, 0, 0));
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(getAppleKey(), PersistentDataType.BYTE, (byte)1); // tag as Phantom Blood
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createPhantomStew() {
        ItemStack stew = new ItemStack(Material.SUSPICIOUS_STEW, 1);
        ItemMeta meta = stew.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Phantom Stew");
        meta.setLore(java.util.List.of(
                ChatColor.GRAY + "Restores what was lost.",
                ChatColor.GRAY + "Use to regain " + ChatColor.GREEN + "1–2" + ChatColor.GRAY + " hearts, up to 10 total."
        ));
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(getStewKey(), PersistentDataType.BYTE, (byte)1); //tag item as phantom stew
        stew.setItemMeta(meta);
        return stew;
    }

    private void registerPhantomStewRecipe() {
        ItemStack result = createPhantomStew();
        ItemStack blood  = createGambleApple();
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(this, "phantom_stew"), result);
        recipe.shape("BBB","BBB","BBB");
        recipe.setIngredient('B', new RecipeChoice.ExactChoice(blood));

        //avoids dupes if /reload is used
        getServer().removeRecipe(new NamespacedKey(this, "phantom_stew"));
        Bukkit.addRecipe(recipe);
    }

    public NamespacedKey getPlayerDeltaKey() { return playerDeltaKey; }
    public NamespacedKey getAppleKey() { return appleKey; }
    public NamespacedKey getStewKey()  { return stewKey; }
}
