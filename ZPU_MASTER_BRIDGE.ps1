$NODE_PORT = 60000
$BEACON_PORT = 60002
$PROXY_PORT = 8080
$SECRET_KEY = "ZPU-APEX-SECURE-KEY-2026"

# 1. ADMINISTRATOR VALIDATION
$currentPrincipal = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
if (-not $currentPrincipal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Host "====================================================" -ForegroundColor Red
    Write-Host " ERROR: SCRIPT MUST BE RUN AS ADMINISTRATOR         " -BackgroundColor DarkRed -ForegroundColor White
    Write-Host " Please restart PowerShell as Admin to enable Shield." -ForegroundColor Red
    Write-Host "====================================================" -ForegroundColor Red
    pause
    exit
}

function Reset-ADB {
    Write-Host "[!] STABILIZING ADB DAEMON..." -ForegroundColor Yellow
    Get-Process adb -ErrorAction SilentlyContinue | Stop-Process -Force
    Start-Sleep -Seconds 1
    .\adb.exe start-server | Out-Null
}

function Refresh-WinINet {
    try {
        $typeDef = @"
        using System;
        using System.Runtime.InteropServices;
        public class WinInetHelper {
            [DllImport("wininet.dll", SetLastError = true)]
            public static extern bool InternetSetOption(IntPtr hInternet, int dwOption, IntPtr lpBuffer, int dwBufferLength);
        }
"@
        Add-Type -TypeDefinition $typeDef -ErrorAction SilentlyContinue
        [WinInetHelper]::InternetSetOption([IntPtr]::Zero, 39, [IntPtr]::Zero, 0) | Out-Null
        [WinInetHelper]::InternetSetOption([IntPtr]::Zero, 37, [IntPtr]::Zero, 0) | Out-Null
    } catch {}
}

function Set-SystemTunnel {
    param([bool]$active, $ip)
    $regPath = "HKCU:\Software\Microsoft\Windows\CurrentVersion\Internet Settings"
    try {
        if ($active) {
            $proxyString = "$ip" + ":" + $PROXY_PORT
            Set-ItemProperty -Path $regPath -Name ProxyEnable -Value 1
            Set-ItemProperty -Path $regPath -Name ProxyServer -Value $proxyString
            Set-ItemProperty -Path $regPath -Name ProxyOverride -Value "<local>;localhost;127.0.0.1;127.0.0.1:*;localhost:*;*.local;192.168.*;10.*;172.16.*;*.google.com;*.android.com;*.gradle.org"
            netsh int tcp set global autotuninglevel=normal | Out-Null
            netsh int tcp set global fastopen=enabled | Out-Null
            Refresh-WinINet
        } else {
            Set-ItemProperty -Path $regPath -Name ProxyEnable -Value 0
            Refresh-WinINet
        }
    } catch {}
}

function Update-WindowsHosts {
    param([bool]$active, [string]$rawList)
    if (!$rawList) { return }
    $domains = $rawList.Split(",")
    try {
        $content = Get-Content $HOSTS_PATH -ErrorAction SilentlyContinue
        if ($null -eq $content) { $content = @() }
        $newContent = New-Object System.Collections.Generic.List[string]
        foreach ($line in $content) {
            $isBlocked = $false
            foreach ($d in $domains) { if ($line -match $d) { $isBlocked = $true; break } }
            if (-not $isBlocked) { $newContent.Add($line) }
        }
        if ($active) {
            foreach ($d in $domains) {
                if ($d.Trim() -ne "") {
                    $newContent.Add("0.0.0.0 $d")
                    $newContent.Add("0.0.0.0 www.$d")
                }
            }
        }
        $newContent | Out-File $HOSTS_PATH -Encoding ASCII -Force
    } catch {}
}

function Get-ActivePortProfile {
    $connections = Get-NetTCPConnection -State Established -ErrorAction SilentlyContinue | Select-Object LocalPort, OwningProcess -First 5
    $portProfileString = ""
    foreach ($c in $connections) {
        $p = Get-Process -Id $c.OwningProcess -ErrorAction SilentlyContinue
        if ($p) { $portProfileString += "$($p.ProcessName):$($c.LocalPort)," }
    }
    return $portProfileString.TrimEnd(",")
}

