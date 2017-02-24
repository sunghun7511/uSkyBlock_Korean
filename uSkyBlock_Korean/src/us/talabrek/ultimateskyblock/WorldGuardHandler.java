package us.talabrek.ultimateskyblock;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuardHandler {
	public static WorldGuardPlugin getWorldGuard() {
		Plugin plugin = uSkyBlock.getInstance().getServer().getPluginManager()
				.getPlugin("WorldGuard");

		if ((plugin == null) || (!(plugin instanceof WorldGuardPlugin))) {
			return null;
		}

		return (WorldGuardPlugin) plugin;
	}

	public static void protectIsland(Player sender, String player, PlayerInfo pi) {
		try {
			if (!Settings.island_protectWithWorldGuard)
				return;
			if ((pi.getIslandLocation() != null)
					&& (!getWorldGuard().getRegionManager(
							uSkyBlock.getSkyBlockWorld()).hasRegion(
							player + "Island"))) {
				ProtectedRegion region = null;
				DefaultDomain owners = new DefaultDomain();
				region = new ProtectedCuboidRegion(player + "Island",
						getProtectionVectorLeft(pi.getIslandLocation()),
						getProtectionVectorRight(pi.getIslandLocation()));
				owners.addPlayer(player);
				region.setOwners(owners);
				region.setParent(getWorldGuard().getRegionManager(
						uSkyBlock.getSkyBlockWorld()).getRegion("__Global__"));
				region.setPriority(100);
				region.setFlag(DefaultFlag.GREET_MESSAGE,
						DefaultFlag.GREET_MESSAGE.parseInput(getWorldGuard(),
								sender,
								"§d** You are entering a protected island area. ("
										+ player + ")"));
				region.setFlag(DefaultFlag.FAREWELL_MESSAGE,
						DefaultFlag.FAREWELL_MESSAGE.parseInput(
								getWorldGuard(), sender,
								"§d** You are leaving a protected island area. ("
										+ player + ")"));
				region.setFlag(DefaultFlag.PVP, DefaultFlag.PVP.parseInput(
						getWorldGuard(), sender, Settings.island_allowPvP));
				region.setFlag(DefaultFlag.CHEST_ACCESS,
						DefaultFlag.CHEST_ACCESS.parseInput(getWorldGuard(),
								sender, "deny"));
				region.setFlag(DefaultFlag.USE, DefaultFlag.USE.parseInput(
						getWorldGuard(), sender, "deny"));
				region.setFlag(DefaultFlag.DESTROY_VEHICLE,
						DefaultFlag.DESTROY_VEHICLE.parseInput(getWorldGuard(),
								sender, "deny"));
				region.setFlag(DefaultFlag.ENTITY_ITEM_FRAME_DESTROY,
						DefaultFlag.ENTITY_ITEM_FRAME_DESTROY.parseInput(
								getWorldGuard(), sender, "deny"));
				region.setFlag(DefaultFlag.ENTITY_PAINTING_DESTROY,
						DefaultFlag.ENTITY_PAINTING_DESTROY.parseInput(
								getWorldGuard(), sender, "deny"));
				ApplicableRegionSet set = getWorldGuard().getRegionManager(
						uSkyBlock.getSkyBlockWorld()).getApplicableRegions(
						pi.getIslandLocation());
				if (set.size() > 0) {
					for (ProtectedRegion regions : set) {
						if (!regions.getId().equalsIgnoreCase("__global__"))
							getWorldGuard().getRegionManager(
									uSkyBlock.getSkyBlockWorld()).removeRegion(
									regions.getId());
					}
				}
				getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld())
						.addRegion(region);
				System.out.print("New protected region created for " + player
						+ "'s Island by " + sender.getName());
				getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld())
						.save();
				return;
			}
			sender.sendMessage("플레이어는 섬을 가지고 있지않거나, 이미 보호 중입니다.");
		} catch (Exception ex) {
			System.out.print("ERROR: Failed to protect " + player
					+ "'s Island (" + sender.getName() + ")");
			ex.printStackTrace();
		}
	}

	public static void islandLock(CommandSender sender, String player) {
		try {
			if (getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld())
					.hasRegion(player + "Island")) {
				getWorldGuard()
						.getRegionManager(uSkyBlock.getSkyBlockWorld())
						.getRegion(player + "Island")
						.setFlag(
								DefaultFlag.ENTRY,
								DefaultFlag.ENTRY.parseInput(getWorldGuard(),
										sender, "deny"));
				sender.sendMessage(ChatColor.YELLOW
						+ "섬이 잠금되었습니다, 오직 리더만 가능합니다.");
				getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld())
						.save();
				return;
			}
		} catch (Exception ex) {
			System.out.print("ERROR: Failed to lock " + player + "'s Island ("
					+ sender.getName() + ")");
			ex.printStackTrace();
		}
	}

	public static void islandUnlock(CommandSender sender, String player) {
		try {
			if (getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld())
					.hasRegion(player + "Island")) {
				getWorldGuard()
						.getRegionManager(uSkyBlock.getSkyBlockWorld())
						.getRegion(player + "Island")
						.setFlag(
								DefaultFlag.ENTRY,
								DefaultFlag.ENTRY.parseInput(getWorldGuard(),
										sender, "allow"));
				sender.sendMessage(ChatColor.YELLOW
						+ "섬이 잠금 해제됩니다. 오직 섬 파티원과 본인만이 블럭을 수정할수 있습니다.");
				getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld())
						.save();
				return;
			}
		} catch (Exception ex) {
			System.out.print("ERROR: Failed to unlock " + player
					+ "'s Island (" + sender.getName() + ")");
			ex.printStackTrace();
		}
	}

	public static BlockVector getProtectionVectorLeft(Location island) {
		return new BlockVector(island.getX() + Settings.island_protectionRange
				/ 2, 255.0D, island.getZ() + Settings.island_protectionRange
				/ 2);
	}

	public static BlockVector getProtectionVectorRight(Location island) {
		return new BlockVector(island.getX() - Settings.island_protectionRange
				/ 2, 0.0D, island.getZ() - Settings.island_protectionRange / 2);
	}

	public static void removePlayerFromRegion(String owner, String player) {
		if (!getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld())
				.hasRegion(owner + "Island"))
			return;
		DefaultDomain owners = getWorldGuard()
				.getRegionManager(uSkyBlock.getSkyBlockWorld())
				.getRegion(owner + "Island").getOwners();
		owners.removePlayer(player);
		getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld())
				.getRegion(owner + "Island").setOwners(owners);
	}

	public static void addPlayerToOldRegion(String owner, String player) {
		if (!getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld())
				.hasRegion(owner + "Island"))
			return;
		DefaultDomain owners = getWorldGuard()
				.getRegionManager(uSkyBlock.getSkyBlockWorld())
				.getRegion(owner + "Island").getOwners();
		owners.addPlayer(player);
		getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld())
				.getRegion(owner + "Island").setOwners(owners);
	}

	public static void resetPlayerRegion(String owner) {
		if (!getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld())
				.hasRegion(owner + "Island"))
			return;
		DefaultDomain owners = new DefaultDomain();
		owners.addPlayer(owner);

		getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld())
				.getRegion(owner + "Island").setOwners(owners);
	}

	public static void transferRegion(String owner, String player,
			CommandSender sender) {
		try {
			ProtectedRegion region2 = null;
			region2 = new ProtectedCuboidRegion(
					player + "Island",
					getWorldGuard()
							.getRegionManager(Bukkit.getWorld("skyworld"))
							.getRegion(owner + "Island").getMinimumPoint(),
					getWorldGuard()
							.getRegionManager(
									Bukkit.getWorld(Settings.general_worldName))
							.getRegion(owner + "Island").getMaximumPoint());
			region2.setOwners(getWorldGuard()
					.getRegionManager(uSkyBlock.getSkyBlockWorld())
					.getRegion(owner + "Island").getOwners());
			region2.setParent(getWorldGuard().getRegionManager(
					uSkyBlock.getSkyBlockWorld()).getRegion("__Global__"));
			region2.setFlag(DefaultFlag.GREET_MESSAGE,
					DefaultFlag.GREET_MESSAGE.parseInput(getWorldGuard(),
							sender,
							"§d** You are entering a protected island area. ("
									+ player + ")"));
			region2.setFlag(DefaultFlag.FAREWELL_MESSAGE,
					DefaultFlag.FAREWELL_MESSAGE.parseInput(getWorldGuard(),
							sender,
							"§d** You are leaving a protected island area. ("
									+ player + ")"));
			region2.setFlag(DefaultFlag.PVP,
					DefaultFlag.PVP.parseInput(getWorldGuard(), sender, "deny"));
			region2.setFlag(DefaultFlag.DESTROY_VEHICLE,
					DefaultFlag.DESTROY_VEHICLE.parseInput(getWorldGuard(),
							sender, "deny"));
			region2.setFlag(DefaultFlag.ENTITY_ITEM_FRAME_DESTROY,
					DefaultFlag.ENTITY_ITEM_FRAME_DESTROY.parseInput(
							getWorldGuard(), sender, "deny"));
			region2.setFlag(DefaultFlag.ENTITY_PAINTING_DESTROY,
					DefaultFlag.ENTITY_PAINTING_DESTROY.parseInput(
							getWorldGuard(), sender, "deny"));
			getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld())
					.removeRegion(owner + "Island");
			getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld())
					.addRegion(region2);
		} catch (Exception e) {
			System.out
					.println("Error transferring WorldGuard Protected Region from ("
							+ owner + ") to (" + player + ")");
		}
	}
}