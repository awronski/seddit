var errorsMsg = {
    "title":    Messages("error.addForm.title"),
    "link":     Messages("error.addForm.link"),
    "body":     Messages("error.addForm.body"),
    "tags":     Messages("error.addForm.tags")
};

var submiting = false;

$(function() {

    $("#submitter").click(function() {
        $("form#addPostForm").submit();
    });

    $("form#addPostForm").submit(function(e){

        e.preventDefault();
        if (submiting) return;

        submitting(true);
        ajax(
            jsRoutes.controllers.posts.CreatePostController.savePost().url,
            getData($(this)),
            processFormResponse,
            processFormError);
    });

    function getData(form) {
        var data = {};
        data['title'] = form.find("#title").val();
        data['link'] = form.find("#link").val();
        data['body'] = form.find("#body").val();
        data['pictures'] = 0;
        data['tags'] = $("#tags").tagsinput('items');
        return JSON.stringify(data);
    }

    function processFormResponse(data) {
        var results = data.results;
        if (results.id != undefined) {
            window.location.href = "/post/" + results.id + "/" + results.title;
        } else {
            showErrors(results.errors);
            submitting(false);
        }
    }
    function processFormError(data) {
        submitting(false);
        console.error("Cannot process form");
    }

    function showErrors(formErrors) {
        $("#addPostForm > * :input, textarea").each(function() {
            var withError = formErrors.indexOf(this.id) != -1;
            toggleError(this, withError);
        });

        var tagsWithError = formErrors.indexOf("tags") != -1;
        toggleError($("#tags")[0], tagsWithError);

        var dropzoneWithError = formErrors.indexOf("pictures") != -1;
        $("#dropzone").toggleClass("has-error", dropzoneWithError);
    }

    function toggleError(elem, withError) {
        var id = elem.id;
        var parent = $(elem).parent();
        parent.toggleClass("has-error", withError);
        if (withError) {
            parent.find("span.error-msg").text(errorsMsg[id]);
        } else {
            parent.find("span.error-msg").empty();
        }
    }

    function submitting(start) {
        submiting = start;
        $("#spinner").toggleClass("hide", !start);
        $("#submitter").toggleClass("btn-info", !start);
        $("#saving-post-msg").toggleClass("hide", !start);
    }

    Dropzone.options.dropzone = {
        previewTemplate: '<div class="dz-preview dz-file-preview"><div class="dz-details"><div class="dz-filename"><span data-dz-name></span></div><div class="dz-size" data-dz-size></div><img data-dz-thumbnail /></div><div class="dz-progress"><span class="dz-upload" data-dz-uploadprogress></span></div><div class="dz-success-mark"><span>V</span></div><div class="dz-error-mark"><span>X</span></div><div class="dz-error-message"><span data-dz-errormessage></span></div></div>',
        dictDefaultMessage: Messages("dropzone.drop.files.here.to.upload"),
        paramName: "pic",
        maxFilesize: maxFileUploadSize,
        maxFiles: 8,
        acceptedFiles: "image/jpg,image/jpeg,image/png"
    };
});
