package nettyStudy.unit9;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Assert;
import org.junit.Test;

public class FixedLengthFrameDecoderTest {

    @Test
    public void testFramesDecoded(){
        ByteBuf buf = Unpooled.buffer();
        for(int i = 0;i<9;i++){
            buf.writeByte(i);
        }
        ByteBuf input = buf.duplicate();
        EmbeddedChannel channel = new EmbeddedChannel(new FixedLengthFrameDecoder(3));
        Assert.assertTrue(channel.writeInbound(input.retain()));
        Assert.assertTrue(channel.finish());
        ByteBuf read = (ByteBuf)channel.readInbound();
        Assert.assertEquals(buf.readSlice(3),read);
        read.release();

        read = (ByteBuf)channel.readInbound();
        Assert.assertEquals(buf.readSlice(3),read);
        read.release();

        read = (ByteBuf)channel.readInbound();
        Assert.assertEquals(buf.readSlice(3),read);
        read.release();

        Assert.assertNull(channel.readInbound());
        buf.release();
    }
}
