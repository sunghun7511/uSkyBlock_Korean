package us.talabrek.ultimateskyblock;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IslandCommand implements CommandExecutor {
	public Location Islandlocation;
	private List<String> banList;
	private String tempTargetPlayer;
	public boolean allowInfo = true;
	Set<String> memberList = null;
	private HashMap<String, String> inviteList = new HashMap<String, String>();
	String tPlayer;

	public IslandCommand() {
		this.inviteList.put("NoInvited", "NoInviter");
	}

	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] split) {
		if (!(sender instanceof Player)) {
			return false;
		}
		Player player = (Player) sender;

		PlayerInfo pi = (PlayerInfo) uSkyBlock.getInstance().getActivePlayers()
				.get(player.getName());
		if (pi == null) {
			player.sendMessage(ChatColor.BOLD + "" + ChatColor.DARK_RED + "[ 오류  ] "
					+ ChatColor.RESET + ChatColor.GREEN
					+ "이 플레어이어의 데이터에 접근 할 수 없습니다.");
			return true;
		}
		String iName = "";
		if (pi.getIslandLocation() != null)
			iName = pi.locationForParty();
		if (split.length == 0) {
			uSkyBlock.getInstance().updatePartyNumber(player);
			player.openInventory(uSkyBlock.getInstance().displayIslandGUI(
					player));

			return true;
		}
		if (split.length == 1) {
			if (((split[0].equals("재시작")) || (split[0].equals("restart")) || (split[0]
					.equals("reset"))) && (pi.getIslandLocation() != null)) {
				if (uSkyBlock.getInstance().getIslandConfig(iName)
						.getInt("party.currentSize") > 1) {
					if (!uSkyBlock.getInstance().getIslandConfig(iName)
							.getString("party.leader")
							.equalsIgnoreCase(player.getName()))
						player.sendMessage(ChatColor.BOLD + "" + ChatColor.DARK_RED
								+ "[ 경고  ] " + ChatColor.RESET
								+ ChatColor.GREEN + "섬의 소유자만이 섬을 재시작할수 있습니다.");
					else
						player.sendMessage(ChatColor.BOLD + "" + ChatColor.DARK_RED
								+ "[ 경고  ] " + ChatColor.RESET
								+ ChatColor.GREEN
								+ "섬을 초기화하기전 파티원들을 모두 내보내야 합니다.");
					return true;
				}
				if ((!uSkyBlock.getInstance().onRestartCooldown(player))
						|| (Settings.general_cooldownRestart == 0)) {
					uSkyBlock.getInstance().restartPlayerIsland(player,
							pi.getIslandLocation());
					uSkyBlock.getInstance().setRestartCooldown(player);
					return true;
				}

				player.sendMessage(ChatColor.GREEN
						+ "당신은 당신의 섬을 "
						+ uSkyBlock.getInstance()
								.getRestartCooldownTime(player) / 1000L
						+ "초 뒤에 다시 재시작할수 있습니다.");
				return true;
			}
			if ((split[0].equals("셋홈"))
					|| (((split[0].equals("sethome")) || (split[0]
							.equals("tpset")))
							&& (pi.getIslandLocation() != null) && (VaultHandler
								.checkPerk(player.getName(),
										"usb.island.sethome", player.getWorld())))) {
				uSkyBlock.getInstance().homeSet(player);
				return true;
			}
			if ((split[0].equals("로그"))
					|| (((split[0].equals("log")) || (split[0].equals("l")))
							&& (pi.getIslandLocation() != null) && (VaultHandler
								.checkPerk(player.getName(),
										"usb.island.create", player.getWorld())))) {
				player.openInventory(uSkyBlock.getInstance().displayLogGUI(
						player));
				return true;
			}
			if ((split[0].equals("생성"))
					|| (((split[0].equals("create")) || (split[0].equals("c"))) && (VaultHandler
							.checkPerk(player.getName(), "usb.island.create",
									player.getWorld())))) {
				if (pi.getIslandLocation() == null || ((pi.getIslandLocation().getBlockX() == 0)
						&& (pi.getIslandLocation().getBlockY() == 0)
						&& (pi.getIslandLocation().getBlockZ() == 0))) {
					uSkyBlock.getInstance().createIsland(sender, pi);
					return true;
				}
				return true;
			}
			if (((split[0].equals("홈")) || (split[0].equals("home")) || (split[0]
					.equals("h")))
					&& (pi.getIslandLocation() != null)
					&& (VaultHandler.checkPerk(player.getName(),
							"usb.island.sethome", player.getWorld()))) {
				if (pi.getHomeLocation() == null) {
					((PlayerInfo) uSkyBlock.getInstance().getActivePlayers()
							.get(player.getName())).setHomeLocation(pi
							.getIslandLocation());
				}
				uSkyBlock.getInstance().homeTeleport(player);
				return true;
			}
			if (((split[0].equals("셋워프")) || (split[0].equals("setwarp")) || (split[0]
					.equals("warpset")))
					&& (pi.getIslandLocation() != null)
					&& (VaultHandler.checkPerk(player.getName(),
							"usb.extra.addwarp", player.getWorld()))) {
				if (uSkyBlock
						.getInstance()
						.getIslandConfig(iName)
						.getBoolean(
								"party.members." + player.getName()
										+ ".canChangeWarp")) {
					uSkyBlock.getInstance().sendMessageToIslandGroup(iName,
							player.getName() + "님이 섬 워프 위치를 변경했습니다.");
					uSkyBlock.getInstance().warpSet(player);
				} else {
					player.sendMessage("§c당신은 섬의 워프 포인트를 설정 할 수있는 권한이 없습니다!");
				}
				return true;
			}
			if ((split[0].equals("warp")) || (split[0].equals("w"))
					|| (split[0].equals("워프"))) {
				if (VaultHandler.checkPerk(player.getName(),
						"usb.extra.addwarp", player.getWorld())) {
					if (uSkyBlock.getInstance().getIslandConfig(iName)
							.getBoolean("general.warpActive")) {
						player.sendMessage(ChatColor.AQUA
								+ "당신의 섬에 워프할수 있습니다. ");
					} else {
						player.sendMessage(ChatColor.RED + "당신의 섬에 워프할수 있습니다. ");
					}
					player.sendMessage(ChatColor.GOLD
							+ "자신의 섬의 워프 지점, 워프를  설정하려면, 섬 메뉴를 이용하세요!");
				} else {
					player.sendMessage(ChatColor.RED
							+ "당신은 당신의 섬에 워프를 만들 수있는 권한이 없습니다!");
				}
				if (VaultHandler.checkPerk(player.getName(), "usb.island.warp",
						player.getWorld())) {
					player.sendMessage(ChatColor.GOLD
							+ "다른 플레이어의 섬에 워프하고 싶다면, " + ChatColor.GREEN
							+ "/섬 워프 <이름>");
				} else {
					player.sendMessage(ChatColor.RED
							+ "당신은 다른 섬에 워프 할 수있는 권한이 없습니다!");
				}
				return true;
			}
			if (((split[0].equals("워프상태")) || (split[0].equals("togglewarp")) || (split[0]
					.equals("tw"))) && (pi.getIslandLocation() != null)) {
				if (VaultHandler.checkPerk(player.getName(),
						"usb.extra.addwarp", player.getWorld())) {
					if (uSkyBlock
							.getInstance()
							.getIslandConfig(iName)
							.getBoolean(
									"party.members." + player.getName()
											+ ".canToggleWarp")) {
						if (!uSkyBlock.getInstance().getIslandConfig(iName)
								.getBoolean("general.warpActive")) {
							if (uSkyBlock.getInstance().getIslandConfig(iName)
									.getBoolean("general.locked")) {
								player.sendMessage(ChatColor.RED
										+ "당신의 섬이 잠겨 있습니다. 당신은 당신의 워프상태를 사용하기 전에 잠금을 해제해야합니다.");
								return true;
							}
							uSkyBlock.getInstance().sendMessageToIslandGroup(
									iName,
									player.getName() + "님이 워프기능을 활성화 했습니다.");
							uSkyBlock
									.getInstance()
									.getIslandConfig(iName)
									.set("general.warpActive",
											Boolean.valueOf(true));
						} else {
							uSkyBlock.getInstance().sendMessageToIslandGroup(
									iName,
									player.getName() + "님이 워프기능을 비활성화 했습니다.");
							uSkyBlock
									.getInstance()
									.getIslandConfig(iName)
									.set("general.warpActive",
											Boolean.valueOf(false));
						}
					} else {
						player.sendMessage("§c당신은 섬의 워프를 활성화 / 비활성화 할 수있는 권한이 없습니다!");
					}
				} else {
					player.sendMessage(ChatColor.RED
							+ "당신은 당신의 섬에 워프를 만들 수있는 권한이 없습니다!");
				}
				uSkyBlock.getInstance().getActivePlayers()
						.put(player.getName(), pi);
				return true;
			}
			if (((split[0].equals("ban")) || (split[0].equals("banned"))
					|| (split[0].equals("banlist")) || (split[0].equals("b")) || (split[0]
						.equals("밴"))) && (pi.getIslandLocation() != null)) {
				if (VaultHandler.checkPerk(player.getName(), "usb.island.ban",
						player.getWorld())) {
					player.sendMessage(ChatColor.GREEN
							+ "이 플레이어들은 당신의 섬에 워프가 금지됩니다.");
					player.sendMessage(ChatColor.RED + getBanList(player));
					player.sendMessage(ChatColor.GREEN
							+ "당신의 섬에 밴, 언밴을 하실랴면 /섬 밴 <플레이어>");
				} else {
					player.sendMessage(ChatColor.RED
							+ "당신은 당신의 섬에서 플레이어 워프를 금지 할 수있는 권한이 없습니다!");
				}
				return true;
			}
			if (((split[0].equals("잠금")) || (split[0].equals("lock")))
					&& (pi.getIslandLocation() != null)) {
				if ((Settings.island_allowIslandLock)
						&& (VaultHandler.checkPerk(player.getName(),
								"usb.lock", player.getWorld()))) {
					if (uSkyBlock
							.getInstance()
							.getIslandConfig(iName)
							.getBoolean(
									"party.members." + player.getName()
											+ ".canToggleLock")) {
						WorldGuardHandler.islandLock(sender, uSkyBlock
								.getInstance().getIslandConfig(iName)
								.getString("party.leader"));
						uSkyBlock.getInstance().getIslandConfig(iName)
								.set("general.locked", Boolean.valueOf(true));
						uSkyBlock.getInstance().sendMessageToIslandGroup(iName,
								player.getName() + " 님이 섬을 잠금하셨습니다.");
						if (uSkyBlock.getInstance().getIslandConfig(iName)
								.getBoolean("general.warpActive")) {
							uSkyBlock
									.getInstance()
									.getIslandConfig(iName)
									.set("general.warpActive",
											Boolean.valueOf(false));
							player.sendMessage(ChatColor.RED
									+ "당신의 섬이 잠겨 있기 때문에 들어오는 워프가 비활성화되었습니다.");
							uSkyBlock.getInstance().sendMessageToIslandGroup(
									iName, player.getName() + " 섬의 워프를 비활성화.");
						}
						uSkyBlock.getInstance().getActivePlayers()
								.put(player.getName(), pi);
						uSkyBlock.getInstance().saveIslandConfig(iName);
					} else {
						player.sendMessage(ChatColor.RED
								+ "당신은 당신의 섬을 잠글 수있는 권한이 없습니다!");
					}
				} else
					player.sendMessage(ChatColor.RED + "이 명령에 접근 할 수 없습니다!");
				return true;
			}
			if (((split[0].equals("잠금해제")) || (split[0].equals("unlock")))
					&& (pi.getIslandLocation() != null)) {
				if ((Settings.island_allowIslandLock)
						&& (VaultHandler.checkPerk(player.getName(),
								"usb.lock", player.getWorld()))) {
					if (uSkyBlock
							.getInstance()
							.getIslandConfig(iName)
							.getBoolean(
									"party.members." + player.getName()
											+ ".canToggleLock")) {
						WorldGuardHandler.islandUnlock(sender, uSkyBlock
								.getInstance().getIslandConfig(iName)
								.getString("party.leader"));
						uSkyBlock.getInstance().getIslandConfig(iName)
								.set("general.locked", Boolean.valueOf(false));
						uSkyBlock.getInstance().sendMessageToIslandGroup(iName,
								player.getName() + " 님이 섬을 잠금해제하셨습니다.");
						uSkyBlock.getInstance().saveIslandConfig(iName);
					} else {
						player.sendMessage(ChatColor.RED
								+ "당신은 섬의 잠금을 해제 할 수있는 권한이 없습니다!");
					}
				} else
					player.sendMessage(ChatColor.RED + "이 명령에 접근 할 수 없습니다!");
				return true;
			}
			if ((split[0].equals("help")) || (split[0].equals("도움말"))) {
				player.sendMessage(ChatColor.AQUA + "[스카이 블럭 명령어]");

				player.sendMessage(ChatColor.GREEN + "/섬 :" + ChatColor.GOLD
						+ " 당신의 섬을 시작하거나, 당신이 섬으로 텔레포트 합니다.");
				player.sendMessage(ChatColor.GREEN + "/섬 재시작 :"
						+ ChatColor.GOLD + " 당신의 섬을 삭제하고 새로운 섬을 시작합니다.");
				player.sendMessage(ChatColor.GREEN + "/섬 셋홈 :"
						+ ChatColor.GOLD + " 당신의 섬의 텔레포트 지점을 설정합니다.");
				if (Settings.island_useIslandLevel) {
					player.sendMessage(ChatColor.GREEN + "/섬 레벨 :"
							+ ChatColor.GOLD + " 당신의 섬 레벨을 확인합니다.");
					player.sendMessage(ChatColor.GREEN + "/섬 레벨 <플레이어> :"
							+ ChatColor.GOLD + " 다른 플레이어의 섬의 레벨을 확인합니다.");
				}
				if (VaultHandler.checkPerk(player.getName(),
						"usb.party.create", player.getWorld())) {
					player.sendMessage(ChatColor.GREEN + "/섬 파티 :"
							+ ChatColor.GOLD + " 파티의 정보를 볼 수 있습니다.");
					player.sendMessage(ChatColor.GREEN + "/섬 초대 <플레이어>:"
							+ ChatColor.GOLD + " 당신의 섬에 플레이어를 초대합니다.");
					player.sendMessage(ChatColor.GREEN + "/섬 떠나기 :"
							+ ChatColor.GOLD + " 다른 플레이어의 섬을 떠납니다.");
				}
				if (VaultHandler.checkPerk(player.getName(), "usb.party.kick",
						player.getWorld())) {
					player.sendMessage(ChatColor.GREEN + "/섬 킥 <플레이어>:"
							+ ChatColor.GOLD + " 당신의 섬에서 플레이어를 제거합니다.");
				}
				if (VaultHandler.checkPerk(player.getName(), "usb.party.join",
						player.getWorld())) {
					player.sendMessage(ChatColor.GREEN + "/섬 [수락/거절]:"
							+ ChatColor.GOLD + " 초대를 수락하거나 거절할수 있습니다.");
				}
				if (VaultHandler.checkPerk(player.getName(),
						"usb.party.makeleader", player.getWorld())) {
					player.sendMessage(ChatColor.GREEN + "/섬 리더설정  <플레이어>:"
							+ ChatColor.GOLD + " 당신의 섬의 리더를 설정합니다.");
				}
				if (VaultHandler.checkPerk(player.getName(), "usb.island.warp",
						player.getWorld())) {
					player.sendMessage(ChatColor.GREEN + "/섬 워프 <플레이어> :"
							+ ChatColor.GOLD + " 다른 플레이어의 섬에 워프합니다.");
				}
				if (VaultHandler.checkPerk(player.getName(),
						"usb.extra.addwarp", player.getWorld())) {
					player.sendMessage(ChatColor.GREEN + "/섬 셋워프 :"
							+ ChatColor.GOLD + " 당신의 섬에 워프 위치를 설정합니다.");
					player.sendMessage(ChatColor.GREEN + "/섬 워프상태 :"
							+ ChatColor.GOLD + " 당신의 섬의 워프기능을 끄고 킵니다.");
				}
				if (VaultHandler.checkPerk(player.getName(), "usb.island.ban",
						player.getWorld())) {
					player.sendMessage(ChatColor.GREEN + "/섬 밴 <플레이어> :"
							+ ChatColor.GOLD + " 여러분의 섬에서 플레이어 접근을 허용 / 금지.");
				}
				player.sendMessage(ChatColor.GREEN + "/섬 랭킹 :"
						+ ChatColor.GOLD + " 섬 랭킹을 봅니다");
				if (Settings.island_allowIslandLock) {
					if (!VaultHandler.checkPerk(player.getName(), "usb.lock",
							player.getWorld())) {
						player.sendMessage(ChatColor.DARK_GRAY + "/섬 잠금 :"
								+ ChatColor.GRAY + " 섬을 잠궈버립니다.");
						player.sendMessage(ChatColor.DARK_GRAY + "/섬 잠금해제 :"
								+ ChatColor.GRAY + " 섬의 잠금을 해제합니다.");
					} else {
						player.sendMessage(ChatColor.GREEN + "/섬 잠금 :"
								+ ChatColor.GOLD + " 섬을 잠궈버립니다.");
						player.sendMessage(ChatColor.GREEN + "/섬 잠금해제 :"
								+ ChatColor.GOLD + " 섬의 잠금을 해제합니다.");
					}

				}

				if (Bukkit.getServer().getServerId()
						.equalsIgnoreCase("UltimateSkyblock")) {
					player.sendMessage(ChatColor.GREEN + "/dungeon :"
							+ ChatColor.GOLD
							+ " to warp to the dungeon world.");
					player.sendMessage(ChatColor.GREEN + "/fun :"
							+ ChatColor.GOLD
							+ " to warp to the Mini-Game/Fun world.");
					player.sendMessage(ChatColor.GREEN + "/pvp :"
							+ ChatColor.GOLD + " join a pvp match.");
					player.sendMessage(ChatColor.GREEN + "/spleef :"
							+ ChatColor.GOLD + " join spleef match.");
					player.sendMessage(ChatColor.GREEN + "/hub :"
							+ ChatColor.GOLD
							+ " warp to the world hub Sanconia.");
				}
				return true;
			}
			if ((split[0].equals("랭킹"))
					&& (VaultHandler.checkPerk(player.getName(),
							"usb.island.topten", player.getWorld()))) {
				uSkyBlock.getInstance().displayTopTen(player);
				return true;
			}
			if (((split[0].equals("바이옴")) || (split[0].equals("biome")) || (split[0]
					.equals("b"))) && (pi.getIslandLocation() != null)) {
				player.openInventory(uSkyBlock.getInstance().displayBiomeGUI(
						player));
				if (!uSkyBlock
						.getInstance()
						.getIslandConfig(iName)
						.getBoolean(
								"party.members." + player.getName()
										+ ".canToggleLock")) {
					player.sendMessage("§c당신은 당신의 현재 섬의 바이옴을 변경할 수있는 권한이 없습니다.");
				}
				return true;
			}
			if (((split[0].equals("info")) || (split[0].equals("level")))
					&& (pi.getIslandLocation() != null)
					&& (VaultHandler.checkPerk(player.getName(),
							"usb.island.info", player.getWorld()))
					&& (Settings.island_useIslandLevel)) {
				if (uSkyBlock.getInstance().playerIsOnIsland(player)) {
					if ((!uSkyBlock.getInstance().onInfoCooldown(player))
							|| (Settings.general_cooldownInfo == 0)) {
						uSkyBlock.getInstance().setInfoCooldown(player);
						if ((!pi.getHasParty()) && (!pi.getHasIsland())) {
							player.sendMessage(ChatColor.RED + "당신은 섬이 없습니다!");
						} else {
							for (int x = Settings.island_protectionRange / 2
									* -1 - 16; x <= Settings.island_protectionRange / 2 + 16; x += 16) {
								for (int z = Settings.island_protectionRange
										/ 2 * -1 - 16; z <= Settings.island_protectionRange / 2 + 16; z += 16) {
									uSkyBlock
											.getSkyBlockWorld()
											.loadChunk(
													(pi.getIslandLocation()
															.getBlockX() + x) / 16,
													(pi.getIslandLocation()
															.getBlockZ() + z) / 16);
								}
							}
							getIslandLevel(player, player.getName());
						}
						return true;
					}

					player.sendMessage(ChatColor.GREEN
							+ "당신의 해당 명령어를 "
							+ uSkyBlock.getInstance().getInfoCooldownTime(
									player) / 1000L + " 초뒤에 입력할수 있습니다.");
					return true;
				}

				player.sendMessage(ChatColor.GREEN
						+ "이 명령을 사용하기 위해 섬에 있어야합니다.");
				return true;
			}
			if (((split[0].equals("invite")) || (split[0].equals("초대")))
					&& (pi.getIslandLocation() != null)
					&& (VaultHandler.checkPerk(player.getName(),
							"usb.party.create", player.getWorld()))) {
				player.sendMessage(ChatColor.GREEN + "도움말, " + ChatColor.GOLD
						+ " /섬 초대 <플레이어>" + ChatColor.GREEN
						+ " 를 입력하시면, 플레이어를 초대할수 있습니다.");
				if (uSkyBlock.getInstance().hasParty(player.getName())) {
					if ((uSkyBlock.getInstance()
							.getIslandConfig(pi.locationForParty())
							.getString("party.leader").equalsIgnoreCase(player
							.getName()))
							|| (uSkyBlock.getInstance().getIslandConfig(
									pi.locationForParty())
									.getBoolean("party.members."
											+ player.getName()
											+ ".canInviteOthers"))) {
						if (VaultHandler.checkPerk(player.getName(),
								"usb.extra.partysize", player.getWorld())) {
							if (uSkyBlock.getInstance()
									.getIslandConfig(pi.locationForParty())
									.getInt("party.currentSize") < Settings.general_maxPartySize * 2) {
								player.sendMessage(ChatColor.AQUA
										+ "당신은 "
										+ (Settings.general_maxPartySize * 2 - uSkyBlock
												.getInstance()
												.getIslandConfig(
														pi.locationForParty())
												.getInt("party.currentSize"))
										+ "명을 더 초대할수 있습니다.");
							} else
								player.sendMessage(ChatColor.RED
										+ "당신은 더이상 초대를 할수 없습니다.");
							return true;
						}

						if (uSkyBlock.getInstance()
								.getIslandConfig(pi.locationForParty())
								.getInt("party.currentSize") < Settings.general_maxPartySize) {
							player.sendMessage(ChatColor.AQUA
									+ "당신은 "
									+ (Settings.general_maxPartySize - uSkyBlock
											.getInstance()
											.getIslandConfig(
													pi.locationForParty())
											.getInt("party.currentSize"))
									+ "명을 더 초대할수 있습니다.");
						} else
							player.sendMessage(ChatColor.RED
									+ "당신은 더이상 초대를 할수 없습니다.");
						return true;
					}

					player.sendMessage(ChatColor.RED
							+ "섬의 주인만 플레이어 초대를 할수있습니다.");
					return true;
				}

				return true;
			}
			if (((split[0].equals("accept")) || (split[0].equals("수락")))
					&& (VaultHandler.checkPerk(player.getName(),
							"usb.party.join", player.getWorld()))) {
				if ((uSkyBlock.getInstance().onRestartCooldown(player))
						&& (Settings.general_cooldownRestart > 0)) {
					player.sendMessage(ChatColor.GREEN
							+ "다른 섬의 초대 수락은 "
							+ uSkyBlock.getInstance().getRestartCooldownTime(
									player) / 1000L + "초 후에 할수있습니다.");
					return true;
				}
				if ((!uSkyBlock.getInstance().hasParty(player.getName()))
						&& (this.inviteList.containsKey(player.getName()))) {
					if (pi.getHasIsland()) {
						uSkyBlock.getInstance().deletePlayerIsland(
								player.getName());
					}
					player.sendMessage(ChatColor.AQUA
							+ "파티에 성공적으로, 등록되셨습니다. 섬 GUI에서 파티 정보를 확인할수 있습니다.");
					addPlayertoParty(player.getName(),
							(String) this.inviteList.get(player.getName()));
					if (Bukkit.getPlayer((String) this.inviteList.get(player
							.getName())) != null) {
						Bukkit.getPlayer(
								(String) this.inviteList.get(player.getName()))
								.sendMessage(
										ChatColor.AQUA + player.getName()
												+ "님이 파티에 들어오셨습니다.");
					} else {
						player.sendMessage(ChatColor.RED
								+ "당신은 섬을 가입할수 없습니다, (풀)");

						return true;
					}
					uSkyBlock.getInstance().setRestartCooldown(player);

					uSkyBlock.getInstance().homeTeleport(player);

					if ((Settings.island_protectWithWorldGuard)
							&& (Bukkit.getServer().getPluginManager()
									.isPluginEnabled("WorldGuard"))
							&& (WorldGuardHandler.getWorldGuard()
									.getRegionManager(
											uSkyBlock.getSkyBlockWorld())
									.hasRegion(uSkyBlock
											.getInstance()
											.getIslandConfig(
													((PlayerInfo) uSkyBlock
															.getInstance()
															.getActivePlayers()
															.get(this.inviteList.get(player
																	.getName())))
															.locationForParty())
											.getString("party.leader")
											+ "Island"))) {
						WorldGuardHandler
								.addPlayerToOldRegion(
										uSkyBlock
												.getInstance()
												.getIslandConfig(
														((PlayerInfo) uSkyBlock
																.getInstance()
																.getActivePlayers()
																.get(this.inviteList
																		.get(player
																				.getName())))
																.locationForParty())
												.getString("party.leader"),
										player.getName());
					}

					this.inviteList.remove(player.getName());
					return true;
				}

				player.sendMessage(ChatColor.RED + "당신은 지금 그 명령을 사용할 수 없습니다.");
				return true;
			}

			if ((split[0].equals("거부")) || (split[0].equals("reject"))) {
				if (this.inviteList.containsKey(player.getName())) {
					player.sendMessage(ChatColor.GREEN + "당신은 섬을 가입 초대를 거절했다.");
					if (Bukkit.getPlayer((String) this.inviteList.get(player
							.getName())) != null) {
						Bukkit.getPlayer(
								(String) this.inviteList.get(player.getName()))
								.sendMessage(
										ChatColor.RED
												+ player.getName()
												+ " has rejected your island invite!");
					}
					this.inviteList.remove(player.getName());
				} else {
					player.sendMessage(ChatColor.RED + "당신에게 들어온 초대장이 없습니다.");
				}
				return true;
			}

			if (split[0].equalsIgnoreCase("파티제거")) {
				if (VaultHandler.checkPerk(player.getName(), "usb.mod.party",
						player.getWorld())) {
					player.sendMessage(ChatColor.RED + "이 명령은 기능하지 않습니다 더 이상!");
				} else
					player.sendMessage(ChatColor.RED + "당신은 그 명령에 접근할 수 없습니다!");
				return true;
			}
			if (split[0].equalsIgnoreCase("파티클리어")) {
				if (VaultHandler.checkPerk(player.getName(), "usb.mod.party",
						player.getWorld())) {
					player.sendMessage(ChatColor.RED + "이 명령은 기능하지 않습니다 더 이상!");
				} else
					player.sendMessage(ChatColor.RED
							+ "당신은 그 명령에 액세스 할 수 없습니다!");
				return true;
			}
			if ((split[0].equalsIgnoreCase("초대장삭제"))
					|| (split[0].equalsIgnoreCase("purgeinvites"))) {
				if (VaultHandler.checkPerk(player.getName(), "usb.mod.party",
						player.getWorld())) {
					player.sendMessage(ChatColor.RED + "모든 초대장 제거");
					invitePurge();
				} else {
					player.sendMessage(ChatColor.RED
							+ "당신은 그 명령에 액세스 할 수 없습니다!");
				}
				return true;
			}
			if (split[0].equalsIgnoreCase("파티리스트")) {
				if (VaultHandler.checkPerk(player.getName(), "usb.mod.party",
						player.getWorld())) {
					player.sendMessage(ChatColor.RED
							+ "이 명령은 현재 활성화되어 있지 않습니다.");
				} else
					player.sendMessage(ChatColor.RED
							+ "당신은 그 명령에 액세스 할 수 없습니다!");
				return true;
			}
			if (split[0].equalsIgnoreCase("초대장리스트")) {
				if (VaultHandler.checkPerk(player.getName(), "usb.mod.party",
						player.getWorld())) {
					player.sendMessage(ChatColor.RED + "초대장 체크");
					inviteDebug(player);
				} else {
					player.sendMessage(ChatColor.RED
							+ "당신은 그 명령에 액세스 할 수 없습니다!");
				}
				return true;
			}

			if (((split[0].equals("떠나기")) || (split[0].equals("leave")))
					&& (pi.getIslandLocation() != null)
					&& (VaultHandler.checkPerk(player.getName(),
							"usb.party.join", player.getWorld()))) {
				if (player
						.getWorld()
						.getName()
						.equalsIgnoreCase(
								uSkyBlock.getSkyBlockWorld().getName())) {
					if (uSkyBlock.getInstance().hasParty(player.getName())) {
						if (uSkyBlock.getInstance().getIslandConfig(iName)
								.getString("party.leader")
								.equalsIgnoreCase(player.getName())) {
							player.sendMessage(ChatColor.GREEN
									+ "당신은이 섬을 소유, 사용 / 섬 대신 <플레이어> 제거합니다.");
							return true;
						}

						if (Settings.extras_sendToSpawn)
							player.performCommand("spawn");
						else {
							player.teleport(uSkyBlock.getSkyBlockWorld()
									.getSpawnLocation());
						}
						if ((Settings.island_protectWithWorldGuard)
								&& (Bukkit.getServer().getPluginManager()
										.isPluginEnabled("WorldGuard"))) {
							WorldGuardHandler.removePlayerFromRegion(
									uSkyBlock
											.getInstance()
											.getIslandConfig(
													pi.locationForParty())
											.getString("party.leader"),
									player.getName());
						}
						removePlayerFromParty(
								player.getName(),
								uSkyBlock.getInstance()
										.getIslandConfig(pi.locationForParty())
										.getString("party.leader"),
								pi.locationForParty());

						player.sendMessage(ChatColor.GREEN + "당신은 섬을 떠났습니다.");
						if (Bukkit.getPlayer(uSkyBlock.getInstance()
								.getIslandConfig(pi.locationForParty())
								.getString("party.leader")) != null) {
							Bukkit.getPlayer(
									uSkyBlock
											.getInstance()
											.getIslandConfig(
													pi.locationForParty())
											.getString("party.leader"))
									.sendMessage(
											ChatColor.RED + player.getName()
													+ " has left your island!");
						}
					} else {
						player.sendMessage(ChatColor.RED
								+ "섬이 없습니다.");
						return true;
					}
				} else {
					player.sendMessage(ChatColor.RED
							+ "이 명령어는 섬 월드에 있어야 사용가능합니다.");
				}
				return true;
			}
			if (((split[0].equals("파티")) || (split[0].equals("party")))
					&& (pi.getIslandLocation() != null)) {
				if (VaultHandler.checkPerk(player.getName(),
						"usb.party.create", player.getWorld())) {
					player.openInventory(uSkyBlock.getInstance()
							.displayPartyGUI(player));
				}

				player.sendMessage(ChatColor.GREEN + "당신의 섬 회원 목록 :");
				String total = "";
				this.memberList = uSkyBlock.getInstance()
						.getIslandConfig(pi.locationForParty())
						.getConfigurationSection("party.members")
						.getKeys(false);
				total = total
						+ "§b<"
						+ uSkyBlock.getInstance()
								.getIslandConfig(pi.locationForParty())
								.getString("party.leader") + "> ";
				Iterator<String> tempIt = this.memberList.iterator();
				while (tempIt.hasNext()) {
					String temp = (String) tempIt.next();
					if (temp.equalsIgnoreCase(uSkyBlock.getInstance()
							.getIslandConfig(pi.locationForParty())
							.getString("party.leader")))
						continue;
					total = total + "§a[" + temp + "]";
				}

				player.sendMessage(total);

				return true;
			}
		} else if (split.length == 2) {
			if (split[0].equalsIgnoreCase("리더설정")
					&& VaultHandler.checkPerk(player.getName(),
							"usb.party.makeleader", player.getWorld())) {
				if (Bukkit.getPlayer(split[1]) == null) {
					player.sendMessage(ChatColor.RED
							+ "해당 플레이어가 온라인 이어야 합니다.");
					return true;
				}

				if ((!uSkyBlock.getInstance().getActivePlayers()
						.containsKey(player.getName()))
						|| (!uSkyBlock
								.getInstance()
								.getActivePlayers()
								.containsKey(
										Bukkit.getPlayer(split[1])
												.getName()))) {
					player.sendMessage(ChatColor.RED + "플레이어가 온라인 이어야 합니다.");
					return true;
				}

				if (!uSkyBlock.getInstance()
								.getIslandConfig(iName)
								.contains("party.members." + player.getName())){
					player.sendMessage(ChatColor.RED + "팜원에게만 팜장을 위임할 수 있습니다.");
					return true;
				}
				String tempTargetPlayer = Bukkit.getPlayer(split[1])
						.getName();
				if (uSkyBlock.getInstance().hasParty(player.getName())) {
					if (uSkyBlock.getInstance()
							.getIslandConfig(pi.locationForParty())
							.getString("party.leader")
							.equalsIgnoreCase(player.getName())) {
						if (uSkyBlock.getInstance()
								.getIslandConfig(pi.locationForParty())
								.contains("party.members." + tempTargetPlayer)) {

							if (Bukkit.getPlayer(split[1]) != null) {
								if (Bukkit.getPlayer(split[1]).getName()
										.equals(player.getName())) {
									player.sendMessage(ChatColor.RED + "자신에게 팜장을 넘기실수 없습니다!");
									return true;
								}
								Bukkit.getPlayer(split[1]).sendMessage(ChatColor.GREEN + "당신은 이제 해당 팜의 팜장입니다!");
							}
							player.sendMessage(ChatColor.GREEN + Bukkit.getPlayer(split[1]).getName()
									+ "§f님이 이제 해당 팜의 팜장입니다!");

							uSkyBlock.getInstance()
									.getIslandConfig(pi.locationForParty())
									.set("party.leader", tempTargetPlayer);
							String location = pi.locationForParty();
							uSkyBlock.getInstance().getIslandConfig(location).set(
									"party.currentSize",
									Integer.valueOf(uSkyBlock.getInstance().getIslandConfig(location).getInt(
											"party.currentSize") - 1));
							uSkyBlock.getInstance().setupPartyLeader(location, tempTargetPlayer);
							uSkyBlock.getInstance().setupPartyMember(pi.locationForParty(), player.getName());
							
							if ((Settings.island_protectWithWorldGuard)
									&& (Bukkit.getServer()
											.getPluginManager()
											.isPluginEnabled("WorldGuard"))) {
								WorldGuardHandler.transferRegion(
										player.getName(), tempTargetPlayer,
										sender);
							}
							return true;
						}
						player.sendMessage(ChatColor.RED
								+ "해당 플레이어는 팜원이 아닙니다.");
					} else {
						player.sendMessage(ChatColor.RED
								+ "이 팜은 당신의 소유가 아닙니다.");
					}
				} else
					player.sendMessage(ChatColor.RED + "당신의 팜원에게만 팜장을 넘기실수 있습니다");

				return true;
			}
			if (((split[0].equals("info")) || (split[0].equals("level")) || (split[0]
					.equals("레벨")))
					&& (pi.getIslandLocation() != null)
					&& (VaultHandler.checkPerk(player.getName(),
							"usb.island.info", player.getWorld()))
					&& (Settings.island_useIslandLevel)) {
				if ((!uSkyBlock.getInstance().onInfoCooldown(player))
						|| (Settings.general_cooldownInfo == 0)) {
					uSkyBlock.getInstance().setInfoCooldown(player);
					if ((!pi.getHasParty()) && (!pi.getHasIsland())) {
						player.sendMessage(ChatColor.RED + "당신은 섬이 없습니다.");
					} else
						getIslandLevel(player, split[1]);
					return true;
				}
				player.sendMessage(ChatColor.GREEN + "당신의 해당 명령어를 "
						+ uSkyBlock.getInstance().getInfoCooldownTime(player)
						/ 1000L + " 초뒤에 입력할수 있습니다.");
				return true;
			}
			if ((split[0].equals("warp")) || (split[0].equals("워프"))
					|| (split[0].equals("w"))) {
				if (VaultHandler.checkPerk(player.getName(), "usb.island.warp",
						player.getWorld())) {
					PlayerInfo wPi = null;
					if (!uSkyBlock.getInstance().getActivePlayers()
							.containsKey(Bukkit.getPlayer(split[1]))) {
						if (!uSkyBlock.getInstance().getActivePlayers()
								.containsKey(split[1])) {
							wPi = new PlayerInfo(split[1]);
							if (!wPi.getHasIsland()) {
								player.sendMessage(ChatColor.RED
										+ "그 플레이어는 섬이 없습니다!");
								return true;
							}
						} else {
							wPi = (PlayerInfo) uSkyBlock.getInstance()
									.getActivePlayers().get(split[1]);
						}
					} else {
						wPi = (PlayerInfo) uSkyBlock.getInstance()
								.getActivePlayers()
								.get(Bukkit.getPlayer(split[1]));
					}
					if (!wPi.getHasIsland())
						return true;
					if (uSkyBlock.getInstance()
							.getIslandConfig(wPi.locationForParty())
							.getBoolean("general.warpActive")) {
						if (!uSkyBlock.getInstance()
								.getIslandConfig(wPi.locationForParty())
								.contains("banned.list." + player.getName())) {
							uSkyBlock.getInstance().warpTeleport(player, wPi);
							return true;
						}
						player.sendMessage(ChatColor.RED
								+ "해당 섬에서 당신의 워프를 밴했습니다.");

						return true;
					}
					player.sendMessage(ChatColor.RED
							+ "해당 섬은 워프를 사용하고 있지 않습니다.");
					return true;
				}

				player.sendMessage(ChatColor.RED
						+ "당신은 다른 섬에 워프 할 수있는 권한이 없습니다!");

				return true;
			}
			if (((split[0].equals("ban")) || (split[0].equals("밴")))
					&& (pi.getIslandLocation() != null)) {
				if (VaultHandler.checkPerk(player.getName(), "usb.island.ban",
						player.getWorld())) {
					if (!uSkyBlock.getInstance()
							.getIslandConfig(pi.locationForParty())
							.contains("banned.list." + player.getName())) {
						this.banList = uSkyBlock.getInstance()
								.getIslandConfig(pi.locationForParty())
								.getStringList("banned.list");
						this.banList.add(split[1]);
						uSkyBlock.getInstance()
								.getIslandConfig(pi.locationForParty())
								.set("banned.list", this.banList);
						player.sendMessage(ChatColor.GREEN + "당신은 "
								+ ChatColor.RED + split[1] + ChatColor.GREEN
								+ "님을 섬에서 밴하였습니다.");
					} else {
						this.banList = uSkyBlock.getInstance()
								.getIslandConfig(pi.locationForParty())
								.getStringList("banned.list");
						this.banList.remove(split[1]);
						uSkyBlock.getInstance()
								.getIslandConfig(pi.locationForParty())
								.set("banned.list", this.banList);
						player.sendMessage(ChatColor.GREEN + "당신은 "
								+ ChatColor.AQUA + split[1] + ChatColor.GREEN
								+ "님을 섬에서 밴을 해제하였습니다.");
					}
				} else {
					player.sendMessage(ChatColor.RED
							+ "You do not have permission to ban players from this island!");
				}
				uSkyBlock.getInstance().getActivePlayers()
						.put(player.getName(), pi);
				return true;
			}
			if (((split[0].equals("바이옴")) || (split[0].equals("biome")) || (split[0]
					.equals("b"))) && (pi.getIslandLocation() != null)) {
				if (uSkyBlock
						.getInstance()
						.getIslandConfig(iName)
						.getBoolean(
								"party.members." + player.getName()
										+ ".canChangeBiome")) {
					if ((!uSkyBlock.getInstance().onBiomeCooldown(player))
							|| (Settings.general_biomeChange == 0)) {
						if (uSkyBlock.getInstance().playerIsOnIsland(player)) {
							if (uSkyBlock.getInstance().changePlayerBiome(
									player, split[1])) {
								player.sendMessage(ChatColor.AQUA
										+ "당신은 섬의 바이옴을 "
										+ split[1].toUpperCase() + "로 변경했습니다.");
								player.sendMessage(ChatColor.AQUA
										+ "당신은 변경된 내용을 보려면 다시 로그인해야 할 수도 있습니다.");
								uSkyBlock.getInstance()
										.sendMessageToIslandGroup(
												iName,
												player.getName()
														+ " 님이 섬의 바이옴을 "
														+ split[1]
																.toUpperCase()
														+ "으로 변경하였습니다.");
								uSkyBlock.getInstance()
										.setBiomeCooldown(player);
								return true;
							}
							player.sendMessage(ChatColor.AQUA
									+ "알수없는 바이옴입니다, 기본 바이옴으로 설정됩니다.");
							player.sendMessage(ChatColor.AQUA
									+ "당신은 변경된 내용을 보려면 다시 로그인해야 할 수도 있습니다.");
							return true;
						}
						player.sendMessage(ChatColor.GREEN
								+ "당신은 바이옴을 변경할 섬에서 이 명령어를 사용해야 합니다.");

						return true;
					}
					player.sendMessage(ChatColor.GREEN
							+ "다시 바이옴을 변경하실려면, "
							+ uSkyBlock.getInstance().getBiomeCooldownTime(
									player) / 1000L / 60L + "분을 기다리세요!");
					return true;
				}

				player.sendMessage(ChatColor.RED
						+ "이 섬의 바이옴을 변경할 수있는 권한이 없습니다!");

				return true;
			}
			if (((split[0].equalsIgnoreCase("invite")) || (split[0]
					.equalsIgnoreCase("초대")))
					&& (pi.getIslandLocation() != null)
					&& (VaultHandler.checkPerk(player.getName(),
							"usb.party.create", player.getWorld()))) {
				if (!uSkyBlock
						.getInstance()
						.getIslandConfig(iName)
						.getBoolean(
								"party.members." + player.getName()
										+ ".canInviteOthers")) {
					player.sendMessage(ChatColor.RED
							+ "당신은 이 섬에 다른 사람을 초대 할 수있는 권한이 없습니다!");
					return true;
				}
				if (Bukkit.getPlayer(split[1]) == null) {
					player.sendMessage(ChatColor.RED
							+ "플레이어가 오프라인 상태이거나 존재하지 않습니다.");
					return true;
				}
				if (!Bukkit.getPlayer(split[1]).isOnline()) {
					player.sendMessage(ChatColor.RED
							+ "플레이어가 오프라인 상태이거나 존재하지 않습니다.");
					return true;
				}
				if (!uSkyBlock.getInstance().hasIsland(player.getName())) {
					player.sendMessage(ChatColor.RED
							+ "당신은 사람들을 초대하기 위해 섬을 가지고 있어야합니다!");
					return true;
				}
				if (player.getName().equalsIgnoreCase(
						Bukkit.getPlayer(split[1]).getName())) {
					player.sendMessage(ChatColor.RED + "자기자신을 초대할수 없습니다!");
					return true;
				}
				if (uSkyBlock.getInstance().hasParty(player.getName())) {
					if (!uSkyBlock
							.getInstance()
							.getIslandConfig(pi.locationForParty())
							.getString("party.leader")
							.equalsIgnoreCase(
									Bukkit.getPlayer(split[1]).getName())) {
						if (!uSkyBlock.getInstance().hasParty(
								Bukkit.getPlayer(split[1]).getName())) {
							if (uSkyBlock.getInstance()
									.getIslandConfig(pi.locationForParty())
									.getInt("party.currentSize") < Settings.general_maxPartySize) {
								if (this.inviteList.containsValue(player
										.getName())) {
									this.inviteList.remove(getKeyByValue(
											this.inviteList, player.getName()));
									player.sendMessage(ChatColor.GREEN
											+ "이전 초대 제거!");
								}
								this.inviteList.put(Bukkit.getPlayer(split[1])
										.getName(), player.getName());
								player.sendMessage(ChatColor.AQUA
										+ "초대장을 보냈습니다, "
										+ Bukkit.getPlayer(split[1]).getName());

								Bukkit.getPlayer(split[1]).sendMessage(
										player.getName()
												+ " 님이 당신을 파티초대 하셨습니다!");
								Bukkit.getPlayer(split[1]).sendMessage(
										ChatColor.GOLD + "/섬 [수락/거절]"
												+ ChatColor.GREEN
												+ " 를 이용하여 거절 또는 수락.");
								Bukkit.getPlayer(split[1])
										.sendMessage(
												ChatColor.RED
														+ "경고 : 당신이 동의하면 당신은 당신의 현재 섬을 잃게됩니다!");
								/*uSkyBlock.getInstance()
										.sendMessageToIslandGroup(
												iName,
												player.getName()
														+ " 님이 초대한 "
														+ Bukkit.getPlayer(
																split[1])
																.getName()
														+ " 님이 파티에 들어오셨습니다.");*/
							} else {
								player.sendMessage(ChatColor.RED
										+ "섬이 꽉차, 더이상 초대할수 없습니다.");
							}
						} else
							player.sendMessage(ChatColor.RED
									+ "그 플레이어는 이미 파티가 있습니다.");
					} else
						player.sendMessage(ChatColor.RED + "이 플레이어는 리더입니다!");
				} else {
					if (!uSkyBlock.getInstance().hasParty(player.getName())) {
						if (!uSkyBlock.getInstance().hasParty(
								Bukkit.getPlayer(split[1]).getName())) {
							if (this.inviteList.containsValue(player.getName())) {
								this.inviteList.remove(getKeyByValue(
										this.inviteList, player.getName()));
								player.sendMessage(ChatColor.GREEN
										+ "이전 초대 제거.");
							}
							this.inviteList.put(Bukkit.getPlayer(split[1])
									.getName(), player.getName());

							player.sendMessage(ChatColor.AQUA + "초대장을 보냈습니다, "
									+ Bukkit.getPlayer(split[1]).getName());
							Bukkit.getPlayer(split[1]).sendMessage(
									player.getName() + " 님이 당신을 파티초대 하셨습니다!");
							Bukkit.getPlayer(split[1]).sendMessage(
									ChatColor.GOLD + "/섬 [수락/거절]"
											+ ChatColor.GREEN
											+ " 를 이용하여 거절 또는 수락.");
							Bukkit.getPlayer(split[1])
									.sendMessage(
											ChatColor.RED
													+ "경고 : 당신이 동의하면 당신은 당신의 현재 섬을 잃게됩니다!");
						} else {
							player.sendMessage(ChatColor.RED
									+ "그 플레이어는 이미 파티가 있습니다.");
						}
						return true;
					}

					player.sendMessage(ChatColor.RED
							+ "오직 섬의 주인만, 플레이어 초대가 가능합니다.");
					return true;
				}
				return true;
			}
			if (((split[0].equalsIgnoreCase("remove"))
					|| (split[0].equalsIgnoreCase("kick")) || (split[0]
						.equalsIgnoreCase("킥")))
					&& (pi.getIslandLocation() != null)
					&& (VaultHandler.checkPerk(player.getName(),
							"usb.party.kick", player.getWorld()))) {
				if (!uSkyBlock
						.getInstance()
						.getIslandConfig(iName)
						.getBoolean(
								"party.members." + player.getName()
										+ ".canKickOthers")) {
					player.sendMessage(ChatColor.RED
							+ "당신은이 섬에서 다른 사람을 킥 할 수있는 권한이 없습니다!");
					return true;
				}
				if ((Bukkit.getPlayer(split[1]) == null)
						&& (Bukkit.getOfflinePlayer(split[1]) == null)) {
					player.sendMessage(ChatColor.RED + "플레이어가 존재하지 않습니다.");
					return true;
				}
				if (Bukkit.getPlayer(split[1]) == null) {
					this.tempTargetPlayer = Bukkit.getOfflinePlayer(split[1])
							.getName();
				} else {
					this.tempTargetPlayer = Bukkit.getPlayer(split[1])
							.getName();
				}
				if (uSkyBlock.getInstance()
						.getIslandConfig(pi.locationForParty())
						.contains("party.members." + split[1])) {
					this.tempTargetPlayer = split[1];
				}
				if (uSkyBlock.getInstance().hasParty(player.getName())) {
					if (!uSkyBlock.getInstance()
							.getIslandConfig(pi.locationForParty())
							.getString("party.leader")
							.equalsIgnoreCase(this.tempTargetPlayer)) {
						if (uSkyBlock
								.getInstance()
								.getIslandConfig(pi.locationForParty())
								.contains(
										"party.members."
												+ this.tempTargetPlayer)) {
							if (player.getName().equalsIgnoreCase(
									this.tempTargetPlayer)) {
								player.sendMessage(ChatColor.RED
										+ "자신을 킥하지 마십시오!");
								return true;
							}
							if (Bukkit.getPlayer(split[1]) != null) {
								if (Bukkit
										.getPlayer(split[1])
										.getWorld()
										.getName()
										.equalsIgnoreCase(
												uSkyBlock.getSkyBlockWorld()
														.getName())) {
									Bukkit.getPlayer(split[1]).sendMessage(
											ChatColor.RED + player.getName()
													+ " 님을 섬에서 킥됬습니다.");
								}
								if (Settings.extras_sendToSpawn)
									Bukkit.getPlayer(split[1]).performCommand(
											"spawn");
								else {
									Bukkit.getPlayer(split[1]).teleport(
											uSkyBlock.getSkyBlockWorld()
													.getSpawnLocation());
								}

							}

							if (Bukkit.getPlayer(uSkyBlock.getInstance()
									.getIslandConfig(pi.locationForParty())
									.getString("party.leader")) != null)
								Bukkit.getPlayer(
										uSkyBlock
												.getInstance()
												.getIslandConfig(
														pi.locationForParty())
												.getString("party.leader"))
										.sendMessage(
												ChatColor.RED
														+ this.tempTargetPlayer
														+ " 님이 섬에서 킥됬습니다.");
							removePlayerFromParty(
									this.tempTargetPlayer,
									uSkyBlock
											.getInstance()
											.getIslandConfig(
													pi.locationForParty())
											.getString("party.leader"),
									pi.locationForParty());

							if ((Settings.island_protectWithWorldGuard)
									&& (Bukkit.getServer().getPluginManager()
											.isPluginEnabled("WorldGuard"))) {
								System.out
										.println("Removing from "
												+ uSkyBlock
														.getInstance()
														.getIslandConfig(
																((PlayerInfo) uSkyBlock
																		.getInstance()
																		.getActivePlayers()
																		.get(player
																				.getName()))
																		.locationForParty())
														.getString(
																"party.leader")
												+ "'s Island");
								WorldGuardHandler
										.removePlayerFromRegion(
												uSkyBlock
														.getInstance()
														.getIslandConfig(
																((PlayerInfo) uSkyBlock
																		.getInstance()
																		.getActivePlayers()
																		.get(player
																				.getName()))
																		.locationForParty())
														.getString(
																"party.leader"),
												this.tempTargetPlayer);
							}
						} else {
							System.out.print("Player " + player.getName()
									+ " failed to remove "
									+ this.tempTargetPlayer);
							player.sendMessage(ChatColor.RED
									+ "해당 플레이어는 섬의 파티원이 아닙니다.");
						}
					} else
						player.sendMessage(ChatColor.RED
								+ "당신은 섬에서 리더를 제거 할 수 없습니다!");
				} else
					player.sendMessage(ChatColor.RED + "이 섬은 솔로입니다.");
				return true;
			}

		}

		return true;
	}

	private void inviteDebug(Player player) {
		player.sendMessage(this.inviteList.toString());
	}

	private void invitePurge() {
		this.inviteList.clear();
		this.inviteList.put("NoInviter", "NoInvited");
	}

	public boolean addPlayertoParty(String playername, String partyleader) {
		if (!uSkyBlock.getInstance().getActivePlayers().containsKey(playername)) {
			System.out.print("Failed to add player to party! (" + playername
					+ ")");
			return false;
		}
		if (!uSkyBlock.getInstance().getActivePlayers()
				.containsKey(partyleader)) {
			System.out.print("Failed to add player to party! (" + playername
					+ ")");
			return false;
		}

		((PlayerInfo) uSkyBlock.getInstance().getActivePlayers()
				.get(playername)).setJoinParty(((PlayerInfo) uSkyBlock
				.getInstance().getActivePlayers().get(partyleader))
				.getIslandLocation());
		if (!playername.equalsIgnoreCase(partyleader)) {
			if (((PlayerInfo) uSkyBlock.getInstance().getActivePlayers()
					.get(partyleader)).getHomeLocation() != null) {
				((PlayerInfo) uSkyBlock.getInstance().getActivePlayers()
						.get(playername))
						.setHomeLocation(((PlayerInfo) uSkyBlock.getInstance()
								.getActivePlayers().get(partyleader))
								.getHomeLocation());
			} else {
				((PlayerInfo) uSkyBlock.getInstance().getActivePlayers()
						.get(playername))
						.setHomeLocation(((PlayerInfo) uSkyBlock.getInstance()
								.getActivePlayers().get(partyleader))
								.getIslandLocation());
			}

			uSkyBlock.getInstance().setupPartyMember(
					((PlayerInfo) uSkyBlock.getInstance().getActivePlayers()
							.get(partyleader)).locationForParty(), playername);
		}

		((PlayerInfo) uSkyBlock.getInstance().getActivePlayers()
				.get(playername)).savePlayerConfig(playername);
		uSkyBlock.getInstance().sendMessageToIslandGroup(
				((PlayerInfo) uSkyBlock.getInstance().getActivePlayers()
						.get(partyleader)).locationForParty(),
				playername + " 님이 파티에 합류했습니다.");

		return true;
	}

	public void removePlayerFromParty(String playername, String partyleader,
			String location) {
		if (uSkyBlock.getInstance().getActivePlayers().containsKey(playername)) {
			uSkyBlock.getInstance().getIslandConfig(location)
					.set("party.members." + playername, null);
			uSkyBlock
					.getInstance()
					.getIslandConfig(location)
					.set("party.currentSize",
							Integer.valueOf(uSkyBlock.getInstance()
									.getIslandConfig(location)
									.getInt("party.currentSize") - 1));
			uSkyBlock.getInstance().saveIslandConfig(location);
			uSkyBlock.getInstance().sendMessageToIslandGroup(location,
					playername + " 님이 그룹에서 제거되었습니다.");
			((PlayerInfo) uSkyBlock.getInstance().getActivePlayers()
					.get(playername)).setHomeLocation(null);
			((PlayerInfo) uSkyBlock.getInstance().getActivePlayers()
					.get(playername)).setLeaveParty();
			((PlayerInfo) uSkyBlock.getInstance().getActivePlayers()
					.get(playername)).savePlayerConfig(playername);
		} else {
			PlayerInfo pi = new PlayerInfo(playername);

			uSkyBlock.getInstance().getIslandConfig(location)
					.set("party.members." + playername, null);
			uSkyBlock
					.getInstance()
					.getIslandConfig(location)
					.set("party.currentSize",
							Integer.valueOf(uSkyBlock.getInstance()
									.getIslandConfig(location)
									.getInt("party.currentSize") - 1));
			uSkyBlock.getInstance().saveIslandConfig(location);
			uSkyBlock.getInstance().sendMessageToIslandGroup(location,
					playername + " 님이 그룹에서 제거되었습니다.");
			pi.setHomeLocation(null);
			pi.setLeaveParty();
			pi.savePlayerConfig(playername);
		}
	}

	public <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Map.Entry<T,E> entry : map.entrySet()) {
			if (value.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	public boolean getIslandLevel(Player player, String islandPlayer) {
		if (this.allowInfo) {
			this.allowInfo = false;
			final String playerx = player.getName();
			final String islandPlayerx = islandPlayer;

			if ((!uSkyBlock.getInstance().hasIsland(islandPlayer))
					&& (!uSkyBlock.getInstance().hasParty(islandPlayer))) {
				player.sendMessage(ChatColor.RED + "플레이어가 유효하지않거나, 섬이 없습니다.");
				this.allowInfo = true;
				return false;
			}
			uSkyBlock
					.getInstance()
					.getServer()
					.getScheduler()
					.runTaskAsynchronously(uSkyBlock.getInstance(),
							new Runnable() {
								public void run() {
									try {
										int[] values = new int[256];
										String player = playerx;
										String islandPlayer = islandPlayerx;

										Location l = ((PlayerInfo) uSkyBlock
												.getInstance()
												.getActivePlayers().get(player))
												.getIslandLocation();
										int blockcount = 0;
										if (player
												.equalsIgnoreCase(islandPlayer)) {
											int px = l.getBlockX();
											int py = l.getBlockY();
											int pz = l.getBlockZ();
											World w = l.getWorld();
											for (int x = Settings.island_protectionRange
													/ 2 * -1; x <= Settings.island_protectionRange / 2; x++) {
												for (int y = 0; y <= 255; y++) {
													for (int z = Settings.island_protectionRange
															/ 2 * -1; z <= Settings.island_protectionRange / 2; z++) {
														values[w.getBlockAt(
																px + x, py + y,
																pz + z)
																.getTypeId()] += 1;
													}
												}
											}
											for (int i = 1; i <= 255; i++) {
												if ((values[i] > Settings.limitList[i])
														&& (Settings.limitList[i] >= 0))
													values[i] = Settings.limitList[i];
												if (Settings.diminishingReturnsList[i] > 0)
													values[i] = (int) Math
															.round(uSkyBlock
																	.getInstance()
																	.dReturns(
																			values[i],
																			Settings.diminishingReturnsList[i]));
												values[i] *= Settings.blockList[i];
												blockcount += values[i];
											}
										}
										if (player
												.equalsIgnoreCase(islandPlayer)) {
											uSkyBlock
													.getInstance()
													.getIslandConfig(
															((PlayerInfo) uSkyBlock
																	.getInstance()
																	.getActivePlayers()
																	.get(player))
																	.locationForParty())
													.set("general.level",
															Integer.valueOf(blockcount
																	/ uSkyBlock
																			.getInstance()
																			.getLevelConfig()
																			.getInt("general.pointsPerLevel")));
											((PlayerInfo) uSkyBlock
													.getInstance()
													.getActivePlayers()
													.get(player))
													.savePlayerConfig(player);
										}
									} catch (Exception e) {
										System.out
												.print("Error while calculating Island Level: "
														+ e);
										IslandCommand.this.allowInfo = true;
									}

									uSkyBlock
											.getInstance()
											.getServer()
											.getScheduler()
											.scheduleSyncDelayedTask(
													uSkyBlock.getInstance(),
													new Runnable() {
														public void run() {
															IslandCommand.this.allowInfo = true;

															if (Bukkit
																	.getPlayer(playerx) == null)
																return;
															Bukkit.getPlayer(
																	playerx)
																	.sendMessage(
																			ChatColor.GREEN
																					+ islandPlayerx
																					+ "의 섬에대한 정보!");
															if (playerx
																	.equalsIgnoreCase(islandPlayerx)) {
																Bukkit.getPlayer(
																		playerx)
																		.sendMessage(
																				ChatColor.AQUA
																						+ "섬의 레벨 : "
																						+ uSkyBlock
																								.getInstance()
																								.getIslandConfig(
																										((PlayerInfo) uSkyBlock
																												.getInstance()
																												.getActivePlayers()
																												.get(playerx))
																												.locationForParty())
																								.getInt("general.level"));
																uSkyBlock
																		.getInstance()
																		.saveIslandConfig(
																				((PlayerInfo) uSkyBlock
																						.getInstance()
																						.getActivePlayers()
																						.get(playerx))
																						.locationForParty());
															} else {
																PlayerInfo pi = new PlayerInfo(
																		islandPlayerx);
																if (!pi.getHasIsland())
																	Bukkit.getPlayer(
																			playerx)
																			.sendMessage(
																					ChatColor.AQUA
																							+ "섬의 레벨 : "
																							+ ChatColor.GOLD
																							+ uSkyBlock
																									.getInstance()
																									.getIslandConfig(
																											pi.locationForParty())
																									.getInt("general.level"));
																else
																	Bukkit.getPlayer(
																			playerx)
																			.sendMessage(
																					ChatColor.RED
																							+ "플레이어를 찾을수 없습니다.");
															}
														}
													}, 0L);
								}
							});
		} else {
			player.sendMessage(ChatColor.RED
					+ "지금 그 명령을 사용할 수 없습니다! 몇 초 후에 다시 시도해보십시오.");
			return false;
		}
		return true;
	}

	public String getBanList(Player player) {
		return null;
	}
}