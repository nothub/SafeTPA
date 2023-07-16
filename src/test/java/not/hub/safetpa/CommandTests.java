package not.hub.safetpa;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CommandTests {
    private ServerMock server;
    private Plugin plugin;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Plugin.class);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void tpa() {
        PlayerMock playerA = server.addPlayer("aaaaa");
        PlayerMock playerB = server.addPlayer("bbbbb");
        playerA.performCommand("/tpa bbbbb");
        System.out.println(playerA.nextMessage());
        System.out.println(playerA.nextComponentMessage());
        System.out.println(playerB.nextMessage());
        System.out.println(playerB.nextComponentMessage());
    }
}
