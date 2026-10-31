package com.pacman;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Tiny synthesised sound engine -- no external assets required. Every tone is
 * generated on the fly and played on a single background thread. All audio is
 * best-effort: on machines without an audio line the calls simply do nothing.
 */
public class SoundManager {

    private static final float RATE = 44100f;

    private final ExecutorService pool = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "pacman-sound");
        t.setDaemon(true);
        return t;
    });

    private volatile boolean enabled = true;
    private long lastChomp;

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        enabled = !enabled;
    }

    public void chomp() {
        long now = System.currentTimeMillis();
        if (now - lastChomp < 110) {
            return;
        }
        lastChomp = now;
        async(() -> play(430 + (int) (System.nanoTime() % 90), 60, 0.22));
    }

    public void power() {
        async(() -> play(170, 240, 0.30));
    }

    public void eatGhost() {
        async(() -> play(760, 160, 0.34));
    }

    public void death() {
        async(() -> {
            int[] notes = {520, 470, 420, 360, 300, 250, 190, 140};
            for (int n : notes) {
                play(n, 110, 0.32);
            }
        });
    }

    public void levelUp() {
        async(() -> {
            int[] notes = {440, 550, 660, 880, 1100};
            for (int n : notes) {
                play(n, 110, 0.30);
            }
        });
    }

    private void async(Runnable r) {
        if (enabled) {
            pool.submit(r);
        }
    }

    private void play(int freq, int ms, double vol) {
        if (!enabled) {
            return;
        }
        try {
            int len = (int) (RATE * ms / 1000.0);
            byte[] buf = new byte[len];
            for (int i = 0; i < len; i++) {
                double t = i / RATE;
                double env = Math.min(1.0, Math.min(i / (RATE * 0.008), (len - i) / (RATE * 0.03)));
                double wobble = Math.sin(2 * Math.PI * freq * t);
                buf[i] = (byte) (wobble * env * vol * 127);
            }
            AudioFormat fmt = new AudioFormat(RATE, 8, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
            if (!AudioSystem.isLineSupported(info)) {
                return;
            }
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(fmt);
            line.start();
            line.write(buf, 0, len);
            line.drain();
            line.close();
        } catch (Exception ignored) {
            // audio is optional
        }
    }
}
