package cf.thdisstudio.ystmusicbot.audio.util;

public class AudioMixer {
    public byte[] mix(byte[] a, byte[] b) {
        byte[] mixed = new byte[Math.max(a.length, b.length)-1];

        for (int i = 0; i < mixed.length; i++) {
            mixed[i] = (byte) (((a.length > i ? a[i] : 0) + (b.length > i ? b[i] : 0))/2);
        }
        return mixed;
    }
}
