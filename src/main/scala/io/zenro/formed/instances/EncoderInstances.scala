package io.zenro.formed.instances

import io.zenro.formed.FormEncoder

import cats._
import cats.data.Chain
import cats.implicits._

import java.util.UUID

import shapeless._
import shapeless.labelled._

trait EncoderInstances {
  implicit val encodeString: FormEncoder[String] = (v: String) => Chain(Chain.empty -> v)
  implicit val encodeUnit: FormEncoder[Unit] = (_: Unit) => Chain.empty
  implicit val encodeBoolean: FormEncoder[Boolean] = encodeString.contramap(_.toString)
  implicit val encodeByte: FormEncoder[Byte] = encodeString.contramap(_.toString)
  implicit val encodeShort: FormEncoder[Short] = encodeString.contramap(_.toString)
  implicit val encodeInt: FormEncoder[Int] = encodeString.contramap(_.toString)
  implicit val encodeLong: FormEncoder[Long] = encodeString.contramap(_.toString)
  implicit val encodeFloat: FormEncoder[Float] = encodeString.contramap(_.toString)
  implicit val encodeDouble: FormEncoder[Double] = encodeString.contramap(_.toString)
  implicit val encodeBigInt: FormEncoder[BigInt] = encodeString.contramap(_.toString)
  implicit val encodeBigDecimal: FormEncoder[BigDecimal] = encodeString.contramap(_.toString)
  implicit val encodeChar: FormEncoder[Char] = encodeString.contramap(_.toString)
  implicit val encodeSymbol: FormEncoder[Symbol] = encodeString.contramap(_.toString)
  implicit val encodeUUID: FormEncoder[UUID] = encodeString.contramap(_.toString)

  implicit def encodeTraverse[F[_]: Traverse, T](implicit ev: FormEncoder[T]): FormEncoder[F[T]] = (value: F[T]) => {
    Traverse[F].zipWithIndex(value).foldMap { case (v, i) =>
      ev.chained(v).map { case (prefix, v) =>
        prefix.prepend(i.toString) -> v
      }
    }
  }

  implicit val encodeHNil: FormEncoder[HNil] = (_: HNil) => Chain.empty

  implicit def encodeHList[K <: Symbol, V, T <: HList](implicit
    witness: Witness.Aux[K],
    headEncodeForm: Lazy[FormEncoder[V]],
    tailEncodeForm: FormEncoder[T]
  ): FormEncoder[FieldType[K, V] :: T] = (value: FieldType[K, V] :: T) => {
    val nested = headEncodeForm.value.chained(value.head)
    nested.map { case (prefix, v) =>
      prefix.prepend(witness.value.name) -> v
    } ++ tailEncodeForm.chained(value.tail)
  }

  implicit def encodeGeneric[T, R](implicit
    gen: LabelledGeneric.Aux[T, R],
    hEncodeForm: Lazy[FormEncoder[R]]
  ): FormEncoder[T] = (value: T) => hEncodeForm.value.chained(gen.to(value))
}

object EncoderInstances extends EncoderInstances
