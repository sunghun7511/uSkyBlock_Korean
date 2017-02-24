package us.talabrek.ultimateskyblock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Furnace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.data.DataException;

@SuppressWarnings({"deprecation", "unchecked"})
public class uSkyBlock extends JavaPlugin {
	public PluginDescriptionFile pluginFile;
	public Logger log;
	Date date;
	public DecimalFormat df = new DecimalFormat(".#");
	private FileConfiguration levelConfig = null;
	private FileConfiguration lastIslandConfig = null;
	private FileConfiguration orphans = null;
	private FileConfiguration tempIsland = null;
	private FileConfiguration tempPlayer = null;
	private HashMap<String, FileConfiguration> islands = new HashMap<String, FileConfiguration>();

	private File levelConfigFile = null;
	private File orphanFile = null;
	private File lastIslandConfigFile = null;
	private File islandConfigFile = null;
	private File tempIslandFile = null;
	private File tempPlayerFile = null;

	public static World skyBlockWorld = null;
	private static uSkyBlock instance;
	public List<String> removeList = new ArrayList<String>();
	List<String> rankDisplay;
	public FileConfiguration configPlugin;
	public File filePlugin;
	private Location lastIsland;
	private Stack<Location> orphaned = new Stack<Location>();
	private Stack<Location> tempOrphaned = new Stack<Location>();
	private Stack<Location> reverseOrphaned = new Stack<Location>();
	public File directoryPlayers;
	public File directoryIslands;
	private File directorySchematics;
	public File[] schemFile;
	public String pName;
	LinkedHashMap<String, Double> topTen;
	HashMap<String, Long> infoCooldown = new HashMap<String, Long>();
	HashMap<String, Long> restartCooldown = new HashMap<String, Long>();
	HashMap<String, Long> biomeCooldown = new HashMap<String, Long>();
	HashMap<String, PlayerInfo> activePlayers = new HashMap<String, PlayerInfo>();
	LinkedHashMap<String, List<String>> challenges = new LinkedHashMap<String, List<String>>();
	HashMap<Integer, Integer> requiredList = new HashMap<Integer, Integer>();
	public boolean purgeActive = false;
	private FileConfiguration skyblockData = null;
	private File skyblockDataFile = null;

	public Inventory GUIparty = null;
	public Inventory GUIpartyPlayer = null;
	public Inventory GUIisland = null;
	public Inventory GUIchallenge = null;
	public Inventory GUIbiome = null;
	public Inventory GUIlog = null;

	ItemStack pHead = new ItemStack(397, 1);

	ItemStack sign = new ItemStack(323, 1);

	ItemStack biome = new ItemStack(6, 1);

	ItemStack lock = new ItemStack(101, 1);

	ItemStack warpset = new ItemStack(90, 1);

	ItemStack warptoggle = new ItemStack(69, 1);

	ItemStack invite = new ItemStack(398, 1);

	ItemStack kick = new ItemStack(301, 1);
	ItemStack currentBiomeItem = null;
	ItemStack currentIslandItem = null;
	ItemStack currentChallengeItem = null;
	ItemStack currentLogItem = null;
	List<String> lores = new ArrayList<String>();
	Iterator<String> tempIt;
	private ArrayList<File> sfiles;

	public void onDisable() {
		try {
			unloadPlayerFiles();

			if (this.lastIsland != null) {
				setLastIsland(this.lastIsland);
			}

		} catch (Exception e) {
			System.out
					.println("Something went wrong saving the island and/or party data!");
			e.printStackTrace();
		}
		this.log.info(this.pluginFile.getName() + " v"
				+ this.pluginFile.getVersion() + " disabled.");
	}

	public void onEnable() {
		pHead.setDurability((short)3);
		biome.setDurability((short)3);
		instance = this;
		saveDefaultConfig();
		saveDefaultLevelConfig();
		saveDefaultOrphans();
		this.pluginFile = getDescription();
		this.log = getLogger();
		this.pName = (ChatColor.GOLD + "[" + ChatColor.AQUA
				+ this.pluginFile.getName() + ChatColor.GOLD + "] ");

		VaultHandler.setupEconomy();
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}
		this.configPlugin = getConfig();
		this.filePlugin = new File(getDataFolder(), "config.yml");
		loadPluginConfig();
		loadLevelConfig();
		registerEvents();
		this.directoryPlayers = new File(getDataFolder() + File.separator
				+ "players");
		this.directoryIslands = new File(getDataFolder() + File.separator
				+ "islands");
		if (!this.directoryPlayers.exists()) {
			this.directoryPlayers.mkdir();
			loadPlayerFiles();
		} else {
			loadPlayerFiles();
		}

		if (!this.directoryIslands.exists()) {
			this.directoryIslands.mkdir();
		}
		this.directorySchematics = new File(getDataFolder() + File.separator
				+ "schematics");
		if (!this.directorySchematics.exists()) {
			this.directorySchematics.mkdir();
		}
		this.schemFile = this.directorySchematics.listFiles();
		if (this.schemFile == null) {
			System.out.print("[uSkyBlock] No schematic file loaded.");
		} else {
			System.out.print("[uSkyBlock] " + this.schemFile.length
					+ " schematics loaded.");
		}
		getCommand("island").setExecutor(new IslandCommand());
		getCommand("섬").setExecutor(new IslandCommand());
		getCommand("도전과제").setExecutor(new ChallengesCommand());
		getCommand("challenges").setExecutor(new ChallengesCommand());
		getCommand("dev").setExecutor(new DevCommand());

