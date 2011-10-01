package me.ahniolator.plugins.sunburn;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

public class Sunburn extends JavaPlugin {

    private String dir = "plugins/SunBurn/";
    private String confDir = "sunburn.";
    private BurningConfig config;
    private BurningPlayerListener playerListener;
    private Configuration yml;
    private int counter = 0, armorTime = 5;
    private List<String> enabledWorlds = new ArrayList<String>();
    private static List<LivingEntity> creatures;
    private short currentHelm, currentBody, currentLegs, currentFeet, maxHelm, maxBody, maxLegs, maxFeet;
    private String stringRadius;
    private int radius = 0;
    private int xv = 1;
    private int zv = 1;
    private static double scriptVersion;
    private static boolean tellUpdate;
    private static String changes, currentVerUrl = "http://ahniolator.aisites.com/SBversion.txt";

    public void onDisable() {
        config.save();
        System.out.println("[SunBurn] has been disabled!");
    }

    public void onEnable() {
        this.config = new BurningConfig(this.dir);
        tellUpdate = this.config.yml.getBoolean(confDir + "update.notifications", true);
        this.playerListener = new BurningPlayerListener(this, tellUpdate, this.config);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_JOIN, this.playerListener, Priority.Low, this);
        boolean armorOn = config.yml.getBoolean("sunburn.protective armor.enable", true);
        if (armorOn) {
            String armorType = config.yml.getString("sunburn.protective armor.type");
            if (armorType.equalsIgnoreCase("gold")) {
                System.out.println("[SunBurn] Protective Armor is ENABLED. Armor type is GOLD");
            } else if (armorType.equalsIgnoreCase("iron")) {
                System.out.println("[SunBurn] Protective Armor is ENABLED. Armor type is IRON");
            } else if (armorType.equalsIgnoreCase("diamond")) {
                System.out.println("[SunBurn] Protective Armor is ENABLED. Armor type is DIAMOND");
            } else if (armorType.equalsIgnoreCase("leather")) {
                System.out.println("[SunBurn] Protective Armor is ENABLED. Armor type is LEATHER");
            } else if (armorType.equalsIgnoreCase("chain") || armorType.equalsIgnoreCase("chainmail")) {
                System.out.println("[SunBurn] Protective Armor is ENABLED. Armor type is CHAINMAIL");
            } else {
                System.out.println("[SunBurn] Armor enabled, but invalid armor type specified. Disabling armor.");
                config.yml.setProperty(confDir + "protective armor.enable", false);
                config.reload();
            }
        }
        getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {

            public void run() {
                for (Player player : getServer().getOnlinePlayers()) {
                    World world = player.getWorld();
                    String worldName = world.getName();
                    List<String> enabledWorlds = config.yml.getStringList("sunburn.enabled worlds", null);
                    if (!enabledWorlds.contains(worldName) || enabledWorlds == null) {
                        continue;
                    }

                    boolean burnCreatures = config.yml.getBoolean("sunburn.burn.mobs", true);
                    if (burnCreatures) {
                        creatures = world.getLivingEntities();
                        for (LivingEntity m : creatures) {
                            if (!isPlayer(m)) {
                                Block creatureBlock = m.getLocation().getBlock();
                                if (creatureBlock.getLightLevel() > 14 && m.getFireTicks() <= 0) {
                                    m.setFireTicks(99999999);
                                    continue;
                                } else if (creatureBlock.getLightLevel() <= 14 && m.getFireTicks() >= 100000) {
                                    m.setFireTicks(-20);
                                    continue;
                                }
                            }
                        }
                    }

                    boolean burnPlayers = config.yml.getBoolean("sunburn.burn.players", true);
                    boolean armorOn = config.yml.getBoolean("sunburn.protective armor.enable", true);
                    boolean armorDamageOn = config.yml.getBoolean("sunburn.protective armor.damage", true);
                    if (burnPlayers && !armorOn) {
                        Block block = player.getLocation().getBlock();
                        if (block.getLightLevel() > 14 && player.getFireTicks() <= 0) {
                            player.setFireTicks(99999999);
                            continue;
                        } else if (block.getLightLevel() <= 14 && player.getFireTicks() >= 100000) {
                            player.setFireTicks(-20);
                            continue;
                        }
                    } else if (burnPlayers && armorOn && !armorDamageOn) {
                        Block block = player.getLocation().getBlock();
                        String armorType = config.yml.getString("sunburn.protective armor.type");
                        int armorHead,
                                armorBody,
                                armorLegs,
                                armorFeet;
                        if (armorType.equalsIgnoreCase("gold")) {
                            armorHead = 314;
                            armorBody = 315;
                            armorLegs = 316;
                            armorFeet = 317;
                        } else if (armorType.equalsIgnoreCase("iron")) {
                            armorHead = 306;
                            armorBody = 307;
                            armorLegs = 308;
                            armorFeet = 309;
                        } else if (armorType.equalsIgnoreCase("diamond")) {
                            armorHead = 310;
                            armorBody = 311;
                            armorLegs = 312;
                            armorFeet = 313;
                        } else if (armorType.equalsIgnoreCase("leather")) {
                            armorHead = 298;
                            armorBody = 299;
                            armorLegs = 300;
                            armorFeet = 301;
                        } else if (armorType.equalsIgnoreCase("chain") || armorType.equalsIgnoreCase("chainmail")) {
                            armorHead = 302;
                            armorBody = 303;
                            armorLegs = 304;
                            armorFeet = 305;
                        } else {
                            System.out.println("[SunBurn] Armor enabled, but invalid armor type specified. Disabling armor.");
                            config.yml.setProperty(confDir + "protective armor.enable", false);
                            config.reload();
                            continue;
                        }
                        int playerHelmID,
                                playerBodyID,
                                playerLegsID,
                                playerFeetID;
                        PlayerInventory inv = player.getInventory();
                        playerHelmID = inv.getHelmet().getTypeId();
                        playerLegsID = inv.getLeggings().getTypeId();
                        playerBodyID = inv.getChestplate().getTypeId();
                        playerFeetID = inv.getBoots().getTypeId();
                        if (block.getLightLevel() > 14 && player.getFireTicks() <= 0 && !(playerHelmID == armorHead && playerBodyID == armorBody && playerLegsID == armorLegs && playerFeetID == armorFeet)) {
                            player.setFireTicks(99999999);
                            continue;
                        } else if (block.getLightLevel() <= 14 && player.getFireTicks() >= 100000) {
                            player.setFireTicks(-20);
                            continue;
                        }
                    } else if (burnPlayers && armorOn && armorDamageOn) {
                        Block block = player.getLocation().getBlock();
                        String armorType = config.yml.getString("sunburn.protective armor.type");
                        int armorHead,
                                armorBody,
                                armorLegs,
                                armorFeet;
                        if (armorType.equalsIgnoreCase("gold")) {
                            armorHead = 314;
                            armorBody = 315;
                            armorLegs = 316;
                            armorFeet = 317;
                            maxHelm = (short) (68);
                            maxBody = (short) (96);
                            maxLegs = (short) (92);
                            maxFeet = (short) (80);
                        } else if (armorType.equalsIgnoreCase("iron")) {
                            armorHead = 306;
                            armorBody = 307;
                            armorLegs = 308;
                            armorFeet = 309;
                            maxHelm = (short) (136);
                            maxBody = (short) (192);
                            maxLegs = (short) (184);
                            maxFeet = (short) (160);
                        } else if (armorType.equalsIgnoreCase("diamond")) {
                            armorHead = 310;
                            armorBody = 311;
                            armorLegs = 312;
                            armorFeet = 313;
                            maxHelm = (short) (272);
                            maxBody = (short) (384);
                            maxLegs = (short) (368);
                            maxFeet = (short) (320);
                        } else if (armorType.equalsIgnoreCase("leather")) {
                            armorHead = 298;
                            armorBody = 299;
                            armorLegs = 300;
                            armorFeet = 301;
                            maxHelm = (short) (34);
                            maxBody = (short) (49);
                            maxLegs = (short) (46);
                            maxFeet = (short) (40);
                        } else if (armorType.equalsIgnoreCase("chain") || armorType.equalsIgnoreCase("chainmail")) {
                            armorHead = 302;
                            armorBody = 303;
                            armorLegs = 304;
                            armorFeet = 305;
                            maxHelm = (short) (68);
                            maxBody = (short) (96);
                            maxLegs = (short) (92);
                            maxFeet = (short) (80);
                        } else {
                            System.out.println("[SunBurn] Armor enabled, but invalid armor type specified. Disabling armor.");
                            config.yml.setProperty(confDir + "protective armor.enable", false);
                            config.reload();
                            continue;
                        }
                        int playerHelmID,
                                playerBodyID,
                                playerLegsID,
                                playerFeetID;
                        PlayerInventory inv = player.getInventory();
                        playerHelmID = inv.getHelmet().getTypeId();
                        playerLegsID = inv.getLeggings().getTypeId();
                        playerBodyID = inv.getChestplate().getTypeId();
                        playerFeetID = inv.getBoots().getTypeId();
                        if (block.getLightLevel() > 14 && player.getFireTicks() <= 0 && !(playerHelmID == armorHead && playerBodyID == armorBody && playerLegsID == armorLegs && playerFeetID == armorFeet)) {
                            player.setFireTicks(99999999);
                            continue;
                        } else if (block.getLightLevel() <= 14 && player.getFireTicks() >= 100000) {
                            player.setFireTicks(-20);
                            continue;
                        } else if (block.getLightLevel() > 14 && player.getFireTicks() <= 0 && (playerHelmID == armorHead && playerBodyID == armorBody && playerLegsID == armorLegs && playerFeetID == armorFeet)) {
                            counter++;
                            armorTime = config.yml.getInt(confDir + "protective armor.time", 5);
                            if (counter >= armorTime) {
                                currentHelm = ((short) (inv.getHelmet().getDurability() + 1));
                                currentBody = ((short) (inv.getChestplate().getDurability() + 1));
                                currentLegs = ((short) (inv.getLeggings().getDurability() + 1));
                                currentFeet = ((short) (inv.getBoots().getDurability() + 1));
                                inv.getHelmet().setDurability(currentHelm);
                                if (currentHelm >= maxHelm) {
                                    inv.setHelmet(null);
                                }
                                inv.getChestplate().setDurability(currentBody);
                                if (currentBody >= maxBody) {
                                    inv.setChestplate(null);
                                }
                                inv.getLeggings().setDurability(currentLegs);
                                if (currentLegs >= maxLegs) {
                                    inv.setLeggings(null);
                                }
                                inv.getBoots().setDurability(currentFeet);
                                if (currentFeet >= maxFeet) {
                                    inv.setBoots(null);
                                }
                                counter = 0;
                            }
                            continue;
                        }
                    }
                }
            }
        }, 0, 20 * 1);
        System.out.println("[SunBurn] v" + this.getDescription().getVersion() + " has been enabled!");
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
        Player player = null;
        if ((cs instanceof Player)) {
            player = (Player) cs;
        }

        if (cmnd.getName().equalsIgnoreCase("sunburn")) {
            String arg = "";
            String name = "";

            try {
                arg = args[0];
            } catch (ArrayIndexOutOfBoundsException e) {
                return false;
            }

            if (arg.equalsIgnoreCase("set")) {
                try {
                    if ((cs instanceof Player) && !player.hasPermission("sunburn.admin")) {
                        cs.sendMessage(ChatColor.RED + "You do not have access to this command");
                        return true;
                    }
                    arg = args[1];
                    if (arg.equalsIgnoreCase("default")) {
                        this.config.setDefaults();
                        cs.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] Config defaults applied");
                        return true;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    return false;
                }
            }

            if (arg.equalsIgnoreCase("clear")) {
                try {
                    if ((cs instanceof Player) && !player.hasPermission("sunburn.admin")) {
                        cs.sendMessage(ChatColor.RED + "You do not have access to this command");
                        return true;
                    }
                    arg = args[1];
                    if (arg.equalsIgnoreCase("worlds")) {
                        enabledWorlds = config.yml.getStringList("sunburn.enabled worlds", null);
                        enabledWorlds.clear();
                        this.config.yml.setProperty("sunburn.enabled worlds", enabledWorlds);
                        this.config.reload();
                        cs.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] Enabled worlds now cleared");
                        return true;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    return false;
                }
            }

            if (arg.equalsIgnoreCase("world")) {
                name = player.getWorld().getName();
                enabledWorlds = config.yml.getStringList("sunburn.enabled worlds", null);
                try {
                    arg = args[1];
                    if ((cs instanceof Player) && !player.hasPermission("sunburn.admin")) {
                        cs.sendMessage(ChatColor.RED + "You do not have access to this command");
                        return true;
                    }
                    if (arg.equalsIgnoreCase("enable")) {
                        enabledWorlds.add(name);
                        this.config.yml.setProperty("sunburn.enabled worlds", enabledWorlds);
                        this.config.reload();
                        cs.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] is now" + ChatColor.GREEN + " ENABLED" + ChatColor.DARK_AQUA + " for world " + name);
                        return true;
                    } else if (arg.equalsIgnoreCase("disable")) {
                        enabledWorlds.remove(name);
                        this.config.yml.setProperty("sunburn.enabled worlds", enabledWorlds);
                        this.config.reload();
                        for (Player players : getServer().getOnlinePlayers()) {
                            if (players.getFireTicks() >= 100000) {
                                players.setFireTicks(-20);
                            }
                        }
                        cs.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] is now" + ChatColor.RED + " DISABLED" + ChatColor.DARK_AQUA + " for world " + name);
                        return true;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    this.config.reload();
                    if (enabledWorlds.contains(name)) {
                        cs.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] is" + ChatColor.GREEN + " ENABLED" + ChatColor.DARK_AQUA + " for world " + name);
                        return true;
                    } else if (!enabledWorlds.contains(name)) {
                        cs.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] is" + ChatColor.RED + " DISABLED" + ChatColor.DARK_AQUA + " for world " + name);
                        return true;
                    }
                }
            }

            if (arg.equalsIgnoreCase("players")) {
                try {
                    arg = args[1];
                    if ((cs instanceof Player) && !player.hasPermission("sunburn.admin")) {
                        cs.sendMessage(ChatColor.RED + "You do not have access to this command");
                        return true;
                    }
                    if (arg.equalsIgnoreCase("enable")) {
                        this.config.yml.setProperty(this.confDir + "burn.players", true);
                        this.config.reload();
                        cs.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] Player burning is now" + ChatColor.GREEN + " ENABLED");
                        return true;
                    } else if (arg.equalsIgnoreCase("disable")) {
                        this.config.yml.setProperty(this.confDir + "burn.players", false);
                        this.config.reload();
                        for (Player players : getServer().getOnlinePlayers()) {
                            if (players.getFireTicks() >= 100000) {
                                players.setFireTicks(-20);
                            }
                        }
                        cs.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] Player burning is now " + ChatColor.RED + "DISABLED");
                        return true;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    boolean burningEnabled = this.config.yml.getBoolean(this.confDir + "burn.mobs", true);
                    if (burningEnabled) {
                        cs.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] Player burning is" + ChatColor.GREEN + " ENABLED");
                        return true;
                    } else if (!burningEnabled) {
                        cs.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] Player burning is" + ChatColor.RED + " DISABLED");
                        return true;
                    }
                }
            }

            if (arg.equalsIgnoreCase("mobs")) {
                try {
                    arg = args[1];
                    if ((cs instanceof Player) && !player.hasPermission("sunburn.admin")) {
                        cs.sendMessage(ChatColor.RED + "You do not have access to this command");
                        return true;
                    }
                    if (arg.equalsIgnoreCase("enable")) {
                        this.config.yml.setProperty(this.confDir + "burn.mobs", true);
                        this.config.reload();
                        cs.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] Creature burning is now" + ChatColor.GREEN + " ENABLED");
                        return true;
                    } else if (arg.equalsIgnoreCase("disable")) {
                        this.config.yml.setProperty(this.confDir + "burn.mobs", false);
                        this.config.reload();
                        cs.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] Creature burning is now" + ChatColor.RED + " DISABLED");
                        return true;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    boolean burningEnabled = this.config.yml.getBoolean(this.confDir + "burn.mobs", true);
                    if (burningEnabled) {
                        cs.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] Creature burning is" + ChatColor.GREEN + " ENABLED");
                        return true;
                    } else if (!burningEnabled) {
                        cs.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] Creature burning is" + ChatColor.RED + " DISABLED");
                        return true;
                    }
                }
            }
            if ((arg.equalsIgnoreCase("wasteland"))) {
                if (!(cs instanceof Player)) {
                    System.out.println("[SunBurn] Only players may use this command.");
                    return true;
                }
                if ((cs instanceof Player) && !player.hasPermission("sunburn.admin")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                try {
                    stringRadius = args[1];
                    radius = Integer.valueOf(stringRadius);
                    float xy = radius / 2;
                    xv = Math.round(xy);
                    zv = Math.round(xy);
                    World world = player.getWorld();
                    getServer().broadcastMessage(ChatColor.DARK_AQUA + "[SunBurn] " + player.getDisplayName() + " has started wasteland generation. This may cause server lag.");
                    for (int x = -xv; x < radius - xv; x++) {
                        for (int z = -zv; z < radius - zv; z++) {
                            Block b = world.getHighestBlockAt(player.getLocation().getBlockX() + x, player.getLocation().getBlockZ() + z);
                            while (true) {
                                if (b.getLocation().getY() <= 2) {
                                    break; //Just in case it reaches the bottom of the world (inf. loop)
                                }
                                Material type = b.getType();
                                if (type == Material.LEAVES) {
                                    b.setType(Material.AIR);
                                } else if (type == Material.GRASS) {
                                    b.setType(Material.DIRT);
                                } else if (type == Material.AIR) {
                                    //to prevent it exiting the loop in gaps
                                } else if (type == Material.TORCH) {
                                    b.setType(Material.AIR);
                                    ItemStack item = new ItemStack(Material.TORCH, 1);
                                    world.dropItem(b.getLocation(), item);
                                } else if (type == Material.BROWN_MUSHROOM) {
                                    b.setType(Material.AIR);
                                    ItemStack item = new ItemStack(Material.BROWN_MUSHROOM, 1);
                                    world.dropItem(b.getLocation(), item);
                                } else if (type == Material.RED_MUSHROOM) {
                                    b.setType(Material.AIR);
                                    ItemStack item = new ItemStack(Material.RED_MUSHROOM, 1);
                                    world.dropItem(b.getLocation(), item);
                                } else if (type == Material.YELLOW_FLOWER) {
                                    b.setType(Material.AIR);
                                    ItemStack item = new ItemStack(Material.YELLOW_FLOWER, 1);
                                    world.dropItem(b.getLocation(), item);
                                } else if (type == Material.RED_ROSE) {
                                    b.setType(Material.AIR);
                                    ItemStack item = new ItemStack(Material.RED_ROSE, 1);
                                    world.dropItem(b.getLocation(), item);
                                } else if (type == Material.LONG_GRASS) {
                                    b.setType(Material.AIR);
                                } else if (type == Material.LOG) {
                                } else if (type == Material.AIR) {
                                } else if (type == Material.VINE) {
                                    b.setType(Material.AIR);
                                } else if (type == Material.STONE) {
                                } else if (type == Material.DIRT) {
                                } else if (type == Material.HUGE_MUSHROOM_1) {
                                } else if (type == Material.HUGE_MUSHROOM_2) {
                                } else {
                                    break;
                                }
                                b = b.getRelative(BlockFace.DOWN);
                            }
                        }
                    }
                    getServer().broadcastMessage(ChatColor.DARK_AQUA + "[SunBurn] Wasteland generation complete");
                    return true;
                } catch (ArrayIndexOutOfBoundsException e) {
                    return false;
                }
            }

            if ((arg.equalsIgnoreCase("info"))) {
                cs.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] was created by: Ahniolator");
                cs.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] Current Version: v" + this.getDescription().getVersion());
                return true;
            }

            if ((arg.equalsIgnoreCase("help"))) {
                cs.sendMessage(ChatColor.DARK_AQUA + "************  [SunBurn] Help List  ***************");
                cs.sendMessage(ChatColor.DARK_AQUA + "/sunburn help: Displays this help dialog");
                cs.sendMessage(ChatColor.DARK_AQUA + "/sunburn world [enable/disable]: Shows world information");
                cs.sendMessage(ChatColor.DARK_AQUA + "     if no arguments are given. Also enables/disables on that world");
                cs.sendMessage(ChatColor.DARK_AQUA + "/sunburn players [enable/disable]: Enables/Disables burning players");
                cs.sendMessage(ChatColor.DARK_AQUA + "     if no arguments are given. Also enables/disables on that world");
                cs.sendMessage(ChatColor.DARK_AQUA + "/sunburn mobs [enable/disable]: Enables/Disables burning mobs");
                cs.sendMessage(ChatColor.DARK_AQUA + "     if no arguments are given. Also enables/disables on that world");
                cs.sendMessage(ChatColor.DARK_AQUA + "/sunburn info: Shows author and version info");
                if ((cs instanceof Player) && !player.hasPermission("sunburn.admin")) {
                    return true;
                }
                cs.sendMessage(ChatColor.DARK_AQUA + "/sunburn wasteland [diameter]: Converts the world within");
                cs.sendMessage(ChatColor.DARK_AQUA + "     [diameter] into a wasteland. Cannot be undone.");
                cs.sendMessage(ChatColor.DARK_AQUA + "/sunburn reload: Reloads config");
                cs.sendMessage(ChatColor.DARK_AQUA + "/sunburn set default: Recreates the default config file and replaces");
                cs.sendMessage(ChatColor.DARK_AQUA + "     the current one with it");
                cs.sendMessage(ChatColor.DARK_AQUA + "/sunburn clear worlds: Removes all worlds from the enabled worlds list");
                return true;
            }

            if (arg.equalsIgnoreCase("reload")) {
                if ((cs instanceof Player) && !player.hasPermission("sunburn.admin")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                this.config.reload();
                cs.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] Reload Complete");
                return true;
            }
            
            if (arg.equalsIgnoreCase("update") || arg.equalsIgnoreCase("notification") || arg.equalsIgnoreCase("notifications")) {
                try {
                    arg = args[1];
                    if ((cs instanceof Player) && !player.hasPermission("sunburn.admin")) {
                        cs.sendMessage(ChatColor.RED + "You do not have access to this command");
                        return true;
                    }
                    if (arg.equalsIgnoreCase("enable")) {
                        this.config.yml.setProperty(confDir + "update.notifications", true);
                        this.config.reload();
                        tellUpdate = this.config.yml.getBoolean(confDir + "update.notifications", true);
                        cs.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] Update notifications are now" + ChatColor.GREEN + " ENABLED");
                        return true;
                    } else if (arg.equalsIgnoreCase("disable")) {
                        this.config.yml.setProperty(confDir + "update.notifications", false);
                        this.config.reload();
                        tellUpdate = this.config.yml.getBoolean(confDir + "update.notifications", true);
                        cs.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] Update notifications are now " + ChatColor.RED + "DISABLED");
                        return true;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    if ((cs instanceof Player) && !player.hasPermission("sunburn.admin")) {
                        cs.sendMessage(ChatColor.RED + "You do not have access to this command");
                        return true;
                    }
                    if ((cs instanceof Player)) {
                        checkForUpdates(this, player, true);
                        return true;
                    } else {
                        checkForUpdates(this, cs);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isPlayer(LivingEntity e) {
        return e instanceof Player;
    }
    
    public static void checkForUpdates(Sunburn plugin, Player player, boolean response) {
        scriptVersion = Double.valueOf(Double.parseDouble(plugin.getDescription().getVersion())).doubleValue();
        try {
            double currver = getCurrentVersion(player, currentVerUrl);
            if (currver > scriptVersion) {
                player.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] There has been an update!");
                player.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] Your current version is " + scriptVersion + ".");
                player.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] The newest version is " + currver);
                player.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] ChangeLog: " + changes);
                player.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] Please visit the BukkitDev page to update!");
            } else {
                if (response) {
                    player.sendMessage(ChatColor.DARK_AQUA + "[BurningCS] is up to date!");
                }
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double getCurrentVersion(Player player, String site) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(new URL(site).openStream()));
            String[] string = r.readLine().split(";");
            changes = string[1];
            double d = Double.parseDouble(string[0]);
            r.close();
            return d;
        } catch (Exception e) {
            player.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] Error checking for latest version.");
            e.printStackTrace();
        }
        return scriptVersion;
    }

    public static void checkForUpdates(Sunburn plugin, CommandSender player) {
        scriptVersion = Double.valueOf(Double.parseDouble(plugin.getDescription().getVersion())).doubleValue();
        try {
            double currver = getCurrentVersion(player, currentVerUrl);
            if (currver > scriptVersion) {
                player.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] There has been an update!");
                player.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] Your current version is " + scriptVersion + ".");
                player.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] The newest version is " + currver);
                player.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] ChangeLog: " + changes);
                player.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] Please visit the BukkitDev page to update!");
            } else {
                player.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] is up to date!");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double getCurrentVersion(CommandSender player, String site) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(new URL(site).openStream()));
            String[] string = r.readLine().split(";");
            changes = string[1];
            double d = Double.parseDouble(string[0]);
            r.close();
            return d;
        } catch (Exception e) {
            player.sendMessage(ChatColor.DARK_AQUA + "[SunBurn] Error checking for latest version.");
            e.printStackTrace();
        }
        return scriptVersion;
    }
}