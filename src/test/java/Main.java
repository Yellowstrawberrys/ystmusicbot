import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        Voice voice = VoiceManager.getInstance().getVoice("kevin16");
        voice.allocate();
        voice.setRate(160);
        voice.setPitch(140);
        voice.setVolume(6);
        voice.speak("Playing High Hopes by Someone");
        Thread.sleep(5000);
    }
}
