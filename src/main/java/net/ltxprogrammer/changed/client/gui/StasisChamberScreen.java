package net.ltxprogrammer.changed.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.block.entity.StasisChamberBlockEntity;
import net.ltxprogrammer.changed.entity.beast.CustomLatexEntity;
import net.ltxprogrammer.changed.process.ProcessTransfur;
import net.ltxprogrammer.changed.util.EntityUtil;
import net.ltxprogrammer.changed.world.inventory.StasisChamberMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class StasisChamberScreen extends AbstractContainerScreen<StasisChamberMenu> {
    private final StasisChamberMenu menu;

    public StasisChamberScreen(StasisChamberMenu menu, Inventory inventory, Component text) {
        super(menu, inventory, text);
        this.menu = menu;
        this.imageWidth = 285;
        this.imageHeight = 166;

        menu.requestUpdate();
    }

    private static final ResourceLocation texture = Changed.modResource("textures/gui/stasis_chamber.png");

    private ButtonScope buttonScope = ButtonScope.DEFAULT;

    @Override
    public void render(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
        this.menu.hideSlots = buttonScope == ButtonScope.MODIFICATIONS;

        this.renderBackground(ms);
        super.render(ms, mouseX, mouseY, partialTicks);

        final int textLeftMargin = this.leftPos + 176;
        final int textRightMargin = this.leftPos + 278;
        final int textTopMargin = this.topPos + 6;
        final int commandsTopMargin = this.topPos + 24;
        final int commandsBottomMargin = this.topPos + 136;

        if (buttonScope == ButtonScope.PROGRAMS) {
            menu.getConfiguredTransfurVariant().ifPresent(variant -> {
                Gui.drawCenteredString(ms, Minecraft.getInstance().font, variant.getEntityType().getDescription(), (textLeftMargin + textRightMargin) / 2, textTopMargin, 0xFFFFFF);
            });

            AtomicInteger yOffset = new AtomicInteger(0);
            menu.getCurrentCommand().ifPresent(command -> {
                Gui.drawString(ms, Minecraft.getInstance().font, command.getActiveDisplayText(), textLeftMargin, commandsTopMargin + yOffset.getAndAdd(12), 0x20FF20);
            });
            menu.getScheduledCommands().forEach(command -> {
                Gui.drawString(ms, Minecraft.getInstance().font, command.getDisplayText(), textLeftMargin, commandsTopMargin + yOffset.getAndAdd(12), 0xFFFFFF);
            });
        }

        else {
            menu.getChamberedEntity().ifPresent(entity -> {
                Gui.drawCenteredString(ms, Minecraft.getInstance().font, entity.getDisplayName(), (textLeftMargin + textRightMargin) / 2, textTopMargin, 0xFFFFFF);

                int entityX = (textLeftMargin + textRightMargin) / 2;
                int entityY = commandsBottomMargin - 5;
                InventoryScreen.renderEntityInInventory(entityX, entityY, 40,
                        (float)(this.leftPos) - mouseX + entityX,
                        (float)(this.topPos) - mouseY + (entityY - 100),
                        EntityUtil.maybeGetOverlaying(entity));
            });
        }

        this.renderTooltip(ms, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack ms, float partialTicks, int gx, int gy) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShaderTexture(0, texture);
        blit(ms, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth + 18, this.imageHeight);

        this.menu.slots.forEach(slot -> {
            if (slot.isActive()) {
                blit(ms, this.leftPos + slot.x - 1, this.topPos + slot.y - 1, this.imageWidth, 0, 18, 18, this.imageWidth + 18, this.imageHeight);
            }
        });

        RenderSystem.setShaderTexture(0, Changed.modResource("textures/gui/progress_bar_back.png"));
        blit(ms, this.leftPos + 217, this.topPos + 144, 0, 0, 48, 12, 48, 12);

        float progress = menu.getFluidLevel(partialTicks);
        RenderSystem.setShaderTexture(0, Changed.modResource("textures/gui/progress_bar_front.png"));
        blit(ms, this.leftPos + 217, this.topPos + 144, 0, 0, (int)(48 * progress), 12, 48, 12);

        RenderSystem.disableBlend();
    }

    @Override
    public boolean keyPressed(int key, int b, int c) {
        if (key == 256) {
            this.minecraft.player.closeContainer();
            return true;
        }

        return super.keyPressed(key, b, c);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
    }

    @Override
    public void onClose() {
        super.onClose();
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
    }

    public enum ButtonScope {
        DEFAULT,
        PROGRAMS,
        MODIFICATIONS;

        public ButtonScope toggleScope(ButtonScope intendedScope) {
            if (this == DEFAULT)
                return intendedScope;
            if (this == intendedScope)
                return DEFAULT;

            return intendedScope;
        }
    }

    private boolean initialized = false;
    private Button programsButton;
    private Button modifyButton;
    private Button openDoorButton;
    private Button closeDoorButton;
    private Button captureNextEntityButton;
    private Button toggleStasisButton;

    private List<Button> programButtons = new ArrayList<>();
    private List<Button> modificationButtons = new ArrayList<>();

    private void checkButtonStates() {
        if (!initialized) return;

        boolean open = menu.isChamberOpen();
        programsButton.active = true;
        modifyButton.active = true;
        openDoorButton.active = !open;
        openDoorButton.visible = buttonScope == ButtonScope.DEFAULT;
        closeDoorButton.active = open;
        closeDoorButton.visible = buttonScope == ButtonScope.DEFAULT;
        captureNextEntityButton.active = true;
        captureNextEntityButton.visible = buttonScope == ButtonScope.DEFAULT;
        toggleStasisButton.active = true;
        toggleStasisButton.visible = buttonScope == ButtonScope.DEFAULT;

        programButtons.forEach(button -> button.visible = buttonScope == ButtonScope.PROGRAMS);
        modificationButtons.forEach(button -> button.visible = buttonScope == ButtonScope.MODIFICATIONS);
    }

    @Override
    public void containerTick() {
        super.containerTick();

        checkButtonStates();
    }

    @Override
    public void init() {
        super.init();

        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);

        int leftMargin = this.leftPos + 7;
        int topMargin = this.topPos + 7;
        int buttonWidth = 79;
        int buttonHeight = 20;
        int buttonHeightSpacing = buttonHeight + 4;
        int buttonWidthSpacing = buttonWidth + 4;

        programsButton = this.addRenderableWidget(new Button(leftMargin, topMargin, buttonWidth, buttonHeight, new TranslatableComponent("changed.stasis.programs"), button -> {
            buttonScope = buttonScope.toggleScope(ButtonScope.PROGRAMS);
        }));

        modifyButton = this.addRenderableWidget(new Button(leftMargin + buttonWidthSpacing, topMargin, buttonWidth, buttonHeight, new TranslatableComponent("changed.stasis.modify"), button -> {
            buttonScope = buttonScope.toggleScope(ButtonScope.MODIFICATIONS);
            //this.minecraft.setScreen(new StasisChamberModifyScreen(menu, this));
        }));

        openDoorButton = this.addRenderableWidget(new Button(leftMargin, topMargin + buttonHeightSpacing, buttonWidth, buttonHeight, new TranslatableComponent("changed.stasis.open"), button -> {
            menu.sendSimpleCommand(StasisChamberMenu.Command.OPEN_DOOR);
        }));

        closeDoorButton = this.addRenderableWidget(new Button(leftMargin + buttonWidthSpacing, topMargin + buttonHeightSpacing, buttonWidth, buttonHeight, new TranslatableComponent("changed.stasis.close"), button -> {
            menu.sendSimpleCommand(StasisChamberMenu.Command.CLOSE_DOOR);
        }));

        captureNextEntityButton = this.addRenderableWidget(new Button(leftMargin, topMargin + buttonHeightSpacing * 2, buttonWidth, buttonHeight, new TranslatableComponent("changed.stasis.capture_next"), button -> {
            menu.inputProgram("captureNextEntity");
        }));

        toggleStasisButton = this.addRenderableWidget(new Button(leftMargin + buttonWidthSpacing, topMargin + buttonHeightSpacing * 2, buttonWidth, buttonHeight, new TranslatableComponent("changed.stasis.toggle_stasis"), button -> {
            menu.inputProgram("toggleStasis");
        }));

        programButtons.add(this.addRenderableWidget(new Button(leftMargin, topMargin + buttonHeightSpacing, buttonWidth, buttonHeight, new TranslatableComponent("changed.stasis.program.transfur"), button -> {
            menu.inputProgram("transfur");
        })));

        programButtons.add(this.addRenderableWidget(new Button(leftMargin + buttonWidthSpacing, topMargin + buttonHeightSpacing, buttonWidth, buttonHeight, new TranslatableComponent("changed.stasis.program.modify"), button -> {
            menu.inputProgram("modify");
        })));

        modificationButtons.add(this.addRenderableWidget(new Button(leftMargin, topMargin + buttonHeightSpacing, buttonWidth, buttonHeight, new TranslatableComponent("changed.stasis.modify.torso", CustomLatexEntity.TorsoType.fromFlags(menu.getConfiguredCustomLatex()).name()), button -> {
            int flags = menu.getConfiguredCustomLatex();
            var next = CustomLatexEntity.TorsoType.fromFlags(flags).cycle();
            menu.inputCustomLatexConfig(next.setFlags(flags));
            button.setMessage(new TranslatableComponent("changed.stasis.modify.torso", next.name()));
        })));

        modificationButtons.add(this.addRenderableWidget(new Button(leftMargin + buttonWidthSpacing, topMargin + buttonHeightSpacing, buttonWidth, buttonHeight, new TranslatableComponent("changed.stasis.modify.hair", CustomLatexEntity.HairType.fromFlags(menu.getConfiguredCustomLatex()).name()), button -> {
            int flags = menu.getConfiguredCustomLatex();
            var next = CustomLatexEntity.HairType.fromFlags(flags).cycle();
            menu.inputCustomLatexConfig(next.setFlags(flags));
            button.setMessage(new TranslatableComponent("changed.stasis.modify.hair", next.name()));
        })));

        modificationButtons.add(this.addRenderableWidget(new Button(leftMargin, topMargin + buttonHeightSpacing * 2, buttonWidth, buttonHeight, new TranslatableComponent("changed.stasis.modify.ears", CustomLatexEntity.EarType.fromFlags(menu.getConfiguredCustomLatex()).name()), button -> {
            int flags = menu.getConfiguredCustomLatex();
            var next = CustomLatexEntity.EarType.fromFlags(flags).cycle();
            menu.inputCustomLatexConfig(next.setFlags(flags));
            button.setMessage(new TranslatableComponent("changed.stasis.modify.ears", next.name()));
        })));

        modificationButtons.add(this.addRenderableWidget(new Button(leftMargin + buttonWidthSpacing, topMargin + buttonHeightSpacing * 2, buttonWidth, buttonHeight, new TranslatableComponent("changed.stasis.modify.tail", CustomLatexEntity.TailType.fromFlags(menu.getConfiguredCustomLatex()).name()), button -> {
            int flags = menu.getConfiguredCustomLatex();
            var next = CustomLatexEntity.TailType.fromFlags(flags).cycle();
            menu.inputCustomLatexConfig(next.setFlags(flags));
            button.setMessage(new TranslatableComponent("changed.stasis.modify.tail", next.name()));
        })));

        modificationButtons.add(this.addRenderableWidget(new Button(leftMargin, topMargin + buttonHeightSpacing * 3, buttonWidth, buttonHeight, new TranslatableComponent("changed.stasis.modify.legs", CustomLatexEntity.LegType.fromFlags(menu.getConfiguredCustomLatex()).name()), button -> {
            int flags = menu.getConfiguredCustomLatex();
            var next = CustomLatexEntity.LegType.fromFlags(flags).cycle();
            menu.inputCustomLatexConfig(next.setFlags(flags));
            button.setMessage(new TranslatableComponent("changed.stasis.modify.legs", next.name()));
        })));

        modificationButtons.add(this.addRenderableWidget(new Button(leftMargin + buttonWidthSpacing, topMargin + buttonHeightSpacing * 3, buttonWidth, buttonHeight, new TranslatableComponent("changed.stasis.modify.arms", CustomLatexEntity.ArmType.fromFlags(menu.getConfiguredCustomLatex()).name()), button -> {
            int flags = menu.getConfiguredCustomLatex();
            var next = CustomLatexEntity.ArmType.fromFlags(flags).cycle();
            menu.inputCustomLatexConfig(next.setFlags(flags));
            button.setMessage(new TranslatableComponent("changed.stasis.modify.arms", next.name()));
        })));

        modificationButtons.add(this.addRenderableWidget(new Button(leftMargin, topMargin + buttonHeightSpacing * 4, buttonWidth, buttonHeight, new TranslatableComponent("changed.stasis.modify.scale", CustomLatexEntity.ScaleType.fromFlags(menu.getConfiguredCustomLatex()).name()), button -> {
            int flags = menu.getConfiguredCustomLatex();
            var next = CustomLatexEntity.ScaleType.fromFlags(flags).cycle();
            menu.inputCustomLatexConfig(next.setFlags(flags));
            button.setMessage(new TranslatableComponent("changed.stasis.modify.scale", next.name()));
        })));

        initialized = true;

        checkButtonStates();
    }
}