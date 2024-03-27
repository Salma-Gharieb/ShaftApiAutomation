package api.tests;

import com.shaft.driver.SHAFT;
import io.restassured.http.ContentType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import java.util.HashMap;

public class testTodoApis {
    SHAFT.GUI.WebDriver driver ;
    static SHAFT.API api = new SHAFT.API("https://todo.qacart.com");
    static String token;
    static String itemName;
    static String itemId;

    @Test
    public void authenticate() {
        String loginEndpoint = "/api/v1/users/login";
        HashMap<String, String> loginBody = new HashMap<>();
        loginBody.put("email", "testuser@example.com");
        loginBody.put("password", "Password@12");

        api.post(loginEndpoint)
                .setRequestBody(loginBody).setContentType(ContentType.JSON).setTargetStatusCode(200).perform();

        Assert.assertEquals(api.getResponseStatusCode(), 200);
        api.assertThatResponse().extractedJsonValue("firstName").equals("test");
        token = api.getResponseJSONValue("access_token");
    }

    @Test
    public void addNewTodo() {
        String addTodoEndPoint = "/api/v1/tasks";
        itemName = "ShaftApi";
        HashMap<String, String> todoBody = new HashMap<>();
        todoBody.put("item", itemName);
        todoBody.put("isCompleted", "false");

        authenticate();
        api.post(addTodoEndPoint)
                .addHeader("Authorization", "Bearer " + token)
                .setRequestBody(todoBody).setContentType(ContentType.JSON)
                .setTargetStatusCode(201).perform();
        api.assertThatResponse().extractedJsonValue("item").equals(itemName);
        Assert.assertEquals(api.getResponseStatusCode(), 201);
    }

    @Test
    public void getTodoList() throws JSONException {
        String getListEndPoint = "/api/v1/tasks";
        addNewTodo();

        api.get(getListEndPoint)
                .addHeader("Authorization", "Bearer " + token)
                .setContentType(ContentType.JSON).setTargetStatusCode(200).perform();

        if (api.getResponseStatusCode() == 200) {
            // Parse the response body to extract the list of items
            String tasksList = api.getResponseJSONValue("tasks");
            JSONArray jsonArray = new JSONArray(tasksList);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject task = jsonArray.getJSONObject(i);
                String item = task.getString("item");

                if (itemName.equals(item)) {
                    itemId = task.getString("_id");
                    System.out.println("Found item " + itemName + " with ID: " + itemId);
                    break;
                }
            }
        }
    }

    @Test
    public void deleteTask() throws JSONException {
        getTodoList();

        String deleteTaskEndPoint = "/api/v1/tasks/"+itemId;
        api.delete(deleteTaskEndPoint)
                .addHeader("Authorization", "Bearer " + token)
                .setContentType(ContentType.JSON).setTargetStatusCode(200).perform();

        Assert.assertEquals(api.getResponseStatusCode(), 200);
        api.assertThatResponse().extractedJsonValue("item").equals(itemName);
    }

    @AfterClass(alwaysRun = true)
    public void afterClass(){
        driver = new SHAFT.GUI.WebDriver();
        driver.quit();
    }
}


