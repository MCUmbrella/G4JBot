package vip.floatationdevice.g4jbot;

import com.google.common.eventbus.Subscribe;
import vip.floatationdevice.guilded4j.event.GuildedWebSocketClosedEvent;
import vip.floatationdevice.guilded4j.object.ChatMessage;
import vip.floatationdevice.guilded4j.object.Embed;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Scanner;

public class G4JBotTest
{
    public static void main(String[] args)
    {
        String token, serverId, channelId;
        Scanner scanner = new Scanner(System.in);
        System.out.print("token: ");
        token = scanner.nextLine();
        System.out.print("serverId: ");
        serverId = scanner.nextLine();
        System.out.print("channelId: ");
        channelId = scanner.nextLine();

        System.out.println("Starting G4JBot");
        G4JBot b = new G4JBot(token);
        b.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 59909)));
        b.setVerbose(true)
                .setCommandPrefix("/")
                .addListeningServerId(serverId)
                .addListeningChannelId(channelId)
                .registerCommand(new GuildedCommandExecutor()
                {
                    @Override
                    public String getCommandName()
                    {
                        return "test";
                    }

                    @Override
                    public void onCommand(G4JBot bot, ChatMessage msg, String[] args)
                    {
                        try
                        {
                            bot.getChatMessageManager().createChannelMessage(msg.getChannelId(),
                                    null,
                                    new Embed[]{new Embed().setTitle("Test OK").setDescription("normal\n*italic*\n**bold**\n~~delete~~\n`code`")},
                                    new String[]{msg.getId()},
                                    null,
                                    null
                            );
                        }
                        catch (Exception e)
                        {
                            System.err.println("Operation failed:");
                            e.printStackTrace();
                        }
                    }
                })
                .registerCommand(new GuildedCommandExecutor()
                {
                    @Override
                    public String getCommandName()
                    {
                        return "repeat";
                    }

                    @Override
                    public void onCommand(G4JBot bot, ChatMessage msg, String[] args)
                    {
                        try
                        {
                            if (args.length != 0)
                            {
                                bot.getChatMessageManager().createChannelMessage(msg.getChannelId(),
                                        msg.getContent().substring(bot.getCommandPrefix().length() + getCommandName().length() + 1),
                                        null,
                                        null,
                                        null,
                                        null
                                );
                            }
                        }
                        catch (Exception e)
                        {
                            System.err.println("Operation failed:");
                            e.printStackTrace();
                        }
                    }
                })
                .registerCommand(new GuildedCommandExecutor()
                {
                    @Override
                    public String getCommandName()
                    {
                        return "exit";
                    }

                    @Override
                    public void onCommand(G4JBot bot, ChatMessage msg, String[] args)
                    {
                        System.out.println(msg.getCreatorId() + " issued command /exit");
                        bot.disconnectWebSocket(true);
                    }
                })
                .registerEventListener(new Object()
                {
                    @Subscribe
                    public void onDisconnected(GuildedWebSocketClosedEvent e)
                    {
                        System.out.println("Connection closed. Exiting");
                    }
                })
                .connectWebSocket(true, null);
        System.out.println("G4JBot started");
    }
}
