package dev.neonjava.settings;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsPlugin extends JavaPlugin implements CommandExecutor, Listener {

    private Map<Integer, String> slotToCommand = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("settings").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("NeonSettings Enabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is for players only.");
            return true;
        }

        openSettingsGui(player);
        return true;
    }

    public void openSettingsGui(Player player) {
        String title = getConfig().getString("gui.title", "Settings");
        int rows = getConfig().getInt("gui.rows", 3);
        Inventory inv = Bukkit.createInventory(null, rows * 9, title);

        slotToCommand.clear();
        ConfigurationSection itemsSection = getConfig().getConfigurationSection("gui.items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection itemKey = itemsSection.getConfigurationSection(key);
                if (itemKey == null)
                    continue;

                Material material = Material.valueOf(itemKey.getString("material", "BARRIER").toUpperCase());
                int slot = itemKey.getInt("slot", 0);
                String name = itemKey.getString("name", "Item");
                List<String> lore = itemKey.getStringList("lore");
                String cmd = itemKey.getString("command", "");

                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(translateAlternateColorCodes(name));
                    List<String> coloredLore = new ArrayList<>();
                    for (String l : lore) {
                        coloredLore.add(translateAlternateColorCodes(l));
                    }
                    meta.setLore(coloredLore);
                    item.setItemMeta(meta);
                }

                inv.setItem(slot, item);
                slotToCommand.put(slot, cmd);
            }
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = getConfig().getString("gui.title", "Settings");
        if (!event.getView().getTitle().equals(title))
            return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        int slot = event.getSlot();
        String command = slotToCommand.get(slot);
        if (command != null && !command.isEmpty()) {
            player.performCommand(command);
            player.closeInventory();
        }
    }

    private String translateAlternateColorCodes(String text) {
        return text.replace('&', '§');
    }
}
