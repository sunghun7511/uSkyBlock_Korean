package us.talabrek.ultimateskyblock;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChallengesCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] split) {
		if (!(sender instanceof Player)) {
			return false;
		}

		Player player = sender.getServer().getPlayer(sender.getName());
		if (!Settings.challenges_allowChallenges) {
			return true;
		}
		if ((!VaultHandler.checkPerk(player.getName(), "usb.island.challenges",
				player.getWorld())) && (!player.isOp())) {
			player.sendMessage(ChatColor.BOLD + "" + ChatColor.DARK_RED
					+ "[ 경고  ] " + ChatColor.RESET + ChatColor.YELLOW
					+ "이 명령어에 접근 할 수 없습니다.");
			return true;
		}
		if (!player.getWorld().getName()
				.equalsIgnoreCase(Settings.general_worldName)) {
			player.sendMessage(ChatColor.BOLD + "" + ChatColor.DARK_RED
					+ "[ 경고  ] " + ChatColor.RESET + ChatColor.YELLOW
					+ "해당 기능은 섬 월드에서 사용가능합니다.");
			return true;
		}

		if (split.length == 0) {
			player.openInventory(uSkyBlock.getInstance().displayChallengeGUI(
					player));
		} else if (split.length == 1) {
			if ((split[0].equalsIgnoreCase("도움말"))
					|| (split[0].equalsIgnoreCase("complete"))
					|| (split[0].equalsIgnoreCase("완료"))) {
				sender.sendMessage(ChatColor.YELLOW
						+ "Use /도전과제 완료 <과제이름> 으로 과제를 검사받을수 있습니다.");
				sender.sendMessage(Settings.challenges_challengeColor.replace(
						'&', '§')
						+ "완료하지않은 "
						+ Settings.challenges_finishedColor.replace('&', '§')
						+ "완료한(반복불가) "
						+ Settings.challenges_repeatableColor.replace('&', '§')
						+ "완료한(반복) ");
			} else if (uSkyBlock.getInstance().isRankAvailable(
					player,
					uSkyBlock
							.getInstance()
							.getConfig()
							.getString(
									"options.challenges.challengeList."
											+ split[0].toLowerCase()
											+ ".rankLevel"))) {
				sender.sendMessage(ChatColor.YELLOW + "도전과제 이름 : "
						+ ChatColor.WHITE + split[0].toLowerCase());
				sender.sendMessage(ChatColor.YELLOW
						+ uSkyBlock
								.getInstance()
								.getConfig()
								.getString(
										new StringBuilder(
												"options.challenges.challengeList.")
												.append(split[0].toLowerCase())
												.append(".description")
												.toString()));
				if (uSkyBlock
						.getInstance()
						.getConfig()
						.getString(
								"options.challenges.challengeList."
										+ split[0].toLowerCase() + ".type")
						.equalsIgnoreCase("onPlayer")) {
					if (uSkyBlock
							.getInstance()
							.getConfig()
							.getBoolean(
									"options.challenges.challengeList."
											+ split[0].toLowerCase()
											+ ".takeItems")) {
						sender.sendMessage(ChatColor.RED
								+ "이 과제를 수행 할 때 필요한 모든 항목을 잃게됩니다!");
					}
				} else if (uSkyBlock
						.getInstance()
						.getConfig()
						.getString(
								"options.challenges.challengeList."
										+ split[0].toLowerCase() + ".type")
						.equalsIgnoreCase("onIsland")) {
					sender.sendMessage(ChatColor.RED + "필요한 모든 항목은 섬에 배치해야합니다!");
				}

				if (Settings.challenges_ranks.length > 1) {
					sender.sendMessage(ChatColor.YELLOW
							+ "Rank: "
							+ ChatColor.WHITE
							+ uSkyBlock
									.getInstance()
									.getConfig()
									.getString(
											new StringBuilder(
													"options.challenges.challengeList.")
													.append(split[0]
															.toLowerCase())
													.append(".rankLevel")
													.toString()));
				}
				if ((((PlayerInfo) uSkyBlock.getInstance().getActivePlayers()
						.get(player.getName())).checkChallenge(split[0]
						.toLowerCase()) > 0)
						&& ((!uSkyBlock
								.getInstance()
								.getConfig()
								.getString(
										"options.challenges.challengeList."
												+ split[0].toLowerCase()
												+ ".type")
								.equalsIgnoreCase("onPlayer")) || (!uSkyBlock
								.getInstance()
								.getConfig()
								.getBoolean(
										"options.challenges.challengeList."
												+ split[0].toLowerCase()
												+ ".repeatable")))) {
					sender.sendMessage(ChatColor.RED + "이 과제는 반복할수 없습니다.");
					return true;
				}
				if ((Settings.challenges_enableEconomyPlugin)
						&& (VaultHandler.econ != null)) {
					if (((PlayerInfo) uSkyBlock.getInstance()
							.getActivePlayers().get(player.getName()))
							.checkChallenge(split[0].toLowerCase()) > 0) {
						sender.sendMessage(ChatColor.YELLOW
								+ "반복 보상 : "
								+ ChatColor.WHITE
								+ uSkyBlock
										.getInstance()
										.getConfig()
										.getString(
												new StringBuilder(
														"options.challenges.challengeList.")
														.append(split[0]
																.toLowerCase())
														.append(".repeatRewardText")
														.toString())
										.replace('&', '§'));
						player.sendMessage(ChatColor.YELLOW
								+ "반복 경험치 보상 : "
								+ ChatColor.WHITE
								+ uSkyBlock
										.getInstance()
										.getConfig()
										.getInt(new StringBuilder(
												"options.challenges.challengeList.")
												.append(split[0].toLowerCase())
												.append(".repeatXpReward")
												.toString()));
						sender.sendMessage(ChatColor.YELLOW
								+ "반복 통화 보상 : "
								+ ChatColor.WHITE
								+ uSkyBlock
										.getInstance()
										.getConfig()
										.getInt(new StringBuilder(
												"options.challenges.challengeList.")
												.append(split[0].toLowerCase())
												.append(".repeatCurrencyReward")
												.toString()) + " "
								+ VaultHandler.econ.currencyNamePlural());
					} else {
						sender.sendMessage(ChatColor.YELLOW
								+ "보상 : "
								+ ChatColor.WHITE
								+ uSkyBlock
										.getInstance()
										.getConfig()
										.getString(
												new StringBuilder(
														"options.challenges.challengeList.")
														.append(split[0]
																.toLowerCase())
														.append(".rewardText")
														.toString())
										.replace('&', '§'));
						player.sendMessage(ChatColor.YELLOW
								+ "경험치 보상 : "
								+ ChatColor.WHITE
								+ uSkyBlock
										.getInstance()
										.getConfig()
										.getInt(new StringBuilder(
												"options.challenges.challengeList.")
												.append(split[0].toLowerCase())
												.append(".xpReward").toString()));
						sender.sendMessage(ChatColor.YELLOW
								+ "통화 보상 : "
								+ ChatColor.WHITE
								+ uSkyBlock
										.getInstance()
										.getConfig()
										.getInt(new StringBuilder(
												"options.challenges.challengeList.")
												.append(split[0].toLowerCase())
												.append(".currencyReward")
												.toString()) + " "
								+ VaultHandler.econ.currencyNamePlural());
					}

				} else if (((PlayerInfo) uSkyBlock.getInstance()
						.getActivePlayers().get(player.getName()))
						.checkChallenge(split[0].toLowerCase()) > 0) {
					sender.sendMessage(ChatColor.YELLOW
							+ "반복 보상 : "
							+ ChatColor.WHITE
							+ uSkyBlock
									.getInstance()
									.getConfig()
									.getString(
											new StringBuilder(
													"options.challenges.challengeList.")
													.append(split[0]
															.toLowerCase())
													.append(".repeatRewardText")
													.toString())
									.replace('&', '§'));
					player.sendMessage(ChatColor.YELLOW
							+ "반복 경험치 보상 : "
							+ ChatColor.WHITE
							+ uSkyBlock
									.getInstance()
									.getConfig()
									.getInt(new StringBuilder(
											"options.challenges.challengeList.")
											.append(split[0].toLowerCase())
											.append(".repeatXpReward")
											.toString()));
				} else {
					sender.sendMessage(ChatColor.YELLOW
							+ "보상 : "
							+ ChatColor.WHITE
							+ uSkyBlock
									.getInstance()
									.getConfig()
									.getString(
											new StringBuilder(
													"options.challenges.challengeList.")
													.append(split[0]
															.toLowerCase())
													.append(".rewardText")
													.toString())
									.replace('&', '§'));
					player.sendMessage(ChatColor.YELLOW
							+ "경험치 보상 : "
							+ ChatColor.WHITE
							+ uSkyBlock
									.getInstance()
									.getConfig()
									.getInt(new StringBuilder(
											"options.challenges.challengeList.")
											.append(split[0].toLowerCase())
											.append(".xpReward").toString()));
				}

				sender.sendMessage(ChatColor.YELLOW
						+ "이 문제를 해결하려면 특정 조건이 필요합니다, " + ChatColor.WHITE
						+ "/c c " + split[0].toLowerCase());
			} else {
				sender.sendMessage(ChatColor.RED + "잘못된 과제의 이름입니다!");
			}
		} else if ((split.length == 2)
				&& ((split[0].equalsIgnoreCase("complete")) || (split[0]
						.equalsIgnoreCase("c")))
				&& (uSkyBlock.getInstance().checkIfCanCompleteChallenge(player,
						split[1].toLowerCase()))) {
			uSkyBlock.getInstance().giveReward(player, split[1].toLowerCase());
		}

		return true;
	}
}