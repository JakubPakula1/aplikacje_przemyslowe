package com.github.jakubpakula1.lab.service;

import com.github.jakubpakula1.lab.exception.ApiException;
import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.Position;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    private final Gson gson = new Gson();
    private final String testApiUrl = "https://jsonplaceholder.typicode.com/users";

    @Test
    void fetchFromAPI_success_parsesEmployees() throws Exception {
        when(response.statusCode()).thenReturn(200);
        String body = "[{\"id\":1,\"name\":\"Jan Kowalski\",\"email\":\"jan@x.com\",\"company\":{\"name\":\"X\"}}]";
        when(response.body()).thenReturn(body);
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        ApiService api = new ApiService(client, gson, testApiUrl);
        Employee[] employees = api.fetchFromAPI();

        assertThat(employees)
                .isNotNull()
                .hasSize(1)
                .extracting(Employee::getEmail)
                .containsExactly("jan@x.com");
    }

    // Analogicznie zaktualizuj pozostałe testy, zastępując:
    // new ApiService(client) -> new ApiService(client, gson, testApiUrl)
    // new ApiService() -> new ApiService(HttpClient.newHttpClient(), gson, testApiUrl)
}