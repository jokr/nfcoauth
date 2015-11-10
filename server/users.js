var users = [];

exports.addUser = function(user) {
  var index = indexOf(user);
  if (index > -1) {
    users[index].updatedAt = Date.now();
  } else {
    user.updatedAt = Date.now();
    users.push(user);
  }
};

exports.getAll = function() {
  return users;
};

function indexOf(user) {
  for (var i = 0; i < users.length; i++) {
    if (users[i].userId === user.userId) {
      return i;
    }
  }
  return -1;
}
