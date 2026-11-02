# VIRIS

Repository github.com/laurensanp/viris (MIT): a Fabric mod for Minecraft 26.3 that runs Voxy's own renderer on the
game's Vulkan renderer through OpenGL interop. Own repo since 2026-11-02 (before: `viris/` in laurensanp/siris; the
history came along, older paths `voxy-vulkan/`, package `com.siris.viris`). Voxy is All-Rights-Reserved: never pack,
commit or modify its jar (runtime mixins only). Vitrail (LGPL-3.0) is not ours either.

Works with SIRIS (DLSS SR + FG, github.com/laurensanp/siris, local clone `../siris`), not required. Docs mention it
only as "works with"; no dependency in either direction. The one link in code: SIRIS checks `isModLoaded("viris")` and
then keeps Vitrail's reload on join (SIRIS 1.1.4+).

The game's default "Graphics API" tries OpenGL first; since 0.3.5 `PreferredGraphicsApiMixin` tries Vulkan first then
(Windows, Voxy loaded), so VIRIS doesn't need SIRIS for that. An explicit `preferredGraphicsBackend:"opengl"` stays:
Voxy draws by itself and VIRIS does nothing (tested 2026-11-02, also with SIRIS, which forces Vulkan either way).

| Path | What |
|---|---|
| `src/main/java/com/viris/` | `VoxyFrame` (the frame), `GlContext`, `Interop`, `AtlasReader`, `pack/` (shader packs), `mixin/` |
| `libs/` (ignored) | compile-only jars: Voxy 0.2.20-beta, Sodium 0.9.2, Vitrail 0.13.0-beta (Modrinth) |
| `run/` (ignored) | test game dir: Sodium, Fabric API, Voxy, optional Vitrail in `mods/`, `options.txt`, `saves/vulkan-test` |

## Build & test

- Gradle needs **JDK 25**: `JAVA_HOME=~/.jdks/jdk-25.0.4.1+1`, use `--no-daemon`. `gradlew build` -> `build/libs/viris-*.jar`.
- `gradlew runClient --no-daemon --args="--quickPlaySingleplayer vulkan-test"`. SIRIS' DevTest options don't exist here.
  For a screenshot use a temporary (never committed) `@Inject(method = "tick")` in `MinecraftMixin`: send commands with
  `mc.player.connection.sendCommand`, `Screenshot.grab(mc.gameDirectory, name, mc.gameRenderer.mainRenderTarget(), 1, ...)`
  at tick 500, `mc.stop()` later. Spectator at `-36 200 18 -90 25` shows the LODs. F2 sent from outside doesn't reach the game.
- One run at render distance 32 so Voxy ingests, then lower it. To test with SIRIS use its test loop (`../siris`).

## Notes