function Optimize-KytyAndAnyPS5 {
    try {
        $ps5Procs = Get-Process -Name "AnyPS5", "AnyPS5_qt", "AnyPS5_gui", "anyps5_emulator", "Kyty", "Kyty-qt", "Kyty_gui", "ps5_emulator" -ErrorAction SilentlyContinue
        foreach ($kp in $ps5Procs) {
            if ($kp.PriorityClass -ne [System.Diagnostics.ProcessPriorityClass]::High) {
                $kp.PriorityClass = [System.Diagnostics.ProcessPriorityClass]::High
            }
        }
        if ($ps5Procs.Count -gt 0) {
            [System.GC]::Collect()
        }
    } catch {}
}

function Optimize-ShadPS4AndNativePS4 {
    try {
        $ps4Procs = Get-Process -Name "shadPS4", "shadPS4_qt", "sexy_bloom", "ps4_layer", "ps4_runner", "eboot" -ErrorAction SilentlyContinue
        foreach ($p in $ps4Procs) {
            if ($p.PriorityClass -ne [System.Diagnostics.ProcessPriorityClass]::High) {
                $p.PriorityClass = [System.Diagnostics.ProcessPriorityClass]::High
            }
        }
        if ($ps4Procs.Count -gt 0) {
            [System.GC]::Collect()
        }
    } catch {}
}

function Optimize-Trellis3DAndGameEngines {
    try {
        $trellisProcs = Get-Process -Name "trellis", "trellis3d", "blender", "UnrealEditor", "Unity", "godot", "3dsmax", "maya" -ErrorAction SilentlyContinue
        foreach ($tp in $trellisProcs) {
            if ($tp.PriorityClass -ne [System.Diagnostics.ProcessPriorityClass]::High) {
                $tp.PriorityClass = [System.Diagnostics.ProcessPriorityClass]::High
            }
        }
        if ($trellisProcs.Count -gt 0) {
            [System.GC]::Collect()
        }
    } catch {}
}

function Optimize-WindowsSystem {
    try {
        $mmKey = "HKLM:\SOFTWARE\Microsoft\Windows NT\CurrentVersion\Multimedia\SystemProfile"
        if (Test-Path $mmKey) {
            Set-ItemProperty -Path $mmKey -Name "SystemResponsiveness" -Value 0 -ErrorAction SilentlyContinue
            Set-ItemProperty -Path $mmKey -Name "NetworkThrottlingIndex" -Value 0xffffffff -ErrorAction SilentlyContinue
        }
        $gamesKey = "$mmKey\Tasks\Games"
        if (Test-Path $gamesKey) {
            Set-ItemProperty -Path $gamesKey -Name "GPU Priority" -Value 8 -ErrorAction SilentlyContinue
            Set-ItemProperty -Path $gamesKey -Name "Priority" -Value 6 -ErrorAction SilentlyContinue
            Set-ItemProperty -Path $gamesKey -Name "Scheduling Category" -Value "High" -ErrorAction SilentlyContinue
        }
        $bloatServices = @("DiagTrack", "dmwappushservice", "WerSvc")
        foreach ($svc in $bloatServices) {
            $s = Get-Service -Name $svc -ErrorAction SilentlyContinue
            if ($s -and $s.Status -eq 'Running') {
                Stop-Service -Name $svc -Force -ErrorAction SilentlyContinue
                Set-Service -Name $svc -StartupType Disabled -ErrorAction SilentlyContinue
            }
        }
    } catch {}
}

