# Pop Culture Quiz

**using google translate to scramble up movie quotes**

## setup
install sbt from https://www.scala-sbt.org/

## running with backend:
the backend requires a running postgres instance, configured with the environment variable `DATABASE_URL`.

ie:
`DATABASE_URL="jdbc:postgresql://localhost:5432/" sbt`


## related work:

* https://github.com/arnaudjuracek/google-translate-chain
* https://github.com/ssut/py-googletrans
* https://stackoverflow.com/questions/65095668/googletrans-api-attributeerror/65109962#65109962
* https://platform.systran.net/reference/translation
