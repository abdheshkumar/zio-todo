import org.scalacheck.{Arbitrary, Gen, Prop}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{Assertion, Succeeded}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.language.implicitConversions

class PropertyTestSpec
    extends AnyFlatSpec
    with Matchers
    with ScalaCheckPropertyChecks {

  implicit val arbString: Arbitrary[String] = Arbitrary(
    Gen.alphaStr.suchThat(_.nonEmpty)
  )
  it should "generate random data" in new ScalaCheckPropertyChecks {
    forAll { (s: String) =>
      println(s)
      s shouldBe s
    }
  }

  it should "generate list of random data" in {
    forAll { (s: List[String]) =>
      s shouldBe s
    }
  }

  it should "without GeneratorDrivenPropertyChecks " in {
    implicit def assertion(as: Assertion): Prop = Prop(as == Succeeded)

    Prop.forAll { (s: List[String]) =>
      println(s)
      s shouldBe s
    }
  }

  it should "check case class random data for non-negative" in {
    case class Person(name: String, age: Int)
    def doSomething(p: Person) = {
      val _ = p
      true
    }

    val people = Gen.resultOf(Person)
    forAll(people) { p =>
      whenever(p.age >= 0) {
        doSomething(p)
      }
    }
  }

  it should "Generate data for case class using GeneratorDrivenPropertyChecks" in {
    case class Test(a: String, b: Int)
    implicit val test: Arbitrary[Test] = Arbitrary(Gen.resultOf(Test))

    forAll { (t: Test) =>
      t.a shouldBe t.a
    }
  }

}
