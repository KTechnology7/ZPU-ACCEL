package com.Bllod100.zpu.core;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

/**
 * ZPU Pure Java Command Engine (Fortress Edition)
 * STRICT RULE: No system shell. Integrated malicious command filtering.
 */
public class SavageShell {
    private static final String[] FORBIDDEN = {"xnxx", "porn", "telnet", "ftp", "rm -rf", "mkfs", "meterpreter"};
    
    public static List<String> exec(String cmd) {
        List<String> output = new ArrayList<>();
        
        // MALICIOUS FILTER
        String check = cmd.toLowerCase();
        for (String f : FORBIDDEN) {
            if (check.contains(f)) {
                output.add("[!] SECURITY ALERT: COMMAND BLOCKED [KALI-IPS]");
                return output;
            }
        }
        
        if (cmd.startsWith("ls ")) {
            internalListFiles(cmd.substring(3), output);
        } else if (cmd.equals("id")) {
            output.add("uid=0(root) gid=0(root) groups=0(root) [ZPU-VIRTUAL-FORTRESS]");
        } else {
            output.add("ZPU_INTERNAL_EXEC: " + cmd + " [SUCCESS]");
        }
        
        return output;
    }

    private static void internalListFiles(String path, List<String> output) {
        File dir = new File(path);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) output.add(f.getName());
            }
        } else {
            output.add("PATH_NOT_FOUND: " + path);
        }
    }

    public static void runBigJob(String job) {
        exec("JOB_INIT: " + job);
        exec("JOB_COMPLETE: " + job);
    }
}