- VIRIS (2026-11-01, user's wish: voxy-vulkan renamed for 0.3.0, blue theme): mod id `viris`, package
  `com.viris` (main class `Viris`), `viris.mixins.json`, `assets/viris/icon.png`, `.github/assets/viris-icon.*`
  (frame stack as in SIRIS' icon, in blue: ground `#0F1522`, `#4F5B6E`, `#7CC4FF`, `#3D8BFF`, 2x2 voxel grid instead of the plus),
  log prefix `VIRIS:`, dev property `-Dviris.timing`, jar `viris-<version>`, Modrinth `viris` (own project). Lunar:
  remove `voxy-vulkan-*.jar` (other mod id, else both load). Theme colour for VIRIS: `#3D8BFF`.
- VIRIS in Lunar: see "Current Lunar install". Lunar's game log shows none of VIRIS' INFO lines (only `Enabling
  optional FeatureSet [VIRIS OpenGL interop]`); check with F3 (voxy line) and F2 screenshots. Voxy Server Side (`lss`
  0.19.0) works with VIRIS: it feeds Voxy's own ingest (`VoxyCompat` raw ingest bridge), VIRIS only draws; dev test via
  LAN (`publish` in -PtestCommands; singleplayer alone waits for LAN). Used by the user on survival.glaaaspenis.de.
- voxy-vulkan 0.2.0 (2026-11-01, user asked for a 1:1 port; the own LodRenderer of 0.1.x is gone): Voxy's own
  GL renderer runs unchanged on a hidden SDL OpenGL 4.6 context (`GlContext`, made current at
  `RenderSystem.initRenderer` RETURN order 800, before Voxy's own init at 900). Voxy's only draw entry is
  `VoxyRenderSystem.setupViewport(proj, modelView, fog, w, h, camXYZ)` + `renderOpaque(viewport, depthGlTex,
  colorGlTex)`; `VoxyFrame` calls it right before the game's "Solid" pass (`LevelRendererMixin` on
  `lambda$addMainPass$0`, matrices from `SodiumWorldRenderer.prepareChunkRendering`) into shared images over an
  empty picture, then a Vulkan pass writes colour (blend ONE, 1-srcA) and depth where Voxy drew; terrain draws
  over them by depth (= Voxy's stencil). Shared images: RenderPearl textures from a VMA pool with exportable
  dedicated memory (`VulkanGpuTextureMixin`, `Interop`), imported with GL_EXT_memory_object_win32; device gets
  VK_KHR_external_memory/semaphore_win32 as an optional FeatureSet. Three image sets in turn, binary semaphores per
  set (glDone/vkDone with texture lists). Lightmap: shared 16x16 copy per set. Atlas: `AtlasReader` rebuilds the
  block atlas from sprite images (texels start at u0/v0 = x + padding, not getX). Rows: RenderPearl's Vulkan images
  keep GL's row order, no flip. Patched Voxy hooks: device check, Iris hook, Sodium draw hook (`postApply`),
  `LightMapHelper.getLightmapTextureId`, `SoftwareModelTextureBakery.setupTexture`.
  Verified 2026-11-01: day and night look like Voxy on OpenGL; with DLSS Quality + FG x2 at 1080p, render distance
  8: Voxy ~0.85 ms render thread + ~0.7 ms GL per frame, 200 vs 230 rendered fps (Voxy rendering off). Lunar
  (`DLSS Optimized`, siris-1.1.2, backup `mods-backup-2026-11-01-voxy/`): Voxy 0.2.20 + voxy-vulkan 0.2.0 run, LODs
  seamless past render distance 8, FG ~357/180 fps (shaders off; the profile has Complementary on, so LODs are off
  there by default). Open: thin dark horizontal lines over LOD water; first seen with DLSS, but on 2026-11-02 the same lines
  showed without SIRIS, on Vulkan and on OpenGL (same places), so not DLSS and not the interop. Hangs seen earlier were VRAM: with Lunar's game open
  (~4 GB) GL frames took up to 1.4 s with 474 evictions and RenderPearl's 5 s submit timeout hit; close Lunar's game
  before dev tests. SIRIS side note: closing Lunar's window once wrote a watchdog crash report
  (`DlssNative.destroyPresenter` hung on shutdown). Tried and dropped: timeline
  semaphores (NVIDIA 617 passed only GL's first signal to Vulkan), GL fence + CPU wait before submit (hung with
  DLSS), reading back a shared texture on the GL side (glGetTextureImage, hung). `-Dviris.timing=true` logs
  Voxy CPU/GL time, free VRAM and evictions. Without Voxy support in the pack the LODs are skipped while it is on.
  Shader packs (2026-11-01, milestone 1): packs with Voxy support (`shaders/<world>/voxy.json` + `voxy_opaque.glsl`/
  `voxy_translucent.glsl`: Complementary, BSL, Photon, Solas, MakeUp...) draw the LODs with their own code, as under
  Iris. `pack/PackPatch` reads the patch with Vitrail's IncludeExpander (place from `DimensionSet.place`,
  `minecraft:overworld` -> `world0`), `pack/PackPipeline` builds Voxy's `IrisVoxyRenderPipelineData` without Iris
  (Unsafe; `VirisMixinPlugin` strips its Iris-only builder methods at load or the verifier fails), std140 uniform
  block filled from Vitrail's `PackValues.catalog()`, `RenderPipelineFactoryMixin` makes Voxy build its
  `IrisVoxyRenderPipeline`, block ids from Vitrail's `BlockStateIds.table`. Vitrail side (`mixin/vitrail/`): `VOXY 3`
  define, vx* uniforms in the engine catalog (`VoxyPackUniforms`, projection un-reversed like Voxy), vxDepthTex samplers
  bound in `PackPass.bindSamplers` to Voxy's depth converted to far = 1 (`DepthConvert`). Frame: GL draws at the main
  pass start, Vulkan waits there (after `GeometryHold.flushIdle` + `GraphicsApi.suspendLevelPass`, Vitrail keeps
  passes open), `PackChain.drawBeforeTranslucents` HEAD writes the LOD gbuffer targets into the pack's targets where
  the scene depth is clear (side = first deferred step's read side), vkDone with the frame's submit. Not yet: pack
  inputs for the patch (shadow maps, noisetex, colortex13/18/19, depthtex1 are dummies: LODs get no shadows), patch
  depth samplers during the translucent pass, renderToVanillaDepth packs. Exceptions in these hooks would stop Vitrail:
  they only switch the LODs off.
  Ghost LODs (0.3.1, 2026-11-01, user's server shot): switching Voxy off in its settings calls
  `voxy$shutdownRenderer`, `IVoxyRenderSystemHolder.getNullable()` turns null and VoxyFrame drew nothing, but
  `packReadSet` kept the last set, so the pack's vxDepthTex still saw LOD depth and shaded grey terrain outlines over
  the sky. `beforeOpaquePass` now clears `packReadSet` every frame. Reproduced in dev with a temporary
  shutdown-after-N-frames hook (not committed). Possibly also the "grey/black-white background" from the user's first
  server shot.
  Rejoin (0.3.2, user: "sometimes outlines when I join a world again"): `compositePack` exposed `packReadSet` before
  checking the pack's gbuffer targets (missing while Vitrail sets the pack up) = depth without colour; it is now set
  after them. And when Voxy built its renderer while the pack wasn't on, it ran its own pipeline and `packFrame` waited
  forever on `data().thePipeline`; now it calls `recreateRenderer()` (at most every 2 s). Verified in dev with a
  temporary hook that rebuilds Voxy's renderer once without the pack pipeline: LODs come back in colour.
  Shader reload (0.3.3, user: "after reloading shaders the LODs are completely gone"): (1) `dropPackPipeline` kept
  `packKey`, so the same pack coming back never rebuilt the pipeline; it now resets it (the key-change branch drops first,
  then sets the key). (2) While the pack switched off, Vitrail still held a pass open and the own-pipeline `render()`
  threw "Cannot add semaphore operation while inside RenderPass" -> `failed` for the session; it now calls
  `closeVitrailPasses()` first when Vitrail is installed. Verified with a temporary pack-off-for-100-frames hook.
  Grey LODs on every join after the first (user's server, "first join fine, second corrupted, shader reload fixes
  it"): SIRIS' `VitrailJoinReloadMixin` skipped Vitrail's pack reload on rejoin and the pack then drew the LODs without
  colour. SIRIS 1.1.4 keeps the reload when `viris` is loaded. Reproduced and verified with `-PtestRejoin=300`. A
  delayed VIRIS pipeline rebuild after join did not help (dropped). Sky light from Voxy Server Side is not the cause.
  Voxy is All-Rights-Reserved: never pack or modify its jar (runtime mixins only).

## Releases

`"C:\Program Files\GitHub CLI\gh.exe" release create <tag> <jar> -R laurensanp/viris --title "VIRIS <version> (Minecraft 26.3)"
--latest --notes-file <file>`. Only this mod's jar; Voxy and Vitrail are never attached.

Modrinth (token `MODRINTH_TOKEN` in `.env` of `../siris`, API v2, header `Authorization: <token>`): `viris` (id
IHYZKsvL, created 2026-11-01 by API, still waiting for review). New version: POST /v2/version multipart (`data` JSON +
`file`; version_type release, game_versions 26.3, loaders fabric, featured; deps: Voxy 0.2.20 version iIrM8ZKQ, Sodium,
Fabric API required, Vitrail optional), then PATCH /v3/version `client_only`. Gallery: 4 images.

Newest first:
- v0.3.5 (2026-11-02): Vulkan first when the Graphics API option is on its default. Modrinth TLmz6juf.
- v0.3.4 (2026-11-02): own repo, package `com.viris`; no change in the game. Modrinth NbOJ6O8J.
- v0.3.3 (2026-11-02): LODs survive shader reloads/toggles. Modrinth o1vibgM3.
- v0.3.2 (2026-11-01): pack sees LOD depth only with colour; Voxy pipeline re-requested. Modrinth ieYulT8H.
- v0.3.1 (2026-11-01): no ghost LODs after Voxy is switched off. Modrinth auUQfWsM.
- v0.3.0 (2026-11-01): voxy-vulkan renamed VIRIS, shader pack LODs.

## Working with the user

- Verify every change with screenshots as above. Ask before downloading third-party code.
- Install in Lunar only while its game isn't running, back up the mods folder first (`mods-backup-<date>-<what>/`).
