package cn.tohsaka.factory.zstdmc.metric;

import cn.tohsaka.factory.zstdmc.Zstdmc;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;

@SuppressWarnings("ALL")
@EventBusSubscriber(modid = Zstdmc.MODID, bus = EventBusSubscriber.Bus.GAME, value = net.neoforged.api.distmarker.Dist.CLIENT)
public class DebugScreenAppender {
    @SubscribeEvent
    public static void onGatherDebugText(CustomizeGuiOverlayEvent.DebugText event) {
        List<String> rightText = event.getRight();
        rightText.add("");
        rightText.add(ChatFormatting.AQUA + "[Zstd Metrics]");
        var player = Minecraft.getInstance().player;
        String rxFormatted = ChatFormatting.GRAY + "RX: " + ChatFormatting.WHITE + formatBytes(ZstdMetric.getRxbytes()) + "/" + formatBytes(ZstdMetric.getRxbytes2());
        String txFormatted = ChatFormatting.GRAY + "TX: " + ChatFormatting.WHITE + formatBytes(ZstdMetric.getTxbytes()) + "/" + formatBytes(ZstdMetric.getTxbytes2());
        rightText.add(rxFormatted);
        rightText.add(txFormatted);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onJoinServerEvent(PlayerEvent.PlayerLoggedInEvent loggedInEvent){
        ZstdMetric.reset();
    }

    public static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
