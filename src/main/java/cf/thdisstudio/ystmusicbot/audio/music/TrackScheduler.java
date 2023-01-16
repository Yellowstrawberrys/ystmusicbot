package cf.thdisstudio.ystmusicbot.audio.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    public final BlockingQueue<AudioTrack> queue;
    public LoopType loop;
    public BlockingQueue<AudioTrack> all;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void queue(AudioTrack track) {
        if (loop == LoopType.All && !all.contains(track)){
            this.all.offer(track);
        }else if (!this.player.startTrack(track, true)) {
            this.queue.offer(track);
        }
    }

    private void queueN(AudioTrack track) {
        if (!this.player.startTrack(track, true)) {
            this.queue.offer(track);
        }
    }

    public LoopType setLoopMode(LoopType l) {
        all = queue;
        loop = l;
        return l;
    }

    public void nextTrack() {
        this.player.startTrack(this.queue.poll(), false);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            if(loop == LoopType.One){
                this.player.startTrack(track.makeClone(), false);
                return;
            }else if(loop == LoopType.All){
                if(queue.isEmpty()) {
                    for(AudioTrack a : new ArrayList<>(all))
                        queueN(a);
                }
            }
            nextTrack();
        }
    }
}
