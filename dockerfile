# Etapa de compilación, un docker especifico, que se etiqueta como build
FROM aragorn7372/gradle-cache-java25:latest AS build

# Directorio de trabajo
WORKDIR /app

# Copia los archivos build.gradle y src de nuestro proyecto
COPY build.gradle.kts .
COPY gradlew .
COPY gradle gradle
COPY src src
COPY custom custom
# Configura la variable de entorno DOCKER_HOST, esto es para que el contenedor pueda comunicarse con el host
# En Windows se usa host.docker.internal, en Linux y macOS se puede usar localhost
ARG DOCKER_HOST_ARG=tcp://host.docker.internal:2375
ENV DOCKER_HOST=$DOCKER_HOST_ARG

# Compila y construye el proyecto, podemos evitar los test evitando con -x test, o cualquier otra tarea de gradle
# RUN ./gradlew build -x test -x jacocoTestReport -x javadoc
RUN ./gradlew build dokkaGenerate jacocoTestReport
#-----------------------------------------------------------------------------------------------------------------------
# nginx etapa webTest
FROM nginx:latest AS testweb
# establezco el directoio de trabajo
WORKDIR /app
# elimino la web por defecto
RUN rm -rf /usr/share/nginx/html/*

# Copiamos informe de test, si es que se genero
COPY --from=build /app/build/reports/tests/test /usr/share/nginx/html

#-----------------------------------------------------------------------------------------------------------------------
# nginx etapa jacoco web
FROM nginx:latest AS jacocoweb
# establezco el directoio de trabajo
WORKDIR /app
# elimino la web por defecto
RUN rm -rf /usr/share/nginx/html/*

# Copiamos informe de test, si es que se genero
COPY --from=build /app/build/reports/jacoco/test/html /usr/share/nginx/html
#----------------------------------------------------------------------------------------------------------------------
# apache2 etapa documentacion web
FROM httpd:latest AS docweb
# establezco el directoio de trabajo
WORKDIR /app
# elimino la web por defecto
RUN rm -rf /usr/local/apache2/htdocs/*

# Copiamos informe de test, si es que se genero
COPY --from=build /app/build/dokka/html /usr/local/apache2/htdocs/

# Etapa de ejecución, un docker especifico, que se etiqueta como run
# Con una imagen de java, solo neceistamos el jre
FROM eclipse-temurin:25-jre-alpine AS run

# Directorio de trabajo
WORKDIR /app

# Copia el jar de la aplicación, ojo que esta en la etapa de compilación, etiquetado como build
# Cuidado con la ruta definida cuando has copiado las cosas en la etapa de compilación
# Para copiar un archivo de una etapa a otra, se usa la instrucción COPY --from=etapaOrigen
COPY --from=build /app/build/libs/*SNAPSHOT.jar /app/my-app.jar

# Ejecuta el jar
ENTRYPOINT ["java","-jar","/app/my-app.jar"]