package us.talabrek.ultimateskyblock;

import java.io.File;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DevCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] split) {
		if (!(sender instanceof Player)) {
			return false;
		}
		Player player = (Player) sender;
		if (split.length == 0) {
			if ((VaultHandler.checkPerk(player.getName(), "usb.mod.protect",
					player.getWorld()))
					|| (VaultHandler.checkPerk(player.getName(),
							"usb.mod.protectall", player.getWorld()))
					|| (VaultHandler.checkPerk(player.getName(),
							"usb.mod.topten", player.getWorld()))
					|| (VaultHandler.checkPerk(player.getName(),
							"usb.mod.orphan", player.getWorld()))
					|| (VaultHandler.checkPerk(player.getName(),
							"usb.admin.delete", player.getWorld()))
					|| (VaultHandler.checkPerk(player.getName(),
							"usb.admin.remove", player.getWorld()))
					|| (VaultHandler.checkPerk(player.getName(),
							"usb.admin.register", player.getWorld()))
					|| (player.isOp())) {
				player.sendMessage("[dev usage]");
				if ((VaultHandler.checkPerk(player.getName(),
						"usb.mod.protect", player.getWorld()))
						|| (player.isOp()))
					player.sendMessage(ChatColor.GREEN
							+ "/dev protect <player>:" + ChatColor.GOLD
							+ " add protection to an island.");
				if ((VaultHandler.checkPerk(player.getName(),
						"usb.admin.reload", player.getWorld()))
						|| (player.isOp()))
					player.sendMessage(ChatColor.GREEN + "/dev reload:"
							+ ChatColor.GOLD
							+ " reload configuration from file.");
				if ((VaultHandler.checkPerk(player.getName(),
						"usb.mod.protectall", player.getWorld()))
						|| (player.isOp()))
					player.sendMessage(ChatColor.GREEN + "/dev protectall:"
							+ ChatColor.GOLD
							+ " add island protection to unprotected islands.");
				if ((VaultHandler.checkPerk(player.getName(), "usb.mod.topten",
						player.getWorld())) || (player.isOp()))
					player.sendMessage(ChatColor.GREEN + "/dev topten:"
							+ ChatColor.GOLD
							+ " manually update the top 10 list");
				if ((VaultHandler.checkPerk(player.getName(), "usb.mod.orphan",
						player.getWorld())) || (player.isOp()))
					player.sendMessage(ChatColor.GREEN + "/dev orphancount:"
							+ ChatColor.GOLD + " unused island locations count");
				if ((VaultHandler.checkPerk(player.getName(), "usb.mod.orphan",
						player.getWorld())) || (player.isOp()))
					player.sendMessage(ChatColor.GREEN + "/dev clearorphan:"
							+ ChatColor.GOLD
							+ " remove any unused island locations.");
				if ((VaultHandler.checkPerk(player.getName(), "usb.mod.orphan",
						player.getWorld())) || (player.isOp()))
					player.sendMessage(ChatColor.GREEN + "/dev saveorphan:"
							+ ChatColor.GOLD
							+ " save the list of old (empty) island locations.");
				if ((VaultHandler.checkPerk(player.getName(),
						"usb.admin.delete", player.getWorld()))
						|| (player.isOp()))
					player.sendMessage(ChatColor.GREEN
							+ "/dev delete <player>:" + ChatColor.GOLD
							+ " delete an island (removes blocks).");
				if ((VaultHandler.checkPerk(player.getName(),
						"usb.admin.remove", player.getWorld()))
						|| (player.isOp()))
					player.sendMessage(ChatColor.GREEN
							+ "/dev remove <player>:" + ChatColor.GOLD
							+ " remove a player from an island.");
				if ((VaultHandler.checkPerk(player.getName(),
						"usb.admin.register", player.getWorld()))
						|| (player.isOp()))
					player.sendMessage(ChatColor.GREEN
							+ "/dev register <player>:" + ChatColor.GOLD
							+ " set a player's island to your location");
				if ((VaultHandler.checkPerk(player.getName(),
						"usb.mod.challenges", player.getWorld()))
						|| (player.isOp()))
					player.sendMessage(ChatColor.GREEN
							+ "/dev completechallenge <challengename> <player>:"
							+ ChatColor.GOLD + " marks a challenge as complete");
				if ((VaultHandler.checkPerk(player.getName(),
						"usb.mod.challenges", player.getWorld()))
						|| (player.isOp()))
					player.sendMessage(ChatColor.GREEN
							+ "/dev resetchallenge <challengename> <player>:"
							+ ChatColor.GOLD
							+ " marks a challenge as incomplete");
				if ((VaultHandler.checkPerk(player.getName(),
						"usb.mod.challenges", player.getWorld()))
						|| (player.isOp()))
					player.sendMessage(ChatColor.GREEN
							+ "/dev resetallchallenges <challengename>:"
							+ ChatColor.GOLD
							+ " resets all of the player's challenges");
				if ((VaultHandler.checkPerk(player.getName(),
						"usb.admin.purge", player.getWorld()))
						|| (player.isOp()))
					player.sendMessage(ChatColor.GREEN
							+ "/dev purge [TimeInDays]:"
							+ ChatColor.GOLD
							+ " delete inactive islands older than [TimeInDays].");
				if ((VaultHandler.checkPerk(player.getName(), "usb.mod.party",
						player.getWorld())) || (player.isOp()))
					player.sendMessage(ChatColor.GREEN
							+ "/dev buildpartylist:"
							+ ChatColor.GOLD
							+ " build a new party list (use this if parties are broken).");
				if ((VaultHandler.checkPerk(player.getName(), "usb.mod.party",
						player.getWorld())) || (player.isOp()))
					player.sendMessage(ChatColor.GREEN
							+ "/dev info <player>:"
							+ ChatColor.GOLD
							+ " check the party information for the given player.");
			} else {
				player.sendMessage(ChatColor.RED + "이 명령을 사용할 수있는 권한이 없습니다.");
			}
			return true;
		}

		if ((split[0].equals("clearorphan"))
				&& ((VaultHandler.checkPerk(player.getName(), "usb.mod.orphan",
						player.getWorld())) || (player.isOp()))) {
			player.sendMessage(ChatColor.GREEN + "모든 이전 (빈)섬의 위치를 지우기.");
			uSkyBlock.getInstance().clearOrphanedIsland();
		} else if ((split[0].equals("protectall"))
				&& ((VaultHandler.checkPerk(player.getName(),
						"usb.mod.protectall", player.getWorld())) || (player
						.isOp()))) {
			player.sendMessage(ChatColor.GREEN
					+ "이 명령어는 WorldGuard을 사용하는 경우에만 사용할 수 있습니다.");
			if (Settings.island_protectWithWorldGuard) {
				player.sendMessage(ChatColor.GREEN + "이 명령은 사용할 수 없습니다.");
			} else
				player.sendMessage(ChatColor.RED
						+ "이 작업을 사용하는 config.yml에 WorldGuard 보호를 사용하도록 설정해야합니다!");
		} else if ((split[0].equals("buildislandlist"))
				&& ((VaultHandler.checkPerk(player.getName(),
						"usb.mod.protectall", player.getWorld())) || (player
						.isOp()))) {
			player.sendMessage(ChatColor.GREEN + "Building island list..");
			uSkyBlock.getInstance().buildIslandList();
			player.sendMessage(ChatColor.GREEN
					+ "Finished building island list..");
		} else if ((split[0].equals("orphancount"))
				&& ((VaultHandler.checkPerk(player.getName(), "usb.mod.orphan",
						player.getWorld())) || (player.isOp()))) {
			player.sendMessage(ChatColor.GREEN.toString()
					+ uSkyBlock.getInstance().orphanCount()
					+ " old island locations will be used before new ones.");
		} else if ((split[0].equals("reload"))
				&& ((VaultHandler.checkPerk(player.getName(),
						"usb.admin.reload", player.getWorld())) || (player
						.isOp()))) {
			uSkyBlock.getInstance().reloadConfig();
			uSkyBlock.getInstance().loadPluginConfig();
			uSkyBlock.getInstance().reloadLevelConfig();
			uSkyBlock.getInstance().loadLevelConfig();
			player.sendMessage(ChatColor.GREEN
					+ "Configuration reloaded from file.");
		} else if ((split[0].equals("saveorphan"))
				&& ((VaultHandler.checkPerk(player.getName(), "usb.mod.orphan",
						player.getWorld())) || (player.isOp()))) {
			player.sendMessage(ChatColor.GREEN + "Saving the orphan list.");
			uSkyBlock.getInstance().saveOrphans();
		} else if ((split[0].equals("topten"))
				&& ((VaultHandler.checkPerk(player.getName(), "usb.mod.topten",
						player.getWorld())) || (player.isOp()))) {
			player.sendMessage(ChatColor.GREEN + "Generating the Top Ten list");
			uSkyBlock.getInstance().updateTopTen(
					uSkyBlock.getInstance().generateTopTen());
			player.sendMessage(ChatColor.GREEN
					+ "Finished generation of the Top Ten list");
		} else {
			if ((split[0].equals("purge"))
					&& ((VaultHandler.checkPerk(player.getName(),
							"usb.admin.purge", player.getWorld())) || (player
							.isOp()))) {
				if (uSkyBlock.getInstance().isPurgeActive()) {
					player.sendMessage(ChatColor.RED
							+ "A purge is already running, please wait for it to finish!");
					return true;
				}
				player.sendMessage(ChatColor.GREEN
						+ "Usage: /dev purge [TimeInDays]");
				return true;
			}
			if (split.length == 2)
				if ((split[0].equals("purge"))
						&& ((VaultHandler.checkPerk(player.getName(),
								"usb.admin.purge", player.getWorld())) || (player
								.isOp()))) {
					if (uSkyBlock.getInstance().isPurgeActive()) {
						player.sendMessage(ChatColor.RED
								+ "A purge is already running, please wait for it to finish!");
						return true;
					}
					uSkyBlock.getInstance().activatePurge();
					final int time = Integer.parseInt(split[1]) * 24;
					player.sendMessage(ChatColor.GREEN
							+ "Marking all islands inactive for more than "
							+ split[1] + " days.");
					uSkyBlock
							.getInstance()
							.getServer()
							.getScheduler()
							.runTaskAsynchronously(uSkyBlock.getInstance(),
									new Runnable() {
										public void run() {
											File directoryPlayers = new File(
													uSkyBlock.getInstance()
															.getDataFolder()
															+ File.separator
															+ "players");

											long offlineTime = 0L;

											for (File child : directoryPlayers
													.listFiles()) {
												if ((Bukkit
														.getOfflinePlayer(child
																.getName()) == null)
														|| (Bukkit
																.getPlayer(child
																		.getName()) != null))
													continue;
												OfflinePlayer oplayer = Bukkit
														.getOfflinePlayer(child
																.getName());
												offlineTime = oplayer
														.getLastPlayed();
												offlineTime = (System
														.currentTimeMillis() - offlineTime) / 3600000L;
												if ((offlineTime <= time)
														|| (!uSkyBlock
																.getInstance()
																.hasIsland(
																		oplayer.getName())))
													continue;
												PlayerInfo pi = new PlayerInfo(
														oplayer.getName());
												if (!pi.getHasIsland())
													continue;
												if (pi.getHasParty())
													continue;
												if ((uSkyBlock
														.getInstance()
														.getTempIslandConfig(
																pi.locationForParty()) == null)
														|| (uSkyBlock
																.getInstance()
																.getTempIslandConfig(
																		pi.locationForParty())
																.getInt("general.level") >= 10)
														|| (child.getName() == null))
													continue;
												uSkyBlock
														.getInstance()
														.addToRemoveList(
																child.getName());
											}

											System.out.print("Removing "
													+ uSkyBlock.getInstance()
															.getRemoveList()
															.size()
													+ " inactive islands.");
											uSkyBlock
													.getInstance()
													.getServer()
													.getScheduler()
													.scheduleSyncRepeatingTask(
															uSkyBlock
																	.getInstance(),
															new Runnable() {
																public void run() {
																	if ((uSkyBlock
																			.getInstance()
																			.getRemoveList()
																			.size() > 0)
																			&& (uSkyBlock
																					.getInstance()
																					.isPurgeActive())) {
																		uSkyBlock
																				.getInstance()
																				.deletePlayerIsland(
																						(String) uSkyBlock
																								.getInstance()
																								.getRemoveList()
																								.get(0));
																		System.out
																				.print("[uSkyBlock] Purge: Removing "
																						+ (String) uSkyBlock
																								.getInstance()
																								.getRemoveList()
																								.get(0)
																						+ "'s island");
																		uSkyBlock
																				.getInstance()
																				.deleteFromRemoveList();
																	}

																	if ((uSkyBlock
																			.getInstance()
																			.getRemoveList()
																			.size() != 0)
																			|| (!uSkyBlock
																					.getInstance()
																					.isPurgeActive()))
																		return;
																	uSkyBlock
																			.getInstance()
																			.deactivatePurge();
																	System.out
																			.print("[uSkyBlock] Finished purging marked inactive islands.");
																}
															}, 0L, 20L);
										}
									});
				} else if ((split[0].equals("goto"))
						&& ((VaultHandler.checkPerk(player.getName(),
								"usb.mod.goto", player.getWorld())) || (player
								.isOp()))) {
					PlayerInfo pi = new PlayerInfo(split[1]);
					if (!pi.getHasIsland()) {
						player.sendMessage(ChatColor.RED
								+ "Error: Invalid Player (check spelling)");
					} else {
						if (pi.getHomeLocation() != null) {
							player.sendMessage(ChatColor.AQUA
									+ "Teleporting to " + split[1]
									+ "'s island.");
							player.teleport(pi.getHomeLocation());
							return true;
						}
						if (pi.getIslandLocation() != null) {
							player.sendMessage(ChatColor.AQUA
									+ "Teleporting to " + split[1]
									+ "'s island.");
							player.teleport(pi.getIslandLocation());
							return true;
						}
						player.sendMessage("Error: That player does not have an island!");
					}
				} else if ((split[0].equals("remove"))
						&& ((VaultHandler.checkPerk(player.getName(),
								"usb.admin.remove", player.getWorld())) || (player
								.isOp()))) {
					PlayerInfo pi = new PlayerInfo(split[1]);
					if (!pi.getHasIsland()) {
						player.sendMessage(ChatColor.RED
								+ "Error: Invalid Player (check spelling)");
					} else {
						if (pi.getIslandLocation() != null) {
							player.sendMessage(ChatColor.GREEN + "Removing "
									+ split[1] + "'s island.");
							uSkyBlock.getInstance().devDeletePlayerIsland(
									split[1]);
							return true;
						}
						player.sendMessage("Error: That player does not have an island!");
					}
				} else if ((split[0].equals("delete"))
						&& ((VaultHandler.checkPerk(player.getName(),
								"usb.admin.delete", player.getWorld())) || (player
								.isOp()))) {
					PlayerInfo pi = new PlayerInfo(split[1]);
					if (!pi.getHasIsland()) {
						player.sendMessage(ChatColor.RED
								+ "Error: Invalid Player (check spelling)");
					} else {
						if (pi.getIslandLocation() != null) {
							player.sendMessage(ChatColor.GREEN + "Removing "
									+ split[1] + "'s island.");
							uSkyBlock.getInstance()
									.deletePlayerIsland(split[1]);
							return true;
						}
						player.sendMessage("Error: That player does not have an island!");
					}
				} else if ((split[0].equals("register"))
						&& ((VaultHandler.checkPerk(player.getName(),
								"usb.admin.register", player.getWorld())) || (player
								.isOp()))) {
					PlayerInfo pi = new PlayerInfo(split[1]);
					if (pi.getHasIsland())
						uSkyBlock.getInstance().devDeletePlayerIsland(split[1]);
					if (uSkyBlock.getInstance().devSetPlayerIsland(player,
							player.getLocation(), split[1])) {
						player.sendMessage(ChatColor.AQUA + "Set " + split[1]
								+ "'s island to the bedrock nearest you.");
					} else
						player.sendMessage(ChatColor.RED
								+ "Bedrock not found: unable to set the island!");
				} else if ((split[0].equals("resetallchallenges"))
						&& ((VaultHandler.checkPerk(player.getName(),
								"usb.mod.challenges", player.getWorld())) || (player
								.isOp()))) {
					if (!uSkyBlock.getInstance().getActivePlayers()
							.containsKey(split[1])) {
						PlayerInfo pi = new PlayerInfo(split[1]);
						if (!pi.getHasIsland()) {
							player.sendMessage(ChatColor.RED
									+ "Error: Invalid Player (check spelling)");
							return true;
						}
						pi.resetAllChallenges();
						pi.savePlayerConfig(split[1]);
						player.sendMessage(ChatColor.GREEN + split[1]
								+ " has had all challenges reset.");
					} else {
						((PlayerInfo) uSkyBlock.getInstance()
								.getActivePlayers().get(split[1]))
								.resetAllChallenges();
						player.sendMessage(ChatColor.GREEN + split[1]
								+ " has had all challenges reset.");
					}
				} else if ((split[0].equals("setbiome"))
						&& ((VaultHandler.checkPerk(player.getName(),
								"usb.mod.setbiome", player.getWorld())) || (player
								.isOp()))) {
					if (!uSkyBlock.getInstance().getActivePlayers()
							.containsKey(split[1])) {
						PlayerInfo pi = new PlayerInfo(split[1]);
						if (!pi.getHasIsland()) {
							player.sendMessage(ChatColor.RED
									+ "Error: Invalid Player (check spelling)");
							return true;
						}
						uSkyBlock.getInstance().setBiome(
								pi.getIslandLocation(), "OCEAN");
						pi.savePlayerConfig(split[1]);
						player.sendMessage(ChatColor.GREEN + split[1]
								+ " has had their biome changed to OCEAN.");
					} else {
						uSkyBlock
								.getInstance()
								.setBiome(
										((PlayerInfo) uSkyBlock.getInstance()
												.getActivePlayers()
												.get(split[1]))
												.getIslandLocation(),
										"OCEAN");
						player.sendMessage(ChatColor.GREEN + split[1]
								+ " has had their biome changed to OCEAN.");
					}
				} else if (split[0].equals("info")
						&& (VaultHandler.checkPerk(player.getName(),
								"usb.mod.party", player.getWorld()) || player
								.isOp())) {
					PlayerInfo pi = new PlayerInfo(split[1]);
					if (!pi.getHasIsland()) {
						player.sendMessage(ChatColor.RED
								+ "Error: Invalid Player (check spelling)");
						return true;
					}
					String total = "";
					total = total
							+ "§b<"
							+ uSkyBlock.getInstance()
									.getIslandConfig(pi.locationForParty())
									.getString("party.leader") + "> ";
					Iterator<String> tempIt = uSkyBlock.getInstance()
							.getIslandConfig(pi.locationForParty())
							.getConfigurationSection("party.members")
							.getKeys(false).iterator();
					while (tempIt.hasNext()) {
						String temp = (String) tempIt.next();
						if (temp.equalsIgnoreCase(uSkyBlock.getInstance()
								.getIslandConfig(pi.locationForParty())
								.getString("party.leader")))
							continue;
						total = total + "§a[" + temp + "]";
					}
					player.sendMessage(total);
					player.sendMessage((new StringBuilder())
							.append(ChatColor.YELLOW)
							.append("Island Location:").append(ChatColor.WHITE)
							.append(" (")
							.append(pi.getPartyIslandLocation().getBlockX())
							.append(",")
							.append(pi.getPartyIslandLocation().getBlockY())
							.append(",")
							.append(pi.getPartyIslandLocation().getBlockZ())
							.append(")").toString());
				}
			if (split.length == 3) {
				if ((split[0].equals("completechallenge"))
						&& ((VaultHandler.checkPerk(player.getName(),
								"usb.mod.challenges", player.getWorld())) || (player
								.isOp()))) {
					if (!uSkyBlock.getInstance().getActivePlayers()
							.containsKey(split[2])) {
						PlayerInfo pi = new PlayerInfo(split[2]);
						if (!pi.getHasIsland()) {
							player.sendMessage(ChatColor.RED
									+ "Error: Invalid Player (check spelling)");
							return true;
						}
						if ((pi.checkChallenge(split[1].toLowerCase()) > 0)
								|| (!pi.challengeExists(split[1].toLowerCase()))) {
							player.sendMessage(ChatColor.RED
									+ "Challenge doesn't exist or is already completed");
							return true;
						}
						pi.completeChallenge(split[1].toLowerCase());
						pi.savePlayerConfig(split[2]);
						player.sendMessage(ChatColor.GREEN + "challange: "
								+ split[1].toLowerCase()
								+ " has been completed for " + split[2]);
					} else {
						if ((((PlayerInfo) uSkyBlock.getInstance()
								.getActivePlayers().get(split[2]))
								.checkChallenge(split[1].toLowerCase()) > 0)
								|| (!((PlayerInfo) uSkyBlock.getInstance()
										.getActivePlayers().get(split[2]))
										.challengeExists(split[1].toLowerCase()))) {
							player.sendMessage(ChatColor.RED
									+ "Challenge doesn't exist or is already completed");
							return true;
						}
						((PlayerInfo) uSkyBlock.getInstance()
								.getActivePlayers().get(split[2]))
								.completeChallenge(split[1].toLowerCase());
						player.sendMessage(ChatColor.GREEN + "challange: "
								+ split[1].toLowerCase()
								+ " has been completed for " + split[2]);
					}
				} else if ((split[0].equals("resetchallenge"))
						&& ((VaultHandler.checkPerk(player.getName(),
								"usb.mod.challenges", player.getWorld())) || (player
								.isOp()))) {
					if (!uSkyBlock.getInstance().getActivePlayers()
							.containsKey(split[2])) {
						PlayerInfo pi = new PlayerInfo(split[2]);
						if (!pi.getHasIsland()) {
							player.sendMessage(ChatColor.RED
									+ "Error: Invalid Player (check spelling)");
							return true;
						}
						if ((pi.checkChallenge(split[1].toLowerCase()) == 0)
								|| (!pi.challengeExists(split[1].toLowerCase()))) {
							player.sendMessage(ChatColor.RED
									+ "Challenge doesn't exist or isn't yet completed");
							return true;
						}
						pi.resetChallenge(split[1].toLowerCase());
						pi.savePlayerConfig(split[2]);
						player.sendMessage(ChatColor.GREEN + "challange: "
								+ split[1].toLowerCase()
								+ " has been reset for " + split[2]);
					} else {
						if ((((PlayerInfo) uSkyBlock.getInstance()
								.getActivePlayers().get(split[2]))
								.checkChallenge(split[1].toLowerCase()) == 0)
								|| (!((PlayerInfo) uSkyBlock.getInstance()
										.getActivePlayers().get(split[2]))
										.challengeExists(split[1].toLowerCase()))) {
							player.sendMessage(ChatColor.RED
									+ "Challenge doesn't exist or isn't yet completed");
							return true;
						}
						((PlayerInfo) uSkyBlock.getInstance()
								.getActivePlayers().get(split[2]))
								.resetChallenge(split[1].toLowerCase());
						player.sendMessage(ChatColor.GREEN + "challange: "
								+ split[1].toLowerCase()
								+ " has been completed for " + split[2]);
					}
				} else if ((split[0].equals("setbiome"))
						&& ((VaultHandler.checkPerk(player.getName(),
								"usb.mod.setbiome", player.getWorld())) || (player
								.isOp()))) {
					if (!uSkyBlock.getInstance().getActivePlayers()
							.containsKey(split[1])) {
						PlayerInfo pi = new PlayerInfo(split[1]);
						if (!pi.getHasIsland()) {
							player.sendMessage(ChatColor.RED
									+ "Error: Invalid Player (check spelling)");
							return true;
						}
						if (uSkyBlock.getInstance().setBiome(
								pi.getIslandLocation(), split[2])) {
							player.sendMessage(ChatColor.GREEN + split[1]
									+ " has had their biome changed to "
									+ split[2].toUpperCase() + ".");
						} else
							player.sendMessage(ChatColor.GREEN + split[1]
									+ " has had their biome changed to OCEAN.");
						pi.savePlayerConfig(split[1]);
					} else if (uSkyBlock
							.getInstance()
							.setBiome(
									((PlayerInfo) uSkyBlock.getInstance()
											.getActivePlayers().get(split[1]))
											.getIslandLocation(),
									split[2])) {
						player.sendMessage(ChatColor.GREEN + split[1]
								+ " has had their biome changed to "
								+ split[2].toUpperCase() + ".");
					} else {
						player.sendMessage(ChatColor.GREEN + split[1]
								+ " has had their biome changed to OCEAN.");
					}
				}
			}
		}
		return true;
	}
}