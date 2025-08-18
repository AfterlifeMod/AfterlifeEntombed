package com.dracolich777.afterlifeentombed.init;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.items.AnubisJudgement;
import com.dracolich777.afterlifeentombed.items.ArmorOfRa;
import com.dracolich777.afterlifeentombed.items.BreathOfShuItem;
import com.dracolich777.afterlifeentombed.items.CollarOfAnubis;
import com.dracolich777.afterlifeentombed.items.CreativeIcon;
import com.dracolich777.afterlifeentombed.items.CreativeIcon2;
import com.dracolich777.afterlifeentombed.items.CrownOfSeth;
import com.dracolich777.afterlifeentombed.items.DevWrench;
import com.dracolich777.afterlifeentombed.items.DusksandClump;
import com.dracolich777.afterlifeentombed.items.DusksteelAxeItem;
import com.dracolich777.afterlifeentombed.items.DusksteelHoeItem;
import com.dracolich777.afterlifeentombed.items.DusksteelIngot;
import com.dracolich777.afterlifeentombed.items.DusksteelPickaxeItem;
import com.dracolich777.afterlifeentombed.items.DusksteelShovelItem;
import com.dracolich777.afterlifeentombed.items.DusksteelTier;
import com.dracolich777.afterlifeentombed.items.GebsMight;
import com.dracolich777.afterlifeentombed.items.GodseekerSword;
import com.dracolich777.afterlifeentombed.items.GodsteelIngotOfRa;
import com.dracolich777.afterlifeentombed.items.GodsteelTemplate;
import com.dracolich777.afterlifeentombed.items.GodstoneOfAnubis;
import com.dracolich777.afterlifeentombed.items.GodstoneOfGeb;
import com.dracolich777.afterlifeentombed.items.GodstoneOfHorus;
import com.dracolich777.afterlifeentombed.items.GodstoneOfIsis;
import com.dracolich777.afterlifeentombed.items.GodstoneOfRa;
import com.dracolich777.afterlifeentombed.items.GodstoneOfSeth;
import com.dracolich777.afterlifeentombed.items.GodstoneOfShu;
import com.dracolich777.afterlifeentombed.items.GodstoneOfThoth;
import com.dracolich777.afterlifeentombed.items.HammerOfGeb;
import com.dracolich777.afterlifeentombed.items.HorusProtectionBow;
import com.dracolich777.afterlifeentombed.items.PricklyPearArmor;
import com.dracolich777.afterlifeentombed.items.PricklyPearArmorMaterial;
import com.dracolich777.afterlifeentombed.items.QuillOfThoth;
import com.dracolich777.afterlifeentombed.items.RaArmorMaterial;
import com.dracolich777.afterlifeentombed.items.RingOfRa;
import com.dracolich777.afterlifeentombed.items.RodOfKevin;
import com.dracolich777.afterlifeentombed.items.ScaleOfApep;
import com.dracolich777.afterlifeentombed.items.SethsTrickery;
import com.dracolich777.afterlifeentombed.items.ShabtiItem;
import com.dracolich777.afterlifeentombed.items.TokenOfHorus;
import com.dracolich777.afterlifeentombed.items.WandOfIsis;
import com.dracolich777.afterlifeentombed.items.WhipOfScorpionItem;

