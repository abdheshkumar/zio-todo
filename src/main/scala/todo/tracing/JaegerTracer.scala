package todo.tracing

import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes
import todo.config.AppConfig
import zio._;

object JaegerTracer {

  def live: RLayer[AppConfig, Tracer] =
    ZLayer
      .service[AppConfig]
      .flatMap(c =>
        (for {
          spanExporter <- Task(
            JaegerGrpcSpanExporter
              .builder()
              .setEndpoint(c.get.tracer.host)
              .build()
          )
          spanProcessor <- UIO(SimpleSpanProcessor.create(spanExporter))
          tracerProvider <- UIO {
            val serviceNameResource = Resource.create(
              Attributes
                .of(ResourceAttributes.SERVICE_NAME, "zio-todo-example")
            )
            SdkTracerProvider
              .builder()
              .addSpanProcessor(spanProcessor)
              .setResource(Resource.getDefault.merge(serviceNameResource))
              .build()
          }
          openTelemetry <- UIO(
            OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build()
          )
          tracer <- UIO(
            openTelemetry
              .getTracer("zio.telemetry.opentelemetry.example.JaegerTracer")
          )
        } yield tracer).toLayer
      )

}
