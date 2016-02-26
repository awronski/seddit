package models

sealed trait Sex

object Sex {

  case object Male extends Sex
  case object Female extends Sex

  def valueOf(value: Char): Sex = value match {
    case 'm' => Male
    case 'f' => Female
    case _ => throw new IllegalArgumentException()
  }

  def charOf(value: Sex): Char = value match {
    case Male => 'm'
    case Female => 'f'
    case _ => throw new IllegalArgumentException()
  }

}
