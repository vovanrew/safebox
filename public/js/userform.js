//server config
var host = "localhost";
var port = "9000";
var serverName = "http://" + host + ":" + port;

//hiding login/register forms
document.getElementById("register").style.display = 'none';
document.getElementById("login").style.display = 'block';
location.href = location.pathname + "#login";

document.getElementById("loginLink").addEventListener("click", hideUserRegisterForm);
document.getElementById("registerLink").addEventListener("click", hideUserLoginForm);

function hideUserRegisterForm() {
    location.href = location.pathname + "#login";
    document.getElementById("register").style.display = 'none';
    document.getElementById("login").style.display = 'block';
}

function hideUserLoginForm() {
    location.href = location.pathname + "#register";
    document.getElementById("login").style.display = 'none';
    document.getElementById("register").style.display = 'block';
}

//login actions
document.getElementById("loginButton").addEventListener("click", login);

function login() {
    var user = new User(0, "", "", "");
    user.email = document.getElementById("loginEmail").value;
    user.password = document.getElementById("loginPassword").value;

    var req = request("POST", serverName + "/login", user);

    req.onload = function () {
        if (req.status === 200) {
            var body = JSON.parse(req.response);
            var profile = body.data;
            localStorage.setItem("profile", profile);
            location.href = "profile.html";
        } else if (req.status >= 400) {
            var respBody = JSON.parse(req.response);
            var data = JSON.parse(respBody.data);
            errorAlert("loginError", data.reason)
        }
    }

    req.onerror = function () {
        errorAlert("registerError", "Something went wrong. Check your internet connection and retry later.")
    }
}


//register action
document.getElementById("registerButton").addEventListener("click", register);

function register() {
    var password = document.getElementById("registerPassword").value;
    var confirmedPassword = document.getElementById("registerConfirmPassword").value;
    var email = document.getElementById("registerEmail").value;
    var name = document.getElementById("registerName").value;

    if (isRegisterDataValid(name, email, password, confirmedPassword) == false) {
        errorAlert("registerError", "Invalid input data. Please check that all fields are filled and passwords are matched.");
    } else {
        var user = new User(0, "", "", "");
        user.email = email;
        user.name = name;
        user.password = password;

        var req = request("POST", serverName + "/register", user)
        req.onload = function () {
            if (req.status === 200) {
                successAlert("registerSuccess", "You are successfully registered. Now you can log in.")
            } else if (req.status >= 400) {
                var respBody = JSON.parse(req.response);
                var data = JSON.parse(respBody.data);
                errorAlert("registerError", data.reason)
            }
        }

        req.onerror = function () {
            errorAlert("registerError", "Something went wrong. Check your internet connection and retry later.")
        }
    }

}

//forms/dto
function User(id, name, email, password) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.password = password;
}

//helpers
function request(method, where, data) {
    var xhr = new XMLHttpRequest();
    var body = JSON.stringify(data);

    xhr.open(method, where, true);
    xhr.withCredentials = true;
    xhr.setRequestHeader('Content-type', 'application/json; charset=utf-8');
    xhr.send(body);

    return xhr;
}

function isRegisterDataValid(name, email, password, confirm) {
    if (name === "" || email === "" || password === "" || confirm === "") {
        return false;
    } else if (password !== confirm) {
        return false;
    }

    return true;
}

function successAlert(alertName, alertText) {
    document.getElementById("registerErrorAlert").style.display = "none";
    document.getElementById(alertName + "Alert").style.display = "block";
    document.getElementById(alertName).innerHTML = alertText;
}

function errorAlert(alertName, alertText) {
    document.getElementById(alertName + "Alert").style.display = "block";
    document.getElementById(alertName).innerHTML = alertText;
}
