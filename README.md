# SafeTP

[![DL](https://img.shields.io/github/downloads/nothub/SafeTP/total?label=DL&style=popout-square)](https://github.com/nothub/SafeTP/releases/latest)
[![LoC](https://img.shields.io/tokei/lines/github/nothub/SafeTP?label=LoC&style=popout-square)](https://github.com/nothub/SafeTP)

A simple Teleport Plugin for Minecraft that uses [vanish](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Player.html#hidePlayer-org.bukkit.plugin.Plugin-org.bukkit.entity.Player-)
to mitigate [Coordinate Exploitation](https://2b2t.miraheze.org/wiki/Coordinate_Exploits#Debug_Exploit/).

---

##### Command Usage

|||
| ------------- | --------------- |
| `/tpa <NAME>` | Send Request    |
| `/tpy <NAME>` | Accept Request  |
| `/tpn <NAME>` | Deny Request    |
| `/tpt`        | Toggle Ignore   |

---

##### Default Config

```
allow-multi-target-request: true
request-timeout-seconds: 60
unvanish-delay-ticks: 20
spawn-tp-deny: true
spawn-tp-deny-radius: 1500
distance-limit: false
distance-limit-radius: 1000
```

---

##### Tested on
Latest [Paper](https://papermc.io/) release for MC 1.12.x, 1.13.x, 1.14.x, 1.15.x, 1.16.x, 1.17.x

---
This Project uses [PaperLib](https://github.com/PaperMC/PaperLib) and [bStats](https://github.com/Bastian/bStats).
