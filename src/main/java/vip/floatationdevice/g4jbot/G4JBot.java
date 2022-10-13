package vip.floatationdevice.g4jbot;

import com.google.common.eventbus.Subscribe;
import vip.floatationdevice.guilded4j.G4JClient;
import vip.floatationdevice.guilded4j.event.ChatMessageCreatedEvent;
import vip.floatationdevice.guilded4j.object.ChatMessage;

import java.util.HashMap;
import java.util.HashSet;

import static vip.floatationdevice.guilded4j.Util.isGID;
import static vip.floatationdevice.guilded4j.Util.isUUID;

/**
 * Subclass of <a href="http://docs.floatationdevice.vip/guilded4j/vip/floatationdevice/guilded4j/G4JClient.html">G4JClient</a>,
 * adds functionality to easily handle commands.
 */
public class G4JBot extends G4JClient
{
    private final HashSet<String> svrs = new HashSet<>();
    private final HashSet<String> chns = new HashSet<>();
    private final HashMap<String, GuildedCommandExecutor> cmds = new HashMap<>();
    private String pfx;

    public G4JBot(String token)
    {
        super(token);
        registerEventListener(this);
    }

    public G4JBot(String token, String lastMessageId)
    {
        super(token, lastMessageId);
        registerEventListener(this);
    }

//============================ COMMAND MANAGER START ============================

    /**
     * Get the command prefix of the bot.
     *
     * @return The prefix of the command.
     */
    public String getCommandPrefix()
    {
        return pfx;
    }

    /**
     * Set the command prefix of the bot.
     *
     * @param pfx The prefix of the command.
     * @throws IllegalArgumentException if the command prefix is null or empty.
     */
    public G4JBot setCommandPrefix(String pfx)
    {
        if (pfx == null || pfx.isEmpty()) throw new IllegalArgumentException("Command must be presented");
        this.pfx = pfx;
        return this;
    }

    /**
     * Add an ID of the server to listen for commands.
     *
     * @param serverId The ID of the server you want to listen for commands.
     * @throws IllegalArgumentException if the server ID is invalid, null or empty.
     */
    public G4JBot addCommandListeningServerId(String serverId)
    {
        if (!isGID(serverId))
            throw new IllegalArgumentException("Invalid server ID: " + serverId);
        svrs.add(serverId);
        return this;
    }

    /**
     * Add a UUID of the channel to listen for commands.
     *
     * @param channelId The UUID of the channel you want to listen for commands.
     * @throws IllegalArgumentException if the UUID is invalid, null or empty.
     */
    public G4JBot addCommandListeningChannelId(String channelId)
    {
        if (!isUUID(channelId))
            throw new IllegalArgumentException("Invalid channel UUID: " + channelId);
        chns.add(channelId);
        return this;
    }

    /**
     * Remove a server ID from command listening server list.
     *
     * @param serverId The server ID to remove.
     * @throws IllegalArgumentException if the server ID is invalid, null or empty.
     */
    public G4JBot removeCommandListeningServerId(String serverId)
    {
        if (!isGID(serverId))
            throw new IllegalArgumentException("Invalid server ID: " + serverId);
        svrs.remove(serverId);
        return this;
    }

    /**
     * Remove a channel UUID from command listening channel list.
     *
     * @param channelId The UUID to remove.
     * @throws IllegalArgumentException if the UUID is invalid, null or empty.
     */
    public G4JBot removeCommandListeningChannelId(String channelId)
    {
        if (!isUUID(channelId))
            throw new IllegalArgumentException("Invalid channel UUID: " + channelId);
        chns.remove(channelId);
        return this;
    }

    /**
     * Get the ID list of command listening servers.
     *
     * @return A String array containing the IDs. An empty array indicates that the bot is listening to all reachable servers.
     */
    public String[] getCommandListeningServerIds()
    {
        return svrs.toArray(new String[svrs.size()]);
    }

    /**
     * Get the ID list of command listening channels.
     *
     * @return A String array containing the IDs. An empty array indicates that the bot is listening to all reachable channels.
     */
    public String[] getCommandListeningChannelIds()
    {
        return chns.toArray(new String[chns.size()]);
    }

