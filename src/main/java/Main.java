import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.util.Arrays;

public class Main extends ListenerAdapter {
    private Commands cmd = new Commands();

    public static void main(String[] args) throws LoginException {
        try
        {
            JDA jda = new JDABuilder(Config.BOT_TOKEN)         // The token of the account that is logging in.
                    .addEventListeners(new Main())  // An instance of a class that will handle events.
                    .build();
            jda.awaitReady(); // Blocking guarantees that JDA will be completely loaded.
            System.out.println("Finished Building JDA!");
        }
        catch (LoginException e)
        {
            //If anything goes wrong in terms of authentication, this is the exception that will represent it
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            //Due to the fact that awaitReady is a blocking method, one which waits until JDA is fully loaded,
            // the waiting can be interrupted. This is the exception that would fire in that situation.
            //As a note: in this extremely simplified example this will never occur. In fact, this will never occur unless
            // you use awaitReady in a thread that has the possibility of being interrupted (async thread usage and interrupts)
            e.printStackTrace();
        }
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

        if(!event.getMessage().getContentRaw().startsWith(Config.PREFIX)) {
            return;
        }

        try {
            String[] content = event.getMessage().getContentRaw().trim().split("\\s+"); //splits content by whitespace
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
                case "eval": cmd.eval(event); break;
                case "clear": cmd.clear(event, content); break;
            }
        } catch(Exception e) {
            System.out.println("Invalid command");
        }

        //event.getChannel().sendMessage("Hello World").queue();
    }
}
