package com.Bllod100.zpu.core;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.Certificate;

public class SiliconAttestator {
    private static boolean isAttested = false;
    private static String bootStatus = "UNVERIFIED";

    public static void verify() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            isAttested = true;
            bootStatus = "LEGACY_OK";
            return;
        }
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            if (!ks.containsAlias("ZPU_DNA")) {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
                kpg.initialize(new KeyGenParameterSpec.Builder("ZPU_DNA", KeyProperties.PURPOSE_SIGN)
                        .setAttestationChallenge("SAVAGE_DNA".getBytes())
                        .setDigests(KeyProperties.DIGEST_SHA256)
                        .build());
                kpg.generateKeyPair();
            }
            Certificate[] chain = ks.getCertificateChain("ZPU_DNA");
            if (chain != null && chain.length > 0) {
                isAttested = true;
                bootStatus = "LOCKED";
            }
        } catch (Exception e) {
            isAttested = false;
            bootStatus = "EXPLOITED";
        }
    }

    public static boolean getAttestationStatus() {
        return isAttested;
    }

    public static String getBootStatus() {
        return bootStatus;
    }
}
