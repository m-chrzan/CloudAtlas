CLASSPATH=$CLASSPATH:~/CloudAtlas/lib/cup.jar:~/CloudAtlas/lib/JLex.jar

java -cp ./lib/cup.jar:./lib/JLex.jar \
-Djava.rmi.server.hostname=localhost \
-Dfreshness_period=60000 \
-Dquery_period=5000 \
-Dgossip_period=5000 \
-DUDUPServer.hostname=localhost \
-DUDUPServer.port=5999 \
-DUDUPServer.timeout=5000 \
-DUDUPServer.bufsize=512 \
-DGossip.zone_strategy="RandomUniform" \
-Dzone_path="/uw/violet/07" \
-Dpublic_key_file="build/tmp/query_signer.pub" \
-jar ./build/libs/cloudatlas-agent.jar
