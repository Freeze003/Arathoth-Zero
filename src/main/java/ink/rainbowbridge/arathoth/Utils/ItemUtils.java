package ink.rainbowbridge.arathoth.Utils;

import ink.rainbowbridge.arathoth.Arathoth;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ItemUtils {
    public static boolean isNull(ItemStack item) {
        if (item == null) {
            return true;
        }

        if (item.getType().equals(Material.AIR)) {
            return true;
        }

        return false;
    }

    public static boolean hasLore(ItemStack item) {
        if (isNull(item)) {
            return false;
        }

        if (!item.getItemMeta().hasLore()) {
            return false;
        }

        return true;
    }

    public static List<String> getUncoloredLore(ItemStack item) {
        if(isApproveItem(item)) {
            List<String> lores = item.getItemMeta().getLore().stream().map(x -> ChatColor.stripColor(x)).collect(Collectors.toList());
            return lores;
        }
        return Arrays.asList(" ");
    }

    public static boolean isApproveItem(ItemStack item) {
        return (!isNull(item) && item.getItemMeta().hasLore());
    }
}
