package vip.floatationdevice.g4jbot;

import com.google.common.eventbus.Subscribe;
import vip.floatationdevice.guilded4j.G4JClient;
import vip.floatationdevice.guilded4j.event.ChatMessageCreatedEvent;
import vip.floatationdevice.guilded4j.object.ChatMessage;

import java.util.HashMap;
import java.util.HashSet;

public class G4JBot extends G4JClient
{
    private String pfx;
    private final HashSet<String> svrs = new HashSet<>();
    private final HashSet<String> chns = new HashSet<>();
    private final HashMap<String, GuildedCommandExecutor> cmds = new HashMap<>();

    public G4JBot(String token)
    {
        super(token);
    }

    public G4JBot(String token, String lastMessageId)
    {
        super(token, lastMessageId);
    }

    public G4JBot registerCommand(GuildedCommandExecutor executor)
    {
        if(pfx == null) throw new IllegalStateException("Command prefix not set");
        if(executor == null || executor.getCommandName() == null || executor.getCommandName().isEmpty())
            throw new IllegalArgumentException("Command is empty");
        cmds.put(executor.getCommandName(), executor);
        return this;
    }

    public G4JBot unregisterCommand(GuildedCommandExecutor executor)
    {
        for(GuildedCommandExecutor e : cmds.values())
            if(e == executor)
            {
                cmds.remove(e.getCommandName());
                return this;
            }
        throw new IllegalArgumentException("Command executor not found");
    }

    public G4JBot unregisterCommand(String cmd)
    {
        if(cmds.remove(cmd) != null)
            return this;
        throw new IllegalArgumentException("Command not found");
    }

    public G4JBot setCommandPrefix(String pfx)
    {
        if(pfx == null || pfx.isEmpty()) throw new IllegalArgumentException("Command must be presented");
        this.pfx = pfx;
        return this;
    }

    public String getCommandPrefix()
    {
        return pfx;
    }

    public G4JBot addListeningServerId(String serverId)
    {
        //TODO
    }

    public G4JBot addListeningChannelId(String channelId)
    {
        //TODO
    }

    @Subscribe
    private void onChatMessage(ChatMessageCreatedEvent e)
    {
        ChatMessage msg = e.getChatMessage();
        if(checkMessageLocation(msg) && msg.getContent().startsWith(pfx))
        {
            //TODO
        }
    }

    private boolean checkMessageLocation(ChatMessage msg) // check if the message is in the listening server and channel
    {
        if(svrs.size() == 0 && chns.size() == 0) return true; // true if listenServers & listenChannels are not set
        else if(svrs.size() == 0) // if listenServers is not set, only check channel
            return chns.contains(msg.getChannelId());
        else if(chns.size() == 0) // if listenChannels is not set, only check server
            return svrs.contains(msg.getServerId());
        else return svrs.contains(msg.getServerId()) && chns.contains(msg.getChannelId()); // check both if all set
    }
}
