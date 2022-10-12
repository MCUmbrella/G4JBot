package vip.floatationdevice.g4jbot;

import vip.floatationdevice.guilded4j.object.ChatMessage;

public interface GuildedCommandExecutor
{
    String getCommandName();

    boolean onCommand(G4JBot bot, ChatMessage msg, String[] args);
}
