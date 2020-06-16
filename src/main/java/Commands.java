import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Commands {
    private static List<String> commands = Arrays.asList("help");

    public void owner(MessageReceivedEvent event) {
        event.getChannel().sendMessage("This app was created by Steve").queue();
    }

    public void help(MessageReceivedEvent event) {
        event.getChannel().sendMessage(this.commands.toString()).queue();
    }

    public void kick(MessageReceivedEvent event, String userToken) {
        if(event.getMember().hasPermission(Permission.KICK_MEMBERS)) {
            try {
                userToken = userToken.substring(3, userToken.length()-1);
                event.getGuild().kick(userToken).queue();
                System.out.println("Kicked " + userToken);
            } catch(Exception e) {
                System.out.println("Could not kick user " + userToken);
            }
        }
    }
}
