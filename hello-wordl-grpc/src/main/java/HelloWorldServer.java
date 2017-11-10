import io.atomix.Atomix;
import io.atomix.AtomixClient;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.group.DistributedGroup;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HelloWorldServer {
    private static final Logger logger = LoggerFactory.getLogger(HelloWorldServer.class);

    /* The port on which the server should run */
    private final int port;
    /* The list of address of the Atomix servers */
    private final List<Address> cluster;
    private Server server;

    public HelloWorldServer(int port, List<Address> cluster) {
        this.port = port;
        this.cluster = cluster;
    }

    public static void main(String[] args) throws Exception {
        List<Address> cluster = new ArrayList<>();
        cluster.add(new Address("0.0.0.0", 12345));

        // Starting a first server
        HelloWorldServer server1 = new HelloWorldServer(50051, cluster);
//        server1.start();

        // Starting a second server
        HelloWorldServer server2 = new HelloWorldServer(50052, cluster);
        server2.start();

//        server1.blockUntilShutdown();
        server2.blockUntilShutdown();
    }

    private void start() throws Exception {
        InetSocketAddress publishAddress = new InetSocketAddress("0.0.0.0", port);
        server = ServerBuilder
                .forPort(port)
                .addService(new GreeterImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);

        // Register
        AtomixClient client = AtomixClient.builder().withTransport(new NettyTransport()).build();
        Atomix atomix = client.connect(cluster).get();
        DistributedGroup group = atomix.getGroup("service-helloworld").get();
        // Add the address in metadata
        group.join(Collections.singletonMap("address", publishAddress)).get();

        // SDH
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                HelloWorldServer.this.stop();
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    private class GreeterImpl extends GreeterGrpc.GreeterImplBase {

        @Override
        public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
            System.err.println("Go a request, hello!!!");
            HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName() + " from " + port).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}