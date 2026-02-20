package cn.tohsaka.factory.zstdmc.codec;

import cn.tohsaka.factory.zstdmc.Config;
import cn.tohsaka.factory.zstdmc.metric.ZstdMetric;
import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdInputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.VarInt;
import net.neoforged.neoforge.logging.PacketDump;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class ZstdCompressionEncoder extends MessageToByteEncoder<ByteBuf> {
    private int threshold;
    public ZstdCompressionEncoder(int threshold){
        this.threshold = threshold;
    }
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        int readableBytes = msg.readableBytes();
        if (readableBytes < threshold) {
            VarInt.write(out, 0);
            out.writeBytes(msg);
        } else {
            VarInt.write(out, readableBytes);
            ByteBuffer source = msg.nioBuffer();

            int maxCompressedSize = (int) Zstd.compressBound(readableBytes);
            out.ensureWritable(maxCompressedSize);
            ByteBuffer dest = out.nioBuffer(out.writerIndex(), out.writableBytes());
            int compressedSize = Zstd.compress(
                    dest,
                    source,
                    Config.getLevel()
            );
            out.writerIndex(out.writerIndex() + compressedSize);
            ZstdMetric.update(ctx,0,0,readableBytes,compressedSize);
        }
    }
    public int getThreshold() {
        return this.threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
}
