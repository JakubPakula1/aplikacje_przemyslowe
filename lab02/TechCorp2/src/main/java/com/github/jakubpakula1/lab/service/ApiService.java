package com.github.jakubpakula1.lab.service;
import java.net.URI;
import java.net.http.*;

import com.github.jakubpakula1.lab.exception.ApiException;
import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.Position;
import com.google.gson.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ApiService {

    public Employee[] fetchFromAPI() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        List<Employee> employeeList = new ArrayList<>();
        try{
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://jsonplaceholder.typicode.com/users"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Gson gson = new Gson();
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
