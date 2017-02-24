package us.talabrek.ultimateskyblock;

import java.io.File;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class PlayerJoin implements Listener {
	private Player hungerman = null;
	private SkullMeta meta = null;

	int randomNum = 0;

	Player p = null;
	String[] playerPerm;

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event) {
		File f = new File(uSkyBlock.getInstance().directoryPlayers, event
				.getPlayer().getName());
		PlayerInfo pi = new PlayerInfo(event.getPlayer().getName());
		if (f.exists()) {
			PlayerInfo pi2 = uSkyBlock.getInstance().readPlayerFile(
					event.getPlayer().getName());
			if (pi2 != null) {
				pi.setIslandLocation(pi2.getIslandLocation());
				pi.setHomeLocation(pi2.getHomeLocation());
				pi.setHasIsland(pi2.getHasIsland());
				if (uSkyBlock.getInstance().getIslandConfig(
						pi.locationForParty()) == null) {
					uSkyBlock.getInstance().createIslandConfig(
							pi.locationForParty(), event.getPlayer().getName());
				}
				uSkyBlock.getInstance().clearIslandConfig(
						pi.locationForParty(), event.getPlayer().getName());
				if ((Settings.island_protectWithWorldGuard)
						&& (Bukkit.getServer().getPluginManager()
								.isPluginEnabled("WorldGuard")))
					WorldGuardHandler.protectIsland(event.getPlayer(), event
							.getPlayer().getName(), pi);
			}
			f.delete();
		}

		uSkyBlock.getInstance()
				.addActivePlayer(event.getPlayer().getName(), pi);
		if ((pi.getHasIsland())
				&& (!uSkyBlock.getInstance()
						.getTempIslandConfig(pi.locationForParty())
						.contains("general.level"))) {
			uSkyBlock.getInstance().createIslandConfig(pi.locationForParty(),
					event.getPlayer().getName());
			System.out.println("Creating new Config File");
		}

		uSkyBlock.getInstance().getIslandConfig(pi.locationForParty());
		System.out.print("Loaded player file for "
				+ event.getPlayer().getName());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent event) {
		if ((uSkyBlock.getInstance().hasIsland(event.getPlayer().getName()))
				&& (!uSkyBlock.getInstance().checkForOnlineMembers(
						event.getPlayer()))) {
			System.out.print("Removing island config from memory.");
			uSkyBlock.getInstance().removeIslandConfig(
					((PlayerInfo) uSkyBlock.getInstance().getActivePlayers()
							.get(event.getPlayer().getName()))
							.locationForParty());
		}

		uSkyBlock.getInstance().removeActivePlayer(event.getPlayer().getName());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerFoodChange(FoodLevelChangeEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		this.hungerman = ((Player) event.getEntity());
		if ((!this.hungerman.getWorld().getName()
				.equalsIgnoreCase(Settings.general_worldName))
				|| (this.hungerman.getFoodLevel() <= event.getFoodLevel())
				|| (!uSkyBlock.getInstance().playerIsOnIsland(this.hungerman)))
			return;
		if (VaultHandler.checkPerk(this.hungerman.getName(),
				"usb.extra.hunger4", this.hungerman.getWorld())) {
			event.setCancelled(true);
			return;
		}

		if (VaultHandler.checkPerk(this.hungerman.getName(),
				"usb.extra.hunger3", this.hungerman.getWorld())) {
			this.randomNum = (1 + (int) (Math.random() * 100.0D));
			if (this.randomNum <= 75) {
				event.setCancelled(true);
				return;
			}
		}
		if (VaultHandler.checkPerk(this.hungerman.getName(),
				"usb.extra.hunger2", this.hungerman.getWorld())) {
			this.randomNum = (1 + (int) (Math.random() * 100.0D));
			if (this.randomNum <= 50) {
				event.setCancelled(true);
				return;
			}
		}
		if (!VaultHandler.checkPerk(this.hungerman.getName(),
				"usb.extra.hunger", this.hungerman.getWorld()))
			return;
		this.randomNum = (1 + (int) (Math.random() * 100.0D));
		if (this.randomNum > 25)
			return;
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if ((!Settings.extras_obsidianToLava)
				|| (!uSkyBlock.getInstance()
						.playerIsOnIsland(event.getPlayer()))
				|| (!event.getPlayer().getWorld().getName()
						.equalsIgnoreCase(Settings.general_worldName))
				|| (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
				|| (event.getPlayer().getItemInHand().getTypeId() != 325)
				|| (event.getClickedBlock().getType() != Material.OBSIDIAN)
				|| (uSkyBlock.getInstance().testForObsidian(event
						.getClickedBlock())))
			return;
		event.getPlayer().sendMessage(
				ChatColor.GREEN + "주의!, 당신의 옵시디언이 용암으로 변합니다!");
		event.getClickedBlock().setType(Material.AIR);
		event.getPlayer().getInventory()
				.removeItem(new ItemStack[] { new ItemStack(325, 1) });
		event.getPlayer().getInventory()
				.addItem(new ItemStack[] { new ItemStack(327, 1) });
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	public void onEntityDamage(EntityDamageEvent event) {
		if ((!event.getEntity().getWorld().getName()
				.equalsIgnoreCase(Settings.general_worldName))
				|| (!event.getEntity().getType().equals(EntityType.ITEM_FRAME)))
			return;
		Iterator<Entity> closePlayers = event.getEntity()
				.getNearbyEntities(3.0D, 3.0D, 3.0D).iterator();
		while (closePlayers.hasNext()) {
			Entity temp = (Entity) closePlayers.next();
			if ((temp instanceof Player)) {
				Player p = (Player) temp;
				if (uSkyBlock.getInstance().locationIsOnIsland(p,
						event.getEntity().getLocation()))
					continue;
				event.setCancelled(true);
				return;
			}

			if ((temp instanceof Arrow)) {
				event.setCancelled(true);
				return;
			}

			if ((temp instanceof Snowball)) {
				event.setCancelled(true);
				return;
			}

			if ((temp instanceof SmallFireball)) {
				event.setCancelled(true);
				return;
			}

			if ((temp instanceof Creeper)) {
				event.setCancelled(true);
				return;
			}

			if (!(temp instanceof Fireball))
				continue;
			event.setCancelled(true);
			return;
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void guiClick(InventoryClickEvent event) {
		if (event.getInventory().getName().equalsIgnoreCase("§9섬 그룹 맴버")) {
			event.setCancelled(true);
			if ((event.getSlot() < 0) || (event.getSlot() > 35))
				return;
			if (event.getCurrentItem().getTypeId() == 397)
				this.meta = ((SkullMeta) event.getCurrentItem().getItemMeta());
			this.p = ((Player) event.getWhoClicked());
			if ((this.meta == null)
					|| (event.getCurrentItem().getType() == Material.SIGN)) {
				this.p.closeInventory();
				this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(
						this.p));
			} else if (this.meta.getLore().contains("§b§l리더")) {
				this.p.closeInventory();
				this.p.openInventory(uSkyBlock.getInstance().displayPartyGUI(
						this.p));
			} else if (!uSkyBlock.getInstance().isPartyLeader(this.p)) {
				this.p.closeInventory();
				this.p.openInventory(uSkyBlock.getInstance().displayPartyGUI(
						this.p));
			} else {
				this.p.closeInventory();
				this.p.openInventory(uSkyBlock.getInstance()
						.displayPartyPlayerGUI(this.p, this.meta.getOwner()));
				this.meta = null;
			}

		} else if (event.getInventory().getName().contains("권한")) {
			event.setCancelled(true);
			if ((event.getSlot() < 0) || (event.getSlot() > 35))
				return;
			this.p = ((Player) event.getWhoClicked());
			this.playerPerm = event.getInventory().getName().split(" ");
			if (event.getCurrentItem().getTypeId() == 6) {
				this.p.closeInventory();
				uSkyBlock.getInstance().changePlayerPermission(this.p,
						this.playerPerm[0], "canChangeBiome");
				this.p.openInventory(uSkyBlock.getInstance()
						.displayPartyPlayerGUI(this.p, this.playerPerm[0]));
			} else if (event.getCurrentItem().getTypeId() == 101) {
				this.p.closeInventory();
				uSkyBlock.getInstance().changePlayerPermission(this.p,
						this.playerPerm[0], "canToggleLock");
				this.p.openInventory(uSkyBlock.getInstance()
						.displayPartyPlayerGUI(this.p, this.playerPerm[0]));
			} else if (event.getCurrentItem().getTypeId() == 90) {
				this.p.closeInventory();
				uSkyBlock.getInstance().changePlayerPermission(this.p,
						this.playerPerm[0], "canChangeWarp");
				this.p.openInventory(uSkyBlock.getInstance()
						.displayPartyPlayerGUI(this.p, this.playerPerm[0]));
			} else if (event.getCurrentItem().getTypeId() == 69) {
				this.p.closeInventory();
				uSkyBlock.getInstance().changePlayerPermission(this.p,
						this.playerPerm[0], "canToggleWarp");
				this.p.openInventory(uSkyBlock.getInstance()
						.displayPartyPlayerGUI(this.p, this.playerPerm[0]));
			} else if (event.getCurrentItem().getTypeId() == 398) {
				this.p.closeInventory();
				uSkyBlock.getInstance().changePlayerPermission(this.p,
						this.playerPerm[0], "canInviteOthers");
				this.p.openInventory(uSkyBlock.getInstance()
						.displayPartyPlayerGUI(this.p, this.playerPerm[0]));
			} else if (event.getCurrentItem().getTypeId() == 301) {
				this.p.closeInventory();
				uSkyBlock.getInstance().changePlayerPermission(this.p,
						this.playerPerm[0], "canKickOthers");
				this.p.openInventory(uSkyBlock.getInstance()
						.displayPartyPlayerGUI(this.p, this.playerPerm[0]));
			} else if (event.getCurrentItem().getTypeId() == 323) {
				this.p.closeInventory();
				this.p.openInventory(uSkyBlock.getInstance().displayPartyGUI(
						this.p));
			} else {
				this.p.closeInventory();
				this.p.openInventory(uSkyBlock.getInstance()
						.displayPartyPlayerGUI(this.p, this.playerPerm[0]));
			}

		} else if (event.getInventory().getName().contains("섬 바이옴")) {
			event.setCancelled(true);
			if ((event.getSlot() < 0) || (event.getSlot() > 35))
				return;
			this.p = ((Player) event.getWhoClicked());

			if ((event.getCurrentItem().getType() == Material.SAPLING)
					&& (event.getCurrentItem().getDurability() == 3)) {
				this.p.closeInventory();
				this.p.performCommand("island biome jungle");
				this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(
						this.p));
			} else if ((event.getCurrentItem().getType() == Material.SAPLING)
					&& (event.getCurrentItem().getDurability() == 1)) {
				this.p.closeInventory();
				this.p.performCommand("island biome forest");
				this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(
						this.p));
			} else if (event.getCurrentItem().getType() == Material.SAND) {
				this.p.closeInventory();
				this.p.performCommand("island biome desert");
				this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(
						this.p));
			} else if (event.getCurrentItem().getType() == Material.SNOW) {
				this.p.closeInventory();
				this.p.performCommand("island biome taiga");
				this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(
						this.p));
			} else if (event.getCurrentItem().getType() == Material.EYE_OF_ENDER) {
				this.p.closeInventory();
				this.p.performCommand("island biome sky");
				this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(
						this.p));
			} else if (event.getCurrentItem().getType() == Material.WATER_LILY) {
				this.p.closeInventory();
				this.p.performCommand("island biome swampland");
				this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(
						this.p));
			} else if (event.getCurrentItem().getType() == Material.FIRE) {
				this.p.closeInventory();
				this.p.performCommand("island biome hell");
				this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(
						this.p));
			} else if (event.getCurrentItem().getType() == Material.RED_MUSHROOM) {
				this.p.closeInventory();
				this.p.performCommand("island biome mushroom");
				this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(
						this.p));
			} else if (event.getCurrentItem().getType() == Material.WATER) {
				this.p.closeInventory();
				this.p.performCommand("island biome ocean");
				this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(
						this.p));
			} else {
				this.p.closeInventory();
				this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(
						this.p));
			}
		} else if (event.getInventory().getName().contains("섬 기록")) {
			event.setCancelled(true);
			if ((event.getSlot() < 0) || (event.getSlot() > 35)) {
				return;
			}
			this.p.closeInventory();
			this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(
					this.p));
		} else {
			if (!event.getInventory().getName().contains("섬 메뉴"))
				return;
			event.setCancelled(true);
			if ((event.getSlot() < 0) || (event.getSlot() > 9))
				return;
			this.p = ((Player) event.getWhoClicked());

			if ((event.getCurrentItem().getType() == Material.SAPLING)
					&& (event.getCurrentItem().getDurability() == 3)) {
				this.p.closeInventory();
				this.p.performCommand("island biome");
			} else if (event.getCurrentItem().getType() == Material.SKULL_ITEM) {
				this.p.closeInventory();
				this.p.performCommand("island party");
			} else if (event.getCurrentItem().getType() == Material.BOOK) {
				this.p.closeInventory();
				this.p.chat("/island create");
			} else if (event.getCurrentItem().getType() == Material.BED) {
				this.p.closeInventory();
				this.p.performCommand("island sethome");
				this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(
						this.p));
			} else if (event.getCurrentItem().getType() == Material.HOPPER) {
				this.p.closeInventory();
				this.p.performCommand("island setwarp");
				this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(
						this.p));
			} else if (event.getCurrentItem().getType() == Material.BOOK_AND_QUILL) {
				this.p.closeInventory();
				this.p.performCommand("island log");
			} else if (event.getCurrentItem().getType() == Material.ENDER_PORTAL) {
				this.p.closeInventory();
				this.p.performCommand("island home");
			} else if (event.getCurrentItem().getType() == Material.GRASS) {
				this.p.closeInventory();
				this.p.performCommand("island create");
			} else if (event.getCurrentItem().getType() == Material.CHEST) {
				this.p.closeInventory();
				this.p.performCommand("chc open perks");
			} else if (event.getCurrentItem().getType() == Material.ENDER_CHEST) {
				this.p.closeInventory();
				if (VaultHandler.checkPerk(this.p.getName(), "group.donor",
						this.p.getWorld())) {
					this.p.performCommand("chc open donor");
				} else {
					this.p.performCommand("donate");
				}
			} else if (event.getCurrentItem().getType() == Material.EXP_BOTTLE) {
				this.p.closeInventory();
				this.p.performCommand("island level");
			} else if ((event.getCurrentItem().getType() == Material.ENDER_STONE)
					|| (event.getCurrentItem().getType() == Material.PORTAL)) {
				this.p.closeInventory();
				this.p.performCommand("island togglewarp");
				this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(
						this.p));
			} else {
				if (event.getCurrentItem().getType() == Material.IRON_FENCE) {
					if (uSkyBlock
							.getInstance()
							.getIslandConfig(
									((PlayerInfo) uSkyBlock
											.getInstance()
											.getActivePlayers()
											.get(event.getWhoClicked()
													.getName()))
											.locationForParty())
							.getBoolean("general.locked")) {
						this.p.closeInventory();
						this.p.performCommand("island unlock");
						this.p.openInventory(uSkyBlock.getInstance()
								.displayIslandGUI(this.p));
						return;
					}
				}
				if (event.getCurrentItem().getType() == Material.IRON_FENCE) {
					if (!uSkyBlock
							.getInstance()
							.getIslandConfig(
									((PlayerInfo) uSkyBlock
											.getInstance()
											.getActivePlayers()
											.get(event.getWhoClicked()
													.getName()))
											.locationForParty())
							.getBoolean("general.locked")) {
						this.p.closeInventory();
						this.p.performCommand("island lock");
						this.p.openInventory(uSkyBlock.getInstance()
								.displayIslandGUI(this.p));
						return;
					}
				}
				this.p.closeInventory();
				this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(
						this.p));
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryDrag(InventoryDragEvent event) {
		if (!event.getInventory().getName().equalsIgnoreCase("§9섬 그룹 맴버"))
			return;
		event.setCancelled(true);
		this.meta = ((SkullMeta) event.getCursor().getItemMeta());
		this.p = ((Player) event.getWhoClicked());
		if (this.meta.getOwner() == null) {
			this.p.updateInventory();
			this.p.closeInventory();
			this.p.openInventory(uSkyBlock.getInstance()
					.displayPartyGUI(this.p));
		} else {
			this.p.updateInventory();
			this.p.closeInventory();
			this.p.openInventory(uSkyBlock.getInstance().displayPartyPlayerGUI(
					this.p, this.meta.getOwner()));
		}
	}
}