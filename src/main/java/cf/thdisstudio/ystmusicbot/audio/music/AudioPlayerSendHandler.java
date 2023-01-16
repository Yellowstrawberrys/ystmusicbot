package cf.thdisstudio.ystmusicbot.audio.music;

import cf.thdisstudio.ystmusicbot.audio.util.AudioMixer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public final class AudioPlayerSendHandler implements AudioSendHandler {
    private final AudioPlayer audioPlayer;
    private final ByteBuffer buffer;
    private final MutableAudioFrame frame;
    private final Queue<byte[]> additionalAudio;
    private final AudioMixer mixer;
    private boolean wasItPaused = false;
    private boolean isCommanding = false;

    public AudioPlayerSendHandler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.buffer = ByteBuffer.allocate(3841);
        this.additionalAudio = new LinkedBlockingQueue<>();
        this.mixer = new AudioMixer();
        this.frame = new MutableAudioFrame();
        this.frame.setBuffer(buffer);
    }

    @Override
    public boolean canProvide() {
        return (this.audioPlayer.provide(this.frame) || !additionalAudio.isEmpty() || !isCommanding);
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        if(!additionalAudio.isEmpty()) {
            if(!audioPlayer.isPaused()) {
                wasItPaused = false;
                audioPlayer.setPaused(true);
            }
            return ByteBuffer.wrap(additionalAudio.poll());
        }else {
            if(!wasItPaused && audioPlayer.isPaused() && !isCommanding) {
                audioPlayer.setPaused(false);
                wasItPaused = true;
            }
            return this.buffer.flip();
        }
    }

    public void setCommanding(boolean is) {
        System.out.println(wasItPaused);
        this.isCommanding = is;
    }

    @Override
    public boolean isOpus() {
        return false;
    }

    public void playAdditionalAudio(Collection<byte[]> b) {
        additionalAudio.addAll(b);
    }
}
