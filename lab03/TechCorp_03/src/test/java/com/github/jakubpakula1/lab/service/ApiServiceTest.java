package com.github.jakubpakula1.lab.service;

import com.github.jakubpakula1.lab.exception.ApiException;
import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.Position;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApiServiceTest {

    @Mock
    private HttpClient client;

    @Mock
    private HttpResponse<String> response;

    // fetchFromAPI - success cases
    @Test
    void fetchFromAPI_success_parsesEmployees() throws Exception {
        when(response.statusCode()).thenReturn(200);
        String body = "[{\"id\":1,\"name\":\"Jan Kowalski\",\"email\":\"jan@x.com\",\"company\":{\"name\":\"X\"}}]";
        when(response.body()).thenReturn(body);
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        ApiService api = new ApiService(client);
        Employee[] employees = api.fetchFromAPI();

        assertThat(employees)
                .isNotNull()
                .hasSize(1)
                .extracting(Employee::getEmail)
                .containsExactly("jan@x.com");
    }

    @Test
    void fetchFromAPI_success_parsesFirstAndLastName() throws Exception {
        when(response.statusCode()).thenReturn(200);
        String body = "[{\"id\":1,\"name\":\"Jan Kowalski\",\"email\":\"jan@x.com\",\"company\":{\"name\":\"X\"}}]";
        when(response.body()).thenReturn(body);
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        ApiService api = new ApiService(client);
        Employee[] employees = api.fetchFromAPI();

        assertThat(employees)
                .hasSize(1);
        assertThat(employees[0])
                .extracting(Employee::getName, Employee::getSurname)
                .containsExactly("Jan", "Kowalski");
    }

    @Test
    void fetchFromAPI_success_parsesCompanyName() throws Exception {
        when(response.statusCode()).thenReturn(200);
        String body = "[{\"id\":1,\"name\":\"Anna Smith\",\"email\":\"anna@acme.com\",\"company\":{\"name\":\"Acme Corp\"}}]";
        when(response.body()).thenReturn(body);
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        ApiService api = new ApiService(client);
        Employee[] employees = api.fetchFromAPI();

        assertThat(employees)
                .hasSize(1)
                .extracting(Employee::getCompany)
                .containsExactly("Acme Corp");
    }

    @Test
    void fetchFromAPI_success_setsDefaultPosition() throws Exception {
        when(response.statusCode()).thenReturn(200);
        String body = "[{\"id\":1,\"name\":\"Test User\",\"email\":\"test@x.com\",\"company\":{\"name\":\"X\"}}]";
        when(response.body()).thenReturn(body);
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        ApiService api = new ApiService(client);
        Employee[] employees = api.fetchFromAPI();

        assertThat(employees)
                .hasSize(1);
        assertThat(employees[0].getPosition())
                .isEqualTo(Position.PROGRAMISTA);
    }

    @Test
    void fetchFromAPI_success_setsDefaultSalary() throws Exception {
        when(response.statusCode()).thenReturn(200);
        String body = "[{\"id\":1,\"name\":\"Test User\",\"email\":\"test@x.com\",\"company\":{\"name\":\"X\"}}]";
        when(response.body()).thenReturn(body);
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        ApiService api = new ApiService(client);
        Employee[] employees = api.fetchFromAPI();

        assertThat(employees)
                .hasSize(1);
        assertThat(employees[0].getSalary())
                .isEqualTo(Position.PROGRAMISTA.getBaseSalary());
    }

    @Test
    void fetchFromAPI_success_multipleEmployees() throws Exception {
        when(response.statusCode()).thenReturn(200);
        String body = "[" +
                "{\"id\":1,\"name\":\"Jan Kowalski\",\"email\":\"jan@x.com\",\"company\":{\"name\":\"X\"}}," +
                "{\"id\":2,\"name\":\"Anna Nowak\",\"email\":\"anna@y.com\",\"company\":{\"name\":\"Y\"}}," +
                "{\"id\":3,\"name\":\"Bob Smith\",\"email\":\"bob@z.com\",\"company\":{\"name\":\"Z\"}}" +
                "]";
        when(response.body()).thenReturn(body);
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        ApiService api = new ApiService(client);
        Employee[] employees = api.fetchFromAPI();

        assertThat(employees)
                .hasSize(3)
                .extracting(Employee::getEmail)
                .containsExactly("jan@x.com", "anna@y.com", "bob@z.com");
    }

    @Test
    void fetchFromAPI_success_emptyArray() throws Exception {
        when(response.statusCode()).thenReturn(200);
        String body = "[]";
        when(response.body()).thenReturn(body);
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        ApiService api = new ApiService(client);
        Employee[] employees = api.fetchFromAPI();

        assertThat(employees).isEmpty();
    }

    @Test
    void fetchFromAPI_success_nameWithoutLastName() throws Exception {
        when(response.statusCode()).thenReturn(200);
        String body = "[{\"id\":1,\"name\":\"Madonna\",\"email\":\"madonna@x.com\",\"company\":{\"name\":\"X\"}}]";
        when(response.body()).thenReturn(body);
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        ApiService api = new ApiService(client);
        Employee[] employees = api.fetchFromAPI();

        assertThat(employees)
                .hasSize(1);
        assertThat(employees[0])
                .extracting(Employee::getName, Employee::getSurname)
                .containsExactly("Madonna", "");
    }

    // fetchFromAPI - error cases
    @Test
    void fetchFromAPI_httpError400_throwsApiException() throws Exception {
        when(response.statusCode()).thenReturn(400);
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        ApiService api = new ApiService(client);

        assertThatThrownBy(api::fetchFromAPI)
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("HTTP error: 400");
    }

    @Test
    void fetchFromAPI_httpError404_throwsApiException() throws Exception {
        when(response.statusCode()).thenReturn(404);
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        ApiService api = new ApiService(client);

        assertThatThrownBy(api::fetchFromAPI)
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("HTTP error: 404");
    }

    @Test
    void fetchFromAPI_httpError500_throwsApiException() throws Exception {
        when(response.statusCode()).thenReturn(500);
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        ApiService api = new ApiService(client);

        assertThatThrownBy(api::fetchFromAPI)
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("HTTP error: 500");
    }

    @Test
    void fetchFromAPI_invalidJson_throwsApiException() throws Exception {
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("invalid-json");
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        ApiService api = new ApiService(client);

        assertThatThrownBy(api::fetchFromAPI)
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Error while parsing JSON");
    }

    @Test
    void fetchFromAPI_malformedJson_throwsApiException() throws Exception {
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("{\"incomplete\": ");
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        ApiService api = new ApiService(client);

        assertThatThrownBy(api::fetchFromAPI)
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Error while parsing JSON");
    }

    @Test
    void fetchFromAPI_ioException_throwsApiException() throws Exception {
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new java.io.IOException("Network error"));

        ApiService api = new ApiService(client);

        assertThatThrownBy(api::fetchFromAPI)
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Error while communicating with API");
    }

    @Test
    void fetchFromAPI_interruptedException_throwsApiException() throws Exception {
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new InterruptedException("Thread interrupted"));

        ApiService api = new ApiService(client);

        assertThatThrownBy(api::fetchFromAPI)
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Error while communicating with API");
    }

    // Constructor tests
    @Test
    void defaultConstructor_createsInstance() {
        ApiService api = new ApiService();

        assertThat(api).isNotNull();
    }

    @Test
    void defaultConstructor_createsInstanceWithHttpClient() {
        ApiService api = new ApiService();

        assertThat(api)
                .isNotNull()
                .isInstanceOf(ApiService.class);
    }

    @Test
    void constructorWithClient_createsInstance() {
        ApiService api = new ApiService(client);

        assertThat(api).isNotNull();
    }

    @Test
    void constructorWithClient_usesProvidedClient() throws IOException, InterruptedException {
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("[]");
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        ApiService api = new ApiService(client);

        assertThatCode(api::fetchFromAPI)
                .doesNotThrowAnyException();
    }
}