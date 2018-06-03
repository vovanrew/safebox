//getting profile

var profileJson = localStorage.getItem("profile");
var profile = JSON.parse(profileJson);
var user = profile.user;
var files = [];

//server config
var host = "localhost";
var port = "9000";
var serverName = "http://" + host + ":" + port;

document.getElementById("newFileButton").addEventListener("click", showNewFileForm);

requestUserFiles();

document.getElementById("logo").addEventListener("click", function () {
    cleanFiles();
    setProfile();
});

document.getElementById("backButton").addEventListener("click", function () {
    cleanFiles();
    setProfile();
});

function cleanFiles() {
    files.forEach(function (file) {
        var node = document.getElementById("fileNode" + file.id);
        node.style.display = "none";
        filesContainer.removeChild(node);
        node = document.getElementById("hr-fileNode" + file.id);
        filesContainer.removeChild(node);
    })
}

function requestUserFiles(user) {
    var req = request("GET", serverName + "/my/files");
    req.onload = function () {
        if(req.status === 200) {
            var data = JSON.parse(req.response);
            files = JSON.parse(data.data);
            setProfile();
        } else {
            console.log("Error ocured while uploading files.")
        }
    }
}

function showNewFileForm() {
    document.getElementById("fileListContainer").style.display = "none";
    document.getElementById("fileContainer").style.display = "none";
    document.getElementById("newFileFormContainer").style.display = "block";
    document.getElementById("backButton").style.display = "block";
}

function setProfile() {
    document.getElementById("profileName").innerHTML = profile.user.name;
    document.getElementById("newFileFormContainer").style.display = "none";
    document.getElementById("fileContainer").style.display = "none";
    document.getElementById("fileListContainer").style.display = "block";
    document.getElementById("backButton").style.display = "none";
    fileList();
}

function fileList() {
    if(files.length > 0) {
        files.forEach(function (file) {
            var html =
                '<div class="col">' +
                '   <h4><p style="cursor:pointer;color:blue;" id="file' + file.id + '">' + file.filename + '</p></h4>\n' +
                '   <p>' + file.createdAt + '</p>\n' +
                '</div>' +
                '<div class="col-1">' +
                '   <button id="fileDelete' + file.id + '" type="button" class="btn btn-secondary">Delete</button>' +
                '</div>';

            addFileElement("filesContainer", "div", "fileNode" + file.id, html);
            document.getElementById("file" + file.id).addEventListener("click", function() {
                document.getElementById("fileListContainer").style.display = "none";
                document.getElementById("newFileFormContainer").style.display = "none";
                document.getElementById("fileContainer").style.display = "block";
                document.getElementById("filename").innerHTML = file.filename;
                document.getElementById("fileDescription").innerHTML = file.description;
                document.getElementById("createdAt").innerHTML = file.createdAt;
                document.getElementById("identifier").innerHTML =
                    location.hostname +":"+ location.port + "/safebox/public" + "/repository.html?file=" + file.urlIdentifier;

                var keyField = document.getElementById("secureKey");
                if (file.isSecured === false) {
                    keyField.style.display = "none";
                } else {
                    document.getElementById("key").innerHTML = file.accessKey;
                }

                document.getElementById("backButton").style.display = "block";
            });

            document.getElementById("fileDelete" + file.id).addEventListener("click", function () {
                deleteFile(file);
            })
        })
    }
}

function deleteFile(file) {
    var req = requestJson("DELETE", serverName + "/file", file);
    req.onload = function (ev) {
        if (req.status === 200) {
            var node = document.getElementById("fileNode" + file.id);
            node.remove();
            node = document.getElementById("hr-fileNode" + file.id);
            node.remove();
        } else {
            setProfile();
            console.log("Error occured while deleting file.")
        }
    }
}

function addFileElement(parentId, elementTag, elementId, html) {
    // Adds an element to the document
    var p = document.getElementById(parentId);
    var newElement = document.createElement(elementTag);
    newElement.setAttribute('id', elementId);
    newElement.setAttribute('class', 'row');
    newElement.innerHTML = html;
    p.appendChild(newElement);
    var hr = document.createElement("hr");
    hr.setAttribute("id", "hr-" + elementId);
    hr.style.width = "102%";
    p.appendChild(hr);
}

var fileSelect = document.getElementById("fileSelect");
var uploadButton = document.getElementById("uploadButton");

document.getElementById("uploadButton").addEventListener("click", sendFile);

