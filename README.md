# Concourse Notes

[![CircleCI](https://circleci.com/gh/heroku/java-getting-started.svg?style=svg)](https://circleci.com/gh/heroku/java-getting-started)
### A note sharing service that makes classes easier

<a href="https://testing.concoursenotes.com/"><img src="https://testing.concoursenotes.com/img/logoCircleColor.png" width="100" height="100"></a>


## Running Locally

Make sure you have Java and Maven installed.  Also, install the [Heroku CLI](https://cli.heroku.com/) for deployment.

```sh
$ git clone https://github.com/techied/concourse-notes.git
$ cd concourse-notes
$ mvn install
$ heroku local:start
```

Concourse should now be running on [localhost:5000](http://localhost:5000/).

We use a database, so ensure you `EXPORT` the testing deployment database:

```
DATABASE_URL=postgres://localhost:5432/java_database_name
```
(this is just a demo, not an actual DB)

## Deploying to Heroku

```sh
$ git push heroku master
$ heroku open
```

## Documentation

For more information, ask on Discord
