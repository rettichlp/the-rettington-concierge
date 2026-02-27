package de.rettichlp.therettingtonconcierge.io.api;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

import static com.google.gson.reflect.TypeToken.getParameterized;
import static de.rettichlp.therettingtonconcierge.io.GsonConfiguration.GSON;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@Builder
public class RequestHandler {

    private final String baseUrl;
    private final Map<String, String> defaultHeaders;

    /**
     * Creates a {@link GetRequest} object to perform an HTTP GET request for a specified resource.
     * <p>
     * This method is designed to encapsulate the details for an HTTP GET request, including the URL of the resource and the expected
     * type of the response data. The created {@link GetRequest} object allows further customization and execution of the GET request
     * and contains the logic to deserialize the JSON response into an object of the specified type.
     *
     * @param <T>        The type of the object expected as the response from the GET request.
     * @param url        The endpoint URL for the GET request.
     * @param modelClass The class type of the model to deserialize the JSON response into.
     *
     * @return A {@link GetRequest} object initialized with the given URL and model class, which can be used to execute the GET request
     *         and retrieve the response.
     */
    public <T> GetRequest<T> get(String url, Class<T> modelClass) {
        return new GetRequest<>(url, modelClass);
    }

    /**
     * Sends an HTTP PUT request to the specified URL with the provided model as the payload.
     * <p>
     * This method constructs a {@link PutRequest} object to represent the HTTP PUT request. The model is serialized into JSON format
     * and included in the request body. The created {@link PutRequest} object can then be executed synchronously or asynchronously.
     *
     * @param <T>   The type of the model to be sent as the request body.
     * @param url   The endpoint URL to which the PUT request is sent.
     * @param model The data model to be serialized and included in the PUT request.
     *
     * @return A {@link PutRequest} object that encapsulates the details of the PUT request.
     */
    public <T> PutRequest<T> put(String url, T model) {
        return new PutRequest<>(url, model);
    }

    /**
     * Sends an HTTP POST request to the specified URL with the provided model as the payload.
     * <p>
     * This method constructs a {@link PostRequest} object to represent the HTTP POST request. The model is serialized into JSON format
     * and included in the request body. The created {@link PostRequest} object can then be executed synchronously or asynchronously.
     *
     * @param <T>   The type of the model to be sent as the request body.
     * @param url   The endpoint URL to which the POST request is sent.
     * @param model The data model to be serialized and included in the POST request.
     *
     * @return A {@link PostRequest} object that encapsulates the details of the POST request.
     */
    public <T> PostRequest<T> post(String url, T model) {
        return new PostRequest<>(url, model);
    }

    /**
     * Sends an HTTP DELETE request to the specified URL with the given model as the payload.
     * <p>
     * The model is serialized into JSON format and included in the request body. The DELETE request is constructed using the provided
     * URL and model. This method is designed to return a {@link DeleteRequest} object representing the configured request, which can
     * be executed synchronously or asynchronously.
     *
     * @param <T>   The type of the model to be sent as the request body.
     * @param url   The endpoint URL to which the DELETE request is sent.
     * @param model The data model to be serialized and included in the DELETE request.
     *
     * @return A {@link DeleteRequest} object that encapsulates the details of the DELETE request.
     */
    public <T> DeleteRequest<T> delete(String url, T model) {
        return new DeleteRequest<>(url, model);
    }

