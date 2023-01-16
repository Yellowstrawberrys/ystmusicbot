package cf.thdisstudio.ystmusicbot.audio.util;

import com.sedmelluq.discord.lavaplayer.format.OpusAudioDataFormat;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.*;

import static net.dv8tion.jda.api.audio.OpusPacket.*;

public class SampleRateConverter {

    public byte[] convert(byte[] b, AudioFormat old, AudioFormat neu) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(neu, new AudioInputStream(new ByteArrayInputStream(b), old, b.length));
            return ais.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] convertToOpus(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.allocateDirect(bytes.length);
        bb.put(bytes).order(ByteOrder.LITTLE_ENDIAN);
        return new com.sedmelluq.discord.lavaplayer.format.transcoder.OpusChunkEncoder(new AudioConfiguration(), new OpusAudioDataFormat(OPUS_CHANNEL_COUNT, OPUS_SAMPLE_RATE, OPUS_FRAME_SIZE))
                .encode(bb.asShortBuffer());
    }
}
