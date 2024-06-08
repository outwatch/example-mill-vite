package backend

import cats.data.{Kleisli, OptionT}

case class AppConfig(
  // authnUrl: String,
  frontendDistributionPath: String
)

object AppConfig {
  def fromEnv(): AppConfig = AppConfig(
    // authnUrl = sys.env("AUTHN_URL"),
    frontendDistributionPath = sys.env("FRONTEND_DISTRIBUTION_PATH")
  )
}
