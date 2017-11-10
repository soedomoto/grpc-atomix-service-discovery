import io.atomix.AtomixReplica;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.server.storage.Storage;

import java.util.UUID;

public class AtomixServer {
    public static void main(String[] args) throws Exception {
        Address address = new Address("0.0.0.0", 12345);
        AtomixReplica replica = AtomixReplica.builder(address)
                .withTransport(new NettyTransport())
                .withStorage(Storage.builder()
                        .withDirectory(System.getProperty("user.dir") + "/logs/" + UUID.randomUUID().toString())
                        .build())
                .build();
        replica.bootstrap();
    }
}
