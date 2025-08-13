// package com.dracolich777.afterlifeentombed.client.gui;

// import com.dracolich777.afterlifeentombed.mobs.ShabtiEntity;
// import com.dracolich777.afterlifeentombed.network.NetworkHandler;
// import com.dracolich777.afterlifeentombed.network.ShabtiConfigPacket;
// import com.mojang.blaze3d.systems.RenderSystem;
// import com.mojang.blaze3d.vertex.PoseStack;
// import net.minecraft.client.gui.components.Button;
// import net.minecraft.client.gui.components.Checkbox;
// import net.minecraft.client.gui.screens.Screen;
// import net.minecraft.client.renderer.GameRenderer;
// import net.minecraft.network.chat.Component;
// import net.minecraft.resources.ResourceLocation;

// public class ShabtiConfigScreen extends Screen {

//     private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("afterlifeentombed", "textures/gui/shabti_config.png");
//     private static final int GUI_WIDTH = 176;
//     private static final int GUI_HEIGHT = 166;

//     private final ShabtiEntity shabtiEntity;
//     private int leftPos;
//     private int topPos;

//     // Checkboxes for display options
//     private Checkbox showArmorCheckbox;
//     private Checkbox showItemsCheckbox;
//     private Checkbox showRotationCheckbox;
//     private Checkbox showHealthCheckbox;
//     private Checkbox showHungerCheckbox;
//     private Checkbox showXpCheckbox;
//     private Checkbox showCoordsCheckbox;
//     private Checkbox showDimensionCheckbox;

//     public ShabtiConfigScreen(ShabtiEntity shabtiEntity) {
//         super(Component.literal("Shabti Configuration"));
//         this.shabtiEntity = shabtiEntity;
//     }

//     @Override
//     protected void init() {
//         super.init();

//         this.leftPos = (this.width - GUI_WIDTH) / 2;
//         this.topPos = (this.height - GUI_HEIGHT) / 2;

//         // Create checkboxes for each display option
//         int checkboxX = leftPos + 20;
//         int startY = topPos + 30;
//         int spacing = 18;

//         showArmorCheckbox = new Checkbox(checkboxX, startY, 20, 20, Component.literal("Show Armor"),
//                 shabtiEntity.hasDisplayFlag(ShabtiEntity.SHOW_ARMOR));
//         this.addRenderableWidget(showArmorCheckbox);

//         showItemsCheckbox = new Checkbox(checkboxX, startY + spacing, 20, 20, Component.literal("Show Items"),
//                 shabtiEntity.hasDisplayFlag(ShabtiEntity.SHOW_ITEMS));
//         this.addRenderableWidget(showItemsCheckbox);

//         showRotationCheckbox = new Checkbox(checkboxX, startY + spacing * 2, 20, 20, Component.literal("Mirror Rotation"),
//                 shabtiEntity.hasDisplayFlag(ShabtiEntity.SHOW_ROTATION));
//         this.addRenderableWidget(showRotationCheckbox);

//         showHealthCheckbox = new Checkbox(checkboxX, startY + spacing * 3, 20, 20, Component.literal("Show Health"),
//                 shabtiEntity.hasDisplayFlag(ShabtiEntity.SHOW_HEALTH));
//         this.addRenderableWidget(showHealthCheckbox);

//         showHungerCheckbox = new Checkbox(checkboxX, startY + spacing * 4, 20, 20, Component.literal("Show Hunger"),
//                 shabtiEntity.hasDisplayFlag(ShabtiEntity.SHOW_HUNGER));
//         this.addRenderableWidget(showHungerCheckbox);

//         showXpCheckbox = new Checkbox(checkboxX, startY + spacing * 5, 20, 20, Component.literal("Show Experience"),
//                 shabtiEntity.hasDisplayFlag(ShabtiEntity.SHOW_XP));
//         this.addRenderableWidget(showXpCheckbox);

//         showCoordsCheckbox = new Checkbox(checkboxX, startY + spacing * 6, 20, 20, Component.literal("Show Coordinates"),
//                 shabtiEntity.hasDisplayFlag(ShabtiEntity.SHOW_COORDS));
//         this.addRenderableWidget(showCoordsCheckbox);

//         showDimensionCheckbox = new Checkbox(checkboxX, startY + spacing * 7, 20, 20, Component.literal("Show Dimension"),
//                 shabtiEntity.hasDisplayFlag(ShabtiEntity.SHOW_DIMENSION));
//         this.addRenderableWidget(showDimensionCheckbox);

//         // Add Done button
//         this.addRenderableWidget(new Button(leftPos + GUI_WIDTH - 80, topPos + GUI_HEIGHT - 30, 60, 20,
//                 Component.literal("Done"), button -> this.onClose()));
//     }

//     @Override
//     public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
//         this.renderBackground(poseStack);

//         // Render the background GUI texture
//         RenderSystem.setShader(GameRenderer::getPositionTexShader);
//         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//         RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
//         this.blit(poseStack, leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT);

//         // Render title
//         this.font.draw(poseStack, this.title, leftPos + 8, topPos + 6, 4210752);

//         super.render(poseStack, mouseX, mouseY, partialTick);
//     }

//     @Override
//     public void onClose() {
//         // Send configuration to server
//         int flags = 0;

//         if (showArmorCheckbox.selected()) {
//             flags |= ShabtiEntity.SHOW_ARMOR;
//         }
//         if (showItemsCheckbox.selected()) {
//             flags |= ShabtiEntity.SHOW_ITEMS;
//         }
//         if (showRotationCheckbox.selected()) {
//             flags |= ShabtiEntity.SHOW_ROTATION;
//         }
//         if (showHealthCheckbox.selected()) {
//             flags |= ShabtiEntity.SHOW_HEALTH;
//         }
//         if (showHungerCheckbox.selected()) {
//             flags |= ShabtiEntity.SHOW_HUNGER;
//         }
//         if (showXpCheckbox.selected()) {
//             flags |= ShabtiEntity.SHOW_XP;
//         }
//         if (showCoordsCheckbox.selected()) {
//             flags |= ShabtiEntity.SHOW_COORDS;
//         }
//         if (showDimensionCheckbox.selected()) {
//             flags |= ShabtiEntity.SHOW_DIMENSION;
//         }

//         NetworkHandler.INSTANCE.sendToServer(new ShabtiConfigPacket(shabtiEntity.getId(), flags));

//         super.onClose();
//     }

//     @Override
//     public boolean isPauseScreen() {
//         return false;
//     }
// }
