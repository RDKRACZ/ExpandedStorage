package ninjaphenix.expandedstorage;

import com.google.common.base.Suppliers;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import ninjaphenix.expandedstorage.internal_api.BaseApi;
import ninjaphenix.expandedstorage.internal_api.Utils;
import ninjaphenix.expandedstorage.base.inventory.PagedMenu;
import ninjaphenix.expandedstorage.base.inventory.ScrollableMenu;
import ninjaphenix.expandedstorage.base.inventory.SingleMenu;
import ninjaphenix.expandedstorage.base.item.StorageMutator;
import ninjaphenix.expandedstorage.wrappers.ConfigWrapper;
import ninjaphenix.expandedstorage.wrappers.NetworkWrapper;
import ninjaphenix.expandedstorage.wrappers.PlatformUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.FormattedMessage;

import java.util.function.Supplier;

public final class BaseCommon {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Supplier<MenuType<SingleMenu>> SINGLE_MENU_TYPE = Suppliers.memoize(() -> PlatformUtils.getInstance().createMenuType(Utils.SINGLE_SCREEN_TYPE, new SingleMenu.Factory()));
    public static final Supplier<MenuType<PagedMenu>> PAGE_MENU_TYPE = Suppliers.memoize(() -> PlatformUtils.getInstance().createMenuType(Utils.PAGED_SCREEN_TYPE, new PagedMenu.Factory()));
    public static final Supplier<MenuType<ScrollableMenu>> SCROLL_MENU_TYPE = Suppliers.memoize(() -> PlatformUtils.getInstance().createMenuType(Utils.SCROLLABLE_SCREEN_TYPE, new ScrollableMenu.Factory()));
    private static final int ICON_SUITABILITY = 0;

    private BaseCommon() {

    }

    static void initialize() {
        if (PlatformUtils.getInstance().isClient()) {
            ConfigWrapper.getInstance().initialise();
            BaseApi.getInstance().registerContainerButtonSettings(Utils.SINGLE_SCREEN_TYPE,
                    Utils.resloc("textures/gui/single_button.png"),
                    Utils.translation("screen.expandedstorage.single_screen"));
            BaseApi.getInstance().registerContainerButtonSettings(Utils.SCROLLABLE_SCREEN_TYPE,
                    Utils.resloc("textures/gui/scrollable_button.png"),
                    Utils.translation("screen.expandedstorage.scrollable_screen"));
            BaseApi.getInstance().registerContainerButtonSettings(Utils.PAGED_SCREEN_TYPE,
                    Utils.resloc("textures/gui/paged_button.png"),
                    Utils.translation("screen.expandedstorage.paged_screen"));
        }
        NetworkWrapper.getInstance().initialise();
        BaseApi.getInstance().offerTabIcon(Items.CHEST, ICON_SUITABILITY);
        BaseApi.getInstance().defineTierUpgradePath(Utils.translation("itemGroup.expandedstorage"), Utils.WOOD_TIER, Utils.IRON_TIER,
                Utils.GOLD_TIER, Utils.DIAMOND_TIER, Utils.OBSIDIAN_TIER, Utils.NETHERITE_TIER);
        BaseApi.getInstance().register(Utils.resloc("chest_mutator"), new StorageMutator(new Item.Properties().stacksTo(1).tab(Utils.TAB)));
    }

    public static ResourceLocation registerStat(ResourceLocation stat) {
        ResourceLocation rv = Registry.register(Registry.CUSTOM_STAT, stat, stat); // Forge doesn't provide registries for stats
        Stats.CUSTOM.get(rv);
        return rv;
    }

    public static void warnThrowableMessage(String message, Throwable cause, Object... messageParams) {
        LOGGER.warn(new FormattedMessage(message, messageParams, cause));
    }
}
