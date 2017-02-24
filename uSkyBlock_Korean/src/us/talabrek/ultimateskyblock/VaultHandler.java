package us.talabrek.ultimateskyblock;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHandler {
	public static Permission perms = null;
	public static Economy econ = null;

	public static void addPerk(Player player, String perk) {
		perms.playerAdd(player, perk);
	}

	public static void removePerk(Player player, String perk) {
		perms.playerRemove(player, perk);
	}

	public static void addGroup(Player player, String perk) {
		perms.playerAddGroup(player, perk);
	}

	public static boolean checkPerk(String player, String perk, World world) {
		if (perms.has(Bukkit.getPlayerExact(player), perk)) {
			return true;
		}
		return perms.has(world, player, perk);
	}

	public static boolean setupPermissions() {
		RegisteredServiceProvider<Permission> rsp = uSkyBlock.getInstance().getServer()
				.getServicesManager().getRegistration(Permission.class);
		if (rsp.getProvider() != null)
			perms = (Permission) rsp.getProvider();
		return perms != null;
	}

	public static boolean setupEconomy() {
		if (uSkyBlock.getInstance().getServer().getPluginManager()
				.getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = uSkyBlock.getInstance().getServer()
				.getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = (Economy) rsp.getProvider();
		return econ != null;
	}
}