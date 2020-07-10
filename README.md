# SafeTP
<a href="https://github.com/blockparole/SafeTP/releases/latest" alt="Download"><img src="https://img.shields.io/github/downloads/blockparole/SafeTP/latest/total.svg?label=download%20latest&style=popout-square" /></a>
<a href="https://github.com/blockparole/SafeTP" alt="Download"><img src="https://img.shields.io/github/languages/code-size/blockparole/SafeTP.svg?label=repo%20size&style=popout-square" /></a>

A simple Teleport Plugin for Minecraft that uses [vanish](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Player.html#hidePlayer-org.bukkit.plugin.Plugin-org.bukkit.entity.Player-) to mitigate [Coordinate Exploitation](https://2b2t.miraheze.org/wiki/Coordinate_Exploits#Debug_Exploit/).  
`/tpa NAME` `/tpy NAME` `/tpn NAME`

Default Config:
```
allow-multi-target-request: true
request-timeout-seconds: 60
unvanish-delay-ticks: 20
spawn-tp-deny: true
spawn-tp-deny-radius: false
```

This Project uses [PaperLib](https://github.com/PaperMC/PaperLib) and was tested on [Paper](https://papermc.io/) [1.12.2](https://papermc.io/api/v1/paper/1.12.2/1618).
