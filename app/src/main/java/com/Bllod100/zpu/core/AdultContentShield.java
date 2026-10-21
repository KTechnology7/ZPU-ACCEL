package com.Bllod100.zpu.core;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class AdultContentShield {
    private static final Set<String> ADULT_DOMAINS = new CopyOnWriteArraySet<>();
    private static final String[] ADULT_KEYWORDS = {
        "porn", "xvideo", "pornhub", "xhamster", "redtube", "youporn", "xnxx",
        "chaturbate", "onlyfans", "cam4", "stripchat", "livejasmin", "beeg",
        "spankbang", "brazzers", "tube8", "tnaflix", "eporner", "thumbzilla",
        "nuvid", "hentai", "rule34", "youjizz", "anysex", "hqporner", "cliphunter",
        "drtuber", "porntrex", "motherless", "nsfw", "erotic", "xxx"
    };

    static {
        ADULT_DOMAINS.add("pornhub.com");
        ADULT_DOMAINS.add("xvideos.com");
        ADULT_DOMAINS.add("xnxx.com");
        ADULT_DOMAINS.add("xhamster.com");
        ADULT_DOMAINS.add("redtube.com");
        ADULT_DOMAINS.add("youporn.com");
        ADULT_DOMAINS.add("chaturbate.com");
        ADULT_DOMAINS.add("onlyfans.com");
        ADULT_DOMAINS.add("stripchat.com");
        ADULT_DOMAINS.add("livejasmin.com");
        ADULT_DOMAINS.add("spankbang.com");
        ADULT_DOMAINS.add("beeg.com");
        ADULT_DOMAINS.add("brazzers.com");
        ADULT_DOMAINS.add("eporner.com");
        ADULT_DOMAINS.add("thumbzilla.com");
        ADULT_DOMAINS.add("hentai.org");
        ADULT_DOMAINS.add("rule34.xxx");
        ADULT_DOMAINS.add("youjizz.com");
    }

    public static boolean isAdultContent(String host) {
        if (host == null || host.isEmpty()) return false;
        String lower = host.toLowerCase().trim();
        for (String domain : ADULT_DOMAINS) {
            if (lower.contains(domain)) {
                return true;
            }
        }
        for (int i = 0; i < ADULT_KEYWORDS.length; i++) {
            String kw = ADULT_KEYWORDS[i];
            if (lower.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    public static String getAdultDomainsList() {
        StringBuilder sb = new StringBuilder();
        for (String d : ADULT_DOMAINS) {
            if (sb.length() > 0) sb.append(",");
            sb.append(d);
        }
        return sb.toString();
    }
}
