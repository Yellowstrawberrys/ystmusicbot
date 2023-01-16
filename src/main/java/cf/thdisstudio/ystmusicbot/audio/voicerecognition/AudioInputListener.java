package cf.thdisstudio.ystmusicbot.audio.voicerecognition;

import cf.thdisstudio.ystmusicbot.audio.music.AudioPlayerSendHandler;
import cf.thdisstudio.ystmusicbot.audio.music.PlayerManager;
import cf.thdisstudio.ystmusicbot.audio.util.SampleRateConverter;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.*;
import java.io.*;
import java.util.*;

public class AudioInputListener implements AudioReceiveHandler {

    public final Guild g;
    public final SampleRateConverter converter = new SampleRateConverter();
    private final VoiceDetector detector = new VoiceDetector(this);
    private final PipedOutputStream aos = new PipedOutputStream();
    private final Sounds sounds = new Sounds(converter);
    private final AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,16000.0F, 16, 1, 2, 16000.0F, false);

    public AudioInputListener(Guild g) {
        this.g = g;
        try {
            detector.getAis().connect(aos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        detector.start();
    }

    @Override
    public boolean canReceiveCombined() {
        return true;
    }

    @Override
    public void handleCombinedAudio(@NotNull CombinedAudio combinedAudio) {
        AudioReceiveHandler.super.handleCombinedAudio(combinedAudio);
        try {
            aos.write(converter.convert(combinedAudio.getAudioData(1), AudioReceiveHandler.OUTPUT_FORMAT, format));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void playSoundN(Collection<byte[]> audios) {
        PlayerManager.getInstance().getMusicManager(g).getSendHandler().playAdditionalAudio(audios);
    }

    public void playSound(InputStream audio) {
        playSoundN(sounds.splitSound(audio));
    }

    public void playSound(byte[] audio) {
        playSound(new ByteArrayInputStream(audio));
    }

    public void playSoundById(int id) {
        playSoundN(sounds.getSound(id));
    }

    public void shutdown() {

    }
}
class Sounds {
    private int bytePer20ms;
    private final List<Collection<byte[]>> sounds = new ArrayList<>();

    public Sounds(SampleRateConverter converter) {
        try {
            AudioFormat format = AudioSystem.getAudioFileFormat(Sounds.class.getResourceAsStream("/sound/listening.wav")).getFormat();
            bytePer20ms = (int) ((((AudioPlayerSendHandler.INPUT_FORMAT.getSampleSizeInBits() * AudioPlayerSendHandler.INPUT_FORMAT.getSampleRate() * AudioPlayerSendHandler.INPUT_FORMAT.getChannels()) / 1000) * 20) / 8);
            //                                                                                                                                                                                             ^^^^    ^^    ^
            //                                                                                                                                                                       [one second in millisecond] [20ms] [bytes in bit]
            sounds.add(
                    splitSound(
                            new ByteArrayInputStream(
                                    converter.convert(
                                            AudioSystem.getAudioInputStream(Sounds.class.getResourceAsStream("/sound/listening.wav")).readAllBytes(),
                                            format,
                                            AudioPlayerSendHandler.INPUT_FORMAT
                                    )
                            )
                    )
            );
            sounds.add(
                    splitSound(
                            new ByteArrayInputStream(
                                    converter.convert(
                                            AudioSystem.getAudioInputStream(Sounds.class.getResourceAsStream("/sound/ok.wav")).readAllBytes(),
                                            format,
                                            AudioPlayerSendHandler.INPUT_FORMAT
                                    )
                            )
                    )
            );
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Collection<byte[]> splitSound(InputStream ipt) {
        List<byte[]> collection = new ArrayList<>();
        try {
            byte[] buffer = new byte[bytePer20ms];
            while (ipt.read(buffer) != -1) {
                collection.add(buffer);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return collection;
    }

    private byte[] adjustVolume(byte[] audioSamples, float volume) {
        byte[] array = new byte[audioSamples.length];
        for (int i = 0; i < array.length; i+=2) {
            // convert byte pair to int
            short buf1 = audioSamples[i+1];
            short buf2 = audioSamples[i];

            buf1 = (short) ((buf1 & 0xff) << 8);
            buf2 = (short) (buf2 & 0xff);

            short res= (short) (buf1 | buf2);
            res = (short) (res * volume);

            // convert back
            array[i] = (byte) res;
            array[i+1] = (byte) (res >> 8);

        }
        return array;
    }

    public Collection<byte[]> getSound(int id) {
        return sounds.get(id);
    }
}