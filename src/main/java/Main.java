import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.util.Arrays;

public class Main extends ListenerAdapter {
    private Commands cmd = new Commands();

    public static void main(String[] args) throws LoginException {
        JDABuilder builder = new JDABuilder(AccountType.BOT).setToken(Config.BOT_TOKEN);
        builder.addEventListeners(new Main());
        builder.build();
    }

    @Override
    public void onReady(ReadyEvent event) {
        super.onReady(event);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        System.out.println("Execution [author, channel]: " + Arrays.asList(event.getAuthor(), event.getChannel()));
        if(event.getAuthor().isBot()) {
            return;
        }

        System.out.println("Message content: " + event.getMessage().getContentRaw());

        try {
            String[] content = event.getMessage().getContentRaw().split(" ");
            String command = content[0].substring(1);

            System.out.println(Arrays.asList(command, content));

            switch(command) {
                case "help": cmd.help(event); break;
                case "owner": cmd.owner(event); break;
                case "kick": cmd.kick(event, content[1]);
            }
        } catch(Exception e) {
            System.out.println("Invalid command");
        }

        //event.getChannel().sendMessage("Hello World").queue();
    }
}
