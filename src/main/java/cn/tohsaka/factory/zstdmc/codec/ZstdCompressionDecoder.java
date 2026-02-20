package cn.tohsaka.factory.zstdmc.codec;

import cn.tohsaka.factory.zstdmc.metric.ZstdMetric;
import com.github.luben.zstd.Zstd;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.CompressionDecoder;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.Inflater;

public class ZstdCompressionDecoder extends ByteToMessageDecoder {
    private int threshold;
    private boolean validateDecompressed;
    private static final int MAX_PACKET_SIZE = 16777216;
    public ZstdCompressionDecoder(int threshold, boolean validateDecompressed) {
        this.threshold = threshold;
        this.validateDecompressed = validateDecompressed;
    }
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() == 0) return;
        int decompressedLength = VarInt.read(in);
        if (decompressedLength == 0) {
            out.add(in.readRetainedSlice(in.readableBytes()));
        } else {
            if (decompressedLength < threshold) {
                throw new DecoderException("Zstd: Badly compressed packet - size of " + decompressedLength + " is below threshold of " + threshold);
            }
            if (decompressedLength > MAX_PACKET_SIZE) {
                throw new DecoderException("Zstd: Packet too large (" + decompressedLength + ")");
            }
            ByteBuf outBuf = ctx.alloc().directBuffer(decompressedLength);
            try {
                ByteBuffer dest = outBuf.nioBuffer(0, decompressedLength);
                int readable = in.readableBytes();
                ByteBuffer source = in.nioBuffer(in.readerIndex(), readable);
                long result = Zstd.decompress(dest, source);
                if (Zstd.isError(result)) {
                    throw new DecoderException("Zstd decompression failed: " + Zstd.getErrorName(result));
                }
                outBuf.writerIndex((int) result);
                in.skipBytes(in.readableBytes());
                out.add(outBuf);
                ZstdMetric.update(ctx,readable,result,0,0);
            } catch (Exception e) {
                /*outBuf.release();
                throw e;*/
                in.resetReaderIndex();
                out.add(in.readRetainedSlice(in.readableBytes()));
                e.printStackTrace();
            }
        }
    }

    public void setThreshold(int threshold, boolean validateDecompressed) {
        this.threshold = threshold;
        this.validateDecompressed = validateDecompressed;
    }
}