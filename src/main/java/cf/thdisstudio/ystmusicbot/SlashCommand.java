package cf.thdisstudio.ystmusicbot;

import cf.thdisstudio.ystmusicbot.google.youtube;
import cf.thdisstudio.ystmusicbot.audio.music.LoopType;
import cf.thdisstudio.ystmusicbot.audio.music.PlayerManager;
import cf.thdisstudio.ystmusicbot.audio.voicerecognition.AudioInputListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlashCommand extends ListenerAdapter {

    Map<Long, AudioInputListener> sessions = new HashMap<>();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "play" -> {
                String val = event.getOption("context").getAsString();
                try {
                    if (!event.getGuild().getSelfMember().getVoiceState().inAudioChannel() || !event.getGuild().getSelfMember().getVoiceState().getChannel().equals(event.getGuild().getMemberById(event.getUser().getId()).getVoiceState().getChannel())) {
                        event.getGuild().getAudioManager().openAudioConnection(event.getMember().getVoiceState().getChannel());
                    }
                    //주소 인지 아닌지 확인
                    if (val.startsWith("http"))
                        PlayerManager.getInstance()
                                .loadAndPlay(event, event.getGuild(), val);
                    else {
                        List<String> si = youtube.info(val);
                        PlayerManager.getInstance()
                                .loadAndPlay(event, event.getGuild(), "https://www.youtube.com/watch?v=" + si.get(3));
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    if (PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.isPaused())
                        PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.setPaused(false);
                } catch (Exception e) {
                    event.replyEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle("오류").addField("오류 코드", e.toString(), false).build()).queue();
                }
            }
            case "skip" -> {
                EmbedBuilder em1 = new EmbedBuilder();
                try {
                    PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.nextTrack();
                    em1.setTitle("🎵 음악").setColor(Color.yellow).addField("✅ 성공적으로 다음 음악으로 건너뛰었습니다", "🎶 플레이중인 음악: " + PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().title, false);
                } catch (Exception e) {
                    em1.setTitle("🎵 음악").setColor(Color.RED).addField("❌ 다음 음악이 없습니다", " ", false);
                }
                event.replyEmbeds(em1.build()).queue();
            }
            case "stop" -> {
                EmbedBuilder em = new EmbedBuilder();
                try {
                    resetAudioHandlers(event.getGuild());
                    event.getGuild().getAudioManager().closeAudioConnection();
                    em.addField("🎵 음악", "음악을 정지하였어요!", false);
                    event.replyEmbeds(em.build()).queue();
                } catch (Exception e) {
                    event.getGuild().getAudioManager().closeAudioConnection();
                }
            }
            case "loop" -> {
                EmbedBuilder em = new EmbedBuilder().setTitle("🎵 음악");
                try {
                    LoopType curr = PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.loop;
                    em.addField("반복 설정", "반복을 **"+(PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.setLoopMode(curr != LoopType.None ? LoopType.None : LoopType.All))+"**으로 설정하였어요!", false);
                }catch (Exception e){
                    e.printStackTrace();
                }
                event.replyEmbeds(em.build()).queue();
            }
            case "join" -> {
                event.getGuild().getAudioManager().openAudioConnection(event.getMember().getVoiceState().getChannel());
            }
        }
    }

    @Override
    public void onGenericGuildVoice(@NotNull GenericGuildVoiceEvent event) {
        if(event.getMember().equals(event.getGuild().getSelfMember())) {
            if (event.getVoiceState().inAudioChannel()) {
                AudioInputListener ail = new AudioInputListener(event.getGuild());
                sessions.put(event.getGuild().getIdLong(), ail);
                event.getGuild().getAudioManager().setReceivingHandler(ail);
            }else if (sessions.containsKey(event.getGuild().getIdLong())) {
                sessions.get(event.getGuild().getIdLong()).shutdown();
            }
        } else if(!event.getVoiceState().inAudioChannel() && event.getGuild().getAudioManager().getConnectedChannel().getMembers().size() < 2) {
            resetAudioHandlers(event.getGuild());
            sessions.get(event.getGuild().getIdLong()).shutdown();
            event.getGuild().getAudioManager().closeAudioConnection();
        }
    }

    public static void resetAudioHandlers(Guild g) {
        PlayerManager.getInstance().getMusicManager(g).audioPlayer.stopTrack();
        PlayerManager.getInstance().getMusicManager(g).scheduler.queue.clear();
        if(PlayerManager.getInstance().getMusicManager(g).scheduler.all != null) PlayerManager.getInstance().getMusicManager(g).scheduler.all.clear();
        PlayerManager.getInstance().getMusicManager(g).scheduler.loop = LoopType.None;
    }
}
