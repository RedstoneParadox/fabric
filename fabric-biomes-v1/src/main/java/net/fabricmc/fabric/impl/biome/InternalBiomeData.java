/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.impl.biome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BuiltInBiomes;
import net.minecraft.world.biome.layer.BiomeLayers;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;
import net.minecraft.world.gen.feature.StructureFeature;

import net.fabricmc.fabric.api.biomes.v1.OverworldClimate;
import net.fabricmc.fabric.mixin.biome.VanillaLayeredBiomeSourceAccessor;

/**
 * Lists and maps for internal use only! Stores data that is used by the various mixins into the world generation
 */
public final class InternalBiomeData {
	private InternalBiomeData() {
	}

	private static final EnumMap<OverworldClimate, WeightedBiomePicker> OVERWORLD_MODDED_CONTINENTAL_BIOME_PICKERS = new EnumMap<>(OverworldClimate.class);
	private static final Map<RegistryKey<Biome>, WeightedBiomePicker> OVERWORLD_HILLS_MAP = new HashMap<>();
	private static final Map<RegistryKey<Biome>, WeightedBiomePicker> OVERWORLD_SHORE_MAP = new HashMap<>();
	private static final Map<RegistryKey<Biome>, WeightedBiomePicker> OVERWORLD_EDGE_MAP = new HashMap<>();
	private static final Map<RegistryKey<Biome>, VariantTransformer> OVERWORLD_VARIANT_TRANSFORMERS = new HashMap<>();
	private static final Map<RegistryKey<Biome>, RegistryKey<Biome>> OVERWORLD_RIVER_MAP = new HashMap<>();

	private static final Set<RegistryKey<Biome>> NETHER_BIOMES = new HashSet<>();
	private static final Map<RegistryKey<Biome>, Biome.MixedNoisePoint> NETHER_BIOME_NOISE_POINTS = new HashMap<>();

	public static void addOverworldContinentalBiome(OverworldClimate climate, RegistryKey<Biome> biome, double weight) {
		Preconditions.checkArgument(climate != null, "Climate is null");
		Preconditions.checkArgument(biome != null, "Biome is null");
		Preconditions.checkArgument(!Double.isNaN(weight), "Weight is NaN");
		Preconditions.checkArgument(weight > 0.0, "Weight is less than or equal to 0.0 (%s)", weight);
		InternalBiomeUtils.ensureIdMapping(biome);
		OVERWORLD_MODDED_CONTINENTAL_BIOME_PICKERS.computeIfAbsent(climate, k -> new WeightedBiomePicker()).addBiome(biome, weight);
		injectOverworldBiome(biome);
	}

	public static void addOverworldHillsBiome(RegistryKey<Biome> primary, RegistryKey<Biome> hills, double weight) {
		Preconditions.checkArgument(primary != null, "Primary biome is null");
		Preconditions.checkArgument(hills != null, "Hills biome is null");
		Preconditions.checkArgument(!Double.isNaN(weight), "Weight is NaN");
		Preconditions.checkArgument(weight > 0.0, "Weight is less than or equal to 0.0 (%s)", weight);
		InternalBiomeUtils.ensureIdMapping(primary);
		InternalBiomeUtils.ensureIdMapping(hills);
		OVERWORLD_HILLS_MAP.computeIfAbsent(primary, biome -> DefaultHillsData.injectDefaultHills(primary, new WeightedBiomePicker())).addBiome(hills, weight);
		injectOverworldBiome(hills);
	}

	public static void addOverworldShoreBiome(RegistryKey<Biome> primary, RegistryKey<Biome> shore, double weight) {
		Preconditions.checkArgument(primary != null, "Primary biome is null");
		Preconditions.checkArgument(shore != null, "Shore biome is null");
		Preconditions.checkArgument(!Double.isNaN(weight), "Weight is NaN");
		Preconditions.checkArgument(weight > 0.0, "Weight is less than or equal to 0.0 (%s)", weight);
		InternalBiomeUtils.ensureIdMapping(primary);
		InternalBiomeUtils.ensureIdMapping(shore);
		OVERWORLD_SHORE_MAP.computeIfAbsent(primary, biome -> new WeightedBiomePicker()).addBiome(shore, weight);
		injectOverworldBiome(shore);
	}