    String sendRequest(String url, HttpMethod httpMethod, @Nullable String json) {
        return json == null ? getClient(url).method(httpMethod)
                .retrieve()
                .bodyToMono(String.class)
                .block() : getClient(url).method(httpMethod)
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

    /**
     * Represents an HTTP GET request for retrieving and deserializing JSON data into a specific model class. This class supports
     * operations to fetch a single object or a list of objects using a specified URL and deserialization type.
     *
     * @param <T> The type of object that the JSON response is deserialized into.
     */
    @Getter
    @RequiredArgsConstructor
    public class GetRequest<T> {

        private final String url;
        private final Class<T> modelClass;

        /**
         * Retrieves and deserializes a single object of type {@code T} from a JSON response received via an HTTP GET request.
         * <p>
         * The method sends a GET request to the specified URL and deserializes the response body into an object of the type
         * represented by {@code modelClass}. Uses Gson for JSON deserialization.
         *
         * @return an instance of type {@code T}, deserialized from the JSON response returned by the GET request
         */
        public T retrieve() {
            return GSON.fromJson(sendRequest(this.url, GET, null), this.modelClass);
        }

        /**
         * Sends an HTTP GET request to the specified URL and deserializes the JSON response into a list of objects of the specified
         * model class type.
         * <p>
         * The method uses Gson for JSON deserialization and generics to infer the type of the resulting list. It assumes the JSON
         * response from the HTTP GET request contains an array of objects that match the structure of the specified model class.
         *
         * @return a list of objects of type {@code T}, deserialized from the JSON response.
         */
        public List<T> retrieveAsList() {
            return GSON.fromJson(sendRequest(this.url, GET, null), getParameterized(List.class, this.modelClass).getType());
        }
    }

    /**
     * Represents a generic HTTP POST request.
     * <p>
     * This class is designed to handle HTTP POST requests by sending a JSON payload derived from a provided model object. The URL
     * endpoint and model are specified during object instantiation.
     * <p>
     * It provides methods for sending the POST request both synchronously and asynchronously.
     *
     * @param <T> The type of the model object to be serialized into a JSON payload and sent in the request body.
     */
    @Getter
    @RequiredArgsConstructor
    public class PostRequest<T> {

        private final String url;
        private final T model;

        /**
         * Sends a POST HTTP request to the specified URL with the model serialized in JSON format.
         * <p>
         * The method utilizes the URL and model provided during the instantiation of the PostRequest. It serializes the model object
         * to JSON using the Gson instance, and sends the request using the HTTP POST method.
         * <p>
         * This method blocks execution until the request is completed.
         * <p>
         * Note: Uses the {@code sendRequest} method of the containing class to perform the HTTP request.
         */
        public void send() {
            sendRequest(this.url, POST, GSON.toJson(this.model));
        }

        /**
         * Sends the current request asynchronously.
         * <p>
         * This method serializes the {@code model} object to a JSON string using Gson and sends it to the specified {@code url} using
         * an asynchronous HTTP POST request. The request is processed without blocking the calling thread.
         * <p>
         * Responsibilities: - Converts the {@code model} into a JSON representation. - Dispatches the JSON payload to the specified
         * {@code url} via a POST request. - Leverages an asynchronous communication mechanism.
         * <p>
         * Use Cases: - Suitable for scenarios where the result of the HTTP request does not need to be immediately available to the
         * calling code.
         * <p>
         * Limitations: - The method does not provide a direct mechanism to handle the response or errors. If needed, the
         * implementation of {@code sendRequestAsync} should be customized to include appropriate error-handling logic or hooks for
         * response processing.
         * <p>
         * Dependencies: - Relies on the {@code sendRequestAsync} method of the {@link RequestHandler} class to perform the
         * asynchronous HTTP POST communication. - Utilizes the Gson serialization framework to convert the {@code model} into JSON
         * format.
         */
        public void sendAsync() {
            sendRequestAsync(this.url, POST, GSON.toJson(this.model));
        }

        /**
         * Retrieves a deserialized instance of the specified model class by sending an HTTP POST request to the associated URL with
         * the serialized JSON representation of the current model object. The method blocks until the request completes and processes
         * the response by deserializing it into an instance of the given model class.
         *
         * @param modelClass the class type of the model to deserialize the response into
         *
         * @return an instance of the specified model class populated with the data from the HTTP response
         */
        public T retrieve(Class<T> modelClass) {
            return GSON.fromJson(sendRequest(this.url, POST, GSON.toJson(this.model)), modelClass);
        }
    }

    /**
     * Represents a generic PUT HTTP request that encapsulates a target URL and a model object. This class allows sending HTTP PUT
     * requests synchronously or asynchronously with the JSON representation of the provided model object.
     * <p>
     * The PutRequest class is parameterized to accept a model of any type. The model is serialized into JSON format using Gson before
     * the request is sent.
     * <p>
     * Responsibilities: - Stores the URL and the model object to be sent in the PUT request. - Provides functionality for sending the
     * request either synchronously or asynchronously. - Utilizes utility methods to handle the HTTP request and JSON serialization.
     * <p>
     * Usage Notes: - Instances of this class are typically created using a factory or builder pattern. - The URL and model object are
     * immutable once the PutRequest object is constructed. - The class depends on an external Gson instance for JSON serialization. -
     * Error handling or response processing is expected to be implemented at a higher level.
     *
     * @param <T> The type of the model object to be sent in the PUT request.
     */
    @Getter
    @RequiredArgsConstructor
    public class PutRequest<T> {

        private final String url;
        private final T model;

        /**
         * Sends a synchronous HTTP PUT request to the specified URL with the serialized JSON representation of the associated model
         * object.
         * <p>
         * This method prepares the JSON payload by serializing the {@code model} field using a Gson instance and uses the
         * {@code sendRequest} utility method for executing the request. The HTTP request is blocking, meaning it is executed in a
         * synchronous manner and does not return a result to the caller.
         * <p>
         * Responsibilities: - Serializes the {@code model} object into JSON format using Gson. - Sends the JSON data to the URL using
         * a PUT HTTP request.
         * <p>
         * Notes: - Callers must ensure that the {@code url} property and {@code model} object are correctly set before invoking this
         * method. - This method does not handle the response or provide feedback about the success or failure of the request.
         */
        public void send() {
            sendRequest(this.url, PUT, GSON.toJson(this.model));
        }

        /**
         * Asynchronously sends a PUT HTTP request to the specified URL with the serialized JSON representation of the current model
         * object. This method utilizes a non-blocking approach to perform the request and does not return any result directly.
         * <p>
         * The method uses the {@code sendRequestAsync} utility to handle the asynchronous HTTP request. The JSON payload is
         * constructed from the state of the {@code model} field using a Gson instance for serialization.
         * <p>
         * Responsibilities: - Converts the {@code model} object to its JSON representation. - Sends the converted JSON data to the URL
         * using a PUT request. - Ensures the operation is performed asynchronously without blocking the calling thread.
         * <p>
         * Notes: - The success or failure of the request is handled by the subscription mechanism inside the {@code sendRequestAsync}
         * method. - Any side effects or processing related to the response should be implemented within the subscription logic of
         * {@code sendRequestAsync}.
         */
        public void sendAsync() {
            sendRequestAsync(this.url, PUT, GSON.toJson(this.model));
        }
    }

    /**
     * Represents an HTTP DELETE request for a specific model type.
     * <p>
     * This class encapsulates the details required to perform an HTTP DELETE request on a given endpoint, with the specified model
     * object serialized as the request payload. The payload is serialized into JSON format using Gson before being sent.
     * <p>
     * Type Parameter:
     *
     * @param <T> The type of the model object that will be serialized and sent as part of the DELETE request.
     *            <p>
     *            Responsibilities: - Stores the target URL for the HTTP DELETE request. - Holds a reference to the model object to be
     *            serialized and sent as part of the request body. - Provides mechanisms to send the request either synchronously or
     *            asynchronously.
     *            <p>
     *            Threading Considerations: - The {@code send} method performs the request synchronously, blocking the current thread
     *            until it completes. - The {@code sendAsync} method performs the request asynchronously, allowing the calling thread
     *            to continue execution without waiting for the request to finish.
     *            <p>
     *            Applications: - Use this class to remove resources from a server via HTTP DELETE requests, particularly when the
     *            server accepts JSON payloads. It is suitable for both blocking and non-blocking operations, depending on the use
     *            case.
     */
    @Getter
    @RequiredArgsConstructor
    public class DeleteRequest<T> {

        private final String url;
        private final T model;

        /**
         * Sends an HTTP DELETE request with the current model serialized as a JSON payload.
         * <p>
         * This method performs a synchronous HTTP DELETE operation. The target URL is determined by the {@code url} field, and the
         * JSON-serialized representation of the {@code model} field serves as the request body. The JSON serialization is handled via
         * Gson.
         * <p>
         * Responsibilities: - Serializes the model field into JSON format. - Sends a DELETE request to the specified URL. - Executes
         * the request synchronously, blocking the thread until the operation completes.
         * <p>
         * Note: This method should be used in scenarios where blocking behavior is acceptable and immediate confirmation of the
         * completion of the request is required.
         */
        public void send() {
            sendRequest(this.url, DELETE, GSON.toJson(this.model));
        }

        /**
         * Sends the DELETE request represented by this instance asynchronously.
         * <p>
         * This method utilizes the {@code sendRequestAsync} function to perform an asynchronous HTTP DELETE request using the provided
         * URL and serialized payload of the model. The payload is converted to a JSON string using the Gson instance associated with
         * this class. The request is executed on a non-blocking thread, and the calling thread is not blocked while the request is
         * being processed.
         * <p>
         * Responsibilities: - Serializes the model associated with this request into JSON format. - Initiates an asynchronous HTTP
         * DELETE request to the specified URL. - Delegates the asynchronous request execution to the {@code sendRequestAsync} method.
         * <p>
         * This method is particularly useful for operations where the result of the delete request is either not immediately needed or
         * handled asynchronously, enabling higher throughput in concurrent scenarios.
         */
        public void sendAsync() {
            sendRequestAsync(this.url, DELETE, GSON.toJson(this.model));
        }
    }
}