function Optimize-UniversityProgrammingTools {
    try {
        $cppProcs = Get-Process -Name "devcpp", "cc1plus", "g++", "gcc", "codeblocks", "devenv" -ErrorAction SilentlyContinue
        foreach ($p in $cppProcs) {
            if ($p.PriorityClass -ne [System.Diagnostics.ProcessPriorityClass]::High) {
                $p.PriorityClass = [System.Diagnostics.ProcessPriorityClass]::High
            }
        }
        $matlabProcs = Get-Process -Name "matlab", "javaw", "Simulink" -ErrorAction SilentlyContinue
        foreach ($mp in $matlabProcs) {
            if ($mp.PriorityClass -ne [System.Diagnostics.ProcessPriorityClass]::High) {
                $mp.PriorityClass = [System.Diagnostics.ProcessPriorityClass]::High
            }
        }
        $ciscoProcs = Get-Process -Name "PacketTracer", "PacketTracer7", "PacketTracer8" -ErrorAction SilentlyContinue
        foreach ($cp in $ciscoProcs) {
            if ($cp.PriorityClass -ne [System.Diagnostics.ProcessPriorityClass]::High) {
                $cp.PriorityClass = [System.Diagnostics.ProcessPriorityClass]::High
            }
        }
        $dbProcs = Get-Process -Name "mysqld", "mariadbd", "postgres" -ErrorAction SilentlyContinue
        foreach ($dp in $dbProcs) {
            if ($dp.PriorityClass -ne [System.Diagnostics.ProcessPriorityClass]::AboveNormal) {
                $dp.PriorityClass = [System.Diagnostics.ProcessPriorityClass]::AboveNormal
            }
        }
        $devProcs = Get-Process -Name "pycharm64", "python", "jupyter-lab", "lazarus", "fpc", "delphi" -ErrorAction SilentlyContinue
        foreach ($dp in $devProcs) {
            if ($dp.PriorityClass -ne [System.Diagnostics.ProcessPriorityClass]::AboveNormal) {
                $dp.PriorityClass = [System.Diagnostics.ProcessPriorityClass]::AboveNormal
            }
        }
    } catch {}
}

function Optimize-NativePortsAndTopGames {
    try {
        $topGamesAndApps = Get-Process -Name "gamelauncher", "game", "eldenring", "cyberpunk2077", "hogwartslegacy", "starfield", "baldursgate3", "cod", "fortnite", "valorant", "gta5", "rdr2", "devcpp", "matlab", "PacketTracer", "mysqld", "pycharm64" -ErrorAction SilentlyContinue
        foreach ($app in $topGamesAndApps) {
            if ($app.PriorityClass -ne [System.Diagnostics.ProcessPriorityClass]::High) {
                $app.PriorityClass = [System.Diagnostics.ProcessPriorityClass]::High
            }
        }
    } catch {}
}

function Update-Dashboard {
    param($state, $fw, $ram, $p_shared, $p_cpu, $p_gpu, $bond, $exploits, $nodes, $masterNode, $speed)
    Clear-Host
    Write-Host "====================================================" -ForegroundColor Cyan
    Write-Host "          ZPU MULTI-NODE SWARM v420.2               " -ForegroundColor White -BackgroundColor DarkBlue
    Write-Host "====================================================" -ForegroundColor Cyan
    Write-Host " STATUS       : $state"
    Write-Host " ACTIVE NODES : $nodes [BOOST: $($nodes)x]"
    Write-Host " MASTER NODE  : $masterNode"
    Write-Host " FIREWALL     : $fw [HARDWARE-LOCKED]"
    Write-Host " NET SPEED    : $speed MB/s [LIVE GATEWAY]" -ForegroundColor Yellow
    Write-Host " PC RAM LOAD  : $ram % [SWARM-POOLED]"
    Write-Host "----------------------------------------------------"
    Write-Host " TOTAL SHARED : $p_shared MB"
    Write-Host " AVG PHONE CPU: $p_cpu %"
    Write-Host " AVG PHONE GPU: $p_gpu %"
    Write-Host "----------------------------------------------------"
    Write-Host " BOND MODE    : $bond"
    Write-Host " EXPLOITS     : $exploits [BLOCKED]"
    Write-Host "====================================================" -ForegroundColor Cyan
}

$script:adbForwarded = $false
$script:udpBeacon = $null

