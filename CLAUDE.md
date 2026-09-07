# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Minecraft/NeoForge Sources

The decompiled Java sources for Minecraft/NeoForge (1.21.1, `net.neoforged.moddev` plugin) are already extracted to `C:\Users\delvi\.gradle\mc-sources\1.21.1-neoforge\` (normal package layout, e.g. `net/minecraft/world/entity/LivingEntity.java`) — read directly from there with Read/Grep/Glob instead of asking the user.

If missing or needing regeneration, the source jar is in the NeoForm cache at `~/.gradle/caches/neoformruntime/intermediate_results/mergeWithSources_*_output.jar` (pick the most recent by date) — extract it with `unzip` into the folder above, discarding the `.class` files.

## Wiki

The wiki is pulled in the same folder as this repo, with .wiki at the end
