var errorsMsg = {
  "nick":    Messages("error.comment.illegal.nick"),
  "body":    Messages("error.comment.body.is.required")
};

$(function() {

    $("form.addCommentForm").submit(function(e){

        var form = $(this);
        e.preventDefault();
        ajax(
            jsRoutes.controllers.comments.CommentsController.saveComment().url,
            getData(form),
            function(data) {
                proccessResponse(data, form);
            },
            processError);
    });

    function getData(form) {
        var data = {};
        data['postId'] = parseInt(form.find('[name="postId"]').val());
        data['nick'] = form.find('[name="nick"]').val();
        data['body'] = form.find('[name="body"]').val();
        return JSON.stringify(data);
    }

    function proccessResponse(data, form) {
      toogleErrors(form, data.errors);
      var isDataCorrect = data.errors === undefined;
      if (isDataCorrect) {
          var c = createComment(data);
          $('#comments-list').append(c);
          form.find('[name="body"]').val('');
      }
    }

    function toogleErrors(form, errors) {
      var elements = [ form.find('[name="nick"]'), form.find('[name="body"]') ];
      $(elements).each(function (idx, elem) {
        var name = elem.attr('name');
        var error = errors != undefined && errors.indexOf(name) != -1;
        var parent = elem.parent();
        parent.toggleClass("has-error", error);
        parent.find("span.error-msg").text(error ? errorsMsg[name] : '');
      });
    }

    function processError(data) {
        console.error(data);
    }

    function createComment(data) {
        return '' +
          '<div class="col-sm-12 comment">' +
          '     <div>' +
            (data.logged ? '<a href="/posts/nick/' + data.author + '/0">' : '') +
          (data.logged ? '' : '~ ') + data.author +
            (data.logged ? '</a>' : '') +
            ' / ' + data.created + '</div>' +
          '     <div>' + data.body + '</div>' +
          '</div>';
    }

});
