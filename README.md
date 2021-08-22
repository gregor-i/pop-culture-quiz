# Pop Culture Quiz

**using google translate to scramble up movie quotes**

This project is in early prove of concept state.

Check out the current development state on [Heroku](https://pop-culture-quiz-2.herokuapp.com/). The first page load may be slow, as the container might be hibernating.

## related stuff:

* https://github.com/arnaudjuracek/google-translate-chain
* https://github.com/ssut/py-googletrans
* https://stackoverflow.com/questions/65095668/googletrans-api-attributeerror/65109962#65109962
* https://platform.systran.net/reference/translation
* https://github.com/marytts/marytts
* https://github.com/Sciss/jump3r

## Test execution
The test execution depends on a running postgres database with the url `postgres://postgres:postgres@localhost:5432/postgres`.

Docker command:
```sh
docker run -p 5432:5432 -e POSTGRES_USER=postgres \
	--name postgres -d postgres:11.5
```

## Favicon
```
seed = 89074.94367230032
n = 7
```
