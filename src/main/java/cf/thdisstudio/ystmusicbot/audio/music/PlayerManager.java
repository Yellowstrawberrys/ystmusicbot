package cf.thdisstudio.ystmusicbot.audio.music;

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public final class PlayerManager {
    private static PlayerManager INSTANCE;

    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;

    public PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);

        this.audioPlayerManager.getConfiguration().setOutputFormat(StandardAudioDataFormats.DISCORD_PCM_S16_BE);
    }

    public GuildMusicManager getMusicManager(Guild guild){
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);

            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());

            return guildMusicManager;
        });
    }

    public void loadAndPlay(SlashCommandInteractionEvent event, Guild g, String trackUrl){
        final GuildMusicManager musicManager = this.getMusicManager(g);
        this.audioPlayerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.scheduler.queue(track);
                if(event != null) {
                    final EmbedBuilder em = new EmbedBuilder()
                            .setTitle("????????? ??????????????? ?????????", track.getInfo().uri)
                            .setColor(Color.yellow)
                            .addField("??????/?????????", track.getInfo().title + " / " + track.getInfo().author, false);
                    event.replyEmbeds(em.build()).queue();
                }else {

                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                new Thread(() -> {
                    for (AudioTrack t : playlist.getTracks()) {
                        musicManager.scheduler.queue(t);
                    }
                    if(event != null) {
                        final EmbedBuilder em = new EmbedBuilder()
                                .setTitle("????????? ???????????? ??????????????? ?????????")
                                .setColor(Color.yellow)
                                .addField("????????? ????????? ??????:", "??????:" + playlist.getName() + " / " + (playlist.getTracks().size() + 1) + "?????? ???", false);
                        event.replyEmbeds(em.build()).queue();
                    }else {

                    }
                }).start();
            }

            @Override
            public void noMatches() {
                if(event != null) {
                    final EmbedBuilder em = new EmbedBuilder()
                            .setTitle("??????")
                            .setColor(Color.red)
                            .addField("????????? ???????????? ????????? ?????? ??? ????????????", "???????????? ???????????? ????????? ?????? ??? ?????????, ????????? ???????????? ????????????", false);
                    event.replyEmbeds(em.build()).queue();
                }else {

                }
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                if(event != null) {
                    final EmbedBuilder em = new EmbedBuilder()
                            .setTitle("??????")
                            .setColor(Color.red)
                            .addField("????????? ???????????? ????????? ?????? ??? ????????????", "??? ??? ?????? ????????? ?????????????????????", false);
                    event.replyEmbeds(em.build()).queue();
                }else {

                }
            }
        });
    }
    public static PlayerManager getInstance(){
        if(INSTANCE == null){
            INSTANCE = new PlayerManager();
        }


        return  INSTANCE;
    }
}
