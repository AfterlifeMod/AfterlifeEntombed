package com.dracolich777.afterlifeentombed.init;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.blockentities.GodHoldBlockEntity;
import com.dracolich777.afterlifeentombed.blockentities.GodforgeBlockEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AfterlifeEntombedMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<GodHoldBlockEntity>> GODHOLD_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("godhold_block_entity", () ->
                    BlockEntityType.Builder.of(GodHoldBlockEntity::new,
                            ModBlocks.GODHOLD.get()).build(null));

    public static final RegistryObject<BlockEntityType<GodforgeBlockEntity>> GODFORGE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("godforge_block_entity", () ->
                    BlockEntityType.Builder.of(GodforgeBlockEntity::new,
                            ModBlocks.GODFORGE.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
