package io.zenro.formed.ops

import io.zenro.formed.FormEncoder

import cats.data.Chain

trait EncoderOps {
  implicit final def toEncoderSyntax[T](value: T): EncoderOps.EncoderSyntax[T] = new EncoderOps.EncoderSyntax(value)
}

object EncoderOps {
  implicit class EncoderSyntax[T](val value: T) extends AnyVal {
    def asFormData(implicit ev: FormEncoder[T]): Chain[(String, String)] = ev.encode(value)
    def asFormUrlEncoded(implicit ev: FormEncoder[T]): String = ev.toUrlEncoded(value)
    def asFormPrettyString(implicit ev: FormEncoder[T]): String = ev.toPrettyString(value)
  }
}