package com.Bllod100.zpu.core;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.Log;

public class ZpuAudioFeedback {
    private static ToneGenerator toneGenerator;

    private static synchronized ToneGenerator getToneGenerator() {
        if (toneGenerator == null) {
            try {
                toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90);
            } catch (Exception e) {
                Log.e("ZPU_AUDIO", "Failed to initialize ToneGenerator", e);
            }
        }
        return toneGenerator;
    }

    public static void playConnectSound() {
        try {
            ToneGenerator tg = getToneGenerator();
            if (tg != null) {
                tg.startTone(ToneGenerator.TONE_PROP_ACK, 150);
                new Thread(() -> {
                    try { Thread.sleep(180); } catch (Exception ignored) {}
                    try { tg.startTone(ToneGenerator.TONE_DTMF_1, 200); } catch (Exception ignored) {}
                }).start();
            }
        } catch (Exception ignored) {}
    }

    public static void playDisconnectSound() {
        try {
            ToneGenerator tg = getToneGenerator();
            if (tg != null) {
                tg.startTone(ToneGenerator.TONE_DTMF_3, 200);
                new Thread(() -> {
                    try { Thread.sleep(220); } catch (Exception ignored) {}
                    try { tg.startTone(ToneGenerator.TONE_PROP_NACK, 250); } catch (Exception ignored) {}
                }).start();
            }
        } catch (Exception ignored) {}
    }

    public static void playSwitchSound(int switchIndex) {
        try {
            ToneGenerator tg = getToneGenerator();
            if (tg != null) {
                int toneType = ToneGenerator.TONE_PROP_BEEP;
                switch (switchIndex % 6) {
                    case 0: toneType = ToneGenerator.TONE_PROP_BEEP; break;
                    case 1: toneType = ToneGenerator.TONE_DTMF_1; break;
                    case 2: toneType = ToneGenerator.TONE_DTMF_2; break;
                    case 3: toneType = ToneGenerator.TONE_DTMF_3; break;
                    case 4: toneType = ToneGenerator.TONE_PROP_ACK; break;
                    case 5: toneType = ToneGenerator.TONE_PROP_PROMPT; break;
                }
                tg.startTone(toneType, 80);
            }
        } catch (Exception ignored) {}
    }
}
