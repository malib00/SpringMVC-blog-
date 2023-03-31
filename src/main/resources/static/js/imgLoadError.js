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

function postImageLoadError(image) {
    image.onerror = "";
    image.src = "/static/img/post-image-default.jpg";
    return true;
}
