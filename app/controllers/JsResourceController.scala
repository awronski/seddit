package controllers

import play.api.mvc.{Action, Controller}
import play.api.routing.JavaScriptReverseRouter

class JsResourceController extends Controller {

  def javascriptRoutes = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        controllers.posts.routes.javascript.CreatePostController.savePost,
        controllers.votes.routes.javascript.VotesController.vote,
        controllers.comments.routes.javascript.CommentsController.saveComment
      )
    ).as("text/javascript")
  }

}
