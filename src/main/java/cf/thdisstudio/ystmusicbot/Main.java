package cf.thdisstudio.ystmusicbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.vosk.Model;

import java.io.IOException;

public class Main {
    public static Model model;

    static {
        try {
            model = new Model("C:\\Users\\Yellowstrawberry\\IdeaProjects\\YSTMusicBot\\src\\main\\resources\\en");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static JDA jda;

    public static void main(String[] args) {
        JDABuilder builder = JDABuilder.createDefault(args[0]);

        // Enable the bulk delete event
        builder.setBulkDeleteSplittingEnabled(false);
        // Set activity (like "playing Something")
        builder.setActivity(Activity.listening("Music"));
        // Adding Slash Command
        builder.addEventListeners(new SlashCommand());
        // Adding Audio
        builder.enableIntents(GatewayIntent.GUILD_VOICE_STATES).enableCache(CacheFlag.VOICE_STATE);

        jda = builder.build();

        jda.updateCommands().addCommands(
                Commands.slash("play", "Play a song")
                        .setGuildOnly(true)
                        .addOption(OptionType.STRING, "context", "Enter name of song or URL of song", true),
                Commands.slash("skip", "Skip a song"),
                Commands.slash("stop", "Stop a song"),
                Commands.slash("loop", "Set Loop"),
                Commands.slash("join", "Join"),
                Commands.slash("test", "test")
        ).queue();
    }
}