	public static void addOverworldEdgeBiome(RegistryKey<Biome> primary, RegistryKey<Biome> edge, double weight) {
		Preconditions.checkArgument(primary != null, "Primary biome is null");
		Preconditions.checkArgument(edge != null, "Edge biome is null");
		Preconditions.checkArgument(!Double.isNaN(weight), "Weight is NaN");
		Preconditions.checkArgument(weight > 0.0, "Weight is less than or equal to 0.0 (%s)", weight);
		InternalBiomeUtils.ensureIdMapping(primary);
		InternalBiomeUtils.ensureIdMapping(edge);
		OVERWORLD_EDGE_MAP.computeIfAbsent(primary, biome -> new WeightedBiomePicker()).addBiome(edge, weight);
		injectOverworldBiome(edge);
	}

	public static void addOverworldBiomeReplacement(RegistryKey<Biome> replaced, RegistryKey<Biome> variant, double chance, OverworldClimate[] climates) {
		Preconditions.checkArgument(replaced != null, "Replaced biome is null");
		Preconditions.checkArgument(variant != null, "Variant biome is null");
		Preconditions.checkArgument(chance > 0 && chance <= 1, "Chance is not greater than 0 or less than or equal to 1");
		InternalBiomeUtils.ensureIdMapping(replaced);
		InternalBiomeUtils.ensureIdMapping(variant);
		OVERWORLD_VARIANT_TRANSFORMERS.computeIfAbsent(replaced, biome -> new VariantTransformer()).addBiome(variant, chance, climates);
		injectOverworldBiome(variant);
	}

	public static void setOverworldRiverBiome(RegistryKey<Biome> primary, RegistryKey<Biome> river) {
		Preconditions.checkArgument(primary != null, "Primary biome is null");
		InternalBiomeUtils.ensureIdMapping(primary);
		InternalBiomeUtils.ensureIdMapping(river);
		OVERWORLD_RIVER_MAP.put(primary, river);

		if (river != null) {
			injectOverworldBiome(river);
		}
	}

	/**
	 * Adds the biomes in world gen to the array for the vanilla layered biome source.
	 * This helps with {@link VanillaLayeredBiomeSource#hasStructureFeature(StructureFeature)} returning correctly for modded biomes as well as in {@link VanillaLayeredBiomeSource#getTopMaterials()}}
	 */
	private static void injectOverworldBiome(RegistryKey<Biome> biome) {
		List<RegistryKey<Biome>> biomes = VanillaLayeredBiomeSourceAccessor.getBIOMES();

		if (biomes instanceof ImmutableList) {
			biomes = new ArrayList<>(biomes);
			VanillaLayeredBiomeSourceAccessor.setBIOMES(biomes);
		}

		biomes.add(biome);
	}

	public static void addNetherBiome(RegistryKey<Biome> biome, Biome.MixedNoisePoint spawnNoisePoint) {
		Preconditions.checkArgument(biome != null, "Biome is null");
		Preconditions.checkArgument(spawnNoisePoint != null, "Biome.MixedNoisePoint is null");
		InternalBiomeUtils.ensureIdMapping(biome);
		NETHER_BIOMES.add(biome);
		NETHER_BIOME_NOISE_POINTS.put(biome, spawnNoisePoint);
	}

	public static Map<RegistryKey<Biome>, WeightedBiomePicker> getOverworldHills() {
		return OVERWORLD_HILLS_MAP;
	}

	public static Map<RegistryKey<Biome>, WeightedBiomePicker> getOverworldShores() {
		return OVERWORLD_SHORE_MAP;
	}

	public static Map<RegistryKey<Biome>, WeightedBiomePicker> getOverworldEdges() {
		return OVERWORLD_EDGE_MAP;
	}

	public static Map<RegistryKey<Biome>, RegistryKey<Biome>> getOverworldRivers() {
		return OVERWORLD_RIVER_MAP;
	}

	public static EnumMap<OverworldClimate, WeightedBiomePicker> getOverworldModdedContinentalBiomePickers() {
		return OVERWORLD_MODDED_CONTINENTAL_BIOME_PICKERS;
	}

	public static Map<RegistryKey<Biome>, VariantTransformer> getOverworldVariantTransformers() {
		return OVERWORLD_VARIANT_TRANSFORMERS;
	}

	public static Set<RegistryKey<Biome>> getNetherBiomes() {
		return Collections.unmodifiableSet(NETHER_BIOMES);
	}

