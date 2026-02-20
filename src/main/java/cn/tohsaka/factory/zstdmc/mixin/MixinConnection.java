package cn.tohsaka.factory.zstdmc.mixin;

import cn.tohsaka.factory.zstdmc.codec.ZstdCompressionDecoder;
import cn.tohsaka.factory.zstdmc.codec.ZstdCompressionEncoder;
import io.netty.channel.Channel;
import net.minecraft.network.CompressionDecoder;
import net.minecraft.network.CompressionEncoder;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class MixinConnection{
    @Shadow
    private Channel channel;

    @Inject(method = "setupCompression",at = @At("HEAD"),cancellable = true)
    public void setupCompression(int threshold, boolean validateDecompressed, CallbackInfo ci) {
        if (threshold >= 0) {
            if (this.channel.pipeline().get("decompress") instanceof ZstdCompressionDecoder compressiondecoder) {
                compressiondecoder.setThreshold(threshold, validateDecompressed);
            } else {
                this.channel.pipeline().addAfter("splitter", "decompress", new ZstdCompressionDecoder(threshold, validateDecompressed));
            }

            if (this.channel.pipeline().get("compress") instanceof ZstdCompressionEncoder compressionencoder) {
                compressionencoder.setThreshold(threshold);
            } else {
                this.channel.pipeline().addAfter("prepender", "compress", new ZstdCompressionEncoder(threshold));
            }
        } else {
            if (this.channel.pipeline().get("decompress") instanceof ZstdCompressionDecoder) {
                this.channel.pipeline().remove("decompress");
            }

            if (this.channel.pipeline().get("compress") instanceof ZstdCompressionEncoder) {
                this.channel.pipeline().remove("compress");
            }
        }
        ci.cancel();
    }
}
