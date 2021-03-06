package us.talabrek.ultimateskyblock;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Settings {
	public static int general_maxPartySize;
	public static String general_worldName;
	public static int island_distance;
	public static int[] blockList = new int[256];
	public static int[] limitList = new int[256];
	public static int[] diminishingReturnsList = new int[256];
	public static int island_height;
	public static int general_spawnSize;
	public static boolean island_removeCreaturesByTeleport;
	public static boolean island_protectWithWorldGuard;
	public static int island_protectionRange;
	public static String island_allowPvP;
	public static ItemStack[] island_chestItems;
	public static boolean island_addExtraItems;
	public static String[] island_extraPermissions;
	public static boolean island_useOldIslands;
	public static boolean island_allowIslandLock;
	public static boolean island_useIslandLevel;
	public static boolean island_useTopTen;
	public static int island_listTime;
	public static int general_cooldownInfo;
	public static int general_cooldownRestart;
	public static int general_biomeChange;
	public static boolean extras_sendToSpawn;
	public static boolean extras_obsidianToLava;
	public static String island_schematicName;
	public static boolean challenges_broadcastCompletion;
	public static String challenges_broadcastText;
	public static String[] challenges_ranks;
	public static boolean challenges_requirePreviousRank;
	public static int challenges_rankLeeway;
	public static String challenges_challengeColor;
	public static String challenges_finishedColor;
	public static String challenges_repeatableColor;
	public static boolean challenges_enableEconomyPlugin;
	public static boolean challenges_allowChallenges;
	public static Set<String> challenges_challengeList;
	public static Material[] itemList = new Material[2000];
}