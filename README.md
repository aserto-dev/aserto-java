# java-aserto
Java library for Aserto services


### Run unit tests
```
mvn clean test
```

### Run integration tests
In order to run integration tests we need to start [topaz](https://github.com/aserto-dev/topaz)
```bash
go install github.com/topaz/cmd/topaz@latest
topaz install
topaz configure -d -s -r ghcr.io/aserto-policies/policy-todo-rebac:latest todo
topaz run
```
and then run the integration tests
```
mvn test -Pintegration
```

## Example
Start [topaz](https://github.com/aserto-dev/topaz)

```java
# create a channel that has the connection details
ManagedChannel channel = new ChannelBuilder()
        .withAddr("localhost:8282")
        .withInsecure(true)
        .build();

# create
AuthzClient authzClient =  new AuthzClient(channel);

# identity context contains information abou the user that requests access to some resource
IdentityCtx identityCtx = new IdentityCtx("rick@the-citadel.com", IdentityType.IDENTITY_TYPE_SUB);

# contains information about the policy we want to check for the provided identity
PolicyCtx policyCtx = new PolicyCtx("todo", "todo", "todoApp.DELETE.todos.__id", new String[]{"allowed"});

# check if the identity is allowed to perform the action
List<Decision> decisions = client.is(identityCtx, policyCtx);
client.close();

decisions.forEach(decision -> {
    String dec = decision.getDecision();
    boolean isAllowed =  decision.getIs();
    System.out.println("For decision [" + dec + "] the answer was [" + isAllowed + "]");
});
```
