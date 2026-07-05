from ghcr.io/graalvm/native-image-community:25 AS image
# ENV JAVA_TOOL_OPTIONS="-Djava.net.preferIPv4Stack=true" // Its because ipv6 is closed in my region, you dont need this :)
workdir /app
copy gradle/ gradle/
copy gradlew build.gradle settings.gradle ./
copy src/ src/
copy META-INF/native-image/org.Amin/Contact-API/reachability-metadata.json META-INF/native-image/org.Amin/Contact-API/reachability-metadata.json
run chmod +x ./gradlew
run ./gradlew nativeCompile --no-daemon

from ubuntu:24.04
workdir /app
copy --from=image /app/build/native/nativeCompile/Server /app/Server
expose 10203
entrypoint ["./Server"]