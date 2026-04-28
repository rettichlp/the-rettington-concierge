package de.rettichlp.therettingtonconcierge.io.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@Builder
public class RequestHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String baseUrl;
    private final Map<String, String> defaultHeaders;

    /**
     * Creates a {@link GetRequest} object to perform an HTTP GET request for a specified resource.
     *
     * @param <T>        The type of the object expected as the response from the GET request.
     * @param url        The endpoint URL for the GET request.
     * @param modelClass The class type of the model to deserialize the JSON response into.
     *
     * @return A {@link GetRequest} object initialized with the given URL and model class.
     */
    public <T> GetRequest<T> get(String url, Class<T> modelClass) {
        return new GetRequest<>(url, modelClass);
    }

    /**
     * Creates a {@link PutRequest} object to perform an HTTP PUT request for a specified resource.
     *
     * @param <T>   The type of the object to be sent as the request body.
     * @param url   The endpoint URL for the PUT request.
     * @param model The data model to be serialized and included in the PUT request.
     *
     * @return A {@link PutRequest} object initialized with the given URL and model.
     */
    public <T> PutRequest<T> put(String url, T model) {
        return new PutRequest<>(url, model);
    }

    /**
     * Creates a {@link PostRequest} object to perform an HTTP POST request for a specified resource.
     *
     * @param <T>   The type of the object to be sent as the request body.
     * @param url   The endpoint URL for the POST request.
     * @param model The data model to be serialized and included in the POST request.
     *
     * @return A {@link PostRequest} object initialized with the given URL and model.
     */
    public <T> PostRequest<T> post(String url, T model) {
        return new PostRequest<>(url, model);
    }

    /**
     * Creates a {@link DeleteRequest} object to perform an HTTP DELETE request for a specified resource.
     *
     * @param <T>   The type of the object to be sent as the request body.
     * @param url   The endpoint URL for the DELETE request.
     * @param model The data model to be serialized and included in the DELETE request.
     *
     * @return A {@link DeleteRequest} object initialized with the given URL and model.
     */
    public <T> DeleteRequest<T> delete(String url, T model) {
        return new DeleteRequest<>(url, model);
    }

    String sendRequest(String url, HttpMethod httpMethod, @Nullable String json) {
        return json == null
                ? getClient(url).method(httpMethod)
                  .retrieve()
                  .bodyToMono(String.class)
                  .block()
                : getClient(url).method(httpMethod)
                  .contentType(APPLICATION_JSON)
                  .body(just(json), String.class)
                  .retrieve()
                  .bodyToMono(String.class)
                  .block();
    }

    void sendRequestAsync(String url, HttpMethod httpMethod, String json) {
        getClient(url).method(httpMethod)
                .contentType(APPLICATION_JSON)
                .body(just(json), String.class)
                .retrieve()
                .toBodilessEntity()
                .subscribe();
    }

    private @NonNull WebClient getClient(String url) {
        WebClient.Builder webClientBuilder = WebClient.builder()
                .baseUrl(this.baseUrl + url)
                .codecs(codecs -> codecs
                        .defaultCodecs()
                        .maxInMemorySize(-1));

        this.defaultHeaders.forEach(webClientBuilder::defaultHeader);

        return webClientBuilder.build();
    }

    @SneakyThrows
    private String toJson(Object model) {
        return this.objectMapper.writeValueAsString(model);
    }

    @SneakyThrows
    private <T> T fromJson(String json, Class<T> modelClass) {
        return this.objectMapper.readValue(json, modelClass);
    }

    @SneakyThrows
    private <T> List<T> fromJsonArray(String json, Class<T> modelClass) {
        CollectionType collectionType = this.objectMapper.getTypeFactory().constructCollectionType(List.class, modelClass);
        return this.objectMapper.readValue(json, collectionType);
    }

    /**
     * Represents an HTTP GET request that retrieves data from a specified URL and deserializes the response into objects of a
     * specified type.
     *
     * @param <T> The type of object that the JSON response will be deserialized into.
     */
    @Getter
    @RequiredArgsConstructor
    public class GetRequest<T> {

        private final String url;
        private final Class<T> modelClass;

        /**
         * Retrieves a resource from the specified URL via an HTTP GET request and deserializes the JSON response into an object of the
         * type specified by the {@code modelClass} field.
         *
         * @return An instance of type {@code T} representing the deserialized response data.
         */
        public T retrieve() {
            return fromJson(sendRequest(this.url, GET, null), this.modelClass);
        }

        /**
         * Retrieves a resource from the specified URL via an HTTP GET request and deserializes the JSON response into a list of
         * objects of the type specified by the {@code modelClass} field.
         *
         * @return A {@code List<T>} representing the deserialized response data.
         */
        public List<T> retrieveAsList() {
            return fromJsonArray(sendRequest(this.url, GET, null), this.modelClass);
        }
    }

    /**
     * Represents an HTTP POST request used to send data to a specified URL with the request body serialized in JSON format.
     *
     * @param <T> The type of the model object to be serialized and included in the HTTP POST request.
     */
    @Getter
    @RequiredArgsConstructor
    public class PostRequest<T> {

        private final String url;
        private final T model;

        public void send() {
            sendRequest(this.url, POST, toJson(this.model));
        }

        public void sendAsync() {
            sendRequestAsync(this.url, POST, toJson(this.model));
        }

        @SneakyThrows
        public T retrieve(Class<T> modelClass) {
            return fromJson(sendRequest(this.url, POST, toJson(this.model)), modelClass);
        }
    }

    /**
     * Represents an HTTP PUT request used to send data to a specified URL with the request body serialized to JSON format.
     *
     * @param <T> The type of the model object to be serialized and included in the HTTP PUT request.
     */
    @Getter
    @RequiredArgsConstructor
    public class PutRequest<T> {

        private final String url;
        private final T model;

        public void send() {
            sendRequest(this.url, PUT, toJson(this.model));
        }

        public void sendAsync() {
            sendRequestAsync(this.url, PUT, toJson(this.model));
        }
    }

    /**
     * Represents an HTTP DELETE request used to send data to a specified URL with the request body serialized to JSON format.
     *
     * @param <T> The type of the model object to be serialized and included in the HTTP DELETE request.
     */
    @Getter
    @RequiredArgsConstructor
    public class DeleteRequest<T> {

        private final String url;
        private final T model;

        public void send() {
            sendRequest(this.url, DELETE, toJson(this.model));
        }

        public void sendAsync() {
            sendRequestAsync(this.url, DELETE, toJson(this.model));
        }
    }
}
