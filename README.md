<div align="center">

<img src=".github/assets/viris-icon.svg" width="128" height="128" alt="VIRIS icon">

# VIRIS

**Voxy LODs on Minecraft's Vulkan renderer.**

![Vulkan](https://img.shields.io/badge/Vulkan-3D8BFF?logo=vulkan&logoColor=white)
![OpenGL interop](https://img.shields.io/badge/OpenGL-interop-3D8BFF?logo=opengl&logoColor=white)
![Voxy](https://img.shields.io/badge/Voxy-LODs-3D8BFF?logoColor=white)

</div>

---

[Voxy](https://modrinth.com/mod/voxy) only renders through OpenGL and refuses the Vulkan backend. VIRIS runs Voxy's
own, unchanged renderer on Vulkan: it opens a hidden OpenGL context next to the game's Vulkan device, Voxy draws into
images both APIs share (GL/Vulkan interop), and the game blends them in before the world's terrain. The picture is
Voxy's own (textures, lighting, water, fog, SSAO), not an imitation.

Status: **experimental**, personal project, Windows only. Tested on an RTX 4060 Ti (driver 617, Windows 11). Bug
reports welcome in the [issues](https://github.com/laurensanp/viris/issues). Called `voxy-vulkan` before 0.3.0.

## What you need

| | |
|---|---|
| OS | Windows 10 / 11, 64-bit |
| GPU | A driver with OpenGL/Vulkan interop (`GL_EXT_memory_object_win32`, `GL_EXT_semaphore_win32`; NVIDIA has it) |
| Game | Minecraft **26.3** with Fabric Loader **0.20.1+** |

Mods, all in `.minecraft/mods/` (or a Lunar profile's `mods/`):

| Mod | Where from | Needed for |
|---|---|---|
| `viris-<version>+mc26.3.jar` | [Modrinth](https://modrinth.com/mod/viris) or [Releases](https://github.com/laurensanp/viris/releases) | this mod |
| [Voxy](https://modrinth.com/mod/voxy) 0.2.20-beta | Modrinth | the LODs |
| [Sodium](https://modrinth.com/mod/sodium) 0.9.x for 26.3 | Modrinth | the terrain pass hook |
| [Fabric API](https://modrinth.com/mod/fabric-api) | Modrinth | Voxy |
| [Vitrail](https://modrinth.com/mod/vitrail-shaders) for 26.3 (tested: 0.13.0-beta) | Modrinth | optional, shader packs |

## Install

1. Put the mods above into `mods/`. Remove an old `voxy-vulkan-*.jar` (different mod id, both would load).
2. Start the game. Since 0.3.5 it starts on **Vulkan** by itself when **Video Settings → Graphics API** is on its default
   (before: set it to Vulkan yourself). If you choose OpenGL there, the game stays on OpenGL: Voxy draws by itself and
   VIRIS has nothing to do. Do that if your driver lacks the interop, since the LODs stay off on Vulkan then.

## What works

* **Without a shader pack: draws like Voxy on OpenGL** (dev world, day and night compared). Cost at 1080p on a
  4060 Ti: about 0.7 ms per rendered frame.
* **With a shader pack (Vitrail):** packs with Voxy support (`voxy.json` in the pack: Complementary, BSL, Photon, Solas,
  MakeUp, ...) draw the LODs with their own code, as under Iris. The LODs stay after reloading or toggling the pack and
  after switching Voxy off and on.
* Works with [Voxy Server Side](https://modrinth.com/plugin/voxy-server-side) (LODs sent by the server): it feeds
  Voxy's own data, VIRIS only draws.
* Runs in Lunar Client profiles with Fabric for 26.3.

## Works with

[SIRIS](https://github.com/laurensanp/siris) (DLSS Super Resolution + Frame Generation): install it next to this mod if
you want those. It is not required. Use SIRIS 1.1.4 or newer, older
versions make the LODs lose their colour when you join a world a second time.

## Known issues

* Shader packs: no shadows on LODs yet (the pack's shadow maps aren't passed in), packs that render into vanilla depth
  aren't supported, packs without Voxy support show no LODs.
* Thin dark lines can show over LOD water. They also show when the game runs on OpenGL, so the interop isn't the cause.
* Windows only.

## Build from source

Requirements: **JDK 25** (Gradle needs it in `JAVA_HOME`), git. What is not ours is downloaded, never committed:

| Item | Where it goes | Source |
|---|---|---|
| Voxy 0.2.20-beta jar (compile only) | `libs/voxy-0.2.20-beta.jar` | [Modrinth](https://modrinth.com/mod/voxy) |
| Sodium 0.9.2 jar (compile only) | `libs/sodium-fabric-0.9.2+mc26.3.jar` | [Modrinth](https://modrinth.com/mod/sodium/version/mc26.3-0.9.2-fabric) |
| Vitrail 0.13.0-beta jar (compile only) | `libs/vitrail-0.13.0-beta+mc26.3.jar` | [Modrinth](https://modrinth.com/mod/vitrail-shaders) |
| Sodium, Fabric API, Voxy, Vitrail jars (test runs) | `run/mods/` | Modrinth |

```bat
git clone https://github.com/laurensanp/viris.git
cd viris
set JAVA_HOME=C:\path\to\jdk-25
gradlew build                                             :: build\libs\viris-*.jar
gradlew runClient --args="--quickPlaySingleplayer <world>"
```

More background for contributors:
[`CLAUDE.md`](CLAUDE.md).

## License

This mod: [MIT](LICENSE). Voxy is All-Rights-Reserved: VIRIS contains none of its code, is only built against it and
hooks into the jar you install at runtime. Voxy, Sodium, Vitrail (LGPL-3.0) and shader packs are not included; get them
from their authors.