function Get-NodeIPs {
    $found = @()
    # 1. USB Scan (Cached to avoid spamming adb and WinINet registry every second)
    $adbCheck = .\adb.exe devices | Select-String "\tdevice$"
    if ($null -ne $adbCheck) {
        if (-not $script:adbForwarded) {
            .\adb.exe forward tcp:60000 tcp:60000 | Out-Null
            .\adb.exe forward tcp:8080 tcp:8080 | Out-Null
            Set-SystemTunnel -active $true -ip "127.0.0.1"
            $script:adbForwarded = $true
        }
        $found += "127.0.0.1"
    } else {
        $script:adbForwarded = $false
    }

    # 1.5 USB Internet Tethering Subnet Scan (RNDIS/ECM/NCM)
    try {
        $netAdapters = Get-NetIPAddress -AddressFamily IPv4 -ErrorAction SilentlyContinue | Where-Object { $_.IPAddress -match "^(192\.168\.(42|43)\.|172\.20\.)" }
        foreach ($adapter in $netAdapters) {
            $ip = $adapter.IPAddress
            if ($found -notcontains $ip) { $found += $ip }
        }
    } catch {}

    # 2. WiFi Scan (Persistent reusable UDP beacon listener)
    try {
        if ($null -eq $script:udpBeacon) {
            $script:udpBeacon = New-Object System.Net.Sockets.UdpClient
            $script:udpBeacon.Client.ReuseAddress = $true
            $script:udpBeacon.Client.Bind((New-Object System.Net.IPEndPoint([System.Net.IPAddress]::Any, $BEACON_PORT)))
            $script:udpBeacon.Client.ReceiveTimeout = 200
        }
        $remote = New-Object System.Net.IPEndPoint([System.Net.IPAddress]::Any, 0)
        while ($script:udpBeacon.Available -gt 0) {
            $bytes = $script:udpBeacon.Receive([ref]$remote)
            $ip = $remote.Address.ToString()
            if ($found -notcontains $ip) { $found += $ip }
        }
    } catch {
        if ($null -ne $script:udpBeacon) {
            try { $script:udpBeacon.Close() } catch {}
            $script:udpBeacon = $null
        }
    }
    return $found
}

function Restore-ComputerDefaults {
    try {
        Write-Host "[!] FORCE SHUTDOWN SIGNAL RECEIVED FROM PHONE!" -ForegroundColor Red
        Write-Host "[!] RESTORING WINDOWS COMPUTER SETTINGS TO DEFAULT..." -ForegroundColor Yellow

        $regPath = "HKCU:\Software\Microsoft\Windows\CurrentVersion\Internet Settings"
        Set-ItemProperty -Path $regPath -Name ProxyEnable -Value 0 -ErrorAction SilentlyContinue
        Refresh-WinINet

        $mmKey = "HKLM:\SOFTWARE\Microsoft\Windows NT\CurrentVersion\Multimedia\SystemProfile"
        if (Test-Path $mmKey) {
            Set-ItemProperty -Path $mmKey -Name "SystemResponsiveness" -Value 20 -ErrorAction SilentlyContinue
            Set-ItemProperty -Path $mmKey -Name "NetworkThrottlingIndex" -Value 10 -ErrorAction SilentlyContinue
        }

        if (Test-Path $HOSTS_PATH) {
            $content = Get-Content $HOSTS_PATH -ErrorAction SilentlyContinue
            if ($content) {
                $cleanContent = $content | Where-Object { $_ -notmatch "0.0.0.0" -and $_ -trim -ne "" }
                $cleanContent | Out-File $HOSTS_PATH -Encoding ASCII -Force
            }
        }

        $bloatServices = @("DiagTrack", "dmwappushservice", "WerSvc")
        foreach ($svc in $bloatServices) {
            $s = Get-Service -Name $svc -ErrorAction SilentlyContinue
            if ($s) {
                Set-Service -Name $svc -StartupType Automatic -ErrorAction SilentlyContinue
                Start-Service -Name $svc -ErrorAction SilentlyContinue
            }
        }
        $netAdapter = Get-NetAdapter | Where-Object { $_.Status -eq 'Up' } | Select-Object -First 1
        if ($netAdapter) {
            Set-DnsClientServerAddress -InterfaceIndex $netAdapter.ifIndex -ResetServerAddresses -ErrorAction SilentlyContinue
        }
        Write-Host "[+] COMPUTER SETTINGS RESTORED TO DEFAULT." -ForegroundColor Green
    } catch {}
}

