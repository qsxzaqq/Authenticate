package com.github.Zoxuh;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.github.Zoxuh.Filter.ConsoleFilter;

public class Authenticate extends JavaPlugin
		implements Listener{
	boolean CoreProtect = false;
	ArrayList<String> name = new ArrayList<String>();
	public void onEnable(){
		File f = new File(getDataFolder(),"config.yml");
		if(!getDataFolder().exists()){
			getDataFolder().mkdir();
		}
		if(!f.exists()){
			saveDefaultConfig();
		}
		File data = new File(getDataFolder(),"Players");
		if(!data.exists()){
			data.mkdir();
		}
		if(Bukkit.getPluginManager().getPlugin("CoreProtect")!=null){
			CoreProtect = true;
		}
		getLogger().info("玩家正版认证插件加载完成!");
		getLogger().info("作者: Shawhoi | 小组: IceFox");
		ConsoleFilter.setupConsoleFilter();
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerChat(AsyncPlayerChatEvent e){
		if(e.getMessage().toLowerCase().startsWith("/zb ")){
			String[] fg = e.getMessage().split(" ");
			if(fg.length>=2){
				e.setMessage(e.getMessage().replace(fg[2], "**********"));
			}
		}
	}
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerCommand(PlayerCommandPreprocessEvent e){
		Player sender = e.getPlayer();
		if(e.getMessage().toLowerCase().startsWith("/zb ")){
			String[] fg = e.getMessage().split(" ");
			File f = new File(getDataFolder()+"\\Players\\",e.getPlayer().getName()+".yml");
			FileConfiguration data = YamlConfiguration.loadConfiguration(f);
			if(fg.length==0||fg.length<=2){
				return;
			}
			e.setMessage(e.getMessage().replace(fg[2], "**********"));
			if(data.getBoolean("Success")){
				e.setCancelled(true);
				sender.sendMessage(getConfig().getString("Prefix").replace("&", "§")+getConfig().getString("Error.success").replace("&", "§"));
				return;
			}
			if(getAmount(sender.getName())>=getConfig().getInt("MaxAmount")){return;}
			sender.sendMessage(getConfig().getString("Prefix").replace("&", "§")+"§a正在连接认证服务器中...");
			try {
				postMojang(e.getPlayer(),fg[1],fg[2]);
			}catch (ParseException e2) {
				e2.printStackTrace();
			}
			getLogger().info(e.getPlayer().getName()+" authenticated!");
		}
	}
	public boolean onCommand(CommandSender sender,Command cmd,String lable,String args[]){
		if(lable.equalsIgnoreCase("zb")){
			if(!(sender instanceof Player)) return true;
			if(args.length==0){
				for(String i:getConfig().getStringList("Error.command")){
					sender.sendMessage(i.replace("&", "§").replace("%amount%", ""+getConfig().getInt("MaxAmount")));
				}
				return true;
			}
			if(getAmount(sender.getName())>=getConfig().getInt("MaxAmount")){
				sender.sendMessage(getConfig().getString("Prefix").replace("&", "§")+getConfig().getString("Error.maxamount").replace("&", "§"));
				return true;
			}
			if(args.length<2){
				sender.sendMessage(getConfig().getString("Prefix").replace("&", "§")+"§4请输入正确的参数!");
				return true;
			}
		}
		return false;
	}
	@SuppressWarnings({ "unchecked" })
	public void postMojang(Player p,String email,String pass) throws ParseException{
		new BukkitRunnable(){
			@Override
			public void run(){
				String url_post = "https://authserver.mojang.com/authenticate";
		        try {
		            URL url = new URL(url_post);
		            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		            connection.setUseCaches(false);
		            connection.setInstanceFollowRedirects(true);
		            connection.setRequestProperty("Content-Type","application/json");
		            connection.setDoOutput(true);
		            connection.setDoInput(true);
		            connection.setRequestMethod("POST");
		            connection.connect();

		            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
		            JSONObject obj = new JSONObject();
		            obj.put("username", email);
		            obj.put("password", pass);
		            JSONObject obj2 = new JSONObject();
		            obj2.put("name", "Minecraft");
		            obj2.put("version", 1);
		            obj.put("agent", obj2);

		            out.writeBytes(obj.toString());
		            out.flush();
		            out.close();
		            
		            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		            String lines;
		            StringBuffer stbu = new StringBuffer("");
		            while ((lines = reader.readLine()) != null) {
		                lines = new String(lines.getBytes(), "utf-8");
		                stbu.append(lines);
		            }
		            String i = stbu.toString();
		            JSONObject x = (JSONObject)(new JSONParser().parse(i));
		            String m = x.get("selectedProfile").toString();
		            JSONObject y = (JSONObject)(new JSONParser().parse(m));
		            if(p.getName().equals(y.get("name"))){
		            	addAmount(p.getName());
		            	p.sendMessage(getConfig().getString("Prefix").replace("&", "§")+getConfig().getString("Success.message").replace("&", "§"));
		            	File f = new File(getDataFolder()+"\\Players\\",p.getName()+".yml");
		            	FileConfiguration data = YamlConfiguration.loadConfiguration(f);
		            	data.set("Success", true);
		            	data.save(f);
		            	for(String c:getConfig().getStringList("Success.Commands")){
		            		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c.replace("&", "§").replace("%player%", p.getName()));
		            	}
		            }else{
		            	addAmount(p.getName());
		            	p.sendMessage(getConfig().getString("Prefix").replace("&", "§")+getConfig().getString("Error.errorname").replace("&", "§"));
		            }
		            reader.close();
		            // 断开连接
		            connection.disconnect();
		        } catch (MalformedURLException e) {
		            p.sendMessage(getConfig().getString("Prefix").replace("&", "§")+getConfig().getString("Error.malformedURL").replace("&", "§").replace("%amount%", ""+(getConfig().getInt("MaxAmount")-getAmount(p.getName()))));
		        } catch (UnsupportedEncodingException e) {
		        	p.sendMessage(getConfig().getString("Prefix").replace("&", "§")+getConfig().getString("Error.errorencoding").replace("&", "§").replace("%amount%", ""+(getConfig().getInt("MaxAmount")-getAmount(p.getName()))));
		        } catch (IOException e) {
		        	addAmount(p.getName());
		        	p.sendMessage(getConfig().getString("Prefix").replace("&", "§")+getConfig().getString("Error.errorpassword").replace("&", "§").replace("%amount%", ""+(getConfig().getInt("MaxAmount")-getAmount(p.getName()))));
		        } catch (ParseException e) {
		        	p.sendMessage(getConfig().getString("Prefix").replace("&", "§")+"§c出现未知错误, 请联系服主修复!");
				}
			}
		}.runTaskLaterAsynchronously(this, 0L);
	}
	public int getAmount(String name){
		int amount = 0;
		File f = new File(getDataFolder()+"\\Players\\",name+".yml");
		FileConfiguration data = YamlConfiguration.loadConfiguration(f);
		if(!f.exists()){
			try {
				f.createNewFile();
				data.set("Amount", 0);
				data.set("Success", false);
				data.save(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		amount = data.getInt("Amount");
		return amount;
	}
	public void addAmount(String name){
		int amount = 0;
		File f = new File(getDataFolder()+"\\Players\\",name+".yml");
		FileConfiguration data = YamlConfiguration.loadConfiguration(f);
		if(!f.exists()){
			try {
				f.createNewFile();
				data.set("Amount", 0);
				data.set("Success", false);
				data.save(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		amount = data.getInt("Amount");
		amount = amount + 1;
		data.set("Amount", amount);
		try {
			data.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}
}
