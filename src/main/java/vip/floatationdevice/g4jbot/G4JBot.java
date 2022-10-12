package vip.floatationdevice.g4jbot;

import com.google.common.eventbus.Subscribe;
import vip.floatationdevice.guilded4j.G4JClient;
import vip.floatationdevice.guilded4j.Util;
import vip.floatationdevice.guilded4j.event.ChatMessageCreatedEvent;
import vip.floatationdevice.guilded4j.object.ChatMessage;

import java.util.HashMap;
import java.util.HashSet;

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

    public G4JBot registerCommand(GuildedCommandExecutor executor)
    {
        if (pfx == null) throw new IllegalStateException("Command prefix not set");
        if (executor == null || executor.getCommandName() == null || executor.getCommandName().isEmpty())
            throw new IllegalArgumentException("Command is empty");
        cmds.put(executor.getCommandName(), executor);
        return this;
    }

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

    public G4JBot unregisterCommand(String cmd)
    {
        if (cmds.remove(cmd) != null)
            return this;
        throw new IllegalArgumentException("Command not found");
    }

    public String getCommandPrefix()
    {
        return pfx;
    }

    public G4JBot setCommandPrefix(String pfx)
    {
        if (pfx == null || pfx.isEmpty()) throw new IllegalArgumentException("Command must be presented");
        this.pfx = pfx;
        return this;
    }

    public G4JBot addListeningServerId(String serverId)
    {
        if (serverId == null || serverId.isEmpty())
            throw new IllegalArgumentException("Invalid server ID: " + serverId);
        svrs.add(serverId);
        return this;
    }

    public G4JBot addListeningChannelId(String channelId)
    {
        if (channelId == null || channelId.isEmpty() || !Util.isUUID(channelId))
            throw new IllegalArgumentException("Invalid channel UUID: " + channelId);
        chns.add(channelId);
        return this;
    }

    public G4JBot removeListeningServerId(String serverId)
    {
        if (serverId == null || serverId.isEmpty())
            throw new IllegalArgumentException("Invalid server ID: " + serverId);
        svrs.remove(serverId);
        return this;
    }

    public G4JBot removeListeningChannelId(String channelId)
    {
        if (channelId == null || channelId.isEmpty() || !Util.isUUID(channelId))
            throw new IllegalArgumentException("Invalid channel UUID: " + channelId);
        chns.remove(channelId);
        return this;
    }

    public String[] getListeningServerIds()
    {
        return svrs.toArray(new String[svrs.size()]);
    }

    public String[] getListeningChannelIds()
    {
        return chns.toArray(new String[chns.size()]);
    }

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

    @Override
    public G4JBot setVerbose(boolean status)
    {
        super.setVerbose(status);
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
}
