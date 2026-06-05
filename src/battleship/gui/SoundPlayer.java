/*
 * Battleship Tournament - ICS4U Final Summative
 * Group: CyberSurvyDogs - Olin Wang, Oson Wang, Willey Yao
 * Date: 2026
 * File: SoundPlayer.java
 *
 * Plays short sound effects and looping background music. Audio files live on
 * the classpath under /battleship/resources/sounds so they load the same way
 * whether the game runs from Eclipse or an exported jar.
 *
 * NOTE: Java's built-in javax.sound.sampled only decodes WAV/AIFF/AU - not MP3.
 * Convert any .mp3 effects to .wav before dropping them in the sounds folder.
 *
 * Everything fails silently: a missing file or unsupported format never crashes
 * the game, it just plays nothing.
 */
package battleship.gui;

import java.io.BufferedInputStream;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public final class SoundPlayer {

    private static final String DIR = "/battleship/resources/sounds/";

    /**
     * How much to attenuate one-shot effects, in decibels. The effect WAVs are
     * mastered hot (they sound bass-boosted at full volume), so we pull them
     * down. Negative is quieter; each -6 dB roughly halves perceived loudness.
     */
    private static final float EFFECT_GAIN_DB = -12.0f;

    /** Whether any audio plays at all (handy for a future mute button). */
    private static boolean enabled = true;

    /** The currently looping background track, so it can be stopped later. */
    private static Clip loopClip;

    private SoundPlayer() {} // utility class: no instances

    /** Turn all audio on or off. Turning off also stops background music. */
    public static void setEnabled(boolean on) {
        enabled = on;
        if (!on) stopLoop();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Play a one-shot effect once. name is the file's base name with no
     * extension, e.g. play("hit") loads /battleship/resources/sounds/hit.wav.
     * A fresh Clip is used each call so overlapping effects can play together.
     */
    public static void play(String name) {
        if (!enabled) return;
        try {
            Clip clip = load(name);
            if (clip == null) return;
            applyGain(clip, EFFECT_GAIN_DB);
            // Release the Clip's resources once playback finishes.
            clip.addLineListener(event -> {
                if (event.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                    event.getLine().close();
                }
            });
            clip.start();
        } catch (Exception e) {
            // Swallow: audio problems must never interrupt gameplay.
        }
    }

    /**
     * Start looping a track continuously (background music). Any track already
     * looping is stopped first, so calling this again just switches tracks.
     */
    public static void loop(String name) {
        if (!enabled) return;
        try {
            stopLoop();
            Clip clip = load(name);
            if (clip == null) return;
            loopClip = clip;
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            // Swallow: missing music should not break the game.
        }
    }

    /** Stop and release the looping background track, if any. */
    public static void stopLoop() {
        if (loopClip != null) {
            try {
                loopClip.stop();
                loopClip.close();
            } catch (Exception e) {
                // ignore
            }
            loopClip = null;
        }
    }

    /**
     * Attenuate (or boost) a Clip by the given decibel amount. Clamped to the
     * range the underlying line actually supports. Does nothing if the line has
     * no gain control, so playback still works on minimal audio systems.
     */
    private static void applyGain(Clip clip, float gainDb) {
        try {
            if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) return;
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float clamped = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), gainDb));
            gain.setValue(clamped);
        } catch (Exception e) {
            // No gain control available: leave volume untouched.
        }
    }

    /** Open a ready-to-play Clip for the named sound, or null if unavailable. */
    private static Clip load(String name) throws Exception {
        InputStream raw = SoundPlayer.class.getResourceAsStream(DIR + name + ".wav");
        if (raw == null) return null; // file not present: stay silent
        // BufferedInputStream supports mark/reset, which AudioSystem needs.
        AudioInputStream in = AudioSystem.getAudioInputStream(new BufferedInputStream(raw));
        Clip clip = AudioSystem.getClip();
        clip.open(in);
        return clip;
    }
}
