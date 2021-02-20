package net.fabricmc.fabric.mixin.brewing;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.BrewingRecipeRegistry;

@Mixin(BrewingRecipeRegistry.class)
public interface BrewingRecipeRegistryAccessor {
	@Invoker
	static void invokeRegisterItemRecipe(Item item, Item item2, Item item3){
		throw new UnsupportedOperationException();
	}
	@Invoker
	static void invokeRegisterPotionType(Item item){
		throw new UnsupportedOperationException();
	}
    @Invoker
	static void invokeRegisterPotionRecipe(Potion potion, Item item, Potion potion2){
		throw new UnsupportedOperationException();
    }
}