		if (Settings.island_useTopTen)
			getInstance().updateTopTen(getInstance().generateTopTen());
		populateChallengeList();
		this.log.info(this.pluginFile.getName() + " v."
				+ this.pluginFile.getVersion() + " enabled.");
		getInstance().getServer().getScheduler()
				.runTaskLater(getInstance(), new Runnable() {
					public void run() {
						if (!Bukkit.getServer().getPluginManager()
								.isPluginEnabled("Vault"))
							return;
						System.out
								.print("[uSkyBlock] Using vault for permissions");
						VaultHandler.setupPermissions();
						try {
							if ((!uSkyBlock.this.getLastIslandConfig()
									.contains("options.general.lastIslandX"))
									&& (uSkyBlock.this.getConfig()
											.contains("options.general.lastIslandX"))) {
								uSkyBlock.this.getLastIslandConfig();
								FileConfiguration.createPath(
										uSkyBlock.this.getLastIslandConfig()
												.getConfigurationSection(
														"options.general"),
										"lastIslandX");
								uSkyBlock.this.getLastIslandConfig();
								FileConfiguration.createPath(
										uSkyBlock.this.getLastIslandConfig()
												.getConfigurationSection(
														"options.general"),
										"lastIslandZ");
								uSkyBlock.this
										.getLastIslandConfig()
										.set("options.general.lastIslandX",
												Integer.valueOf(uSkyBlock.this
														.getConfig()
														.getInt("options.general.lastIslandX")));
								uSkyBlock.this
										.getLastIslandConfig()
										.set("options.general.lastIslandZ",
												Integer.valueOf(uSkyBlock.this
														.getConfig()
														.getInt("options.general.lastIslandZ")));
								uSkyBlock.this.saveLastIslandConfig();
							}
							uSkyBlock.this.lastIsland = new Location(
									uSkyBlock.getSkyBlockWorld(),
									uSkyBlock.this
											.getLastIslandConfig()
											.getInt("options.general.lastIslandX"),
									Settings.island_height,
									uSkyBlock.this
											.getLastIslandConfig()
											.getInt("options.general.lastIslandZ"));
						} catch (Exception e) {
							uSkyBlock.this.lastIsland = new Location(uSkyBlock
									.getSkyBlockWorld(), uSkyBlock.this
									.getConfig().getInt(
											"options.general.lastIslandX"),
									Settings.island_height,
									uSkyBlock.this.getConfig().getInt(
											"options.general.lastIslandZ"));
						}
						if (uSkyBlock.this.lastIsland == null) {
							uSkyBlock.this.lastIsland = new Location(uSkyBlock
									.getSkyBlockWorld(), 0.0D,
									Settings.island_height, 0.0D);
						}

						if ((Settings.island_protectWithWorldGuard)
								&& (!Bukkit.getServer().getPluginManager()
										.isPluginEnabled("WorldGuard"))) {
							PluginManager manager = uSkyBlock.getInstance()
									.getServer().getPluginManager();
							System.out
									.print("[uSkyBlock] WorldGuard not loaded! Using built in protection.");
							manager.registerEvents(new ProtectionEvents(),
									uSkyBlock.getInstance());
						}
						uSkyBlock.getInstance().setupOrphans();
					}
				}, 0L);
	}

	public static uSkyBlock getInstance() {
		return instance;
	}

	public void loadPlayerFiles() {
		int onlinePlayerCount = 0;
		onlinePlayerCount = Bukkit.getServer().getOnlinePlayers().length;
		Player[] onlinePlayers = Bukkit.getServer().getOnlinePlayers();
		for (int i = 0; i < onlinePlayerCount; i++) {
			if (!onlinePlayers[i].isOnline())
				continue;
			File f = new File(getInstance().directoryPlayers,
					onlinePlayers[i].getName());
			PlayerInfo pi = new PlayerInfo(onlinePlayers[i].getName());
			if (f.exists()) {
				PlayerInfo pi2 = getInstance().readPlayerFile(
						onlinePlayers[i].getName());
				if (pi2 != null) {
					pi.setIslandLocation(pi2.getIslandLocation());
					pi.setHomeLocation(pi2.getHomeLocation());
					pi.setHasIsland(pi2.getHasIsland());
					if (getInstance().getIslandConfig(pi.locationForParty()) == null) {
						getInstance().createIslandConfig(pi.locationForParty(),
								onlinePlayers[i].getName());
					}
					getInstance().clearIslandConfig(pi.locationForParty(),
							onlinePlayers[i].getName());
					if ((Settings.island_protectWithWorldGuard)
							&& (Bukkit.getServer().getPluginManager()
									.isPluginEnabled("WorldGuard")))
						WorldGuardHandler.protectIsland(onlinePlayers[i],
								onlinePlayers[i].getName(), pi);
				}
				f.delete();
			}
			getInstance().addActivePlayer(onlinePlayers[i].getName(), pi);
			if ((pi.getHasIsland())
					&& (getInstance()
							.getTempIslandConfig(pi.locationForParty()) == null)) {
				getInstance().createIslandConfig(pi.locationForParty(),
						onlinePlayers[i].getName());
				System.out.println("Creating new Config File");
			}
			getInstance().getIslandConfig(pi.locationForParty());
		}

		System.out.print("Island Configs Loaded:");
		getInstance().displayIslandConfigs();
	}

	public void unloadPlayerFiles() {
		for (int i = 0; i < Bukkit.getServer().getOnlinePlayers().length; i++) {
			Player[] removedPlayers = Bukkit.getServer().getOnlinePlayers();
			if (getActivePlayers().containsKey(removedPlayers[i].getName()))
				removeActivePlayer(removedPlayers[i].getName());
		}
	}

	public void registerEvents() {
		PluginManager manager = getServer().getPluginManager();

		manager.registerEvents(new PlayerJoin(), this);
		if (!Settings.island_protectWithWorldGuard) {
			System.out.print("[uSkyBlock] Using built in protection.");
			manager.registerEvents(new ProtectionEvents(), getInstance());
		} else {
			System.out.print("[uSkyBlock] Using WorldGuard protection.");
		}
	}

	public void loadPluginConfig() {
		try {
			getConfig();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Settings.general_maxPartySize = getConfig().getInt(
					"options.general.maxPartySize");
			if (Settings.general_maxPartySize < 0)
				Settings.general_maxPartySize = 0;
		} catch (Exception e) {
			Settings.general_maxPartySize = 6;
		}
		try {
			Settings.island_distance = getConfig().getInt(
					"options.island.distance");
			if (Settings.island_distance < 50)
				Settings.island_distance = 50;
		} catch (Exception e) {
			Settings.island_distance = 110;
		}
		try {
			Settings.island_protectionRange = getConfig().getInt(
					"options.island.protectionRange");
			if (Settings.island_protectionRange > Settings.island_distance)
				Settings.island_protectionRange = Settings.island_distance;
		} catch (Exception e) {
			Settings.island_protectionRange = 100;
		}
		try {
			Settings.general_cooldownInfo = getConfig().getInt(
					"options.general.cooldownInfo");
			if (Settings.general_cooldownInfo < 0)
				Settings.general_cooldownInfo = 0;
		} catch (Exception e) {
			Settings.general_cooldownInfo = 60;
		}
		try {
			Settings.general_biomeChange = getConfig().getInt(
					"options.general.biomeChange");
			if (Settings.general_biomeChange < 0)
				Settings.general_biomeChange = 0;
		} catch (Exception e) {
			Settings.general_biomeChange = 3600;
		}
		try {
			Settings.general_cooldownRestart = getConfig().getInt(
					"options.general.cooldownRestart");
			if (Settings.general_cooldownRestart < 0)
				Settings.general_cooldownRestart = 0;
		} catch (Exception e) {
			Settings.general_cooldownRestart = 60;
		}
		try {
			Settings.island_height = getConfig()
					.getInt("options.island.height");
			if (Settings.island_height < 20)
				Settings.island_height = 20;
		} catch (Exception e) {
			Settings.island_height = 120;
		}
		try {
			Settings.challenges_rankLeeway = getConfig().getInt(
					"options.challenges.rankLeeway");
			if (Settings.challenges_rankLeeway < 0)
				Settings.challenges_rankLeeway = 0;
		} catch (Exception e) {
			Settings.challenges_rankLeeway = 0;
		}

		if (!getConfig().contains("options.extras.obsidianToLava")) {
			getConfig().set("options.extras.obsidianToLava",
					Boolean.valueOf(true));
			saveConfig();
		}
		if (!getConfig().contains("options.general.spawnSize")) {
			getConfig().set("options.general.spawnSize", Integer.valueOf(50));
			saveConfig();
		}
		try {
			Settings.general_spawnSize = getConfig().getInt(
					"options.general.spawnSize");
			if (Settings.general_spawnSize < 50)
				Settings.general_spawnSize = 50;
		} catch (Exception e) {
			Settings.general_spawnSize = 50;
		}

		String[] chestItemString = getConfig().getString(
				"options.island.chestItems").split(" ");
		ItemStack[] tempChest = new ItemStack[chestItemString.length];
		String[] amountdata = new String[2];
		for (int i = 0; i < tempChest.length; i++) {
			amountdata = chestItemString[i].split(":");
			tempChest[i] = new ItemStack(Integer.parseInt(amountdata[0]),
					Integer.parseInt(amountdata[1]));
		}
		Settings.island_chestItems = tempChest;
		Settings.island_allowPvP = getConfig().getString(
				"options.island.allowPvP");
		Settings.island_schematicName = getConfig().getString(
				"options.island.schematicName");
		if (!Settings.island_allowPvP.equalsIgnoreCase("allow"))
			Settings.island_allowPvP = "deny";
		Set<String> permissionList = getConfig().getConfigurationSection(
				"options.island.extraPermissions").getKeys(true);
		Settings.island_addExtraItems = getConfig().getBoolean(
				"options.island.addExtraItems");
		Settings.extras_obsidianToLava = getConfig().getBoolean(
				"options.extras.obsidianToLava");
		Settings.island_useIslandLevel = getConfig().getBoolean(
				"options.island.useIslandLevel");
		Settings.island_extraPermissions = (String[]) permissionList
				.toArray(new String[]{});
		Settings.island_protectWithWorldGuard = getConfig().getBoolean(
				"options.island.protectWithWorldGuard");
		Settings.extras_sendToSpawn = getConfig().getBoolean(
				"options.extras.sendToSpawn");
		Settings.island_useTopTen = getConfig().getBoolean(
				"options.island.useTopTen");

		Settings.general_worldName = getConfig().getString(
				"options.general.worldName");
		Settings.island_removeCreaturesByTeleport = getConfig().getBoolean(
				"options.island.removeCreaturesByTeleport");
		Settings.island_allowIslandLock = getConfig().getBoolean(
				"options.island.allowIslandLock");
		Settings.island_useOldIslands = getConfig().getBoolean(
				"options.island.useOldIslands");

		Set<String> challengeList = getConfig().getConfigurationSection(
				"options.challenges.challengeList").getKeys(false);
		Settings.challenges_challengeList = challengeList;
		Settings.challenges_broadcastCompletion = getConfig().getBoolean(
				"options.challenges.broadcastCompletion");
		Settings.challenges_broadcastText = getConfig().getString(
				"options.challenges.broadcastText");
		Settings.challenges_challengeColor = getConfig().getString(
				"options.challenges.challengeColor");
		Settings.challenges_enableEconomyPlugin = getConfig().getBoolean(
				"options.challenges.enableEconomyPlugin");
		Settings.challenges_finishedColor = getConfig().getString(
				"options.challenges.finishedColor");
		Settings.challenges_repeatableColor = getConfig().getString(
				"options.challenges.repeatableColor");
		Settings.challenges_requirePreviousRank = getConfig().getBoolean(
				"options.challenges.requirePreviousRank");
		Settings.challenges_allowChallenges = getConfig().getBoolean(
				"options.challenges.allowChallenges");
		String[] rankListString = getConfig().getString(
				"options.challenges.ranks").split(" ");
		Settings.challenges_ranks = rankListString;
	}

	public List<Party> readPartyFile() {
		File f = new File(getDataFolder(), "partylist.bin");
		if (!f.exists()) {
			return null;
		}
		try {
			FileInputStream fileIn = new FileInputStream(f);
			ObjectInputStream in = new ObjectInputStream(fileIn);

			List<Party> p = (List<Party>) in.readObject();
			in.close();
			fileIn.close();
			return p;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void writePartyFile(List<Party> pi) {
		File f = new File(getDataFolder(), "partylist.bin");
		try {
			FileOutputStream fileOut = new FileOutputStream(f);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(pi);
			out.flush();
			out.close();
			fileOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public PlayerInfo readPlayerFile(String playerName) {
		File f = new File(this.directoryPlayers, playerName);
		if (!f.exists()) {
			return null;
		}
		try {
			FileInputStream fileIn = new FileInputStream(f);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			PlayerInfo p = (PlayerInfo) in.readObject();
			in.close();
			fileIn.close();
			return p;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean displayTopTen(Player player) {
		int i = 1;
		int playerrank = 0;
		player.sendMessage(ChatColor.GREEN + "랭킹 10위 섬 목록 :  ");
		if (this.topTen == null) {
			player.sendMessage(ChatColor.RED + "아직 랭킹이 발표되지 않았습니다.");
			return false;
		}

		for (String playerName : this.topTen.keySet()) {
			if (i <= 10) {
				player.sendMessage(ChatColor.AQUA + "#" + i + ": "
						+ playerName + " - 섬 레벨 "
						+ ((Double) this.topTen.get(playerName)).intValue());
			}
			if (playerName.equalsIgnoreCase(player.getName())) {
				playerrank = i;
			}
			i++;
		}
		player.sendMessage(ChatColor.GREEN + "당신의 랭킹은 : " + ChatColor.GOLD
				+ playerrank);
		return true;
	}

	public void updateTopTen(LinkedHashMap<String, Double> map) {
		this.topTen = map;
	}

	public Location getLocationString(String s) {
		if ((s == null) || (s.trim() == "")) {
			return null;
		}
		String[] parts = s.split(":");
		if (parts.length == 4) {
			World w = getServer().getWorld(parts[0]);
			int x = Integer.parseInt(parts[1]);
			int y = Integer.parseInt(parts[2]);
			int z = Integer.parseInt(parts[3]);
			return new Location(w, x, y, z);
		}
		return null;
	}

	public String getStringLocation(Location l) {
		if (l == null) {
			return "";
		}
		return l.getWorld().getName() + ":" + l.getBlockX() + ":"
				+ l.getBlockY() + ":" + l.getBlockZ();
	}

	public void setStringbyPath(FileConfiguration fc, File f, String path,
			Object value) {
		fc.set(path, value.toString());
		try {
			fc.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getStringbyPath(FileConfiguration fc, File file, String path,
			Object stdValue, boolean addMissing) {
		if (!fc.contains(path)) {
			if (addMissing) {
				setStringbyPath(fc, file, path, stdValue);
			}
			return stdValue.toString();
		}
		return fc.getString(path);
	}

	public static World getSkyBlockWorld() {
		if (skyBlockWorld == null) {
			skyBlockWorld = WorldCreator.name(Settings.general_worldName)
					.type(WorldType.FLAT).environment(World.Environment.NORMAL)
					.generator(new SkyBlockChunkGenerator()).createWorld();
			if (Bukkit.getServer().getPluginManager()
					.isPluginEnabled("Multiverse-Core")) {
				Bukkit.getServer().dispatchCommand(
						Bukkit.getConsoleSender(),
						"mv import " + Settings.general_worldName
								+ " normal -g uSkyBlock");
			}
		}

		return skyBlockWorld;
	}

	public void clearOrphanedIsland() {
		while (hasOrphanedIsland())
			this.orphaned.pop();
	}

	public void clearArmorContents(Player player) {
	}

	public void getAllFiles(String path) {
		File dirpath = new File(path);

		if (!dirpath.exists()) {
			return;
		}

		for (File f : dirpath.listFiles()) {
			try {
				if (!f.isDirectory()) {
					this.sfiles.add(f);
				} else
					getAllFiles(f.getAbsolutePath());
			} catch (Exception ex) {
				this.log.warning(ex.getMessage());
			}
		}
	}

	public Location getYLocation(Location l) {
		for (int y = 0; y < 254; y++) {
			int px = l.getBlockX();
			int py = y;
			int pz = l.getBlockZ();
			Block b1 = new Location(l.getWorld(), px, py, pz).getBlock();
			Block b2 = new Location(l.getWorld(), px, py + 1, pz).getBlock();
			Block b3 = new Location(l.getWorld(), px, py + 2, pz).getBlock();
			if ((!b1.getType().equals(Material.AIR))
					&& (b2.getType().equals(Material.AIR))
					&& (b3.getType().equals(Material.AIR))) {
				return b2.getLocation();
			}
		}
		return l;
	}

	public Location getSafeHomeLocation(PlayerInfo p) {
		Location home = p.getHomeLocation();

		if (home == null) {
			if (p.getIslandLocation() != null) {
				home = p.getIslandLocation();
			}

		}

		if (isSafeLocation(home)) {
			return home;
		}

		for (int y = home.getBlockY() + 25; y > 0; y--) {
			Location n = new Location(home.getWorld(), home.getBlockX(), y,
					home.getBlockZ());
			if (isSafeLocation(n)) {
				return n;
			}
		}
		for (int y = home.getBlockY(); y < 255; y++) {
			Location n = new Location(home.getWorld(), home.getBlockX(), y,
					home.getBlockZ());
			if (isSafeLocation(n)) {
				return n;
			}
		}

		Location island = p.getIslandLocation();
		if (isSafeLocation(island)) {
			return island;
		}

		for (int y = island.getBlockY() + 25; y > 0; y--) {
			Location n = new Location(island.getWorld(), island.getBlockX(), y,
					island.getBlockZ());
			if (isSafeLocation(n)) {
				return n;
			}
		}
		for (int y = island.getBlockY(); y < 255; y++) {
			Location n = new Location(island.getWorld(), island.getBlockX(), y,
					island.getBlockZ());
			if (isSafeLocation(n)) {
				return n;
			}
		}
		return p.getHomeLocation();
	}

	public Location getSafeWarpLocation(PlayerInfo p) {
		Location warp = null;
		getTempIslandConfig(p.locationForParty());
		if (this.tempIsland.getInt("general.warpLocationX") == 0) {
			if (p.getHomeLocation() == null) {
				if (p.getIslandLocation() != null)
					warp = p.getIslandLocation();
			} else {
				warp = p.getHomeLocation();
			}
		} else {
			warp = new Location(skyBlockWorld,
					this.tempIsland.getInt("general.warpLocationX"),
					this.tempIsland.getInt("general.warpLocationY"),
					this.tempIsland.getInt("general.warpLocationZ"));
		}

		if (warp == null) {
			System.out.print("Error warping player to " + p.getPlayerName()
					+ "'s island.");
			return null;
		}

		if (isSafeLocation(warp)) {
			return warp;
		}

		for (int y = warp.getBlockY() + 25; y > 0; y--) {
			Location n = new Location(warp.getWorld(), warp.getBlockX(), y,
					warp.getBlockZ());
			if (isSafeLocation(n)) {
				return n;
			}
		}
		for (int y = warp.getBlockY(); y < 255; y++) {
			Location n = new Location(warp.getWorld(), warp.getBlockX(), y,
					warp.getBlockZ());
			if (isSafeLocation(n)) {
				return n;
			}
		}
		return null;
	}

	public boolean isSafeLocation(Location l) {
		if (l == null) {
			return false;
		}

		Block ground = l.getBlock().getRelative(BlockFace.DOWN);
		Block air1 = l.getBlock();
		Block air2 = l.getBlock().getRelative(BlockFace.UP);
		if (ground.getType().equals(Material.AIR))
			return false;
		if (ground.getType().equals(Material.LAVA))
			return false;
		if (ground.getType().equals(Material.STATIONARY_LAVA))
			return false;
		if (ground.getType().equals(Material.CACTUS)) {
			return false;
		}
		return ((air1.getType().equals(Material.AIR))
				|| (air1.getType().equals(Material.CROPS))
				|| (air1.getType().equals(Material.LONG_GRASS))
				|| (air1.getType().equals(Material.RED_ROSE))
				|| (air1.getType().equals(Material.YELLOW_FLOWER))
				|| (air1.getType().equals(Material.DEAD_BUSH))
				|| (air1.getType().equals(Material.SIGN_POST)) || (air1
					.getType().equals(Material.SIGN)))
				&& (air2.getType().equals(Material.AIR));
	}

	public void removeCreatures(Location l) {
		if ((!Settings.island_removeCreaturesByTeleport) || (l == null)) {
			return;
		}

		int px = l.getBlockX();
		int py = l.getBlockY();
		int pz = l.getBlockZ();
		for (int x = -1; x <= 1; x++)
			for (int z = -1; z <= 1; z++) {
				Chunk c = l.getWorld()
						.getChunkAt(
								new Location(l.getWorld(), px + x * 16, py, pz
										+ z * 16));
				for (Entity e : c.getEntities()) {
					if ((e.getType() != EntityType.SPIDER)
							&& (e.getType() != EntityType.CREEPER)
							&& (e.getType() != EntityType.ENDERMAN)
							&& (e.getType() != EntityType.SKELETON)
							&& (e.getType() != EntityType.ZOMBIE))
						continue;
					e.remove();
				}
			}
	}

	public void deletePlayerIsland(String player) {
		if (!getActivePlayers().containsKey(player)) {
			PlayerInfo pi = new PlayerInfo(player);
			if ((Settings.island_protectWithWorldGuard)
					&& (Bukkit.getServer().getPluginManager()
							.isPluginEnabled("WorldGuard"))
					&& (WorldGuardHandler.getWorldGuard().getRegionManager(
							getSkyBlockWorld()).hasRegion(player + "Island"))) {
				WorldGuardHandler.getWorldGuard()
						.getRegionManager(getSkyBlockWorld())
						.removeRegion(player + "Island");
			}
			this.orphaned.push(pi.getIslandLocation());
			removeIsland(pi.getIslandLocation());
			deleteIslandConfig(pi.locationForParty());
			pi.removeFromIsland();
			saveOrphans();
			pi.savePlayerConfig(player);
		} else {
			if ((Settings.island_protectWithWorldGuard)
					&& (Bukkit.getServer().getPluginManager()
							.isPluginEnabled("WorldGuard"))
					&& (WorldGuardHandler.getWorldGuard().getRegionManager(
							getSkyBlockWorld()).hasRegion(player + "Island"))) {
				WorldGuardHandler.getWorldGuard()
						.getRegionManager(getSkyBlockWorld())
						.removeRegion(player + "Island");
			}
			this.orphaned.push(((PlayerInfo) getActivePlayers().get(player))
					.getIslandLocation());
			removeIsland(((PlayerInfo) getActivePlayers().get(player))
					.getIslandLocation());
			deleteIslandConfig(((PlayerInfo) getActivePlayers().get(player))
					.locationForParty());
			PlayerInfo pi = new PlayerInfo(player);
			pi.removeFromIsland();

			addActivePlayer(player, pi);
			saveOrphans();
		}
	}

	public void restartPlayerIsland(Player player, Location next) {
		boolean hasIslandNow = false;
		if ((next.getBlockX() == 0) && (next.getBlockZ() == 0)) {
			return;
		}
		removeIsland(next);
		if ((getInstance().getSchemFile().length > 0)
				&& (Bukkit.getServer().getPluginManager()
						.isPluginEnabled("WorldEdit"))) {
			String cSchem = "";
			for (int i = 0; i < getInstance().getSchemFile().length; i++) {
				if (hasIslandNow)
					continue;
				if (getInstance().getSchemFile()[i].getName().lastIndexOf('.') > 0) {
					cSchem = getInstance().getSchemFile()[i].getName()
							.substring(
									0,
									getInstance().getSchemFile()[i].getName()
											.lastIndexOf('.'));
				} else {
					cSchem = getInstance().getSchemFile()[i].getName();
				}
				if (!VaultHandler.checkPerk(player.getName(), "usb.schematic."
						+ cSchem, getSkyBlockWorld()))
					continue;
				try {
					if (!WorldEditHandler.loadIslandSchematic(
							getSkyBlockWorld(),
							getInstance().getSchemFile()[i], next))
						continue;
					setChest(next, player);
					hasIslandNow = true;
				} catch (MaxChangedBlocksException e) {
					e.printStackTrace();
				} catch (DataException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

			if (!hasIslandNow) {
				for (int i = 0; i < getInstance().getSchemFile().length; i++) {
					if (getInstance().getSchemFile()[i].getName().lastIndexOf(
							'.') > 0) {
						cSchem = getInstance().getSchemFile()[i].getName()
								.substring(
										0,
										getInstance().getSchemFile()[i]
												.getName().lastIndexOf('.'));
					} else
						cSchem = getInstance().getSchemFile()[i].getName();
					if (!cSchem.equalsIgnoreCase(Settings.island_schematicName))
						continue;
					try {
						if (!WorldEditHandler.loadIslandSchematic(
								getSkyBlockWorld(), getInstance()
										.getSchemFile()[i], next))
							continue;
						setChest(next, player);
						hasIslandNow = true;
					} catch (MaxChangedBlocksException e) {
						e.printStackTrace();
					} catch (DataException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		if (!hasIslandNow) {
			if (!Settings.island_useOldIslands)
				generateIslandBlocks(next.getBlockX(), next.getBlockZ(),
						player, getSkyBlockWorld());
			else
				oldGenerateIslandBlocks(next.getBlockX(), next.getBlockZ(),
						player, getSkyBlockWorld());
		}
		next.setY(Settings.island_height);
		System.out.println(next.getBlockY());

		setNewPlayerIsland(player, next);
		getInstance().changePlayerBiome(player, "OCEAN");
		for (int x = Settings.island_protectionRange / 2 * -1 - 16; x <= Settings.island_protectionRange / 2 + 16; x += 16) {
			for (int z = Settings.island_protectionRange / 2 * -1 - 16; z <= Settings.island_protectionRange / 2 + 16; z += 16) {
				getSkyBlockWorld().refreshChunk((next.getBlockX() + x) / 16,
						(next.getBlockZ() + z) / 16);
			}
		}
		Iterator<Entity> ents = player.getNearbyEntities(
				Settings.island_protectionRange / 2, 250.0D,
				Settings.island_protectionRange / 2).iterator();

		while (ents.hasNext()) {
			Entity tempent = (Entity) ents.next();
			if ((tempent instanceof Player)) {
				continue;
			}

			tempent.remove();
		}
	}

	public void devDeletePlayerIsland(String player) {
		if (!getActivePlayers().containsKey(player)) {
			PlayerInfo pi = new PlayerInfo(player);
			if ((Settings.island_protectWithWorldGuard)
					&& (Bukkit.getServer().getPluginManager()
							.isPluginEnabled("WorldGuard"))
					&& (WorldGuardHandler.getWorldGuard().getRegionManager(
							getSkyBlockWorld()).hasRegion(player + "Island"))) {
				WorldGuardHandler.getWorldGuard()
						.getRegionManager(getSkyBlockWorld())
						.removeRegion(player + "Island");
			}

			pi = new PlayerInfo(player);
			pi.savePlayerConfig(player);
		} else {
			if ((Settings.island_protectWithWorldGuard)
					&& (Bukkit.getServer().getPluginManager()
							.isPluginEnabled("WorldGuard"))
					&& (WorldGuardHandler.getWorldGuard().getRegionManager(
							getSkyBlockWorld()).hasRegion(player + "Island"))) {
				WorldGuardHandler.getWorldGuard()
						.getRegionManager(getSkyBlockWorld())
						.removeRegion(player + "Island");
			}
			PlayerInfo pi = new PlayerInfo(player);
			removeActivePlayer(player);
			addActivePlayer(player, pi);
		}
	}

	public boolean devSetPlayerIsland(Player sender, Location l, String player) {
		if (!getActivePlayers().containsKey(player)) {
			PlayerInfo pi = new PlayerInfo(player);
			int px = l.getBlockX();
			int py = l.getBlockY();
			int pz = l.getBlockZ();
			for (int x = -10; x <= 10; x++) {
				for (int y = -10; y <= 10; y++) {
					for (int z = -10; z <= 10; z++) {
						Block b = new Location(l.getWorld(), px + x, py + y, pz
								+ z).getBlock();
						if (b.getTypeId() != 7)
							continue;
						pi.setHomeLocation(new Location(l.getWorld(), px + x,
								py + y + 3, pz + z));
						pi.setHasIsland(true);
						pi.setIslandLocation(b.getLocation());
						pi.savePlayerConfig(player);
						getInstance().createIslandConfig(pi.locationForParty(),
								player);
						getInstance().clearIslandConfig(pi.locationForParty(),
								player);
						if ((Settings.island_protectWithWorldGuard)
								&& (Bukkit.getServer().getPluginManager()
										.isPluginEnabled("WorldGuard")))
							WorldGuardHandler.protectIsland(sender, player, pi);
						getInstance().getIslandConfig(pi.locationForParty());
						return true;
					}
				}
			}
		} else {
			int px = l.getBlockX();
			int py = l.getBlockY();
			int pz = l.getBlockZ();
			for (int x = -10; x <= 10; x++) {
				for (int y = -10; y <= 10; y++) {
					for (int z = -10; z <= 10; z++) {
						Block b = new Location(l.getWorld(), px + x, py + y, pz
								+ z).getBlock();
						if (b.getTypeId() != 7)
							continue;
						((PlayerInfo) getActivePlayers().get(player))
								.setHomeLocation(new Location(l.getWorld(), px
										+ x, py + y + 3, pz + z));
						((PlayerInfo) getActivePlayers().get(player))
								.setHasIsland(true);
						((PlayerInfo) getActivePlayers().get(player))
								.setIslandLocation(b.getLocation());
						PlayerInfo pi = (PlayerInfo) getActivePlayers().get(
								player);
						removeActivePlayer(player);
						addActivePlayer(player, pi);
						if ((Settings.island_protectWithWorldGuard)
								&& (Bukkit.getServer().getPluginManager()
										.isPluginEnabled("WorldGuard")))
							WorldGuardHandler.protectIsland(sender, player, pi);
						return true;
					}
				}
			}
		}

		return false;
	}

	public int orphanCount() {
		return this.orphaned.size();
	}

	public void removeIsland(Location loc) {
		if (loc != null) {
			Location l = loc;
			int px = l.getBlockX();
			int py = l.getBlockY();
			int pz = l.getBlockZ();
			for (int x = Settings.island_protectionRange / 2 * -1; x <= Settings.island_protectionRange / 2; x++)
				for (int y = 0; y <= 255; y++)
					for (int z = Settings.island_protectionRange / 2 * -1; z <= Settings.island_protectionRange / 2; z++) {
						Block b = new Location(l.getWorld(), px + x, py + y, pz
								+ z).getBlock();
						if (b.getType().equals(Material.AIR))
							continue;
						if (b.getType().equals(Material.CHEST)) {
							Chest c = (Chest) b.getState();
							ItemStack[] items = new ItemStack[c.getInventory()
									.getContents().length];
							c.getInventory().setContents(items);
						} else if (b.getType().equals(Material.FURNACE)) {
							Furnace f = (Furnace) b.getState();
							ItemStack[] items = new ItemStack[f.getInventory()
									.getContents().length];
							f.getInventory().setContents(items);
						} else if (b.getType().equals(Material.DISPENSER)) {
							Dispenser d = (Dispenser) b.getState();
							ItemStack[] items = new ItemStack[d.getInventory()
									.getContents().length];
							d.getInventory().setContents(items);
						}
						b.setType(Material.AIR);
					}
		}
	}

	public void removeIslandBlocks(Location loc) {
		if (loc != null) {
			System.out.print("Removing blocks from an abandoned island.");
			Location l = loc;
			int px = l.getBlockX();
			int py = l.getBlockY();
			int pz = l.getBlockZ();
			for (int x = -20; x <= 20; x++)
				for (int y = -20; y <= 20; y++)
					for (int z = -20; z <= 20; z++) {
						Block b = new Location(l.getWorld(), px + x, py + y, pz
								+ z).getBlock();
						if (b.getType().equals(Material.AIR))
							continue;
						if (b.getType().equals(Material.CHEST)) {
							Chest c = (Chest) b.getState();
							ItemStack[] items = new ItemStack[c.getInventory()
									.getContents().length];
							c.getInventory().setContents(items);
						} else if (b.getType().equals(Material.FURNACE)) {
							Furnace f = (Furnace) b.getState();
							ItemStack[] items = new ItemStack[f.getInventory()
									.getContents().length];
							f.getInventory().setContents(items);
						} else if (b.getType().equals(Material.DISPENSER)) {
							Dispenser d = (Dispenser) b.getState();
							ItemStack[] items = new ItemStack[d.getInventory()
									.getContents().length];
							d.getInventory().setContents(items);
						}
						b.setType(Material.AIR);
					}
		}
	}

	public boolean hasParty(String playername) {
		if (getActivePlayers().containsKey(playername)) {
			return getIslandConfig(
					((PlayerInfo) getActivePlayers().get(playername))
							.locationForParty()).getInt("party.currentSize") > 1;
		}

		PlayerInfo pi = new PlayerInfo(playername);
		if (!pi.getHasIsland()) {
			return false;
		}
		return getTempIslandConfig(pi.locationForParty()).getInt(
				"party.currentSize") > 1;
	}

	public Location getLastIsland() {
		if (this.lastIsland.getWorld().getName()
				.equalsIgnoreCase(Settings.general_worldName)) {
			return this.lastIsland;
		}

		setLastIsland(new Location(getSkyBlockWorld(), 0.0D,
				Settings.island_height, 0.0D));
		return new Location(getSkyBlockWorld(), 0.0D, Settings.island_height,
				0.0D);
	}

	public void setLastIsland(Location island) {
		getLastIslandConfig().set("options.general.lastIslandX",
				Integer.valueOf(island.getBlockX()));
		getLastIslandConfig().set("options.general.lastIslandZ",
				Integer.valueOf(island.getBlockZ()));
		saveLastIslandConfig();
		this.lastIsland = island;
	}

	public boolean hasOrphanedIsland() {
		return !this.orphaned.empty();
	}

	public Location checkOrphan() {
		return (Location) this.orphaned.peek();
	}

	public Location getOrphanedIsland() {
		if (hasOrphanedIsland()) {
			return (Location) this.orphaned.pop();
		}

		return null;
	}

	public void addOrphan(Location island) {
		this.orphaned.push(island);
	}

	public void removeNextOrphan() {
		this.orphaned.pop();
	}

	public void saveOrphans() {
		String fullOrphan = "";
		this.tempOrphaned = ((Stack<Location>) this.orphaned.clone());

		while (!this.tempOrphaned.isEmpty()) {
			this.reverseOrphaned.push((Location) this.tempOrphaned.pop());
		}
		while (!this.reverseOrphaned.isEmpty()) {
			Location tempLoc = (Location) this.reverseOrphaned.pop();
			fullOrphan = fullOrphan + tempLoc.getBlockX() + ","
					+ tempLoc.getBlockZ() + ";";
		}

		getOrphans().set("orphans.list", fullOrphan);
		saveOrphansFile();
	}

	public void setupOrphans() {
		if (!getOrphans().contains("orphans.list"))
			return;
		String fullOrphan = getOrphans().getString("orphans.list");
		if (fullOrphan.isEmpty())
			return;
		String[] orphanArray = fullOrphan.split(";");

		this.orphaned = new Stack<Location>();
		for (int i = 0; i < orphanArray.length; i++) {
			String[] orphanXY = orphanArray[i].split(",");
			Location tempLoc = new Location(getSkyBlockWorld(),
					Integer.parseInt(orphanXY[0]), Settings.island_height,
					Integer.parseInt(orphanXY[1]));
			this.orphaned.push(tempLoc);
		}
	}

	public boolean homeTeleport(Player player) {
		Location homeSweetHome = null;
		if (getActivePlayers().containsKey(player.getName())) {
			homeSweetHome = getInstance().getSafeHomeLocation(
					(PlayerInfo) getActivePlayers().get(player.getName()));
		}

		if (homeSweetHome == null) {
			player.performCommand("spawn");
			player.sendMessage(ChatColor.RED + "스폰지역으로 돌아가십시오!");
			return true;
		}

		getInstance().removeCreatures(homeSweetHome);
		player.teleport(homeSweetHome);
		player.sendMessage(ChatColor.AQUA + "섬으로 텔레포트 되었습니다.");
		return true;
	}

	public boolean warpTeleport(Player player, PlayerInfo pi) {
		Location warpSweetWarp = null;
		if (pi == null) {
			player.sendMessage(ChatColor.RED + "존재하지 않는 플레이어입니다.");
			return true;
		}
		warpSweetWarp = getInstance().getSafeWarpLocation(pi);

		if (warpSweetWarp == null) {
			player.sendMessage(ChatColor.RED + "그 플레이어의 섬으로 워프 할 수 없습니다!");
			return true;
		}

		player.teleport(warpSweetWarp);
		player.sendMessage(ChatColor.AQUA + "당신을 " + pi.getPlayerName()
				+ "의 섬으로 텔레포트 하였습니다.");
		return true;
	}

	public boolean homeSet(Player player) {
		if (!player.getWorld().getName()
				.equalsIgnoreCase(getSkyBlockWorld().getName())) {
			player.sendMessage(ChatColor.RED + "당신은 홈을 설정하려는 섬에 있어야합니다.");
			return true;
		}
		if (playerIsOnIsland(player)) {
			if (getActivePlayers().containsKey(player.getName())) {
				((PlayerInfo) getActivePlayers().get(player.getName()))
						.setHomeLocation(player.getLocation());
			}

			player.sendMessage(ChatColor.AQUA + "당신의 섬의 홈 위치는 현재 위치로 설정되었습니다.");
			return true;
		}
		player.sendMessage(ChatColor.RED + "당신은 홈을 설정하려는 섬에 있어야합니다.");
		return true;
	}

	public boolean warpSet(Player player) {
		if (!player.getWorld().getName()
				.equalsIgnoreCase(getSkyBlockWorld().getName())) {
			player.sendMessage(ChatColor.RED + "당신은 워프를 설정하려는 섬에 있어야합니다.");
			return true;
		}
		if (playerIsOnIsland(player)) {
			if (getActivePlayers().containsKey(player.getName())) {
				setWarpLocation(
						((PlayerInfo) getActivePlayers().get(player.getName()))
								.locationForParty(),
						player.getLocation());
			}

			player.sendMessage(ChatColor.AQUA
					+ "당신의 섬의 워프 위치는 현재 위치로 설정되었습니다.");
			return true;
		}
		player.sendMessage(ChatColor.RED + "당신은 워프를 설정하려는 섬에 있어야합니다.");
		return true;
	}

	public boolean homeSet(String player, Location loc) {
		if (getActivePlayers().containsKey(player)) {
			((PlayerInfo) getActivePlayers().get(player)).setHomeLocation(loc);
		} else {
			PlayerInfo pi = new PlayerInfo(player);
			pi.setHomeLocation(loc);
			pi.savePlayerConfig(player);
		}

		return true;
	}

	public boolean playerIsOnIsland(Player player) {
		if (getActivePlayers().containsKey(player.getName())) {
			Location islandTestLocation = null;
			if (((PlayerInfo) getActivePlayers().get(player.getName()))
					.getHasIsland()) {
				islandTestLocation = ((PlayerInfo) getActivePlayers().get(
						player.getName())).getIslandLocation();
			}
			if (islandTestLocation == null)
				return false;
			if ((player.getLocation().getX() > islandTestLocation.getX()
					- Settings.island_protectionRange / 2)
					&& (player.getLocation().getX() < islandTestLocation
							.getX() + Settings.island_protectionRange / 2)
					&& (player.getLocation().getZ() > islandTestLocation
							.getZ() - Settings.island_protectionRange / 2)
					&& (player.getLocation().getZ() < islandTestLocation
							.getZ() + Settings.island_protectionRange / 2))
				return true;
		}
		return false;
	}

	public boolean locationIsOnIsland(Player player, Location loc) {
		if (getActivePlayers().containsKey(player.getName())) {
			Location islandTestLocation = null;
			if (((PlayerInfo) getActivePlayers().get(player.getName()))
					.getHasIsland()) {
				islandTestLocation = ((PlayerInfo) getActivePlayers().get(
						player.getName())).getIslandLocation();
			}
			if (islandTestLocation == null)
				return false;
			if ((loc.getX() > islandTestLocation.getX()
					- Settings.island_protectionRange / 2)
					&& (loc.getX() < islandTestLocation.getX()
							+ Settings.island_protectionRange / 2)
					&& (loc.getZ() > islandTestLocation.getZ()
							- Settings.island_protectionRange / 2)
					&& (loc.getZ() < islandTestLocation.getZ()
							+ Settings.island_protectionRange / 2))
				return true;
		}
		return false;
	}

	public boolean playerIsInSpawn(Player player) {
		return (player.getLocation().getX() > Settings.general_spawnSize * -1)
				&& (player.getLocation().getX() < Settings.general_spawnSize)
				&& (player.getLocation().getZ() > Settings.general_spawnSize
						* -1)
				&& (player.getLocation().getZ() < Settings.general_spawnSize);
	}

	public boolean hasIsland(String playername) {
		if (getActivePlayers().containsKey(playername)) {
			return ((PlayerInfo) getActivePlayers().get(playername))
					.getHasIsland();
		}

		PlayerInfo pi = new PlayerInfo(playername);
		return pi.getHasIsland();
	}

	public Location getPlayerIsland(String playername) {
		if (getActivePlayers().containsKey(playername)) {
			return ((PlayerInfo) getActivePlayers().get(playername))
					.getIslandLocation();
		}

		PlayerInfo pi = new PlayerInfo(playername);
		if (!pi.getHasIsland())
			return null;
		return pi.getIslandLocation();
	}

	public boolean islandAtLocation(Location loc) {
		if (loc == null) {
			return true;
		}
		int px = loc.getBlockX();
		int py = loc.getBlockY();
		int pz = loc.getBlockZ();
		for (int x = -2; x <= 2; x++) {
			for (int y = -50; y <= 50; y++) {
				for (int z = -2; z <= 2; z++) {
					Block b = new Location(loc.getWorld(), px + x, py + y, pz
							+ z).getBlock();
					if (b.getTypeId() != 0)
						return true;
				}
			}
		}
		return false;
	}

	public boolean islandInSpawn(Location loc) {
		if (loc == null) {
			return true;
		}

		return (loc.getX() > -50.0D) && (loc.getX() < 50.0D)
				&& (loc.getZ() > -50.0D) && (loc.getZ() < 50.0D);
	}

	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
		return new SkyBlockChunkGenerator();
	}

	public Stack<SerializableLocation> changeStackToFile(Stack<Location> stack) {
		Stack<SerializableLocation> finishStack = new Stack<SerializableLocation>();
		Stack<Location> tempStack = new Stack<Location>();
		while (!stack.isEmpty()) {
			tempStack.push((Location) stack.pop());
		}

		while (!tempStack.isEmpty()) {
			if (tempStack.peek() != null) {
				finishStack.push(new SerializableLocation((Location) tempStack
						.pop()));
			} else {
				tempStack.pop();
			}
		}

		return finishStack;
	}

	public Stack<Location> changestackfromfile(Stack<SerializableLocation> stack) {
		Stack<SerializableLocation> tempStack = new Stack<SerializableLocation>();
		Stack<Location> finishStack = new Stack<Location>();
		while (!stack.isEmpty())
			tempStack.push((SerializableLocation) stack.pop());
		while (!tempStack.isEmpty()) {
			if (tempStack.peek() != null)
				finishStack.push(((SerializableLocation) tempStack.pop())
						.getLocation());
			else
				tempStack.pop();
		}
		return finishStack;
	}

	public boolean largeIsland(Location l) {
		int blockcount = 0;
		int px = l.getBlockX();
		int py = l.getBlockY();
		int pz = l.getBlockZ();
		for (int x = -30; x <= 30; x++) {
			for (int y = -30; y <= 30; y++) {
				for (int z = -30; z <= 30; z++) {
					Block b = new Location(l.getWorld(), px + x, py + y, pz + z)
							.getBlock();
					if ((b.getTypeId() != 0) && (b.getTypeId() != 8)
							&& (b.getTypeId() != 10) && (blockcount > 200)) {
						return true;
					}
				}

			}

		}

		return blockcount > 200;
	}

	public boolean clearAbandoned() {
		int numOffline = 0;
		OfflinePlayer[] oplayers = Bukkit.getServer().getOfflinePlayers();
		System.out.print("Attemping to add more orphans");
		for (int i = 0; i < oplayers.length; i++) {
			long offlineTime = oplayers[i].getLastPlayed();
			offlineTime = (System.currentTimeMillis() - offlineTime) / 3600000L;
			if ((offlineTime <= 250L)
					|| (!getInstance().hasIsland(oplayers[i].getName()))
					|| (offlineTime >= 50000L))
				continue;
			PlayerInfo pi = new PlayerInfo(oplayers[i].getName());
			Location l = pi.getIslandLocation();
			int blockcount = 0;
			int px = l.getBlockX();
			int py = l.getBlockY();
			int pz = l.getBlockZ();
			for (int x = -30; x <= 30; x++) {
				for (int y = -30; y <= 30; y++) {
					for (int z = -30; z <= 30; z++) {
						Block b = new Location(l.getWorld(), px + x, py + y, pz
								+ z).getBlock();
						if ((b.getTypeId() == 0) || (b.getTypeId() == 8)
								|| (b.getTypeId() == 10))
							continue;
						blockcount++;
					}
				}
			}

			if (blockcount >= 200) {
				continue;
			}
			numOffline++;
			WorldGuardHandler.getWorldGuard()
					.getRegionManager(getSkyBlockWorld())
					.removeRegion(oplayers[i].getName() + "Island");
			this.orphaned.push(pi.getIslandLocation());

			pi.setHomeLocation(null);
			pi.setHasIsland(false);
			pi.setIslandLocation(null);
			pi.savePlayerConfig(pi.getPlayerName());
		}

		if (numOffline > 0) {
			System.out.print("Added " + numOffline + " new orphans.");
			saveOrphans();
			return true;
		}
		System.out.print("No new orphans to add!");
		return false;
	}

	public LinkedHashMap<String, Double> generateTopTen() {
		HashMap<String, Double> tempMap = new LinkedHashMap<String, Double>();
		File folder = this.directoryIslands;
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if ((getTempIslandConfig(listOfFiles[i].getName().replaceAll(
					".yml", "")) == null)
					|| (getTempIslandConfig(
							listOfFiles[i].getName().replaceAll(".yml", ""))
							.getInt("general.level") <= 0)) {
				continue;
			}
			tempMap.put(
					getTempIslandConfig(
							listOfFiles[i].getName().replaceAll(".yml", ""))
							.getString("party.leader"), Double
							.valueOf(getTempIslandConfig(
									listOfFiles[i].getName().replaceAll(".yml",
											"")).getInt("general.level")));
		}

		LinkedHashMap<String, Double> sortedMap = sortHashMapByValuesD(tempMap);
		return sortedMap;
	}

	public LinkedHashMap<String, Double> sortHashMapByValuesD(
			HashMap<String, Double> passedMap) {
		List<String> mapKeys = new ArrayList<String>(passedMap.keySet());
		
		List<Double> mapValues = new ArrayList<Double>(passedMap.values());
		Collections.sort(mapValues);
		Collections.reverse(mapValues);
		Collections.sort(mapKeys);
		Collections.reverse(mapKeys);

		LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<String, Double>();

		Iterator<Double> valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			Double val = (Double) valueIt.next();
			Iterator<String> keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				String key = (String) keyIt.next();
				String comp1 = ((Double) passedMap.get(key)).toString();
				String comp2 = val.toString();

				if (!comp1.equals(comp2))
					continue;
				passedMap.remove(key);
				mapKeys.remove(key);
				sortedMap.put(key, val);
				break;
			}

		}

		return sortedMap;
	}

	public boolean onInfoCooldown(Player player) {
		if (this.infoCooldown.containsKey(player.getName())) {
			return ((Long) this.infoCooldown.get(player.getName())).longValue() > Calendar
					.getInstance().getTimeInMillis();
		}

		return false;
	}

	public boolean onBiomeCooldown(Player player) {
		if (this.biomeCooldown.containsKey(player.getName())) {
			return ((Long) this.biomeCooldown.get(player.getName()))
					.longValue() > Calendar.getInstance().getTimeInMillis();
		}

		return false;
	}

	public boolean onRestartCooldown(Player player) {
		if (this.restartCooldown.containsKey(player.getName())) {
			return ((Long) this.restartCooldown.get(player.getName()))
					.longValue() > Calendar.getInstance().getTimeInMillis();
		}

		return false;
	}

	public long getInfoCooldownTime(Player player) {
		if (this.infoCooldown.containsKey(player.getName())) {
			if (((Long) this.infoCooldown.get(player.getName())).longValue() > Calendar
					.getInstance().getTimeInMillis()) {
				return ((Long) this.infoCooldown.get(player.getName()))
						.longValue() - Calendar.getInstance().getTimeInMillis();
			}

			return 0L;
		}

		return 0L;
	}

	public long getBiomeCooldownTime(Player player) {
		if (this.biomeCooldown.containsKey(player.getName())) {
			if (((Long) this.biomeCooldown.get(player.getName())).longValue() > Calendar
					.getInstance().getTimeInMillis()) {
				return ((Long) this.biomeCooldown.get(player.getName()))
						.longValue() - Calendar.getInstance().getTimeInMillis();
			}

			return 0L;
		}

		return 0L;
	}

	public long getRestartCooldownTime(Player player) {
		if (this.restartCooldown.containsKey(player.getName())) {
			if (((Long) this.restartCooldown.get(player.getName())).longValue() > Calendar
					.getInstance().getTimeInMillis()) {
				return ((Long) this.restartCooldown.get(player.getName()))
						.longValue() - Calendar.getInstance().getTimeInMillis();
			}

			return 0L;
		}

		return 0L;
	}

	public void setInfoCooldown(Player player) {
		this.infoCooldown.put(
				player.getName(),
				Long.valueOf(Calendar.getInstance().getTimeInMillis()
						+ Settings.general_cooldownInfo * 1000));
	}

	public void setBiomeCooldown(Player player) {
		this.biomeCooldown.put(
				player.getName(),
				Long.valueOf(Calendar.getInstance().getTimeInMillis()
						+ Settings.general_biomeChange * 1000));
	}

	public void setRestartCooldown(Player player) {
		this.restartCooldown.put(
				player.getName(),
				Long.valueOf(Calendar.getInstance().getTimeInMillis()
						+ Settings.general_cooldownRestart * 1000));
	}

	public File[] getSchemFile() {
		return this.schemFile;
	}

	public boolean testForObsidian(Block block) {
		for (int x = -3; x <= 3; x++)
			for (int y = -3; y <= 3; y++)
				for (int z = -3; z <= 3; z++) {
					Block testBlock = getSkyBlockWorld().getBlockAt(
							block.getX() + x, block.getY() + y,
							block.getZ() + z);
					if (((x != 0) || (y != 0) || (z != 0))
							&& (testBlock.getType() == Material.OBSIDIAN)) {
						return true;
					}
				}
		return false;
	}

	public void removeInactive(List<String> removePlayerList) {
		getInstance().getServer().getScheduler()
				.scheduleSyncRepeatingTask(getInstance(), new Runnable() {
					public void run() {
						if ((uSkyBlock.getInstance().getRemoveList().size() <= 0)
								|| (uSkyBlock.getInstance().isPurgeActive()))
							return;
						uSkyBlock.getInstance().deletePlayerIsland(
								(String) uSkyBlock.getInstance()
										.getRemoveList().get(0));
						System.out.print("[uSkyBlock] Purge: Removing "
								+ (String) uSkyBlock.getInstance()
										.getRemoveList().get(0) + "'s island");
						uSkyBlock.getInstance().deleteFromRemoveList();
					}
				}, 0L, 200L);
	}

	public List<String> getRemoveList() {
		return this.removeList;
	}

	public void addToRemoveList(String string) {
		this.removeList.add(string);
	}

	public void deleteFromRemoveList() {
		this.removeList.remove(0);
	}

	public boolean isPurgeActive() {
		return this.purgeActive;
	}

	public void activatePurge() {
		this.purgeActive = true;
	}

	public void deactivatePurge() {
		this.purgeActive = false;
	}

	public HashMap<String, PlayerInfo> getActivePlayers() {
		return this.activePlayers;
	}

	public void addActivePlayer(String player, PlayerInfo pi) {
		this.activePlayers.put(player, pi);
	}

	public void removeActivePlayer(String player) {
		if (!this.activePlayers.containsKey(player)) {
			return;
		}
		((PlayerInfo) this.activePlayers.get(player)).savePlayerConfig(player);

		this.activePlayers.remove(player);
		System.out.print("Removing player from memory: " + player);
	}

	public void populateChallengeList() {
		List<String> templist = new ArrayList<String>();
		for (int i = 0; i < Settings.challenges_ranks.length; i++) {
			this.challenges.put(Settings.challenges_ranks[i], templist);
			templist = new ArrayList<String>();
		}
		Iterator<String> itr = Settings.challenges_challengeList.iterator();
		while (itr.hasNext()) {
			String tempString = (String) itr.next();
			if (!this.challenges.containsKey(getConfig().getString(
					"options.challenges.challengeList." + tempString
							+ ".rankLevel")))
				continue;
			((List<String>) this.challenges.get(getConfig().getString(
					"options.challenges.challengeList." + tempString
							+ ".rankLevel"))).add(tempString);
		}
	}

	public String getChallengesFromRank(Player player, String rank) {
		this.rankDisplay = ((List<String>) this.challenges.get(rank));
		String fullString = "";
		PlayerInfo pi = (PlayerInfo) getActivePlayers().get(player.getName());
		Iterator<String> itr = this.rankDisplay.iterator();
		while (itr.hasNext()) {
			String tempString = (String) itr.next();
			if (pi.checkChallenge(tempString) > 0) {
				if (getConfig().getBoolean(
						"options.challenges.challengeList." + tempString
								+ ".repeatable")) {
					fullString = fullString
							+ Settings.challenges_repeatableColor.replace('&',
									'§') + tempString + ChatColor.DARK_GRAY
							+ " - ";
				} else {
					fullString = fullString
							+ Settings.challenges_finishedColor.replace('&',
									'§') + tempString + ChatColor.DARK_GRAY
							+ " - ";
				}
			} else {
				fullString = fullString
						+ Settings.challenges_challengeColor.replace('&', '§')
						+ tempString + ChatColor.DARK_GRAY + " - ";
			}
		}
		if (fullString.length() > 4)
			fullString = fullString.substring(0, fullString.length() - 3);
		return fullString;
	}

	public int checkRankCompletion(Player player, String rank) {
		if (!Settings.challenges_requirePreviousRank)
			return 0;
		this.rankDisplay = ((List<String>) this.challenges.get(rank));
		int ranksCompleted = 0;
		PlayerInfo pi = (PlayerInfo) getActivePlayers().get(player.getName());
		Iterator<String> itr = this.rankDisplay.iterator();
		while (itr.hasNext()) {
			String tempString = (String) itr.next();
			if (pi.checkChallenge(tempString) <= 0)
				continue;
			ranksCompleted++;
		}

		return this.rankDisplay.size() - Settings.challenges_rankLeeway
				- ranksCompleted;
	}

	public boolean isRankAvailable(Player player, String rank) {
		if (this.challenges.size() < 2) {
			return true;
		}

		for (int i = 0; i < Settings.challenges_ranks.length; i++) {
			if (!Settings.challenges_ranks[i].equalsIgnoreCase(rank))
				continue;
			if (i == 0) {
				return true;
			}

			if (checkRankCompletion(player, Settings.challenges_ranks[(i - 1)]) <= 0) {
				return true;
			}

		}

		return false;
	}

	public boolean checkIfCanCompleteChallenge(Player player, String challenge) {
		PlayerInfo pi = (PlayerInfo) getActivePlayers().get(player.getName());

		if (!isRankAvailable(
				player,
				getConfig().getString(
						"options.challenges.challengeList." + challenge
								+ ".rankLevel"))) {
			player.sendMessage(ChatColor.RED + "아직이 과제를 잠금을 해제하지 않았습니다!");
			return false;
		}
		if (!pi.challengeExists(challenge)) {
			player.sendMessage(ChatColor.RED + "알수없는 도전과제 이름입니다.");
			return false;
		}
		if ((pi.checkChallenge(challenge) > 0)
				&& (!getConfig().getBoolean(
						"options.challenges.challengeList." + challenge
								+ ".repeatable"))) {
			player.sendMessage(ChatColor.RED + "이 문제는 중복 완료가 불가능합니다.");
			return false;
		}

		if ((pi.checkChallenge(challenge) > 0)
				&& ((getConfig().getString(
						"options.challenges.challengeList." + challenge
								+ ".type").equalsIgnoreCase("onIsland")) || (getConfig()
						.getString(
								"options.challenges.challengeList." + challenge
										+ ".type").equalsIgnoreCase("onIsland")))) {
			player.sendMessage(ChatColor.RED + "이 문제는 중복 완료가 불가능합니다.");
			return false;
		}
		if (getConfig().getString(
				"options.challenges.challengeList." + challenge + ".type")
				.equalsIgnoreCase("onPlayer")) {
			if (!hasRequired(player, challenge, "onPlayer")) {
				player.sendMessage(ChatColor.RED
						+ getConfig().getString(
								new StringBuilder(
										"options.challenges.challengeList.")
										.append(challenge)
										.append(".description").toString()));
				player.sendMessage(ChatColor.RED
						+ "당신은 과제를 깨기위한 충분한 항목들이 없습니다.");
				return false;
			}
			return true;
		}
		if (getConfig().getString(
				"options.challenges.challengeList." + challenge + ".type")
				.equalsIgnoreCase("onIsland")) {
			if (!playerIsOnIsland(player)) {
				player.sendMessage(ChatColor.RED + "당신은 당신의 섬에서 해야합니다!");
			}
			if (!hasRequired(player, challenge, "onIsland")) {
				player.sendMessage(ChatColor.RED
						+ getConfig().getString(
								new StringBuilder(
										"options.challenges.challengeList.")
										.append(challenge)
										.append(".description").toString()));

				player.sendMessage(ChatColor.RED
						+ "당신은 필요한 모든 항목의 10 블록 내 에서 해야합니다");
				return false;
			}
			return true;
		}
		if (getConfig().getString(
				"options.challenges.challengeList." + challenge + ".type")
				.equalsIgnoreCase("islandLevel")) {
			if (getInstance().getIslandConfig(
					((PlayerInfo) getInstance().getActivePlayers().get(
							player.getName())).locationForParty()).getInt(
					"general.level") >= getConfig().getInt(
					"options.challenges.challengeList." + challenge
							+ ".requiredItems")) {
				return true;
			}

			player.sendMessage(ChatColor.RED
					+ "당신의 섬의 레벨이 "
					+ getConfig().getInt(
							new StringBuilder(
									"options.challenges.challengeList.")
									.append(challenge).append(".requiredItems")
									.toString()) + " 이 되어야 합니다!");
			return false;
		}

		return false;
	}

	public boolean takeRequired(Player player, String challenge, String type) {
		if (type.equalsIgnoreCase("onPlayer")) {
			String[] reqList = getConfig().getString(
					"options.challenges.challengeList." + challenge
							+ ".requiredItems").split(" ");

			int reqItem = 0;
			int reqAmount = 0;
			int reqMod = -1;
			String[] arrayOfString1;
			int j = (arrayOfString1 = reqList).length;
			int i = 0;
			do {
				String s = arrayOfString1[i];

				String[] sPart = s.split(":");
				if (sPart.length == 2) {
					reqItem = Integer.parseInt(sPart[0]);
					String[] sScale = sPart[1].split(";");
					if (sScale.length == 1) {
						reqAmount = Integer.parseInt(sPart[1]);
					} else if (sScale.length == 2) {
						if (sScale[1].charAt(0) == '+') {
							reqAmount = Integer.parseInt(sScale[0])
									+ Integer.parseInt(sScale[1].substring(1))
									* ((PlayerInfo) getInstance()
											.getActivePlayers().get(
													player.getName()))
											.checkChallengeSinceTimer(challenge);
						} else if (sScale[1].charAt(0) == '*') {
							reqAmount = Integer.parseInt(sScale[0])
									* Integer.parseInt(sScale[1].substring(1))
									* ((PlayerInfo) getInstance()
											.getActivePlayers().get(
													player.getName()))
											.checkChallengeSinceTimer(challenge);
						} else if (sScale[1].charAt(0) == '-') {
							reqAmount = Integer.parseInt(sScale[0])
									- Integer.parseInt(sScale[1].substring(1))
									* ((PlayerInfo) getInstance()
											.getActivePlayers().get(
													player.getName()))
											.checkChallengeSinceTimer(challenge);
						} else if (sScale[1].charAt(0) == '/') {
							reqAmount = Integer.parseInt(sScale[0])
									/ Integer.parseInt(sScale[1].substring(1))
									* ((PlayerInfo) getInstance()
											.getActivePlayers().get(
													player.getName()))
											.checkChallengeSinceTimer(challenge);
						}
					}
					if (!player.getInventory().contains(reqItem, reqAmount)) {
						return false;
					}

					player.getInventory()
							.removeItem(
									new ItemStack[] { new ItemStack(reqItem,
											reqAmount) });
				} else if (sPart.length == 3) {
					reqItem = Integer.parseInt(sPart[0]);
					String[] sScale = sPart[2].split(";");
					if (sScale.length == 1)
						reqAmount = Integer.parseInt(sPart[2]);
					else if (sScale.length == 2) {
						if (sScale[1].charAt(0) == '+') {
							reqAmount = Integer.parseInt(sScale[0])
									+ Integer.parseInt(sScale[1].substring(1))
									* ((PlayerInfo) getInstance()
											.getActivePlayers().get(
													player.getName()))
											.checkChallengeSinceTimer(challenge);
						} else if (sScale[1].charAt(0) == '*') {
							reqAmount = Integer.parseInt(sScale[0])
									* Integer.parseInt(sScale[1].substring(1))
									* ((PlayerInfo) getInstance()
											.getActivePlayers().get(
													player.getName()))
											.checkChallengeSinceTimer(challenge);
						} else if (sScale[1].charAt(0) == '-') {
							reqAmount = Integer.parseInt(sScale[0])
									- Integer.parseInt(sScale[1].substring(1))
									* ((PlayerInfo) getInstance()
											.getActivePlayers().get(
													player.getName()))
											.checkChallengeSinceTimer(challenge);
						} else if (sScale[1].charAt(0) == '/') {
							reqAmount = Integer.parseInt(sScale[0])
									/ Integer.parseInt(sScale[1].substring(1))
									* ((PlayerInfo) getInstance()
											.getActivePlayers().get(
													player.getName()))
											.checkChallengeSinceTimer(challenge);
						}
					}
					reqMod = Integer.parseInt(sPart[1]);
					if (!player.getInventory().containsAtLeast(
							new ItemStack(reqItem, reqAmount, (short) reqMod),
							reqAmount)) {
						return false;
					}
					player.getInventory().removeItem(
							new ItemStack[] { new ItemStack(reqItem, reqAmount,
									(short) reqMod) });
				}
				i++;
			} while (i < j);

			return true;
		}

		if (type.equalsIgnoreCase("onIsland")) {
			return true;
		}

		return type.equalsIgnoreCase("islandLevel");
	}

	public boolean hasRequired(Player player, String challenge, String type) {
		String[] reqList = getConfig().getString(
				"options.challenges.challengeList." + challenge
						+ ".requiredItems").split(" ");

		if (type.equalsIgnoreCase("onPlayer")) {
			int reqItem = 0;
			int reqAmount = 0;
			int reqMod = -1;
			for (String s : reqList) {
				String[] sPart = s.split(":");
				if (sPart.length == 2) {
					reqItem = Integer.parseInt(sPart[0]);
					String[] sScale = sPart[1].split(";");
					if (sScale.length == 1)
						reqAmount = Integer.parseInt(sPart[1]);
					else if (sScale.length == 2) {
						if (sScale[1].charAt(0) == '+') {
							reqAmount = Integer.parseInt(sScale[0])
									+ Integer.parseInt(sScale[1].substring(1))
									* ((PlayerInfo) getInstance()
											.getActivePlayers().get(
													player.getName()))
											.checkChallengeSinceTimer(challenge);
						} else if (sScale[1].charAt(0) == '*') {
							reqAmount = Integer.parseInt(sScale[0])
									* Integer.parseInt(sScale[1].substring(1))
									* ((PlayerInfo) getInstance()
											.getActivePlayers().get(
													player.getName()))
											.checkChallengeSinceTimer(challenge);
						} else if (sScale[1].charAt(0) == '-') {
							reqAmount = Integer.parseInt(sScale[0])
									- Integer.parseInt(sScale[1].substring(1))
									* ((PlayerInfo) getInstance()
											.getActivePlayers().get(
													player.getName()))
											.checkChallengeSinceTimer(challenge);
						} else if (sScale[1].charAt(0) == '/') {
							reqAmount = Integer.parseInt(sScale[0])
									/ Integer.parseInt(sScale[1].substring(1))
									* ((PlayerInfo) getInstance()
											.getActivePlayers().get(
													player.getName()))
											.checkChallengeSinceTimer(challenge);
						}
					}

					if (!player.getInventory().containsAtLeast(
							new ItemStack(reqItem, reqAmount), reqAmount))
						return false;
				} else {
					if (sPart.length != 3)
						continue;
					reqItem = Integer.parseInt(sPart[0]);
					String[] sScale = sPart[2].split(";");
					if (sScale.length == 1)
						reqAmount = Integer.parseInt(sPart[2]);
					else if (sScale.length == 2) {
						if (sScale[1].charAt(0) == '+') {
							reqAmount = Integer.parseInt(sScale[0])
									+ Integer.parseInt(sScale[1].substring(1))
									* ((PlayerInfo) getInstance()
											.getActivePlayers().get(
													player.getName()))
											.checkChallengeSinceTimer(challenge);
						} else if (sScale[1].charAt(0) == '*') {
							reqAmount = Integer.parseInt(sScale[0])
									* Integer.parseInt(sScale[1].substring(1))
									* ((PlayerInfo) getInstance()
											.getActivePlayers().get(
													player.getName()))
											.checkChallengeSinceTimer(challenge);
						} else if (sScale[1].charAt(0) == '-') {
							reqAmount = Integer.parseInt(sScale[0])
									- Integer.parseInt(sScale[1].substring(1))
									* ((PlayerInfo) getInstance()
											.getActivePlayers().get(
													player.getName()))
											.checkChallengeSinceTimer(challenge);
						} else if (sScale[1].charAt(0) == '/') {
							reqAmount = Integer.parseInt(sScale[0])
									/ Integer.parseInt(sScale[1].substring(1))
									* ((PlayerInfo) getInstance()
											.getActivePlayers().get(
													player.getName()))
											.checkChallengeSinceTimer(challenge);
						}
					}
					reqMod = Integer.parseInt(sPart[1]);
					if (!player.getInventory().containsAtLeast(
							new ItemStack(reqItem, reqAmount, (short) reqMod),
							reqAmount))
						return false;
				}
			}
			if (getConfig().getBoolean(
					"options.challenges.challengeList." + challenge
							+ ".takeItems"))
				takeRequired(player, challenge, type);
			return true;
		}
		if (type.equalsIgnoreCase("onIsland")) {
			int[][] neededItem = new int[reqList.length][2];
			for (int i = 0; i < reqList.length; i++) {
				String[] sPart = reqList[i].split(":");
				neededItem[i][0] = Integer.parseInt(sPart[0]);
				neededItem[i][1] = Integer.parseInt(sPart[1]);
			}
			Location l = player.getLocation();
			int px = l.getBlockX();
			int py = l.getBlockY();
			int pz = l.getBlockZ();
			for (int k = -10; k <= 10; k++) {
				for (int i1 = -3; i1 <= 10; i1++)
					for (int z = -10; z <= 10; z++) {
						Block b = new Location(l.getWorld(), px + k, py + i1,
								pz + z).getBlock();
						for (int i = 0; i < neededItem.length; i++) {
							if (b.getTypeId() != neededItem[i][0]) {
								continue;
							}
							neededItem[i][1] -= 1;
						}
					}
			}
			int l1 = 0;
			while (true) {
				if (neededItem[l1][1] > 0) {
					return false;
				}
				l1++;

				if (l1 < neededItem.length) {
					continue;
				}

			}

		}

		return true;
	}

	public boolean giveReward(Player player, String challenge) {
		String[] permList = getConfig().getString(
				"options.challenges.challengeList." + challenge.toLowerCase()
						+ ".permissionReward").split(" ");
		double rewCurrency = 0.0D;
		player.sendMessage(ChatColor.AQUA + "당신은 " + challenge
				+ " 과제를 완료했습니다!");
		String[] rewList;
		if (((PlayerInfo) getInstance().getActivePlayers()
				.get(player.getName())).checkChallenge(challenge) == 0) {
			rewList = getConfig().getString(
					"options.challenges.challengeList."
							+ challenge.toLowerCase() + ".itemReward").split(
					" ");
			if ((Settings.challenges_enableEconomyPlugin)
					&& (VaultHandler.econ != null))
				rewCurrency = getConfig().getInt(
						"options.challenges.challengeList."
								+ challenge.toLowerCase() + ".currencyReward");
		} else {
			rewList = getConfig().getString(
					"options.challenges.challengeList."
							+ challenge.toLowerCase() + ".repeatItemReward")
					.split(" ");
			if ((Settings.challenges_enableEconomyPlugin)
					&& (VaultHandler.econ != null)) {
				rewCurrency = getConfig().getInt(
						"options.challenges.challengeList."
								+ challenge.toLowerCase()
								+ ".repeatCurrencyReward");
			}
		}
		int rewItem = 0;
		int rewAmount = 0;
		double rewBonus = 1.0D;
		int rewMod = -1;
		if ((Settings.challenges_enableEconomyPlugin)
				&& (VaultHandler.econ != null)) {
			if (VaultHandler.checkPerk(player.getName(), "group.memberplus",
					getSkyBlockWorld())) {
				rewBonus += 0.05D;
			}
			if (VaultHandler.checkPerk(player.getName(), "usb.donor.all",
					getSkyBlockWorld())) {
				rewBonus += 0.05D;
			}
			if (VaultHandler.checkPerk(player.getName(), "usb.donor.25",
					getSkyBlockWorld())) {
				rewBonus += 0.05D;
			}
			if (VaultHandler.checkPerk(player.getName(), "usb.donor.50",
					getSkyBlockWorld())) {
				rewBonus += 0.05D;
			}
			if (VaultHandler.checkPerk(player.getName(), "usb.donor.75",
					getSkyBlockWorld())) {
				rewBonus += 0.1D;
			}
			if (VaultHandler.checkPerk(player.getName(), "usb.donor.100",
					getSkyBlockWorld())) {
				rewBonus += 0.2D;
			}
			VaultHandler.econ.depositPlayer(player.getName(), rewCurrency
					* rewBonus);
			if (((PlayerInfo) getInstance().getActivePlayers().get(
					player.getName())).checkChallenge(challenge) > 0) {
				player.giveExp(getInstance().getConfig().getInt(
						"options.challenges.challengeList." + challenge
								+ ".repeatXpReward"));
				player.sendMessage(ChatColor.GREEN
						+ "반복 보수 : "
						+ ChatColor.GOLD
						+ getInstance()
								.getConfig()
								.getString(
										new StringBuilder(
												"options.challenges.challengeList.")
												.append(challenge)
												.append(".repeatRewardText")
												.toString()).replace('&', '§'));
				player.sendMessage(ChatColor.GREEN
						+ "반복 경험치 보수 : "
						+ ChatColor.GOLD
						+ getInstance().getConfig().getInt(
								new StringBuilder(
										"options.challenges.challengeList.")
										.append(challenge)
										.append(".repeatXpReward").toString()));
				player.sendMessage(ChatColor.GREEN
						+ "반복 금액 보수 : "
						+ ChatColor.GOLD
						+ this.df.format(getInstance().getConfig().getInt(
								new StringBuilder(
										"options.challenges.challengeList.")
										.append(challenge)
										.append(".repeatCurrencyReward")
										.toString())
								* rewBonus) + " "
						+ VaultHandler.econ.currencyNamePlural() + "§b(+"
						+ this.df.format((rewBonus - 1.0D) * 100.0D) + "%)");
			} else {
				if (Settings.challenges_broadcastCompletion)
					Bukkit.getServer().broadcastMessage(
							Settings.challenges_broadcastText.replace('&', '§')
									+ player.getName() + " has completed the "
									+ challenge + " challenge!");
				player.giveExp(getInstance().getConfig().getInt(
						"options.challenges.challengeList." + challenge
								+ ".xpReward"));
				player.sendMessage(ChatColor.GREEN
						+ "보수 : "
						+ ChatColor.GOLD
						+ getInstance()
								.getConfig()
								.getString(
										new StringBuilder(
												"options.challenges.challengeList.")
												.append(challenge)
												.append(".rewardText")
												.toString()).replace('&', '§'));
				player.sendMessage(ChatColor.GREEN
						+ "경험치 보수 : "
						+ ChatColor.GOLD
						+ getInstance().getConfig().getInt(
								new StringBuilder(
										"options.challenges.challengeList.")
										.append(challenge).append(".xpReward")
										.toString()));
				player.sendMessage(ChatColor.GREEN
						+ "금액 보수 : "
						+ ChatColor.GOLD
						+ this.df.format(getInstance().getConfig().getInt(
								new StringBuilder(
										"options.challenges.challengeList.")
										.append(challenge)
										.append(".currencyReward").toString())
								* rewBonus) + " "
						+ VaultHandler.econ.currencyNamePlural() + "§b(+"
						+ this.df.format((rewBonus - 1.0D) * 100.0D) + "%)");
			}

		} else if (((PlayerInfo) getInstance().getActivePlayers().get(
				player.getName())).checkChallenge(challenge) > 0) {
			player.giveExp(getInstance().getConfig().getInt(
					"options.challenges.challengeList." + challenge
							+ ".repeatXpReward"));
			player.sendMessage(ChatColor.GREEN
					+ "반복 보수 : "
					+ ChatColor.GOLD
					+ getInstance()
							.getConfig()
							.getString(
									new StringBuilder(
											"options.challenges.challengeList.")
											.append(challenge)
											.append(".repeatRewardText")
											.toString()).replace('&', '§'));
			player.sendMessage(ChatColor.GREEN
					+ "반복 경험치 보수 : "
					+ ChatColor.GOLD
					+ getInstance().getConfig().getInt(
							new StringBuilder(
									"options.challenges.challengeList.")
									.append(challenge)
									.append(".repeatXpReward").toString()));
		} else {
			if (Settings.challenges_broadcastCompletion)
				Bukkit.getServer().broadcastMessage(
						Settings.challenges_broadcastText.replace('&', '§')
								+ player.getName() + " has completed the "
								+ challenge + " challenge!");
			player.giveExp(getInstance().getConfig().getInt(
					"options.challenges.challengeList." + challenge
							+ ".xpReward"));
			player.sendMessage(ChatColor.GREEN
					+ "보수 : "
					+ ChatColor.GOLD
					+ getInstance()
							.getConfig()
							.getString(
									new StringBuilder(
											"options.challenges.challengeList.")
											.append(challenge)
											.append(".rewardText").toString())
							.replace('&', '§'));
			player.sendMessage(ChatColor.GREEN
					+ "경험치 보수 : "
					+ ChatColor.GOLD
					+ getInstance().getConfig().getInt(
							new StringBuilder(
									"options.challenges.challengeList.")
									.append(challenge).append(".xpReward")
									.toString()));
		}

		for (String s : permList) {
			if ((s.equalsIgnoreCase("none"))
					|| (VaultHandler.checkPerk(player.getName(), s,
							player.getWorld())))
				continue;
			VaultHandler.addPerk(player, s);
		}

		for (String s : rewList) {
			String[] sPart = s.split(":");
			if (sPart.length == 2) {
				rewItem = Integer.parseInt(sPart[0]);
				rewAmount = Integer.parseInt(sPart[1]);
				player.getInventory().addItem(
						new ItemStack[] { new ItemStack(rewItem, rewAmount) });
			} else {
				if (sPart.length != 3)
					continue;
				rewItem = Integer.parseInt(sPart[0]);
				rewAmount = Integer.parseInt(sPart[2]);
				rewMod = Integer.parseInt(sPart[1]);
				player.getInventory().addItem(
						new ItemStack[] { new ItemStack(rewItem, rewAmount,
								(short) rewMod) });
			}
		}
		((PlayerInfo) getInstance().getActivePlayers().get(player.getName()))
				.completeChallenge(challenge);

		return true;
	}

	public void reloadData() {
		if (this.skyblockDataFile == null) {
			this.skyblockDataFile = new File(getDataFolder(),
					"skyblockData.yml");
		}
		this.skyblockData = YamlConfiguration
				.loadConfiguration(this.skyblockDataFile);

		InputStream defConfigStream = getResource("skyblockData.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration
					.loadConfiguration(defConfigStream);
			this.skyblockData.setDefaults(defConfig);
		}
	}

	public FileConfiguration getData() {
		if (this.skyblockData == null) {
			reloadData();
		}
		return this.skyblockData;
	}

	double dReturns(double val, double scale) {
		if (val < 0.0D)
			return -dReturns(-val, scale);
		double mult = val / scale;
		double trinum = (Math.sqrt(8.0D * mult + 1.0D) - 1.0D) / 2.0D;
		return trinum * scale;
	}

	public void reloadLevelConfig() {
		if (this.levelConfigFile == null) {
			this.levelConfigFile = new File(getDataFolder(), "levelConfig.yml");
		}
		this.levelConfig = YamlConfiguration
				.loadConfiguration(this.levelConfigFile);

		InputStream defConfigStream = getResource("levelConfig.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration
					.loadConfiguration(defConfigStream);
			this.levelConfig.setDefaults(defConfig);
		}
	}

	public FileConfiguration getLevelConfig() {
		if (this.levelConfig == null) {
			reloadLevelConfig();
		}
		return this.levelConfig;
	}

	public void saveLevelConfig() {
		if ((this.levelConfig == null) || (this.levelConfigFile == null))
			return;
		try {
			getLevelConfig().save(this.levelConfigFile);
		} catch (IOException ex) {
			getLogger().log(Level.SEVERE,
					"Could not save config to " + this.levelConfigFile, ex);
		}
	}

	public void saveDefaultLevelConfig() {
		if (this.levelConfigFile == null) {
			this.levelConfigFile = new File(getDataFolder(), "levelConfig.yml");
		}
		if (!this.levelConfigFile.exists())
			getInstance().saveResource("levelConfig.yml", false);
	}

	public void loadLevelConfig() {
		try {
			getLevelConfig();
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (int i = 1; i <= 255; i++) {
			if (getLevelConfig().contains("blockValues." + i))
				Settings.blockList[i] = getLevelConfig().getInt(
						"blockValues." + i);
			else
				Settings.blockList[i] = getLevelConfig().getInt(
						"general.default");
			if (getLevelConfig().contains("blockLimits." + i))
				Settings.limitList[i] = getLevelConfig().getInt(
						"blockLimits." + i);
			else
				Settings.limitList[i] = -1;
			if (getLevelConfig().contains("diminishingReturns." + i)) {
				Settings.diminishingReturnsList[i] = getLevelConfig().getInt(
						"diminishingReturns." + i);
			} else if (getLevelConfig().getBoolean(
					"general.useDiminishingReturns")) {
				Settings.diminishingReturnsList[i] = getLevelConfig().getInt(
						"general.defaultScale");
			} else {
				Settings.diminishingReturnsList[i] = -1;
			}
		}
	}

	public void clearIslandConfig(String location, String leader) {
		getIslandConfig(location).set("general.level", Integer.valueOf(0));
		getIslandConfig(location).set("general.warpLocationX",
				Integer.valueOf(0));
		getIslandConfig(location).set("general.warpLocationY",
				Integer.valueOf(0));
		getIslandConfig(location).set("general.warpLocationZ",
				Integer.valueOf(0));
		getIslandConfig(location).set("general.warpActive",
				Boolean.valueOf(false));
		getIslandConfig(location).set("log.logPos", Integer.valueOf(1));
		getIslandConfig(location).set("log.1",
				"§d[skyblock] The island has been created.");
		setupPartyLeader(location, leader);
	}

	public void setupPartyLeader(String location, String leader) {
		getIslandConfig(location).createSection("party.members." + leader);
		getIslandConfig(location);
		FileConfiguration.createPath(getIslandConfig(location)
				.getConfigurationSection("party.members." + leader),
				"canChangeBiome");
		getIslandConfig(location);
		FileConfiguration.createPath(getIslandConfig(location)
				.getConfigurationSection("party.members." + leader),
				"canToggleLock");
		getIslandConfig(location);
		FileConfiguration.createPath(getIslandConfig(location)
				.getConfigurationSection("party.members." + leader),
				"canChangeWarp");
		getIslandConfig(location);
		FileConfiguration.createPath(getIslandConfig(location)
				.getConfigurationSection("party.members." + leader),
				"canToggleWarp");
		getIslandConfig(location);
		FileConfiguration.createPath(getIslandConfig(location)
				.getConfigurationSection("party.members." + leader),
				"canInviteOthers");
		getIslandConfig(location);
		FileConfiguration.createPath(getIslandConfig(location)
				.getConfigurationSection("party.members." + leader),
				"canKickOthers");
		getIslandConfig(location).set("party.leader", leader);
		getIslandConfig(location).set(
				"party.members." + leader + ".canChangeBiome",
				Boolean.valueOf(true));
		getIslandConfig(location).set(
				"party.members." + leader + ".canToggleLock",
				Boolean.valueOf(true));
		getIslandConfig(location).set(
				"party.members." + leader + ".canChangeWarp",
				Boolean.valueOf(true));
		getIslandConfig(location).set(
				"party.members." + leader + ".canToggleWarp",
				Boolean.valueOf(true));
		getIslandConfig(location).set(
				"party.members." + leader + ".canInviteOthers",
				Boolean.valueOf(true));
		getIslandConfig(location).set(
				"party.members." + leader + ".canKickOthers",
				Boolean.valueOf(true));
		saveIslandConfig(location);
	}

	public void setupPartyMember(String location, String member) {
		getIslandConfig(location).createSection("party.members." + member);
		getIslandConfig(location);
		FileConfiguration.createPath(getIslandConfig(location)
				.getConfigurationSection("party.members." + member),
				"canChangeBiome");
		getIslandConfig(location);
		FileConfiguration.createPath(getIslandConfig(location)
				.getConfigurationSection("party.members." + member),
				"canToggleLock");
		getIslandConfig(location);
		FileConfiguration.createPath(getIslandConfig(location)
				.getConfigurationSection("party.members." + member),
				"canChangeWarp");
		getIslandConfig(location);
		FileConfiguration.createPath(getIslandConfig(location)
				.getConfigurationSection("party.members." + member),
				"canToggleWarp");
		getIslandConfig(location);
		FileConfiguration.createPath(getIslandConfig(location)
				.getConfigurationSection("party.members." + member),
				"canInviteOthers");
		getIslandConfig(location);
		FileConfiguration.createPath(getIslandConfig(location)
				.getConfigurationSection("party.members." + member),
				"canKickOthers");
		getIslandConfig(location).set(
				"party.members." + member + ".canChangeBiome",
				Boolean.valueOf(false));
		getIslandConfig(location).set(
				"party.currentSize",
				Integer.valueOf(getIslandConfig(location).getInt(
						"party.currentSize") + 1));
		getIslandConfig(location).set(
				"party.members." + member + ".canToggleLock",
				Boolean.valueOf(false));
		getIslandConfig(location).set(
				"party.members." + member + ".canChangeWarp",
				Boolean.valueOf(false));
		getIslandConfig(location).set(
				"party.members." + member + ".canToggleWarp",
				Boolean.valueOf(false));
		getIslandConfig(location).set(
				"party.members." + member + ".canInviteOthers",
				Boolean.valueOf(false));
		getIslandConfig(location).set(
				"party.members." + member + ".canKickOthers",
				Boolean.valueOf(false));
		getIslandConfig(location).set(
				"party.members." + member + ".canBanOthers",
				Boolean.valueOf(false));
		saveIslandConfig(location);
	}

	public void reloadIslandConfig(String location) {
		this.islandConfigFile = new File(this.directoryIslands, location
				+ ".yml");
		this.islands.put(location,
				YamlConfiguration.loadConfiguration(this.islandConfigFile));
		InputStream defConfigStream = getResource("island.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration
					.loadConfiguration(defConfigStream);
			((FileConfiguration) this.islands.get(location))
					.setDefaults(defConfig);
		}
		saveIslandConfig(location);
	}

	public FileConfiguration getTempIslandConfig(String location) {
		this.tempIslandFile = new File(this.directoryIslands, location + ".yml");
		this.tempIsland = YamlConfiguration
				.loadConfiguration(this.tempIslandFile);
		return this.tempIsland;
	}

	public FileConfiguration getCurrentPlayerConfig(String player) {
		this.tempPlayerFile = new File(this.directoryPlayers, player + ".yml");
		this.tempPlayer = YamlConfiguration
				.loadConfiguration(this.tempPlayerFile);
		return this.tempPlayer;
	}

	public void createIslandConfig(String location, String leader) {
		saveDefaultIslandsConfig(location);
		this.islandConfigFile = new File(this.directoryIslands, location
				+ ".yml");

		InputStream defConfigStream = getResource("island.yml");
		if (defConfigStream == null)
			return;
		this.islands.put(location,
				YamlConfiguration.loadConfiguration(defConfigStream));
		getIslandConfig(location);
		setupPartyLeader(location, leader);
	}

	public FileConfiguration getIslandConfig(String location) {
		if (this.islands.get(location) == null) {
			reloadIslandConfig(location);
		}
		return (FileConfiguration) this.islands.get(location);
	}

	public void saveIslandConfig(String location) {
		if (this.islands.get(location) == null)
			return;
		try {
			this.islandConfigFile = new File(this.directoryIslands, location
					+ ".yml");
			getIslandConfig(location).save(this.islandConfigFile);
		} catch (IOException ex) {
			getLogger().log(Level.SEVERE,
					"Could not save config to " + this.islandConfigFile, ex);
		}
	}

	public void deleteIslandConfig(String location) {
		this.islandConfigFile = new File(this.directoryIslands, location
				+ ".yml");
		this.islandConfigFile.delete();
	}

	public void saveDefaultIslandsConfig(String location) {
		try {
			if (this.islandConfigFile == null) {
				this.islandConfigFile = new File(this.directoryIslands,
						location + ".yml");
				getIslandConfig(location).save(this.islandConfigFile);
			}
		} catch (IOException ex) {
			getLogger().log(Level.SEVERE,
					"Could not save config to " + this.islandConfigFile, ex);
		}
	}

	public void reloadLastIslandConfig() {
		if (this.lastIslandConfigFile == null) {
			this.lastIslandConfigFile = new File(getDataFolder(),
					"lastIslandConfig.yml");
		}
		this.lastIslandConfig = YamlConfiguration
				.loadConfiguration(this.lastIslandConfigFile);

		InputStream defConfigStream = getResource("lastIslandConfig.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration
					.loadConfiguration(defConfigStream);
			this.lastIslandConfig.setDefaults(defConfig);
		}
	}

	public FileConfiguration getLastIslandConfig() {
		if (this.lastIslandConfig == null) {
			reloadLastIslandConfig();
		}
		return this.lastIslandConfig;
	}

	public void saveLastIslandConfig() {
		if ((this.lastIslandConfig == null)
				|| (this.lastIslandConfigFile == null))
			return;
		try {
			getLastIslandConfig().save(this.lastIslandConfigFile);
		} catch (IOException ex) {
			getLogger()
					.log(Level.SEVERE,
							"Could not save config to "
									+ this.lastIslandConfigFile, ex);
		}
	}

	public void saveDefaultLastIslandConfig() {
		if (this.lastIslandConfigFile == null) {
			this.lastIslandConfigFile = new File(getDataFolder(),
					"lastIslandConfig.yml");
		}
		if (!this.lastIslandConfigFile.exists())
			getInstance().saveResource("lastIslandConfig.yml", false);
	}

	public void reloadOrphans() {
		if (this.orphanFile == null) {
			this.orphanFile = new File(getDataFolder(), "orphans.yml");
		}
		this.orphans = YamlConfiguration.loadConfiguration(this.orphanFile);

		InputStream defConfigStream = getResource("orphans.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration
					.loadConfiguration(defConfigStream);
			this.orphans.setDefaults(defConfig);
		}
	}

	public FileConfiguration getOrphans() {
		if (this.orphans == null) {
			reloadOrphans();
		}
		return this.orphans;
	}

	public void saveOrphansFile() {
		if ((this.orphans == null) || (this.orphanFile == null))
			return;
		try {
			getOrphans().save(this.orphanFile);
		} catch (IOException ex) {
			getLogger().log(Level.SEVERE,
					"Could not save config to " + this.orphanFile, ex);
		}
	}

	public void saveDefaultOrphans() {
		if (this.orphanFile == null) {
			this.orphanFile = new File(getDataFolder(), "orphans.yml");
		}
		if (!this.orphanFile.exists())
			getInstance().saveResource("orphans.yml", false);
	}

	public boolean setBiome(Location loc, String bName) {
		int px = loc.getBlockX();
		int pz = loc.getBlockZ();
		Biome bType = Biome.OCEAN;
		if (bName.equalsIgnoreCase("jungle")) {
			bType = Biome.JUNGLE;
		} else if (bName.equalsIgnoreCase("hell")) {
			bType = Biome.HELL;
		} else if (bName.equalsIgnoreCase("sky")) {
			bType = Biome.SKY;
		} else if (bName.equalsIgnoreCase("mushroom")) {
			bType = Biome.MUSHROOM_ISLAND;
		} else if (bName.equalsIgnoreCase("ocean")) {
			bType = Biome.OCEAN;
		} else if (bName.equalsIgnoreCase("swampland")) {
			bType = Biome.SWAMPLAND;
		} else if (bName.equalsIgnoreCase("taiga")) {
			bType = Biome.TAIGA;
		} else if (bName.equalsIgnoreCase("desert")) {
			bType = Biome.DESERT;
		} else if (bName.equalsIgnoreCase("forest")) {
			bType = Biome.FOREST;
		} else {
			bType = Biome.OCEAN;
		}
		for (int x = Settings.island_protectionRange / 2 * -1 - 16; x <= Settings.island_protectionRange / 2 + 16; x += 16) {
			for (int z = Settings.island_protectionRange / 2 * -1 - 16; z <= Settings.island_protectionRange / 2 + 16; z += 16) {
				getSkyBlockWorld().loadChunk((px + x) / 16, (pz + z) / 16);
			}
		}
		for (int x = Settings.island_protectionRange / 2 * -1; x <= Settings.island_protectionRange / 2; x++) {
			for (int z = Settings.island_protectionRange / 2 * -1; z <= Settings.island_protectionRange / 2; z++) {
				getSkyBlockWorld().setBiome(px + x, pz + z, bType);
			}
		}
		for (int x = Settings.island_protectionRange / 2 * -1 - 16; x <= Settings.island_protectionRange / 2 + 16; x += 16) {
			for (int z = Settings.island_protectionRange / 2 * -1 - 16; z <= Settings.island_protectionRange / 2 + 16; z += 16) {
				getSkyBlockWorld().refreshChunk((px + x) / 16, (pz + z) / 16);
			}

		}

		return bType != Biome.OCEAN;
	}

	public boolean changePlayerBiome(Player player, String bName) {
		if (VaultHandler.checkPerk(player.getName(), "usb.biome." + bName,
				player.getWorld())) {
			if (getInstance().getIslandConfig(
					((PlayerInfo) getInstance().getActivePlayers().get(
							player.getName())).locationForParty()).getBoolean(
					"party.members." + player.getName() + ".canChangeBiome")) {
				setBiome(
						((PlayerInfo) getInstance().getActivePlayers().get(
								player.getName())).getIslandLocation(), bName);
				setConfigBiome(player, bName);
				return true;
			}
			return false;
		}
		return false;
	}

	public void listBiomes(Player player) {
		String biomeList = ", ";
		if (VaultHandler.checkPerk(player.getName(), "usb.biome.ocean",
				getSkyBlockWorld())) {
			biomeList = "OCEAN, ";
		}
		if (VaultHandler.checkPerk(player.getName(), "usb.biome.forest",
				getSkyBlockWorld())) {
			biomeList = biomeList + "FOREST, ";
		}
		if (VaultHandler.checkPerk(player.getName(), "usb.biome.jungle",
				getSkyBlockWorld())) {
			biomeList = biomeList + "JUNGLE, ";
		}
		if (VaultHandler.checkPerk(player.getName(), "usb.biome.desert",
				getSkyBlockWorld())) {
			biomeList = biomeList + "DESERT, ";
		}
		if (VaultHandler.checkPerk(player.getName(), "usb.biome.taiga",
				getSkyBlockWorld())) {
			biomeList = biomeList + "TAIGA, ";
		}
		if (VaultHandler.checkPerk(player.getName(), "usb.biome.swampland",
				getSkyBlockWorld())) {
			biomeList = biomeList + "SWAMPLAND, ";
		}
		if (VaultHandler.checkPerk(player.getName(), "usb.biome.mushroom",
				getSkyBlockWorld())) {
			biomeList = biomeList + "MUSHROOM, ";
		}
		if (VaultHandler.checkPerk(player.getName(), "usb.biome.hell",
				getSkyBlockWorld())) {
			biomeList = biomeList + "HELL, ";
		}
		if (VaultHandler.checkPerk(player.getName(), "usb.biome.sky",
				getSkyBlockWorld())) {
			biomeList = biomeList + "SKY, ";
		}
		player.sendMessage(ChatColor.GREEN + "다음과 같은 바이옴에 액세스 할 수 있습니다 :");
		player.sendMessage(ChatColor.AQUA
				+ biomeList.substring(0, biomeList.length() - 2));
		player.sendMessage(ChatColor.GREEN + "당신은 "
				+ Settings.general_biomeChange / 60 + " 분후에 바이옴을 바꿀수 있습니다.");
		player.sendMessage(ChatColor.GREEN
				+ "/섬 바이옴 <바이옴이름> 으로 당신의 바이옴을 바꿀수 있습니다.");
	}

	public boolean createIsland(CommandSender sender, PlayerInfo pi) {
		System.out.println("Creating player island...");
		Player player = (Player) sender;
		Location last = getInstance().getLastIsland();
		last.setY(Settings.island_height);
		try {
			while(getInstance().hasOrphanedIsland()
					&& getInstance()
					.islandAtLocation(getInstance().checkOrphan())) {
				getInstance().removeNextOrphan();
			}

			while ((getInstance().hasOrphanedIsland())
					&& (!getInstance().checkOrphan().getWorld().getName()
							.equalsIgnoreCase(Settings.general_worldName))) {
				getInstance().removeNextOrphan();
			}
			Location next;
			if (getInstance().hasOrphanedIsland()){
				next = getInstance().getOrphanedIsland();
				getInstance().saveOrphans();
			} else {
				next = nextIslandLocation(last);
				getInstance().setLastIsland(next);

				while (getInstance().islandAtLocation(next)) {
					next = nextIslandLocation(next);
				}

				while (getInstance().islandInSpawn(next)) {
					next = nextIslandLocation(next);
				}

				getInstance().setLastIsland(next);
			}
			boolean hasIslandNow = false;

			if ((getInstance().getSchemFile().length > 0)
					&& (Bukkit.getServer().getPluginManager()
							.isPluginEnabled("WorldEdit"))) {
				String cSchem = "";
				for (int i = 0; i < getInstance().getSchemFile().length; i++) {
					if (hasIslandNow)
						continue;
					if (getInstance().getSchemFile()[i].getName().lastIndexOf(
							'.') > 0) {
						cSchem = getInstance().getSchemFile()[i].getName()
								.substring(
										0,
										getInstance().getSchemFile()[i]
												.getName().lastIndexOf('.'));
					} else {
						cSchem = getInstance().getSchemFile()[i].getName();
					}
					if ((!VaultHandler.checkPerk(player.getName(),
							"usb.schematic." + cSchem, getSkyBlockWorld()))
							|| (!WorldEditHandler.loadIslandSchematic(
									getSkyBlockWorld(), getInstance()
											.getSchemFile()[i], next)))
						continue;
					setChest(next, player);
					hasIslandNow = true;
				}

				if (!hasIslandNow) {
					for (int i = 0; i < getInstance().getSchemFile().length; i++) {
						if (getInstance().getSchemFile()[i].getName()
								.lastIndexOf('.') > 0) {
							cSchem = getInstance().getSchemFile()[i]
									.getName()
									.substring(
											0,
											getInstance().getSchemFile()[i]
													.getName().lastIndexOf('.'));
						} else
							cSchem = getInstance().getSchemFile()[i].getName();
						if ((!cSchem
								.equalsIgnoreCase(Settings.island_schematicName))
								|| (!WorldEditHandler.loadIslandSchematic(
										getSkyBlockWorld(), getInstance()
												.getSchemFile()[i], next)))
							continue;
						setChest(next, player);
						hasIslandNow = true;
					}
				}

			}

			if (!hasIslandNow) {
				if (!Settings.island_useOldIslands)
					generateIslandBlocks(next.getBlockX(), next.getBlockZ(),
							player, getSkyBlockWorld());
				else
					oldGenerateIslandBlocks(next.getBlockX(), next.getBlockZ(),
							player, getSkyBlockWorld());
			}
			next.setY(Settings.island_height);

			System.out.println("Preparing to set new player information...");
			setNewPlayerIsland(player, next);
			System.out.println("Finished setting new player information.");

			System.out.println("Preparing to set initial player biome...");
			getInstance().changePlayerBiome(player, "OCEAN");
			System.out.println("Finished setting initial player biome.");
			for (int x = Settings.island_protectionRange / 2 * -1 - 16; x <= Settings.island_protectionRange / 2 + 16; x += 16) {
				for (int z = Settings.island_protectionRange / 2 * -1 - 16; z <= Settings.island_protectionRange / 2 + 16; z += 16) {
					getSkyBlockWorld().refreshChunk(
							(next.getBlockX() + x) / 16,
							(next.getBlockZ() + z) / 16);
				}
			}
			Iterator<Entity> ents = player.getNearbyEntities(50.0D, 250.0D, 50.0D)
					.iterator();
			while (ents.hasNext()) {
				Entity tempent = (Entity) ents.next();
				if ((tempent instanceof Player)) {
					continue;
				}

				tempent.remove();
			}
			if ((Settings.island_protectWithWorldGuard)
					&& (Bukkit.getServer().getPluginManager()
							.isPluginEnabled("WorldGuard")))
				WorldGuardHandler.protectIsland(player, sender.getName(), pi);
		} catch (Exception ex) {
			player.sendMessage("당신의 섬을 만들 수 없습니다. 서버 관리자에게 문의하십시오.");
			ex.printStackTrace();
			return false;
		}
		System.out.println("Finished creating player island.");
		return true;
	}

	public void generateIslandBlocks(int x, int z, Player player, World world) {
		int y = Settings.island_height;
		Block blockToChange = world.getBlockAt(x, y, z);
		blockToChange.setTypeId(7);
		islandLayer1(x, z, player, world);
		islandLayer2(x, z, player, world);
		islandLayer3(x, z, player, world);
		islandLayer4(x, z, player, world);
		islandExtras(x, z, player, world);
	}

	public void oldGenerateIslandBlocks(int x, int z, Player player, World world) {
		int y = Settings.island_height;

		for (int x_operate = x; x_operate < x + 3; x_operate++) {
			for (int y_operate = y; y_operate < y + 3; y_operate++) {
				for (int z_operate = z; z_operate < z + 6; z_operate++) {
					Block blockToChange = world.getBlockAt(x_operate,
							y_operate, z_operate);
					blockToChange.setTypeId(2);
				}
			}
		}

		for (int x_operate = x + 3; x_operate < x + 6; x_operate++) {
			for (int y_operate = y; y_operate < y + 3; y_operate++) {
				for (int z_operate = z + 3; z_operate < z + 6; z_operate++) {
					Block blockToChange = world.getBlockAt(x_operate,
							y_operate, z_operate);
					blockToChange.setTypeId(2);
				}
			}

		}

		for (int x_operate = x + 3; x_operate < x + 7; x_operate++) {
			for (int y_operate = y + 7; y_operate < y + 10; y_operate++) {
				for (int z_operate = z + 3; z_operate < z + 7; z_operate++) {
					Block blockToChange = world.getBlockAt(x_operate,
							y_operate, z_operate);
					blockToChange.setTypeId(18);
				}
			}

		}

		for (int y_operate = y + 3; y_operate < y + 9; y_operate++) {
			Block blockToChange = world.getBlockAt(x + 5, y_operate, z + 5);
			blockToChange.setTypeId(17);
		}

		Block blockToChange = world.getBlockAt(x + 1, y + 3, z + 1);
		blockToChange.setTypeId(54);
		Chest chest = (Chest) blockToChange.getState();
		Inventory inventory = chest.getInventory();
		inventory.setContents(Settings.island_chestItems);
		if (Settings.island_addExtraItems) {
			for (int i = 0; i < Settings.island_extraPermissions.length; i++) {
				if (!VaultHandler.checkPerk(player.getName(), "usb."
						+ Settings.island_extraPermissions[i],
						player.getWorld()))
					continue;
				String[] chestItemString = getInstance()
						.getConfig()
						.getString(
								"options.island.extraPermissions."
										+ Settings.island_extraPermissions[i])
						.split(" ");
				ItemStack[] tempChest = new ItemStack[chestItemString.length];
				String[] amountdata = new String[2];
				for (int j = 0; j < chestItemString.length; j++) {
					amountdata = chestItemString[j].split(":");
					tempChest[j] = new ItemStack(
							Integer.parseInt(amountdata[0]),
							Integer.parseInt(amountdata[1]));
					inventory.addItem(new ItemStack[] { tempChest[j] });
				}
			}

		}

		blockToChange = world.getBlockAt(x, y, z);
		blockToChange.setTypeId(7);

		blockToChange = world.getBlockAt(x + 2, y + 1, z + 1);
		blockToChange.setTypeId(12);
		blockToChange = world.getBlockAt(x + 2, y + 1, z + 2);
		blockToChange.setTypeId(12);
		blockToChange = world.getBlockAt(x + 2, y + 1, z + 3);
		blockToChange.setTypeId(12);
	}

	private Location nextIslandLocation(Location lastIsland) {
		int x = (int) lastIsland.getX();
		int z = (int) lastIsland.getZ();
		Location nextPos = lastIsland;
		if (x < z) {
			if (-1 * x < z) {
				nextPos.setX(nextPos.getX() + Settings.island_distance);
				return nextPos;
			}
			nextPos.setZ(nextPos.getZ() + Settings.island_distance);
			return nextPos;
		}
		if (x > z) {
			if (-1 * x >= z) {
				nextPos.setX(nextPos.getX() - Settings.island_distance);
				return nextPos;
			}
			nextPos.setZ(nextPos.getZ() - Settings.island_distance);
			return nextPos;
		}
		if (x <= 0) {
			nextPos.setZ(nextPos.getZ() + Settings.island_distance);
			return nextPos;
		}
		nextPos.setZ(nextPos.getZ() - Settings.island_distance);
		return nextPos;
	}

	private void islandLayer1(int x, int z, Player player, World world) {
		int y = Settings.island_height;
		y = Settings.island_height + 4;
		for (int x_operate = x - 3; x_operate <= x + 3; x_operate++) {
			for (int z_operate = z - 3; z_operate <= z + 3; z_operate++) {
				Block blockToChange = world.getBlockAt(x_operate, y, z_operate);
				blockToChange.setTypeId(2);
			}
		}
		Block blockToChange = world.getBlockAt(x - 3, y, z + 3);
		blockToChange.setTypeId(0);
		blockToChange = world.getBlockAt(x - 3, y, z - 3);
		blockToChange.setTypeId(0);
		blockToChange = world.getBlockAt(x + 3, y, z - 3);
		blockToChange.setTypeId(0);
		blockToChange = world.getBlockAt(x + 3, y, z + 3);
		blockToChange.setTypeId(0);
	}

	private void islandLayer2(int x, int z, Player player, World world) {
		int y = Settings.island_height;
		y = Settings.island_height + 3;
		for (int x_operate = x - 2; x_operate <= x + 2; x_operate++) {
			for (int z_operate = z - 2; z_operate <= z + 2; z_operate++) {
				Block blockToChange = world.getBlockAt(x_operate, y, z_operate);
				blockToChange.setTypeId(3);
			}
		}
		Block blockToChange = world.getBlockAt(x - 3, y, z);
		blockToChange.setTypeId(3);
		blockToChange = world.getBlockAt(x + 3, y, z);
		blockToChange.setTypeId(3);
		blockToChange = world.getBlockAt(x, y, z - 3);
		blockToChange.setTypeId(3);
		blockToChange = world.getBlockAt(x, y, z + 3);
		blockToChange.setTypeId(3);
		blockToChange = world.getBlockAt(x, y, z);
		blockToChange.setTypeId(12);
	}

	private void islandLayer3(int x, int z, Player player, World world) {
		int y = Settings.island_height;
		y = Settings.island_height + 2;
		for (int x_operate = x - 1; x_operate <= x + 1; x_operate++) {
			for (int z_operate = z - 1; z_operate <= z + 1; z_operate++) {
				Block blockToChange = world.getBlockAt(x_operate, y, z_operate);
				blockToChange.setTypeId(3);
			}
		}
		Block blockToChange = world.getBlockAt(x - 2, y, z);
		blockToChange.setTypeId(3);
		blockToChange = world.getBlockAt(x + 2, y, z);
		blockToChange.setTypeId(3);
		blockToChange = world.getBlockAt(x, y, z - 2);
		blockToChange.setTypeId(3);
		blockToChange = world.getBlockAt(x, y, z + 2);
		blockToChange.setTypeId(3);
		blockToChange = world.getBlockAt(x, y, z);
		blockToChange.setTypeId(12);
	}

	private void islandLayer4(int x, int z, Player player, World world) {
		int y = Settings.island_height;
		y = Settings.island_height + 1;
		Block blockToChange = world.getBlockAt(x - 1, y, z);
		blockToChange.setTypeId(3);
		blockToChange = world.getBlockAt(x + 1, y, z);
		blockToChange.setTypeId(3);
		blockToChange = world.getBlockAt(x, y, z - 1);
		blockToChange.setTypeId(3);
		blockToChange = world.getBlockAt(x, y, z + 1);
		blockToChange.setTypeId(3);
		blockToChange = world.getBlockAt(x, y, z);
		blockToChange.setTypeId(12);
	}

	private void islandExtras(int x, int z, Player player, World world) {
		int y = Settings.island_height;

		Block blockToChange = world.getBlockAt(x, y + 5, z);
		blockToChange.setTypeId(17);
		blockToChange = world.getBlockAt(x, y + 6, z);
		blockToChange.setTypeId(17);
		blockToChange = world.getBlockAt(x, y + 7, z);
		blockToChange.setTypeId(17);
		y = Settings.island_height + 8;
		for (int x_operate = x - 2; x_operate <= x + 2; x_operate++) {
			for (int z_operate = z - 2; z_operate <= z + 2; z_operate++) {
				blockToChange = world.getBlockAt(x_operate, y, z_operate);
				blockToChange.setTypeId(18);
			}
		}
		blockToChange = world.getBlockAt(x + 2, y, z + 2);
		blockToChange.setTypeId(0);
		blockToChange = world.getBlockAt(x + 2, y, z - 2);
		blockToChange.setTypeId(0);
		blockToChange = world.getBlockAt(x - 2, y, z + 2);
		blockToChange.setTypeId(0);
		blockToChange = world.getBlockAt(x - 2, y, z - 2);
		blockToChange.setTypeId(0);
		blockToChange = world.getBlockAt(x, y, z);
		blockToChange.setTypeId(17);
		y = Settings.island_height + 9;
		for (int x_operate = x - 1; x_operate <= x + 1; x_operate++) {
			for (int z_operate = z - 1; z_operate <= z + 1; z_operate++) {
				blockToChange = world.getBlockAt(x_operate, y, z_operate);
				blockToChange.setTypeId(18);
			}
		}
		blockToChange = world.getBlockAt(x - 2, y, z);
		blockToChange.setTypeId(18);
		blockToChange = world.getBlockAt(x + 2, y, z);
		blockToChange.setTypeId(18);
		blockToChange = world.getBlockAt(x, y, z - 2);
		blockToChange.setTypeId(18);
		blockToChange = world.getBlockAt(x, y, z + 2);
		blockToChange.setTypeId(18);
		blockToChange = world.getBlockAt(x, y, z);
		blockToChange.setTypeId(17);
		y = Settings.island_height + 10;
		blockToChange = world.getBlockAt(x - 1, y, z);
		blockToChange.setTypeId(18);
		blockToChange = world.getBlockAt(x + 1, y, z);
		blockToChange.setTypeId(18);
		blockToChange = world.getBlockAt(x, y, z - 1);
		blockToChange.setTypeId(18);
		blockToChange = world.getBlockAt(x, y, z + 1);
		blockToChange.setTypeId(18);
		blockToChange = world.getBlockAt(x, y, z);
		blockToChange.setTypeId(17);
		blockToChange = world.getBlockAt(x, y + 1, z);
		blockToChange.setTypeId(18);

		blockToChange = world.getBlockAt(x, Settings.island_height + 5, z + 1);
		blockToChange.setTypeId(54);
		Chest chest = (Chest) blockToChange.getState();
		Inventory inventory = chest.getInventory();
		inventory.setContents(Settings.island_chestItems);
		if (!Settings.island_addExtraItems)
			return;
		for (int i = 0; i < Settings.island_extraPermissions.length; i++) {
			if (!VaultHandler.checkPerk(player.getName(), "usb."
					+ Settings.island_extraPermissions[i], player.getWorld()))
				continue;
			String[] chestItemString = getInstance()
					.getConfig()
					.getString(
							"options.island.extraPermissions."
									+ Settings.island_extraPermissions[i])
					.split(" ");
			ItemStack[] tempChest = new ItemStack[chestItemString.length];
			String[] amountdata = new String[2];
			for (int j = 0; j < chestItemString.length; j++) {
				amountdata = chestItemString[j].split(":");
				tempChest[j] = new ItemStack(Integer.parseInt(amountdata[0]),
						Integer.parseInt(amountdata[1]));
				inventory.addItem(new ItemStack[] { tempChest[j] });
			}
		}
	}

	public void setChest(Location loc, Player player) {
		for (int x = -15; x <= 15; x++)
			for (int y = -15; y <= 15; y++)
				for (int z = -15; z <= 15; z++) {
					if (getSkyBlockWorld().getBlockAt(loc.getBlockX() + x,
							loc.getBlockY() + y, loc.getBlockZ() + z)
							.getTypeId() != 54)
						continue;
					Block blockToChange = getSkyBlockWorld().getBlockAt(
							loc.getBlockX() + x, loc.getBlockY() + y,
							loc.getBlockZ() + z);
					Chest chest = (Chest) blockToChange.getState();
					Inventory inventory = chest.getInventory();
					inventory.setContents(Settings.island_chestItems);
					if (!Settings.island_addExtraItems)
						continue;
					for (int i = 0; i < Settings.island_extraPermissions.length; i++) {
						if (!VaultHandler.checkPerk(player.getName(), "usb."
								+ Settings.island_extraPermissions[i],
								player.getWorld()))
							continue;
						String[] chestItemString = getInstance()
								.getConfig()
								.getString(
										"options.island.extraPermissions."
												+ Settings.island_extraPermissions[i])
								.split(" ");
						ItemStack[] tempChest = new ItemStack[chestItemString.length];
						String[] amountdata = new String[2];
						for (int j = 0; j < chestItemString.length; j++) {
							amountdata = chestItemString[j].split(":");
							tempChest[j] = new ItemStack(
									Integer.parseInt(amountdata[0]),
									Integer.parseInt(amountdata[1]));
							inventory.addItem(new ItemStack[] { tempChest[j] });
						}
					}
				}
	}

	public Location getChestSpawnLoc(Location loc, Player player) {
		for (int x = -15; x <= 15; x++) {
			for (int y = -15; y <= 15; y++) {
				for (int z = -15; z <= 15; z++) {
					if (getSkyBlockWorld().getBlockAt(loc.getBlockX() + x,
							loc.getBlockY() + y, loc.getBlockZ() + z)
							.getTypeId() != 54)
						continue;
					if ((getSkyBlockWorld().getBlockAt(loc.getBlockX() + x,
							loc.getBlockY() + y, loc.getBlockZ() + z + 1)
							.getTypeId() == 0)
							&& (getSkyBlockWorld().getBlockAt(
									loc.getBlockX() + x,
									loc.getBlockY() + y - 1,
									loc.getBlockZ() + z + 1).getTypeId() != 0))
						return new Location(getSkyBlockWorld(), loc.getBlockX()
								+ x, loc.getBlockY() + y + 1, loc.getBlockZ()
								+ z + 1);
					if ((getSkyBlockWorld().getBlockAt(loc.getBlockX() + x,
							loc.getBlockY() + y, loc.getBlockZ() + z - 1)
							.getTypeId() == 0)
							&& (getSkyBlockWorld().getBlockAt(
									loc.getBlockX() + x,
									loc.getBlockY() + y - 1,
									loc.getBlockZ() + z - 1).getTypeId() != 0))
						return new Location(getSkyBlockWorld(), loc.getBlockX()
								+ x, loc.getBlockY() + y + 1, loc.getBlockZ()
								+ z + 1);
					if ((getSkyBlockWorld().getBlockAt(loc.getBlockX() + x + 1,
							loc.getBlockY() + y, loc.getBlockZ() + z)
							.getTypeId() == 0)
							&& (getSkyBlockWorld().getBlockAt(
									loc.getBlockX() + x + 1,
									loc.getBlockY() + y - 1,
									loc.getBlockZ() + z).getTypeId() != 0))
						return new Location(getSkyBlockWorld(), loc.getBlockX()
								+ x, loc.getBlockY() + y + 1, loc.getBlockZ()
								+ z + 1);
					if ((getSkyBlockWorld().getBlockAt(loc.getBlockX() + x - 1,
							loc.getBlockY() + y, loc.getBlockZ() + z)
							.getTypeId() == 0)
							&& (getSkyBlockWorld().getBlockAt(
									loc.getBlockX() + x - 1,
									loc.getBlockY() + y - 1,
									loc.getBlockZ() + z).getTypeId() != 0))
						return new Location(getSkyBlockWorld(), loc.getBlockX()
								+ x, loc.getBlockY() + y + 1, loc.getBlockZ()
								+ z + 1);
					loc.setY(loc.getY() + 1.0D);
					return loc;
				}
			}
		}

		return loc;
	}

	private void setNewPlayerIsland(Player player, Location loc) {
		((PlayerInfo) getInstance().getActivePlayers().get(player.getName()))
				.startNewIsland(loc);
		player.teleport(getChestSpawnLoc(loc, player));
		if (getIslandConfig(((PlayerInfo) getInstance().getActivePlayers().get(
				player.getName())).locationForParty()) == null) {
			createIslandConfig(((PlayerInfo) getInstance().getActivePlayers()
					.get(player.getName())).locationForParty(),
					player.getName());
		}
		clearIslandConfig(
				((PlayerInfo) getInstance().getActivePlayers().get(
						player.getName())).locationForParty(), player.getName());
		getInstance().updatePartyNumber(player);
		getInstance().homeSet(player);
		((PlayerInfo) getInstance().getActivePlayers().get(player.getName()))
				.savePlayerConfig(player.getName());
	}

	public void setWarpLocation(String location, Location loc) {
		getIslandConfig(location).set("general.warpLocationX",
				Integer.valueOf(loc.getBlockX()));
		getIslandConfig(location).set("general.warpLocationY",
				Integer.valueOf(loc.getBlockY()));
		getIslandConfig(location).set("general.warpLocationZ",
				Integer.valueOf(loc.getBlockZ()));
		getIslandConfig(location).set("general.warpActive",
				Boolean.valueOf(true));
		saveIslandConfig(location);
	}

	public void buildIslandList() {
		File folder = getInstance().directoryPlayers;
		File[] listOfFiles = folder.listFiles();

		System.out.print(ChatColor.GREEN
				+ "[uSkyBlock] Building a new island list...");
		for (int i = 0; i < listOfFiles.length; i++) {
			PlayerInfo pi = new PlayerInfo(listOfFiles[i].getName());
			if (!pi.getHasIsland())
				continue;
			System.out.print("Creating new island file for "
					+ pi.getPlayerName());
			createIslandConfig(pi.locationForParty(), pi.getPlayerName());
			saveIslandConfig(pi.locationForParty());
		}

		for (int i = 0; i < listOfFiles.length; i++) {
			PlayerInfo pi = new PlayerInfo(listOfFiles[i].getName());
			if ((pi.getHasIsland())
					|| (pi.getPartyIslandLocation() == null)
					|| (getTempIslandConfig(pi.locationForPartyOld()) == null)
					|| (getTempIslandConfig(pi.locationForPartyOld())
							.contains("party.members." + pi.getPlayerName())))
				continue;
			setupPartyMember(pi.locationForPartyOld(), pi.getPlayerName());
			saveIslandConfig(pi.locationForParty());
		}

		System.out
				.print(ChatColor.GREEN + "[uSkyBlock] Party list completed.");
	}

	public void removeIslandConfig(String location) {
		this.islands.remove(location);
	}

	public void displayIslandConfigs() {
		Iterator<String> islandList = this.islands.keySet().iterator();
		while (islandList.hasNext()) {
			System.out.print((String) islandList.next());
		}
	}

	public void updatePartyNumber(Player player) {
		if ((getInstance().getIslandConfig(
				((PlayerInfo) getInstance().getActivePlayers().get(
						player.getName())).locationForParty()).getInt(
				"party.maxSize") < 8)
				&& (VaultHandler.checkPerk(player.getName(),
						"usb.extra.partysize", player.getWorld()))) {
			getInstance().getIslandConfig(
					((PlayerInfo) getInstance().getActivePlayers().get(
							player.getName())).locationForParty()).set(
					"party.maxSize", Integer.valueOf(8));
			getInstance().saveIslandConfig(
					((PlayerInfo) getInstance().getActivePlayers().get(
							player.getName())).locationForParty());
			return;
		}

		if ((getInstance().getIslandConfig(
				((PlayerInfo) getInstance().getActivePlayers().get(
						player.getName())).locationForParty()).getInt(
				"party.maxSize") < 7)
				&& (VaultHandler.checkPerk(player.getName(),
						"usb.extra.party3", player.getWorld()))) {
			getInstance().getIslandConfig(
					((PlayerInfo) getInstance().getActivePlayers().get(
							player.getName())).locationForParty()).set(
					"party.maxSize", Integer.valueOf(7));
			getInstance().saveIslandConfig(
					((PlayerInfo) getInstance().getActivePlayers().get(
							player.getName())).locationForParty());
			return;
		}

		if ((getInstance().getIslandConfig(
				((PlayerInfo) getInstance().getActivePlayers().get(
						player.getName())).locationForParty()).getInt(
				"party.maxSize") < 6)
				&& (VaultHandler.checkPerk(player.getName(),
						"usb.extra.party2", player.getWorld()))) {
			getInstance().getIslandConfig(
					((PlayerInfo) getInstance().getActivePlayers().get(
							player.getName())).locationForParty()).set(
					"party.maxSize", Integer.valueOf(6));
			getInstance().saveIslandConfig(
					((PlayerInfo) getInstance().getActivePlayers().get(
							player.getName())).locationForParty());
			return;
		}

		if ((getInstance().getIslandConfig(
				((PlayerInfo) getInstance().getActivePlayers().get(
						player.getName())).locationForParty()).getInt(
				"party.maxSize") >= 5)
				|| (!VaultHandler.checkPerk(player.getName(),
						"usb.extra.party1", player.getWorld())))
			return;
		getInstance().getIslandConfig(
				((PlayerInfo) getInstance().getActivePlayers().get(
						player.getName())).locationForParty()).set(
				"party.maxSize", Integer.valueOf(5));
		getInstance().saveIslandConfig(
				((PlayerInfo) getInstance().getActivePlayers().get(
						player.getName())).locationForParty());
	}

	public void changePlayerPermission(Player player, String playername,
			String perm) {
		if (!getInstance().getIslandConfig(
				((PlayerInfo) getInstance().getActivePlayers().get(
						player.getName())).locationForParty()).contains(
				"party.members." + playername + "." + perm))
			return;
		if (getInstance().getIslandConfig(
				((PlayerInfo) getInstance().getActivePlayers().get(
						player.getName())).locationForParty()).getBoolean(
				"party.members." + playername + "." + perm))
			getInstance().getIslandConfig(
					((PlayerInfo) getInstance().getActivePlayers().get(
							player.getName())).locationForParty()).set(
					"party.members." + playername + "." + perm,
					Boolean.valueOf(false));
		else
			getInstance().getIslandConfig(
					((PlayerInfo) getInstance().getActivePlayers().get(
							player.getName())).locationForParty()).set(
					"party.members." + playername + "." + perm,
					Boolean.valueOf(true));
		getInstance().saveIslandConfig(
				((PlayerInfo) getInstance().getActivePlayers().get(
						player.getName())).locationForParty());
	}

	public boolean checkForOnlineMembers(Player p) {
		Iterator<String> temp = getInstance()
				.getIslandConfig(
						((PlayerInfo) getInstance().getActivePlayers().get(
								p.getName())).locationForParty())
				.getConfigurationSection("party.members").getKeys(false)
				.iterator();
		while (temp.hasNext()) {
			String tString = (String) temp.next();
			if ((Bukkit.getPlayer(tString) != null)
					&& (!Bukkit.getPlayer(tString).getName()
							.equalsIgnoreCase(p.getName()))) {
				return true;
			}
		}
		return false;
	}

	public boolean checkCurrentBiome(Player p, String biome) {
		return getInstance()
				.getIslandConfig(
						((PlayerInfo) getInstance().getActivePlayers().get(
								p.getName())).locationForParty())
				.getString("general.biome").equalsIgnoreCase(biome);
	}

	public void setConfigBiome(Player p, String biome) {
		getInstance()
				.getIslandConfig(
						((PlayerInfo) getInstance().getActivePlayers().get(
								p.getName())).locationForParty()).set(
						"general.biome", biome);
		getInstance()
				.saveIslandConfig(
						((PlayerInfo) getInstance().getActivePlayers().get(
								p.getName())).locationForParty());
	}

	public Inventory displayPartyPlayerGUI(Player player, String pname) {
		this.GUIpartyPlayer = Bukkit.createInventory(null, 9, pname + " <권한>");
		ItemStack pHead = new ItemStack(397, 1);
		pHead.setDurability((short)3);
		SkullMeta meta3 = (SkullMeta) pHead.getItemMeta();
		ItemMeta meta4 = this.sign.getItemMeta();
		meta4.setDisplayName("§h플레이어 권한");
		this.lores.add("§a클릭시 메인화면으로 이동.");
		meta4.setLore(this.lores);
		this.sign.setItemMeta(meta4);
		this.GUIpartyPlayer.addItem(new ItemStack[] { this.sign });
		this.lores.clear();
		meta3.setDisplayName(pname + "의 권한");
		this.lores.add("§a마우스 오버시 정보 보기");
		meta3.setLore(this.lores);
		pHead.setItemMeta(meta3);
		this.GUIpartyPlayer.addItem(new ItemStack[] { pHead });
		this.lores.clear();

		meta4 = this.biome.getItemMeta();
		if (getInstance().getIslandConfig(
				((PlayerInfo) getInstance().getActivePlayers().get(
						player.getName())).locationForParty()).getBoolean(
				"party.members." + pname + ".canChangeBiome")) {
			meta4.setDisplayName("§b바이옴 수정");
			this.lores.add("§6이 플레이어는 바이옴을 §6수정할수 §c있습니다.");
		} else {
			meta4.setDisplayName("§c바이옴 수정");
			this.lores.add("§6이 플레이어는 바이옴을 §6수정할수 §c없습니다.");
		}
		meta4.setLore(this.lores);
		this.biome.setItemMeta(meta4);
		this.GUIpartyPlayer.addItem(new ItemStack[] { this.biome });
		this.lores.clear();

		meta4 = this.lock.getItemMeta();
		if (getInstance().getIslandConfig(
				((PlayerInfo) getInstance().getActivePlayers().get(
						player.getName())).locationForParty()).getBoolean(
				"party.members." + pname + ".canToggleLock")) {
			meta4.setDisplayName("§b섬 잠금");
			this.lores.add("§6이 플레이어는 잠금 상태을 §6수정할수 §c있습니다.");
		} else {
			meta4.setDisplayName("§b섬 잠금");
			this.lores.add("§6이 플레이어는 잠금 상태을 §6수정할수 §c없습니다.");
		}
		meta4.setLore(this.lores);
		this.lock.setItemMeta(meta4);
		this.GUIpartyPlayer.addItem(new ItemStack[] { this.lock });
		this.lores.clear();

		meta4 = this.warpset.getItemMeta();
		if (getInstance().getIslandConfig(
				((PlayerInfo) getInstance().getActivePlayers().get(
						player.getName())).locationForParty()).getBoolean(
				"party.members." + pname + ".canChangeWarp")) {
			meta4.setDisplayName("§b섬 워프 변경");
			this.lores.add("§6이 플레이어는 워프 수정을 §6수정할수 §c있습니다.");
		} else {
			meta4.setDisplayName("§b섬 워프 변경");
			this.lores.add("§6이 플레이어는 워프 수정을 §6수정할수 §c없습니다.");
		}
		meta4.setLore(this.lores);
		this.warpset.setItemMeta(meta4);
		this.GUIpartyPlayer.addItem(new ItemStack[] { this.warpset });
		this.lores.clear();

		meta4 = this.warptoggle.getItemMeta();
		if (getInstance().getIslandConfig(
				((PlayerInfo) getInstance().getActivePlayers().get(
						player.getName())).locationForParty()).getBoolean(
				"party.members." + pname + ".canToggleWarp")) {
			meta4.setDisplayName("§b섬 워프");
			this.lores.add("§6이 플레이어는 워프를 §6사용할수 §c있습니다.");
		} else {
			meta4.setDisplayName("§b섬 워프");
			this.lores.add("§6이 플레이어는 워프를 §6사용할수 §c없습니다.");
		}
		meta4.setLore(this.lores);
		this.warptoggle.setItemMeta(meta4);
		this.GUIpartyPlayer.addItem(new ItemStack[] { this.warptoggle });
		this.lores.clear();

		meta4 = this.invite.getItemMeta();
		if (getInstance().getIslandConfig(
				((PlayerInfo) getInstance().getActivePlayers().get(
						player.getName())).locationForParty()).getBoolean(
				"party.members." + pname + ".canInviteOthers")) {
			meta4.setDisplayName("§b플레이어 초대");
			this.lores.add("§6이 플레이어는 플레이어 초대를 §6사용할수 §c있습니다.");
		} else {
			meta4.setDisplayName("§b플레이어 초대");
			this.lores.add("§6이 플레이어는 플레이어 초대를 §6사용할수 §c없습니다.");
		}
		meta4.setLore(this.lores);
		this.invite.setItemMeta(meta4);
		this.GUIpartyPlayer.addItem(new ItemStack[] { this.invite });
		this.lores.clear();

		meta4 = this.kick.getItemMeta();
		if (getInstance().getIslandConfig(
				((PlayerInfo) getInstance().getActivePlayers().get(
						player.getName())).locationForParty()).getBoolean(
				"party.members." + pname + ".canKickOthers")) {
			meta4.setDisplayName("§b플레이어 강퇴");
			this.lores.add("§6이 플레이어는 플레이어 강퇴를 §6사용할수 §c있습니다.");
		} else {
			meta4.setDisplayName("§b플레이어 강퇴");
			this.lores.add("§6이 플레이어는 플레이어 강퇴를 §6사용할수 §c없습니다.");
		}
		meta4.setLore(this.lores);
		this.kick.setItemMeta(meta4);
		this.GUIpartyPlayer.addItem(new ItemStack[] { this.kick });
		this.lores.clear();
		return this.GUIpartyPlayer;
	}

	public Inventory displayPartyGUI(Player player) {
		this.GUIparty = Bukkit.createInventory(null, 18, "§9섬 그룹 맴버");

		Set<String> memberList = getInstance()
				.getIslandConfig(
						((PlayerInfo) getInstance().getActivePlayers().get(
								player.getName())).locationForParty())
				.getConfigurationSection("party.members").getKeys(false);
		this.tempIt = memberList.iterator();
		SkullMeta meta3 = (SkullMeta) this.pHead.getItemMeta();
		ItemMeta meta4 = this.sign.getItemMeta();
		meta4.setDisplayName("§b그룹 정보");
		this.lores.add("그룹 멤버 : §2"
				+ getInstance().getIslandConfig(
						((PlayerInfo) getInstance().getActivePlayers().get(
								player.getName())).locationForParty()).getInt(
						"party.currentSize")
				+ "§7/§a"
				+ Settings.general_maxPartySize);

		if (getInstance().getIslandConfig(
				((PlayerInfo) getInstance().getActivePlayers().get(
						player.getName())).locationForParty()).getInt(
				"party.currentSize") < Settings.general_maxPartySize)
			this.lores.add("§b섬에 플레어이를 추가할수 있습니다.");
		else
			this.lores.add("§c이 섬은 꽉 찼습니다.");
		this.lores.add("§a플레이어 아이콘에 마우스를 올리면, 펄미션을 볼수있습니다");
		meta4.setLore(this.lores);
		this.sign.setItemMeta(meta4);
		this.GUIparty.addItem(new ItemStack[] { this.sign });
		this.lores.clear();
		while (this.tempIt.hasNext()) {
			String temp = (String) this.tempIt.next();
			if (temp.equalsIgnoreCase(getInstance().getIslandConfig(
					((PlayerInfo) getInstance().getActivePlayers().get(
							player.getName())).locationForParty()).getString(
					"party.leader"))) {
				meta3.setDisplayName("§6" + temp);
				this.lores.add("§b§l리더");
				this.lores.add("§b바이옴 변경§6 권한");
				this.lores.add("§b잠금 변경§6 권한");
				this.lores.add("§b워프 지정 §6 권한");
				this.lores.add("§b워프 사용 §6 권한");
				this.lores.add("§b파티 초대 §6 권한");
				this.lores.add("§b파티 추방 §6 권한");
				meta3.setLore(this.lores);
				this.lores.clear();
			} else {
				meta3.setDisplayName("§6" + temp);
				this.lores.add("§a§lMember");
				if (getInstance()
						.getIslandConfig(
								((PlayerInfo) getInstance().getActivePlayers()
										.get(player.getName()))
										.locationForParty()).getBoolean(
								"party.members." + temp + ".canChangeBiome")) {
					this.lores.add("§b바이옴 변경§6 권한");
				} else
					this.lores.add("§c바이옴 변경§6 권한");
				if (getInstance().getIslandConfig(
						((PlayerInfo) getInstance().getActivePlayers().get(
								player.getName())).locationForParty())
						.getBoolean("party.members." + temp + ".canToggleLock")) {
					this.lores.add("§b잠금 변경§6 권한");
				} else
					this.lores.add("§c잠금 변경§6 권한");
				if (getInstance().getIslandConfig(
						((PlayerInfo) getInstance().getActivePlayers().get(
								player.getName())).locationForParty())
						.getBoolean("party.members." + temp + ".canChangeWarp")) {
					this.lores.add("§b워프 지정 §6 권한");
				} else
					this.lores.add("§c워프 지정 §6 권한");
				if (getInstance().getIslandConfig(
						((PlayerInfo) getInstance().getActivePlayers().get(
								player.getName())).locationForParty())
						.getBoolean("party.members." + temp + ".canToggleWarp"))
					this.lores.add("§b워프 사용 §6 권한");
				else
					this.lores.add("§c워프 사용 §6 권한");
				if (getInstance().getIslandConfig(
						((PlayerInfo) getInstance().getActivePlayers().get(
								player.getName())).locationForParty())
						.getBoolean(
								"party.members." + temp + ".canInviteOthers"))
					this.lores.add("§b파티 초대 §6 권한");
				else
					this.lores.add("§c파티 초대 §6 권한");
				if (getInstance().getIslandConfig(
						((PlayerInfo) getInstance().getActivePlayers().get(
								player.getName())).locationForParty())
						.getBoolean("party.members." + temp + ".canKickOthers"))
					this.lores.add("§b파티 추방 §6 권한");
				else {
					this.lores.add("§c파티 추방 §6 권한");
				}
				if (player.getName().equalsIgnoreCase(
						getInstance().getIslandConfig(
								((PlayerInfo) getInstance().getActivePlayers()
										.get(player.getName()))
										.locationForParty()).getString(
								"party.leader"))) {
					this.lores.add("§a<클릭하면 유저의 권한을 수정할수 있습니다.>");
				}
				meta3.setLore(this.lores);
				this.lores.clear();
			}
			meta3.setOwner(temp);
			this.pHead.setItemMeta(meta3);
			this.GUIparty.addItem(new ItemStack[] { this.pHead });
		}
		return this.GUIparty;
	}

	public Inventory displayLogGUI(Player player) {
		this.GUIlog = Bukkit.createInventory(null, 9, "§9섬 기록");
		ItemMeta meta4 = this.sign.getItemMeta();
		meta4.setDisplayName("§l섬 기록");
		this.lores.add("§a클릭시 메인화면으로 이동.");
		meta4.setLore(this.lores);
		this.sign.setItemMeta(meta4);
		this.GUIlog.addItem(new ItemStack[] { this.sign });
		this.lores.clear();
		this.currentLogItem = new ItemStack(Material.BOOK_AND_QUILL, 1);
		meta4 = this.currentLogItem.getItemMeta();
		meta4.setDisplayName("§a§l섬 로그");
		for (int i = 1; i <= 10; i++) {
			if (getInstance().getIslandConfig(
					((PlayerInfo) getActivePlayers().get(player.getName()))
							.locationForParty()).contains("log." + i))
				this.lores.add(getInstance().getIslandConfig(
						((PlayerInfo) getActivePlayers().get(player.getName()))
								.locationForParty()).getString("log." + i));
		}
		meta4.setLore(this.lores);
		this.currentLogItem.setItemMeta(meta4);
		this.GUIlog.setItem(8, this.currentLogItem);
		this.lores.clear();
		return this.GUIlog;
	}

	public Inventory displayBiomeGUI(Player player) {
		this.GUIbiome = Bukkit.createInventory(null, 18, "§9섬 바이옴");

		ItemMeta meta4 = this.sign.getItemMeta();
		meta4.setDisplayName("§b섬 바이옴");
		this.lores.add("§a클릭시 메인화면으로 이동.");
		meta4.setLore(this.lores);
		this.sign.setItemMeta(meta4);
		this.GUIbiome.addItem(new ItemStack[] { this.sign });
		this.lores.clear();

		this.currentBiomeItem = new ItemStack(Material.WATER, 1);
		meta4 = this.currentBiomeItem.getItemMeta();
		if (VaultHandler.checkPerk(player.getName(), "usb.biome.ocean",
				player.getWorld())) {
			meta4.setDisplayName("§b바이옴 : 대양");
			this.lores.add("§6기본적인 바이옴 이다.");
			if (checkCurrentBiome(player, "OCEAN")) {
				this.lores.add("§2§l이미 선택되어있습니다.");
			} else
				this.lores.add("§a§l클릭시 바이옴이 바뀝니다.");
		} else {
			meta4.setDisplayName("§b바이옴 : 대양");
			this.lores.add("§c당신은 이 바이옴을 사용할수 없습니다.");
		}
		meta4.setLore(this.lores);
		this.currentBiomeItem.setItemMeta(meta4);
		this.GUIbiome.addItem(new ItemStack[] { this.currentBiomeItem });
		this.lores.clear();

		this.currentBiomeItem = new ItemStack(Material.SAPLING, 1);
		currentBiomeItem.setDurability((short) 1);
		meta4 = this.currentBiomeItem.getItemMeta();
		if (VaultHandler.checkPerk(player.getName(), "usb.biome.forst",
				player.getWorld())) {
			meta4.setDisplayName("§b바이옴 : 숲");
			this.lores.add("§6숲이 있는 바이옴이다");
			if (checkCurrentBiome(player, "FOREST")) {
				this.lores.add("§2§l이미 선택되어있습니다.");
			} else
				this.lores.add("§a§l클릭시 바이옴이 바뀝니다.");
		} else {
			meta4.setDisplayName("§b바이옴 : 숲");
			this.lores.add("§c당신은 이 바이옴을 사용할수 없습니다.");
		}
		meta4.setLore(this.lores);
		this.currentBiomeItem.setItemMeta(meta4);
		this.GUIbiome.addItem(new ItemStack[] { this.currentBiomeItem });
		this.lores.clear();

		this.currentBiomeItem = new ItemStack(Material.SAND, 1);
		meta4 = this.currentBiomeItem.getItemMeta();
		if (VaultHandler.checkPerk(player.getName(), "usb.biome.desert",
				player.getWorld())) {
			meta4.setDisplayName("§b바이옴 : 사막");
			this.lores.add("§6사막이 있는 바이옴이다");
			if (checkCurrentBiome(player, "DESERT")) {
				this.lores.add("§2§l이미 선택되어있습니다.");
			} else
				this.lores.add("§a§l클릭시 바이옴이 바뀝니다.");
		} else {
			meta4.setDisplayName("§b바이옴 : 사막");
			this.lores.add("§c당신은 이 바이옴을 사용할수 없습니다.");
		}
		meta4.setLore(this.lores);
		this.currentBiomeItem.setItemMeta(meta4);
		this.GUIbiome.addItem(new ItemStack[] { this.currentBiomeItem });
		this.lores.clear();

		this.currentBiomeItem = new ItemStack(Material.SAPLING, 1);
		currentBiomeItem.setDurability((short) 3);
		meta4 = this.currentBiomeItem.getItemMeta();
		if (VaultHandler.checkPerk(player.getName(), "usb.biome.jungle",
				player.getWorld())) {
			meta4.setDisplayName("§b바이옴 : 정글");
			this.lores.add("§6정글이 있는 바이옴이다.");
			if (checkCurrentBiome(player, "JUNGLE")) {
				this.lores.add("§2§l이미 선택되어있습니다.");
			} else
				this.lores.add("§a§l클릭시 바이옴이 바뀝니다.");
		} else {
			meta4.setDisplayName("§b바이옴 : 정글");
			this.lores.add("§c당신은 이 바이옴을 사용할수 없습니다.");
		}
		meta4.setLore(this.lores);
		this.currentBiomeItem.setItemMeta(meta4);
		this.GUIbiome.addItem(new ItemStack[] { this.currentBiomeItem });
		this.lores.clear();

		this.currentBiomeItem = new ItemStack(Material.WATER_LILY, 1);
		meta4 = this.currentBiomeItem.getItemMeta();
		if (VaultHandler.checkPerk(player.getName(), "usb.biome.swampland",
				player.getWorld())) {
			meta4.setDisplayName("§b바이옴 : 습지대");
			this.lores.add("§6습한 지대가 있는 바이옴이다.");
			if (checkCurrentBiome(player, "SWAMPLAND")) {
				this.lores.add("§2§l이미 선택되어있습니다.");
			} else
				this.lores.add("§a§l클릭시 바이옴이 바뀝니다.");
		} else {
			meta4.setDisplayName("§b바이옴 : 습지대");
			this.lores.add("§c당신은 이 바이옴을 사용할수 없습니다.");
		}
		meta4.setLore(this.lores);
		this.currentBiomeItem.setItemMeta(meta4);
		this.GUIbiome.addItem(new ItemStack[] { this.currentBiomeItem });
		this.lores.clear();

		this.currentBiomeItem = new ItemStack(Material.SNOW, 1);
		meta4 = this.currentBiomeItem.getItemMeta();
		if (VaultHandler.checkPerk(player.getName(), "usb.biome.taiga",
				player.getWorld())) {
			meta4.setDisplayName("§b바이옴 : 침엽수림");
			this.lores.add("§6침엽수림인 지역이다.");
			if (checkCurrentBiome(player, "TAIGA")) {
				this.lores.add("§2§l이미 선택되어있습니다.");
			} else
				this.lores.add("§a§l클릭시 바이옴이 바뀝니다.");
		} else {
			meta4.setDisplayName("§b바이옴 : 침엽수림");
			this.lores.add("§c당신은 이 바이옴을 사용할수 없습니다.");
		}
		meta4.setLore(this.lores);
		this.currentBiomeItem.setItemMeta(meta4);
		this.GUIbiome.addItem(new ItemStack[] { this.currentBiomeItem });
		this.lores.clear();

		this.currentBiomeItem = new ItemStack(Material.RED_MUSHROOM, 1);
		meta4 = this.currentBiomeItem.getItemMeta();
		if (VaultHandler.checkPerk(player.getName(), "usb.biome.mushroom",
				player.getWorld())) {
			meta4.setDisplayName("§b바이옴 : 버섯지대");
			this.lores.add("§6버섯 바이옴.");
			if (checkCurrentBiome(player, "MUSHROOM")) {
				this.lores.add("§2§l이미 선택되어있습니다.");
			} else
				this.lores.add("§a§l클릭시 바이옴이 바뀝니다.");
		} else {
			meta4.setDisplayName("§b바이옴 : 버섯지대");
			this.lores.add("§c당신은 이 바이옴을 사용할수 없습니다.");
		}
		meta4.setLore(this.lores);
		this.currentBiomeItem.setItemMeta(meta4);
		this.GUIbiome.addItem(new ItemStack[] { this.currentBiomeItem });
		this.lores.clear();

		this.currentBiomeItem = new ItemStack(Material.FIRE, 1);
		meta4 = this.currentBiomeItem.getItemMeta();
		if (VaultHandler.checkPerk(player.getName(), "usb.biome.hell",
				player.getWorld())) {
			meta4.setDisplayName("§b바이옴 : 지옥");
			this.lores.add("§6지옥 바이옴 이다.");
			if (checkCurrentBiome(player, "HELL")) {
				this.lores.add("§2§l이미 선택되어있습니다.");
			} else
				this.lores.add("§a§l클릭시 바이옴이 바뀝니다.");
		} else {
			meta4.setDisplayName("§b바이옴 : 지옥");
			this.lores.add("§c당신은 이 바이옴을 사용할수 없습니다.");
		}
		meta4.setLore(this.lores);
		this.currentBiomeItem.setItemMeta(meta4);
		this.GUIbiome.addItem(new ItemStack[] { this.currentBiomeItem });
		this.lores.clear();

		this.currentBiomeItem = new ItemStack(Material.EYE_OF_ENDER, 1);
		meta4 = this.currentBiomeItem.getItemMeta();
		if (VaultHandler.checkPerk(player.getName(), "usb.biome.sky",
				player.getWorld())) {
			meta4.setDisplayName("§b바이옴 : 하늘");
			this.lores.add("§6하늘의 바이옴");
			if (checkCurrentBiome(player, "SKY")) {
				this.lores.add("§2§l이미 선택되어있습니다.");
			} else
				this.lores.add("§a§l클릭시 바이옴이 바뀝니다.");
		} else {
			meta4.setDisplayName("§b바이옴 : 하늘");
			this.lores.add("§c당신은 이 바이옴을 사용할수 없습니다.");
		}
		meta4.setLore(this.lores);
		this.currentBiomeItem.setItemMeta(meta4);
		this.GUIbiome.addItem(new ItemStack[] { this.currentBiomeItem });
		this.lores.clear();
		return this.GUIbiome;
	}

	public Inventory displayChallengeGUI(Player player) {
		this.GUIchallenge = Bukkit.createInventory(null, 36, "§9도전과제 메뉴");
		PlayerInfo pi = (PlayerInfo) getInstance().getActivePlayers().get(
				player.getName());
		populateChallengeRank(player, 0, Material.DIRT, 0, pi);
		populateChallengeRank(player, 1, Material.IRON_BLOCK, 9, pi);
		populateChallengeRank(player, 2, Material.GOLD_BLOCK, 18, pi);
		populateChallengeRank(player, 3, Material.DIAMOND_BLOCK, 27, pi);
		return this.GUIchallenge;
	}

	public Inventory displayIslandGUI(Player player) {
		this.GUIisland = Bukkit.createInventory(null, 9, "§9섬 메뉴");
		if (hasIsland(player.getName())) {
			this.currentIslandItem = new ItemStack(Material.ENDER_PORTAL, 1);
			ItemMeta meta4 = this.currentIslandItem.getItemMeta();
			meta4.setDisplayName("§b§l집으로 돌아가기");
			this.lores.add("§6당신의 섬의 홈으로 텔레포트 합니다.");
			this.lores.add("§6섬의 홈은 자유롭게 변경 가능합니다.");
			this.lores.add("§6홈 설정은 해당 명령어를 참조하세요. §b/섬");
			this.lores.add("§a§l클릭시 텔레포트 합니다.");
			meta4.setLore(this.lores);
			this.currentIslandItem.setItemMeta(meta4);
			this.GUIisland.addItem(new ItemStack[] { this.currentIslandItem });
			this.lores.clear();
			
			this.currentIslandItem = new ItemStack(Material.EXP_BOTTLE, 1);
			meta4 = this.currentIslandItem.getItemMeta();
			meta4.setDisplayName("§b§l섬 레벨");
			this.lores.add("§a현재 레벨 : §b" + showIslandLevel(player));
			this.lores.add("§6섬의 크기를 확장하면 레벨이 오릅니다!");
			this.lores.add("§6섬의 도전과제를 성공하면 레벨이 오릅니다!");
			this.lores.add("§a§l클릭시 새로고침.");
			this.lores.add("§a§l당신의 섬에서 작동합니다.");
			meta4.setLore(this.lores);
			this.currentIslandItem.setItemMeta(meta4);
			this.GUIisland.addItem(new ItemStack[] { this.currentIslandItem });
			this.lores.clear();

			this.currentIslandItem = new ItemStack(Material.SKULL_ITEM, 1);
					currentIslandItem.setDurability((short) 3);
			SkullMeta meta3 = (SkullMeta) this.currentIslandItem.getItemMeta();
			meta3.setDisplayName("§b§l섬 그룹");
			this.lores.add("§a맴버 : §2" + showCurrentMembers(player) + "/"
					+ showMaxMembers(player));
			this.lores.add("§6당신의 섬의 구성원들을 볼 수있습니다.");
			this.lores.add("§6권한이 있다면 그룹을 설정할수 있습니다.");
			this.lores.add("§a§l클릭시 보거나 설정.");
			meta3.setLore(this.lores);
			this.currentIslandItem.setItemMeta(meta3);
			this.GUIisland.addItem(new ItemStack[] { this.currentIslandItem });
			this.lores.clear();

			this.currentIslandItem = new ItemStack(Material.SAPLING, 1);
			currentIslandItem.setDurability((short) 3);
			meta4 = this.currentIslandItem.getItemMeta();
			meta4.setDisplayName("§b§l섬 바이옴 변경");
			this.lores.add("§a현재 바이옴 : §b"
					+ getCurrentBiome(player).toUpperCase());
			this.lores.add("§6바이옴은 섬의 생물 작용 및 기후에 영향을 줍니다.");
			this.lores.add("§6잔디의 색상에도 영향을 줍니다.");
			if (checkIslandPermission(player, "canChangeBiome"))
				this.lores.add("§a§l클릭시 바이옴 설정");
			else
				this.lores.add("§c§l당신은 바이옴을 설정할수 없습니다.");
			meta4.setLore(this.lores);
			this.currentIslandItem.setItemMeta(meta4);
			this.GUIisland.addItem(new ItemStack[] { this.currentIslandItem });
			this.lores.clear();

			this.currentIslandItem = new ItemStack(Material.IRON_FENCE, 1);
			meta4 = this.currentIslandItem.getItemMeta();
			meta4.setDisplayName("§b§l섬 잠금/해제");
			if (getInstance().getIslandConfig(
					((PlayerInfo) getInstance().getActivePlayers().get(
							player.getName())).locationForParty()).getBoolean(
					"general.locked")) {
				this.lores.add("§a잠금 상태 : §b활성화");
				this.lores.add("§6당신의 섬을 잠금 해제 할 수 있습니다.");
				if (checkIslandPermission(player, "canToggleLock"))
					this.lores.add("§a§l클릭시 섬 잠금 해제.");
				else
					this.lores.add("§c§l당신은 섬을 잠금 해제 할 수 없습니다.");
			} else {
				this.lores.add("§a잠금 상태 : §b비활성화");
				this.lores.add("§6당신의 섬을 잠금 할 수 있습니다.");
				if (checkIslandPermission(player, "canToggleLock"))
					this.lores.add("§a§l클릭시 섬 잠금.");
				else
					this.lores.add("§c§l당신은 섬을 잠금 할 수 없습니다.");
			}
			meta4.setLore(this.lores);
			this.currentIslandItem.setItemMeta(meta4);
			this.GUIisland.addItem(new ItemStack[] { this.currentIslandItem });
			this.lores.clear();

			if (getInstance().getIslandConfig(
					((PlayerInfo) getInstance().getActivePlayers().get(
							player.getName())).locationForParty()).getBoolean(
					"general.warpActive")) {
				this.currentIslandItem = new ItemStack(Material.PORTAL, 1);
				meta4 = this.currentIslandItem.getItemMeta();
				meta4.setDisplayName("§b§l섬 워프");
				this.lores.add("§a워프 상태 : §b활성화");
				this.lores.add("§6당신의 섬의 워프기능을 비활성화 합니다.");
				if ((checkIslandPermission(player, "canToggleWarp"))
						&& (VaultHandler.checkPerk(player.getName(),
								"usb.extra.addwarp", getSkyBlockWorld())))
					this.lores.add("§a§l클릭시 워프 잠금.");
				else
					this.lores.add("§c§l당신은 워프를 잠금 할 수 없습니다.");
			} else {
				this.currentIslandItem = new ItemStack(Material.ENDER_STONE, 1);
				meta4 = this.currentIslandItem.getItemMeta();
				meta4.setDisplayName("§b§l섬 워프");
				this.lores.add("§a워프 상태 : §b비활성화");
				this.lores.add("§6당신의 섬의 워프기능을 활성화 합니다.");
				if ((checkIslandPermission(player, "canToggleWarp"))
						&& (VaultHandler.checkPerk(player.getName(),
								"usb.extra.addwarp", getSkyBlockWorld())))
					this.lores.add("§a§l클릭시 워프 열기.");
				else {
					this.lores.add("§c§l당신은 워프를 활성화 할 수 없습니다.");
				}
			}
			meta4.setLore(this.lores);
			this.currentIslandItem.setItemMeta(meta4);
			this.GUIisland.addItem(new ItemStack[] { this.currentIslandItem });
			this.lores.clear();

			this.currentIslandItem = new ItemStack(Material.BOOK_AND_QUILL, 1);
			meta4 = this.currentIslandItem.getItemMeta();
			meta4.setDisplayName("§b§l섬 기록");
			this.lores.add("§6섬의 기록을 볼수 있습니다.");
			this.lores.add("§a§l클릭시 로그 보기");
			meta4.setLore(this.lores);
			this.currentIslandItem.setItemMeta(meta4);
			this.GUIisland.addItem(new ItemStack[] { this.currentIslandItem });
			this.lores.clear();

			this.currentIslandItem = new ItemStack(Material.BED, 1);
			meta4 = this.currentIslandItem.getItemMeta();
			meta4.setDisplayName("§b§l섬 홈 변경");
			this.lores.add("§6섬의 홈 위치를 변경할수 있습니다.");
			this.lores.add("§a§l클릭시 위치를 이곳으로 변경");
			meta4.setLore(this.lores);
			this.currentIslandItem.setItemMeta(meta4);
			this.GUIisland.addItem(new ItemStack[] { this.currentIslandItem });
			this.lores.clear();

			this.currentIslandItem = new ItemStack(Material.HOPPER, 1);
			meta4 = this.currentIslandItem.getItemMeta();
			meta4.setDisplayName("§b§l섬 워프 변경");
			this.lores.add("§6섬의 워프 위치를 변경할수 있습니다.");
			this.lores.add("§a§l클릭시 위치를 이곳으로 변경");
			meta4.setLore(this.lores);
			this.currentIslandItem.setItemMeta(meta4);
			this.GUIisland.addItem(new ItemStack[] { this.currentIslandItem });
			this.lores.clear();
		} else /*if (VaultHandler.checkPerk(player.getName(), "group.member",
				getSkyBlockWorld())) {
			this.currentIslandItem = new ItemStack(Material.GRASS, 1);
			ItemMeta meta4 = this.currentIslandItem.getItemMeta();
			meta4.setDisplayName("§b§l섬 시작");
			this.lores.add("§a§l클릭시 섬이 생성되고, 시작됩니다.");
			meta4.setLore(this.lores);
			this.currentIslandItem.setItemMeta(meta4);
			this.GUIisland.addItem(new ItemStack[] { this.currentIslandItem });
			this.lores.clear();

			this.currentIslandItem = new ItemStack(Material.SKULL_ITEM, 1);
			currentIslandItem.setDurability((short) 3);
			SkullMeta meta3 = (SkullMeta) this.currentIslandItem.getItemMeta();
			meta3.setDisplayName("§b§l섬 수락하기");
			this.lores.add("§a§l클릭시 초대를 수락합니다.");
			meta3.setLore(this.lores);
			this.currentIslandItem.setItemMeta(meta3);
			this.GUIisland.setItem(4, this.currentIslandItem);
			this.lores.clear();
		} else */{
			this.currentIslandItem = new ItemStack(Material.BOOK, 1);
			ItemMeta meta4 = this.currentIslandItem.getItemMeta();
			meta4.setDisplayName("§d[ §6" + player.getName() + "님 반갑습니다! §d]");
			this.lores.add("§a이 버튼을 클릭할시 섬이 자동으로 생성됩니다.");
			this.lores.add("§a인벤토리는 초기화되지 않습니다.");
			this.lores.add("§b즐거운 플레이 하시길 바랍니다!");
			//this.lores.add("§e───[ §d칭§b트롤§a팜 §e]───");
			meta4.setLore(this.lores);
			this.currentIslandItem.setItemMeta(meta4);
			this.GUIisland.setItem(4, this.currentIslandItem);
			this.lores.clear();
		}

		return this.GUIisland;
	}

	public boolean isPartyLeader(Player player) {
		return getInstance()
				.getIslandConfig(
						((PlayerInfo) getInstance().getActivePlayers().get(
								player.getName())).locationForParty())
				.getString("party.leader").equalsIgnoreCase(player.getName());
	}

	public boolean checkIslandPermission(Player player, String permission) {
		return getInstance().getIslandConfig(
				((PlayerInfo) getInstance().getActivePlayers().get(
						player.getName())).locationForParty()).getBoolean(
				"party.members." + player.getName() + "." + permission);
	}

	public String getCurrentBiome(Player player) {
		return getInstance().getIslandConfig(
				((PlayerInfo) getInstance().getActivePlayers().get(
						player.getName())).locationForParty()).getString(
				"general.biome");
	}

	public int showIslandLevel(Player player) {
		return getInstance().getIslandConfig(
				((PlayerInfo) getInstance().getActivePlayers().get(
						player.getName())).locationForParty()).getInt(
				"general.level");
	}

	public int showCurrentMembers(Player player) {
		return getInstance().getIslandConfig(
				((PlayerInfo) getInstance().getActivePlayers().get(
						player.getName())).locationForParty()).getInt(
				"party.currentSize");
	}

	public int showMaxMembers(Player player) {
		return Settings.general_maxPartySize;
	}

	public void populateChallengeRank(Player player, int rankIndex,
			Material mat, int location, PlayerInfo pi) {
		int rankComplete = 0;
		this.currentChallengeItem = new ItemStack(mat, 1);
		ItemMeta meta4 = this.currentChallengeItem.getItemMeta();
		meta4.setDisplayName("§a§l랭크: " + Settings.challenges_ranks[rankIndex]);
		this.lores.add("§6다음 단계의 랭크를 해제할려면, 이 랭크를 먼저 해제하십시오.");
		meta4.setLore(this.lores);
		this.currentChallengeItem.setItemMeta(meta4);
		this.GUIchallenge.setItem(location, this.currentChallengeItem);
		this.lores.clear();
		String[] challengeList = getChallengesFromRank(player,
				Settings.challenges_ranks[rankIndex]).split(" - ");
		for (int i = 0; i < challengeList.length; i++) {
			if (rankIndex > 0) {
				rankComplete = getInstance().checkRankCompletion(player,
						Settings.challenges_ranks[(rankIndex - 1)]);
				if (rankComplete <= 0)
					continue;
				this.currentChallengeItem = new ItemStack(Material.GLASS, 1);
				currentChallengeItem.setDurability((short) 14);
				meta4 = this.currentChallengeItem.getItemMeta();
				meta4.setDisplayName("§4§l잠긴 도전과제");
				this.lores.add("§7" + rankComplete + " 개의 "
						+ Settings.challenges_ranks[(rankIndex - 1)]
						+ " 도전과제를 성공시");
				this.lores.add("§7 랭크가 잠금 해제됩니다.");
				meta4.setLore(this.lores);
				this.currentChallengeItem.setItemMeta(meta4);
				location++;
				this.GUIchallenge.setItem(location, this.currentChallengeItem);
				this.lores.clear();
			} else {
				if (challengeList[i].charAt(1) == 'e') {
					this.currentChallengeItem = new ItemStack(Material.GLASS,
							1);
					currentChallengeItem.setDurability((short) 4);
					meta4 = this.currentChallengeItem.getItemMeta();
					meta4.setDisplayName(challengeList[i].replace("§a", "§a§l"));
					challengeList[i] = challengeList[i].replace("§a", "");
					challengeList[i] = challengeList[i].replace("§8", "");
				} else if (challengeList[i].charAt(1) == 'a') {
					if (!getInstance().getConfig().contains(
							"options.challenges.challengeList."
									+ challengeList[i].replace("§b", "")
											.replace("§2", "")
											.replace("§a", "")
											.replace("§8", "").toLowerCase()
									+ ".displayItem")){
						this.currentChallengeItem = new ItemStack(
								Material.GLASS, 1);
						currentChallengeItem.setDurability((short) 5);
					}else
						this.currentChallengeItem = new ItemStack(
								Material.getMaterial(getInstance()
										.getConfig()
										.getInt("options.challenges.challengeList."
												+ challengeList[i]
														.replace("§b", "")
														.replace("§2", "")
														.replace("§a", "")
														.replace("§8", "")
														.toLowerCase()
												+ ".displayItem")), 1);
					meta4 = this.currentChallengeItem.getItemMeta();
					meta4.setDisplayName(challengeList[i].replace("§b", "§b§l"));
					challengeList[i] = challengeList[i].replace("§b", "");
					challengeList[i] = challengeList[i].replace("§8", "");
				} else if (challengeList[i].charAt(1) == '2') {
					this.currentChallengeItem = new ItemStack(Material.GLASS,
							1);
					currentChallengeItem.setDurability((short) 13);
					meta4 = this.currentChallengeItem.getItemMeta();
					meta4.setDisplayName(challengeList[i].replace("§2", "§2§l"));
					challengeList[i] = challengeList[i].replace("§2", "");
					challengeList[i] = challengeList[i].replace("§8", "");
				} else {
					this.currentChallengeItem = new ItemStack(Material.GLASS,
							1);
					currentChallengeItem.setDurability((short) 4);
					meta4 = this.currentChallengeItem.getItemMeta();
					meta4.setDisplayName(challengeList[i].replace("§a", "§a§l"));
					challengeList[i] = challengeList[i].replace("§a", "");
					challengeList[i] = challengeList[i].replace("§8", "");
				}

				this.lores.add("§7"
						+ getInstance().getConfig().getString(
								new StringBuilder(
										"options.challenges.challengeList.")
										.append(challengeList[i].toLowerCase())
										.append(".description").toString()));
				this.lores.add("§a이 과제를 해결하려면 다음 항목들이 필요합니다");
				String[] reqList = getConfig().getString(
						"options.challenges.challengeList."
								+ challengeList[i].toLowerCase()
								+ ".requiredItems").split(" ");

				int reqItem = 0;
				int reqAmount = 0;
				int reqMod = -1;
				for (String s : reqList) {
					String[] sPart = s.split(":");
					if (sPart.length == 2) {
						reqItem = Integer.parseInt(sPart[0]);
						String[] sScale = sPart[1].split(";");
						if (sScale.length == 1)
							reqAmount = Integer.parseInt(sPart[1]);
						else if (sScale.length == 2) {
							if (sScale[1].charAt(0) == '+') {
								reqAmount = Integer.parseInt(sScale[0])
										+ Integer.parseInt(sScale[1]
												.substring(1))
										* ((PlayerInfo) getInstance()
												.getActivePlayers().get(
														player.getName()))
												.checkChallengeSinceTimer(challengeList[i]
														.toLowerCase());
							} else if (sScale[1].charAt(0) == '*') {
								reqAmount = Integer.parseInt(sScale[0])
										* Integer.parseInt(sScale[1]
												.substring(1))
										* ((PlayerInfo) getInstance()
												.getActivePlayers().get(
														player.getName()))
												.checkChallengeSinceTimer(challengeList[i]
														.toLowerCase());
							} else if (sScale[1].charAt(0) == '-') {
								reqAmount = Integer.parseInt(sScale[0])
										- Integer.parseInt(sScale[1]
												.substring(1))
										* ((PlayerInfo) getInstance()
												.getActivePlayers().get(
														player.getName()))
												.checkChallengeSinceTimer(challengeList[i]
														.toLowerCase());
							} else if (sScale[1].charAt(0) == '/') {
								reqAmount = Integer.parseInt(sScale[0])
										/ Integer.parseInt(sScale[1]
												.substring(1))
										* ((PlayerInfo) getInstance()
												.getActivePlayers().get(
														player.getName()))
												.checkChallengeSinceTimer(challengeList[i]
														.toLowerCase());
							}
						}
					} else if (sPart.length == 3) {
						reqItem = Integer.parseInt(sPart[0]);
						String[] sScale = sPart[2].split(";");
						if (sScale.length == 1)
							reqAmount = Integer.parseInt(sPart[2]);
						else if (sScale.length == 2) {
							if (sScale[1].charAt(0) == '+') {
								reqAmount = Integer.parseInt(sScale[0])
										+ Integer.parseInt(sScale[1]
												.substring(1))
										* ((PlayerInfo) getInstance()
												.getActivePlayers().get(
														player.getName()))
												.checkChallengeSinceTimer(challengeList[i]
														.toLowerCase());
							} else if (sScale[1].charAt(0) == '*') {
								reqAmount = Integer.parseInt(sScale[0])
										* Integer.parseInt(sScale[1]
												.substring(1))
										* ((PlayerInfo) getInstance()
												.getActivePlayers().get(
														player.getName()))
												.checkChallengeSinceTimer(challengeList[i]
														.toLowerCase());
							} else if (sScale[1].charAt(0) == '-') {
								reqAmount = Integer.parseInt(sScale[0])
										- Integer.parseInt(sScale[1]
												.substring(1))
										* ((PlayerInfo) getInstance()
												.getActivePlayers().get(
														player.getName()))
												.checkChallengeSinceTimer(challengeList[i]
														.toLowerCase());
							} else if (sScale[1].charAt(0) == '/') {
								reqAmount = Integer.parseInt(sScale[0])
										/ Integer.parseInt(sScale[1]
												.substring(1))
										* ((PlayerInfo) getInstance()
												.getActivePlayers().get(
														player.getName()))
												.checkChallengeSinceTimer(challengeList[i]
														.toLowerCase());
							}
						}
						reqMod = Integer.parseInt(sPart[1]);
					}
					ItemStack newItem = new ItemStack(reqItem, reqAmount,
							(short) reqMod);
					this.lores.add("§6" + newItem.getAmount() + " "
							+ newItem.getType().toString());
				}

				if ((pi.checkChallenge(challengeList[i].toLowerCase()) > 0)
						&& (getInstance().getConfig()
								.getBoolean("options.challenges.challengeList."
										+ challengeList[i].toLowerCase()
										+ ".repeatable"))) {
					if (pi.onChallengeCooldown(challengeList[i].toLowerCase())) {
						if (pi.getChallengeCooldownTime(challengeList[i]
								.toLowerCase()) / 86400000L >= 1L) {
							int days = (int) pi
									.getChallengeCooldownTime(challengeList[i]
											.toLowerCase()) / 86400000;
							this.lores.add("§4Requirements will reset in "
									+ days + " days.");
						} else {
							int hours = (int) pi
									.getChallengeCooldownTime(challengeList[i]
											.toLowerCase()) / 3600000;
							this.lores.add("§4Requirements will reset in "
									+ hours + " hours.");
						}
					}
					this.lores
							.add("§6아이템 보수 : §b"
									+ getInstance()
											.getConfig()
											.getString(
													new StringBuilder(
															"options.challenges.challengeList.")
															.append(challengeList[i]
																	.toLowerCase())
															.append(".repeatRewardText")
															.toString()));
					this.lores
							.add("§6금액 보수 : §b"
									+ getInstance()
											.getConfig()
											.getInt(new StringBuilder(
													"options.challenges.challengeList.")
													.append(challengeList[i]
															.toLowerCase())
													.append(".repeatCurrencyReward")
													.toString()));
					this.lores
							.add("§6경험치 보수 : §b"
									+ getInstance()
											.getConfig()
											.getInt(new StringBuilder(
													"options.challenges.challengeList.")
													.append(challengeList[i]
															.toLowerCase())
													.append(".repeatXpReward")
													.toString()));
					this.lores.add("§dTotal times completed: §6"
							+ pi.getChallenge(challengeList[i].toLowerCase())
									.getTimesCompleted());
					this.lores.add("§a§lClick to complete this challenge.");
				} else {
					this.lores
							.add("§6아이템 보수 : §b"
									+ getInstance()
											.getConfig()
											.getString(
													new StringBuilder(
															"options.challenges.challengeList.")
															.append(challengeList[i]
																	.toLowerCase())
															.append(".rewardText")
															.toString()));
					this.lores
							.add("§6금액 보수 : §b"
									+ getInstance()
											.getConfig()
											.getInt(new StringBuilder(
													"options.challenges.challengeList.")
													.append(challengeList[i]
															.toLowerCase())
													.append(".currencyReward")
													.toString()));
					this.lores
							.add("§6경험치 보수 : §b"
									+ getInstance()
											.getConfig()
											.getInt(new StringBuilder(
													"options.challenges.challengeList.")
													.append(challengeList[i]
															.toLowerCase())
													.append(".xpReward")
													.toString()));
					if (getInstance().getConfig().getBoolean(
							"options.challenges.challengeList."
									+ challengeList[i].toLowerCase()
									+ ".repeatable"))
						this.lores.add("§a§l이 도전을 완료하세요!");
					else
						this.lores.add("§4§l이 과제는 반복할수 없습니다.");
				}
				meta4.setLore(this.lores);
				this.currentChallengeItem.setItemMeta(meta4);
				location++;
				this.GUIchallenge.setItem(location, this.currentChallengeItem);
				this.lores.clear();
			}
		}
	}

	public void sendMessageToIslandGroup(String location, String message) {
		Iterator<String> temp = getInstance().getIslandConfig(location)
				.getConfigurationSection("party.members").getKeys(false)
				.iterator();

		this.date = new Date();
		String myDateString = DateFormat.getDateInstance(3).format(this.date)
				.toString();
		String dateTxt = myDateString;
		int currentLogPos = getInstance().getIslandConfig(location).getInt(
				"log.logPos");
		while (temp.hasNext()) {
			String player = (String) temp.next();
			if (Bukkit.getPlayer(player) == null)
				continue;
			Bukkit.getPlayer(player).sendMessage("§d[섬] " + message);
		}

		currentLogPos++;
		getInstance().getIslandConfig(location).set("log." + currentLogPos,
				"§d[" + dateTxt + "] " + message);
		if (currentLogPos < 10)
			getInstance().getIslandConfig(location).set("log.logPos",
					Integer.valueOf(currentLogPos));
		else
			getInstance().getIslandConfig(location).set("log.logPos",
					Integer.valueOf(0));
	}
}