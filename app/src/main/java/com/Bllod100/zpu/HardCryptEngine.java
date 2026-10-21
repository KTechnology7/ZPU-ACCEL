package com.Bllod100.zpu;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import com.Bllod100.zpu.core.SavageWatchdog;
import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeyTemplates;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.integration.android.AndroidKeysetManager;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
public class HardCryptEngine {
    private static final String K = "ultimate_keyset";
    private static final String K_DIRECT = "direct_app_keyset";
    private static final String PREF = "zpu_v20_prefs";
    private static final String URI = "android-keystore://zpu_ultimate_v20";
    private static final String URI_DIRECT = "android-keystore://zpu_direct_v51";
    private static Aead aead = null;
    private static Aead directAead = null;
    private static boolean isLeanMode = false;
    private static boolean tamperDetected = false;
    public static void init(Context context) {
        verifySignature(context);
        try {
            AeadConfig.register();
            aead = new AndroidKeysetManager.Builder()
                .withSharedPref(context, K, PREF)
                .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                .withMasterKeyUri(URI)
                .build().getKeysetHandle().getPrimitive(Aead.class);
            directAead = new AndroidKeysetManager.Builder()
                .withSharedPref(context, K_DIRECT, PREF)
                .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                .withMasterKeyUri(URI_DIRECT)
                .build().getKeysetHandle().getPrimitive(Aead.class);
        } catch (Exception e) { e.printStackTrace(); }
    }
    private static void verifySignature(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature s : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(s.toByteArray());
            }
        } catch (Exception e) { tamperDetected = true; }
    }
    public static byte[] enc(byte[] d, boolean isDirect) {
        if (d == null || d.length == 0) return new byte[0];
        long start = System.nanoTime();
        if (tamperDetected || !SavageWatchdog.isHealthy()) return new byte[d.length];
        if (isLeanMode) {
            byte[] r = Arrays.copyOf(d, d.length);
            for (int i = 0; i < r.length; i++) r[i] = (byte)(r[i] ^ 0xFF);
            return r;
        }
        try {
            Aead k = isDirect ? directAead : aead;
            byte[] res = (k != null) ? k.encrypt(d, null) : d;
            if (System.nanoTime() - start > 10000000) {
                tamperDetected = true;
                return new byte[res.length];
            }
            return polyMorph(res, 0);
        } catch (Exception e) { return d; }
    }
    public static byte[] dec(byte[] d, boolean isDirect) {
        if (d == null || d.length == 0) return new byte[0];
        long start = System.nanoTime();
        if (tamperDetected || !SavageWatchdog.isHealthy()) return new byte[d.length];
        byte[] raw = polyMorph(Arrays.copyOf(d, d.length), 0);
        if (isLeanMode) return raw;
        try {
            Aead k = isDirect ? directAead : aead;
            byte[] res = (k != null) ? k.decrypt(raw, null) : raw;
            if (System.nanoTime() - start > 10000000) {
                tamperDetected = true;
                scrub(raw);
                return new byte[res.length];
            }
            scrub(raw);
            return res;
        } catch (Exception e) {
            scrub(raw);
            return d;
        }
    }
    private static final byte[] HKDF_SEED = "ZPU_HARDENED_SILICON_HKDF_V20".getBytes();

    public static byte[] polyMorph(byte[] data, int matchCode) {
        if (data == null || data.length == 0) return data;
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            sha256.update(HKDF_SEED);
            sha256.update((byte) (matchCode & 0xFF));
            byte[] keyStream = sha256.digest();

            for (int i = 0; i < data.length; i++) {
                byte k = keyStream[i % keyStream.length];
                data[i] = (byte) (data[i] ^ k ^ ((i * 31) & 0xFF));
            }
        } catch (Exception e) {
            int salt = (matchCode ^ 0xA1A) & 0xFF;
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (data[i] ^ (salt ^ (i % 256)));
            }
        }
        return data;
    }
    public static void scrub(byte[] data) {
        if (data != null) Arrays.fill(data, (byte)0);
    }
    public static byte[] cmp(byte[] d) {
        Deflater def = new Deflater(Deflater.BEST_SPEED);
        def.setInput(d);
        def.finish();
        byte[] b = new byte[d.length + 64];
        int l = def.deflate(b);
        def.end();
        return Arrays.copyOfRange(b, 0, l);
    }
    public static byte[] dcmp(byte[] d, int ol) {
        Inflater inf = new Inflater();
        inf.setInput(d);
        byte[] b = new byte[ol];
        int l = 0;
        try { l = inf.inflate(b); } catch (Exception ignored) {}
        inf.end();
        return Arrays.copyOfRange(b, 0, l);
    }
    public static boolean isTamperDetected() { return tamperDetected; }
    public static void setLeanMode(boolean a) { isLeanMode = a; }
    public static boolean isLean() { return isLeanMode; }
}