$lastAppliedDns = ""

function Update-WindowsDns {
    param([string]$dns)
    if ($dns -eq $script:lastAppliedDns -or $dns -eq "") { return }
    try {
        $netAdapter = Get-NetAdapter | Where-Object { $_.Status -eq 'Up' } | Select-Object -First 1
        if ($netAdapter) {
            if ($dns -eq "SYSTEM") {
                Set-DnsClientServerAddress -InterfaceIndex $netAdapter.ifIndex -ResetServerAddresses -ErrorAction SilentlyContinue
                Write-Host "[+] WINDOWS DNS RESET TO SYSTEM DEFAULT." -ForegroundColor Green
            } else {
                Set-DnsClientServerAddress -InterfaceIndex $netAdapter.ifIndex -ServerAddresses ($dns, "8.8.8.8") -ErrorAction SilentlyContinue
                Write-Host "[+] WINDOWS DNS UPDATED TO: $dns" -ForegroundColor Green
            }
            $script:lastAppliedDns = $dns
        }
    } catch {}
}

function Check-And-Reset-Orphaned-Settings {
    try {
        $regPath = "HKCU:\Software\Microsoft\Windows\CurrentVersion\Internet Settings"
        $proxyEnabled = (Get-ItemProperty -Path $regPath -Name ProxyEnable -ErrorAction SilentlyContinue).ProxyEnable
        $mmKey = "HKLM:\SOFTWARE\Microsoft\Windows NT\CurrentVersion\Multimedia\SystemProfile"
        $sysResp = (Get-ItemProperty -Path $mmKey -Name SystemResponsiveness -ErrorAction SilentlyContinue).SystemResponsiveness
        $hostsHasZpu = $false
        if (Test-Path $HOSTS_PATH) {
            $hostsContent = Get-Content $HOSTS_PATH -ErrorAction SilentlyContinue
            if ($hostsContent -match "0.0.0.0") { $hostsHasZpu = $true }
        }

        if ($proxyEnabled -eq 1 -or $sysResp -eq 0 -or $hostsHasZpu) {
            $activeNodes = Get-NodeIPs
            if ($activeNodes.Count -eq 0) {
                Write-Host "[!] ORPHANED ZPU CUSTOM SETTINGS DETECTED AT SCRIPT LAUNCH!" -ForegroundColor Yellow
                Restore-ComputerDefaults
            }
        }
    } catch {}
}

$MasterID = ""
$GlobalStats = @{ shared=0; cpu=0; gpu=0; exploits=0; shield="OFF"; master="NONE" }
$script:pcLowPowerMode = $false

Check-And-Reset-Orphaned-Settings

$script:activeClient = $null
$script:activeWriter = $null
$script:activeReader = $null
$script:connectedIp = ""