import net.minecraft.core.registries.Registries; // Import for Registries
import net.minecraft.network.chat.Component; // Import for Component
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTab; // Import for CreativeModeTab
import net.minecraft.world.item.CreativeModeTabs; // Import for CreativeModeTabs
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AfterlifeEntombedMod.MOD_ID);

    // DeferredRegister for CreativeModeTabs
    public static final RegistryObject<Item> GODSTONE_OF_RA = ITEMS.register("godstone_of_ra",
            () -> new GodstoneOfRa(new Item.Properties()));

    public static final RegistryObject<Item> RING_OF_RA = ITEMS.register("ring_of_ra",
            () -> new RingOfRa(new Item.Properties().stacksTo(1).fireResistant()));

    public static final RegistryObject<Item> GODSTONE_OF_HORUS = ITEMS.register("godstone_of_horus",
            () -> new GodstoneOfHorus(new Item.Properties()));

    public static final RegistryObject<Item> TOKEN_OF_HORUS = ITEMS.register("token_of_horus",
            () -> new TokenOfHorus(new Item.Properties().stacksTo(1).fireResistant()));

    public static final RegistryObject<Item> GODSTONE_OF_THOTH = ITEMS.register("godstone_of_thoth",
            () -> new GodstoneOfThoth(new Item.Properties()));

    public static final RegistryObject<Item> QUILL_OF_THOTH = ITEMS.register("quill_of_thoth",
            () -> new QuillOfThoth(new Item.Properties().stacksTo(1).fireResistant()));

    public static final RegistryObject<Item> CREATIVE_ICON = ITEMS.register("creative_icon",
            () -> new CreativeIcon(new Item.Properties().stacksTo(1).fireResistant()));

    public static final RegistryObject<Item> CREATIVE_ICON2 = ITEMS.register("creative_icon2",
            () -> new CreativeIcon2(new Item.Properties().stacksTo(1).fireResistant()));

    public static final RegistryObject<Item> GODSTONE_OF_SHU = ITEMS.register("godstone_of_shu",
            () -> new GodstoneOfShu(new Item.Properties()));

    public static final RegistryObject<Item> GODSTONE_OF_GEB = ITEMS.register("godstone_of_geb",
            () -> new GodstoneOfGeb(new Item.Properties()));

    public static final RegistryObject<Item> GODSTONE_OF_ISIS = ITEMS.register("godstone_of_isis",
            () -> new GodstoneOfIsis(new Item.Properties()));

    public static final RegistryObject<Item> GODSTONE_OF_SETH = ITEMS.register("godstone_of_seth",
            () -> new GodstoneOfSeth(new Item.Properties()));

    public static final RegistryObject<Item> GODSTONE_OF_ANUBIS = ITEMS.register("godstone_of_anubis",
            () -> new GodstoneOfAnubis(new Item.Properties()));

    public static final RegistryObject<Item> SCALE_OF_APEP = ITEMS.register("scale_of_apep",
            () -> new ScaleOfApep(new Item.Properties()));

    public static final RegistryObject<Item> HAMMER_OF_GEB = ITEMS.register("hammer_of_geb",
            () -> new HammerOfGeb(new Item.Properties().stacksTo(1).fireResistant()));

    public static final RegistryObject<Item> CROWN_OF_SETH = ITEMS.register("crown_of_seth",
            () -> new CrownOfSeth(new Item.Properties().stacksTo(1).fireResistant()));

    public static final RegistryObject<Item> BREATH_OF_SHU
            = ITEMS.register("breath_of_shu", () -> new BreathOfShuItem(new Item.Properties().stacksTo(1).fireResistant()));

    public static final RegistryObject<Item> ROD_OF_KEVIN
            = ITEMS.register("rod_of_kevin", () -> new RodOfKevin(new Item.Properties().stacksTo(16).fireResistant()));

    public static final RegistryObject<Item> WAND_OF_ISIS
            = ITEMS.register("wand_of_isis", () -> new WandOfIsis(new Item.Properties().stacksTo(1).fireResistant()));

    public static final RegistryObject<Item> GODSTEEL_INGOT_OF_RA = ITEMS.register("godsteel_ingot_of_ra",
            () -> new GodsteelIngotOfRa(new Item.Properties().stacksTo(64).fireResistant()));

    public static final RegistryObject<Item> GODSTEEL_TEMPLATE = ITEMS.register("godsteel_template",
            () -> new GodsteelTemplate(new Item.Properties().stacksTo(16).fireResistant()));

    public static final RegistryObject<Item> RA_HELMET = ITEMS.register("ra_helmet",
            () -> new ArmorOfRa(RaArmorMaterial.RA, ArmorItem.Type.HELMET, new Item.Properties().fireResistant()));

    public static final RegistryObject<Item> RA_CHESTPLATE = ITEMS.register("ra_chestplate",
            () -> new ArmorOfRa(RaArmorMaterial.RA, ArmorItem.Type.CHESTPLATE, new Item.Properties().fireResistant()));

    public static final RegistryObject<Item> RA_LEGGINGS = ITEMS.register("ra_leggings",
            () -> new ArmorOfRa(RaArmorMaterial.RA, ArmorItem.Type.LEGGINGS, new Item.Properties().fireResistant()));

    public static final RegistryObject<Item> RA_BOOTS = ITEMS.register("ra_boots",
            () -> new ArmorOfRa(RaArmorMaterial.RA, ArmorItem.Type.BOOTS, new Item.Properties().fireResistant()));

    public static final RegistryObject<Item> GODSEEKER_SWORD = ITEMS.register("godseeker_sword",
            () -> new GodseekerSword(new Item.Properties().stacksTo(1).durability(0).setNoRepair()));

    public static final RegistryObject<Item> DEV_WRENCH = ITEMS.register("dev_wrench",
            () -> new DevWrench(new Item.Properties().stacksTo(1).fireResistant()));

    public static final RegistryObject<Item> GEBS_MIGHT = ITEMS.register("gebs_might",
            () -> new GebsMight(new Item.Properties()));

    public static final RegistryObject<Item> SETHS_TRICKERY = ITEMS.register("seths_trickery",
            () -> new SethsTrickery(new Item.Properties().stacksTo(1).durability(0).setNoRepair().fireResistant()));

    public static final RegistryObject<Item> ANUBIS_JUDGEMENT = ITEMS.register("anubis_judgement",
            () -> new AnubisJudgement(new Item.Properties().stacksTo(1).durability(0).setNoRepair().fireResistant()));

    public static final RegistryObject<Item> COLLAR_OF_ANUBIS = ITEMS.register("collar_of_anubis",
            () -> new CollarOfAnubis());

    public static final RegistryObject<Item> DUSKSAND_CLUMP = ITEMS.register("dusksand_clump",
            () -> new DusksandClump(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> DUSKSTEEL_INGOT = ITEMS.register("dusksteel_ingot",
            () -> new DusksteelIngot(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<PricklyPearArmor> PRICKLY_PEAR_HELMET
            = ITEMS.register("prickly_pear_helmet",
                    () -> new PricklyPearArmor(PricklyPearArmorMaterial.PRICKLY_PEAR, ArmorItem.Type.HELMET,
                            new Item.Properties()));

    public static final RegistryObject<PricklyPearArmor> PRICKLY_PEAR_CHESTPLATE
            = ITEMS.register("prickly_pear_chestplate",
                    () -> new PricklyPearArmor(PricklyPearArmorMaterial.PRICKLY_PEAR, ArmorItem.Type.CHESTPLATE,
                            new Item.Properties()));

    public static final RegistryObject<PricklyPearArmor> PRICKLY_PEAR_LEGGINGS
            = ITEMS.register("prickly_pear_leggings",
                    () -> new PricklyPearArmor(PricklyPearArmorMaterial.PRICKLY_PEAR, ArmorItem.Type.LEGGINGS,
                            new Item.Properties()));

    public static final RegistryObject<PricklyPearArmor> PRICKLY_PEAR_BOOTS
            = ITEMS.register("prickly_pear_boots",
                    () -> new PricklyPearArmor(PricklyPearArmorMaterial.PRICKLY_PEAR, ArmorItem.Type.BOOTS,
                            new Item.Properties()));

public static final RegistryObject<DusksteelPickaxeItem> DUSKSTEEL_PICKAXE = ITEMS.register("dusksteel_pickaxe", 
    () -> new DusksteelPickaxeItem(DusksteelTier.DUSKSTEEL, 3, -2.8F, new Item.Properties()));

public static final RegistryObject<DusksteelAxeItem> DUSKSTEEL_AXE = ITEMS.register("dusksteel_axe", 
    () -> new DusksteelAxeItem(DusksteelTier.DUSKSTEEL, 6, -3.0F, new Item.Properties()));

public static final RegistryObject<DusksteelHoeItem> DUSKSTEEL_HOE = ITEMS.register("dusksteel_hoe", 
    () -> new DusksteelHoeItem(DusksteelTier.DUSKSTEEL, -3, 0.0F, new Item.Properties()));

public static final RegistryObject<DusksteelShovelItem> DUSKSTEEL_SHOVEL = ITEMS.register("dusksteel_shovel", 
    () -> new DusksteelShovelItem(DusksteelTier.DUSKSTEEL, 2, -3.0F, new Item.Properties()));


    public static final RegistryObject<Item> WHIP_OF_SCORPION = ITEMS.register("whip_of_scorpion", ()
            -> new WhipOfScorpionItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SHABTI = ITEMS.register("shabti",
            () -> new ShabtiItem(new Item.Properties().stacksTo(1).fireResistant()));

    public static final RegistryObject<Item> HORUS_PROTECTION_BOW = ITEMS.register("horus_protection_bow",
            () -> new HorusProtectionBow(new Item.Properties().stacksTo(1).durability(0).setNoRepair().fireResistant()));
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS
            = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AfterlifeEntombedMod.MOD_ID);

    // Registering the custom Creative Mode Tab
    public static final RegistryObject<CreativeModeTab> AFTERLIFE_ENTOMBED_TAB = CREATIVE_MODE_TABS.register("afterlife_entombed_tab",
            () -> CreativeModeTab.builder()
                    .withTabsBefore(CreativeModeTabs.COMBAT) // Position the tab before the Combat tab
                    .title(Component.translatable("Afterlife: Entombed")) // Set the display title for the tab
                    .icon(() -> CREATIVE_ICON2.get().getDefaultInstance()) // Set the icon of the tab to the Ring of Ra
                    .displayItems((parameters, output) -> {
                        // Add all custom items to this creative tab
                        output.accept(GODSTONE_OF_RA.get());
                        output.accept(RING_OF_RA.get());
                        output.accept(GODSTONE_OF_HORUS.get());
                        output.accept(TOKEN_OF_HORUS.get());
                        output.accept(GODSTONE_OF_THOTH.get());
                        output.accept(QUILL_OF_THOTH.get());
                        output.accept(GODSTONE_OF_SHU.get());
                        output.accept(BREATH_OF_SHU.get());
                        output.accept(GODSTONE_OF_GEB.get());
                        output.accept(HAMMER_OF_GEB.get());
                        output.accept(GODSTONE_OF_ISIS.get());
                        output.accept(WAND_OF_ISIS.get());
                        output.accept(GODSTONE_OF_SETH.get());
                        output.accept(CROWN_OF_SETH.get());
                        output.accept(GODSTONE_OF_ANUBIS.get());
                        output.accept(COLLAR_OF_ANUBIS.get());
                        output.accept(ROD_OF_KEVIN.get());
                        output.accept(GODSTEEL_TEMPLATE.get());
                        output.accept(GODSTEEL_INGOT_OF_RA.get());
                        output.accept(RA_HELMET.get());
                        output.accept(RA_CHESTPLATE.get());
                        output.accept(RA_LEGGINGS.get());
                        output.accept(RA_BOOTS.get());
                        output.accept(PRICKLY_PEAR_HELMET.get());
                        output.accept(PRICKLY_PEAR_CHESTPLATE.get());
                        output.accept(PRICKLY_PEAR_LEGGINGS.get());
                        output.accept(PRICKLY_PEAR_BOOTS.get());
                        output.accept(DUSKSTEEL_PICKAXE.get());
                        output.accept(DUSKSTEEL_AXE.get());
                        output.accept(DUSKSTEEL_SHOVEL.get());
                        output.accept(DUSKSTEEL_HOE.get());
                        output.accept(SETHS_TRICKERY.get());
                        output.accept(ANUBIS_JUDGEMENT.get());
                        output.accept(GEBS_MIGHT.get());
                        output.accept(HORUS_PROTECTION_BOW.get());
                        output.accept(DEV_WRENCH.get());
                        output.accept(SHABTI.get());
                        

                    })
                    .build());

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        CREATIVE_MODE_TABS.register(eventBus); // Register the creative mode tabs
    }
}
