package ninjaphenix.expandedstorage.base.wrappers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkHooks;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;
import ninjaphenix.expandedstorage.base.internal_api.Utils;
import ninjaphenix.expandedstorage.base.internal_api.inventory.ServerMenuFactory;
import ninjaphenix.expandedstorage.base.internal_api.inventory.SyncedMenuFactory;
import ninjaphenix.expandedstorage.base.inventory.PagedMenu;
import ninjaphenix.expandedstorage.base.inventory.ScrollableMenu;
import ninjaphenix.expandedstorage.base.inventory.SingleMenu;
import ninjaphenix.expandedstorage.base.network.OpenSelectScreenMessage;
import ninjaphenix.expandedstorage.base.network.RemovePlayerPreferenceCallbackMessage;
import ninjaphenix.expandedstorage.base.network.RequestOpenSelectScreenMessage;
import ninjaphenix.expandedstorage.base.network.ScreenTypeUpdateMessage;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

final class NetworkWrapperImpl implements NetworkWrapper {
    private static NetworkWrapperImpl INSTANCE;
    private final Map<UUID, Consumer<ResourceLocation>> preferenceCallbacks = new HashMap<>();
    private final Map<UUID, ResourceLocation> playerPreferences = new HashMap<>();
    private final Map<ResourceLocation, ServerMenuFactory> menuFactories = Utils.unmodifiableMap(map -> {
        map.put(Utils.SINGLE_SCREEN_TYPE, SingleMenu::new);
        map.put(Utils.SCROLLABLE_SCREEN_TYPE, ScrollableMenu::new);
        map.put(Utils.PAGED_SCREEN_TYPE, PagedMenu::new);
    });
    private SimpleChannel channel;

    public static NetworkWrapperImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NetworkWrapperImpl();
        }
        return INSTANCE;
    }

    @SubscribeEvent
    public static void onPlayerConnected(ClientPlayerNetworkEvent.LoggedInEvent event) {
        NetworkWrapper.getInstance().c2s_sendTypePreference(ConfigWrapper.getInstance().getPreferredScreenType());
    }

    @SubscribeEvent
    public static void onPlayerDisconnected(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) { // Probably called on both sides.
            NetworkWrapper.getInstance().s_setPlayerScreenType(player, Utils.UNSET_SCREEN_TYPE);
        }
    }

    public void initialise() {
        String channelVersion = "2";
        channel = NetworkRegistry.newSimpleChannel(Utils.resloc("channel"), () -> channelVersion, channelVersion::equals, channelVersion::equals);

        channel.registerMessage(0, ScreenTypeUpdateMessage.class, ScreenTypeUpdateMessage::encode, ScreenTypeUpdateMessage::decode, ScreenTypeUpdateMessage::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(1, RequestOpenSelectScreenMessage.class, RequestOpenSelectScreenMessage::encode, RequestOpenSelectScreenMessage::decode, RequestOpenSelectScreenMessage::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(2, RemovePlayerPreferenceCallbackMessage.class, RemovePlayerPreferenceCallbackMessage::encode, RemovePlayerPreferenceCallbackMessage::decode, RemovePlayerPreferenceCallbackMessage::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(3, OpenSelectScreenMessage.class, OpenSelectScreenMessage::encode, OpenSelectScreenMessage::decode, OpenSelectScreenMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        if (PlatformUtils.getInstance().isClient()) {
            MinecraftForge.EVENT_BUS.addListener(NetworkWrapperImpl::onPlayerConnected);
        }
        MinecraftForge.EVENT_BUS.addListener(NetworkWrapperImpl::onPlayerDisconnected);
    }

    @Override
    public void c2s_removeTypeSelectCallback() {
        ClientPacketListener listener = Minecraft.getInstance().getConnection();
        if (listener != null && channel.isRemotePresent(listener.getConnection())) {
            //noinspection InstantiationOfUtilityClass
            channel.sendToServer(new RemovePlayerPreferenceCallbackMessage());
        }
    }

    @Override
    public void c2s_openTypeSelectScreen() {
        ClientPacketListener listener = Minecraft.getInstance().getConnection();
        if (listener != null && channel.isRemotePresent(listener.getConnection())) {
            //noinspection InstantiationOfUtilityClass
            channel.sendToServer(new RequestOpenSelectScreenMessage());
        }
    }

    @Override
    public void c2s_setSendTypePreference(ResourceLocation selection) {
        if (ConfigWrapper.getInstance().setPreferredScreenType(selection)) {
            this.c2s_sendTypePreference(selection);
        }
    }

    @Override
    public void s2c_openMenu(ServerPlayer player, SyncedMenuFactory menuFactory) {
        UUID uuid = player.getUUID();
        if (playerPreferences.containsKey(uuid) && this.isValidScreenType(playerPreferences.get(uuid))) {
            NetworkHooks.openGui(player, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return menuFactory.getMenuTitle();
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player1) {
                    return menuFactory.createMenu(windowId, inventory, (ServerPlayer) player1);
                }
            }, buffer -> menuFactory.writeClientData(player, buffer));
        } else {
            this.s2c_openSelectScreen(player, (type) -> this.s2c_openMenu(player, menuFactory));
        }
    }

    @Override
    public void s2c_openSelectScreen(ServerPlayer player, @Nullable Consumer<ResourceLocation> playerPreferenceCallback) {
        if (playerPreferenceCallback != null) {
            preferenceCallbacks.put(player.getUUID(), playerPreferenceCallback);
        }
        ServerGamePacketListenerImpl listener = player.connection;
        // todo: is listener always non-null?
        if (listener != null && channel.isRemotePresent(listener.getConnection())) {
            channel.send(PacketDistributor.PLAYER.with(() -> player), new OpenSelectScreenMessage(menuFactories.keySet()));
        }
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, BlockPos pos, Container container, Inventory inventory, Component containerName) {
        UUID uuid = inventory.player.getUUID();
        ResourceLocation playerPreference;
        if (playerPreferences.containsKey(uuid) && menuFactories.containsKey(playerPreference = playerPreferences.get(uuid))) {
            return menuFactories.get(playerPreference).create(windowId, pos, container, inventory, containerName);
        }
        return null;
    }

    @Override
    public boolean isValidScreenType(ResourceLocation screenType) {
        return screenType != null && menuFactories.containsKey(screenType);
    }

    @Override
    public void c2s_sendTypePreference(ResourceLocation selection) {
        ClientPacketListener listener = Minecraft.getInstance().getConnection();
        if (listener != null && channel.isRemotePresent(listener.getConnection())) {
            channel.sendToServer(new ScreenTypeUpdateMessage(selection));
        }
    }

    @Override
    public void s_setPlayerScreenType(ServerPlayer player, ResourceLocation screenType) {
        UUID uuid = player.getUUID();
        if (menuFactories.containsKey(screenType)) {
            playerPreferences.put(uuid, screenType);
            if (preferenceCallbacks.containsKey(uuid)) {
                preferenceCallbacks.remove(uuid).accept(screenType);
            }
        } else {
            playerPreferences.remove(uuid);
            preferenceCallbacks.remove(uuid);
        }
    }

    @Override
    public void removeTypeSelectCallback(ServerPlayer player) {
        preferenceCallbacks.remove(player.getUUID());
    }
}
