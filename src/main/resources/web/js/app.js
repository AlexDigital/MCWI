var app = angular.module('app', ['ngRoute', 'ngCookies', 'Services']);

app.config(function ($routeProvider) {
    $routeProvider.when('/dashboard', {
        templateUrl: 'parts/dashboard.html',
        controller: 'dashboardctrl'
    });
    $routeProvider.when('/login', {
        templateUrl: 'parts/login.html',
        controller: 'loginctrl'
    });
    $routeProvider.when('/players', {
        templateUrl: 'parts/players.html',
        controller: 'playersctrl'
    });
    $routeProvider.when('/worlds', {
        templateUrl: 'parts/worlds.html',
        controller: 'worldsctrl'
    });
    $routeProvider.when('/plugins', {
        templateUrl: 'parts/plugins.html',
        controller: 'pluginsctrl'
    });
    $routeProvider.otherwise({redirectTo: '/dashboard'});
});

app.run(function ($rootScope, Socket, $cookies, $location) {
    $rootScope.loggedin = false;

    $rootScope.isActive = function (path) {
        return $location.url() === path;
    };

    $rootScope.$on('$routeChangeStart', function (event, next) {
        if (!$rootScope.loggedin) {
            if ($cookies.get('session') !== undefined) {
                Socket.on('login-success', function (data) {
                    $rootScope.loggedin = true;
                    $rootScope.username = data.username;
                    if (next.templateUrl == 'parts/login.html') {
                        $location.path('/dashboard');
                    }
                });
                Socket.on('login-failed', function (data) {
                    if (next.templateUrl !== 'parts/login.html') {
                        $location.path('/login');
                    }
                });
                console.log({cookie: $cookies.get('session'), username: $cookies.get('user')});
                Socket.emit('login-cookie', {cookie: $cookies.get('session'), username: $cookies.get('username')});
            } else {
                if (next.templateUrl === 'parts/dashboard.html') {
                    $location.path('/login');
                }
            }
        } else {
            if (next.templateUrl === 'parts/login.html') {
                $location.path('/dashboard');
            }
        }
    });

    Socket.on('connect', function () {
        $('#disconnectedModal').modal('hide');
        if ($location.url() !== "/login") {
            console.log("Connected");
        }
    });

    Socket.on('disconnect', function () {
        $('#disconnectedModal').modal('show');
    });
});

app.controller('dashboardctrl', function ($rootScope, $scope, Socket) {
    $rootScope.console = [];
    var maxCount = 20;
    Socket.on('console', function (data) {
        if ($rootScope.console.length == maxCount) $rootScope.console.shift();
        $rootScope.console.push(data.trim().replace(/\[0;..;\d+m/gmi, "").replace(/\[m$/gmi, ""));
    });

    $scope.sendCommand = function () {
        Socket.emit('console-command', $scope.command);
        $scope.command = "";
    };
});

app.controller('playersctrl', function ($scope, Socket) {
    $scope.users = [];

    $scope.kick = function (user) {
        Socket.emit('console-command', "kick " + user);
    };
    $scope.ban = function (user) {
        Socket.emit('console-command', "ban " + user);
    };
    $scope.pardon = function (user) {
        Socket.emit('console-command', "pardon " + user);
    };
    $scope.op = function (user) {
        Socket.emit('console-command', "op " + user);
    };

    Socket.emit('request', 'player-list');
    Socket.on('player-list', function (users) {
        $scope.users = users;
    });
});

app.controller('worldsctrl', function ($scope, Socket) {
    $scope.worlds = [];

    // TODO Make it with modals
    $scope.delete = function (name) {
        if (confirm("Are you sure?")) {
            Socket.emit('world-action', {action: 'delete', worldName: name});
        }
    };

    $scope.clone = function (name) {
        var to = prompt("Cloned world name:");
        Socket.emit('world-action', {action: 'clone', worldName: name, to: to});
    };

    $scope.rename = function (name) {
        var to = prompt("New world name:");
        Socket.emit('world-action', {action: 'rename', worldName: name, to: to});
    };

    Socket.emit('request', 'world-list');
    Socket.on('world-list', function (worlds) {
        $scope.worlds = worlds;
    });
});

app.controller('pluginsctrl', function ($scope, Socket) {
    $scope.plugins = [];

    $scope.disable = function (name) {
        Socket.emit('plugin-disable', name);
    };

    Socket.emit('request', 'plugin-list');
    Socket.on('plugin-list', function (plugins) {
        $scope.plugins = plugins;
    });
});

app.controller('loginctrl', function ($scope, $rootScope, $location, $cookies, Socket) {

    $scope.username = "";
    $scope.password = "";

    Socket.on('login-success', function (data) {
        $rootScope.loggedin = true;
        $rootScope.username = data.username;
        $cookies.put('session', data.session);
        $cookies.put('username', data.username);
        $location.path('/dashboard');
    });
    Socket.on('login-failed', function (data) {
        $scope.username = "";
        $scope.password = "";
        $location.path('/login');
    });

    $scope.signin = function () {
        Socket.emit('login-data', {username: $scope.username, password: $scope.password});
    };

});

app.controller('navctrl', function ($rootScope, $scope, $cookies, Socket) {

    $scope.logout = function () {
        Socket.emit('logout', $rootScope.username);
        console.log("Logged out");
        location.reload();
    };

});

angular.module('Services', ['ngRoute']).
factory('Socket', function ($rootScope, $location) {
    var socket = io.connect('http://127.0.0.1:8081', {reconnect: true});

    return {
        on: function (eventName, callback) {
            socket.on(eventName, function () {
                var args = arguments;
                $rootScope.$apply(function () {
                    callback.apply(socket, args);
                });
            });
        },
        emit: function (eventName, data, callback) {
            if (typeof data == 'function') {
                callback = data;
                data = {};
            }
            socket.emit(eventName, data, function () {
                var args = arguments;
                $rootScope.$apply(function () {
                    if (callback) {
                        callback.apply(socket, args);
                    }
                });
            });
        }
    };
});