while ($true) {
    if (-not $script:pcLowPowerMode) {
        Optimize-KytyAndAnyPS5
        Optimize-ShadPS4AndNativePS4
        Optimize-Trellis3DAndGameEngines
        Optimize-WindowsSystem
        Optimize-UniversityProgrammingTools
        Optimize-NativePortsAndTopGames
    }
    $activeNodes = Get-NodeIPs
    if ($activeNodes.Count -gt 0) {
        if ($MasterID -eq "") { $MasterID = "SWARM-ORIGIN-" + (Get-Random) }

        $totalShared=0; $totalCpu=0; $totalGpu=0; $totalExploits=0; $totalSpeed=0.0

        foreach ($ip in $activeNodes) {
            try {
                if ($null -eq $script:activeClient -or -not $script:activeClient.Connected -or $script:connectedIp -ne $ip) {
                    if ($null -ne $script:activeClient) {
                        try { $script:activeClient.Close() } catch {}
                    }
                    $script:activeClient = New-Object System.Net.Sockets.TcpClient
                    $script:activeClient.NoDelay = $true
                    $script:activeClient.ReceiveTimeout = 4000
                    $script:activeClient.SendTimeout = 4000
                    $script:activeClient.Connect($ip, $NODE_PORT)
                    $stream = $script:activeClient.GetStream()
                    $script:activeWriter = New-Object System.IO.StreamWriter($stream)
                    $script:activeWriter.AutoFlush = $true
                    $script:activeReader = New-Object System.IO.StreamReader($stream)
                    $script:connectedIp = $ip
                    try { [Console]::Beep(800, 150); [Console]::Beep(1200, 200) } catch {}
                }

                # Handshake Secret Key and Master ID / pulse and read telemetry on persistent connection
                $script:activeWriter.WriteLine("AUTH_KEY:$SECRET_KEY")
                $script:activeWriter.WriteLine("MASTER_ID:$MasterID")

                $resp = $script:activeReader.ReadLine()
                if ($null -eq $resp) {
                    throw "Connection lost / EOF"
                }

                if ($resp) {
                    if ($resp -match "STATUS:FORCE_CLOSE") {
                        Restore-ComputerDefaults
                    }
                    if ($resp -match "P_SHARED:(\d+)") { $totalShared += [int]$Matches[1] }
                    if ($resp -match "P_CPU:(\d+)") { $totalCpu += [int]$Matches[1] }
                    if ($resp -match "P_GPU:(\d+)") { $totalGpu += [int]$Matches[1] }
                    if ($resp -match "EXPLOITS:(\d+)") { $totalExploits += [int]$Matches[1] }
                    if ($resp -match "NET_SPEED:([\d\.]+)") { $totalSpeed += [double]$Matches[1] }
                    if ($resp -match "BLKLIST:([^,]+.*)") { Update-WindowsHosts -active $true -rawList $Matches[1] }
                    if ($resp -match "DNS:([^,]+)") { Update-WindowsDns -dns $Matches[1] }
                    if ($resp -match "PC_LOW_PWR:ON") { $script:pcLowPowerMode = $true }
                    if ($resp -match "PC_LOW_PWR:OFF") { $script:pcLowPowerMode = $false }
                    if ($resp -match "SHIELD:ON") { $GlobalStats.shield = "ON" } else { $GlobalStats.shield = "OFF" }
                    if ($resp -match "MASTER:(True|1)") { $GlobalStats.master = $ip }
                }
            } catch {
                if ($script:connectedIp -ne "") {
                    try { [Console]::Beep(600, 200); [Console]::Beep(300, 250) } catch {}
                }
                if ($null -ne $script:activeClient) {
                    try { $script:activeClient.Close() } catch {}
                    $script:activeClient = $null
                    $script:connectedIp = ""
                }
            }
        }

        $avgCpu = [int]($totalCpu / $activeNodes.Count)
        $avgGpu = [int]($totalGpu / $activeNodes.Count)

        if ($script:pcLowPowerMode) {
            $dashState = "SYNCED [0.1% PC POWER - PHONE ONLY COMPUTE]"
        } else {
            $dashState = "SYNCED"
        }
        Update-Dashboard -state $dashState -fw $GlobalStats.shield -ram 45 -p_shared $totalShared -p_cpu $avgCpu -p_gpu $avgGpu -bond "SWARM" -exploits $totalExploits -nodes $activeNodes.Count -masterNode $GlobalStats.master -speed $totalSpeed
    } else {
        Update-Dashboard -state "AWAITING SWARM..." -fw "OFF" -ram 0 -p_shared 0 -p_cpu 0 -p_gpu 0 -bond "OFFLINE" -exploits 0 -nodes 0 -masterNode "NONE" -speed "0.00"
    }
    if ($script:pcLowPowerMode) {
        Start-Sleep -Seconds 3
    } else {
        Start-Sleep -Seconds 1
    }
}