function sendFile() {
    uploadButton.innerHTML = 'Uploading...';
    var files = fileSelect.files;

    if(files.length === 0) {
        uploadButton.innerHTML = 'Upload';
        return;
    }

    reader = new FileReader();
    var file = files[0];

    reader.onload = function(e) {
        var data = e.target.result;
        (async () => {
            var mode = 'AES-GCM',
                length = 256,
                ivLength = 12;

            var encrypted = await encrypt(data, 'password', mode, length, ivLength);
            console.log(encrypted); // { cipherText: ArrayBuffer, iv: Uint8Array }
            var enc = new TextDecoder("utf-8");
            var encryptedFile = new File([encrypted.cipherText], file.name);

            var formData = new FormData();
            formData.append("newFile", encryptedFile, file.name);

            var req = sendFileData("POST", serverName + "/upload", formData);
            req.onload = function () {
                if (req.status === 200) {
                    var description = document.getElementById("description").value;
                    var checkBox = document.getElementById("isSecured");
                    var isSecured;

                    if (checkBox.checked === true) {
                        isSecured = true;
                    } else {
                        isSecured = false;
                    }

                    console.log(encrypted.iv);
                    var meta = new FileMetadata(0, user.id, file.name, description, Date.now(), "", JSON.stringify(encrypted.iv), isSecured, "");
                    var xhr = requestJson("POST", serverName + "/metaupload", meta);
                    xhr.onload = function (ev) {
                        if(xhr.status === 200) {
                            console.log("File uploaded successfully");
                            var file = JSON.parse(JSON.parse(xhr.response).data);
                            fileInfo(file);
                        } else {
                            console.log("error ocured while uploading file metadata")
                        }
                    };

                    uploadButton.innerHTML = 'Upload';
                } else {
                    console.log(req.response);
                    console.log("error ocured");
                    uploadButton.innerHTML = 'Upload';
                }
            }

            var decrypted = await decrypt(encrypted, 'password', mode, length);
            var enc = new TextDecoder("utf-8");
            console.log(enc.decode(decrypted)); // Secret text
        })();
    };

    reader.readAsArrayBuffer(file);
}

function fileInfo(file) {
    document.getElementById("backButton").style.display = "block";
    document.getElementById("fileListContainer").style.display = "none";
    document.getElementById("newFileFormContainer").style.display = "none";
    document.getElementById("fileContainer").style.display = "block";
    document.getElementById("filename").innerHTML = file.filename;
    document.getElementById("fileDescription").innerHTML = file.description;
    document.getElementById("createdAt").innerHTML = file.createdAt;
    document.getElementById("identifier").innerHTML =
        location.hostname +":"+ location.port + "/safebox/public" + "/repository.html?file=" + file.urlIdentifier;

    var keyField = document.getElementById("secureKey");
    if (file.isSecured === false) {
        keyField.style.display = "none";
    } else {
        document.getElementById("key").innerHTML = file.accessKey;
    }
}

//helpers
function request(method, where, data) {
    var xhr = new XMLHttpRequest();
    xhr.open(method, where, true);
    xhr.withCredentials = true;
    xhr.send();

    return xhr;
}

function sendFileData(method, where, data) {
    var xhr = new XMLHttpRequest();
    xhr.open(method, where, true);
    xhr.withCredentials = true;
    xhr.setRequestHeader("encoding", "multipart/form-data");
    xhr.send(data);

    return xhr;
}

function requestJson(method, where, data) {
    var xhr = new XMLHttpRequest();
    var body = JSON.stringify(data);
    console.log(body);
    xhr.open(method, where, true);
    xhr.withCredentials = true;
    xhr.setRequestHeader('Content-type', 'application/json; charset=utf-8');
    xhr.send(body);

    return xhr;
}

//file meta dto
function FileMetadata(id, userId, filename, description, createdAt, urlIdentifier, initVector, isSecured, accessKey) {
    this.id = id;
    this.userId = userId;
    this.filename = filename;
    this.description = description;
    this.createdAt = createdAt;
    this.urlIdentifier = urlIdentifier;
    this.initVector = initVector;
    this.isSecured = isSecured;
    this.accessKey = accessKey;
}

async function genEncryptionKey (password, mode, length) {
    var algo = {
        name: 'PBKDF2',
        hash: 'SHA-256',
        salt: new TextEncoder().encode('a-unique-salt'),
        iterations: 1000
    };
    var derived = { name: mode, length: length };
    var encoded = new TextEncoder().encode(password);
    var key = await crypto.subtle.importKey('raw', encoded, { name: 'PBKDF2' }, false, ['deriveKey']);

    return crypto.subtle.deriveKey(algo, key, derived, false, ['encrypt', 'decrypt']);
}

async function encrypt (text, password, mode, length, ivLength) {
    var algo = {
        name: mode,
        length: length,
        iv: crypto.getRandomValues(new Uint8Array(ivLength))
    };
    var key = await genEncryptionKey(password, mode, length);
    var encoded = text;

    return {
        cipherText: await crypto.subtle.encrypt(algo, key, encoded),
        iv: algo.iv
    };
}

async function decrypt (encrypted, password, mode, length) {
    var algo = {
        name: mode,
        length: length,
        iv: encrypted.iv
    };
    var key = await genEncryptionKey(password, mode, length);
    var decrypted = await crypto.subtle.decrypt(algo, key, encrypted.cipherText);

    return decrypted;
}
