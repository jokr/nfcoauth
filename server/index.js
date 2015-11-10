var bodyParser = require('body-parser');
var express = require("express");
var http = require("http");
var morgan = require("morgan");
var path = require("path");
var request = require("request");
var users = require("./users");

var app = express();

app.locals.moment = require("moment");

app.set("views", path.join(__dirname, "views"));
app.set("view engine", "jade");
app.use(bodyParser.json());
app.use(morgan("combined"));
app.use(express.static("public"));

app.get("/", (req, res, next) => {
  res.render("users", {users: users.getAll()});
});

app.post("/auth", (req, res, next) => {
  request(
    "https://graph.facebook.com/v2.5/me?fields=id,name,picture&access_token=" + req.body.token,
    (error, response, body) => {
      if (error) {
        console.warning(error);
        res.status(500).send("Error.");
        return;
      }

      if (response.statusCode !== 200) {
        console.warning(body);
        res.status(500).send("Error.");
        return;
      }

      users.addUser(JSON.parse(body))
      res.status(200).send("Success.");
    }
  );
});

app.use((req, res, next) => {
  res.status(404).send("Not found.");
});

var server = http.createServer(app);
server.listen(8090);
