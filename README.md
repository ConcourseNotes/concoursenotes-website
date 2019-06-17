# Concourse Notes

[![CircleCI](https://circleci.com/gh/heroku/java-getting-started.svg?style=svg)](https://circleci.com/gh/heroku/java-getting-started)
### A note sharing service that makes classes easier


[![Visit the testing deployment](https://testing.concoursenotes.com/img/logoCircleColor.png)](https://testing.concoursenotes.com/)

## Running Locally

Make sure you have Java and Maven installed.  Also, install the [Heroku CLI](https://cli.heroku.com/) for deployment.

```sh
$ git clone https://github.com/techied/concourse-notes.git
$ cd concourse-notes
$ mvn install
$ heroku local:start
```

Concourse should now be running on [localhost:5000](http://localhost:5000/).

We use a database, so ensure you have a local `.env` file that contains the testing deployment database:

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

For more information, ask techied or ncsariowan on Discord
