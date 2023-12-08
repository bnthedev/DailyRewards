package dailyrewards.dailyrewards;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class Main extends JavaPlugin implements Listener, CommandExecutor {

    private Map<UUID, Long> lastClaimTime = new HashMap<>();
    private FileConfiguration config;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("dailyreward").setExecutor(this);
        saveDefaultConfig();
        config = getConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("dailyreward")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(formatMessage(config.getString("messages.onlyPlayers")));
                return true;
            }

            Player player = (Player) sender;
            UUID playerUUID = player.getUniqueId();

            if (canClaimDailyReward(playerUUID)) {
                openRewardGUI(player);
            } else {
                player.sendMessage(formatMessage(config.getString("messages.alreadyClaimed")));
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (event.getView().getTitle().equals(formatMessage(config.getString("gui.title")))){
            event.setCancelled(true);
        }
        if (event.getView().getTitle().equals(formatMessage(config.getString("gui.title")))
                && clickedItem != null && clickedItem.getType() == Material.CHEST_MINECART) {
            event.setCancelled(true);
            player.sendMessage(formatMessage(config.getString("messages.rewardClaimed")));
            ConsoleCommandSender console = Bukkit.getConsoleSender();
            Bukkit.dispatchCommand(console, "economy give " + player.getDisplayName() + " " + config.getInt("reward.amount"));
            lastClaimTime.put(player.getUniqueId(), System.currentTimeMillis());
            player.closeInventory();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (!lastClaimTime.containsKey(playerUUID)) {
            lastClaimTime.put(playerUUID, 0L);
        }
    }

    private boolean canClaimDailyReward(UUID playerUUID) {
        long lastClaim = lastClaimTime.get(playerUUID);
        long currentTime = System.currentTimeMillis();
        long timeSinceLastClaim = currentTime - lastClaim;
        long claimCooldown = config.getLong("cooldown.duration") * 1000;

        return timeSinceLastClaim >= claimCooldown;
    }

    private void openRewardGUI(Player player) {
        Inventory gui = Bukkit.createInventory(player, 9, formatMessage(config.getString("gui.title")));

        ItemStack diamondReward = new ItemStack(Material.CHEST_MINECART);
        ItemMeta meta = diamondReward.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Daily Reward!");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Click to earn daily reward!", ChatColor.GRAY + "Rewards:", ChatColor.GRAY + "+ " + ChatColor.YELLOW + "50$"));
        diamondReward.setItemMeta(meta);
        ItemStack i = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName("");
        i.setItemMeta(m);
        gui.setItem(4, diamondReward);
        gui.setItem(0, i);
        gui.setItem(1, i);
        gui.setItem(2, i);
        gui.setItem(3, i);
        gui.setItem(5, i);
        gui.setItem(6, i);
        gui.setItem(7, i);
        gui.setItem(8, i);








        player.openInventory(gui);
    }

    private String formatMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', message)
                .replace("%reward_amount%", String.valueOf(config.getInt("reward.amount")));
    }
}
