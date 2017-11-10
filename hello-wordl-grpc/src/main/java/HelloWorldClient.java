import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.util.RoundRobinLoadBalancerFactory;
import nameresolver.AtomixNameResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloWorldClient {
    private static final Logger logger = LoggerFactory.getLogger(HelloWorldServer.class);

    public static void main(String[] args) throws Exception {
        ManagedChannel channel = ManagedChannelBuilder
                .forTarget("atomix://0.0.0.0:12345/service-helloworld")
                .nameResolverFactory(new AtomixNameResolverFactory())
                .loadBalancerFactory(RoundRobinLoadBalancerFactory.getInstance())
                .usePlaintext(true)
                .build();

        GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(channel);
        HelloReply resp = stub.sayHello(HelloRequest.newBuilder().setName("Aris").build());

        logger.info(resp.getMessage());

        channel.shutdown();
    }
}
