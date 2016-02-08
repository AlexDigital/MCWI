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
    $routeProvider.otherwise({redirectTo: '/dashboard'});
});

app.run(function ($rootScope, Socket, $cookies, $location) {
    $rootScope.loggedin = false;

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
        if ($location.url() !== "/login") {
            console.log("Connected");
            $('#disconnectedModal').modal('hide');
            location.reload();
        }
    });

    Socket.on('disconnect', function () {
        $('#disconnectedModal').modal('show');
    });
});

app.controller('dashboardctrl', function ($scope, Socket) {
    $scope.console = [];
    $scope.users = [];
    var maxCount = 20;
    Socket.on('console', function (data) {
        if ($scope.console.length == maxCount) $scope.console.shift();
        $scope.console.push(data.trim().replace(/\[0;..;\d+m/gmi, "").replace(/\[m$/gmi, ""));
    });

    $scope.sendCommand = function () {
        Socket.emit('console-command', $scope.command);
        $scope.command = "";
    };

    $scope.kick = function (user) {
        Socket.emit('console-command', "kick " + user);
    };
    $scope.ban = function (user) {
        Socket.emit('console-command', "ban " + user);
    };
    $scope.pardon = function (user) {
        Socket.emit('console-command', "pardon " + user);
    };

    Socket.emit('request', 'player-list');
    Socket.on('player-list', function (users) {
        $scope.users = users;
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