package cn.tohsaka.factory.zstdmc;

import cn.tohsaka.factory.zstdmc.metric.ZstdMetric;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static cn.tohsaka.factory.zstdmc.metric.DebugScreenAppender.formatBytes;

@SuppressWarnings("ALL")
@EventBusSubscriber(modid = Zstdmc.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // 注册 /zstdmc 基础命令
        dispatcher.register(Commands.literal("zstd")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("status")
                .executes(ModCommands::runStatus))
            .then(Commands.literal("reset")
                .executes(ModCommands::runReset))
            .then(Commands.literal("top10")
                .executes(ModCommands::listTop10))

        );
    }

    private static int listTop10(CommandContext<CommandSourceStack> ctx) {
        var playerList = ctx.getSource().getServer().getPlayerList();
        var map = ZstdMetric.getMap();
        var list = map.keySet().stream().sorted(Comparator.comparingLong((UUID uuid) -> map.get(uuid).getTxbytes2()).reversed()).limit(10).collect(Collectors.toUnmodifiableList());
        int i=0;
        List<String> msg = new ArrayList<>();
        for (UUID uuid : list) {
            i++;
            var entry = map.get(uuid);
            String rxFormatted = formatBytes(entry.getRxbytes2()) + "/" + formatBytes(entry.getRxbytes());
            String txFormatted = formatBytes(entry.getTxbytes()) + "/" + formatBytes(entry.getTxbytes2());
            var player = playerList.getPlayer(uuid);
            msg.add(String.format("[%d][%s]发送字节数:%s | 接收字节数:%s",i,player.getName().getString(),txFormatted,rxFormatted));
        }
        ctx.getSource().sendSuccess(() -> Component.literal(String.join("\n",msg)), true);
        return 1;
    }

    private static int runStatus(CommandContext<CommandSourceStack> context) {
        String rxFormatted = ChatFormatting.GRAY + "RX: " + ChatFormatting.WHITE + formatBytes(ZstdMetric.getRxbytes2()) + "/" + formatBytes(ZstdMetric.getRxbytes());
        String txFormatted = ChatFormatting.GRAY + "TX: " + ChatFormatting.WHITE + formatBytes(ZstdMetric.getTxbytes()) + "/" + formatBytes(ZstdMetric.getTxbytes2());
        context.getSource().sendSuccess(() -> Component.literal(rxFormatted + "\n" + txFormatted), true);
        return 1;
    }

    private static int runReset(CommandContext<CommandSourceStack> context) {
        ZstdMetric.reset();
        context.getSource().sendSuccess(() -> Component.literal("统计数据已重置"), true);
        return 1;
    }
}