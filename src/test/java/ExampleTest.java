import io.restassured.path.json.JsonPath;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class ExampleTest extends AbstractTest {
    private static final String get_path = "recipes/complexSearch";
    private static String post_path = "recipes/cuisine";
    private static String recipeTitle;

    @Test
    void getTests() {
        Integer totalResults = given()
                .when()
                .get(getBaseUrl() + get_path)
                .then()
                .extract()
                .jsonPath()
                .get("totalResults");
                if (totalResults > 0) {
            System.out.println("Рецептов нашли, штук - " + totalResults);
        } else {
            System.out.println("ничего не нашли.");
        }

        totalResults = given()
                .queryParam("cuisine", "Italian")
                .queryParam("intolerances", "gluten")
                .queryParam("maxCalories", "25")

                .response()
                .expect()
                .body("totalResults", equalTo(2))

                .when()
                .get(getBaseUrl() + get_path)
                .then()
                .extract()
                .jsonPath()
                .get("totalResults");

        System.out.println("2: Нашли " + totalResults + " низколорийных безглютеновых блюда итальянской кухни.");

        totalResults = given()
                .queryParam("includeIngredients", "1,2,3")

                .response()
                .expect()
                .body("totalResults", equalTo(0))

                .when()
                .get(getBaseUrl() + get_path)
                .then()
                .extract()
                .jsonPath()
                .get("totalResults");

        System.out.println("3: Нашли " + totalResults + " блюд с несуществующими ингредиентами.");

        totalResults = given()
                .queryParam("includeIngredients", "meat,fish,potato")
                .queryParam("minIron", "10")
                .queryParam("minSugar", "10")
                .queryParam("minCalories", "100")
                .queryParam("minMagnesium", "10")

                .response()
                .expect()
                .body("totalResults", equalTo(1))

                .when()
                .get(getBaseUrl() + get_path)
                .then()
                .extract()
                .jsonPath()
                .get("totalResults");

        System.out.println("4: Нашли рыбно-мясных блюд с прочими ограничениями - " + totalResults + ".");

        JsonPath response = given()
                .queryParam("cuisine", "Indian")
                .when()
                .get(getBaseUrl() + get_path)
                .body()
                .jsonPath();
        assertThat(response.get("totalResults"), CoreMatchers.<Object>not(0) );
        recipeTitle = response.get("results.title[0]");
        System.out.println("5: Название блюда индийской кухни: " + recipeTitle);
    }

    @Test
    void postTests() {

        String cuisine = given()
                .when()
                .post(getBaseUrl() + post_path)
                .path("cuisine");
        assertThat(cuisine, anyOf(containsString("Italian"),
                containsString("Mediterranean"),
                containsString("European")));
        System.out.println("Кухня итальянско-средиземноморско-европейская: " + cuisine);

        cuisine = given()
//                .contentType("application/x-www-form-urlencoded")
                .formParam("title", "Burger")
                .formParam("ingredientList", "tuna")
                .when()
                .post(getBaseUrl() + post_path)
                .then()
                .extract()
                .jsonPath()
                .get("cuisine")
                .toString();
        System.out.println("2: Бургер с тунцом оказался - " + cuisine + "!");

        cuisine = given()
                .formParam("title", "Traditional Chinese Recipes")
                .when()
                .post(getBaseUrl() + post_path)
                .path("cuisine");
        assertThat(cuisine, equalTo("Chinese"));
        System.out.println("3: Традиционная Китайская кухня - " + cuisine);

        JsonPath response = given()
                .formParam("title", "Churros")
                .when()
                .post(getBaseUrl() + post_path)
                .body()
                .jsonPath();
        assertThat(response.get("cuisines[1]"), CoreMatchers.<Object>equalTo("Spanish") );
        System.out.println("4: Чурросы - " + response.get("cuisines[1]") );

        cuisine = given()
                .formParam("title", recipeTitle)
                .when()
                .post(getBaseUrl() + post_path)

                .path("cuisine");
        assertThat(cuisine, equalTo("Indian"));
        System.out.println("5: Индийский рецепт из GET действительно индийский? " + cuisine.equals("Indian"));
    }

    @Test
    void addDish_getList_deleteDishTest() {
        String urlChain = getBaseUrl() + "mealplanner/gbtest/shopping-list";

        NewMealRequest request = new NewMealRequest();
        request.setItem("РусскаяКухня");
        request.setAisle("Картошка");
        request.setParse(true);

        String idCreate = given()

                .body(request)
                .when()
                .post(urlChain + "/items")
                .then()
                .extract()
                .jsonPath()
                .get("id")
                .toString();
        System.out.println("Создано блюдо с id= " + idCreate);

        String idDish = given()
                .when()
                .get(urlChain )
                .then()
                .extract()
                .response()
                .body()
                .jsonPath()

                .get("aisles.items[0].id[0]")
                .toString();
        System.out.println("Есть ли блюдо с созданным id? "+ idCreate.equals(idDish));

        given()

                .delete(urlChain + "/items/" + idCreate);
        System.out.println("Успешно удалено блюдо с id= " + idDish);
    }
}