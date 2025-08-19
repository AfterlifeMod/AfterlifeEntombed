package com.dracolich777.afterlifeentombed.init;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.blocks.DuatPortalBlock;
import com.dracolich777.afterlifeentombed.blocks.DusksandBlock;
import com.dracolich777.afterlifeentombed.blocks.DusksteelBlock;
// import com.dracolich777.afterlifeentombed.blocks.GodHoldBlock;
import com.dracolich777.afterlifeentombed.blocks.SethCurseBlock;
import com.dracolich777.afterlifeentombed.blocks.GebCurseBlock;
import com.dracolich777.afterlifeentombed.blocks.ThothCurseBlock;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS
            = DeferredRegister.create(ForgeRegistries.BLOCKS, AfterlifeEntombedMod.MOD_ID);

    public static final DeferredRegister<Item> ITEMS
            = DeferredRegister.create(ForgeRegistries.ITEMS, AfterlifeEntombedMod.MOD_ID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS
            = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AfterlifeEntombedMod.MOD_ID);

    // Register DUSKSAND block and item
    public static final RegistryObject<Block> DUSKSAND = BLOCKS.register("dusksand",
            DusksandBlock::new);

    public static final RegistryObject<Item> DUSKSAND_ITEM = ITEMS.register("dusksand",
            () -> new BlockItem(DUSKSAND.get(), new Item.Properties()));

    public static final RegistryObject<Block> DUSKSTEEL = BLOCKS.register("dusksteel",
            DusksteelBlock::new);

    public static final RegistryObject<Item> DUSKSTEEL_ITEM = ITEMS.register("dusksteel",
            () -> new BlockItem(DUSKSTEEL.get(), new Item.Properties()));

    public static final RegistryObject<Block> DUAT_PORTAL = BLOCKS.register("duat_portal",
            DuatPortalBlock::new);

    public static final RegistryObject<Item> DUAT_PORTAL_ITEM = ITEMS.register("duat_portal",
            () -> new BlockItem(DUAT_PORTAL.get(), new Item.Properties()));

    public static final RegistryObject<Block> SETH_CURSE_BLOCK = BLOCKS.register("seth_curse_block",
            SethCurseBlock::new);

    public static final RegistryObject<Item> SETH_CURSE_BLOCK_ITEM = ITEMS.register("seth_curse_block",
            () -> new BlockItem(SETH_CURSE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> GEB_CURSE_BLOCK = BLOCKS.register("geb_curse_block",
            GebCurseBlock::new);

    public static final RegistryObject<Item> GEB_CURSE_BLOCK_ITEM = ITEMS.register("geb_curse_block",
            () -> new BlockItem(GEB_CURSE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> THOTH_CURSE_BLOCK = BLOCKS.register("thoth_curse_block",
            ThothCurseBlock::new);

    public static final RegistryObject<Item> SHU_CURSE_BLOCK_ITEM = ITEMS.register("thoth_curse_block",
            () -> new BlockItem(THOTH_CURSE_BLOCK.get(), new Item.Properties()));

//     public static final RegistryObject<Block> GODHOLD = BLOCKS.register("godhold",
//             GodHoldBlock::new);

//     public static final RegistryObject<Item> GODHOLD_ITEM = ITEMS.register("godhold",
//             () -> new BlockItem(GODHOLD.get(), new Item.Properties()));

    // Register the Creative Tab
    public static final RegistryObject<CreativeModeTab> AFTERLIFE_ENTOMBED_TAB = CREATIVE_MODE_TABS.register(
            "afterlife_entombed_blocks_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("Afterlife: Entombed Blocks"))
                    .icon(() -> new ItemStack(DUSKSAND_ITEM.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(DUSKSAND_ITEM.get());
                        output.accept(DUSKSTEEL_ITEM.get());
                        output.accept(SETH_CURSE_BLOCK_ITEM.get());
                        output.accept(GEB_CURSE_BLOCK_ITEM.get());
                        output.accept(DUAT_PORTAL_ITEM.get());
                        // output.accept(GODHOLD_ITEM.get());
                    })
                    .build()
    );

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        CREATIVE_MODE_TABS.register(eventBus);  // This was missing in your code
    }
}
