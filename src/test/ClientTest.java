package test;

import api.Client;
import impl.ClientImpl;
import org.junit.Before;
import org.junit.Test;
import utils.FileSystem;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ClientTest {
    static Client client;

    @Before
    public void setUp() {
        client = new ClientImpl();
    }

    @Test
    public void testWriteRead() {
        String filename = FileSystem.newFilename();
        int fd = client.open(filename, 0b11);
        client.append(fd, "hello".getBytes(StandardCharsets.UTF_8));
        for (int i = 0; i < "hello".getBytes(StandardCharsets.UTF_8).length; i++) {
            assertEquals("Block ID: " + fd + ". Read block bytes and appended bytes differ at the " + i
                    + " byte to the eof.", "hello".getBytes(StandardCharsets.UTF_8)[i], client.read(fd)[i]);
        }
        assertEquals(client.read(fd).length, 4096);
//        assertArrayEquals(client.read(fd), "hello".getBytes(StandardCharsets.UTF_8));
        client.append(fd, " world".getBytes(StandardCharsets.UTF_8));
        for (int i = 0; i < "hello world".getBytes(StandardCharsets.UTF_8).length; i++) {
            assertEquals("Block ID: " + fd + ". Read block bytes and appended bytes differ at the " + i
                    + " byte to the eof.", "hello world".getBytes(StandardCharsets.UTF_8)[i], client.read(fd)[i]);
        }
        assertEquals(client.read(fd).length, 4096);
//        assertArrayEquals(client.read(fd),"hello world".getBytes(StandardCharsets.UTF_8));
        client.close(fd);
    }

    @Test
    public void testWriteFail() {
        String filename = FileSystem.newFilename();
        int fd = client.open(filename, 0b01);
        client.append(fd, "Lala-land".getBytes(StandardCharsets.UTF_8));
        byte[] bytes = client.read(fd);
        for (byte aByte : bytes) {
            assertEquals(aByte, 0);
        }
//        assertArrayEquals(client.read(fd), "".getBytes(StandardCharsets.UTF_8));
        client.close(fd);
    }

    @Test
    public void testReadFail() {
        String filename = FileSystem.newFilename();
        int fd = client.open(filename, 0b10);
        assertNull(client.read(fd));
        client.close(fd);
    }
}
