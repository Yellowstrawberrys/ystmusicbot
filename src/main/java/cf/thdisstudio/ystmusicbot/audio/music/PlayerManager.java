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
                            .setTitle("음악이 성공적으로 추가됨", track.getInfo().uri)
                            .setColor(Color.yellow)
                            .addField("제목/업로더", track.getInfo().title + " / " + track.getInfo().author, false);
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
                                .setTitle("플레이 리스트가 성공적으로 추가됨")
                                .setColor(Color.yellow)
                                .addField("플레이 리스트 정보:", "이름:" + playlist.getName() + " / " + (playlist.getTracks().size() + 1) + "개의 곡", false);
                        event.replyEmbeds(em.build()).queue();
                    }else {

                    }
                }).start();
            }

            @Override
            public void noMatches() {
                if(event != null) {
                    final EmbedBuilder em = new EmbedBuilder()
                            .setTitle("음악")
                            .setColor(Color.red)
                            .addField("플레이 리스트에 음악을 넣을 수 없습니다", "해당되는 링크에서 음악을 찾을 수 없거나, 링크가 존제하지 않습니다", false);
                    event.replyEmbeds(em.build()).queue();
                }else {

                }
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                if(event != null) {
                    final EmbedBuilder em = new EmbedBuilder()
                            .setTitle("음악")
                            .setColor(Color.red)
                            .addField("플레이 리스트에 음악을 넣을 수 없습니다", "알 수 없는 오류가 발생하였습니다", false);
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
