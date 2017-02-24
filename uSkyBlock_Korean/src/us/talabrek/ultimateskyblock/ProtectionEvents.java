package us.talabrek.ultimateskyblock;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;

public class ProtectionEvents implements Listener {
	private Player attacker = null;
	private Player breaker = null;

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onPlayerBlockBreak(BlockBreakEvent event) {
		if ((!event.getPlayer().getWorld().getName()
				.equalsIgnoreCase(Settings.general_worldName))
				|| (uSkyBlock.getInstance().locationIsOnIsland(
						event.getPlayer(), event.getBlock().getLocation()))
				|| (VaultHandler.checkPerk(event.getPlayer().getName(),
						"usb.mod.bypassprotection", event.getPlayer()
								.getWorld())) || (event.getPlayer().isOp()))
			return;
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onPlayerBlockPlace(BlockPlaceEvent event) {
		if ((!event.getPlayer().getWorld().getName()
				.equalsIgnoreCase(Settings.general_worldName))
				|| (uSkyBlock.getInstance().locationIsOnIsland(
						event.getPlayer(), event.getBlock().getLocation()))
				|| (VaultHandler.checkPerk(event.getPlayer().getName(),
						"usb.mod.bypassprotection", event.getPlayer()
								.getWorld())) || (event.getPlayer().isOp()))
			return;
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if ((!event.getPlayer().getWorld().getName()
				.equalsIgnoreCase(Settings.general_worldName))
				|| (uSkyBlock.getInstance().playerIsOnIsland(event.getPlayer()))
				|| (uSkyBlock.getInstance().playerIsInSpawn(event.getPlayer()))
				|| (VaultHandler.checkPerk(event.getPlayer().getName(),
						"usb.mod.bypassprotection", event.getPlayer()
								.getWorld())) || (event.getPlayer().isOp()))
			return;
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	public void onPlayerBedEnter(PlayerBedEnterEvent event) {
		if ((!event.getPlayer().getWorld().getName()
				.equalsIgnoreCase(Settings.general_worldName))
				|| (uSkyBlock.getInstance().playerIsOnIsland(event.getPlayer()))
				|| (VaultHandler.checkPerk(event.getPlayer().getName(),
						"usb.mod.bypassprotection", event.getPlayer()
								.getWorld())) || (event.getPlayer().isOp()))
			return;
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	public void onPlayerShearEntity(PlayerShearEntityEvent event) {
		if ((!event.getPlayer().getWorld().getName()
				.equalsIgnoreCase(Settings.general_worldName))
				|| (uSkyBlock.getInstance().playerIsOnIsland(event.getPlayer()))
				|| (VaultHandler.checkPerk(event.getPlayer().getName(),
						"usb.mod.bypassprotection", event.getPlayer()
								.getWorld())) || (event.getPlayer().isOp()))
			return;
		event.getPlayer().sendMessage(
				ChatColor.RED + "You can only do that on your island!");
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if ((!event.getPlayer().getWorld().getName()
				.equalsIgnoreCase(Settings.general_worldName))
				|| (uSkyBlock.getInstance().playerIsOnIsland(event.getPlayer()))
				|| (VaultHandler.checkPerk(event.getPlayer().getName(),
						"usb.mod.bypassprotection", event.getPlayer()
								.getWorld())) || (event.getPlayer().isOp()))
			return;
		event.getPlayer().sendMessage(
				ChatColor.RED + "You can only do that on your island!");
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		if ((!event.getPlayer().getWorld().getName()
				.equalsIgnoreCase(Settings.general_worldName))
				|| (uSkyBlock.getInstance().locationIsOnIsland(event
						.getPlayer(), event.getBlockClicked().getLocation()))
				|| (VaultHandler.checkPerk(event.getPlayer().getName(),
						"usb.mod.bypassprotection", event.getPlayer()
								.getWorld())) || (event.getPlayer().isOp()))
			return;
		event.getPlayer().sendMessage(
				ChatColor.RED + "You can only do that on your island!");
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		if ((!event.getPlayer().getWorld().getName()
				.equalsIgnoreCase(Settings.general_worldName))
				|| (uSkyBlock.getInstance().locationIsOnIsland(event
						.getPlayer(), event.getBlockClicked().getLocation()))
				|| (VaultHandler.checkPerk(event.getPlayer().getName(),
						"usb.mod.bypassprotection", event.getPlayer()
								.getWorld())) || (event.getPlayer().isOp()))
			return;
		event.getPlayer().sendMessage(
				ChatColor.RED + "You can only do that on your island!");
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	public void onPlayerBreakHanging(HangingBreakByEntityEvent event) {
		if (!(event.getRemover() instanceof Player))
			return;
		this.breaker = ((Player) event.getRemover());
		if ((!this.breaker.getWorld().getName()
				.equalsIgnoreCase(Settings.general_worldName))
				|| (uSkyBlock.getInstance().locationIsOnIsland(this.breaker,
						event.getEntity().getLocation()))
				|| (VaultHandler.checkPerk(this.breaker.getName(),
						"usb.mod.bypassprotection", this.breaker.getWorld()))
				|| (this.breaker.isOp()))
			return;
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	public void onPlayerVehicleDamage(VehicleDamageEvent event) {
		if (!(event.getAttacker() instanceof Player))
			return;
		this.breaker = ((Player) event.getAttacker());
		if ((!this.breaker.getWorld().getName()
				.equalsIgnoreCase(Settings.general_worldName))
				|| (uSkyBlock.getInstance().locationIsOnIsland(this.breaker,
						event.getVehicle().getLocation()))
				|| (VaultHandler.checkPerk(this.breaker.getName(),
						"usb.mod.bypassprotection", this.breaker.getWorld()))
				|| (this.breaker.isOp()))
			return;
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onPlayerAttack(EntityDamageByEntityEvent event) {
		if ((event.getDamager() instanceof Player)) {
			this.attacker = ((Player) event.getDamager());
			if ((!this.attacker.getWorld().getName()
					.equalsIgnoreCase(Settings.general_worldName))
					|| ((event.getEntity() instanceof Player))
					|| (uSkyBlock.getInstance().playerIsOnIsland(this.attacker))
					|| (VaultHandler.checkPerk(this.attacker.getName(),
							"usb.mod.bypassprotection",
							this.attacker.getWorld()))
					|| (this.attacker.isOp()))
				return;
			event.setCancelled(true);
		}
	}
}