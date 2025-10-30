package com.github.jakubpakula1.lab.service;

import com.github.jakubpakula1.lab.exception.ApiException;
import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.Position;
import com.google.gson.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class ApiService {

    private final HttpClient client;
    private final Gson gson;
    private final String apiUrl;

    public ApiService(HttpClient client, Gson gson, @Value("${app.api.url}") String apiUrl) {
        this.client = client;
        this.gson = gson;
        this.apiUrl = apiUrl;
    }

    public Employee[] fetchFromAPI() throws IOException, InterruptedException {
        List<Employee> employeeList = new ArrayList<>();
        try{
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(this.apiUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonArray employees = gson.fromJson(response.body(), JsonArray.class);
                for(JsonElement employeeElement : employees){
                    JsonObject employee = employeeElement.getAsJsonObject();
                    int id = employee.get("id").getAsInt();
                    String name = employee.get("name").getAsString();
                    String email = employee.get("email").getAsString();
                    String companyName = employee.getAsJsonObject("company").get("name").getAsString();

                    String[] nameParts = name.split(" ", 2);
                    String firstName = nameParts[0];
                    String lastName = nameParts.length > 1 ? nameParts[1] : "";

                    Employee newEmployee = new Employee(firstName, lastName, companyName, email, Position.PROGRAMISTA, Position.PROGRAMISTA.getBaseSalary());
                    employeeList.add(newEmployee);
                }
            } else {
                throw new ApiException("HTTP error: " + response.statusCode());
            }
        }catch (IOException | InterruptedException e){
            throw new ApiException("Error while communicating with API: " + e.getMessage());
        }catch (JsonSyntaxException e){
            throw new ApiException("Error while parsing JSON: " + e.getMessage());
        }
        return  employeeList.toArray(new Employee[0]);
    }

}
