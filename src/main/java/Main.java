import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

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

        if(!event.getMessage().getContentRaw().startsWith("!")) {
            return;
        }

        try {
            String[] content = event.getMessage().getContentRaw().split("\\s+"); //splits content by whitespace
            String command = content[0].substring(1); //gets everything past the prefix
            command = command.toLowerCase(); //changes the command to lowercase so caps don't mess it up

            System.out.println(Arrays.asList(command, Arrays.toString(content))); //prints out the command and its content split


            switch(command) {
                case "help": cmd.help(event); break;
                case "owner": cmd.owner(event); break;
                case "kick": cmd.kick(event, content[1]); break;
                case "ban": cmd.ban(event, content); break;
                case "unban": cmd.unban(event, content[1]); break;
                case "solve": cmd.wolframAlpha(event); break;
            }
        } catch(Exception e) {
            System.out.println("Invalid command");
        }

        //event.getChannel().sendMessage("Hello World").queue();
    }
}
