package net.fabricmc.fabric.api.brewing.v1;

import java.util.Objects;

import net.minecraft.item.Item;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.Potion;

import net.fabricmc.fabric.mixin.brewing.BrewingRecipeRegistryAccessor;

public final class FabricBrewingRecipeRegistry {
	public static void registerItemRecipe(PotionItem base, Item ingredient, PotionItem result) {
		Objects.requireNonNull(base, "base was null.");
		Objects.requireNonNull(ingredient, "ingredient was null.");
		Objects.requireNonNull(result, "result was null");

		BrewingRecipeRegistryAccessor.invokeRegisterItemRecipe(base, ingredient, result);
	}

	public static void registerPotionType(PotionItem item) {
		Objects.requireNonNull(item, "item was null");

		BrewingRecipeRegistryAccessor.invokeRegisterPotionType(item);
	}

	public static void registerPotionRecipe(Potion base, Item ingredient, PotionItem result) {

	}
}
