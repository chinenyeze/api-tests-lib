# API Tests Library from Perkbox

This library simplifies the api tests process. It is built on rest-assured and testng. Latest version: 1.2.7.



### Documentation

Please find the documentation
[here](https://rawgit.com/chinenyeze/api-tests-lib/master/doc/Documentation.html) for work in progress version or
[here](https://cdn.rawgit.com/chinenyeze/api-tests-lib/1.2.7/doc/Documentation.html) for last tagged version.



### Setting up the test project

##### Naming Data Input Files

All data files are stored in
 * the folder: `data/input/{endpoint}/`
 * as: `{your.choice}.json`

Where:

 * `{endpoint}` and `{Action}` is the name of the endpoint in the service being tested.
 * `{your.choice}.json` is the specific context of the json file. E.g. `question.json`, `auth.create.json`, `schema.text.json`

Library References:

* JsonHelper library class provides a JsonHelper(String file) constructor and some static methods which loads json files from `data/input/`.
* Responses library class provides an assertSchema(String file) method which loads json schema files from `data/input/`.

##### Config

The project config file is stored in
 * `api-tests/resources/config.properties`
 * Variables stored in the config can be called as `Config.get("VAR_NAME")`


##### Env

The `Env` is an environment loader.
 * Variables stored in the environment can be called as `Env.get("VAR_NAME")`
 * `Env` first attempts to read a variable from the environment, and if not found, it then calls `Config.get("VAR_NAME")`.


##### TestNG Runner

All test classes to be run are referenced in
 * `api-tests/testng.xml`
 * This is the file used to run the test.



### Usage

##### POST: Sample Valid Request
```
    @Test
    public void validCreate() {
        String resourcePath = "/endpointOne";
        Responses response = (new Requests(resourcePath))
                .withAuthorization(Token.create())
                .withBody(JsonHelper.getJson("endpointOne/CreateOne-valid"))
                .post();

        response.log("validCreate"); // log response and statusCode

        String expect = "{\"links\":{\"self\":\"/v1/endpointOne/%REGEX\"}}";

        response.assertMatch(201, expect, Regex.UUID);
    }
```

##### GET: Sample Valid Request
```
    @Test
    public void validGet() {
        String resourcePath = "/endpointOne/7dab6d55-755e-418e-a932-08061913142f3";
        Responses response = (new Requests(resourcePath))
                .withAuthorization(Token.read())
                .get();

        JsonHelper json = new JsonHelper("endpointOne/CreateOne-code");

        MapBuilder map = (new MapBuilder())
                .add("$.uuid", json.getParamAsStr("$.uuid"))
                .add("$.endpoint", json.getParamAsStr("$.endpoint"))
                .add("$.create", json.getParamAsStr("$.create"));

        response.assertTrue(200, map, true); // Third true parameter is to log expected and actual values
    }
```

##### GET-List: Sample Valid Request
```
    @Test
    public void validList() {
        String resourcePath = "/endpointTwo";
        Responses response = (new Requests(resourcePath))
                .withAuthorization(Token.read())
                .get();

        JsonHelper json = new JsonHelper("endpointTwo/ListTwo-valid");

        MapBuilder map = (new MapBuilder())
                .add("$.data", json.getParamAsStr("$.data"));

        response.assertTrue(200, map);
    }
```

##### UPDATE: Sample Valid Request
```
    @Test
    public void validUpdate () {
        String resourcePath = "/endpointTwo/7dab6d55-755e-418e-a932-08061913142f3";

        String updatedBody = JsonHelper.modifyParams("endpointTwo/UpdateTwo-valid", (new MapBuilder())
                .add("$.endpoint", "twoA")
                .add("$.update", "twoA"));

        Responses response = (new Requests(resourcePath))
                .withAuthorization(Token.update())
                .withBody(updatedBody)
                .withIfMatch("4e666f2a4fa329c100016d239fad257f")
                .put(true, true); // The boolean values means detailed request and response logging

        MapBuilder result = (new MapBuilder())
                .add("$.message", "The item was successfully updated");

        response.assertTrue(200, result);
    }
```



### License

This project is licensed under GNU General Public License v3.0

