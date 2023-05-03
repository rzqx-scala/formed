package io.github.rzqx.formed

import cats._
import cats.data.Chain
import cats.implicits._

import java.net.URLEncoder

trait FormEncoder[T] {
  def chained(value: T): Chain[(Chain[String], String)]

  def encode(value: T): Chain[(String, String)] =
    chained(value).map { case (prefix, v) =>
      val k = prefix.deleteFirst(_ => true) match {
        case Some((head, tail)) => head + tail.foldMap(v => s"[$v]")
        case None               => ""
      }
      (k, v)
    }

  def toPrettyString(value: T): String = {
    encode(value)
      .map { case (k, v) => s"$k=$v" }
      .toList
      .mkString("\n")
  }

  def toUrlEncoded(value: T): String = {
    val combined = encode(value)
      .map { case (k, v) => s"$k=$v" }
      .toList
      .mkString("&")

    URLEncoder.encode(combined, "UTF-8")
  }
}

object FormEncoder {
  def apply[T](implicit ev: FormEncoder[T]): FormEncoder[T] = ev
  def encode[T: FormEncoder](value: T): Chain[(String, String)] = FormEncoder[T].encode(value)
  def toUrlEncoded[T: FormEncoder](value: T): String = FormEncoder[T].toUrlEncoded(value)
  def toPrettyString[T: FormEncoder](value: T): String = FormEncoder[T].toPrettyString(value)

  implicit val catsContravariantForEncodeForm: Contravariant[FormEncoder] = new Contravariant[FormEncoder] {
    override def contramap[A, B](fa: FormEncoder[A])(f: B => A): FormEncoder[B] = (v: B) => fa.chained(f(v))
  }
}
