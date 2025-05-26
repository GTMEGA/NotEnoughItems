# NotEnoughItems MEGA

Fork of the GTNewHorizons NotEnoughItems Unofficial, with pack-specific changes for the MEGA modpack. Please use the GTNH forks in your pack,
as this fork has functionality changes that don't really fit outside MEGA.

## MEGA changes

* Migrated to unified buildscript
* Moved some of the API calls from GTNHLib to Blendtronic to reduce number of dependencies


## Branch structure

* master
  * Pack-specific changes that may be divergent from upstream. Based on upstream/migrated-buildscript.
* migrated-buildscript
  * Full feature parity with upstream with simple migrations: Buildscript swap, GTNHLib -> Blendtronic, CCC from MEGA maven.
* upstream-tracking
  * The latest dev branch from the GTNH NEI repo that the repo is managed against. Usually the `master` branch from GTNH.

After the initial branch creations, EVERY upstream merge to the above branches MUST be done via merge commits. DO NOT rebase.

## Versioning scheme

To avoid divergence, we use an upstream-tracking "differential" versioning scheme:

- GTNH release: 2.7.0-GTNH
- First MEGA release: 2.7.0-mega
- Second MEGA release: 2.7.0-mega2
- GTNH updates: 2.7.1-GTNH
- Third MEGA release (not yet rebased): 2.7.0-mega3
- Fourth MEGA release (after rebase): 2.7.1-mega
- and so on...

## GTNH changes below

## New Features:

* Speed
  - Uses a parallel stream to search the item list over multiple cores, resulting in 2-6x faster searches on average
  - Loads the recipe handlers in parallel
* A textbox for search with most of the features you'd expect - moving forward, backwards, selection, etc
* Bookmarks! Are you in the process of crafting? Bookmark it using either 'A' or configure your own key.
* Toggle bookmark pane.  Default shortcut key `B`.  Item Subsets menu is only available if bookmarks are not visible.
* Utility/Cheat buttons line up and wrap based on GUI size
* ItemList is no longer regenerated from the ItemRegistry on every inventory load
* JEI (Or Creative) Style tabs [Optional]  Note: Requires explicit support to be added for an ItemStack to render, otherwise falls back to the first two letters of the handler name.
* Tabs/Handlers are loaded from a CSV config in the JAR (or optionally from the config folder).  NBT IMCEvents `registerHandlerInfo` and `removeHandlerInfo` are available for mod authors to add handler information, using the same fields as the CSV file
* `@[Mod]->[item]` searching.  ex: `@Mod.gregtech->iron ingot`
* Cycle between Recipe, Utility, and Cheat mode by ctrl clicking on the Wrench Icon
* GT5u Tools/Items and GT6 tools should now properly work with the Overlay Recipe Transfer

## Other items of note:

* Remove TMI style
* Removed inventory Load/Save state

## License

MEGA Modifications Copyright (c) 2025 MEGA Team

GTNH Modifications Copyright (c) 2019-2024 mitchej123 and the GTNH Team

Licensed under LGPL-3.0 or later - use this however you want, but please give back any modifications

Parts inspired/borrowed/backported from [JEI](https://github.com/mezz/JustEnoughItems/tree/1.12) under the MIT License.

Originial code Copyright (c) 2014-2015 mezz and was licensed under the MIT License.
