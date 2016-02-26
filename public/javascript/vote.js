function vote(postId, value) {
    var data = { "postId": postId, "vote": value };
    ajax(
        jsRoutes.controllers.votes.VotesController.vote().url,
        JSON.stringify(data),
        function(data) { proccessVoteResponse(data, postId) },
        function(data) { processVoteError(data, postId) }
    );
}

function proccessVoteResponse(data, postId) {
    if (data.result != undefined) {
        var votes = $("#votes" + postId);
        votes.fadeToggle();
        votes.html(data.result);
        votes.fadeToggle();
    } else {
        processVoteError(data, postId);
    }
}

function processVoteError(data, postId) {
    console.warn("Cannot process vote for post " + postId);
}

$(function() {
    $(".vote").click(function() {
        var e = $(this);
        var postId = parseInt(e.attr("href").substring(1));
        var value = e.hasClass("vote-up") ? 1 : -1;
        var id = e.attr("id");

        vote(postId, value, id)
    });
});
