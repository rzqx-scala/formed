package io.zenro.formed

import cats.data.Chain
import cats.effect.IO

import weaver.SimpleIOSuite

import implicits._
import syntax._

object EncoderSpec extends SimpleIOSuite {
  test("Should encode basic") {
    final case class C(a: String, b: Int)
    val instance = C("a", 1)

    IO {
      expect {
        instance.asFormData == Chain(("a", "a"), ("b", "1"))
      }
    }
  }

  test("Should encode nested") {
    final case class Inner(v: String)
    final case class C(a: String, b: Inner)
    val instance = C("a", Inner("b"))

    IO {
      expect {
        instance.asFormData == Chain(("a", "a"), ("b[v]", "b"))
      }
    }
  }

  test("Should encode nested traversable") {
    final case class Inner(v: String)
    final case class C(a: String, b: List[Inner])
    val instance = C("a", List(Inner("b"), Inner("c")))

    IO {
      expect {
        instance.asFormData == Chain(("a", "a"), ("b[0][v]", "b"), ("b[1][v]", "c"))
      }
    }
  }

  test("Should encode list") {
    final case class C(a: List[Int])
    val instance = C(List(1, 2, 3))

    IO {
      expect {
        instance.asFormData == Chain(("a[0]", "1"), ("a[1]", "2"), ("a[2]", "3"))
      }
    }
  }
}
