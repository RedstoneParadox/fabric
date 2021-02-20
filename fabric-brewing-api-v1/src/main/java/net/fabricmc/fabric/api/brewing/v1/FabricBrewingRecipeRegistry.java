package net.fabricmc.fabric.api.brewing.v1;

import java.util.Objects;

import net.minecraft.item.Item;
import net.minecraft.item.PotionItem;

public final class FabricBrewingRecipeRegistry {
	public static void registerItemRecipe(PotionItem base, Item ingredient, PotionItem result) {
		Objects.requireNonNull(ingredient, "ingredient was null.");
	}
}
