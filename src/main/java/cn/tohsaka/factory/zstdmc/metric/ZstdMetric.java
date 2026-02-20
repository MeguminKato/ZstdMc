package cn.tohsaka.factory.zstdmc.metric;

import cn.tohsaka.factory.zstdmc.Zstdmc;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public class ZstdMetric {
    private static ConcurrentHashMap<UUID,MetricData> map = new ConcurrentHashMap<>();
    private static LongAdder rxbytes = new LongAdder();
    private static LongAdder txbytes = new LongAdder();
    private static LongAdder rxbytes2 = new LongAdder();
    private static LongAdder txbytes2 = new LongAdder();

    public static void reset(){
        map.clear();
        rxbytes.reset();
        rxbytes2.reset();
        txbytes.reset();
        txbytes2.reset();
    }

    public static void update(ChannelHandlerContext ctx, long rx, long rx2, long tx, long tx2){
        Connection connection = (Connection) ctx.channel().pipeline().get("packet_handler");
        if (connection != null) {
            var listener = connection.getPacketListener();
            if(listener instanceof ServerGamePacketListenerImpl serverGamePacketListener){
                var player = serverGamePacketListener.getPlayer();
                var uuid = player.getUUID();
                if(!map.containsKey(uuid)){
                    map.put(uuid,new MetricData());
                }
                map.get(uuid).update(player,player.getName().getString(),player.getIpAddress(),rx,rx2,tx,tx2);
            }
            rxbytes.add(rx);
            txbytes.add(tx);
            rxbytes2.add(rx2);
            txbytes2.add(tx2);
        }
    }

    public static Long getRxbytes() {
        return rxbytes.longValue();
    }
    public static Long getTxbytes() {
        return txbytes.longValue();
    }
    public static Long getRxbytes2() {
        return rxbytes2.longValue();
    }
    public static Long getTxbytes2() {
        return txbytes2.longValue();
    }

    public static Map<UUID, MetricData> getMap() {
        return Collections.unmodifiableMap(map);
    }
}
