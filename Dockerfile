FROM ghcr.io/graalvm/native-image-community:25 AS image
WORKDIR /app
copy gradle/ gradle/
copy gradlew build.gradle settings.gradle ./
copy src/ src/
copy META-INF/native-image/org.Amin/Contact-API/reachability-metadata.json META-INF/native-image/org.Amin/Contact-API/reachability-metadata.json
run chmod +x ./gradlew
run ./gradlew nativeCompile --no-daemon

FROM ubuntu:24.04
WORKDIR /app
copy --from=image /app/build/native/nativeCompile/Server /app/Server
EXPOSE 10203
ENTRYPOINT["./Server"]