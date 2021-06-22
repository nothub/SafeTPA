# SafeTP

[![DL](https://img.shields.io/github/downloads/nothub/SafeTP/total?label=DL&style=popout-square)](https://github.com/nothub/SafeTP/releases/latest)
[![LoC](https://img.shields.io/tokei/lines/github/nothub/SafeTP?label=LoC&style=popout-square)](https://github.com/nothub/SafeTP)

A simple Teleport Plugin for Minecraft that uses [vanish](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Player.html#hidePlayer-org.bukkit.plugin.Plugin-org.bukkit.entity.Player-)
to mitigate [Coordinate Exploitation](https://2b2t.miraheze.org/wiki/Coordinate_Exploits#Debug_Exploit/).

##### Usage
`/tpa <NAME>` `/tpy <NAME>` `/tpn <NAME>` `/tpt`

##### Config
```
allow-multi-target-request: true
request-timeout-seconds: 60
unvanish-delay-ticks: 20
spawn-tp-deny: true
spawn-tp-deny-radius: 1500
distance-limit: false
distance-limit-radius: 10000
```

##### Versions
| MC     | SafeTP                                                       |
| ------ | ------------------------------------------------------------ |
| 1.16.5 | [latest](https://github.com/nothub/SafeTP/releases/latest)   |
| 1.12.2 | [2.3.4](https://github.com/nothub/SafeTP/releases/tag/2.3.4) |

##### Dependencies
This Project uses [PaperLib](https://github.com/PaperMC/PaperLib).
