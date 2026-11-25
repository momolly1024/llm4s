package org.llm4s.imageguidecheck

// TEMPORARY: compiles every Scala block of docs/guide/image-processing.md in CI (llm4s/llm4s#1544).
// Each object holds one block as written; nothing here is ever called. Remove before opening the pull request.

// docs/guide/image-processing.md, line 62
object ChoosingAClientSnippet {

  import org.llm4s.imageprocessing.{ ImageProcessing, ImageProcessingClient }

  def localClient(): ImageProcessingClient =
    ImageProcessing.localImageProcessor()

  def openAIClient(apiKey: String, model: String): ImageProcessingClient =
    ImageProcessing.openAIVisionClient(apiKey, model)

  def anthropicClient(apiKey: String, model: String): ImageProcessingClient =
    ImageProcessing.anthropicVisionClient(apiKey, model)

  def geminiClient(apiKey: String, model: String): ImageProcessingClient =
    ImageProcessing.geminiVisionClient(apiKey, model)
}

// docs/guide/image-processing.md, line 86
object ThumbnailSnippet {

  import org.llm4s.error.LLMError
  import org.llm4s.imageprocessing.{ ImageOperation, ImageProcessing, ProcessedImage }
  import java.nio.file.Paths

  def makeThumbnail(inputPath: String): Either[LLMError, ProcessedImage] = {
    val processor  = ImageProcessing.localImageProcessor()
    val operations = List(ImageOperation.Resize(width = 512, height = 512), ImageOperation.Grayscale)

    for {
      thumbnail <- processor.preprocessImage(inputPath, operations)
      _         <- thumbnail.saveToFile(Paths.get("thumbnail.png"))
    } yield thumbnail
  }
}

// docs/guide/image-processing.md, line 108
object ConvertAndResizeSnippet {

  import org.llm4s.error.LLMError
  import org.llm4s.imageprocessing.{ ImageProcessing, ProcessedImage }
  import org.llm4s.media.MediaType

  def convertAndResize(inputPath: String): Either[LLMError, (ProcessedImage, ProcessedImage)] = {
    val processor = ImageProcessing.localImageProcessor()

    for {
      jpeg    <- processor.convertFormat(inputPath, MediaType.Jpeg)
      resized <- processor.resizeImage(inputPath, width = 800, height = 600, maintainAspectRatio = false)
    } yield (jpeg, resized)
  }
}

// docs/guide/image-processing.md, line 135
object LocalAnalysisSnippet {

  import org.llm4s.imageprocessing.ImageProcessing

  def describeLocally(inputPath: String): Unit = {
    val processor = ImageProcessing.localImageProcessor()

    processor.analyzeImage(inputPath) match {
      case Right(analysis) => println(s"${analysis.description} (tags: ${analysis.tags.mkString(", ")})")
      case Left(error)     => println(error.message)
    }
  }
}

// docs/guide/image-processing.md, line 157
object VisionAnalysisSnippet {

  import org.llm4s.error.LLMError
  import org.llm4s.imageprocessing.{ ImageAnalysisResult, ImageProcessing }

  def askAboutImage(apiKey: String, model: String, imagePath: String): Either[LLMError, ImageAnalysisResult] = {
    val client = ImageProcessing.openAIVisionClient(apiKey = apiKey, model = model)

    client.analyzeImage(imagePath, Some("What is in this picture? Answer in two sentences."))
  }
}

// docs/guide/image-processing.md, line 190
object AsyncAnalysisSnippet {

  import org.llm4s.imageprocessing.ImageProcessingClient
  import scala.concurrent.Future

  def printDescriptionLater(client: ImageProcessingClient, imagePath: String): Future[Unit] = {
    import scala.concurrent.ExecutionContext.Implicits.global

    client.analyzeImageAsync(imagePath).map(result => println(result.map(_.description)))
  }
}

// docs/guide/image-processing.md, line 204
object CustomEndpointSnippet {

  import org.llm4s.imageprocessing.ImageProcessingClient
  import org.llm4s.imageprocessing.config.OpenAIVisionConfig
  import org.llm4s.imageprocessing.provider.OpenAIVisionClient
  import org.llm4s.security.Secret
  import scala.concurrent.duration._

  def clientForGateway(apiKey: String, model: String, baseUrl: String): ImageProcessingClient = {
    val config = OpenAIVisionConfig(
      apiKey = Secret(apiKey),
      model = model,
      baseUrl = baseUrl,
      requestTimeout = 2.minutes
    )

    new OpenAIVisionClient(config)
  }
}

// docs/guide/image-processing.md, line 266
object ExplainErrorSnippet {

  import org.llm4s.error.{ ConfigurationError, InvalidInputError, LLMError, ProcessingError, ServiceError }

  def explain(error: LLMError): String =
    error match {
      case e: InvalidInputError  => s"Rejected ${e.field}: ${e.reason}"
      case e: ConfigurationError => s"Fix the configuration: ${e.message}"
      case e: ServiceError       => s"${e.provider} answered HTTP ${e.status}: ${e.message}"
      case e: ProcessingError    => s"The ${e.operation} step failed: ${e.message}"
      case other                 => other.message
    }
}

// docs/guide/image-processing.md, line 282
object SavePathCheckSnippet {

  import org.llm4s.error.LLMError
  import org.llm4s.imageprocessing.ProcessedImage
  import java.nio.file.Paths

  def saveOutsideWorkingDirectory(image: ProcessedImage): Either[LLMError, Unit] =
    // A Left: the normalised path is ../escaped.png
    image.saveToFile(Paths.get("out/../../escaped.png"))
}
