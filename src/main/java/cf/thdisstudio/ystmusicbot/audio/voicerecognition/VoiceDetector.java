package cf.thdisstudio.ystmusicbot.audio.voicerecognition;

import cf.thdisstudio.ystmusicbot.Main;
import cf.thdisstudio.ystmusicbot.SlashCommand;
import cf.thdisstudio.ystmusicbot.audio.music.PlayerManager;
import cf.thdisstudio.ystmusicbot.google.youtube;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import org.json.JSONObject;
import org.vosk.Recognizer;

import java.io.IOException;
import java.io.PipedInputStream;
import java.util.List;

public class VoiceDetector extends Thread {

    private AudioInputListener root;
    private PipedInputStream ais = new PipedInputStream();

    public VoiceDetector(AudioInputListener root) {
        this.root = root;
    }

    boolean running = true;
    boolean isCommanding = false;

    @Override
    public void run() {
        try (Recognizer recognizer = new Recognizer(Main.model, 16000)) {
            while (running) {
                int nbytes;
                byte[] b = new byte[4096];
                try {
                    while ((nbytes = ais.read(b)) >= 0) {
                        if(recognizer.acceptWaveForm(b, nbytes)) {
                            if(isCommanding) {
                                root.playSoundById(1);

                                JSONObject obj = new JSONObject(recognizer.getFinalResult());
                                String command = obj.getString("text").split(" ")[0];
                                String suffix = obj.getString("text").substring(command.length());

                                if (command.startsWith("play")) {
                                    List<String> si = youtube.info(suffix);
                                    PlayerManager.getInstance()
                                            .loadAndPlay(null, root.g, "https://www.youtube.com/watch?v=" + si.get(3));
                                }else if (command.startsWith("stop")) {
                                    SlashCommand.resetAudioHandlers(root.g);
                                }else if (command.startsWith("skip")) {
                                    PlayerManager.getInstance().getMusicManager(root.g).scheduler.nextTrack();
                                }else if (command.startsWith("pause")) {
                                    AudioPlayer p = PlayerManager.getInstance().getMusicManager(root.g).audioPlayer;
                                    p.setPaused(!p.isPaused());
                                }

                                PlayerManager.getInstance().getMusicManager(root.g).getSendHandler().setCommanding(false);
                                isCommanding = false;
                            }else {
                                String r = recognizer.getPartialResult().replaceAll("\n", "");
                                if (r.equals("{  \"partial\" : \"hey strawberry\"}") ||
                                                r.equals("{  \"partial\" : \"a strawberry\"}")) {
                                    root.playSoundById(0);
                                    PlayerManager.getInstance().getMusicManager(root.g).getSendHandler().setCommanding(true);
                                    isCommanding = true;
                                }
                            }
                            recognizer.reset();
                        }
                    }

                }catch (Exception e){}
            }
            recognizer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        running = false;
    }

    public PipedInputStream getAis() {
        return ais;
    }
}
