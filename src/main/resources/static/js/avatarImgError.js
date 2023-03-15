function getDefaultAvatar() {
    document.getElementById('avatar').src = 'static/img/profile-avatar-default.jpg'
}
function getDefaultAvatarMini() {
    document.getElementById('avatarMini').src = 'static/img/profile-avatar-default.jpg'
}

function avatarLoadError(image) {
    image.onerror = "";
    image.src = "/static/img/profile-avatar-default.jpg";
    return true;
}

function avatarLoadError2(image) {
    image.onerror = "";
    image.src = "/static/img/profile-avatar-default.jpg";
    return true;
}