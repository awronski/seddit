package modules

import actors.PicturesCacheActor
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class ActorModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[PicturesCacheActor]("picturesCacheActor")
  }

}
