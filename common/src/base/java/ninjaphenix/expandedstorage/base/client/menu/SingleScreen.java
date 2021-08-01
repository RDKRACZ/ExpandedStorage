package ninjaphenix.expandedstorage.base.client.menu;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import ninjaphenix.expandedstorage.base.inventory.SingleMenu;
import ninjaphenix.expandedstorage.base.inventory.screen.SingleScreenMeta;

import java.util.Collections;
import java.util.List;

public final class SingleScreen extends AbstractScreen<SingleMenu, SingleScreenMeta> {
    public SingleScreen(SingleMenu container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title, (screenMeta) -> (screenMeta.width * 18 + 14) / 2 - 80);
        imageWidth = 14 + 18 * screenMeta.width;
        imageHeight = 17 + 97 + 18 * screenMeta.height;
    }

    public List<Rect2i> getExclusionZones() {
        return Collections.emptyList();
    }
}
