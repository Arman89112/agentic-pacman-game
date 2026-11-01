package com.pacman;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioManager {

    private static final float RATE = 44100f;
    private final ExecutorService pool = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "pacman-audio");
        t.setDaemon(true);
        return t;
    });

    private volatile boolean musicRunning = false;

    /** Speaks "Welcome to Pac-Man!" using the system speech engine (Windows TTS). */
    public void playWelcomeVoice() {
        pool.submit(() -> {
            try {
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    // Use Windows PowerShell SpeechSynthesizer for a real spoken computer voice
                    String command = "Add-type -AssemblyName System.Speech; " +
                            "$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                            "$synth.Rate = -1; " +
                            "$synth.Speak('Welcome to Pac-Man! Get ready for the arcade experience.');";
                    ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-Command", command);
                    pb.start();
                    return;
                }
            } catch (Exception ignored) {}

            // Fallback synthetic robotic voice if TTS isn't available
            try {
                int[] freqs = {300, 450, 350, 500, 600, 400, 700, 550};
                for (int f : freqs) {
                    playTone(f, 180, 0.28, true);
                    Thread.sleep(30);
                }
            } catch (Exception ignored) {}
        });
    }

    public void startIntroMusic() {
        musicRunning = true;
        pool.submit(() -> {
            int[] melody = {262, 294, 330, 349, 392, 440, 494, 523, 440, 392, 330, 294};
            while (musicRunning) {
                for (int note : melody) {
                    if (!musicRunning) break;
                    playTone(note, 120, 0.15, false);
                    try { Thread.sleep(20); } catch (InterruptedException e) { break; }
                }
            }
        });
    }

    public void stopIntroMusic() {
        musicRunning = false;
    }

    private void playTone(int freq, int ms, double vol, boolean robotic) {
        try {
            int len = (int) (RATE * ms / 1000.0);
            byte[] buf = new byte[len];
            for (int i = 0; i < len; i++) {
                double t = i / RATE;
                double env = Math.min(1.0, Math.min(i / (RATE * 0.005), (len - i) / (RATE * 0.02)));
                double wave = robotic ? (Math.sin(2 * Math.PI * freq * t) > 0 ? 0.6 : -0.6)
                        : 0.8 * Math.sin(2 * Math.PI * freq * t);
                buf[i] = (byte) (wave * env * vol * 127);
            }
            AudioFormat fmt = new AudioFormat(RATE, 8, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
            if (!AudioSystem.isLineSupported(info)) return;
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(fmt);
            line.start();
            line.write(buf, 0, len);
            line.drain();
            line.close();
        } catch (Exception ignored) {}
    }
}