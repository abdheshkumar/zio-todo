import org.scalacheck.{Arbitrary, Gen}

class ScalaCheckTesting {
  // strGen generates a fixed length random string
  val strGen = (n: Int) => Gen.listOfN(n, Gen.alphaChar).map(_.mkString)
  case class Account(accountId: String, balance: Double, country: String)

  case class Customer(
      customerId: String,
      name: String,
      nationality: String,
      accounts: Seq[Account]
  )

  // Account generator
  val genAccount = for {
    accountId <- Gen.identifier
    balance <- Arbitrary.arbitrary[Double]
    country <- Gen.oneOf("NL", "BE", "LU")
  } yield Account(accountId, balance, country)

  // Forcing customers to be Dutch will be as easy as:
  val genDutchCustomer = for {
    customerId <- Gen.identifier
    name <- Arbitrary.arbitrary[String].suchThat(_.nonEmpty)
    nationality <- Gen.const("NL")
    accounts <- Gen.nonEmptyListOf(genAccount)
  } yield Customer(customerId, name, nationality, accounts)

  val genIntList: Gen[List[Int]] =
    Gen.containerOf[List, Int](Gen.oneOf(1, 3, 5))

  val genBoolArray: Gen[Array[Boolean]] = Gen.containerOf[Array, Boolean](true)

  val emailGen = for {
    firstName <- Gen.alphaStr
    lastName <- Gen.alphaStr
    email <- Gen.alphaStr
    emailDomain <- Gen.oneOf("expediagroup.com", "gmail.com")
  } yield s"$firstName,$lastName,$email@$emailDomain"

  val csvData = Gen.listOfN(5, emailGen)


  println(csvData.sample.toList.flatten.mkString("\n"))

}