    /**
     * Register the executor of a command.
     *
     * @param executor The executor to register.
     * @throws IllegalStateException    if the bot command prefix is not set.
     * @throws IllegalArgumentException if executor is null or its command name is null or empty.
     */
    public G4JBot registerCommand(GuildedCommandExecutor executor)
    {
        if (pfx == null) throw new IllegalStateException("Command prefix not set");
        if (executor == null || executor.getCommandName() == null || executor.getCommandName().isEmpty())
            throw new IllegalArgumentException("Command is empty");
        cmds.put(executor.getCommandName(), executor);
        return this;
    }

    /**
     * Unregister the executor of a command.
     *
     * @param executor The executor to unregister.
     * @throws IllegalArgumentException if the executor is not found in the executor list.
     */
    public G4JBot unregisterCommand(GuildedCommandExecutor executor)
    {
        for (GuildedCommandExecutor e : cmds.values())
            if (e == executor)
            {
                cmds.remove(e.getCommandName());
                return this;
            }
        throw new IllegalArgumentException("Command executor not found");
    }

    /**
     * Unregister a command executor by its name.
     *
     * @param cmd The name of the command executor.
     * @throws IllegalArgumentException if no executor with the given command name was found.
     */
    public G4JBot unregisterCommand(String cmd)
    {
        if (cmds.remove(cmd) != null)
            return this;
        throw new IllegalArgumentException("Command not found");
    }

//============================ COMMAND MANAGER END ============================

//============================ INTERNAL EVENT LISTENER START ============================

    @Subscribe
    private void onChatMessage(ChatMessageCreatedEvent e)
    {
        ChatMessage msg = e.getChatMessage();
        if (checkMessageLocation(msg) && msg.getContent().startsWith(pfx))
        {
            // [pfx]cmd arg1 arg2...
            String[] cmd = msg.getContent().substring(pfx.length()).split(" "); // [cmd, arg1, arg2...]
            if (cmd.length > 0 && cmds.get(cmd[0]) != null)
            {
                String[] args = new String[cmd.length - 1]; // [arg1, arg2...]
                System.arraycopy(cmd, 1, args, 0, args.length);
                cmds.get(cmd[0]).onCommand(this, msg, args);
            }
        }
    }

    private boolean checkMessageLocation(ChatMessage msg) // check if the message is in the listening server and channel
    {
        if (svrs.size() == 0 && chns.size() == 0) return true; // true if listenServers & listenChannels are not set
        else if (svrs.size() == 0) // if listenServers is not set, only check channel
            return chns.contains(msg.getChannelId());
        else if (chns.size() == 0) // if listenChannels is not set, only check server
            return svrs.contains(msg.getServerId());
        else return svrs.contains(msg.getServerId()) && chns.contains(msg.getChannelId()); // check both if all set
    }
//============================ INTERNAL EVENT LISTENER END ============================

//============================ OVERRIDE FUNCTIONS START ============================

    @Override
    public G4JBot setVerbose(boolean status)
    {
        super.setVerbose(status);
        return this;
    }

    @Override
    public G4JBot setAutoReconnect(boolean status)
    {
        super.setAutoReconnect(status);
        return this;
    }

    @Override
    public G4JBot registerEventListener(Object listener)
    {
        super.registerEventListener(listener);
        return this;
    }

    @Override
    public G4JBot unregisterEventListener(Object listener)
    {
        super.unregisterEventListener(listener);
        return this;
    }

    @Override
    public G4JBot connectWebSocket(boolean blocking, String lastMessageId)
    {
        super.connectWebSocket(blocking, lastMessageId);
        return this;
    }

    @Override
    public G4JBot connectWebSocket(String lastMessageId)
    {
        super.connectWebSocket(lastMessageId);
        return this;
    }

    @Override
    public G4JBot connectWebSocket()
    {
        super.connectWebSocket();
        return this;
    }

    @Override
    public G4JBot disconnectWebSocket(boolean blocking)
    {
        super.disconnectWebSocket(blocking);
        return this;
    }

    @Override
    public G4JBot disconnectWebSocket()
    {
        super.disconnectWebSocket();
        return this;
    }

//============================ OVERRIDE FUNCTIONS END ============================
}
