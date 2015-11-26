var bodyParser = require('body-parser');
var express = require("express");
var http = require("http");
var morgan = require("morgan");
var path = require("path");
var request = require("request");
var users = require("./users");
var groups = require("./googlegroups")
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
    getAuthURL(req.body.logintype) + req.body.token,
    (error, response, body) => {
      if (error) {
        console.warn(error);
        res.status(500).send("Error.");
        return;
      }

      if (response.statusCode !== 200) {
        console.warn(body);
        res.status(500).send("Error.");
        return;
      }
      handleOAuthReponse(req.body.logintype, res, JSON.parse(body));     
    }
  );
});

app.use((req, res, next) => {
  res.status(404).send("Not found.");
});

var server = http.createServer(app);
server.listen(8090);

function handleOAuthReponse(logintype, res, body) {
  switch(logintype) {
    case 'Facebook':
      users.addUser(body);
      res.status(200).send("Success.");
      break;
    case 'Google':
      if(groups.checkMembership(res, body.email)) {
        users.addUser(body);
      }
      break;
  }
}

function getAuthURL(logintype) {
  switch(logintype) {
    case 'Facebook':
      return 'https://graph.facebook.com/v2.5/me?fields=id,name,picture&access_token=';
    case 'Google':
      return 'https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=';
  }
  return undefined
}
