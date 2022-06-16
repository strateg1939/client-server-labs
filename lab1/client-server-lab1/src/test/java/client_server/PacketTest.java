package client_server;

import client_server.ciphers.Crc16Checker;
import client_server.exceptions.IllegalPacketException;
import client_server.models.MessageObject;
import client_server.models.Product;
import org.junit.Before;
import org.junit.Test;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for Packet.
 */
public class PacketTest {
    private static final int DEFAULT_LENGTH = 100;
    private ByteBuffer byteBuffer;
    @Before
    public void setupBuffer(){
        byteBuffer = ByteBuffer.allocate(DEFAULT_LENGTH);
        byteBuffer.put(Packet.bMagic);
    }


    @Test(expected = IllegalPacketException.class)
    public void packetThrowsIfMagicByteIsIncorrect() {
        byte[] array = new byte[1];
        array[0] = 0;

        Packet packet = new Packet(array);
    }
    @Test(expected = IllegalPacketException.class)
    public void packetThrowsIfCrc16IsIncorrect() {
        byteBuffer.put((byte) 0);
        byteBuffer.putLong(0L);
        byteBuffer.putInt(0);
        Crc16Checker crc16Checker = new Crc16Checker();
        short crc16 = (short) (crc16Checker.createCrc16(byteBuffer.array()) + 1);
        byteBuffer.putShort(crc16);

        Packet packet = new Packet(byteBuffer.array());
    }
    @Test(expected = BufferUnderflowException.class)
    public void packetThrowsIfArrayIncorrectLength() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES);
        byteBuffer.put(Packet.bMagic);
        byteBuffer.put(Byte.MAX_VALUE);
        byteBuffer.putShort(Short.MAX_VALUE);

        Packet packet = new Packet(byteBuffer.array());
    }
    @Test(expected = IllegalPacketException.class)
    public void packetThrowsIfCrc16OfMessageIsWrong() {
        Message message = new Message(0,0, new MessageObject());
        byte[] array = createByteArrayWithRightCrc16((byte) 0, 0l, message);
        array[array.length - 1]--;

        Packet packet = new Packet(array);
    }
    @Test
    public void packetReturnsRightIds(){
        byte expectedSrcId = 1;
        long expectedPacketId = 1000;
        Message message = new Message(0,0, new MessageObject());
        byte[] array = createByteArrayWithRightCrc16(expectedSrcId, expectedPacketId, message);

        Packet packet = new Packet(array);

        assertEquals(expectedSrcId, packet.getSrcId());
        assertEquals(expectedPacketId, packet.getPacketId());
    }
    @Test
    public void packetReturnsRightMessageIds(){
        int expectedCommand = 14;
        int expectedUserId = 15;
        Message message = new Message(expectedCommand, expectedUserId, new MessageObject());
        byte[] array = createByteArrayWithRightCrc16((byte) 0, 0, message);

        Packet packet = new Packet(array);

        assertEquals(expectedCommand, packet.getMessage().getCommandType());
        assertEquals(expectedUserId, packet.getMessage().getUserId());
    }
    @Test
    public void packetReturnsRightMessageObject(){
        MessageObject expectedMessageObject = new MessageObject("expected");
        Message message = new Message(0,0, expectedMessageObject);
        byte[] array = createByteArrayWithRightCrc16((byte) 0, 0, message);

        Packet packet = new Packet(array);

        assertEquals(expectedMessageObject.getMessageString(), packet.getMessage().getMessageObject().getMessageString());
    }


    private byte[] createByteArrayWithRightCrc16(byte srcId, long packetId, Message message) {
        PacketSerializer packetSerializer = new PacketSerializer(message, srcId, packetId);
        return packetSerializer.getPacket();
    }
}
