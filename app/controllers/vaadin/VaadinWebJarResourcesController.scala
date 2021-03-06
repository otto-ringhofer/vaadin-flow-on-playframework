package controllers.vaadin

import akka.stream.scaladsl.StreamConverters
import com.typesafe.config.Config
import javax.inject.Inject
import org.vaadin.playintegration.VaadinResources
import play.api.Environment
import play.api.cache.SyncCacheApi
import play.api.http.{FileMimeTypes, HttpEntity}
import play.api.mvc._

// TODO make use of Vaadins WebJarServer here?
class VaadinWebJarResourcesController @Inject()(
                                                 configuration: Config,
                                                 cache: SyncCacheApi,
                                                 environment: Environment,
                                                 mimeTypes: FileMimeTypes,
                                                 cc: ControllerComponents) extends AbstractController(cc) {
  val defaultChunkSize = 8192

  def webJarResource(path: String): Action[AnyContent] = cc.actionBuilder.apply { request =>
    var resource = VaadinResources.getWebjarResourceAsStream(path)
    val response = resource.map(stream => {
      val mimetype = mimeTypes.forFileName(path)
      Result(
        header = ResponseHeader(200),
        body = HttpEntity.Streamed(
          StreamConverters.fromInputStream(() => stream, defaultChunkSize),
          None,
          mimetype
        )
      )

    }).getOrElse({
      Results.NotFound("resource with path " + path + " not found")
    })

    response
  }

}