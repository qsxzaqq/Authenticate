package com.github.Zoxuh.Filter;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

public class log4jFilter extends AbstractFilter{
    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return validateMessage(msg);
    }
    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
        return validateMessage(msg);
    }
    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        String candidate = null;
        if(msg != null) {
            candidate = msg.toString();
        }
        return validateMessage(candidate);
    }
    private static Result validateMessage(String message) {
        return message.contains("/zb ")?Result.DENY:Result.NEUTRAL;
    }
    @Override
    public Result filter(LogEvent event) {
        Message candidate = null;
        if(event != null) {
            candidate = event.getMessage();
        }
        return validateMessage(candidate);
    }
    private static Result validateMessage(Message message) {
        if (message == null) {
            return Result.NEUTRAL;
        }
        return validateMessage(message.getFormattedMessage());
    }
}
