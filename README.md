# SafeTPA

Minecraft self-service teleport requests

---

##### Commands

| usage           | aliases             | description                | permission |
|-----------------|---------------------|----------------------------|------------|
| `/tpa <NAME>`   | `tpask`             | Send request               | `tpa.tpa`  |
| `/tpy <NAME>`   | `tpaccept`, `tpyes` | Accept request             | `tpa.tpy`  |
| `/tpn <NAME>`   | `tpdeny`, `tpno`    | Deny request               | `tpa.tpn`  |
| `/tpi [<NAME>]` | `tpignore`          | Ignore requests per player | `tpa.tpi`  |
| `/tpt`          | `tptoggle`          | Ignore requests globally   | `tpa.tpt`  |

---

This plugin depends on [SuperVanish](https://github.com/LeonMangler/SuperVanish) to mitigate [coordinate](https://2b2t.miraheze.org/wiki/Coordinate_Exploits#Debug_Exploit/) [exploitation](https://github.com/PaperMC/Paper/issues/2016).
