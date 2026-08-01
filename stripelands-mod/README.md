# Stripelands

A Fabric mod for Minecraft 26.2 that:

1. Truncates entity position and velocity to float32 precision on every write,
   replicating Bedrock Edition's IEEE 754 single-precision coordinate storage
   (and the "Stripe Lands" jitter/snapping it causes once magnitudes pass
   2^24 = 16,777,216 on X/Z).
2. Raises `WorldBorder`'s max settable size and bypasses the separate,
   hardcoded ±30,000,000 world edge, so you can build and travel past it.

## Why this isn't a literal "intercept the rasterizer" mod

The request asked to intercept coordinates "in the rendering and physics
engines" directly. That's not really achievable as a Fabric mod: the renderer
doesn't have one central place where "the coordinate" passes through — it's
computed fresh per-frame from entity/camera state, mesh vertices, and matrix
math scattered across dozens of classes. Chasing all of those down would be
fragile and mostly redundant.

The mixin in `EntityPrecisionMixin` does the same job more reliably: it
truncates the *source* position (`Entity#setPos`) that both physics
(bounding box / collision) and rendering (camera-relative rendering derives
from entity position) read from. Corrupt the source once, and the corruption
propagates everywhere downstream for free — which is functionally identical
to what you'd get chasing down every render call site, without needing to
touch the renderer at all.

## Setup

1. Push this folder to a new GitHub repo.
2. The included workflow (`.github/workflows/build.yml`) builds automatically
   on every push. Check the **Actions** tab, open the latest run, and download
   the `stripelands-mod` artifact — that's your built jar.
3. To build locally instead: install JDK 25, then run `gradle build`
   (or generate a wrapper first with `gradle wrapper --gradle-version 9.5.1`
   if you'd rather use `./gradlew`).
4. Drop the jar from `build/libs/` into your `.minecraft/mods` folder,
   alongside Fabric Loader 0.19.3+ and Fabric API for 26.2.

## World border removal is currently disabled

The first real test run confirmed `WorldBorderMixin`'s guess (`WorldBorder#getMaxSize`)
was wrong — Mixin couldn't find any method by that name in 26.2 and crashed the
integrated server on world load. Rather than guess a third name and cost you another
crash-and-relaunch cycle, both `WorldBorderMixin` and `LevelBoundsMixin` (which relies
on an equally unverified guess, `Level#isInWorldBounds`) have been removed from
`stripelands.mixins.json`'s `mixins` list. The `.java` files are still in the repo,
just inert — nothing references them until you re-add their names to that list.

**To find the real method names:** open [mcsrc.dev](https://mcsrc.dev), pick version
`26.2` (unobfuscated), open `net/minecraft/world/level/border/WorldBorder` and
`net/minecraft/world/level/Level`, and look for whatever method returns the
~5.9999968E7 max-size constant and whatever static method checks an X/Z position
against ±30,000,000. Send me the real signatures (or paste a screenshot/excerpt) and
I'll fix the two mixins properly instead of guessing again.

With those two disabled, the mod now only does the float-precision physics/rendering
part (`EntityPrecisionMixin` + `EntityVelocityMixin`) — that's the part confirmed to
load cleanly in your last run.

## Position/velocity mixins updated to verified real method names

`EntityPrecisionMixin` and `EntityVelocityMixin` originally targeted `setPos(DDD)V`
and `setDeltaMovement(Vec3)V` — reasonable guesses, but unverified. They now target
`setPosRaw(DDD)V` and `setDeltaMovement(DDD)V`, confirmed against a working
third-party mod (`setPosRaw` is also the better hook regardless: it's the terminal
method that actually stores the position field, so `setPos`/`move`/`teleport`/etc.
all funnel through it either way).

## Two more things fixed after the first test run

- **Don't put the `-sources.jar` in your mods folder.** Your log showed both
  `stripelands-0.1.0.jar` and `stripelands-0.1.0-sources.jar` loaded as separate
  mods. Only the non-sources jar belongs in `mods/` — the sources jar is a dev
  convenience (lets your IDE show real source when you attach the dependency), not
  something Fabric Loader needs to run.
- **The `${version}` placeholder wasn't getting substituted** into the built jar's
  `fabric.mod.json` (visible as a loader warning in your log). Rather than debug the
  Gradle resource-filtering pipeline, the version is now just hardcoded to `0.1.0`
  directly in `fabric.mod.json` — one less moving part.

## Things worth knowing before you build

- **Minecraft 26.2 ships fully unobfuscated** (Mojang switched to official
  names and year-based versioning this year). That means no Yarn mappings,
  no `modImplementation`/`remapJar` — this project already reflects that.
- **`LevelBoundsMixin` is the shakiest piece.** It targets
  `Level#isInWorldBounds`, which I'm reasonably but not 100% confident is
  still named that in 26.2's official mappings after this year's rendering/
  registration refactor. If Fabric Loader refuses to launch with a mixin
  application error, the log will name the exact class/method it expected —
  paste that into [mcsrc.dev](https://mcsrc.dev) (Fabric's own decompiled-
  source browser for unobfuscated 26.x builds) to find the current
  name/signature and fix the `@Inject` target.
- **Double-check version numbers before building.** `gradle.properties`
  has the latest Fabric Loader/API versions I could confirm as of this
  writing (Loader 0.19.3, Fabric API 0.154.2+26.2, Loom 1.17, Gradle 9.5.1).
  Fabric ships patch releases often — worth a quick check against
  https://fabricmc.net/develop before you build.
- Java 25 is a hard requirement for 26.2 mod development, both for Gradle's
  JVM and the compiled bytecode target.
