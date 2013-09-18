package com.comze_instancelabs.maze;

/**
 * 
 * @author instancelabs
 *
 */

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class Main extends JavaPlugin implements Listener{
	
	
	public static Economy econ = null;
	public boolean economy = false;
	
	WorldGuardPlugin worldGuard = null;
	
	
	static HashMap<Player, String> arenap = new HashMap<Player, String>(); // playername -> arenaname
	static HashMap<Player, String> tpthem = new HashMap<Player, String>(); // playername -> arenaname
	
	
	@Override
	public void onEnable(){
		getServer().getPluginManager().registerEvents(this, this);
		worldGuard = (WorldGuardPlugin) getWorldGuard();
		
		getConfig().addDefault("config.use_points", true);
		getConfig().addDefault("config.use_economy", false);
		getConfig().addDefault("config.moneyreward_amount", 20.0);
		getConfig().addDefault("config.itemid", 264);
		getConfig().addDefault("config.itemamount", 1);
		getConfig().addDefault("config.auto_updating", true);
		getConfig().addDefault("config.cooldown", 24);
		
		getConfig().addDefault("strings.nopermission", "§4You don't have permission!");
		getConfig().addDefault("strings.createcourse", "§2Maze saved. Now create a spawn and a lobby. :)");
		getConfig().addDefault("strings.help1", "§2Maze help:");
		getConfig().addDefault("strings.help2", "§2Use '/maze createmaze <name>' to create a new maze.");
		getConfig().addDefault("strings.help3", "§2Use '/maze setlobby <name>' to set the lobby for an maze.");
		getConfig().addDefault("strings.help4", "§2Use '/maze setspawn <name>' to set a new maze spawn.");
		getConfig().addDefault("strings.lobbycreated", "§2Lobby successfully created!");
		getConfig().addDefault("strings.spawn", "§2Spawnpoint registered.");
		getConfig().addDefault("strings.courseremoved", "§4Maze removed.");
		getConfig().addDefault("strings.reload", "§2Maze config successfully reloaded.");
		getConfig().addDefault("strings.nothing", "§4This command action was not found.");
		getConfig().addDefault("strings.ingame", "§eYou are not able to use any commands while in this minigame. You can use /maze leave if you want to leave the minigame.");
		getConfig().addDefault("strings.left", "§eYou left the maze!");
		
		
		if(getConfig().getBoolean("config.use_economy")){
			economy = true;
			if (!setupEconomy()) {
	            getLogger().severe(String.format("[%s] - No iConomy dependency found! Disabling Economy.", getDescription().getName()));
	            //getServer().getPluginManager().disablePlugin(this);
	            economy = false;
	        }
		}
		
		getConfig().options().copyDefaults(true);
		this.saveConfig();
	}
	
	public Plugin getWorldGuard(){
    	return Bukkit.getPluginManager().getPlugin("WorldGuard");
    }


	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		getLogger().info(cmd.getName());
		if(cmd.getName().equalsIgnoreCase("maze")){
    		if(args.length < 1){
    			sender.sendMessage(getConfig().getString("strings.help1"));
    			sender.sendMessage(getConfig().getString("strings.help2"));
    			sender.sendMessage(getConfig().getString("strings.help3"));
    			sender.sendMessage(getConfig().getString("strings.help4"));
    			return true;
    		}else{
    			Player p = (Player)sender;
    			if(args.length > 0){
    				String action = args[0];
    				if(action.equalsIgnoreCase("createmaze") && args.length > 1){
    					// Create arena
    					if(p.hasPermission("maze.create")){
    						this.getConfig().set(args[1] + ".name", args[1]);
	    	    			this.getConfig().set(args[1] + ".world", p.getWorld().getName());
	    	    			this.saveConfig();
	    	    			String arenaname = args[1];
	    	    			sender.sendMessage(getConfig().getString("strings.createcourse"));
    					}
    				}else if(action.equalsIgnoreCase("setlobby") && args.length > 1){
    					// setlobby
    					if(p.hasPermission("maze.setlobby")){
    						String arena = args[1];
	    		    		Location l = p.getLocation();
	    		    		getConfig().set(args[1] + ".lobbyspawn.x", (int)l.getX());
	    		    		getConfig().set(args[1] + ".lobbyspawn.y", (int)l.getY());
	    		    		getConfig().set(args[1] + ".lobbyspawn.z", (int)l.getZ());
	    		    		getConfig().set(args[1] + ".lobbyspawn.world", p.getWorld().getName());
	    		    		this.saveConfig();
	    		    		sender.sendMessage(getConfig().getString("strings.lobbycreated"));
    					}
    				}else if(action.equalsIgnoreCase("setspawn") && args.length > 1){
    					// setspawn
    					if(p.hasPermission("maze.setspawn")){
    						String arena = args[1];
    			    		Location l = p.getLocation();
    			    		getConfig().set(args[1] + ".spawn.x", (int)l.getX());
    			    		getConfig().set(args[1] + ".spawn.y", (int)l.getY());
    			    		getConfig().set(args[1] + ".spawn.z", (int)l.getZ());
    			    		getConfig().set(args[1] + ".spawn.world", p.getWorld().getName());
    			    		this.saveConfig();
    			    		sender.sendMessage(getConfig().getString("strings.spawn"));
    					}
    				}else if(action.equalsIgnoreCase("removemaze") && args.length > 1){
    					// removearena
    					if(p.hasPermission("maze.remove")){
    						this.getConfig().set(args[1], null);
	    	    			this.saveConfig();
	    	    			sender.sendMessage(getConfig().getString("strings.courseremoved"));
    					}
    				}else if(action.equalsIgnoreCase("leave")){
    					// leave
    					if(p.hasPermission("maze.leave")){
    						String arena = arenap.get(p);
    						final Location t = new Location(Bukkit.getWorld(getConfig().getString(arena + ".lobbyspawn.world")), getConfig().getDouble(arena + ".lobbyspawn.x"), getConfig().getDouble(arena + ".lobbyspawn.y"), getConfig().getDouble(arena + ".lobbyspawn.z"));
                			p.teleport(t);
                			arenap.remove(p);
                			p.sendMessage(getConfig().getString("strings.left"));
    					}
    				}else if(action.equalsIgnoreCase("list")){
    					// list
    					if(p.hasPermission("maze.list")){
    						ArrayList<String> keys = new ArrayList<String>();
	    			        keys.addAll(getConfig().getKeys(false));
	    			        try{
	    			        	keys.remove("config");
	    			        	keys.remove("strings");
	    			        }catch(Exception e){
	    			        	
	    			        }
	    			        for(int i = 0; i < keys.size(); i++){
	    			        	if(!keys.get(i).equalsIgnoreCase("config") && !keys.get(i).equalsIgnoreCase("strings")){
	    			        		sender.sendMessage("§2" + keys.get(i));
	    			        	}
	    			        }
    					}
    				}else if(action.equalsIgnoreCase("reload")){
    					if(sender.hasPermission("maze.reload")){
	    					this.reloadConfig();
	    					sender.sendMessage(getConfig().getString("strings.reload"));
    					}else{
    						sender.sendMessage(getConfig().getString("strings.nopermission"));
    					}
    				}else if(action.equalsIgnoreCase("resethours")){
    					if(sender.hasPermission("maze.reset")){
	    					String pname = args[1];
	    					getConfig().set(pname + ".hoursleft", null);
	    					this.saveConfig();
	    					sender.sendMessage("§2Successfully reset " + pname);
    					}else{
    						sender.sendMessage(getConfig().getString("strings.nopermission"));
    					}
    				}else{
    					sender.sendMessage(getConfig().getString("strings.nothing"));
    				}
    			}
    		}
    		return true;
    	}
    	return false;
    }
	
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event){
		Player p = event.getPlayer();
		tpthem.put(p, arenap.get(p));
		arenap.remove(p);
	}
	
	@EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
		Player p = event.getEntity();
		tpthem.put(p, arenap.get(p));
		arenap.remove(p);
	}
	
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		if(tpthem.containsKey(event.getPlayer())){
			String arena = tpthem.get(event.getPlayer());
			Player p = event.getPlayer();
			final Location t = new Location(Bukkit.getWorld(getConfig().getString(arena + ".lobbyspawn.world")), getConfig().getDouble(arena + ".lobbyspawn.x"), getConfig().getDouble(arena + ".lobbyspawn.y"), getConfig().getDouble(arena + ".lobbyspawn.z"));
			p.teleport(t);
		}
	}
	
	
	@EventHandler
	public void onSignUse(PlayerInteractEvent event)
	{	
	    if (event.hasBlock() && event.getAction() == Action.RIGHT_CLICK_BLOCK)
	    {
	        if (event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.WALL_SIGN)
	        {
	            final Sign s = (Sign) event.getClickedBlock().getState();
	            if(getConfig().contains("player." + event.getPlayer().getName())){
	            	//TODO get date and if 24h cooldown true or false
	            }
                if (s.getLine(0).equalsIgnoreCase("§2[maze]"))
                {
                	if(!getConfig().isSet(event.getPlayer().getName() + ".hoursleft." + s.getLine(1))){
		        		SimpleDateFormat sdfToDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		        		StringBuilder test = new StringBuilder(sdfToDate.format(new Date()));
		        		getConfig().set(event.getPlayer().getName() + ".hoursleft." + s.getLine(1), test.toString());
		        		this.saveConfig();
		        		
			        	String arena = s.getLine(1);
	                	arena = arena.substring(2);
	                	Player p = event.getPlayer();
	                	
	                	arenap.put(event.getPlayer(), arena);
	                	
	                	event.getPlayer().sendMessage("§2You have entered the maze minigame!");
	                	
	                	final Location t = new Location(Bukkit.getWorld(getConfig().getString(arena + ".spawn.world")), getConfig().getDouble(arena + ".spawn.x"), getConfig().getDouble(arena + ".spawn.y"), getConfig().getDouble(arena + ".spawn.z"));
	        			event.getPlayer().teleport(t);	
                	}else{
                		if(checkHours(event.getPlayer(), s.getLine(1))){
		        			//event.getPlayer().giveExp(getConfig().getInt(godname + ".xp"));
		        			SimpleDateFormat sdfToDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		        			StringBuilder test = new StringBuilder(sdfToDate.format(new Date()));
			        		getConfig().set(event.getPlayer().getName() + ".hoursleft." + s.getLine(1), test.toString());
			        		this.saveConfig();
			        		String arena = s.getLine(1);
		                	arena = arena.substring(2);
		                	Player p = event.getPlayer();
		                	
		                	arenap.put(event.getPlayer(), arena);
		                	
		                	event.getPlayer().sendMessage("§2You have entered the maze minigame!");
		                	
		                	final Location t = new Location(Bukkit.getWorld(getConfig().getString(arena + ".spawn.world")), getConfig().getDouble(arena + ".spawn.x"), getConfig().getDouble(arena + ".spawn.y"), getConfig().getDouble(arena + ".spawn.z"));
		        			event.getPlayer().teleport(t);	
		        		}else{
		        			event.getPlayer().sendMessage("§4You need to wait 24 hours between joining the same maze. :/");
		        		}	
                	}
                	
                }else if(s.getLine(0).equalsIgnoreCase("§2[mreward]")){
                	if(arenap.containsKey(event.getPlayer())){
	                	if(getConfig().getBoolean("config.use_economy")){
	                		EconomyResponse r = econ.depositPlayer(event.getPlayer().getName(), getConfig().getDouble("config.moneyreward_amount"));
	            			if(!r.transactionSuccess()) {
	            				event.getPlayer().sendMessage(String.format("An error occured: %s", r.errorMessage));
	                            //sender.sendMessage(String.format("You were given %s and now have %s", econ.format(r.amount), econ.format(r.balance)));
	                        }
	                	}else{
	                		if(getConfig().getBoolean("config.use_points")){
	                			getServer().dispatchCommand(getServer().getConsoleSender(), "enjin addpoints " + event.getPlayer().getName() + " " + s.getLine(1));
	                		}else{
		                		event.getPlayer().getInventory().addItem(new ItemStack(Material.getMaterial(getConfig().getInt("config.itemid")), getConfig().getInt("config.itemamount")));
		                		event.getPlayer().updateInventory();
	                		}
	                		
	                	}
	                	event.getPlayer().sendMessage("§2Congratulations you beat the maze, here's your reward!");
	                	Player p = event.getPlayer();
	                	String arena = arenap.get(p);
						final Location t = new Location(Bukkit.getWorld(getConfig().getString(arena + ".lobbyspawn.world")), getConfig().getDouble(arena + ".lobbyspawn.x"), getConfig().getDouble(arena + ".lobbyspawn.y"), getConfig().getDouble(arena + ".lobbyspawn.z"));
            			p.teleport(t);
            			arenap.remove(p);
                	}
                	
                }
	        }
	    }
	}
	
	
	
	/***
	 * Checks if player is able to use the action again
	 * @param p Player to check
	 * @return returns true if last use 1 hour ago, false if not
	 */
	public boolean checkHours(Player p, String line1){
		SimpleDateFormat sdfToDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date datecurrent = new Date();
		String daysdate = getConfig().getString(p.getName() + ".hoursleft." + line1);
		//p.sendMessage(daysdate);
		Date date1 = null;
		try {
			date1 = sdfToDate.parse(daysdate);
			System.out.println(date1);
		} catch (ParseException ex2){
			ex2.printStackTrace();
		}
		Integer between = this.hoursBetween(datecurrent, date1);
		getLogger().info(Integer.toString(between));
		if(between > 23 || between < -23){
			return true;
		}else{
			return false;
		}
	}
	
		
	public int hoursBetween(Date d1, Date d2){
	    long differenceMilliSeconds = d2.getTime() - d1.getTime();
	    long hours = differenceMilliSeconds / 1000 / 60 / 60;
	    return (int) hours;
	}
	
	
	
	@EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player p = event.getPlayer();
        if(event.getLine(0).toLowerCase().contains("[maze]")){
        	if(event.getPlayer().hasPermission("maze.sign")){
	        	event.setLine(0, "§2[Maze]");
	        	if(!event.getLine(1).equalsIgnoreCase("")){
	        		String arena = event.getLine(1);
	        		event.setLine(1, "§5" +  arena);
	        	}
        	}
        }else if(event.getLine(0).toLowerCase().contains("[mreward]")){
        	if(event.getPlayer().hasPermission("maze.sign")){
	        	event.setLine(0, "§2[MReward]");
	        	event.getPlayer().sendMessage("§2You have successfully created a reward sign for maze!");
        	}
        }
	}
	

	
	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent event){
		if(arenap.containsKey(event.getPlayer())){
			// j leave
			if(event.getMessage().equalsIgnoreCase("/maze leave")){
				// nothing
			}else{
				event.setCancelled(true);
				event.getPlayer().sendMessage(getConfig().getString("strings.ingame"));
			}
		}
	}
	
}
