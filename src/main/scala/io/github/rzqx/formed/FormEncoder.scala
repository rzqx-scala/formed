package io.github.rzqx.formed

import cats._
import cats.data.Chain

import java.net.URLEncoder

trait FormEncoder[T] {
  def chained(value: T): Chain[(Chain[String], String)]

  def encode(value: T, prefixEncoder: Chain[String] => String): Chain[(String, String)] =
    chained(value).map { case (prefix, v) =>
      prefixEncoder(prefix) -> v
    }

  def toDisplay(value: T, prefixEncoder: Chain[String] => String): String = {
    encode(value, prefixEncoder)
      .map { case (k, v) => s"$k=$v" }
      .toList
      .mkString("\n")
  }

  def toUrlEncoded(value: T, prefixEncoder: Chain[String] => String): String = {
    val combined = encode(value, prefixEncoder)
      .map { case (k, v) => s"$k=$v" }
      .toList
      .mkString("&")

    URLEncoder.encode(combined, "UTF-8")
  }
}

object FormEncoder {
  def apply[T](implicit ev: FormEncoder[T]): FormEncoder[T] = ev
  def encode[T: FormEncoder](value: T)(implicit pe: PrefixEncoder): Chain[(String, String)] =
    FormEncoder[T].encode(value, pe.encode)
  def toUrlEncoded[T: FormEncoder](value: T)(implicit pe: PrefixEncoder): String =
    FormEncoder[T].toUrlEncoded(value, pe.encode)
  def toDisplay[T: FormEncoder](value: T)(implicit pe: PrefixEncoder): String =
    FormEncoder[T].toDisplay(value, pe.encode)

  implicit val catsContravariantForEncodeForm: Contravariant[FormEncoder] = new Contravariant[FormEncoder] {
    override def contramap[A, B](fa: FormEncoder[A])(f: B => A): FormEncoder[B] = (v: B) => fa.chained(f(v))
  }
}
