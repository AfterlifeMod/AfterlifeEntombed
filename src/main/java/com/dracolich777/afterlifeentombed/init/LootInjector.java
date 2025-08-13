package com.dracolich777.afterlifeentombed.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.dracolich777.afterlifeentombed.init.ModItems;

@Mod.EventBusSubscriber(modid = "yourmodid")
public class LootInjector {
    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation table = event.getName();

        if (!table.getPath().contains("chests")) return;

        LootPool.Builder poolBuilder = LootPool.lootPool()
            .name("godstone_injection")
            .setRolls(ConstantValue.exactly(1));

        if (table.getPath().contains("nether") || table.getPath().contains("bastion") || table.getPath().contains("fortress")) {
            addItem(poolBuilder, ModItems.GODSTONE_OF_SETH.get(), 0.15f);
            addItem(poolBuilder, ModItems.GODSTONE_OF_ANUBIS.get(), 0.15f);
        } else if (table.getPath().contains("village")) {
            addItem(poolBuilder, ModItems.GODSTONE_OF_ISIS.get(), 0.10f);
        } else if (table.getPath().contains("dungeon") || table.getPath().contains("mineshaft") || table.getPath().contains("stronghold")) {
            addItem(poolBuilder, ModItems.GODSTONE_OF_GEB.get(), 0.20f);
            addItem(poolBuilder, ModItems.GODSTONE_OF_SHU.get(), 0.20f);
        } else {
            addItem(poolBuilder, ModItems.GODSTONE_OF_RA.get(), 0.15f);
            addItem(poolBuilder, ModItems.GODSTONE_OF_HORUS.get(), 0.15f);
        }

        event.getTable().addPool(poolBuilder.build());
    }

    private static void addItem(LootPool.Builder pool, Item item, float chance) {
    pool.add(LootItem.lootTableItem(item)
        .when(LootItemRandomChanceCondition.randomChance(chance)));
}
}