	public static Map<RegistryKey<Biome>, Biome.MixedNoisePoint> getNetherBiomeNoisePoints() {
		return NETHER_BIOME_NOISE_POINTS;
	}

	private static class DefaultHillsData {
		private static final ImmutableMap<RegistryKey<Biome>, RegistryKey<Biome>> DEFAULT_HILLS;

		static WeightedBiomePicker injectDefaultHills(RegistryKey<Biome> base, WeightedBiomePicker picker) {
			RegistryKey<Biome> defaultHill = DEFAULT_HILLS.get(base);

			if (defaultHill != null) {
				picker.addBiome(defaultHill, 1);
			} else if (BiomeLayers.areSimilar(InternalBiomeUtils.getRawId(base), InternalBiomeUtils.getRawId(BuiltInBiomes.WOODED_BADLANDS_PLATEAU))) {
				picker.addBiome(BuiltInBiomes.BADLANDS, 1);
			} else if (base == BuiltInBiomes.DEEP_OCEAN || base == BuiltInBiomes.DEEP_LUKEWARM_OCEAN || base == BuiltInBiomes.DEEP_COLD_OCEAN) {
				picker.addBiome(BuiltInBiomes.PLAINS, 1);
				picker.addBiome(BuiltInBiomes.FOREST, 1);
			} else if (base == BuiltInBiomes.DEEP_FROZEN_OCEAN) {
				// Note: Vanilla Deep Frozen Oceans only have a 1/3 chance of having default hills.
				// This is a clever trick that ensures that when a mod adds hills with a weight of 1, the 1/3 chance is fulfilled.
				// 0.5 + 1.0 = 1.5, and 0.5 / 1.5 = 1/3.

				picker.addBiome(BuiltInBiomes.PLAINS, 0.25);
				picker.addBiome(BuiltInBiomes.FOREST, 0.25);
			} else if (base == BuiltInBiomes.PLAINS) {
				picker.addBiome(BuiltInBiomes.WOODED_HILLS, 1);
				picker.addBiome(BuiltInBiomes.FOREST, 2);
			}

			return picker;
		}

		static {
			// This map mirrors the hardcoded logic in AddHillsLayer#sample
			ImmutableMap.Builder<RegistryKey<Biome>, RegistryKey<Biome>> builder = ImmutableMap.builder();
			builder.put(BuiltInBiomes.DESERT, BuiltInBiomes.DESERT_HILLS);
			builder.put(BuiltInBiomes.FOREST, BuiltInBiomes.WOODED_HILLS);
			builder.put(BuiltInBiomes.BIRCH_FOREST, BuiltInBiomes.BIRCH_FOREST_HILLS);
			builder.put(BuiltInBiomes.DARK_FOREST, BuiltInBiomes.PLAINS);
			builder.put(BuiltInBiomes.TAIGA, BuiltInBiomes.TAIGA_HILLS);
			builder.put(BuiltInBiomes.GIANT_TREE_TAIGA, BuiltInBiomes.GIANT_TREE_TAIGA_HILLS);
			builder.put(BuiltInBiomes.SNOWY_TAIGA, BuiltInBiomes.SNOWY_TAIGA_HILLS);
			builder.put(BuiltInBiomes.SNOWY_TUNDRA, BuiltInBiomes.SNOWY_MOUNTAINS);
			builder.put(BuiltInBiomes.JUNGLE, BuiltInBiomes.JUNGLE_HILLS);
			builder.put(BuiltInBiomes.BAMBOO_JUNGLE, BuiltInBiomes.BAMBOO_JUNGLE_HILLS);
			builder.put(BuiltInBiomes.OCEAN, BuiltInBiomes.DEEP_OCEAN);
			builder.put(BuiltInBiomes.LUKEWARM_OCEAN, BuiltInBiomes.DEEP_LUKEWARM_OCEAN);
			builder.put(BuiltInBiomes.COLD_OCEAN, BuiltInBiomes.DEEP_COLD_OCEAN);
			builder.put(BuiltInBiomes.FROZEN_OCEAN, BuiltInBiomes.DEEP_FROZEN_OCEAN);
			builder.put(BuiltInBiomes.MOUNTAINS, BuiltInBiomes.WOODED_MOUNTAINS);
			builder.put(BuiltInBiomes.SAVANNA, BuiltInBiomes.SAVANNA_PLATEAU);
			DEFAULT_HILLS = builder.build();
		}
	}
}
