
# Formed

Helper library to squash generic nested products into a list of fields
for www-form-urlencoded requests, typically to interface with APIs
that don't support JSON.

Currently supports Scala 2.12 and 2.13.

# Quick Usage

Imports:
```scala mdoc
import io.github.rzqx.formed.implicits._
import io.github.rzqx.formed.syntax._
```

Define your ADT (using Stripe's API as an example) and create an instance:
```scala mdoc:silent
final case class LineItem(price: String, quantity: Int)
final case class CheckoutSession(mode: String, line_items: List[LineItem])

val item1 = LineItem("price1", 1)
val item2 = LineItem("price2", 2)
val checkout = CheckoutSession("payment", List(item1, item2))
```

Squash the instance to a form:
```scala mdoc
checkout.asFormData

checkout.asFormUrlEncoded
```

Use in http4s:
```scala mdoc
import org.http4s.UrlForm

UrlForm.fromChain(checkout.asFormData)
```

## Typeclasses

Define an encoder for a new type by converting it into a string:
```scala mdoc
import io.github.rzqx.formed.FormEncoder
import cats.implicits._
import scala.concurrent.duration._

implicit val durationEncoder: FormEncoder[Duration] =
  FormEncoder[String].contramap(_.toSeconds.toString)
  
final case class Foo(duration: Duration)

Foo(1.hour).asFormDisplay 
```

Define a prefix encoder to change the way nested fields are encoded:
```scala mdoc:reset
import io.github.rzqx.formed.PrefixEncoder
import io.github.rzqx.formed.instances.EncoderInstances._
import io.github.rzqx.formed.syntax._
import cats.data.Chain
import cats.implicits._

implicit val arrowPrefixEncoder: PrefixEncoder = (value: Chain[String]) =>
    value.deleteFirst(_ => true) match {
      case Some((head, tail)) => head + tail.foldMap(v => s" --> $v")
      case None => ""
    }


final case class LineItem(price: String, quantity: Int)
final case class CheckoutSession(mode: String, line_items: List[LineItem])

CheckoutSession("payment", List(LineItem("price1", 1))).asFormDisplay
```