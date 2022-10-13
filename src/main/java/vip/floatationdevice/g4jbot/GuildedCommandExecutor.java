package vip.floatationdevice.g4jbot;

import vip.floatationdevice.guilded4j.object.ChatMessage;

/**
 * Handler for commands.
 */
public interface GuildedCommandExecutor
{
    /**
     * Gets the name of the command (the first word after the command prefix).
     * For example, if the command prefix is set to "/", the command you want
     * to implement is "/test", this function should return "test".
     *
     * @return The name of the command.
     */
    String getCommandName();

    /**
     * The code to execute when the command is triggered.
     *
     * @param bot  The bot this command belongs to.
     * @param msg  The chat message object that triggered the command.
     * @param args The command arguments.
     */
    void onCommand(G4JBot bot, ChatMessage msg, String[] args);
}
