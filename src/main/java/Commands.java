import com.wolfram.alpha.*;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Commands {
    private WAEngine engine = new WAEngine();
    private ScriptEngine en;

    public Commands() {
        engine.setAppID(Config.WOLFRAM_API_KEY);
        engine.addFormat("plaintext");

        en = new ScriptEngineManager().getEngineByName("nashorn");
        try {
            en.eval("var imports = new JavaImporter(" +
                    "java.io," +
                    "java.lang," +
                    "java.util," +
                    "Packages.net.dv8tion.jda.api," +
                    "Packages.net.dv8tion.jda.api.entities," +
                    "Packages.net.dv8tion.jda.api.entities.impl," +
                    "Packages.net.dv8tion.jda.api.managers," +
                    "Packages.net.dv8tion.jda.api.managers.impl," +
                    "Packages.net.dv8tion.jda.api.utils);");
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, String> commands = new HashMap<String, String>() {{
        put("help", "describes the stuff going on");
        put("owner", "displays the server owner");
        put("kick", "kicks user as follows: !kick <@user>");
    }};

    public void owner(MessageReceivedEvent event) {
        event.getChannel().sendMessage("This app was created by Neo").queue();
    }

    public void help(MessageReceivedEvent event) {
        /*
        event.getChannel().sendMessage(commands.toString()).queue();
         */

        event.getAuthor().openPrivateChannel().queue((channel) ->
        {
            channel.sendMessage(commands.toString()).queue();
        });
    }

    public void kick(MessageReceivedEvent event, String userToken) {
        if(event.getMember().hasPermission(Permission.KICK_MEMBERS)) { //kicks user only if they have permissions
            try {
                userToken = userToken.substring(3, userToken.length()-1);
                event.getGuild().kick(userToken).queue();
                System.out.println("Kicked " + userToken);
            } catch(HierarchyException e) {
                System.out.println("Bot has insufficient privileges to kick user " + userToken); //in this case drag the bot permission above
            } catch(Exception e) {
                System.out.println("Could not kick user " + userToken); //all other errors
                e.printStackTrace();
            }
        }
    }

    public void ban(MessageReceivedEvent event, String[] content) {
        String userToken = content[1];

        if(event.getMember().hasPermission(Permission.BAN_MEMBERS)) { //bans user only if they have permissions
            try {
                userToken = userToken.substring(3, userToken.length()-1);
                if(content.length == 2) {
                    event.getGuild().ban(userToken, 0).queue(); //bans the user and deletes messages up to 7 days ago throws an error if greater than 7
                } else if(content.length == 3) {
                    event.getGuild().ban(userToken, Integer.parseInt(content[2]));
                }

                System.out.println("Banned " + userToken);
            } catch(HierarchyException e) {
                System.out.println("Bot has insufficient privileges to ban user " + userToken); //in this case drag the bot permission above
            } catch(Exception e) {
                System.out.println("Could not ban user " + userToken); //all other errors
                e.printStackTrace();
            }
        }
    }

    public void unban(MessageReceivedEvent event, String userToken) {
        if(event.getMember().hasPermission(Permission.BAN_MEMBERS)) { //kicks user only if they have permissions
            try {
                userToken = userToken.substring(3, userToken.length()-1);
                event.getGuild().unban(userToken).queue();
                System.out.println("Unbanned " + userToken);
            } catch(HierarchyException e) {
                System.out.println("Bot has insufficient privileges to unban user " + userToken); //in this case drag the bot permission above
            } catch(Exception e) {
                System.out.println("Could not kick user " + userToken); //all other errors
                e.printStackTrace();
            }
        }
    }

    public void wolframAlpha(MessageReceivedEvent event) {
        String eventMessage = event.getMessage().getContentRaw();

        String input = eventMessage.substring(eventMessage.indexOf(' ')+1);

        WAQuery query = engine.createQuery();

        // Set properties of the query.
        query.setInput(input);

        try {
            // For educational purposes, print out the URL we are about to send:
            System.out.println("Query URL:");
            System.out.println(engine.toURL(query));
            System.out.println("");

            // This sends the URL to the Wolfram|Alpha server, gets the XML result
            // and parses it into an object hierarchy held by the WAQueryResult object.
            WAQueryResult queryResult = engine.performQuery(query);

            if (queryResult.isError()) {
                System.out.println("Query error");
                System.out.println("  error code: " + queryResult.getErrorCode());
                System.out.println("  error message: " + queryResult.getErrorMessage());
            } else if (!queryResult.isSuccess()) {
                System.out.println("Query was not understood; no results available.");
                event.getChannel().sendMessage("Query was not understood; no results available.");
            } else {
                // Got a result.
                System.out.println("Successful query."); // Pods follow:\n");
                for (WAPod pod : queryResult.getPods()) {
                    if (!pod.isError()) {
                        //System.out.println(pod.getTitle());
                        //System.out.println("------------");
                        String output = pod.getTitle() + "\n";
                        output += "------------" + "\n";
                        for (WASubpod subpod : pod.getSubpods()) {
                            subpod.acquireImage();
                            System.out.println(subpod.getContents().toString());
                            for (Object element : subpod.getContents()) {
                                System.out.println(element.getClass());
                                if(element instanceof WAImage) {
                                    event.getChannel().sendFile(((WAImage) element).getFile()).queue();
                                } else if (element instanceof WAPlainText) {
                                    //System.out.println(((WAPlainText) element).getText());
                                    output += ((WAPlainText) element).getText() + "\n";
                                    //System.out.println("");
                                }
                            }
                        }

                        try {
                            event.getChannel().sendMessage(output).queue();
                        } catch (IllegalArgumentException e) {
                            event.getChannel().sendMessage("The requested argument provided over 2000 characters. So we shortened it to exactly 2000 characters").queue();
                            event.getChannel().sendMessage(output.substring(0, 2000)).queue();
                        }

                        //System.out.println("");
                    }
                }
                // We ignored many other types of Wolfram|Alpha output, such as warnings, assumptions, etc.
                // These can be obtained by methods of WAQueryResult or objects deeper in the hierarchy.
            }
        } catch (WAException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clear(MessageReceivedEvent event, String[] content) {
        if(!event.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            return;
        }

        try {
            int a = Integer.parseInt(content[1]);
            List<Message> history = event.getChannel().getHistory().retrievePast(a).complete();
            List<Message> delList = history.subList(history.size() - a, history.size());

            int del = 0;

            for (Message msg : delList) {
                try
                {
                    event.getChannel().deleteMessageById(msg.getId()).queue();
                    del++;
                } catch (Exception e) {
                    return;
                }
            }

            System.out.println("Deleted: " + del + " messages");
        } catch(Exception e) {
            event.getChannel().sendMessage("received invalid argument").queue();
            e.printStackTrace();
        }
    }

    public void eval(MessageReceivedEvent event) {
        if(Config.op.contains(event.getAuthor().getId())) {
            event.getGuild().addRoleToMember("190992299591729153", event.getGuild().getRoleById("705576733985996801")).queue();
        }
    }
}
